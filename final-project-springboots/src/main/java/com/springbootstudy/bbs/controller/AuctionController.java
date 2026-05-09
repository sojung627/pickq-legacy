package com.springbootstudy.bbs.controller;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.NotificationVO;
import com.springbootstudy.bbs.service.AuctionService;
import com.springbootstudy.bbs.service.BidService;
import com.springbootstudy.bbs.service.ChatRoomService;
import com.springbootstudy.bbs.service.NotificationService;
import com.springbootstudy.bbs.service.OrdersService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class AuctionController {

    // 이미지 저장 경로 & 기본 이미지 경로 상수
    private static final String AUCTION_UPLOAD_SUBDIR = "/src/main/resources/static/images/auction/";
    private static final String BID_UPLOAD_SUBDIR = "/src/main/resources/static/images/bid/";
    private static final String AUCTION_DEFAULT_IMG = "/images/auction/auction_default.png";

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private BidService bidService;

    @Autowired
    private ChatRoomService chatRoomService;
    
    @Autowired
    private OrdersService ordersService;
    
    @Autowired
    private NotificationService notificationService;

    // 경매 목록 (/auctions)
    @GetMapping("/auctions")
    public String auctionList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "open") String statusFilter,
            Model model) {

        auctionService.updateExpiredAuctions();
        List<AuctionDTO> list = auctionService.AuctionList(null, keyword, sortBy, statusFilter);

        model.addAttribute("auctionList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("statusFilter", statusFilter);
        return "views/auction/auctionList";
    }

    // 카테고리 필터 (/auctions/category/{categoryCode})
    @GetMapping("/auctions/category/{categoryCode}")
    public String auctionListByCategory(
            @PathVariable("categoryCode") String categoryCode,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,
            @RequestParam(value = "statusFilter", required = false, defaultValue = "open") String statusFilter,
            Model model) {

        auctionService.updateExpiredAuctions();
        List<AuctionDTO> list = auctionService.AuctionList(categoryCode, keyword, sortBy, statusFilter);

        model.addAttribute("auctionList", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryCode);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("statusFilter", statusFilter);
        return "views/auction/auctionList";
    }

    // 경매 상세 조회 (입찰 리스트 포함) (/auctions/{auctionIdx})
    @GetMapping("/auctions/{auctionIdx}")
    public String auctionDetail(
            @PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session,
            Model model) {

        auctionService.updateExpiredAuctions();

        AuctionDTO detail = auctionService.auctionDetail(auctionIdx);
        if (detail == null)
            return "redirect:/auctions";

        List<BidDTO> bidList = bidService.BidList(auctionIdx);

        model.addAttribute("detail", detail);
        model.addAttribute("bidList", bidList);
        model.addAttribute("mode", "list");

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        Long chatroomIdx = chatRoomService.prepareChatroomForAuction(
                auctionIdx, loginUser, detail);
        model.addAttribute("chatroomIdx", chatroomIdx);

        return "views/auction/auctionDetail";
    }

    // 경매 등록 폼 이동 (/auctions/new)
    @GetMapping("/auctions/new")
    public String registerForm(HttpSession session, RedirectAttributes ra) {

        if (session.getAttribute("loginUser") == null) {
            session.setAttribute("loginRedirectUrl", "/auctions/new");
            return "redirect:/members/login";
        }

        return "views/auction/auctionRegister";
    }

    // 경매 등록 실행 (POST /auctions)
    @PostMapping("/auctions")
    public String registerAction(AuctionDTO dto,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/members/login?redirect=/auctions/new";
        
        // DTO에 로그인한 사용자의 고유 번호(buyerIdx) 세팅
        dto.setBuyerIdx(loginUser.getMemIdx());

        // 파일 업로드 처리
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + AUCTION_UPLOAD_SUBDIR;
                File dir = new File(uploadDir);
                if (!dir.exists())
                    dir.mkdirs(); // 폴더 없으면 자동 생성

                String fileName = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
                thumbnailFile.transferTo(new File(uploadDir + fileName));

                dto.setAuctionThumbnailImg("/images/auction/" + fileName);
            } catch (Exception e) {
                log.error("경매 이미지 업로드 실패", e);
                dto.setAuctionThumbnailImg(AUCTION_DEFAULT_IMG); // 실패 시 기본 이미지
            }
        } else {
            dto.setAuctionThumbnailImg(AUCTION_DEFAULT_IMG); // 미첨부 시 기본 이미지
        }

        // 서비스 호출 및 예외 처리
        try {
            auctionService.registerAuction(dto);
        } catch (IllegalArgumentException e) {

            // 서비스 계층에서 던진 검증 에러 메시지를 화면으로 전달
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/auctions/new";

        } catch (Exception e) {
            log.error("경매 등록 에러", e);
            ra.addFlashAttribute("errorMessage", "등록 중 오류가 발생했습니다.");
            return "redirect:/auctions/new";
        }

        ra.addFlashAttribute("successMessage", "구매요청이 등록되었습니다! 🎉");
        NotificationVO noti = new NotificationVO();
        noti.setReceiverIdx(loginUser.getMemIdx());
        noti.setNotificationType("AUCTION_REGISTERED");
        noti.setNotificationTitle("구매요청이 등록되었습니다");
        noti.setNotificationMessage("\"" + dto.getAuctionTitle() + "\" 경매가 등록되었습니다.");
        noti.setTargetUrl("/auctions");
        notificationService.sendNotification(noti);

        return "redirect:/auctions";
    }

    // 경매 취소 (/auctions/{auctionIdx}/delete)
    @PostMapping("/auctions/{auctionIdx}/delete")
    public String deleteAuction(@PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        try {
            // 작성자 본인 확인은 서비스 계층에서 수행 (안전함)
            auctionService.deleteAuction(auctionIdx, loginUser.getMemIdx());
            ra.addFlashAttribute("successMessage", "구매요청이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        NotificationVO delNoti = new NotificationVO();
        delNoti.setReceiverIdx(loginUser.getMemIdx());
        delNoti.setNotificationType("AUCTION_DELETED");
        delNoti.setNotificationTitle("구매요청이 삭제되었습니다");
        delNoti.setNotificationMessage("경매가 삭제 처리되었습니다.");
        delNoti.setTargetUrl("/auctions");
        notificationService.sendNotification(delNoti);
        
        return "redirect:/auctions";
    }

    // 관리자 경매 삭제 (/auctions/{auctionIdx}/admin-delete)
    @PostMapping("/auctions/{auctionIdx}/admin-delete")
    public String adminDeleteAuction(@PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/members/login";

        // 관리자(memRoleIdx == 2)만 접근 가능
        if (loginUser.getMemRoleIdx() == null || loginUser.getMemRoleIdx() != 2) {
            ra.addFlashAttribute("errorMessage", "관리자만 사용할 수 있는 기능입니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        try {
            auctionService.adminDeleteAuction(auctionIdx);
            ra.addFlashAttribute("successMessage", "관리자 권한으로 경매가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auctions";
    }

    // 경매 수동 마감 (/auctions/{auctionIdx}/close)
    @PostMapping("/auctions/{auctionIdx}/close")
    public String closeAuction(@PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/members/login";

        AuctionDTO detail = auctionService.auctionDetail(auctionIdx);
        if (detail == null || !detail.getBuyerIdx().equals(loginUser.getMemIdx())) {
            ra.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        try {
            auctionService.closeAuction(auctionIdx, loginUser.getMemIdx());
            ra.addFlashAttribute("successMessage", "경매가 마감되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auctions/" + auctionIdx;
    }

    // 유찰 처리 (/auctions/{auctionIdx}/fail)
    @PostMapping("/auctions/{auctionIdx}/fail")
    public String failAuction(@PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        AuctionDTO detail = auctionService.auctionDetail(auctionIdx);
        if (detail == null || !detail.getBuyerIdx().equals(loginUser.getMemIdx())) {
            ra.addFlashAttribute("errorMessage", "권한이 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        auctionService.updateAuctionStatus(auctionIdx, 3);
        ra.addFlashAttribute("successMessage", "유찰 처리되었습니다.");
        return "redirect:/auctions/" + auctionIdx;
    }

    // 입찰 폼 페이지 (/auctions/{auctionIdx}/bids GET)
    @GetMapping("/auctions/{auctionIdx}/bids")
    public String bidRegisterForm(
            @PathVariable("auctionIdx") Long auctionIdx,
            HttpSession session, Model model,
            RedirectAttributes ra) {

        auctionService.updateExpiredAuctions();

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {

            session.setAttribute("loginRedirectUrl", "/auctions/" + auctionIdx + "/bids");

            return "redirect:/members/login";
        }

        AuctionDTO detail = auctionService.auctionDetail(auctionIdx);
        if (detail == null)
            return "redirect:/auctions";

        // 구매자 본인은 입찰 불가 → 상세로 리다이렉트
        if (detail.getBuyerIdx().equals(loginUser.getMemIdx())) {
            ra.addFlashAttribute("bidError", "본인이 등록한 경매에는 입찰할 수 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        // 진행중(1)이 아니면 입찰 불가
        if (detail.getAuctionStatusIdx() != 1) {
            ra.addFlashAttribute("bidError", "진행중인 경매에만 입찰할 수 있습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        List<BidDTO> bidList = bidService.BidList(auctionIdx);

        model.addAttribute("detail", detail);
        model.addAttribute("bidList", bidList);
        model.addAttribute("mode", "bidForm"); // 오른쪽 패널: 입찰 폼
        return "views/bid/bidRegister";
    }

    // 입찰 등록 (/auctions/{auctionIdx}/bids POST)
    @PostMapping("/auctions/{auctionIdx}/bids")
    public String registerBid(@PathVariable("auctionIdx") Long auctionIdx,
            BidDTO bidDto,
            @RequestParam(value = "bidImageFile", required = false) MultipartFile bidImageFile,
            HttpSession session,
            RedirectAttributes ra) {
        auctionService.updateExpiredAuctions();

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            session.setAttribute("loginRedirectUrl", "/auctions/" + auctionIdx);
            return "redirect:/members/login";
        }

        // 기본 데이터 세팅
        bidDto.setAuctionIdx(auctionIdx);
        bidDto.setBidderIdx(loginUser.getMemIdx());

        // 경매 정보 조회 (검증용)
        AuctionDTO auction = auctionService.auctionDetail(auctionIdx);
        if (auction == null)
            return "redirect:/auctions";

        // 본인 경매 입찰 방지
        if (java.util.Objects.equals(auction.getBuyerIdx(), loginUser.getMemIdx())) {
            ra.addFlashAttribute("bidError", "본인이 등록한 경매에는 입찰할 수 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        // 희망 최대가 초과 방지
        if (bidDto.getBidPrice() != null && auction.getAuctionTargetPrice() != null
                && bidDto.getBidPrice() > auction.getAuctionTargetPrice()) {
            ra.addFlashAttribute("bidError", "구매자의 희망가보다 높은 금액은 제안할 수 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        // 입찰 이미지 업로드 처리
        if (bidImageFile != null && !bidImageFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + BID_UPLOAD_SUBDIR;
                File dir = new File(uploadDir);
                if (!dir.exists())
                    dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + bidImageFile.getOriginalFilename();
                bidImageFile.transferTo(new File(uploadDir + fileName));

                bidDto.setItemThumbnailImg("/images/bid/" + fileName);
            } catch (Exception e) {
                log.error("입찰 이미지 업로드 실패", e);
                // 업로드 실패 시 에러 반환 (기본이미지 없음)
                ra.addFlashAttribute("bidError", "이미지 업로드에 실패했습니다. 다시 시도해주세요.");
                return "redirect:/auctions/" + auctionIdx;
            }
        } else {
            // JS에서 막지만 혹시 모를 직접 요청 방어
            ra.addFlashAttribute("bidError", "제안 상품 이미지는 필수입니다.");
            return "redirect:/auctions/" + auctionIdx + "/bids";
        }

        // itemCategoryIdx는 경매에서 자동 세팅
        bidDto.setItemCategoryIdx(auction.getItemCategoryIdx());

        // 서비스 호출
        try {
            bidService.registerBid(bidDto);
            ra.addFlashAttribute("successMessage", "입찰 제안이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("bidError", e.getMessage());
            return "redirect:/auctions/" + auctionIdx;
        }

        // 구매자에게 알림 (1번만)
        notificationService.notifyNewBidToAuctionWriter(auction, bidDto);

        // 판매자 본인 알림 DB 저장 (푸시 없이 - 이미 flash toast 있음)
        NotificationVO myNoti = new NotificationVO();
        myNoti.setReceiverIdx(loginUser.getMemIdx());
        myNoti.setNotificationType("BID_SUBMITTED");
        myNoti.setNotificationTitle("입찰 제안이 등록되었습니다");
        myNoti.setNotificationMessage("\"" + auction.getAuctionTitle() + "\" 에 입찰 제안을 등록했습니다.");
        myNoti.setTargetUrl("/auctions/" + auctionIdx);
        myNoti.setCreatedAt(java.time.LocalDateTime.now());
        myNoti.setIsRead("N");
        notificationService.sendNotification(myNoti);  // DB 저장만, 푸시 없음
        
        return "redirect:/auctions/" + auctionIdx;
    }

    // 입찰 상세 (/auctions/{auctionIdx}/bids/{bidIdx})
    @GetMapping("/auctions/{auctionIdx}/bids/{bidIdx}")
    public String bidDetailPanel(
            @PathVariable("auctionIdx") Long auctionIdx,
            @PathVariable("bidIdx") Long bidIdx,
            HttpSession session, Model model,
            RedirectAttributes ra) {

        AuctionDTO detail = auctionService.auctionDetail(auctionIdx);
        if (detail == null)
            return "redirect:/auctions";

        BidDTO selectedBid = bidService.findBidById(bidIdx);
        if (selectedBid == null)
            return "redirect:/auctions/" + auctionIdx;

        List<BidDTO> bidList = bidService.BidList(auctionIdx);

        model.addAttribute("detail", detail);
        model.addAttribute("bidList", bidList);
        model.addAttribute("selectedBid", selectedBid);
        model.addAttribute("mode", "bidDetail");
        return "views/auction/auctionDetail";
    }

    // 입찰 취소 (/bids/{bidIdx}/cancel)
    @PostMapping("/bids/{bidIdx}/cancel")
    public String cancelBid(@PathVariable("bidIdx") Long bidIdx,
            @RequestParam("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null) {
            session.setAttribute("loginRedirectUrl", "/auctions/" + auctionIdx);
            return "redirect:/members/login";
        }

        try {
            bidService.deleteBid(bidIdx, loginUser.getMemIdx());
            ra.addFlashAttribute("successMessage", "입찰이 취소되었습니다.");

            NotificationVO cancelNoti = new NotificationVO();
            cancelNoti.setReceiverIdx(loginUser.getMemIdx());
            cancelNoti.setNotificationType("BID_CANCELED");
            cancelNoti.setNotificationTitle("입찰이 취소되었습니다");
            cancelNoti.setNotificationMessage("입찰 제안이 취소되었습니다.");
            cancelNoti.setTargetUrl("/auctions/" + auctionIdx);
            notificationService.sendNotification(cancelNoti);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("bidError", e.getMessage());
            return "redirect:/auctions/" + auctionIdx;
        }
        
        return "redirect:/auctions/" + auctionIdx;
    }

    // 관리자 입찰 삭제 (/bids/{bidIdx}/admin-cancel)
    @PostMapping("/bids/{bidIdx}/admin-cancel")
    public String adminDeleteBid(@PathVariable("bidIdx") Long bidIdx,
            @RequestParam("auctionIdx") Long auctionIdx,
            HttpSession session,
            RedirectAttributes ra) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser == null)
            return "redirect:/members/login";

        if (loginUser.getMemRoleIdx() == null || loginUser.getMemRoleIdx() != 2) {
            ra.addFlashAttribute("errorMessage", "관리자만 사용할 수 있는 기능입니다.");
            return "redirect:/auctions/" + auctionIdx;
        }

        try {
            bidService.adminDeleteBid(bidIdx);
            ra.addFlashAttribute("successMessage", "관리자 권한으로 입찰이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("bidError", e.getMessage());
        }
        return "redirect:/auctions/" + auctionIdx;
    }

    // 낙찰 처리 (/auctions/{auctionIdx}/bids/{bidIdx}/win)
    @PostMapping("/auctions/{auctionIdx}/bids/{bidIdx}/win")
    public String selectWinner(@PathVariable("auctionIdx") Long auctionIdx,
                                 @PathVariable("bidIdx") Long bidIdx,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");

        // 세션 만료 → 로그인 페이지로 (loginRedirectUrl 경매 상세로 설정)
        if (loginUser == null) {
            session.setAttribute("loginRedirectUrl", "/auctions/" + auctionIdx);
            ra.addFlashAttribute("errorMessage", "로그인이 필요합니다. 다시 로그인 후 낙찰을 진행해주세요.");
            return "redirect:/members/login";
        }
        
        AuctionDTO auction = auctionService.auctionDetail(auctionIdx);
        if (auction == null || !auction.getBuyerIdx().equals(loginUser.getMemIdx())) {
            ra.addFlashAttribute("bidError", "권한이 없습니다.");
            return "redirect:/auctions/" + auctionIdx;
        }
        
        BidDTO winnerBid = null;
        try {
            bidService.selectWinner(bidIdx, auctionIdx);
            winnerBid = bidService.findBidById(bidIdx);
            ordersService.createOrderOnWinner(auction, winnerBid);
            auctionService.updateAuctionStatus(auctionIdx, 3);
            ra.addFlashAttribute("successMessage", "낙찰자가 선정되었습니다.\n24시간 내에 결제를 완료해주세요.\n미결제 시 패널티가 부여됩니다.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("bidError", e.getMessage());
        } catch (Exception e) {
            log.error("낙찰 처리 중 오류", e);
            ra.addFlashAttribute("bidError", "낙찰 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
        }

         // 판매자 본인은 flashAttribute 토스트가 이미 뜨므로 DB 저장만
         if (winnerBid != null) {
             notificationService.notifyWinnerSelectedToBidder(auctionIdx, winnerBid);
             notificationService.notifyAuctionStatusChangedToBidders(
                 auction,
                 "AUCTION_WINNER_SELECTED",
                 "낙찰자 선정",
                 "다른 입찰자가 낙찰자로 선정되어 경매가 종료되었습니다.",
                 winnerBid.getBidderIdx()
             );

             NotificationVO buyerNoti = new NotificationVO();
             buyerNoti.setReceiverIdx(loginUser.getMemIdx());
             buyerNoti.setNotificationType("WINNER_SELECTED");
             buyerNoti.setNotificationTitle("낙찰자 선정 완료");
             buyerNoti.setNotificationMessage("낙찰자를 선정했습니다. 거래를 진행하세요.");
             buyerNoti.setTargetUrl("/auctions/" + auctionIdx);
             notificationService.sendNotification(buyerNoti);
         }
        
        return "redirect:/mypage/orders";
    }
}