package com.dz.dzim.controller;

import com.dz.dzim.common.Result;
import com.dz.dzim.pojo.vo.OnlineUserVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 服务器给客户推送消息
 */
@RestController
public class SendToMsgController {

    @Autowired
    WebSocket webSocket;

    @Autowired
    WebSocketImpl2 webSocketImpl2;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 一对一发送
     *
     * @param userId
     * @param msg
     * @return
     * @throws IOException
     */
    @GetMapping("/sendTo")
    public String sendTo(@RequestParam("userId") String userId, @RequestParam("msg") String msg) throws IOException {

        webSocket.sendMessageTo(msg, userId);

        return "推送成功";
    }


    /**
     * 群发
     *
     * @param msg
     * @return
     * @throws IOException
     */
    @GetMapping("/sendAll")
    public String sendAll(@RequestParam("msg") String msg) throws IOException {

        String fromUserId = "system";//其实没用上
        webSocket.sendMessageAll(msg, fromUserId);

        return "推送成功";
    }

    @GetMapping("/sendMq")
    public String sendMq() throws IOException {
//
//        MsgData userTest = new MsgData();
//        userTest.setDesc("");
//        userTest.setTime("20200119");
//        userTest.setType("01");

        /**1.指定发送的交换机
         *      发送的消息会先发送给 virtual-host: /(顶级路由) 再由它到交换机
         *      由交换机通过路由键指定给具体的管道
         *
         * 2.路由键
         *      有的交换机需要路由键 有的不需要(发送给交换机的消息会被发送给所有管道)
         *
         * 3.发送的消息
         *      如果是对象的话必须实现序列化接口因为网络传输只能传二进制
         *exchange 交换机-> rountingkey 路由  -->queues 指定队列
         *
         * */
        //   rabbitTemplate.convertAndSend("boot_topic_exchange", "boot.haha", userTest);
        return "推送成功";
    }

    /**
     * TODO 获取当前在线人数
     */
    @RequestMapping(value = "/getOnlineCount", method = RequestMethod.GET)
    public Result<Map<String, String>> getOnlineCount() {
        Map<String, String> onlineCount = webSocketImpl2.getOnlineCount();
        return Result.success(onlineCount);
    }

    @RequestMapping(value = "/getOnlineUsersList", method = RequestMethod.GET)
    public Result<List<OnlineUserVo>> getOnlineUsersList(@RequestParam("userType") String userType) {
        List<OnlineUserVo> onlineUsers = webSocketImpl2.getOnlineUsers(userType);
        return Result.success(onlineUsers);
    }
}
