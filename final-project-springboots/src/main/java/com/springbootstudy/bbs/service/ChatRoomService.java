package com.springbootstudy.bbs.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.AuctionDTO;
import com.springbootstudy.bbs.domain.ChatRoomVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.mapper.ChatRoomMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
	
	@Autowired
    private final ChatRoomMapper chatRoomMapper;

    public Long prepareChatroomForMembers(Long auctionIdx,
                                          Long buyerIdx,
                                          Long bidderIdx) {

        if (auctionIdx == null || buyerIdx == null || bidderIdx == null) {
            return null;
        }

        if (buyerIdx.equals(bidderIdx)) {
            return null;
        }

        ChatRoomVO room = chatRoomMapper.findByAuctionAndMembers(
                auctionIdx,
                buyerIdx,
                bidderIdx
        );

        if (room == null) {
            ChatRoomVO newRoom = new ChatRoomVO();
            newRoom.setAuctionIdx(auctionIdx);
            newRoom.setBuyerIdx(buyerIdx);
            newRoom.setBidderIdx(bidderIdx);
            chatRoomMapper.insertChatRoom(newRoom);
            return newRoom.getChatroomIdx();
        }

        return room.getChatroomIdx();
    }

    public Long prepareChatroomForAuction(Long auctionIdx,
                                          MemberVO loginUser,
                                          AuctionDTO detail) {

        if (loginUser == null || detail == null) {
            return null;
        }

        Long loginMemIdx = loginUser.getMemIdx();

        // ❗ 여기는 네 AuctionDTO 실제 필드명에 맞게 고쳐야 함
        Long buyerIdx = detail.getBuyerIdx(); // 예: getBuyerIdx(), getBuyerMemIdx() 등

        // 구매자는 자기 경매에서 채팅 시작 못 한다 (원하면 이 조건 지워도 됨)
        if (loginMemIdx.equals(buyerIdx)) {
            return null;
        }

        return prepareChatroomForMembers(auctionIdx, buyerIdx, loginMemIdx);
    }

    public List<Map<String, Object>> getRoomSummariesByMember(Long memIdx) {
        return chatRoomMapper.findRoomSummariesByMember(memIdx);
    }
}