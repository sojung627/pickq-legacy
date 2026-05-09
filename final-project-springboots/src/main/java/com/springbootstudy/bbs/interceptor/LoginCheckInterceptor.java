package com.springbootstudy.bbs.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

// 접속자가 로그인 상태인지 체크하는 인터셉터
@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

   /*
    * preHandle() 메서드는 클라이언트의 요청이 들어오고 컨트롤러가 실행되기
    * 전에 공통으로 적용할 기능을 구현할 때 사용한다.
    * 예를 들면 특정 요청에 대해 로그인이 되어 있지 않으면 컨트롤러를 실행하지
    * 않거나 컨트롤러를 실행하기 전에 그 컨트롤러에서 필요한 정보를 생성해
    * 넘겨 줄 필요가 있을 때 preHandler() 메서드를 이용해 구현하면 된다.
    * 이 메서드가 false를 반환하면 다음으로 연결된 HandlerInterceptor
    * 또는 컨트롤러 자체를 실행하지 않게 할 수 있다.
    **/

	// 로그인 실패 시 띄워주는 인터셉터
	@Override
	public boolean preHandle(HttpServletRequest request,
	      HttpServletResponse response, Object handler) throws Exception {

	   log.info("##########LoginCheckInterceptor - preHandle()##########");

	   HttpSession session = request.getSession();
	   String uri = request.getRequestURI();
	   String query = request.getQueryString();

	   // 로그인 필요한 경로는 차단
	   // 여기에 등록 안하면 인터셉터 안뜹니다!!
	   // 다른분들의 페이지는 나중에 끝나고 경로 추가할 부분 알려주세요!!
	   boolean needLogin =
			   uri.startsWith("/members/memberUpdate") ||
               uri.startsWith("/members/memberAddr") ||
               uri.startsWith("/members/memberAddrUpdate") ||
               uri.startsWith("/members/memberProfileUpdate") ||
               uri.startsWith("/memberDelete") ||
               uri.startsWith("/mypage/") ||
               uri.startsWith("/auctions/new") ||
               uri.startsWith("/auctions/") && uri.contains("/bids") && !uri.endsWith("/win") ||
               uri.startsWith("/boards/write") ||
               uri.startsWith("/boards/edit") ||
               uri.startsWith("/review/write");
	   
	   // 페이징
	   if (needLogin && session.getAttribute("loginUser") == null) {
           // 현재 URL(쿼리스트링 포함)을 세션에 저장
           String returnUrl = uri;
           if (query != null) returnUrl += "?" + query;
           session.setAttribute("loginRedirectUrl", returnUrl);
           session.setAttribute("loginMsg", "로그인이 필요한 서비스입니다.");
           response.sendRedirect("/members/login");
           return false; 
       }
       return true;
	}
	
	/*
	 * postHandle() 메서드는 클라이언트 요청이 들어오고 컨트롤러가 정상적으로
	 * 실행된 이후에 공통적으로 적용할 추가 기능이 있을 때 주로 사용한다.
	 * 만약 컨트롤러 실행 중에 예외가 발생하게 되면 postHandle() 메서드는
	 * 호출되지 않는다.
	 **/
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.info("##########LoginCheckInterceptor - postHandle()##########");
		/*
		 * 수정 폼에서 수정 요청을 보내면서 비밀번호가 틀리면 자바스크립트로
		 * history.back()을 사용하는데 POST 요청에서 Redirect 시키지 않을 경우
		 * 브라우저에서 "양식 다시 제출 확인 - ERR_CACHE_MISS" 페이지가 뜰 수
		 * 있다. 이런 경우 응답 데이터에 노캐쉬 설정을 하면 해결할 수 있다.
		 **/
		response.setHeader("Cache-Control", "no-cache");
	}

   /*
    * afterCompletion() 메서드는 클라이언트의 요청을 처리하고 뷰를 생성해
    * 클라이언트로 전송한 후에 호출된다. 클라이언트 실행 중에 예외가 발생하게 되면
    * 이 메서드 4번째 파라미터로 예외 정보를 받을 수 있다. 예외가 발생하지 않으면
    * 4번째 파라미터는 null을 받게 된다.
    **/
   @Override
   public void afterCompletion(HttpServletRequest request,
         HttpServletResponse response, Object handler, Exception ex)
         throws Exception {
      log.info("##########LoginCheckInterceptor - afterCompletion()##########");
   }
   
}
