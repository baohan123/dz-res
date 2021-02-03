package com.dz.dzim.listener;

import com.alibaba.fastjson.JSON;
import com.dz.dzim.mapper.ChatRecordMapper;
import com.dz.dzim.mapper.MeetingActorDao;
import com.dz.dzim.mapper.MeetingChattingDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.doman.MeetingActorEntity;
import com.dz.dzim.pojo.doman.MeetingChattingEntity;
import com.dz.dzim.pojo.doman.MeetingPlazaEntity;
import com.dz.dzim.pojo.doman.MsgRecordsEntity;
import com.dz.dzim.utils.ObjectUtis;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.DefaultConsumer;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.BatchMessageListener;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import javax.annotation.Resource;


/**
 * @author
 */
@Component
public class RabbitMQListener  /*implements BatchMessageListener */{

    @Autowired
    private MeetingChattingDao meetingChattingDao;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Autowired
    MeetingActorDao meetingActorDao;

    @Autowired
    MeetingPlazaDao meetingPlazaDao;



    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
    @RabbitListener(queues = "imageQueue")
    public void onMessage(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws Exception {

        try {
            if (StringUtils.isNotBlank(msg)){

                MeetingChattingEntity meetingChattingEntity = JSON.parseObject(msg, MeetingChattingEntity.class);

                MeetingActorEntity meetingActorEntity = JSON.parseObject(msg, MeetingActorEntity.class);

                MeetingPlazaEntity meetingPlazaEntity = JSON.parseObject(msg, MeetingPlazaEntity.class);

                if (!ObjectUtis.isAllFieldNull(meetingChattingEntity)){
                    meetingChattingDao.insert((MeetingChattingEntity) meetingChattingEntity);
                }
                if (!ObjectUtis.isAllFieldNull(meetingActorEntity)){
                    meetingActorDao.insert((MeetingActorEntity) meetingActorEntity);
                }
                if (!ObjectUtis.isAllFieldNull(meetingPlazaEntity)){

                    int insert = meetingPlazaDao.insert(meetingPlazaEntity);
                }

            }

            //进行消息签收
            channel.basicAck(deliveryTag, true);

        } catch (Exception e) {
             /*
             拒绝签收
            第三个参数：requeue：重回队列。如果设置为true，则消息重新回到queue，broker会重新发送该消息给消费端
             */
            channel.basicNack(deliveryTag, true, true);

        }
    }






}
