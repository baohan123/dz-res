package com.dz.dzim.service;

import com.dz.dzim.mapper.ChatRecordMapper;

import com.dz.dzim.utils.UUIDUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

/**
 * @author xxxyyy
 * @className UploadService
 * @description TODO
 * @date 2021/1/27 13:06
 */
@Component
public class UploadService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${filepath.imgurl}")
    private String imgUrl;

    @Value("${filepath.imgdir}")
    private String imgDir;


    public String uploadImage(MultipartFile multipartFile, HttpServletRequest request) throws Exception {

        if (multipartFile.isEmpty()) {
            throw new Exception("multipartFile为空！");
        }
        return execute(multipartFile,request);
    }

    private String execute(MultipartFile multipartFile, HttpServletRequest request) throws Exception {

        HttpSession session = request.getSession();
        /*从session中获取用户当前信息*/

        /*获取根路径*/
        String rootPath = request.getServletContext().getRealPath("/");
        /*
            获取文件原始名称
        * */
        String originalFilename = multipartFile.getOriginalFilename();

        if (StringUtils.isEmpty(originalFilename)) {
            throw new Exception("originalFilename为空！");
        }
        /*截取文件名称的后缀名*/
        String type = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        /*创建新的文件名称*/
        String fileName = UUIDUtils.create() + "." + type;

        /*创建提供浏览其访问的URL，*/
        String url= imgUrl+fileName;

        /*图片存放到服务器的具体路径*/
        String distPath = imgDir+fileName;

        //将url路径作为消息发送到MQ
        rabbitTemplate.convertAndSend("imageExchange","img.#",url);

        File file = new File(distPath);
        if (null==file){
            System.out.println("空文件");
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try {
            /*文件保存到本地*/
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*返回文件真实路径*/
        return url;

    }

}
