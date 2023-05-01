package com.checo.moocmini.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.base.utils.IdWorkerUtils;
import com.checo.moocmini.base.utils.QRCodeUtil;
import com.checo.moocmini.messagesdk.model.po.MqMessage;
import com.checo.moocmini.messagesdk.service.MqMessageService;
import com.checo.moocmini.orders.config.AlipayConfig;
import com.checo.moocmini.orders.config.PayNotifyConfig;
import com.checo.moocmini.orders.mapper.MoocminiOrdersGoodsMapper;
import com.checo.moocmini.orders.mapper.MoocminiOrdersMapper;
import com.checo.moocmini.orders.mapper.MoocminiPayRecordMapper;
import com.checo.moocmini.orders.model.dto.AddOrderDto;
import com.checo.moocmini.orders.model.dto.PayRecordDto;
import com.checo.moocmini.orders.model.dto.PayStatusDto;
import com.checo.moocmini.orders.model.po.MoocminiOrders;
import com.checo.moocmini.orders.model.po.MoocminiOrdersGoods;
import com.checo.moocmini.orders.model.po.MoocminiPayRecord;
import com.checo.moocmini.orders.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private MoocminiOrdersMapper moocminiOrdersMapper;

    @Autowired
    private MoocminiOrdersGoodsMapper moocminiOrdersGoodsMapper;

    @Autowired
    private MoocminiPayRecordMapper payRecordMapper;

    @Autowired
    private OrdersServiceImpl currentProxy;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Value("${pay.qrcodeurl}")
    private String qrcodeurl;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    @Override
    @Transactional
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 添加商品订单
        MoocminiOrders moocminiOrders = saveMoocminiOrders(userId, addOrderDto);
        if (moocminiOrders == null)
            MoocMiniException.castException("订单创建失败");

        // 添加支付记录
        MoocminiPayRecord payRecord = createPayRecord(moocminiOrders);

        // 生成二维码
        String qrCode = null;
        try {
            // url 要可以被模拟器访问到，url 为下单接口
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            MoocMiniException.castException("生成二维码出错");
        }

        // 返回所需数据
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    /**
     * 添加商品订单
     */
    public MoocminiOrders saveMoocminiOrders(String userId, AddOrderDto addOrderDto) {
        // 幂等性处理
        MoocminiOrders moocminiOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (moocminiOrders != null) {
            return moocminiOrders;
        }
        moocminiOrders = new MoocminiOrders();
        // 生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();
        moocminiOrders.setId(orderId);
        moocminiOrders.setTotalPrice(addOrderDto.getTotalPrice());
        moocminiOrders.setCreateDate(LocalDateTime.now());
        moocminiOrders.setStatus("600001"); // 未支付
        moocminiOrders.setUserId(userId);
        moocminiOrders.setOrderType(addOrderDto.getOrderType());
        moocminiOrders.setOrderName(addOrderDto.getOrderName());
        moocminiOrders.setOrderDetail(addOrderDto.getOrderDetail());
        moocminiOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        moocminiOrders.setOutBusinessId(addOrderDto.getOutBusinessId()); // 选课记录 id
        moocminiOrdersMapper.insert(moocminiOrders);
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<MoocminiOrdersGoods> moocminiOrdersGoodsList = JSON.parseArray(orderDetailJson, MoocminiOrdersGoods.class);
        moocminiOrdersGoodsList.forEach(goods -> {
            MoocminiOrdersGoods moocminiOrdersGoods = new MoocminiOrdersGoods();
            BeanUtils.copyProperties(goods, moocminiOrdersGoods);
            moocminiOrdersGoods.setOrderId(orderId);//订单号
            moocminiOrdersGoodsMapper.insert(moocminiOrdersGoods);
        });
        return moocminiOrders;
    }

    // 根据业务id查询订单
    private MoocminiOrders getOrderByBusinessId(String businessId) {
        return moocminiOrdersMapper.selectOne(new LambdaQueryWrapper<MoocminiOrders>().eq(MoocminiOrders::getOutBusinessId, businessId));
    }

    /**
     * 添加支付记录
     */
    public MoocminiPayRecord createPayRecord(MoocminiOrders moocminiOrders) {
        if (moocminiOrders == null)
            MoocMiniException.castException("订单不存在");
        if (moocminiOrders.getStatus().equals("600002"))
            MoocMiniException.castException("订单已支付");

        MoocminiPayRecord payRecord = new MoocminiPayRecord();
        // 生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(moocminiOrders.getId()); // 商品订单号
        payRecord.setOrderName(moocminiOrders.getOrderName());
        payRecord.setTotalPrice(moocminiOrders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001"); // 未支付
        payRecord.setUserId(moocminiOrders.getUserId());
        payRecordMapper.insert(payRecord);
        return payRecord;
    }

    @Override
    public MoocminiPayRecord getPayRecordByPayno(String payNo) {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<MoocminiPayRecord>().eq(MoocminiPayRecord::getPayNo, payNo));
    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        MoocminiPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null)
            MoocMiniException.castException("请重新点击支付获取二维码");

        // 支付状态
        String status = payRecord.getStatus();
        // 如果支付成功直接返回
        if ("601002".equals(status)) {
            PayRecordDto payRecordDto = new PayRecordDto();
            BeanUtils.copyProperties(payRecord, payRecordDto);
            return payRecordDto;
        }
        // 从支付宝查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        // 保存支付结果
        currentProxy.saveAliPayStatus(payStatusDto);
        // 重新查询支付记录
        payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        return payRecordDto;
    }

    /**
     * 请求支付宝查询支付结果
     *
     * @param payNo 支付交易号
     * @return 支付结果
     */
    private PayStatusDto queryPayResultFromAlipay(String payNo) {
        // ========请求支付宝查询支付结果=============
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess())
                MoocMiniException.castException("请求支付查询查询失败");

        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}", e.toString(), e);
            MoocMiniException.castException("请求支付查询查询失败");
        }

        //获取支付结果
        String resultJson = response.getBody();
        //转map
        Map resultMap = JSON.parseObject(resultJson, Map.class);
        Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
        //支付结果
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        //保存支付结果
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTotal_amount(total_amount);
        return payStatusDto;
    }

    @Override
    @Transactional
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        // 支付流水号
        String payNo = payStatusDto.getOut_trade_no();
        MoocminiPayRecord payRecord = getPayRecordByPayno(payNo);
        if (payRecord == null)
            MoocMiniException.castException("支付记录找不到");

        // 支付结果
        String trade_status = payStatusDto.getTrade_status();
        log.debug("收到支付结果:{},支付记录:{}}", payStatusDto.toString(), payRecord.toString());
        if (trade_status.equals("TRADE_SUCCESS")) {

            // 支付金额变为分
            Float totalPrice = payRecord.getTotalPrice() * 100;
            Float total_amount = Float.parseFloat(payStatusDto.getTotal_amount()) * 100;
            // 校验是否一致
            if (totalPrice.intValue() != total_amount.intValue()) {
                // 校验失败
                log.info("校验支付结果失败,支付记录:{},APP_ID:{},totalPrice:{}", payRecord.toString(), payStatusDto.getApp_id(), total_amount.intValue());
                MoocMiniException.castException("校验支付结果失败");
            }
            log.debug("更新支付结果,支付交易流水号:{},支付结果:{}", payNo, trade_status);
            MoocminiPayRecord payRecord_u = new MoocminiPayRecord();
            payRecord_u.setStatus("601002");//支付成功
            payRecord_u.setOutPayChannel("Alipay");
            payRecord_u.setOutPayNo(payStatusDto.getTrade_no()); // 支付宝交易号
            payRecord_u.setPaySuccessTime(LocalDateTime.now()); // 通知时间
            int update1 = payRecordMapper.update(payRecord_u, new LambdaQueryWrapper<MoocminiPayRecord>().eq(MoocminiPayRecord::getPayNo, payNo));
            if (update1 > 0) {
                log.info("更新支付记录状态成功:{}", payRecord_u.toString());
            } else {
                log.info("更新支付记录状态失败:{}", payRecord_u.toString());
                MoocMiniException.castException("更新支付记录状态失败");
            }
            // 关联的订单号
            Long orderId = payRecord.getOrderId();
            MoocminiOrders moocminiOrders = moocminiOrdersMapper.selectById(orderId);
            if (moocminiOrders == null) {
                log.info("根据支付记录[{}}]找不到订单", payRecord_u.toString());
                MoocMiniException.castException("根据支付记录找不到订单");
            }
            MoocminiOrders order_u = new MoocminiOrders();
            order_u.setStatus("600002");//支付成功
            int update = moocminiOrdersMapper.update(order_u, new LambdaQueryWrapper<MoocminiOrders>().eq(MoocminiOrders::getId, orderId));
            if (update > 0) {
                log.info("更新订单表状态成功,订单号:{}", orderId);
            } else {
                log.info("更新订单表状态失败,订单号:{}", orderId);
                MoocMiniException.castException("更新订单表状态失败");
            }

            // 保存消息记录,参数1：支付结果通知类型，2: 业务id，3:业务类型
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", moocminiOrders.getOutBusinessId(), moocminiOrders.getOrderType(), null);
            // 通知消息
            notifyPayResult(mqMessage);
        }
    }

    @Override
    public void notifyPayResult(MqMessage message) {
        // 1.消息体转 JSON
        String msg = JSON.toJSONString(message);
        // 设置消息持久化
        Message msgObj = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();
        // 2.全局唯一的消息 ID，需要封装到 CorrelationData 中
        CorrelationData correlationData = new CorrelationData(message.getId().toString());
        // 3.添加 callback
        correlationData.getFuture().addCallback(
                result -> {
                    if (result.isAck()) {
                        // 3.1.ack，消息成功
                        log.debug("通知支付结果消息发送成功, ID:{}", correlationData.getId());
                        // 删除消息表中的记录
                        mqMessageService.completed(message.getId());
                    } else {
                        // 3.2.nack，消息失败
                        log.error("通知支付结果消息发送失败, ID:{}, 原因{}", correlationData.getId(), result.getReason());
                    }
                },
                ex -> log.error("消息发送异常, ID:{}, 原因{}", correlationData.getId(), ex.getMessage())
        );
        // 发送消息
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", msgObj, correlationData);
    }

}
