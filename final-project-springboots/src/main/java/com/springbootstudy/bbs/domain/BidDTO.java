package com.springbootstudy.bbs.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BidDTO {

    // Bid 기본 정보
    private Long bidIdx;          // 입찰 번호 (PK)
    private Long auctionIdx;      // 경매 번호 (FK)
    private Long bidderIdx;       // 입찰자 회원 번호 (FK)
    private String auctionTitle;  // 경매 제목

    // 입찰 내용
    private Long bidPrice;        // 제안 가격
    private Integer bidQuantity;
    private String bidMessage;    // 상세페이지에서 사용
    
    // 입찰 상태
    private Integer bidStatusIdx;     // 상태 코드 (1: 일반, 2: 낙찰, 3: 실패, 4: 취소, 5: 삭제)
    private String bidStatusName;     // 상태 이름 (한글)
    
    // 입찰자 정보 (마스킹 및 등급 정보)
    private String memName;       // 뷰에서 가져온 실명 → 구매자에게 보여줄 때 (bid.memName)
    private String realMemName;   // 뷰에서 가져온 실명 → 입찰 상세에서 사용 (bid.realMemName)
    private String bidderName;    // 서비스에서 마스킹해서 채워줌 → 일반 사용자에게 보여줄 때 (bid.bidderName)
    
    // 입찰자 ID 및 등급 정보
    private String bidderMemId;   // 입찰자 회원 아이디 (마스킹 전)
    private String bidderMemIdMasked; // 입찰자 회원 아이디 (마스킹됨: 일부가림*처리)
    private Integer bidderGradeIdx; // 입찰자 등급 인덱스
    private String bidderGradeName; // 입찰자 등급명 (bronze, silver, gold, 등)
    private Double bidderAverageRating; // 입찰자 평균 별점

    // 아이템 정보 (상세페이지용)
    private Long itemIdx;
    private Integer itemCategoryIdx;   // insertItem 시 사용
    private String itemName;
    private String itemBrand;
    private String itemThumbnailImg;
    private String itemCategoryName;   // bid_list_view에서 JOIN으로 조회

    // 시간 정보
    private LocalDateTime bidRegdate; // 입찰 등록일
    
}