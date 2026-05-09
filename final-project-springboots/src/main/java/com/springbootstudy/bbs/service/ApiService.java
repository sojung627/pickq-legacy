package com.springbootstudy.bbs.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.mapper.ApiMapper;

@Service
public class ApiService {
	
	 @Autowired
	 private ApiMapper apiMapper;

	    // 이메일로 회원 조회 (네이버 로그인용)
	    public MemberVO findByEmail(String email) {
	        return apiMapper.findByEmail(email);
	    }

	    // 회원가입 (네이버 포함)
	    public void register(String memId, String memPwd, String memName, 
	                         String memTel, String memEmail, String memIp,
	                         Integer memRoleIdx, Integer memGradeIdx,
	                         LocalDate memBday, String loginType) {

	        MemberVO vo = new MemberVO();
	        vo.setMemId(memId);
	        vo.setMemPwd(memPwd);
	        vo.setMemName(memName);
	        vo.setMemTel(memTel);
	        vo.setMemEmail(memEmail);
	        vo.setMemIp(memIp);
	        vo.setMemRoleIdx(memRoleIdx);
	        vo.setMemGradeIdx(memGradeIdx);
	        vo.setMemBday(memBday); 
	        vo.setMemLoginType(loginType);

	        apiMapper.insertMember(vo);  
	    }
	
}
