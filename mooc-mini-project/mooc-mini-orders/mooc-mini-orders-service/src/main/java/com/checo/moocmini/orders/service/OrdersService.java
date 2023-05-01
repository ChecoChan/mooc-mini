package com.checo.moocmini.orders.service;

import com.checo.moocmini.messagesdk.model.po.MqMessage;
import com.checo.moocmini.orders.model.dto.AddOrderDto;
import com.checo.moocmini.orders.model.dto.PayRecordDto;
import com.checo.moocmini.orders.model.dto.PayStatusDto;
import com.checo.moocmini.orders.model.po.MoocminiPayRecord;

public interface OrdersService {

    /**
     * 创建商品订单
     *
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付记录(包括二维码)
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付记录
     *
     * @param payNo 交易记录号
     * @return com.checo.moocmini.orders.model.po.MoocminiPayRecord
     */
    MoocminiPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付宝支付结果
     *
     * @param payStatusDto 支付结果信息
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     */
    void notifyPayResult(MqMessage message);


}
