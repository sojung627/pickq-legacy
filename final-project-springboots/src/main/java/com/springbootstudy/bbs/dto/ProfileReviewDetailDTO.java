package com.springbootstudy.bbs.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProfileReviewDetailDTO {
    private Long reviewIdx;
    private Long profileMemIdx;
    private Long reviewerMemIdx;

    private String reviewTitle;
    private String reviewContent;
    private Integer reviewStar;
    private LocalDateTime reviewRegdate;

    private Long auctionIdx;
    private Long bidIdx;
    private String auctionTitle;
    private String itemName;
    private LocalDateTime tradeDate;

    private String reviewerNickname;
    private String reviewerGradeName;
    private String profileNickname;
    private String profileGradeName;
}
