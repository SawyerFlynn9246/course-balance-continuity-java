package learning.continuity;

import java.math.BigDecimal;

public final class CourseContinuityApp {
    public static void main(String[] args) {
        InfraiConfig config = InfraiConfig.fromEnvironment();
        CourseContinuityService service = new CourseContinuityService(new InfraiHttpGateway(config));
        CourseContinuityService.ContinuityResult result = service.protectLessonFlow(
                config.teamEmail(), new BigDecimal(config.triggerBalance()), new BigDecimal(config.rechargeAmount()));
        if (result.rechargeConfigured()) {
            System.out.println("recharge configured; notification message_id=" + result.messageId());
        } else {
            System.out.println("balance is sufficient: " + result.balance().toPlainString());
        }
    }
}
