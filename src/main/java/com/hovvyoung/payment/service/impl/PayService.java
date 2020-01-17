package com.hovvyoung.payment.service.impl;

import com.hovvyoung.payment.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/*
* use best-pay-sdk
* */

@Slf4j
@Service
public class PayService implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {

        PayRequest payRequest = new PayRequest();
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        payRequest.setOrderId(orderId);
        payRequest.setOrderName("6556518-最好的支付sdk");
        payRequest.setOrderAmount(amount.doubleValue());
//        payRequest.setOpenid("openid_xxxxxx");

        // write to db;
        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("response={}", payResponse);

        return payResponse;
    }

    /*
    * handle async notification*/
    @Override
    public String asyncNotify(String notifyData) {
        //1. check signature
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("asyncNotify payResponse={}", payResponse);

        //2. check amount of order（from database）

        //3. modify order status

        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //4. return response to wechat not to notify anymore;
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }

        throw new RuntimeException("异步通知中错误的支付平台");
    }
}
