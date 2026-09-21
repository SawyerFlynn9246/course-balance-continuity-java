package learning.continuity;

import java.math.BigDecimal;

public interface InfraiGateway {
    BigDecimal accountBalance() throws InfraiException;
    void configureAutoRecharge(BigDecimal triggerBalance, BigDecimal rechargeAmount) throws InfraiException;
    String sendTeacherUpdate(String to, String text) throws InfraiException;
}
