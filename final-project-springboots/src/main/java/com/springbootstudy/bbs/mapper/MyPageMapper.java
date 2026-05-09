package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.BoardVO;
import com.springbootstudy.bbs.domain.MyPagePaymentVO;
import com.springbootstudy.bbs.domain.PaymentVO;

@Mapper
public interface MyPageMapper {
  // 내 경매 목록
  List<AuctionDTO> getMyAuctions(@Param("memIdx") Long memIdx);

  // 내 입찰 목록
  List<BidDTO> getMyBids(@Param("memIdx") Long memIdx);

  // 내 게시글 목록
  List<BoardVO> getMyBoards(@Param("memIdx") Long memIdx);

  List<PaymentVO> getMyWonBids(@Param("memIdx") Long memIdx);

  Long getWonBidIdxByAuctionIdx(@Param("auctionIdx") Long auctionIdx);

  List<MyPagePaymentVO> getMyPayments(@Param("memIdx") Long memIdx);

  List<MyPagePaymentVO> getMySales(@Param("memIdx") Long memIdx);
}
