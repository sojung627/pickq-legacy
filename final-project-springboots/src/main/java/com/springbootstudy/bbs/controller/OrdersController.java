package com.springbootstudy.bbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.dto.OrdersListDTO;
import com.springbootstudy.bbs.service.DeliveryService;
import com.springbootstudy.bbs.service.OrdersService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class OrdersController {
	
	@Autowired
	private OrdersService ordersService;

	@Autowired
	private DeliveryService deliveryService;
	
	// 내 거래 목록 전체 (구매 + 판매)
	@GetMapping("/orders")
	public String myOrders(HttpSession session, Model model) {

		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		Long memIdx = loginUser.getMemIdx();
		List<OrdersListDTO> orders = ordersService.getMyOrders(memIdx);
		model.addAttribute("orders", orders);
		model.addAttribute("ordersView", "all");
		model.addAttribute("activeMenu", "myOrders");
		model.addAttribute("pageTitle", "나의 거래 - 전체");
		model.addAttribute("pageDescription", "구매/판매로 참여한 모든 거래를 확인할 수 있습니다.");
		model.addAttribute("emptyMessage", "참여한 거래가 없습니다.");

		return "views/mypage/orders";
	}

	// 내가 구매한 거래
	@GetMapping("/orders/buy")
	public String myBuyOrders(HttpSession session, Model model) {
		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		Long memIdx = loginUser.getMemIdx();
		List<OrdersListDTO> orders = ordersService.getOrdersAsBuyer(memIdx);
		model.addAttribute("orders", orders);
		model.addAttribute("ordersView", "buy");
		model.addAttribute("activeMenu", "myOrders");
		model.addAttribute("pageTitle", "나의 거래 - 구매 내역");
		model.addAttribute("pageDescription", "내가 구매자로 참여한 거래의 결제/배송 상태를 확인할 수 있습니다.");
		model.addAttribute("emptyMessage", "구매한 거래가 없습니다.");

		return "views/mypage/orders";
	}

	// 내가 판매한 거래
	@GetMapping("/orders/sell")
	public String mySellOrders(HttpSession session, Model model) {
		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		Long memIdx = loginUser.getMemIdx();
		List<OrdersListDTO> orders = ordersService.getOrdersAsSeller(memIdx);
		model.addAttribute("orders", orders);
		model.addAttribute("ordersView", "sell");
		model.addAttribute("activeMenu", "myOrders");
		model.addAttribute("pageTitle", "나의 거래 - 판매 내역");
		model.addAttribute("pageDescription", "내가 판매자로 참여한 거래의 결제/배송 상태를 확인할 수 있습니다.");
		model.addAttribute("emptyMessage", "판매한 거래가 없습니다.");

		return "views/mypage/orders";
	}

	@GetMapping("/orders/{orderIdx}")
	public String orderDetail(@PathVariable("orderIdx") Long orderIdx,
			HttpSession session,
			Model model,
			RedirectAttributes ra) {
		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		OrdersListDTO order = ordersService.getOrderDetailForMember(orderIdx, loginUser.getMemIdx());
		if (order == null) {
			ra.addFlashAttribute("errorMessage", "조회 권한이 없거나 존재하지 않는 거래입니다.");
			return "redirect:/mypage/orders";
		}

		model.addAttribute("order", order);
		model.addAttribute("activeMenu", "myOrders");
		return "views/mypage/orderDetail";
	}

	@PostMapping("/orders/{orderIdx}/confirm")
	public String confirmReceipt(@PathVariable("orderIdx") Long orderIdx,
			HttpSession session,
			RedirectAttributes ra) {
		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		OrdersVO order = ordersService.findByOrderIdx(orderIdx);
		if (order == null || !loginUser.getMemIdx().equals(order.getBuyerIdx())) {
			ra.addFlashAttribute("errorMessage", "구매확정 권한이 없습니다.");
			return "redirect:/mypage/orders";
		}

		try {
			deliveryService.confirmReceipt(order.getBidIdx());
			ra.addFlashAttribute("successMessage", "구매확정이 완료되었습니다.");
		} catch (Exception e) {
			ra.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/mypage/orders/" + orderIdx;
	}

	@PostMapping("/orders/{orderIdx}/cancel")
	public String cancelOrder(@PathVariable("orderIdx") Long orderIdx,
			HttpSession session,
			RedirectAttributes ra) {
		MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
		if (loginUser == null) {
			return "redirect:/members/login";
		}

		OrdersVO order = ordersService.findByOrderIdx(orderIdx);
		if (order == null) {
			ra.addFlashAttribute("errorMessage", "주문 정보를 찾을 수 없습니다.");
			return "redirect:/mypage/orders";
		}

		Long memIdx = loginUser.getMemIdx();
		if (!memIdx.equals(order.getSellerIdx())) {
			ra.addFlashAttribute("errorMessage", "거래 취소 권한이 없습니다.");
			return "redirect:/mypage/orders";
		}

		try {
			ordersService.cancelOrderBySeller(orderIdx, memIdx);
			ra.addFlashAttribute("successMessage", "거래가 취소되고 결제가 환불 처리됩니다.");
			ra.addFlashAttribute("warningMessage", "판매자 거래 취소 정책에 따라 패널티 1점이 부과됩니다.");
		} catch (Exception e) {
			ra.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/mypage/orders/" + orderIdx;
	}
}
