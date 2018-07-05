package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by 10353 on 2018/1/6.
 */
@Service("iFileService")
public class IFileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(IFileServiceImpl.class);

    public String upload(String path, MultipartFile file){

        String fileName = file.getOriginalFilename();

        //获取拓展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);

        //定制唯一文件名
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("上传文件开始：上传的文件名{},上传的路径{},上传的新文件名{}",fileName,path,uploadFileName);

        //Tomcat中创建文件
        File fileDir = new File(path);
        if(!fileDir.exists()){
            //赋予写权限,默认没有写权限
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadFileName);

        try {
            //图片在tomcat保存完成
            file.transferTo(targetFile);
            //图片上传至FTP服务器
            boolean isSuccess = FTPUtil.upload(Lists.newArrayList(targetFile));
            //清除本地残余
            targetFile.delete();

            if(!isSuccess)
                return null;
        } catch (IOException e) {
            logger.error("上传文件异常 : " + e);
            return null;
        }
        return targetFile.getName();
    }
}
