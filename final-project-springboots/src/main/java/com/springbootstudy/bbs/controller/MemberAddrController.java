package com.springbootstudy.bbs.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springbootstudy.bbs.domain.MemberAddrVO;
import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.service.MemberAddrService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MemberAddrController {

	@Autowired
	private MemberAddrService memberAddrService;
	
	// 주소등록창 -----------------------------------------------------------------
	
	@GetMapping("/mypage/addresses/new") 
    public String memberAddrInsert(@RequestParam(value = "redirect", required = false) String redirect,
            HttpSession session, Model model) {

        MemberVO loginMember = (MemberVO) session.getAttribute("loginUser");

        // 로그인 안한 사람 로그인으로
        if (loginMember == null) {
            return "redirect:/members/login";
        }
        
        model.addAttribute("member", loginMember);
        model.addAttribute("redirectAfterSave", sanitizeRedirect(redirect));

        return "/views/member/memberAddrInsert";  
	}

    private String sanitizeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "/mypage/addresses";
        }

        if (!redirect.startsWith("/")) {
            return "/mypage/addresses";
        }

        if (redirect.startsWith("//")) {
            return "/mypage/addresses";
        }

        return redirect;
    }
	
	// memberAddrInsert.html 의 Ajax 저장용
	@PostMapping("/member/insertAddrAjax.do")
	@ResponseBody
	public int insertAddr(MemberAddrVO vo) {

		// 두개 확인 필요
	    int result = memberAddrService.registerAddr(vo);
	    vo.setIsPrimary("N");
	    
	    return result; 
	}

	// 주소목록창 -----------------------------------------------------------------
	
	// 저장된 값 보여주기 
	@GetMapping("/mypage/addresses") 
    public String memberAddr(HttpSession session, Model model) {

        // 로그인 안했으면 쫓아내기
        MemberVO loginMember = (MemberVO) session.getAttribute("loginUser");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        List<MemberAddrVO> addrList = memberAddrService.selectAddrList(loginMember.getMemIdx());
        model.addAttribute("addrList", addrList);
        model.addAttribute("member", loginMember); 

        return "/views/member/memberAddr";
    }

    // 주소 삭제 -----------------------------------------------------------------
    
    @PostMapping("/mypage/addresses/delete")
    public String deleteAddr(@RequestParam("addrIdx") Long addrIdx) {

        memberAddrService.deleteAddr(addrIdx);

        return "redirect:/mypage/addresses";
    }

    // 주소 수정 -----------------------------------------------------------------
    
    @GetMapping("/mypage/addresses/edit") 
    public String updateAddrForm(@RequestParam("addrIdx") Long addrIdx,HttpSession session, Model model) { 
    	// 로그인 정보 받아오기
        MemberVO loginMember = (MemberVO) session.getAttribute("loginUser");

        // 로그인 안했으면 쫓아내기
        if (loginMember == null) {
            return "redirect:/members/login";  
        }

        // 주소가 없어도 쫓아내기
        if (addrIdx == null) {
            return "redirect:/mypage/addresses";
        }

        MemberAddrVO addr = memberAddrService.selectOne(addrIdx);

        // 로그인 세션
        model.addAttribute("member", loginMember);
        model.addAttribute("addr", addr);

        return "/views/member/memberAddrUpdate";
    }

    // 주소 수정
    @PostMapping("/member/updateAddrAjax.do")
    @ResponseBody
    public int updateAddr(MemberAddrVO vo) {

        return memberAddrService.updateAddr(vo);
    }
    
    
    // 버튼 누르면 대표배송지로 수정
    @PostMapping("/mypage/addresses/primary")
    public String setPrimary(@RequestParam("addrIdx") Long addrIdx,
                             HttpSession session) {

        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/members/login";
        }

        memberAddrService.setPrimary(addrIdx, loginUser.getMemIdx());

        return "redirect:/mypage/addresses";
    }

}
