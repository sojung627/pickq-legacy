package com.springbootstudy.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.springbootstudy.bbs.domain.MemberVO;

@Mapper
public interface ApiMapper {
	
	// 이메일로 회원 조회
    MemberVO findByEmail(String memEmail);

    // 회원 등록
    void insertMember(MemberVO vo); 


}
