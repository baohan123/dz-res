package com.dz.dzim.aop;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.dz.dzim.common.MessageTypeEnum;
import com.dz.dzim.mapper.ChatRecordMapper;
import com.dz.dzim.pojo.User;
import com.dz.dzim.pojo.doman.MsgRecordsEntity;
import com.dz.dzim.pojo.vo.MessageVO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;

/**
 * 聊天记录切面类
 *
 * @author baohan
 * @date 2021/1/26
 */
@Aspect
@Component
public class ChatRecordAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ChatRecordMapper chatRecordMapper;



    @Pointcut("@annotation(com.dz.dzim.annotation.ChatRecord)")
    public void chatRecordPointcut() {
    }

    @Before("chatRecordPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        logger.debug("before -> {}", joinPoint);

        MessageVO messageVO = null;
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof MessageVO) {
                messageVO = (MessageVO) obj;
                break;
            }
        }
        MsgRecordsEntity msgRecordsEntity = new MsgRecordsEntity();

        BeanUtils.copyProperties(messageVO, msgRecordsEntity);
        BeanUtils.copyProperties(messageVO.getUser(), msgRecordsEntity);
        Assert.notNull(messageVO, "方法必需以MessageVO类或该类的子类作为参数");
        msgRecordsEntity.setSendType(messageVO.getSendType().toString());
        msgRecordsEntity.setReceivers(Arrays.toString(messageVO.getReceiver()));
        chatRecordMapper.insert(msgRecordsEntity);
        if (MessageTypeEnum.USETTOCLENT.toString().equals(messageVO.getSendType())) {
            // 是否对消息类容安全性的字段进行过滤 。对于User类型的消息做敏感词处理
        }

        logger.debug("添加聊天记录 -> {}", messageVO);
        //chatRecordService.addRecord(ChatRecordDTO.toChatRecordDTO(messageVO));
    }

}
