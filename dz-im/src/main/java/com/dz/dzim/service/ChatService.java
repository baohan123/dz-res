//package com.dz.dzim.service;
//
//import com.alibaba.fastjson.JSONObject;
//import com.dz.dzim.model.ChatMessage;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.MessagingException;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//import org.springframework.stereotype.Service;
//
//
//@Service
//public class ChatService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
//
//    @Autowired
//    private SimpMessageSendingOperations simpMessageSendingOperations;
//
//
//    public Boolean sendMsg(String msg) {
//        try {
//            JSONObject msgJson = JSONObject.parseObject(msg);
//            if (msgJson.getString("to").equals("all") && msgJson.getString("type").equals(ChatMessage.MessageType.CHAT.toString())){
//                simpMessageSendingOperations.convertAndSend("/topic/public", msgJson);
//            }else if (msgJson.getString("to").equals("all") && msgJson.getString("type").equals(ChatMessage.MessageType.JOIN.toString())) {
//                simpMessageSendingOperations.convertAndSend("/topic/public", msgJson);
//            }else if(msgJson.getString("to").equals("all") &&  msgJson.getString("type").equals(ChatMessage.MessageType.LEAVE.toString())) {
//                simpMessageSendingOperations.convertAndSend("/topic/public", msgJson);
//            }else if (!msgJson.getString("to").equals("all") &&  msgJson.getString("type").equals(ChatMessage.MessageType.CHAT.toString())){
//                simpMessageSendingOperations.convertAndSendToUser(msgJson.getString("to"),"/topic/msg", msgJson);
//            }
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//
//    }
//
//
//    }
//
//
//
//
