package com.springbootstudy.bbs.dto;

import lombok.Data;

@Data
public class ProfilePageDTO {
    private Long memIdx;
    private String memId;
    private String maskedMemId;
    private String memName;
    private String memNickname;
    private String memIntro;
    private String memImg;
    private String gradeName;
    private Double avgRating;
    private Integer reviewCount;
}
