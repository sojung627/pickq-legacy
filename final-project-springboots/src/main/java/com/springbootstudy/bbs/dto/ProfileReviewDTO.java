package com.springbootstudy.bbs.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProfileReviewDTO {
    private Long reviewIdx;
    private String reviewTitle;
    private Integer reviewStar;
    private LocalDateTime reviewRegdate;
    private String auctionTitle;
    private String itemName;
    private Long reviewerMemIdx;
    private String reviewerNickname;
}
