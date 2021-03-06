package com.hovvyoung.payment.service;

import com.hovvyoung.payment.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {

    /**
    * create and start pay;
    */

    PayResponse create (String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /*
    * async notify
    * */
    String asyncNotify(String notifyData);

    /**
     * query payment record by orderId
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);
}
