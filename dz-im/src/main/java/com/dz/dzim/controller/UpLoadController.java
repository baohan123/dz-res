package com.dz.dzim.controller;/**
 * @className UpLoadController
 * @description TODO
 * @author xxxyyy
 * @date 2021/2/2 15:37
 */

import com.dz.dzim.pojo.vo.ResponseVO;
import com.dz.dzim.service.UploadService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 *@className UpLoadController
 *@description TODO
 *@author xxxyyy
 *@date 2021/2/2 15:37
 */


@RestController
public class UpLoadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping(value = "/upload/img")
    public ResponseVO uploadImage(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) throws Exception {

        String imgsrc = uploadService.uploadImage(multipartFile, request);

        if (StringUtils.isNotBlank(imgsrc)) {
            return new ResponseVO(imgsrc);
        }
        return new ResponseVO("上传失败！");
    }
}
