//package com.dz.dzim.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.dz.dzim.common.MapUtils;
//import com.dz.dzim.common.MessageTypeEnum;
//import com.dz.dzim.common.SysConstant;
//import com.dz.dzim.config.SpringContextUtil;
//import com.dz.dzim.pojo.OnlineUser;
//import com.dz.dzim.pojo.User;
//import com.dz.dzim.pojo.vo.MessageVO;
//import com.dz.dzim.pojo.vo.OnlineUserVo;
//import com.dz.dzim.service.SendMsgService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Component;
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.rmi.ServerException;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @Author：baohan
// * @Description： websocket实现
// * @Date： created in 09:31 2021/1/19
// */
//@Component
//@ServerEndpoint(value = "/connectWebSocket2/{userId}/{userType}")
//public class WebSocketImpl3 {
//
//
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private SendMsgService sendMsgService = (SendMsgService) SpringContextUtil.getApplicationContext().getBean("sendMsgService");
//
//    //默认用户在线
//    public static Integer MAX_RS = 3;
//    /**
//     * 所有在线用户(session + userId + username + createTime  -->
//     */
//    public static Map<String, OnlineUser> clients = new ConcurrentHashMap<>();
//
//    /**
//     * 所有在线客服
//     */
//    public static Map<String, OnlineUser> kfclients = new ConcurrentHashMap<>();
//
//    //当前可用客服
//    public static Map<String, Integer> usableClentsAll = new ConcurrentHashMap<>();
//
//    //排队消息
//    public static LinkedList<MessageVO> pdUser = new LinkedList<>();
//
//    //在线客服已产生的用户会话
//    public static Map<String, Set<String>> cmpMaps = new ConcurrentHashMap<>();
//
//
//    /**
//     * 建立连接  连接成功调用的方法
//     *
//     * @param session
//     */
//    @OnOpen
//    public void onOpen(@PathParam("userId") String userId, @PathParam("userType") String userType, Session session, EndpointConfig config) throws ServerException {
//
//
//        User user = new User();
//        user.setUserId(userId);
//        user.setStatus(MessageTypeEnum.ONLINE);
//        //当前消息为用户 加入大会话
//        if (userType.equals(MessageTypeEnum.USER.toString())) {
//            clients.put(userId, new OnlineUser(userId, userId, session, new Date().toString(), MessageTypeEnum.USER, MessageTypeEnum.ONLINE));
//        //加入会话后等待分配客服
//
//        }
//
//        //当前消息为客服
//        if (userType.equals(MessageTypeEnum.KFUSER.toString())) {
//            kfclients.put(userId, new OnlineUser(userId, userId, session,
//                    new Date().toString(), MessageTypeEnum.KFUSER, MessageTypeEnum.ONLINE,
//                    MAX_RS, new LinkedHashMap<String, User>(), MAX_RS));
//        }
//        logger.info("系统消息:" + userId + " 上线了" + "，用户类型：" + userType);
//    }
//
//    /**
//     * 服务端 收到客户端的消息
//     *
//     * @param message 消息
//     * @param session 会话
//     */
//    @OnMessage
//    public void onMessage(@PathParam("userId") String userId, @PathParam("userType") String userType, String
//            message, Session session) {
//        try {
//            logger.info("服务端收到客户端的消息：" + message + "===》来自于id为：" + session.getId() + "session的客户");
//            MessageVO messageVO = JSONObject.parseObject(message, MessageVO.class);
//
//            //消息来自用户 -->发送客服
//            if (messageVO.getSendType().toString().equals(MessageTypeEnum.USETTOCLENT.toString())) {
//                for (Map.Entry<String, Set<String>> entry : cmpMaps.entrySet()) {
//                    //判断客服是否已经连接过 如果已建立会话直接发送消息
//                    if (entry.getValue().contains(userId)) {
//                        messageVO.setReceiver(new String[]{entry.getKey()});
//                        sendMsgService.sendMessage(messageVO, messageVO.getSendType().toString());
//                        return;
//                    }
//                }
//                //分配指定客服
//                allocationClent(messageVO, userId);
//            }
//            sendMsgService.sendMessage(messageVO, messageVO.getSendType().toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.info("onMessage ==> 发生了错误了");
//        }
//
//    }
//
//    @OnError
//    public void onError(Session session, Throwable error) {
//        logger.info("服务端发生了错误" + error.getMessage());
//        error.printStackTrace();
//    }
//
//    /**
//     * 连接关闭
//     */
//    @OnClose
//    public void onClose(@PathParam("userId") String userId, @PathParam("userType") String userType) {
//        User user = new User();
//        user.setUserId(userId);
//        user.setStatus(MessageTypeEnum.OFFLINE);
//        if (userType.equals(MessageTypeEnum.USER)) {
//            // 所有在线用户中去除下线用户
//            clients.remove(userId);
//
//        } else {
//            kfclients.remove(userId);
//        }
//        String content = "系统消息:" + userId + " 下线了";
//        MessageVO messageVO = new MessageVO(user, content, MessageTypeEnum.OFFLINE, MessageTypeEnum.SYSTEM);
//        sendMsgService.sendMessage(messageVO, MessageTypeEnum.USETTOCLENT.toString());
//        logger.info(userId + ":已离线！ 当前在线人数" + "userType:" + userType);
//    }
//
//
//    /**
//     * 获取当前在线人数
//     *
//     * @return
//     */
//    public Map<String, String> getOnlineCount() {
//        Map<String, String> map = new HashMap<>();
//        map.put("user", String.valueOf(clients.size()));
//        map.put("kfUser", String.valueOf(kfclients.size()));
//        return map;
//    }
//
//    /**
//     * 获取当前在线列表, 把onlineUsers 转到 OnlineUsersVO返回
//     * @return
//     */
//    public synchronized List<OnlineUserVo> getOnlineUsers(String userType) {
//        List<OnlineUserVo> onlineUsersVOList = new ArrayList<>();
//        Map<String, OnlineUser> onlineMap = new ConcurrentHashMap<>();
//        if (userType.equals(MessageTypeEnum.USER)) {
//            onlineMap = clients;
//        } else if (userType.equals(MessageTypeEnum.KFUSER)) {
//            onlineMap = kfclients;
//        } else {
//            onlineMap.putAll(clients);
//            onlineMap.putAll(kfclients);
//        }
//        for (OnlineUser onlineUsers : onlineMap.values()) {
//            OnlineUserVo onlineUserVo = new OnlineUserVo();
//            BeanUtils.copyProperties(onlineUsers, onlineUserVo);
//            onlineUsersVOList.add(onlineUserVo);
//        }
//        return onlineUsersVOList;
//    }
//
//    /**
//     * 分配指定客服策略
//     * @param messageVO
//     * @param userId
//     */
//    public void allocationClent(MessageVO messageVO, String userId) {
//        //如果之前没有建立会话 客服则优先分配资源最丰富的客服
//        List<Map.Entry<String, Integer>> entries = MapUtils.mapTosortClent(kfclients, true);
//        //如果可用客服资源为0 加入队列优先排序
//        if (null == entries || entries.size() == 0) {
//            messageVO.setReceiver(new String[]{userId});
//            messageVO.setMessage("当前客服繁忙，请等待，加入排队");
//            //加入排队消息
//            pdUser.add(messageVO);
//        } else {
//            //取出资源最丰富的客户，将新客户放入对应会话
//            kfclients.get(entries.get(SysConstant.ZERO).getKey()).getUsers().put(userId, new User(userId, new Date()));
//            Integer usableCents = kfclients.get(entries.get(SysConstant.ZERO).getKey()).getUsableClents();
//            kfclients.get(entries.get(SysConstant.ZERO).getKey()).setUsableClents(usableCents--);
//            messageVO.setReceiver(new String[]{entries.get(SysConstant.ZERO).getKey()});
//        }
//    }
//
////    @Override
////    public Object getPayload() {
////        System.out.println(0000);
////        return null;
////    }
////
////    @Override
////    public int getPayloadLength() {
////        System.out.println(0000);
////
////        return 0;
////    }
////
////    @Override
////    public boolean isLast() {
////        System.out.println(0000);
////
////        return false;
////    }
//
//
//}
