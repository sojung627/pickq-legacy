package com.springbootstudy.bbs.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewVO {
	private Long reviewIdx;	// pk - 리뷰
	
	private Long buyerIdx;   // fk - 구매자(리뷰하는 자)
	private Long bidderIdx;  // fk - 입찰자(리뷰 대상자)
	private Long auctionIdx; // fk - 역경매(해당 경매)
	private Long bidIdx; 	  // fk - 입찰(해당 입찰)
	
	private String reviewTitle;	  // 리뷰 제목
	private String reviewContent; // 리뷰 내용
	private int reviewStar;	  	  // 별점
	
	private LocalDateTime reviewRegdate; // 리뷰 작성일
	private String reviewIsDeleted;		 // 리뷰 삭제 여부 - default N (N / Y)
	private LocalDateTime reviewDelete;  // 리뷰 삭제일
	
	// 리뷰 게시를 위한 추가 필드
	private String content;
	private String auctionTitle;
	private String itemName;
	
	// 리뷰 남기기용
	public void setReviewStar(int reviewStar) { this.reviewStar = reviewStar; }  
	public void setContent(String content) { this.content = content; }
	public Long getBuyerIdx() { return buyerIdx; }
	public void setBuyerIdx(Long buyerIdx) { this.buyerIdx = buyerIdx; }  
	
	// 리뷰 조회
	public LocalDateTime getReviewRegdate() { return reviewRegdate; }
	
	// 리뷰 상세보기
	private int bidPrice;
	private String memName;
	private LocalDateTime bidRegdate;
	private Integer auctionTargetPrice;
	private String bidderName; 
	
}
