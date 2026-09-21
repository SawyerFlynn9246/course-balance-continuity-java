package learning.continuity;

import java.math.BigDecimal;

public final class CourseContinuityServiceTest {
    public static void main(String[] args) {
        RecordingGateway gateway = new RecordingGateway(new BigDecimal("8.00"));
        CourseContinuityService.ContinuityResult result = new CourseContinuityService(gateway)
                .protectLessonFlow("instructor@example.edu", new BigDecimal("12.00"), new BigDecimal("40.00"));
        if (!result.rechargeConfigured() || gateway.configurations != 1 || gateway.notifications != 1
                || !"lesson-message-17".equals(result.messageId())) {
            throw new AssertionError("Low balance must configure recharge and notify the course team");
        }
        System.out.println("PASS low balance configures recharge and sends one update");
    }

    private static final class RecordingGateway implements InfraiGateway {
        private final BigDecimal balance;
        int configurations;
        int notifications;

        private RecordingGateway(BigDecimal balance) { this.balance = balance; }
        public BigDecimal accountBalance() { return balance; }
        public void configureAutoRecharge(BigDecimal trigger, BigDecimal amount) { configurations++; }
        public String sendTeacherUpdate(String to, String text) { notifications++; return "lesson-message-17"; }
    }
}
