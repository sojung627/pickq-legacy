package com.springbootstudy.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.DeliveryVO;

@Mapper
public interface DeliveryMapper {

  // 배송 정보 등록
  int insertDelivery(DeliveryVO deliveryVO);

  // 배송 상태 변경
  int updateDeliveryStatus(@Param("bidIdx") Long bidIdx,
      @Param("deliveryStatus") String deliveryStatus);

  // bid_idx로 배송 정보 조회
  DeliveryVO findDeliveryByBidIdx(@Param("bidIdx") Long bidIdx);

  // pay_idx로 배송 정보 조회
  DeliveryVO findDeliveryByPayIdx(@Param("payIdx") Long payIdx);

}