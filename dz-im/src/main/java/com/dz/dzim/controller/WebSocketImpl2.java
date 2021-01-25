package com.dz.dzim.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dz.dzim.common.MessageTypeEnum;
import com.dz.dzim.pojo.MessageVO;
import com.dz.dzim.pojo.OnlineUser;
import com.dz.dzim.pojo.OnlineUserVo;
import com.dz.dzim.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ServerException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author：baohan
 * @Description： {userId} 最好是IP
 * @Date： created in 09:31 2021/1/19
 */
@Component
@ServerEndpoint(value = "/connectWebSocket2/{userId}")
public class WebSocketImpl2 {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 在线人数  //使用原子类AtomicInteger, --->  static并发会产生线程安全问题，
     * //public  static Integer onlineNumber = 0;
     */
    public static AtomicInteger onlineNumber = new AtomicInteger(0);
    /**
     * 所有用户信息(session + userId + username + createTime  --> 以用户的id为key, 通过用户key来获取用户session进行消息发送)
     */
    public static Map<String, OnlineUser> clients = new ConcurrentHashMap<>();


    /**
     * 建立连接  连接成功调用的方法
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session, EndpointConfig config) throws ServerException {
        User user = new User();
        user.setUserId(userId);
        user.setStatus(MessageTypeEnum.ONLINE);
        // 自增1
        onlineNumber.getAndIncrement();
        // 保存新用户id,用户名,session会话,登录时间
        clients.put(userId, new OnlineUser(userId, null, session));
        String[] strArray = {"1", "2", "3"};
        // 告诉所有人,我上线了 !!! new String[]{"All"}
        String content = "系统消息:" + userId + " 上线了";
        MessageVO messageVO = new MessageVO(user, content, MessageTypeEnum.ONLINE);
        sendMessage(messageVO);
        // 给自己发一条消息：告诉自己现在都有谁在线
        MessageVO messageVO2 = new MessageVO(user, content, MessageTypeEnum.ONLINE, new String[]{userId});
        sendMessage(messageVO2);

        logger.info("有新连接加入！sessionId：{} userId：{}  当前在线人数:{}", session.getId(), userId, onlineNumber);

    }


    //发消息
    public void sendMessage(MessageVO messageVO2) {
        String[] receiver = messageVO2.getReceiver();
        if (null == receiver || receiver.length == 0) {
            logger.error("收信人不能为空");
            return;
        }
        if ("ALL".equals(messageVO2.getReceiver()[0]) || "all".equals(messageVO2.getReceiver()[0])) {
            // 发送消息给所有人
            Set<String> userIds = clients.keySet();
            for (String userId : userIds) {
                this.sendMsg(userId, messageVO2);
            }
        } else {
            //发送消息给指定人
            for (String userId : receiver) {
                this.sendMsg(userId, messageVO2);
            }
        }
    }

    /**
     * 消息发送(最后发送, 在send方法中循环用户Id 列表依次发送消息给指定人)
     * <p>
     * // 消息发送（同步:getBasicRemote 异步:getAsyncRemote）
     * </P>
     *
     * @param userId    消息接收人ID , onlineUsers 的 key
     * @param messageVO 消息内容
     */
    private void sendMsg(String userId, MessageVO messageVO) {
        // 判断用户是否在线, 在线发送消息推送
        if (clients.containsKey(userId)) {
            try {
                clients.get(userId).getSession().getAsyncRemote().sendText(JSON.toJSONString(messageVO), new SendHandler() {
                    @Override
                    public void onResult(SendResult sendResult) {
                        logger.info("------------异步群发");
                        if (sendResult.isOK()) {
                            logger.info("异步成功");
                            //发送成功 判断如果是聊天记录则 存入数据库
                            //   pushToDB(item.session.getId(), message, sendResult.isOK());
                        } else {
                            //失败重发 发过三次 策略
                            logger.info("异步失败");
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                logger.info(userId + "上线的时候通知所有人发生了错误");
            }
        } else {
            //--------处理逻辑------
            logger.info("用户不在线需要发送离线消息");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("服务端发生了错误" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        User user = new User();
        user.setUserId(userId);
        user.setStatus(MessageTypeEnum.OFFLINE);
        // 自减1
        onlineNumber.getAndDecrement();
        // 所有在线用户中去除下线用户
        clients.remove(userId);
        // 告诉所有人,我下线了
        String content = "系统消息:" + userId + " 下线了";
        //this.send(new SendMsgVO(2, userId, , "ALL", content, null));

        MessageVO messageVO = new MessageVO(user, content, MessageTypeEnum.ONLINE);
        sendMessage(messageVO);
        // 日志
        logger.info(userId + ":已离线！ 当前在线人数" + onlineNumber);

    }

    /**
     * 服务端 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(@PathParam("userId") String userId, String message, Session session) {
        try {
            logger.info("服务端收到客户端的消息：" + message + "===》来自于id为：" + session.getId() + "session的客户");
            MessageVO messageVO = JSONObject.parseObject(message, MessageVO.class);
            if (null == messageVO.getSendTime()) {
                messageVO.setSendTime(new Date().toString());
            }
            sendMessage(messageVO);
        } catch (Exception e) {

            e.printStackTrace();
            logger.info("发生了错误了");
        }

    }

    /**
     * 获取当前在线人数
     *
     * @return
     */
    public Integer getOnlineCount() {
        return onlineNumber.get();
    }

    /**
     * 获取当前在线列表
     * <p>
     * 获取当前在线列表, 把onlineUsers 转到 OnlineUsersVO返回
     * </p>
     *
     * @return
     */
    public synchronized List<OnlineUserVo> getOnlineUsers() {
        List<OnlineUserVo> onlineUsersVOList = new ArrayList<>();
        for (OnlineUser onlineUsers : clients.values()) {
            OnlineUserVo onlineUserVo = new OnlineUserVo();
            BeanUtils.copyProperties(onlineUsers, onlineUserVo);
            onlineUsersVOList.add(onlineUserVo);
       }
        return onlineUsersVOList;
    }

}
