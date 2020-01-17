package com.hovvyoung.payment.service.impl;

import com.hovvyoung.payment.PaymentApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceImplTest extends PaymentApplicationTests {
    @Autowired
    private PayServiceImpl payServiceImpl;

    @Test
    public void create() {
            // or new BigDecimal("0.01")
            payServiceImpl.create("12345674941960", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }
}