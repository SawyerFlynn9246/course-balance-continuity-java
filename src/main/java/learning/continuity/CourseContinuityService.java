package learning.continuity;

import java.math.BigDecimal;

public final class CourseContinuityService {
    private final InfraiGateway infrai;

    public CourseContinuityService(InfraiGateway infrai) {
        this.infrai = infrai;
    }

    public ContinuityResult protectLessonFlow(String teacherEmail, BigDecimal triggerBalance,
                                              BigDecimal rechargeAmount) {
        BigDecimal balance = infrai.accountBalance();
        if (balance.compareTo(triggerBalance) >= 0) {
            return new ContinuityResult(balance, false, null);
        }
        infrai.configureAutoRecharge(triggerBalance, rechargeAmount);
        String messageId = infrai.sendTeacherUpdate(teacherEmail,
                "Recharge configured for course delivery. Balance was " + balance.toPlainString()
                        + "; the threshold is " + triggerBalance.toPlainString() + ".");
        return new ContinuityResult(balance, true, messageId);
    }

    public record ContinuityResult(BigDecimal balance, boolean rechargeConfigured, String messageId) { }
}
