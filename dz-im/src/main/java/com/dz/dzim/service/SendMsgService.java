package com.dz.dzim.service;

import com.alibaba.fastjson.JSON;
import com.dz.dzim.annotation.ChatRecord;
import com.dz.dzim.common.MessageTypeEnum;
import com.dz.dzim.controller.WebSocketImpl2;
import com.dz.dzim.pojo.OnlineUser;
import com.dz.dzim.pojo.vo.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baohan
 * @className SendMsgService
 * @description TODO
 * @date 2021/1/26 22:29
 */
@Component
public class SendMsgService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * @param userId    消息接收人ID , onlineUsers 的 key
     * @param messageVO 消息内容
     */
    @ChatRecord
    public void sendMsg(String userId, MessageVO messageVO, String sendType) {
        try {
            Map<String, OnlineUser> maps = new ConcurrentHashMap();
            if (sendType.equals(MessageTypeEnum.USETTOCLENT.toString())) {
                maps = WebSocketImpl2.kfclients;
            } else if (sendType.equals(MessageTypeEnum.CLENTTOUSER.toString())) {
                maps = WebSocketImpl2.clients;
            } else {
                maps.putAll(WebSocketImpl2.clients);
                maps.putAll(WebSocketImpl2.kfclients);
            }

            Map<String, OnlineUser> finalMaps = maps;
            maps.get(userId).getSession().getAsyncRemote().sendText(JSON.toJSONString(messageVO), new SendHandler() {
                @Override
                public void onResult(SendResult sendResult) {
                    if (sendResult.isOK()) {
                        logger.info("异步成功");
                    } else {
                        //失败重发 发过三次 策略
                        logger.info("异步失败");
                        sendMsgToError(userId, messageVO, finalMaps);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(userId + "上线的时候通知所有人发生了错误");
        }
    }

    /**
     * 消息发送失败处理
     *
     * @param userId
     * @param messageVO
     */
    public void sendMsgToError(String userId, MessageVO messageVO, Map<String, OnlineUser> maps) {
        MessageVO messageVO1 = messageVO; //给自己发的消息
        messageVO1.setMessage("消息未发送到达");
        //1.先给自己发送消息 （同步发送）
        maps.get(messageVO1.getUser().getUserId()).getSession().getAsyncRemote().sendText(JSON.toJSONString(messageVO));
        //2，在重新尝试发送
        maps.get(userId).getSession().getAsyncRemote().sendText(JSON.toJSONString(messageVO), new SendHandler() {
            @Override
            public void onResult(SendResult sendResult) {
                if (sendResult.isOK()) {
                    logger.info("重发成功");
                    // 记录下来
                } else {
                    //失败重发 发过三次 策略
                    logger.info("异步失败");
                    messageVO.setMessage("失败后已经第二次发送消息仍然未到达 ");
                    messageVO1.setMessage("失败后已经第二次发送消息仍然未到达");
                    maps.get(messageVO1.getUser().getUserId()).getSession().getAsyncRemote().sendText(JSON.toJSONString(messageVO));
                    //记录
                    //  sendMsg(messageVO.getUser().getUserId(),messageVO);
                }
            }
        });
    }

}
