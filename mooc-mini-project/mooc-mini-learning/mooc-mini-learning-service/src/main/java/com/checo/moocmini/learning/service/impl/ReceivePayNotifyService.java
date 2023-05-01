package com.checo.moocmini.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.checo.moocmini.base.exception.MoocMiniException;
import com.checo.moocmini.learning.config.PayNotifyConfig;
import com.checo.moocmini.learning.service.MyCourseTablesService;
import com.checo.moocmini.messagesdk.model.po.MqMessage;
import com.checo.moocmini.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    // 监听消息队列接收支付结果通知
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message, Channel channel) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 获取消息
        MqMessage mqMessage = JSON.parseObject(message.getBody(), MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);

        // 消息类型
        String messageType = mqMessage.getMessageType();
        // 订单类型，60201 表示购买课程
        String orderType = mqMessage.getBusinessKey2();
        // 这里只处理支付结果通知
        if (PayNotifyConfig.MESSAGE_TYPE.equals(messageType) && orderType.equals("60201")) {
            // 选课记录id
            String choosecourseId = mqMessage.getBusinessKey1();
            // 添加选课
            boolean b = myCourseTablesService.saveChooseCourseSuccess(choosecourseId);
            if (!b) {
                // 添加选课失败，抛出异常，消息重回队列
                MoocMiniException.castException("收到支付结果，添加选课失败");
            }
        }
    }

}
