package com.hovvyoung.payment.service.impl;

import com.hovvyoung.payment.dao.PayInfoMapper;
import com.hovvyoung.payment.enums.PayPlatformEnum;
import com.hovvyoung.payment.pojo.PayInfo;
import com.hovvyoung.payment.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
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
public class PayServiceImpl implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {

        PayRequest payRequest = new PayRequest();
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        payRequest.setOrderId(orderId);
        payRequest.setOrderName("6556518-最好的支付sdk");
        payRequest.setOrderAmount(amount.doubleValue());
//        payRequest.setOpenid("openid_xxxxxx");

        // write to db;
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("create payResponse={}", payResponse);

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
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        // query get null is severe situation. Should send warning to phone;
        if (payInfo == null) {
            throw new RuntimeException("get null from query 'orderNo' ");
        }
        //if not "pay success"
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {

            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
                throw new RuntimeException("asynNotify(): unconsistent pay amounts with database，orderNo=" + payResponse.getOrderId());
            }

            //3. modify order status to success
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            // 1 .payInfo.setUpdateTime(null);
            // 2. (I use this way) delete in mapper: <if test="updateTime != null">
            //        update_time = #{updateTime,jdbcType=TIMESTAMP},</if>
            // To achieve update_time always managed by mysql
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO payment send a MQ message, then mall receive the message.

        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //4. return response to wxPay not to notify anymore;
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }

        throw new RuntimeException("asynNotify: Wrong payment platform");
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
