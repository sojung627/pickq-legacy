package com.springbootstudy.bbs.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ImageUploadController {

    private static final String WEB_PATH = "/images/board/";

    @PostMapping("/summernoteImageUpload")
    @ResponseBody
    public Map<String, Object> imageUpload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        Map<String, Object> result = new HashMap<>();

        if (file.isEmpty()) {
            result.put("error", "파일이 없습니다.");
            return result;
        }

        String uploadDir = System.getProperty("user.dir")
                         + "/src/main/resources/static/images/board/";

        log.info("=== 이미지 업로드 경로: " + uploadDir);  // 확인용 로그

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        file.transferTo(new File(uploadDir + fileName));

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                       + ":" + request.getServerPort();
        result.put("url", baseUrl + WEB_PATH + fileName);
        return result;
    }
}