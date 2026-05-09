package com.springbootstudy.bbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.dto.ProfilePageDTO;
import com.springbootstudy.bbs.dto.ProfileReviewDTO;
import com.springbootstudy.bbs.dto.ProfileReviewDetailDTO;
import com.springbootstudy.bbs.service.AuctionService;
import com.springbootstudy.bbs.service.ChatRoomService;
import com.springbootstudy.bbs.service.MemberProfileService;
import com.springbootstudy.bbs.service.ReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileViewController {

    @Autowired
    private MemberProfileService memberProfileService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private HttpSession session;

    @GetMapping("/profile/{memIdx}")
    public String profilePage(
            @PathVariable("memIdx") Long memIdx,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "auctionId", required = false) Long auctionId,
            Model model) {

        ProfilePageDTO profile = memberProfileService.getProfilePage(memIdx);
        if (profile == null) {
            return "redirect:/auctions";
        }

        // 입찰내역과 동일한 마스킹 규칙: 앞2글자 + 마스킹 + 끝1글자
        profile.setMaskedMemId(maskMemId(profile.getMemId()));

        List<ProfileReviewDTO> reviews = reviewService.getReceivedReviewsForProfile(memIdx);

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        Long profileChatroomIdx = null;
        if (loginUser != null
                && !loginUser.getMemIdx().equals(memIdx)
                && "auction".equals(from)
                && auctionId != null) {

            AuctionDTO auction = auctionService.auctionDetail(auctionId);
            if (auction != null && auction.getBuyerIdx() != null) {
                Long buyerIdx = auction.getBuyerIdx();
                Long loginMemIdx = loginUser.getMemIdx();

                if (loginMemIdx.equals(buyerIdx)) {
                    profileChatroomIdx = chatRoomService.prepareChatroomForMembers(
                            auctionId,
                            buyerIdx,
                            memIdx
                    );
                } else if (memIdx.equals(buyerIdx)) {
                    profileChatroomIdx = chatRoomService.prepareChatroomForMembers(
                            auctionId,
                            buyerIdx,
                            loginMemIdx
                    );
                }
            }
        }

        model.addAttribute("profile", profile);
        model.addAttribute("reviews", reviews);
        model.addAttribute("from", from);
        model.addAttribute("auctionId", auctionId);
        model.addAttribute("profileChatroomIdx", profileChatroomIdx);

        // 어디서 왔는지(from) 추적해서 뒤로가기 목적지 보존
        String profileBackUrl = null;
        if ("auction".equals(from) && auctionId != null) {
            profileBackUrl = "/auctions/" + auctionId;
        }
        model.addAttribute("profileBackUrl", profileBackUrl);

        return "views/profile/profile";
    }

    @GetMapping("/reviews/detail/{reviewIdx}")
    public String reviewDetailPage(
            @PathVariable("reviewIdx") Long reviewIdx,
            @RequestParam(value = "fromProfile", required = false) Boolean fromProfile,
            @RequestParam(value = "memIdx", required = false) Long memIdx,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "auctionId", required = false) Long auctionId,
            Model model) {

        ProfileReviewDetailDTO review = reviewService.getProfileReviewDetail(reviewIdx);
        if (review == null) {
            return "redirect:/auctions";
        }

        Long profileMemIdx = memIdx != null ? memIdx : review.getProfileMemIdx();

        model.addAttribute("review", review);
        model.addAttribute("fromProfile", fromProfile != null ? fromProfile : false);
        model.addAttribute("memIdx", profileMemIdx);
        model.addAttribute("from", from);
        model.addAttribute("auctionId", auctionId);

        // 리뷰 상세 -> 프로필 뒤로가기 목적지(진입 파라미터 유지)
        String reviewBackUrl = "/profile/" + profileMemIdx;
        if (from != null || auctionId != null) {
            StringBuilder sb = new StringBuilder(reviewBackUrl);
            sb.append("?");
            boolean hasPrev = false;
            if (from != null && !from.isBlank()) {
                sb.append("from=").append(from);
                hasPrev = true;
            }
            if (auctionId != null) {
                if (hasPrev) {
                    sb.append("&");
                }
                sb.append("auctionId=").append(auctionId);
            }
            reviewBackUrl = sb.toString();
        }
        model.addAttribute("reviewBackUrl", reviewBackUrl);

        return "views/profile/getReview";
    }

    private String maskMemId(String memId) {
        if (memId == null || memId.isBlank()) {
            return "-";
        }
        if (memId.length() > 2) {
            int len = memId.length();
            return memId.substring(0, 2)
                    + "*".repeat(Math.max(1, len - 3))
                    + memId.substring(len - 1);
        }
        return memId.substring(0, 1) + "*".repeat(Math.max(1, memId.length() - 1));
    }
}
