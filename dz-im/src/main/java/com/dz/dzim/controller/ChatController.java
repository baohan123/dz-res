package com.dz.dzim.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dz.dzim.common.GeneralUtils;
import com.dz.dzim.common.Result;
import com.dz.dzim.common.SysConstant;
import com.dz.dzim.exception.handler.ExceptionHandle;
import com.dz.dzim.mapper.ChatRecordMapper;
import com.dz.dzim.mapper.MeetingChattingDao;
import com.dz.dzim.mapper.MeetingDao;
import com.dz.dzim.mapper.MeetingPlazaDao;
import com.dz.dzim.pojo.doman.MeetingChattingEntity;
import com.dz.dzim.pojo.doman.MeetingEntity;
import com.dz.dzim.pojo.doman.MeetingPlazaEntity;
import com.dz.dzim.pojo.doman.MsgRecordsEntity;
import com.dz.dzim.pojo.vo.QueryParams;
import com.dz.dzim.pojo.vo.ResponseVO;
import com.dz.dzim.service.UploadService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ChatController extends ExceptionHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);


    @Autowired
    private ChatRecordMapper chatRecordMapper;


    @Autowired
    private MeetingPlazaDao meetingPlazaDao;

    @Autowired
    private MeetingDao meetingDao;


    @Autowired
    private MeetingChattingDao meetingChattingDao;

    @Autowired
    private UploadService uploadService;

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
        Long userId = jsonObject.getLong("userId");
        String talkerType = (String) jsonObject.get("talkerType");
        String id;
        String nextIdNew = GeneralUtils.randomUUID(SysConstant.ELEVEN);
        String prevIdNew = null;
        List<MeetingPlazaEntity> queryEntity = meetingPlazaDao.selectList(new QueryWrapper<>(
                new MeetingPlazaEntity(userId, talkerType))
                .orderByDesc("enter_time"));

        if (SysConstant.ZERO == queryEntity.size()) {
            id = GeneralUtils.randomUUID(SysConstant.ELEVEN);

        } else {
            id = queryEntity.get(SysConstant.ZERO).getNextId();
            prevIdNew = queryEntity.get(SysConstant.ZERO).getId();
        }
        MeetingPlazaEntity entity = new MeetingPlazaEntity(id,
                userId,
                talkerType,
                null,
                new Date(),
                null,
                SysConstant.ONE,
                prevIdNew,
                nextIdNew);
        meetingPlazaDao.insert(entity);
        //rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY3, GeneralUtils.objectToString("insert", entity));
        return new ResponseVO(id);
    }


    /**
     * 创建小会场
     */
    @PostMapping("/creatSmallSession")
    public ResponseVO creatSmallSession() {
        String id = GeneralUtils.randomUUID(SysConstant.SEX);
        MeetingEntity meetingEntity = new MeetingEntity(id, new Date(), SysConstant.ZERO, new Date());
        meetingDao.insert(meetingEntity);
        //rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.KEY4, GeneralUtils.objectToString("insert", meetingEntity));
        return new ResponseVO(id);
    }


    /**
     * 分页查询聊天记录
     */
    @PostMapping("/queryChat")
    public ResponseVO queryChat(@RequestBody QueryParams params) {
        Long startTime = params.getStartTime();
        Long endTime = params.getEndTime();
        if (null == endTime || SysConstant.ZERO == endTime) {
            endTime = System.currentTimeMillis();
        }

        Page<MeetingChattingEntity> page = QueryParams.getPage(params);
        Long talker = params.getTalker();
        //如果是用户
        QueryWrapper<MeetingChattingEntity> queryWrapper = new QueryWrapper();
        //条件查询
        queryWrapper.eq("talker", talker).or().eq("addr_id", talker).orderByDesc("server_time");
        if (null != startTime && SysConstant.ZERO != startTime) {
            Long finalEndTime = endTime;
            queryWrapper.and(wrapper -> wrapper.ge("server_time", startTime).le("server_time", finalEndTime)) ;
        }
        Page<MeetingChattingEntity> meetingChattingEntityPage = meetingChattingDao.selectPage(page, queryWrapper);
        return new ResponseVO(meetingChattingEntityPage);
    }


    @PostMapping(value = "/upload/img")
    public ResponseVO uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        String imgsrc = uploadService.uploadImage(file, request);

        if (StringUtils.isNotBlank(imgsrc)) {
            return new ResponseVO(imgsrc);
        }
        return new ResponseVO("上传失败！");
    }


}
