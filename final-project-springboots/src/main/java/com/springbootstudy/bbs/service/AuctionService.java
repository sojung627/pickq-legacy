package com.springbootstudy.bbs.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.mapper.AuctionMapper;
import com.springbootstudy.bbs.mapper.BidMapper;

@Service
public class AuctionService {
	
	@Autowired
	private AuctionMapper auctionMapper;
	
	@Autowired
	private BidMapper bidMapper;

	@Autowired
	private NotificationService notificationService;
	
	// 경매 리스트 조회 (검색 및 카테고리 필터링 포함)
	public List<AuctionDTO> AuctionList(String categoryCode, String keyword, String sortBy, String statusFilter) {
	    List<AuctionDTO> list = auctionMapper.auctionList(categoryCode, keyword, sortBy, statusFilter);
	    for (AuctionDTO dto : list) {
	        refine(dto);
	    }
	    return list;
	}
	
	// 경매 상세 정보 조회 
    public AuctionDTO auctionDetail(Long auctionIdx) {
        AuctionDTO dto = auctionMapper.auctionDetail(auctionIdx);
        if (dto != null) {
            refine(dto);
        }
        return dto;
    }
	
	// 경매 수동 마감 (구매자가 직접 종료)
	@Transactional
	public void closeAuction(Long auctionIdx, Long buyerIdx) {
	    AuctionDTO detail = auctionMapper.auctionDetail(auctionIdx);
	    if (detail == null || !detail.getBuyerIdx().equals(buyerIdx)) {
	        throw new IllegalArgumentException("권한이 없습니다.");
	    }
	    if (detail.getAuctionStatusIdx() != 1) {
	        throw new IllegalArgumentException("진행중인 경매만 마감할 수 있습니다.");
	    }
	    // 입찰 있으면 결정대기(2), 없으면 유찰(4)
	    int statusIdx = (detail.getBidCount() != null && detail.getBidCount() > 0) ? 2 : 4;
	    auctionMapper.updateAuctionStatus(auctionIdx, statusIdx);

	    if (statusIdx == 2) {
	        notificationService.notifyAuctionBidClosedToOwner(detail);
	        notificationService.notifyAuctionStatusChangedToBidders(
	            detail,
	            "AUCTION_BID_CLOSED",
	            "입찰 마감 알림",
	            "입찰이 마감되어 낙찰자 결정을 기다리게 되었습니다."
	        );
	    } else {
	        notificationService.notifyAuctionStatusChangedToOwner(
	            detail,
	            "AUCTION_FAILED",
	            "유찰 알림",
	            "입찰이 마감되었지만 제안이 없어 유찰되었습니다."
	        );
	    }
	}

	// 상태 직접 변경 (유찰 처리 등)
	public void updateAuctionStatus(Long auctionIdx, int statusIdx) {
	    auctionMapper.updateAuctionStatus(auctionIdx, statusIdx);
	}
	
	// 경매 데이터 가공 (남은 시간 계산 및 ID 마스킹)
	private void refine(AuctionDTO dto) {
	    LocalDateTime now = LocalDateTime.now();

	    if (dto.getAuctionStatusIdx() == 1 && dto.getAuctionEndAt() != null) {
	        Duration d = Duration.between(now, dto.getAuctionEndAt());
	        if (!d.isNegative()) {
	            dto.setTimeDisplay(formatDuration(d));
	        }
	        // 음수면 timeDisplay 비워둠 (updateExpiredAuctions가 곧 처리)

	    } else if (dto.getAuctionStatusIdx() == 2 && dto.getAuctionDecisionDeadline() != null) {
	        Duration d = Duration.between(now, dto.getAuctionDecisionDeadline());
	        if (!d.isNegative()) {
	            dto.setTimeDisplay("결정 " + formatDuration(d));
	        }
	        // 음수면 비워둠

	    } else {
	        dto.setTimeDisplay(""); // 마감/유찰/취소 등은 timeDisplay 없음
	    }
	    
	    // 구매자 ID 마스킹
	    maskBuyerMemId(dto);
	}
	
	// 구매자 ID 마스킹 처리
	private void maskBuyerMemId(AuctionDTO dto) {
	    String buyerMemId = dto.getBuyerMemId();
	    if (buyerMemId != null && buyerMemId.length() > 2) {
	        // 첫 2글자 + * + 마지막 1글자 형태로 마스킹 (예: user123 → us***3)
	        int len = buyerMemId.length();
	        dto.setBuyerMemIdMasked(buyerMemId.substring(0, 2) + "*".repeat(Math.max(1, len - 3)) + buyerMemId.substring(len - 1));
	    } else if (buyerMemId != null) {
	        // 3글자 미만은 간단하게 처리
	        dto.setBuyerMemIdMasked(buyerMemId.substring(0, 1) + "*".repeat(Math.max(1, buyerMemId.length() - 1)));
	    }
	}
	
	// 시간 포맷 편의 메서드(경매 데이터 가공 메서드)
	private String formatDuration(Duration d) {
		long totalSeconds = Math.max(0, d.getSeconds());
	    long days = totalSeconds / 86400;
	    long hours = (totalSeconds % 86400) / 3600;
	    long minutes = (totalSeconds % 3600) / 60;
	    long seconds = totalSeconds % 60;

	    if (days >= 1) {
	        return String.format("%d일 %d시간", days, hours);
	    }
	    if (hours >= 1) {
	        return String.format("%d시간 %d분", hours, minutes);
	    }
	    return String.format("%02d:%02d", minutes, seconds);
	}
	
	// 경매 등록 (구매 요청)
	@Transactional
	public void registerAuction(AuctionDTO dto) {

	    // 희망 최대가 검증
	    if (dto.getAuctionTargetPrice() == null || dto.getAuctionTargetPrice() <= 0) {
	        throw new IllegalArgumentException("희망 최대가는 0원보다 커야 합니다.");
	    }
	    if (dto.getAuctionTargetPrice() % 1000 != 0) {
	        throw new IllegalArgumentException("희망 최대가는 1000원 단위로 입력해야 합니다.");
	    }

	    // 입찰 마감일 검증
	    LocalDateTime now = LocalDateTime.now();
	    if (dto.getAuctionEndAt() == null || dto.getAuctionEndAt().isBefore(now)) {
	        throw new IllegalArgumentException("입찰 마감일은 현재 시간 이후여야 합니다.");
	    }

	    // 결정 마감일 검증
	    if (dto.getAuctionDecisionDeadline() == null || dto.getAuctionDecisionDeadline().isBefore(now)) {
	        throw new IllegalArgumentException("결정 마감일은 현재 시간 이후여야 합니다.");
	    }
	    if (dto.getAuctionDecisionDeadline().isBefore(dto.getAuctionEndAt())) {
	        throw new IllegalArgumentException("결정 마감일은 입찰 마감일 이후여야 합니다.");
	    }
	    LocalDateTime maxDeadline = dto.getAuctionEndAt().plusDays(3);
	    if (dto.getAuctionDecisionDeadline().isAfter(maxDeadline)) {
	        throw new IllegalArgumentException("결정 마감일은 입찰 마감일로부터 3일을 초과할 수 없습니다.");
	    }
	    
	    auctionMapper.insertAuction(dto);
	}
	
	// 마감 경매 상태 자동 업데이트
	@Transactional
	public void updateExpiredAuctions() {

	    // 입찰마감 지난 진행중(1) 경매 처리
	    List<AuctionDTO> expiredList = auctionMapper.findExpiredAuctions();
	    for (AuctionDTO dto : expiredList) {
	        int bidCount = dto.getBidCount() != null ? dto.getBidCount() : 0;
	        // 입찰 있으면 결정대기(2), 없으면 유찰(4)
	        int nextStatus = bidCount > 0 ? 2 : 4;
	        auctionMapper.updateAuctionStatus(dto.getAuctionIdx(), nextStatus);

	        if (nextStatus == 2) {
	            AuctionDTO detail = auctionMapper.auctionDetail(dto.getAuctionIdx());
	            if (detail != null) {
	                notificationService.notifyAuctionBidClosedToOwner(detail);
	                notificationService.notifyAuctionStatusChangedToBidders(
	                    detail,
	                    "AUCTION_BID_CLOSED",
	                    "입찰 마감 알림",
	                    "입찰이 마감되어 낙찰자 결정을 기다리게 되었습니다."
	                );
	            }
	        }
	    }

	    // 결정마감 지난 결정대기(2) → 유찰(4)
	    // (낙찰 선택 시 이미 3(마감)으로 바뀌므로 여기 오는 건 항상 낙찰자 없음)
	    List<AuctionDTO> expiredDecisions = auctionMapper.findExpiredDecisions();
	    for (AuctionDTO dto : expiredDecisions) {
	        auctionMapper.updateAuctionStatus(dto.getAuctionIdx(), 4);

	        AuctionDTO detail = auctionMapper.auctionDetail(dto.getAuctionIdx());
	        if (detail == null) {
	            continue;
	        }

	        notificationService.notifyAuctionDecisionClosedToOwner(detail);
	        notificationService.notifyAuctionStatusChangedToBidders(
	            detail,
	            "AUCTION_DECISION_CLOSED",
	            "결정 마감 알림",
	            "낙찰자 선정 기한이 종료되어 유찰 처리되었습니다."
	        );

	        List<Long> bidderIdxList = bidMapper.findDistinctBidderIdxByAuction(dto.getAuctionIdx());
	        for (Long bidderIdx : bidderIdxList) {
	            if (bidderIdx == null) {
	                continue;
	            }
	            notificationService.notifyDecisionDeadlineToBidder(detail, bidderIdx);
	        }
	    }
	}
	
	// 경매 취소 - 소프트 딜리트
	@Transactional
	public void deleteAuction(Long auctionIdx, Long buyerIdx) {
	    AuctionDTO detail = auctionMapper.auctionDetail(auctionIdx);
	    int result = auctionMapper.softDeleteAuction(auctionIdx, buyerIdx);
	    if (result == 0) {
	        throw new IllegalArgumentException("삭제 권한이 없거나 존재하지 않는 경매입니다.");
	    }
	    
	    // 해당 경매의 진행중 입찰 일괄 취소
	    bidMapper.cancelBidsByAuction(auctionIdx);
	    if (detail != null) {
	        notificationService.notifyAuctionStatusChangedToBidders(
	            detail,
	            "AUCTION_CANCELED",
	            "경매 취소 알림",
	            "참여하신 경매가 구매자에 의해 취소되었습니다."
	        );
	    }
	}
	
	// 관리자 경매 삭제
	@Transactional
	public void adminDeleteAuction(Long auctionIdx) {
	    AuctionDTO detail = auctionMapper.auctionDetail(auctionIdx);
	    int result = auctionMapper.adminDeleteAuction(auctionIdx);
	    if (result == 0) {
	        throw new IllegalArgumentException("존재하지 않는 경매입니다.");
	    }
	    if (detail != null) {
	        notificationService.notifyAuctionStatusChangedToBidders(
	            detail,
	            "AUCTION_DELETED",
	            "경매 삭제 알림",
	            "참여하신 경매가 관리자에 의해 삭제되었습니다."
	        );
	    }
	}
	
}
