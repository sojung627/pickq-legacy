package com.springbootstudy.bbs.domain;

import lombok.Data;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@Data
public class AuctionDTO {

    // Auction 기본 정보
    private Long auctionIdx;
    private String auctionTitle;
    private Long auctionTargetPrice;
    private String auctionDesc;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime auctionEndAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime auctionDecisionDeadline;
    private LocalDateTime auctionRegdate; // 리스트 정렬 및 날짜 표시용

    private Integer auctionStatusIdx;
    private String auctionStatusName;

    // 카테고리
    private Integer itemCategoryIdx;
    private String itemCategoryName;
    private String itemCategoryCode;   // URL용 카테고리 코드 (ball, racket 등)

    // 이미지
    private String auctionThumbnailImg;

    // 입찰 집계 (리스트용 최소 정보)
    private Long minBidPrice;
    private Integer bidCount;

    // 마이페이지 경매 목록용 낙찰 정보
    private Long winnerBidIdx;
    private String winnerItemName;
    private Long winnerBidPrice;
    private String winnerBidderMemId;
    private String winnerBidderMemIdMasked;

    // 작성자
    private Long buyerIdx;
    private String buyerMemId;     // 구매자 회원 아이디 (마스킹 전)
    private String buyerMemIdMasked; // 구매자 회원 아이디 (마스킹됨)

    // 서비스 가공 데이터
    // private int discountRate;
    private String timeDisplay;
    private String statusBadge;
    private Long auctionViewCount;   // ← 추가
}