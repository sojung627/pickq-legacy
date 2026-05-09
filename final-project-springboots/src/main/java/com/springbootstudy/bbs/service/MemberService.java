package com.springbootstudy.bbs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.mapper.MemberMapper;

@Service
public class MemberService {

   @Autowired
    private MemberMapper memberMapper;

   // 회원가입 처리
    public void insertMember(
            String memId,
            String memPwd,
            String memName,
            String memTel,
            String fullEmail,
            String memIp,
            Long memRoleIdx,
            int memGradeIdx
    ) {

        memberMapper.insertMember(
                memId, memPwd, memName, memTel, fullEmail,
                memIp, memRoleIdx, memGradeIdx
        );
    }
   
   // 회원 가입시 아이디 중복을 체크하는 메서드   
   public boolean overlapIdCheck(String memId) {
      MemberVO member = memberMapper.getMemberVO(memId);
      
      if(member == null) {
         return false; 
      }  
      return true; 
   }
   
   
   // 로그인 ===============================================
   
   public int login(String memId, String memPwd) {

        // 아이디 확인
        int count = memberMapper.checkId(memId);
        if (count == 0) {
            return -1;
        }

        // 비밀번호 확인
        String dbPwd = memberMapper.getPassword(memId);

        if (!dbPwd.equals(memPwd)) {
            return 0;
        }

        return 1;
    }

    public MemberVO getMemberVO(String memId) {
        return memberMapper.getMemberVO(memId);
    } 
    
    
    // 탈퇴 -----------------------------------------------------
    
    public int deleteMember(String memId) {
        return memberMapper.deleteMember(memId);
    }

    
    // 회원정보 수정 -----------------------------------------------------
    
    public void updateMember(MemberVO vo) {
        memberMapper.update(vo);
    }
    
    // 회원정보 수정창에서 비밀번호 재발급
    public String selectPwdById(String memId) {
        return memberMapper.selectPwdById(memId);
    }

    
    // 비밀번호 재발급 -----------------------------------------------------
    
    // 비밀번호 잃어버렸을 때 재발급! (위쪽 비번이랑 전혀 다른 아이!!)
    // 비밀번호 재발급 시 아이디 + 전화번호 맞는 지 확인
	public MemberVO findByIdAndTel(String memId, String memTel) {
		return memberMapper.findByIdAndTel(memId, memTel);
	}

	// 새 비밀번호로 변경
	public void updatePassword(String memId, String newPassword) {
		memberMapper.updatePassword(memId, newPassword);
	}

	

	


}
