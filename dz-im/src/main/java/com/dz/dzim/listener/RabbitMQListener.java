//package com.dz.dzim.listener;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.dz.dzim.mapper.*;
//import com.dz.dzim.pojo.doman.*;
//import com.dz.dzim.utils.ObjectUtis;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.rabbitmq.client.DefaultConsumer;
//import org.apache.commons.lang3.ObjectUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.amqp.core.BatchMessageListener;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Component;
//
//import com.rabbitmq.client.Channel;
//
//import javax.annotation.Resource;
//import java.io.IOException;
//
//
///**
// * @author
// */
//@Component
//public class RabbitMQListener  /*implements BatchMessageListener */{
//
//    @Autowired
//    private MeetingChattingDao meetingChattingDao;
//
//    @Resource
//    RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    MeetingActorDao meetingActorDao;
//
//    @Autowired
//    MeetingPlazaDao meetingPlazaDao;
//
//    @Autowired
//    MeetingDao meetingDao;
//
//    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
//    @RabbitListener(queues = "meeting_chatting")
//    public void onMessage(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException{
//
//        if (StringUtils.isNotBlank(msg)){
//            JSONObject jsonObject = JSONObject.parseObject(msg);
//
//            String type = (String) jsonObject.getString("type");
//
//            String obj = jsonObject.getString("obj");
//
//
//            MeetingChattingEntity meetingChattingEntity = JSON.parseObject(obj, MeetingChattingEntity.class);
//
//            if (StringUtils.equals("insert",type)){
//
//                if (!ObjectUtis.isAllFieldNull(meetingChattingEntity)){
//                    meetingChattingDao.insert( meetingChattingEntity);
//                }
//            }
//            if (StringUtils.equals("update",type)){
//
//                if (!ObjectUtis.isAllFieldNull(meetingChattingEntity)){
//                    meetingChattingDao.updateById(meetingChattingEntity);
//                }
//            }
//        }
//
//        System.out.println("消息{}消费成功:"+msg);
//
//    }
//
//    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
//    @RabbitListener(queues = "meeting_actor")
//    public void onMessages(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
//
//
//            if (StringUtils.isNotBlank(msg)){
//
//                JSONObject jsonObject = JSONObject.parseObject(msg);
//
//                String type = (String) jsonObject.getString("type");
//
//                String obj = jsonObject.getString("obj");
//
//                MeetingActorEntity meetingActorEntity =  JSON.parseObject(obj, MeetingActorEntity.class);
//                UpdateWrapper<MeetingActorEntity> updateWrapper = JSON.parseObject(obj, UpdateWrapper.class);
//
//                if (StringUtils.equals("insert",type)){
//
//                    if (!ObjectUtis.isAllFieldNull(meetingActorEntity)){
//                        meetingActorDao.insert(meetingActorEntity);
//                    }
//
//                    if (StringUtils.equals("update",type)){
//
//                        if (!ObjectUtis.isAllFieldNull(meetingActorEntity)){
//                            meetingActorDao.updateById(meetingActorEntity);
//                        }
//                        if (!ObjectUtis.isAllFieldNull(updateWrapper)){
//                            meetingActorDao.update(null,updateWrapper);
//                        }
//                    }
//                }
//            }
//
//    }
//
//
//    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
//    @RabbitListener(queues = "meeting_plaza")
//    public void onMessage3(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
//
//            if (StringUtils.isNotBlank(msg)){
//
//                JSONObject jsonObject = JSONObject.parseObject(msg);
//
//                String type = (String) jsonObject.getString("type");
//
//                String obj = jsonObject.getString("obj");
//
//                MeetingPlazaEntity meetingPlazaEntity = JSON.parseObject(obj,MeetingPlazaEntity.class);
//
//                if (StringUtils.equals("insert",type)){
//                    if (!ObjectUtis.isAllFieldNull(meetingPlazaEntity)){
//                        meetingPlazaDao.insert(meetingPlazaEntity);
//                    }
//                }
//                if (StringUtils.equals("update",type)){
//                    if (!ObjectUtis.isAllFieldNull(meetingPlazaEntity)){
//                        meetingPlazaDao.updateById(meetingPlazaEntity);
//                    }
//                }
//            }
//    }
//
//
//    //定义方法进行信息的监听   RabbitListener中的参数用于表示监听的是哪一个队列
//    @RabbitListener(queues = "meeting")
//    public void onMessage4(@Payload String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel) throws IOException {
//
//            if (StringUtils.isNotBlank(msg)){
//
//                JSONObject jsonObject = JSONObject.parseObject(msg);
//
//                String type = (String) jsonObject.getString("type");
//
//                String obj = jsonObject.getString("obj");
//
//                MeetingEntity meetingEntity = JSON.parseObject(obj,MeetingEntity.class);
//
//                if (StringUtils.equals("insert",type)){
//                    if (!ObjectUtis.isAllFieldNull(meetingEntity)){
//                        meetingDao.insert(meetingEntity);
//                    }
//                }
//
//                if (StringUtils.equals("update",type)){
//                    if (!ObjectUtis.isAllFieldNull(meetingEntity)){
//                        meetingDao.updateById(meetingEntity);
//                    }
//                }
//            }
//    }
//
//
//
//
//}
