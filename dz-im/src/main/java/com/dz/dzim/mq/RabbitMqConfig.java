//package com.dz.dzim.mq;
//
////import com.dz.dzim.mq.MqSocket;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * mq消息监听类
// */
//@Configuration
//public class RabbitMqConfig {
//    private static Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);
//
//    @Autowired
//    private RabbitTemplate  rabbitTemplate;
//
//    //定义交换机的名字
//    public static final String  EXCHANGE_NAME = "boot_topic_exchange";
//    //定义队列的名字
//    public static final String QUEUE_NAME = "boot_queue";
//
//    /**
//     * 创建交换器
//     */
//    @Bean
//    FanoutExchange exchange() {
//        return new FanoutExchange(EXCHANGE_NAME);
//    }
//
//    @Bean
//    public Queue queue(){
//        return new Queue(QUEUE_NAME);
//    }
//
//    @Bean
//    Binding bindingExchangeMessage(Queue queue,FanoutExchange exchange) {
//        return BindingBuilder.bind(queue).to(exchange);
//    }
//
//    @Bean
//    public SimpleMessageListenerContainer messageListenerContainer(MqReceiver mqReceiver, @Qualifier("rabbitConnectionFactory") CachingConnectionFactory cachingConnectionFactory){
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cachingConnectionFactory);
//        // 监听队列的名称
//        container.setQueueNames(QUEUE_NAME);
//        container.setExposeListenerChannel(true);
//        // 设置每个消费者获取的最大消息数量
//        container.setPrefetchCount(100);
//        // 消费者的个数
//        container.setConcurrentConsumers(1);
//        // 设置确认模式为自动确认
//        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
//        container.setMessageListener(mqReceiver);
//        return container;
//    }
//
//
//}
