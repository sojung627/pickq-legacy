package com.springbootstudy.bbs.controller;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.NotificationVO;
import com.springbootstudy.bbs.service.NotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
public class NotificationController {

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private HttpSession session;

	
	// 내 알림 목록 페이지 - 전체
	@GetMapping("/notifications")
	public String notifications(HttpSession session, Model model) {

	    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "redirect:/member/login";
	    }

	    Long memIdx = loginUser.getMemIdx();

	    // 전체 알림 목록
	    List<NotificationVO> notifications = notificationService.getNotificationsForMember(memIdx);
	    // 읽지 않은 개수 (hasUnread만 있다면 countUnread 추가해도 됨)
	    int unreadCount = notificationService.getUnreadCount(memIdx);

	    model.addAttribute("notifications", notifications);
	    model.addAttribute("unreadCount", unreadCount);
	    model.addAttribute("filter", "all"); // 어떤 탭인지 표시용

	    return "views/notification/notifications";
	}

	// 내 알림 목록 페이지 - 읽지 않은 것만
	@GetMapping("/notifications/unread")
	public String unreadNotifications(HttpSession session, Model model) {

	    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "redirect:/member/login";
	    }

	    Long memIdx = loginUser.getMemIdx();

	    // 전체 알림을 가져오고, 뷰에서 isRead == 'N'만 보여줄 것
	    List<NotificationVO> notifications = notificationService.getNotificationsForMember(memIdx);
	    int unreadCount = notificationService.getUnreadCount(memIdx);

	    model.addAttribute("notifications", notifications);
	    model.addAttribute("unreadCount", unreadCount);
	    model.addAttribute("filter", "unread");

	    return "views/notification/notifications";
	}
	
	// 읽지 않은 알림이 하나라도 있을 때 (헤더에 표시용)
	@GetMapping("/notifications/has-unread")
	@ResponseBody
	public String hasUnread(HttpSession session) {

	    MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
	    if (loginUser == null) {
	        return "N"; // 비로그인이면 알림 없음으로 처리
	    }

	    Long memIdx = loginUser.getMemIdx();
	    boolean hasUnread = notificationService.hasUnread(memIdx);

	    return hasUnread ? "Y" : "N";
	}

	// 헤더 미리보기용 최근 알림 2개 조회
	@GetMapping("/notifications/recent")
	@ResponseBody
	public ResponseEntity<List<NotificationVO>> recentNotifications(HttpSession session,
			@RequestParam(name = "limit", defaultValue = "2") int limit) {

		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
		}

		int safeLimit = Math.max(1, Math.min(limit, 5));
		Long memIdx = loginUser.getMemIdx();
		List<NotificationVO> recent = notificationService.getRecentNotificationsForMember(memIdx, safeLimit);
		return ResponseEntity.ok(recent);
	}


	 // 알림 단건 읽음 처리 (AJAX)
	@PostMapping("/notifications/read")
	@ResponseBody
	public String markAsRead(@RequestParam("notificationIdx") Long notificationIdx, HttpSession session) {

		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "NOT_LOGIN";
		}

		notificationService.markAsRead(notificationIdx);
		return "OK";
	}

	
	 // 내 알림 전체 읽음 처리 (AJAX)
	@PostMapping("/notifications/read-all")
	@ResponseBody
	public String markAllAsRead(HttpSession session) {

		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "NOT_LOGIN";
		}
		
		Long memIdx = loginUser.getMemIdx();
		notificationService.markAllAsRead(memIdx);
		return "OK";
	}
}