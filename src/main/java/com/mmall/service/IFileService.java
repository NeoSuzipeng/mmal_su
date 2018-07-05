package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by 10353 on 2018/1/6.
 */
public interface IFileService {
    String upload(String path, MultipartFile file);
}
