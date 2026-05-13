package com.springbootstudy.bbs.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springbootstudy.bbs.domain.MemberVO;
import com.springbootstudy.bbs.service.ApiService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ApiController {

	@Autowired
	ApiService apiService;

	@GetMapping("/members/naverCallback")
	public String naverCallbackPage() {
		return "views/member/naverCallback";
	}

	// @RequestMapping → @PostMapping 으로 변경
	// String 반환 → @ResponseBody + Map 반환으로 변경
	@PostMapping("/members/naverCallback")
	@ResponseBody
	public Map<String, Object> naverCallback(
			@RequestParam("memName") String memName,
			@RequestParam("memEmail") String memEmail,
			@RequestParam("memTel") String memTel,
			@RequestParam("memBday") String memBday,
			HttpSession session,
			HttpServletRequest request) {
		Map<String, Object> result = new HashMap<>();

		try {
			String memId = memEmail;
			String tempPwd = "TMP_" + UUID.randomUUID().toString().substring(0, 8);
			String memIp = "172.30.1.94";
			Integer memRoleIdx = 1;
			Integer memGradeIdx = 1;

			MemberVO user = apiService.findByEmail(memEmail);

			if (user == null) {
				LocalDate bday = LocalDate.parse(memBday);
				apiService.register(memId, tempPwd, memName, memTel, memEmail,
						memIp, memRoleIdx, memGradeIdx, bday, "NAVER");
				user = apiService.findByEmail(memEmail);
			}

			session.setAttribute("isLogin", true);
			session.setAttribute("loginId", memId);
			session.setAttribute("loginUser", user);
			session.setAttribute("memIdx", user.getMemIdx());

			System.out.println("user: " + user);

			result.put("success", true);
			result.put("redirectUrl", "/main");

		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "로그인 처리 중 오류가 발생했습니다.");
		}

		return result;
	}
}
