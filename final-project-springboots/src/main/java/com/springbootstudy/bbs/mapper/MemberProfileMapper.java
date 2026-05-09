package com.springbootstudy.bbs.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.springbootstudy.bbs.domain.MemberProfileVO;
import com.springbootstudy.bbs.dto.ProfilePageDTO;

@Mapper
public interface MemberProfileMapper {

    // 조회용
    MemberProfileVO selectProfileByMemIdx(Long memIdx);

    // 공개 프로필 조회용 (회원+프로필+등급+리뷰집계)
    ProfilePageDTO selectProfilePageByMemIdx(Long memIdx);

    // 저장용
    void insertProfile(MemberProfileVO vo);

    // 수정용
    void updateProfile(MemberProfileVO vo);

    // 닉네임 중복 체크
    int countByNickname(String memNickname);

}
