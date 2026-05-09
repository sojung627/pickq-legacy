package com.springbootstudy.bbs.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.BidDTO;
import com.springbootstudy.bbs.domain.BoardVO;
import com.springbootstudy.bbs.domain.NotificationVO;
import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.domain.ReplyVO;
import com.springbootstudy.bbs.mapper.BidMapper;
import com.springbootstudy.bbs.mapper.NotificationMapper;

@Service
public class NotificationService {

	@Autowired
	private NotificationMapper notificationMapper;

	@Autowired
	private BidMapper bidMapper;

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	// 알림 저장
	public void sendNotification(NotificationVO notification) {
		notificationMapper.insertNotification(notification);
	}

	// 알림 저장 + 실시간 푸시
	@Transactional
	public void sendAndPush(NotificationVO notification) {
		if (notification == null || notification.getReceiverIdx() == null) {
			throw new IllegalArgumentException("receiverIdx는 필수입니다.");
		}

		notification.setIsRead("N");
		if (notification.getCreatedAt() == null) {
			notification.setCreatedAt(LocalDateTime.now());
		}

		notificationMapper.insertNotification(notification);
		simpMessagingTemplate.convertAndSend(
				"/topic/notifications/" + notification.getReceiverIdx(),
				notification
		);

		if (notification.getAuctionIdx() != null
				&& notification.getNotificationType() != null
				&& notification.getNotificationType().startsWith("AUCTION_")) {
			simpMessagingTemplate.convertAndSend(
					"/topic/auctions/" + notification.getAuctionIdx() + "/status",
					notification
			);
		}
	}

	// 회원별 알림 전체 조회
	public List<NotificationVO> getNotificationsForMember(Long memIdx) {
		return notificationMapper.selectNotificationsByMember(memIdx);
	}

	// 회원별 최근 알림 조회
	public List<NotificationVO> getRecentNotificationsForMember(Long memIdx, int limit) {
		if (limit <= 0) {
			return List.of();
		}
		return notificationMapper.selectRecentNotificationsByMember(memIdx, limit);
	}

	// 단건 읽음 처리
	public void markAsRead(Long notificationIdx) {
		notificationMapper.updateNotificationRead(notificationIdx);
	}

	// 전체 읽음 처리
	public void markAllAsRead(Long memIdx) {
		notificationMapper.updateAllNotificationsRead(memIdx);
	}

	// 안 읽은 알림 존재 여부 (Y/N만 필요)
	public boolean hasUnread(Long memIdx) {
		return notificationMapper.countUnread(memIdx) > 0;
	}

	// 안 읽은 알림 개수 (정확한 갯수가 필요)
	public int getUnreadCount(Long memIdx) {
		return notificationMapper.countUnread(memIdx);
	}

	public void notifyNewBidToAuctionWriter(AuctionDTO auction, BidDTO bid) {
		if (auction == null || bid == null || auction.getBuyerIdx() == null) {
			return;
		}
		if (auction.getBuyerIdx().equals(bid.getBidderIdx())) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(auction.getBuyerIdx());
		notification.setSenderIdx(bid.getBidderIdx());
		notification.setAuctionIdx(auction.getAuctionIdx());
		notification.setBidIdx(bid.getBidIdx());
		notification.setNotificationType("AUCTION_NEW_BID");
		notification.setNotificationTitle("새 입찰이 등록되었습니다");
		notification.setNotificationMessage("내 경매에 새로운 입찰이 도착했습니다.");
		notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
		sendAndPush(notification);
	}

	public void notifyAuctionBidClosedToOwner(AuctionDTO auction) {
		if (auction == null || auction.getBuyerIdx() == null) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(auction.getBuyerIdx());
		notification.setAuctionIdx(auction.getAuctionIdx());
		notification.setNotificationType("AUCTION_BID_CLOSED");
		notification.setNotificationTitle("입찰 마감 알림");
		notification.setNotificationMessage("입찰이 마감되어 낙찰자 결정을 진행할 수 있습니다.");
		notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
		sendAndPush(notification);
	}

	// 경매 상태 변경 알림 - 구매자
	public void notifyAuctionStatusChangedToOwner(AuctionDTO auction, String notificationType, String title, String message) {
		if (auction == null || auction.getBuyerIdx() == null) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(auction.getBuyerIdx());
		notification.setAuctionIdx(auction.getAuctionIdx());
		notification.setNotificationType(notificationType);
		notification.setNotificationTitle(title);
		notification.setNotificationMessage(message);
		notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
		sendAndPush(notification);
	}

	public void notifyAuctionDecisionClosedToOwner(AuctionDTO auction) {
		if (auction == null || auction.getBuyerIdx() == null) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(auction.getBuyerIdx());
		notification.setAuctionIdx(auction.getAuctionIdx());
		notification.setNotificationType("AUCTION_DECISION_CLOSED");
		notification.setNotificationTitle("결정 마감 알림");
		notification.setNotificationMessage("낙찰자 선정 기한이 종료되었습니다.");
		notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
		sendAndPush(notification);
	}

	public void notifyWinnerSelectedToBidder(Long auctionIdx, BidDTO winnerBid) {
		if (winnerBid == null || winnerBid.getBidderIdx() == null || auctionIdx == null) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(winnerBid.getBidderIdx());
		notification.setSenderIdx(null);
		notification.setAuctionIdx(auctionIdx);
		notification.setBidIdx(winnerBid.getBidIdx());
		notification.setNotificationType("AUCTION_WINNER_SELECTED");
		notification.setNotificationTitle("낙찰자로 선정되었습니다");
		notification.setNotificationMessage("입찰하신 건이 낙찰되어 거래를 진행할 수 있습니다.");
		notification.setTargetUrl("/mypage/orders");
		sendAndPush(notification);
	}

	public void notifyDecisionDeadlineToBidder(AuctionDTO auction, Long bidderIdx) {
		if (auction == null || bidderIdx == null) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(bidderIdx);
		notification.setAuctionIdx(auction.getAuctionIdx());
		notification.setNotificationType("AUCTION_DECISION_DEADLINE_REACHED");
		notification.setNotificationTitle("결정 마감 기한 도래");
		notification.setNotificationMessage("해당 경매의 낙찰자 선정 기한이 종료되었습니다.");
		notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
		sendAndPush(notification);
	}

	// 경매 상태 변경 알림 - 참여 입찰자들
	public void notifyAuctionStatusChangedToBidders(AuctionDTO auction, String notificationType, String title, String message) {
		notifyAuctionStatusChangedToBidders(auction, notificationType, title, message, null);
	}

	// 특정 입찰자 제외용 (낙찰자 제외 등)
	public void notifyAuctionStatusChangedToBidders(AuctionDTO auction, String notificationType, String title, String message, Long excludeBidderIdx) {
		if (auction == null || auction.getAuctionIdx() == null) {
			return;
		}

		List<Long> bidderIdxList = bidMapper.findDistinctBidderIdxByAuction(auction.getAuctionIdx());
		if (bidderIdxList == null || bidderIdxList.isEmpty()) {
			return;
		}

		for (Long bidderIdx : bidderIdxList) {
			if (bidderIdx == null) {
				continue;
			}
			if (excludeBidderIdx != null && excludeBidderIdx.equals(bidderIdx)) {
				continue;
			}

			NotificationVO notification = new NotificationVO();
			notification.setReceiverIdx(bidderIdx);
			notification.setAuctionIdx(auction.getAuctionIdx());
			notification.setNotificationType(notificationType);
			notification.setNotificationTitle(title);
			notification.setNotificationMessage(message);
			notification.setTargetUrl("/auctions/" + auction.getAuctionIdx());
			sendAndPush(notification);
		}
	}

	public void notifyNewReplyToBoardWriter(BoardVO board, ReplyVO reply) {
		if (board == null || reply == null || board.getMemIdx() == null) {
			return;
		}
		if (board.getMemIdx().equals(reply.getMemIdx())) {
			return;
		}

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(board.getMemIdx());
		notification.setSenderIdx(reply.getMemIdx());
		notification.setBoardIdx(board.getBoardIdx());
		notification.setReplyIdx(reply.getReplyIdx());
		notification.setNotificationType("BOARD_NEW_REPLY");
		notification.setNotificationTitle("내 게시글에 새 댓글이 달렸습니다");
		notification.setNotificationMessage("게시글에 새로운 댓글이 등록되었습니다.");
		notification.setTargetUrl("/boards/" + board.getBoardTypeCode() + "/" + board.getBoardIdx());
		sendAndPush(notification);
	}

	public void notifyOrderPaid(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "결제가 완료되었습니다.");
		sendTradeNotificationToBoth(order, "TRADE_PAYMENT_COMPLETED", "거래 결제 완료", message);
	}

	public void notifyOrderShipped(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "배송이 시작되었습니다.");
		sendTradeNotificationToBoth(order, "TRADE_SHIPPING_STARTED", "거래 배송 시작", message);
	}

	public void notifyOrderReceiptConfirmed(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "구매확정이 완료되었습니다.");
		sendTradeNotificationToBoth(order, "TRADE_RECEIPT_CONFIRMED", "거래 구매확정", message);
	}

	public void notifyOrderCompleted(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "거래가 완료되었습니다.");
		sendTradeNotificationToBoth(order, "TRADE_COMPLETED", "거래 완료 안내", message);
	}

	public void notifyReviewWriteReminder(OrdersVO order) {
		if (order == null || order.getBuyerIdx() == null || order.getAuctionIdx() == null || order.getBidIdx() == null) {
			return;
		}

		String itemName = resolveItemName(order);

		NotificationVO notification = new NotificationVO();
		notification.setReceiverIdx(order.getBuyerIdx());
		notification.setSenderIdx(order.getSellerIdx());
		notification.setAuctionIdx(order.getAuctionIdx());
		notification.setBidIdx(order.getBidIdx());
		notification.setNotificationType("REVIEW_WRITE_REMINDER");
		notification.setNotificationTitle("리뷰 작성 안내");
		notification.setNotificationMessage("'" + itemName + "' 거래의 구매가 확정되었습니다. 상품에 대해 리뷰를 남겨주세요.");
		notification.setTargetUrl("/mypage/reviews/reviewWrite?auctionIdx=" + order.getAuctionIdx()
				+ "&bidIdx=" + order.getBidIdx()
				+ "&bidderIdx=" + order.getSellerIdx());
		sendAndPush(notification);
	}

	public void notifyOrderCanceled(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "거래가 취소되었습니다.");
		sendTradeNotificationToBoth(order, "TRADE_CANCELED", "거래 취소 안내", message);
	}

	public void notifyOrderCanceledBySellerWithRefund(OrdersVO order) {
		if (order == null) {
			return;
		}
		String message = buildTradeMessage(order, "판매자 사정으로 거래가 취소되었고 결제가 환불 처리됩니다.");
		sendTradeNotificationToBoth(order, "TRADE_CANCELED_BY_SELLER", "거래 취소 및 환불", message);
	}

	private String buildTradeMessage(OrdersVO order, String eventText) {
		String itemName = resolveItemName(order);
		return "'" + itemName + "' 거래에서 " + eventText;
	}

	private String resolveItemName(OrdersVO order) {
		if (order == null || order.getBidIdx() == null) {
			return "상품 정보 없음";
		}

		BidDTO bid = bidMapper.findBidById(order.getBidIdx());
		if (bid == null || bid.getItemName() == null || bid.getItemName().isBlank()) {
			return "상품 정보 없음";
		}

		return bid.getItemName();
	}

	private void sendTradeNotificationToBoth(OrdersVO order, String type, String title, String message) {
		if (order.getOrderIdx() == null) {
			return;
		}

		if (order.getBuyerIdx() != null) {
			NotificationVO buyerNotification = new NotificationVO();
			buyerNotification.setReceiverIdx(order.getBuyerIdx());
			buyerNotification.setAuctionIdx(order.getAuctionIdx());
			buyerNotification.setBidIdx(order.getBidIdx());
			buyerNotification.setNotificationType(type);
			buyerNotification.setNotificationTitle(title);
			buyerNotification.setNotificationMessage(message);
			buyerNotification.setTargetUrl("/mypage/orders/" + order.getOrderIdx());
			sendAndPush(buyerNotification);
		}

		if (order.getSellerIdx() != null) {
			NotificationVO sellerNotification = new NotificationVO();
			sellerNotification.setReceiverIdx(order.getSellerIdx());
			sellerNotification.setAuctionIdx(order.getAuctionIdx());
			sellerNotification.setBidIdx(order.getBidIdx());
			sellerNotification.setNotificationType(type);
			sellerNotification.setNotificationTitle(title);
			sellerNotification.setNotificationMessage(message);
			sellerNotification.setTargetUrl("/mypage/orders/" + order.getOrderIdx());
			sendAndPush(sellerNotification);
		}
	}
}