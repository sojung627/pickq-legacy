package com.springbootstudy.bbs.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.dto.OrdersListDTO;
import com.springbootstudy.bbs.mapper.OrdersMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

	private final OrdersMapper ordersMapper;
	private final EscrowRefundService escrowRefundService;
	private final NotificationService notificationService;

	// 1. 낙찰 시 주문 생성
	public OrdersVO createOrderOnWinner(AuctionDTO auction, BidDTO bid) {
		
		OrdersVO existing = ordersMapper.findByAuctionAndBid(auction.getAuctionIdx(), bid.getBidIdx());
		if (existing != null) {
			return existing;
		}

		OrdersVO order = new OrdersVO();
		order.setAuctionIdx(auction.getAuctionIdx());
		order.setBidIdx(bid.getBidIdx());
		order.setBuyerIdx(auction.getBuyerIdx());
		order.setSellerIdx(bid.getBidderIdx());
		order.setOrderAmount(bid.getBidPrice());
		order.setOrderStatus("CREATED"); // 결제 대기
		order.setIsSettled("N");

		ordersMapper.insertOrder(order);
		return order;
	}

	// 2-1. 결제 완료 시 상태 변화
	@Transactional
	public void markOrderPaid(Long orderIdx) {
		OrdersVO order = requireOrder(orderIdx);

		if ("PAID".equals(order.getOrderStatus())) {
			log.info("주문 {} 는 이미 결제완료 상태입니다. 중복 결제완료 처리를 건너뜁니다.", orderIdx);
			return;
		}

		if (!"CREATED".equals(order.getOrderStatus())) {
			throw new IllegalStateException("CREATED 상태에서만 결제완료 처리할 수 있습니다.");
		}

		// 비즈니스 룰: 마감/낙찰 이후 auctionStatus, bidStatus 는 더 이상 변경하지 않는다.
		ordersMapper.updateOrderPaid(orderIdx);
		notificationService.notifyOrderPaid(requireOrder(orderIdx));
	}

	// 2-2. 배송 시작 시 상태 변화
	@Transactional
	public void markOrderShipped(Long orderIdx) {
		OrdersVO order = requireOrder(orderIdx);

		if ("SHIPPED".equals(order.getOrderStatus())) {
			log.info("주문 {} 는 이미 배송시작 상태입니다. 중복 배송시작 처리를 건너뜁니다.", orderIdx);
			return;
		}

		if (!"PAID".equals(order.getOrderStatus())) {
			throw new IllegalStateException("PAID 상태에서만 배송시작 처리할 수 있습니다.");
		}

		// 비즈니스 룰: 마감/낙찰 이후 auctionStatus, bidStatus 는 더 이상 변경하지 않는다.
		ordersMapper.updateOrderShipped(orderIdx);
		notificationService.notifyOrderShipped(requireOrder(orderIdx));
	}

	// 2-3. 구매 확정 시 상태 변화
	@Transactional
	public void markOrderConfirmed(Long orderIdx) {
		OrdersVO order = requireOrder(orderIdx);

		if ("CONFIRMED".equals(order.getOrderStatus())) {
			log.info("주문 {} 는 이미 구매확정 상태입니다. 중복 구매확정 처리를 건너뜁니다.", orderIdx);
			return;
		}

		if (!"SHIPPED".equals(order.getOrderStatus())) {
			throw new IllegalStateException("SHIPPED 상태에서만 구매확정 처리할 수 있습니다.");
		}

		// 비즈니스 룰: 마감/낙찰 이후 auctionStatus, bidStatus 는 더 이상 변경하지 않는다.
		ordersMapper.updateOrderConfirmed(orderIdx);
		OrdersVO updatedOrder = requireOrder(orderIdx);
		notificationService.notifyOrderReceiptConfirmed(updatedOrder);
		notificationService.notifyOrderCompleted(updatedOrder);
		notificationService.notifyReviewWriteReminder(updatedOrder);
	}

	// 2-4. 거래 취소 시 상태 변화
	@Transactional
	public void markOrderCanceled(Long orderIdx) {
		OrdersVO order = requireOrder(orderIdx);

		if ("CANCELED".equals(order.getOrderStatus())) {
			log.info("주문 {} 는 이미 거래취소 상태입니다. 중복 취소 처리를 건너뜁니다.", orderIdx);
			return;
		}

		if ("CONFIRMED".equals(order.getOrderStatus())) {
			throw new IllegalStateException("이미 거래완료된 주문은 취소할 수 없습니다.");
		}

		ordersMapper.updateOrderCanceled(orderIdx);
		notificationService.notifyOrderCanceled(requireOrder(orderIdx));
	}

	/**
	 * 판매자 결제완료 취소 전용 플로우.
	 * 비즈니스 룰:
	 * 1) 판매자 본인만 취소 가능
	 * 2) 결제완료(PAID) 상태에서만 가능
	 * 3) 배송 시작 이후(SHIPPED)는 취소 불가
	 * 4) 패널티 1점 정책은 추후 member_penalty 연동 예정(TODO)
	 */
	@Transactional
	public void cancelOrderBySeller(Long orderIdx, Long sellerMemIdx) {
		OrdersVO order = requireOrder(orderIdx);

		if (sellerMemIdx == null || !sellerMemIdx.equals(order.getSellerIdx())) {
			throw new IllegalStateException("판매자 본인만 거래 취소를 요청할 수 있습니다.");
		}

		if (!"PAID".equals(order.getOrderStatus())) {
			throw new IllegalStateException("결제완료 상태에서만 판매자 거래 취소가 가능합니다.");
		}

		// 배송이 시작된 뒤에는 구매자 보호를 위해 임의 취소를 금지한다.
		if ("SHIPPED".equals(order.getOrderStatus()) || "CONFIRMED".equals(order.getOrderStatus())) {
			throw new IllegalStateException("배송 시작 이후에는 판매자가 임의로 거래를 취소할 수 없습니다.");
		}

		escrowRefundService.refundPaidEscrowByBidIdx(order.getBidIdx(),
				"판매자 요청으로 주문 [" + order.getOrderIdx() + "] 거래가 취소되었습니다.");

		ordersMapper.updateOrderCanceled(orderIdx);
		OrdersVO updatedOrder = requireOrder(orderIdx);

		// TODO: 패널티 1점 정책은 member_penalty 실구현 단계에서 DB 적재로 확장.
		log.info("판매자 거래취소 패널티 정책 적용 대상 - orderIdx={}, sellerIdx={}", orderIdx, sellerMemIdx);

		notificationService.notifyOrderCanceledBySellerWithRefund(updatedOrder);
	}

	// 2-5. bid 기준으로 주문 찾기 (배송/수령확인에서 사용)
	public OrdersVO findByBidIdx(Long bidIdx) {
		return ordersMapper.findByBidIdx(bidIdx);
	}

	public OrdersVO findByOrderIdx(Long orderIdx) {
		return ordersMapper.findByOrderIdx(orderIdx);
	}

	public OrdersListDTO getOrderDetailForMember(Long orderIdx, Long memIdx) {
		OrdersListDTO order = ordersMapper.findDetailByOrderIdxAndMember(orderIdx, memIdx);
		maskOrderParticipantIds(order);
		return order;
	}

	// 3-1. 내가 구매한 거래내역 조회
	public List<OrdersListDTO> getOrdersAsBuyer(Long buyerIdx) {
		List<OrdersListDTO> orders = ordersMapper.findAllByBuyerIdx(buyerIdx);
		maskOrderParticipantIds(orders);
		return orders;
	}

	// 3-2. 내가 판매한 거래내역 조회
	public List<OrdersListDTO> getOrdersAsSeller(Long sellerIdx) {
		List<OrdersListDTO> orders = ordersMapper.findAllBySellerIdx(sellerIdx);
		maskOrderParticipantIds(orders);
		return orders;
	}

	// 3-3. 내가 참여한 전체 거래내역 (구매 + 판매) 조회
	public List<OrdersListDTO> getMyOrders(Long memIdx) {
		List<OrdersListDTO> orders = ordersMapper.findAllByMemberIdx(memIdx);
		maskOrderParticipantIds(orders);
		return orders;
	}

	private void maskOrderParticipantIds(List<OrdersListDTO> orders) {
		if (orders == null) {
			return;
		}
		for (OrdersListDTO order : orders) {
			maskOrderParticipantIds(order);
		}
	}

	private void maskOrderParticipantIds(OrdersListDTO order) {
		if (order == null) {
			return;
		}
		order.setBuyerMemIdMasked(maskMemId(order.getBuyerMemId()));
		order.setSellerMemIdMasked(maskMemId(order.getSellerMemId()));
	}

	private String maskMemId(String memId) {
		if (memId == null || memId.isBlank()) {
			return "-";
		}
		if (memId.length() > 2) {
			int len = memId.length();
			return memId.substring(0, 2) + "*".repeat(Math.max(1, len - 3)) + memId.substring(len - 1);
		}
		return memId.substring(0, 1) + "*".repeat(Math.max(1, memId.length() - 1));
	}

	private OrdersVO requireOrder(Long orderIdx) {
		OrdersVO order = ordersMapper.findByOrderIdx(orderIdx);
		if (order == null) {
			throw new IllegalArgumentException("주문 정보를 찾을 수 없습니다.");
		}
		return order;
	}
}