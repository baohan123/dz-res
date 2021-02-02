package com.dz.dzim.config.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author
 */


@Configuration
public class RabbitMqConfig  {

    @Autowired
    RabbitTemplate rabbitTemplate;

    Logger logger= LoggerFactory.getLogger(RabbitMqConfig.class);

    //定义交换机的名字
    public static final String  EXCHANGE_NAME = "imageExchange";
    //定义队列的名字
    public static final String QUEUE_NAME = "imageQueue";

    //1、声明交换机
    @Bean("bootExchange")
    public Exchange bootExchange(){

        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    //2、声明队列
    @Bean("bootQueue")
    public Queue bootQueue(){

        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    //3、队列与交换机进行绑定
    @Bean
    public Binding bindQueueExchange(@Qualifier("bootQueue") Queue queue, @Qualifier("bootExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("img.#").noargs();
    }

    /**
     * 设置返回回调和确认回调
     *
     * @param connectionFactory
     * @return
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,RabbitMQConfirmAndReturn rabbitMQConfirmAndReturn) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setConfirmCallback(rabbitMQConfirmAndReturn);
        rabbitTemplate.setReturnCallback(rabbitMQConfirmAndReturn);
        //Mandatory为true时,消息通过交换器无法匹配到队列会返回给生产者，为false时匹配不到会直接被丢弃
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

//    @PostConstruct
//    public void initRabbitTemplate(){
//
//        //定义确认机制回调方法
//        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
//            /**
//             *
//             * @param correlationData 相关配置信息
//             * @param ack   exchange交换机 是否成功收到了消息。true 成功，false代表失败
//             * @param cause 失败原因
//             */
//            @Override
//            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                //ack 为  true表示 消息已经到达交换机
//                if (ack) {
//                    //接收成功
//                    logger.info("交换机接收消息成功:" + cause);
//                } else {
//                    //接收失败
//                    logger.info("交换机接收失败消息:" + cause);
//                }
//            }
//        });
//
//        //设置交换机处理失败消息的模式  为true的时候，消息达到不了队列时，会将消息重新返回给生产者
//        rabbitTemplate.setMandatory(true);
//
//        //定义返回机制回调
//        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
//            /**
//             *
//             * @param message   消息对象
//             * @param replyCode 错误码
//             * @param replyText 错误信息
//             * @param exchange  交换机
//             * @param routingKey 路由键
//             */
//            @Override
//            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
//
//                logger.info("交换机转发消息到指定队列失败！！！");
//                //进行消息重新发送
//                rabbitTemplate.convertAndSend(exchange,routingKey,message);
//            }
//        });
//    }


}