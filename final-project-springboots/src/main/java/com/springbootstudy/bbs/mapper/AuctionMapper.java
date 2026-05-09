package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.AuctionDTO;

@Mapper
public interface AuctionMapper {
	
	// 경매 구매요청 전체 리스트 조회 (카테고리 코드 + 키워드 검색)
    List<AuctionDTO> auctionList(@Param("categoryCode") String categoryCode, 
    		@Param("keyword") String keyword,@Param("sortBy") String sortBy, @Param("statusFilter") String statusFilter);

    // 구매요청 상세보기 조회
    AuctionDTO auctionDetail(Long auctionIdx);

    // 경매글 등록
    int insertAuction(AuctionDTO dto);
    
    // 마감 시간이 지난 경매 조회 (스케줄러용)
    List<AuctionDTO> findExpiredAuctions();
    
    // 결정마감일이 지난 결정대기 경매 조회 (결정대기 → 마감/유찰 처리)
    List<AuctionDTO> findExpiredDecisions();
    
    // 경매 상태 변경 (진행중 -> 마감/유찰 등)
    int updateAuctionStatus(@Param("auctionIdx") Long auctionIdx, @Param("statusIdx") int statusIdx);
    
    // 경매 취소 (소프트 딜리트: 구매자 본인)
    int softDeleteAuction(@Param("auctionIdx") Long auctionIdx, @Param("buyerIdx") Long buyerIdx);

    // 경매 삭제 (관리자 전용: buyer_idx 조건 없음)
    int adminDeleteAuction(@Param("auctionIdx") Long auctionIdx);
    
    // 경매 수정
    int updateAuction(AuctionDTO dto);
    
}
