package com.springbootstudy.bbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.BoardVO;
import com.springbootstudy.bbs.domain.MyPagePaymentVO;
import com.springbootstudy.bbs.domain.PaymentVO;
import com.springbootstudy.bbs.mapper.MyPageMapper;

@Service
public class MyPageService {
  @Autowired
  private MyPageMapper mypageMapper;

  // 내 경매 목록
  public List<AuctionDTO> getMyAuctions(Long memIdx) {
    return mypageMapper.getMyAuctions(memIdx);
  }

  // 내 입찰 목록
  public List<BidDTO> getMyBids(Long memIdx) {
    return mypageMapper.getMyBids(memIdx);
  }

  // 내 게시글 목록
  public List<BoardVO> getMyBoards(Long memIdx) {
    return mypageMapper.getMyBoards(memIdx);
  }

  public List<PaymentVO> getMyWonBids(Long memIdx) {
    return mypageMapper.getMyWonBids(memIdx);
  }

  public Long getWonBidIdxByAuctionIdx(Long auctionIdx) {
    return mypageMapper.getWonBidIdxByAuctionIdx(auctionIdx);
  }

  public List<MyPagePaymentVO> getMyPayments(Long memIdx) {
    return mypageMapper.getMyPayments(memIdx);
  }

  public List<MyPagePaymentVO> getMySales(Long memIdx) {
    return mypageMapper.getMySales(memIdx);
  }
}
