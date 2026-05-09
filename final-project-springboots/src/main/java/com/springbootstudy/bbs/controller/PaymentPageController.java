package com.springbootstudy.bbs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.MemberAddrVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.service.AuctionService;
import com.springbootstudy.bbs.service.BidService;
import com.springbootstudy.bbs.service.MemberAddrService;
import com.springbootstudy.bbs.service.MyPageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentPageController {

  private final AuctionService auctionService;
  private final BidService bidService;
  private final MemberAddrService memberAddrService;
  private final MyPageService mypageService;

  // 1. 결제 페이지 (bid 정보 + 대표 배송지 함께 전달)
  @GetMapping("/pay")
  public String payPage(@RequestParam("auctionIdx") Long auctionIdx,
      HttpSession session, Model model) {

    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
    if (loginUser == null)
      return "redirect:/members/login";

    AuctionDTO auction = auctionService.auctionDetail(auctionIdx);
    if (auction == null || auction.getBuyerIdx() == null || !auction.getBuyerIdx().equals(loginUser.getMemIdx())) {
      return "redirect:/mypage/auctions";
    }

    // auctionIdx로 낙찰된 bidIdx 조회
    Long bidIdx = mypageService.getWonBidIdxByAuctionIdx(auctionIdx);
    if (bidIdx == null)
      return "redirect:/mypage/auctions";

    BidDTO bid = bidService.findBidById(bidIdx);
    if (bid == null)
      return "redirect:/auctions";

    MemberAddrVO addr = memberAddrService.getPrimaryAddr(loginUser.getMemIdx());
    String paymentReturnUrl = "/payment/pay?auctionIdx=" + auctionIdx;
    model.addAttribute("bid", bid);
    model.addAttribute("addr", addr);
    model.addAttribute("memTel", loginUser.getMemTel());
    model.addAttribute("paymentReturnUrl", paymentReturnUrl);

    return "views/payment/pay";
  }

  // 2. 결제 성공 페이지
  @GetMapping("/success")
  public String successPage(HttpSession session, Model model) {
    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
    if (loginUser == null)
      return "redirect:/members/login";

    MemberAddrVO addr = memberAddrService.getPrimaryAddr(loginUser.getMemIdx());
    model.addAttribute("addr", addr);
    model.addAttribute("memTel", loginUser.getMemTel());

    return "views/payment/pay_success";
  }

  // 3. 결제 실패 페이지
  @GetMapping("/fail")
  public String failPage(@RequestParam(value = "message", required = false) String message,
      HttpSession session, Model model) {
    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
    if (loginUser == null)
      return "redirect:/members/login";

    model.addAttribute("failMessage", message);
    return "views/payment/pay_fail";
  }
}