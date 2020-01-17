package com.hovvyoung.payment.service.impl;

import com.hovvyoung.payment.PaymentApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceTest extends PaymentApplicationTests {
    @Autowired
    private PayService payService;

    @Test
    public void create() {
            // or new BigDecimal("0.01")
            payService.create("12345674941960", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }
}