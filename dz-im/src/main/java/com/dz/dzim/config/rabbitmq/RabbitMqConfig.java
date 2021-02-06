package com.dz.dzim.config.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;


/**
 * @author
 */


@Configuration
public class RabbitMqConfig {


    Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);

    //定义交换机的名字
    public static final String EXCHANGE_NAME = "im-exchange";
    //定义队列的名字
    public static final String QUEUE_NAME = "queue";
    public static final String QUEUE_NAME1 = "meeting_chatting";
    public static final String QUEUE_NAME2 = "meeting_actor";
    public static final String QUEUE_NAME3 = "meeting_plaza";
    public static final String QUEUE_NAME4 = "meeting";
    //路由key
    public static final String KEY1="msg1";
    public static final String KEY2="msg2";
    public static final String KEY3="msg3";
    public static final String KEY4="msg4";

    private static String dlxExchangeName ="dlx-exch";
    private static String dlxQueueName ="dlx-que";
    private static String dlxRoutingKey ="dlx-key";

    private static Map<String,Object> dlxMap;


    //声明交换机
    @Bean("bootExchange")
    public Exchange bootExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }


    @Bean("bootQueue1")
    public Queue bootQueue1() {

        return QueueBuilder.durable(QUEUE_NAME1).build();
    }

    @Bean("bootQueue2")
    public Queue bootQueue2() {
        return QueueBuilder.durable(QUEUE_NAME2).build();
    }

    @Bean("bootQueue3")
    public Queue bootQueue3() {
        return QueueBuilder.durable(QUEUE_NAME3).build();
    }

    @Bean("bootQueue4")
    public Queue bootQueue4() {
        return QueueBuilder.durable(QUEUE_NAME4).build();
    }

    @Bean
    public BindingBuilder.GenericArgumentsConfigurer bindQueueExchange1(@Qualifier("bootQueue1") Queue queue, @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY1);

    }

    @Bean
    public BindingBuilder.GenericArgumentsConfigurer bindQueueExchange2(@Qualifier("bootQueue2") Queue queue, @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY2);

    }

    @Bean
    public BindingBuilder.GenericArgumentsConfigurer bindQueueExchange3(@Qualifier("bootQueue3") Queue queue, @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY3);

    }

    @Bean
    public BindingBuilder.GenericArgumentsConfigurer bindQueueExchange4(@Qualifier("bootQueue4") Queue queue, @Qualifier("bootExchange") Exchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(KEY4);

    }


    /**
     * 设置返回回调和确认回调
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate();

        rabbitTemplate.setConnectionFactory(connectionFactory);

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //ack 为 true表示 消息已经到达交换机
                if (ack) {
                    //接收成功
                    logger.info("消息发送到交换机成功" );
                } else {
                    //接收失败
                    // 根据本地消息的状态为失败，可以用定时任务去处理数据
                    logger.info("消息发送到交换机失败 ====>:" + cause);
                }
            }
        });

        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             *
             * @param message   消息对象
             * @param replyCode 错误码
             * @param replyText 错误信息
             * @param exchange  交换机
             * @param routingKey 路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                String msgId = message.getMessageProperties().getCorrelationId();
                String data = new String(message.getBody());
                logger.error("消息发送到指定队列失败-消息回退，应答码：{}，原因：{}，交换机：{}，路由键：{}", replyCode, replyText, exchange, routingKey);
                logger.error("msgId==>"+msgId);
                logger.error("data==>"+data);
                //进行消息重新发送
                // rabbitTemplate.convertAndSend(exchange, routingKey, message);
            }
        });


        return rabbitTemplate;
    }


}