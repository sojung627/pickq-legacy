package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.BidDTO;

@Mapper
public interface BidMapper {
    // 특정 경매글에 달린 입찰 리스트 조회
    List<BidDTO> BidList(Long auctionIdx);

    // 입찰 단건 상세 조회 (상세 페이지)
    BidDTO findBidById(@Param("bidIdx") Long bidIdx);

    // 입찰 시 아이템 정보 등록 (역경매 특성상 아이템 정보가 먼저 등록되어야 함)
    void insertItem(BidDTO bidDto);

    // 입찰 정보 등록
    int insertBid(BidDTO bidDto);

    // 입찰 취소 (소프트 딜리트: 입찰자 본인)
    int softDeleteBid(@Param("bidIdx") Long bidIdx, @Param("bidderIdx") Long bidderIdx);

    // 입찰 삭제 (관리자 전용: bidder_idx 조건 없음)
    int adminDeleteBid(@Param("bidIdx") Long bidIdx);

    // 특정 입찰 낙찰 처리
    int selectWinnerBid(@Param("bidIdx") Long bidIdx, @Param("auctionIdx") Long auctionIdx);

    // 낙찰된 입찰 외 나머지 입찰들 실패(거절) 처리
    int rejectOtherBids(@Param("auctionIdx") Long auctionIdx, @Param("bidIdx") Long bidIdx);

    // 경매 취소 시 해당 경매의 진행중 입찰 일괄 취소
    int cancelBidsByAuction(@Param("auctionIdx") Long auctionIdx);

    // 특정 경매에 입찰한 회원 목록
    List<Long> findDistinctBidderIdxByAuction(@Param("auctionIdx") Long auctionIdx);

}
