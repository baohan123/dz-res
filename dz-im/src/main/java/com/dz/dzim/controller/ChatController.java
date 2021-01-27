package com.dz.dzim.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dz.dzim.common.Result;
import com.dz.dzim.mapper.ChatRecordMapper;
import com.dz.dzim.pojo.doman.MsgRecordsEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);


    @Autowired
    private ChatRecordMapper chatRecordMapper;

    /**
     *
     * @param username 查询条件
     * @param pageNum 当前页
     * @param pageSize  分页长度
     * @return
     */
    @GetMapping("/queryByPage")
    public Result<Object> queryByPage(String username, Integer pageNum, Integer pageSize) {
        Page<MsgRecordsEntity> page = new Page<>(pageNum, pageSize);
        IPage<MsgRecordsEntity> customerIPage = chatRecordMapper.selectPage(page, null);
        return Result.success(customerIPage);
    }
}
