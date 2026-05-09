package com.springbootstudy.bbs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springbootstudy.bbs.domain.MemberProfileVO;
import com.springbootstudy.bbs.dto.ProfilePageDTO;
import com.springbootstudy.bbs.mapper.MemberProfileMapper;

@Service
@Transactional 
public class MemberProfileService {
	
	@Autowired
    private MemberProfileMapper memberProfileMapper;


	// 조회
    public MemberProfileVO getProfile(Long memIdx) {
    	
    	System.out.println("memberProfileService.java 진입 완료(insert)"); 
    	
        return memberProfileMapper.selectProfileByMemIdx(memIdx); 
    }

    // 공개 프로필 조회
    public ProfilePageDTO getProfilePage(Long memIdx) {
        return memberProfileMapper.selectProfilePageByMemIdx(memIdx);
    }
 
    // 수정
    public void updateProfile(MemberProfileVO vo) {

        // 기존 프로필 있는지 체크
        MemberProfileVO exist = memberProfileMapper.selectProfileByMemIdx(vo.getMemIdx());
        
        System.out.println("memberProfileService.java 진입 완료(update)");

        if (exist == null) {
            memberProfileMapper.insertProfile(vo);
        } else {
            memberProfileMapper.updateProfile(vo);
        }
    }
    
    // 닉네임 중복 체크
    public int checkNickname(String memNickname) {
        return memberProfileMapper.countByNickname(memNickname);
    }

}
