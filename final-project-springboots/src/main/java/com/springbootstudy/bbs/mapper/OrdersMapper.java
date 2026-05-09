package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.OrdersVO;
import com.springbootstudy.bbs.dto.OrdersListDTO;

@Mapper
public interface OrdersMapper {

    // 낙찰된 특정 경매+입찰 조합으로 주문 한 건 조회
    OrdersVO findByAuctionAndBid(@Param("auctionIdx") Long auctionIdx,@Param("bidIdx") Long bidIdx);

    // bid 기준으로 주문 한 건 조회 (배송/수령확인에서 사용)
    OrdersVO findByBidIdx(Long bidIdx);

    // order PK 기준 조회
    OrdersVO findByOrderIdx(Long orderIdx);

    // 주문 생성 (낙찰 직후)
    int insertOrder(OrdersVO order);

    // 상태 업데이트용
    int updateOrderPaid(Long orderIdx);

    int updateOrderShipped(Long orderIdx);

    int updateOrderConfirmed(Long orderIdx);

    int updateOrderCanceled(Long orderIdx);

    // 구매자로 참여한 거래 전체 조회
    List<OrdersListDTO> findAllByBuyerIdx(Long buyerIdx);

    // 판매자로 참여한 거래 전체 조회
    List<OrdersListDTO> findAllBySellerIdx(Long sellerIdx);

    // 구매자/판매자 둘 다 포함한 내 거래내역
    List<OrdersListDTO> findAllByMemberIdx(Long memIdx);

    // 내 거래 상세 (구매자/판매자 권한 체크 포함)
    OrdersListDTO findDetailByOrderIdxAndMember(@Param("orderIdx") Long orderIdx,
                                                @Param("memIdx") Long memIdx);
}