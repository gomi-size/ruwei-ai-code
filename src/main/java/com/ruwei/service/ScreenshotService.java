package com.ruwei.service;

import org.springframework.stereotype.Service;

@Service
public interface ScreenshotService {


    /**
     * 通用的截图服务，可以得到访问地址
     * @param url
     * @return
     */
    String generateAndUploadScreenshot( String url);
}
