package com.springbootstudy.bbs.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.ChatRoomVO;

@Mapper
public interface ChatRoomMapper {

    ChatRoomVO findByChatIdx(Long chatroomIdx);	// 해당 채팅방 조회
    
    // 옥션-참여자로 채팅 조회하기
    ChatRoomVO findByAuctionAndMembers(@Param("auctionIdx") Long auctionIdx,
                                       @Param("buyerIdx") Long buyerIdx,
                                       @Param("bidderIdx") Long bidderIdx);

    int insertChatRoom(ChatRoomVO chatRoom);	// 새로운 채팅 생성
    List<ChatRoomVO> findByMember(Long memIdx); // 내가 참여한 방 목록 조회
    List<Map<String, Object>> findRoomSummariesByMember(Long memIdx); // 목록 표시용 요약 정보
}