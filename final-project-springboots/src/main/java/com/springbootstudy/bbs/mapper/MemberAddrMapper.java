package com.springbootstudy.bbs.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.springbootstudy.bbs.domain.MemberAddrVO;

@Mapper
public interface MemberAddrMapper {

	// 주소 등록
	int insertAddr(MemberAddrVO memberAddrVO);

	List<MemberAddrVO> selectAddrList(Long memIdx);

	// 대표 주소 하나만 등록하기
	void resetPrimaryAddr(@Param("memIdx") Long memIdx);

	// 주소 삭제
	void deleteAddr(Long addrIdx);

	// 주소 수정
	int updateAddr(MemberAddrVO vo);

	MemberAddrVO selectOne(Long addrIdx);

	// 대표 배송지 단건 조회
	MemberAddrVO selectPrimaryAddr(@Param("memIdx") Long memIdx);

	// 대표배송지 버튼
	void resetPrimary(Long memIdx);
	void updatePrimary(Long addrIdx);
	
}
