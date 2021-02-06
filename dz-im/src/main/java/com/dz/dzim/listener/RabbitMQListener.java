package com.dz.dzim.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dz.dzim.mapper.*;
import com.dz.dzim.pojo.doman.*;
import com.dz.dzim.utils.ObjectUtis;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author
 */
@Component
public class RabbitMQListener {

    @Autowired
    private MeetingChattingDao meetingChattingDao;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Autowired
    MeetingActorDao meetingActorDao;

    @Autowired
    MeetingPlazaDao meetingPlazaDao;

    @Autowired
    MeetingDao meetingDao;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static AtomicInteger atomicInteger = new AtomicInteger(1);


    @Autowired
    FailMessageMapper failMessageMapper;

    /*异常处理方法*/
    private void excepHandler(@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel, IOException e, String msg) throws Exception {

        e.printStackTrace();

        if (atomicInteger.get() > 5) {
            System.out.println("重试多次后消息最终消费失败！");
            channel.basicAck(deliveryTag, true);
            /*失败消息入库或者发邮件
            需要根据msg的不同类型，调用不同的mapper接口进行插入操作，实现失败消息入库操作
            * */
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = simpleDateFormat.format(new Date());
            Timestamp timestamp = Timestamp.valueOf(format);
            failMessageMapper.insert(new FailMessage(msg,timestamp));
            /*重置计数器*/
            atomicInteger.set(1);
        } else {
            System.out.println("重试第 " + atomicInteger + " 次！");
            channel.basicNack(deliveryTag, true, true);
            atomicInteger.getAndIncrement();
        }
    }


    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
    @RabbitListener(queues = "meeting_chatting")
    public void onMessage(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws Exception{

        try {
            if (StringUtils.isNotBlank(msg)){

                JSONObject jsonObject = JSONObject.parseObject(msg);
                String type = (String) jsonObject.getString("type");
                String obj = jsonObject.getString("obj");

                MeetingChattingEntity meetingChattingEntity = JSON.parseObject(obj, MeetingChattingEntity.class);

                if (StringUtils.equals("insert",type)){

                    if (!ObjectUtis.isAllFieldNull(meetingChattingEntity)){
                        meetingChattingDao.insert( meetingChattingEntity);
                    }
                }
                if (StringUtils.equals("update",type)){

                    if (!ObjectUtis.isAllFieldNull(meetingChattingEntity)){
                        meetingChattingDao.updateById(meetingChattingEntity);
                    }
                }
            }

            channel.basicAck(deliveryTag, true);
            logger.info("收到了消息ID: " + deliveryTag + "," + "消息内容" + msg);

        } catch (IOException e){
            excepHandler(deliveryTag, channel, e, msg);
        }
    }

    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
    @RabbitListener(queues = "meeting_actor")
    public void onMessages(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws Exception {
        try {
            if (StringUtils.isNotBlank(msg)){

                JSONObject jsonObject = JSONObject.parseObject(msg);

                String type = (String) jsonObject.getString("type");

                String obj = jsonObject.getString("obj");

                MeetingActorEntity meetingActorEntity =  JSON.parseObject(obj, MeetingActorEntity.class);
                UpdateWrapper<MeetingActorEntity> updateWrapper = JSON.parseObject(obj, UpdateWrapper.class);

                if (StringUtils.equals("insert",type)){
                    if (!ObjectUtis.isAllFieldNull(meetingActorEntity)){
                        meetingActorDao.insert(meetingActorEntity);
                    }
                    if (StringUtils.equals("update",type)){
                        if (!ObjectUtis.isAllFieldNull(meetingActorEntity)){
                            meetingActorDao.updateById(meetingActorEntity);
                        }
                        if (!ObjectUtis.isAllFieldNull(updateWrapper)){
                            meetingActorDao.update(null,updateWrapper);
                        }
                    }
                }
            }
            channel.basicAck(deliveryTag, true);
            logger.info("收到了消息ID: " + deliveryTag + "," + "消息内容" + msg);
        } catch (IOException e) {
            excepHandler(deliveryTag, channel, e, msg);
        }
    }


    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
    @RabbitListener(queues = "meeting_plaza")
    public void onMessage3(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws Exception {

        try {
            if (StringUtils.isNotBlank(msg)){

                JSONObject jsonObject = JSONObject.parseObject(msg);

                String type = (String) jsonObject.getString("type");

                String obj = jsonObject.getString("obj");

                MeetingPlazaEntity meetingPlazaEntity = JSON.parseObject(obj,MeetingPlazaEntity.class);

                if (StringUtils.equals("insert",type)){
                    if (!ObjectUtis.isAllFieldNull(meetingPlazaEntity)){
                        meetingPlazaDao.insert(meetingPlazaEntity);
                    }
                }
                if (StringUtils.equals("update",type)){
                    if (!ObjectUtis.isAllFieldNull(meetingPlazaEntity)){
                        meetingPlazaDao.updateById(meetingPlazaEntity);
                    }
                }
            }
            channel.basicAck(deliveryTag, true);
            logger.info("收到了消息ID: " + deliveryTag + "," + "消息内容" + msg);
        } catch (IOException e) {
            excepHandler(deliveryTag, channel, e, msg);
        }
    }


    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
    @RabbitListener(queues = "meeting")
    public void onMessage4(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws Exception {

        try {
            if (StringUtils.isNotBlank(msg)){

                JSONObject jsonObject = JSONObject.parseObject(msg);

                String type = (String) jsonObject.getString("type");

                String obj = jsonObject.getString("obj");

                MeetingEntity meetingEntity = JSON.parseObject(obj,MeetingEntity.class);

                if (StringUtils.equals("insert",type)){
                    if (!ObjectUtis.isAllFieldNull(meetingEntity)){
                        meetingDao.insert(meetingEntity);
                    }
                }
                if (StringUtils.equals("update",type)){
                    if (!ObjectUtis.isAllFieldNull(meetingEntity)){
                        meetingDao.updateById(meetingEntity);
                    }
                }
            }
            channel.basicAck(deliveryTag, true);
            logger.info("收到了消息ID: " + deliveryTag + "," + "消息内容" + msg);
        } catch (IOException e) {
            excepHandler(deliveryTag, channel, e,msg);
        }
    }

}
