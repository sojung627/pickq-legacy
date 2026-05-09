package com.springbootstudy.bbs.domain;

import lombok.Data;
import java.time.LocalDateTime;

// 회원 패널티 테이블
@Data
public class MemberPenaltyVO {

	private Long 			penaltyIdx; 	// PK
	private Long 			memIdx; 		// 패널티 받은 회원 (FK → member.mem_idx)
	private Long 			auctionIdx; 	// 관련 경매 (FK → auction.auction_idx, nullable)
	private Long 			bidIdx; 		// 관련 입찰 (FK → bid.bid_idx, nullable)
	private String 			penaltyCode; 	// 패널티 코드 (NO_PAYMENT, NO_SHIPMENT, LATE_CANCEL 등)
	private String 			penaltyReason; 	// 추가 설명/메모 (선택)
	private Integer 		penaltyScore; 	// 패널티 점수/카운트 (보통 1 고정, 필요시 가중치)
	private LocalDateTime 	createdAt; 		// 부과 일시
}
