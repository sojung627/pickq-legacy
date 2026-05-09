package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AuctionVO {

 private Long    		auctionIdx;				// PK
 private Long    		buyerIdx;				// FK → member.mem_idx
 private Integer 		itemCategoryIdx;		// FK → item_category
 private String    		auctionThumbnailImg;    // 경매 이미지
 private String  		auctionTitle;			// 경매 제목
 private String  		auctionDesc;			// 경매 설명
 private Long    		auctionTargetPrice;		// 희망 최대가 (nullable)
 private Long    		auctionViewCount;		// 조회수
 private LocalDateTime 	auctionStartAt;			// 경매 시작일시
 private LocalDateTime 	auctionEndAt;			// 입찰 마감일시
 private LocalDateTime 	auctionDecisionDeadline;// 결정 마감일
 private Integer 		auctionStatusIdx;		// FK → auction_status
 private LocalDateTime 	auctionRegdate;			// 등록일
 private LocalDateTime 	auctionModdate;    		// 수정일
 private String        	auctionIsDeleted;  		// 'Y'/'N' 소프트 딜리트
 private LocalDateTime 	auctionDeldate;    		// 삭제일

}
