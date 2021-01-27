package com.dz.dzim.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dz.dzim.common.MapUtils;
import com.dz.dzim.common.MessageTypeEnum;
import com.dz.dzim.common.StringUtilsCom;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.config.SpringContextUtil;
import com.dz.dzim.config.WebSocketConfig;
import com.dz.dzim.pojo.*;
import com.dz.dzim.pojo.vo.MessageVO;
import com.dz.dzim.pojo.vo.OnlineUserVo;
import com.dz.dzim.service.SendMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.rmi.ServerException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：baohan
 * @Description： {userId} 最好是IP
 * @Date： created in 09:31 2021/1/19
 */
@Component
@ServerEndpoint(value = "/connectWebSocket2/{userId}/{userType}/{sendType}")
public class WebSocketImpl2 {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private SendMsgService sendMsgService = (SendMsgService) SpringContextUtil.getApplicationContext().getBean("sendMsgService");

    //默认用户在线
    public static Integer MAX_RS = 3;
    /**
     * 所有在线用户信息(session + userId + username + createTime  -->
     * 以用户的id为key, 通过用户key来获取用户session进行消息发送)
     */
    public static Map<String, OnlineUser> clients = new ConcurrentHashMap<>();

    //默认在线客服
    public static Map<String, OnlineUser> kfclients = new ConcurrentHashMap<>();

    //当前可用客服
    public static Map<String, Integer> usableClentsAll = new ConcurrentHashMap<>();

    //排队消息
    public static LinkedList<MessageVO> pdUser = new LinkedList<>();

    //在线客服已产生的用户会话
    public static Map<String, Set<String>> cmpMaps = new ConcurrentHashMap<>();


    /**
     * 建立连接  连接成功调用的方法
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, @PathParam("userType") String userType, @PathParam("sendType") String sendType, Session session, EndpointConfig config) throws ServerException {
        User user = new User();
        user.setUserId(userId);
        user.setStatus(MessageTypeEnum.ONLINE);
        //当前消息为用户
        if (userType.equals(MessageTypeEnum.USER.toString())) {
            clients.put(userId, new OnlineUser(userId, userId, session, new Date().toString(), MessageTypeEnum.USER, MessageTypeEnum.ONLINE));
        }
        //当前消息为客服
        if (userType.equals(MessageTypeEnum.KFUSER.toString())) {
            kfclients.put(userId, new OnlineUser(userId, userId, session,
                    new Date().toString(), MessageTypeEnum.KFUSER, MessageTypeEnum.ONLINE,
                    MAX_RS, new LinkedHashMap<String, User>(), MAX_RS));
        }
        logger.info("系统消息:" + userId + " 上线了" + "，用户类型：" + userType);
    }

    /**
     * 服务端 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(@PathParam("userId") String userId, @PathParam("userType") String userType, @PathParam("sendType") String sendType, String
            message, Session session) {
        try {
            logger.info("服务端收到客户端的消息：" + message + "===》来自于id为：" + session.getId() + "session的客户");
            MessageVO messageVO = JSONObject.parseObject(message, MessageVO.class);
            //消息来自用户 -->发送客服
            if (sendType.equals(MessageTypeEnum.USETTOCLENT.toString())) {
                for (Map.Entry<String, Set<String>> entry : cmpMaps.entrySet()) {
                    //判断客服是否已经连接过 如果已建立会话直接发送消息
                    if (entry.getValue().contains(userId)) {
                        messageVO.setReceiver(new String[]{entry.getKey()});
                        sendMessage(messageVO, sendType);
                        return;
                    }
                }
                //分配指定客服
                allocationClent(messageVO, userId);
            }
            sendMessage(messageVO, sendType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("onMessage ==> 发生了错误了");
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
    public void onClose(@PathParam("userId") String userId, @PathParam("userType") String userType, @PathParam("sendType") String sendType) {
        User user = new User();
        user.setUserId(userId);
        user.setStatus(MessageTypeEnum.OFFLINE);
        if (userType.equals(MessageTypeEnum.USER)) {
            // 所有在线用户中去除下线用户
            clients.remove(userId);
        } else {
            kfclients.remove(userId);
        }
        String content = "系统消息:" + userId + " 下线了";
        MessageVO messageVO = new MessageVO(user, content, MessageTypeEnum.OFFLINE, MessageTypeEnum.SYSTEM);
        sendMessage(messageVO, sendType);
        logger.info(userId + ":已离线！ 当前在线人数" + "下线的类型:" + sendType);
    }

    //发消息
    public void sendMessage(MessageVO messageVO, String sendType) {
        String[] receiver = messageVO.getReceiver();
        //验证收信人是否为空
        if (!checkUserisNull(receiver, messageVO, sendType)) {
            return;
        }
        //群发
        if ("ALL".equals(messageVO.getReceiver()[SysConstant.ZERO]) || "all".equals(messageVO.getReceiver()[SysConstant.ZERO])) {
            //区分群发用户类型
            Set<String> allUser = sendAllDecide(sendType);
            for (String userId : allUser) {
                sendMsgService.sendMsg(userId, messageVO, sendType);
            }
        } else {
            //单反 或 指定发送
            for (String userId : receiver) {
                sendMsgService.sendMsg(userId, messageVO, sendType);
            }
        }
    }


    /**
     * 验证收信人是否为空
     *
     * @param receiver
     * @param messageVO
     * @return
     */
    public boolean checkUserisNull(String[] receiver, MessageVO messageVO, String sendType) {
        if (!StringUtilsCom.isStringLenth(receiver)) {
            logger.error("收信人不能为空");
            messageVO.setMessage("收信人不能为空，或收信人不在线");
            sendMsgService.sendMsg(messageVO.getUser().getUserId(), messageVO, sendType);
            return false;
        }
        return true;
    }

    /**
     * 群发用户类型判断
     *
     * @param sendType 消息类型
     * @return
     */
    public Set<String> sendAllDecide(String sendType) {
        Set<String> userIds = clients.keySet();
        Set<String> kfUserIds = kfclients.keySet();
        // 发送消息给所有人
        Set<String> allUser = new HashSet<>();
        if (sendType.equals(MessageTypeEnum.CLENTTOUSER.toString())) {
            //群发给用户
            allUser = userIds;
        } else {
            //群发给客服
            allUser = kfUserIds;
        }
        return allUser;
    }

    /**
     * 获取当前在线人数
     * @return
     */
    public Map<String, String> getOnlineCount() {
        Map<String, String> map = new HashMap<>();
        map.put("user", String.valueOf(clients.size()));
        map.put("kfUser", String.valueOf(kfclients.size()));
        return map;
    }

    /**
     * 获取当前在线列表, 把onlineUsers 转到 OnlineUsersVO返回
     *
     * @return
     */
    public synchronized List<OnlineUserVo> getOnlineUsers(String userType) {
        List<OnlineUserVo> onlineUsersVOList = new ArrayList<>();
        Map<String, OnlineUser> onlineMap = new ConcurrentHashMap<>();
        if (userType.equals(MessageTypeEnum.USER)) {
            onlineMap = clients;
        } else if (userType.equals(MessageTypeEnum.KFUSER)) {
            onlineMap = kfclients;
        } else {
            onlineMap.putAll(clients);
            onlineMap.putAll(kfclients);
        }
        for (OnlineUser onlineUsers : onlineMap.values()) {
            OnlineUserVo onlineUserVo = new OnlineUserVo();
            BeanUtils.copyProperties(onlineUsers, onlineUserVo);
            onlineUsersVOList.add(onlineUserVo);
        }
        return onlineUsersVOList;
    }

    /**
     * 分配指定客服策略
     *
     * @param messageVO
     * @param userId
     */
    public void allocationClent(MessageVO messageVO, String userId) {
        //如果之前没有建立会话 客服则优先分配资源最丰富的客服
        List<Map.Entry<String, Integer>> entries = MapUtils.mapTosortClent(kfclients, true);
        //如果可用客服资源为0 加入队列优先排序
        if (null == entries || entries.size() == 0) {
            messageVO.setReceiver(new String[]{userId});
            messageVO.setMessage("当前客服繁忙，请等待，加入排队");
            //加入排队消息
            pdUser.add(messageVO);
        } else {
            //取出资源最丰富的客户，将新客户放入对应会话
            kfclients.get(entries.get(SysConstant.ZERO).getKey()).getUsers().put(userId, new User(userId, new Date()));
            Integer usableCents = kfclients.get(entries.get(SysConstant.ZERO).getKey()).getUsableClents();
            kfclients.get(entries.get(SysConstant.ZERO).getKey()).setUsableClents(usableCents--);
            messageVO.setReceiver(new String[]{entries.get(SysConstant.ZERO).getKey()});
        }
    }

}
