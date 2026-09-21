package learning.continuity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiHttpGateway implements InfraiGateway {
    private static final Pattern OK = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)");
    private final HttpClient client;
    private final InfraiConfig config;

    public InfraiHttpGateway(InfraiConfig config) {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), config);
    }

    InfraiHttpGateway(HttpClient client, InfraiConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public BigDecimal accountBalance() {
        String envelope = request("GET", "/v1/account/balance", null);
        return new BigDecimal(requiredString(envelope, "balance"));
    }

    @Override
    public void configureAutoRecharge(BigDecimal triggerBalance, BigDecimal rechargeAmount) {
        String body = "{\"trigger_balance\":" + triggerBalance.toPlainString()
                + ",\"recharge_amount\":" + rechargeAmount.toPlainString() + "}";
        request("PUT", "/v1/account/autorecharge/configure", body);
    }

    @Override
    public String sendTeacherUpdate(String to, String text) {
        String body = "{\"to\":\"" + escape(to) + "\",\"subject\":\"Course balance recharge configured\",\"body\":\""
                + escape(text) + "\"}";
        String envelope = request("POST", "/v1/email/send", body);
        return requiredString(envelope, "message_id");
    }

    private String request(String method, String path, String body) {
        HttpResponse<String> response = sendWithRateLimit(method, path, body);
        String envelope = response.body();
        Matcher ok = OK.matcher(envelope);
        if (!ok.find()) {
            throw new InfraiException(response.statusCode(), "INVALID_ENVELOPE", "Response did not contain an Infrai envelope");
        }
        if (!Boolean.parseBoolean(ok.group(1))) {
            throw new InfraiException(response.statusCode(), optionalString(envelope, "code", "REQUEST_REJECTED"),
                    optionalString(envelope, "message", "Infrai rejected the request"));
        }
        if (response.statusCode() >= 500) {
            throw new InfraiException(response.statusCode(), "TRANSPORT_FAILURE", "Request could not complete");
        }
        return envelope;
    }

    private HttpResponse<String> sendWithRateLimit(String method, String path, String body) {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.baseUrl() + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + config.apiKey())
                    .header("Accept", "application/json");
            if (body != null) {
                builder.header("Content-Type", "application/json");
            }
            HttpRequest request = body == null
                    ? builder.method(method, HttpRequest.BodyPublishers.noBody()).build()
                    : builder.method(method, HttpRequest.BodyPublishers.ofString(body)).build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 429 || attempt == 2) {
                    return response;
                }
                pause(retryDelay(response, attempt));
            } catch (IOException e) {
                throw new InfraiException(0, "TRANSPORT_FAILURE", e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InfraiException(0, "INTERRUPTED", "Request interrupted");
            }
        }
        throw new InfraiException(429, "RATE_LIMITED", "Rate limit retry window ended");
    }

    private static long retryDelay(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> Long.parseLong(value) * 1000L)
                .orElse(250L * (1L << attempt));
    }

    private static void pause(long milliseconds) {
        try { Thread.sleep(milliseconds); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new InfraiException(0, "INTERRUPTED", "Request interrupted"); }
    }

    private static String requiredString(String json, String field) {
        String value = optionalString(json, field, null);
        if (value == null) throw new InfraiException(200, "MISSING_DATA", "Envelope data omitted " + field);
        return value;
    }

    private static String optionalString(String json, String field, String fallback) {
        Pattern fieldPattern = Pattern.compile("\\\"" + Pattern.quote(field) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        Matcher matcher = fieldPattern.matcher(json);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
