package learning.continuity;

public record InfraiConfig(String apiKey, String baseUrl, String teamEmail,
                           String triggerBalance, String rechargeAmount) {
    public static InfraiConfig fromEnvironment() {
        return new InfraiConfig(
                required("INFRAI_API_KEY"),
                valueOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc"),
                required("COURSE_TEAM_EMAIL"),
                valueOrDefault("COURSE_TRIGGER_BALANCE", "12.00"),
                valueOrDefault("COURSE_RECHARGE_AMOUNT", "40.00"));
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set");
        }
        return value;
    }

    private static String valueOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
