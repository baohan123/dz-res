//package com.dz.dzim.mq;
//
//import com.rabbitmq.client.Channel;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//public class MqReceiver implements ChannelAwareMessageListener {
//    private static Logger logger = LoggerFactory.getLogger(MqReceiver.class);
//
//    @Autowired
//    private MqSocket mqSocket;
//
//    @Override
//    public void onMessage(Message message, Channel channel) throws Exception {
//        byte[] body = message.getBody();
//        logger.info("接收到消息:" + new String(body));
//        try {
//            mqSocket.sendMessage(new String(body));
//        } catch (IOException e) {
//            logger.error("send rabbitmq message error", e);
//        }
//    }
//
//}
