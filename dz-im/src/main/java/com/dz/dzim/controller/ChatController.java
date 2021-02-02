package com.dz.dzim.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dz.dzim.common.GeneralUtils;
import com.dz.dzim.common.Result;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.common.enums.CodeEnum;
import com.dz.dzim.common.enums.inter.Code;
import com.dz.dzim.mapper.ChatRecordMapper;
import com.dz.dzim.mapper.MeetingChattingDao;
import com.dz.dzim.mapper.MeetingDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.doman.MeetingChattingEntity;
import com.dz.dzim.pojo.doman.MeetingEntity;
import com.dz.dzim.pojo.doman.MeetingPlazaEntity;
import com.dz.dzim.pojo.doman.MsgRecordsEntity;
import com.dz.dzim.pojo.vo.MeetingAndActorEntityVo;
import com.dz.dzim.pojo.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);


    @Autowired
    private ChatRecordMapper chatRecordMapper;


    @Autowired
    private MeetingPlazaDao meetingPlazaDao;

    @Autowired
    private MeetingDao meetingDao;

    @Autowired
    private MeetingChattingDao meetingChattingDao;

    /**
     * @param username 查询条件
     * @param pageNum  当前页
     * @param pageSize 分页长度
     * @return
     */
    @GetMapping("/queryByPage")
    public Result<Object> queryByPage(String username, Integer pageNum, Integer pageSize) {
        Page<MsgRecordsEntity> page = new Page<>(pageNum, pageSize);
        IPage<MsgRecordsEntity> customerIPage = chatRecordMapper.selectPage(page, null);
        return Result.success(customerIPage);
    }

    /**
     * 创建大会场
     */
    @PostMapping("/creatBigSession")
    public ResponseVO creatBigSession(@RequestBody JSONObject jsonObject) {
        Long userId = (Long) jsonObject.getLong("userId");
        String talkerType = (String) jsonObject.get("talkerType");
        String id = GeneralUtils.randomUUID(SysConstant.ELEVEN);
        List<MeetingPlazaEntity> queryEntity = meetingPlazaDao.selectList(new QueryWrapper<>(
                new MeetingPlazaEntity(userId, talkerType))
                .orderByDesc("enter_time"));

        if (SysConstant.ZERO == queryEntity.size()) {
            int insert = meetingPlazaDao.insert(new MeetingPlazaEntity(id, userId, talkerType,
                    null, new Date(), null, SysConstant.ONE,
                    null, GeneralUtils.randomUUID(SysConstant.ELEVEN)));
            if (1 != insert) {
                return new ResponseVO(CodeEnum.CREATION);
            }
        } else {
            meetingPlazaDao.insert(new MeetingPlazaEntity(
                    queryEntity.get(SysConstant.ZERO).getNextId(),
                    userId,
                    talkerType,
                    null,
                    new Date(),
                    null,
                    SysConstant.ONE,
                    queryEntity.get(SysConstant.ZERO).getId(),
                    GeneralUtils.randomUUID(SysConstant.ELEVEN)));
        }
        return new ResponseVO(id);
    }

    /**
     * 创建小会场
     */
    @PostMapping("/creatSmallSession")
    public ResponseVO creatSmallSession() {
        String id = GeneralUtils.randomUUID(SysConstant.SEX);
        if (1 == meetingDao.insert(new MeetingEntity(id, new Date(), SysConstant.ZERO, new Date()))) {
            return new ResponseVO(id);
        } else {
            return new ResponseVO(CodeEnum.CREATION);
        }
    }


    /**
     * 分页查询聊天记录
     */
    @PostMapping("/queryChat")
    public ResponseVO queryChat(Long talker, Integer pageNum, Integer pageSize) {
        pageNum = null == pageNum ? 0 : pageNum;
        pageSize = null == pageSize ? 0 : pageSize;
        Page<MeetingChattingEntity> page = new Page<>(pageNum, pageSize);
        //如果是用户
        QueryWrapper<MeetingChattingEntity> queryWrapper = new QueryWrapper();
        //条件查询
        queryWrapper.eq("talker", talker).or().eq("addr_id", talker).orderByDesc("server_time");

        Page<MeetingChattingEntity> meetingChattingEntityPage = meetingChattingDao.selectPage(page, queryWrapper);

        //  queryWrapper.or().

        return new ResponseVO(meetingChattingEntityPage);
    }


}
