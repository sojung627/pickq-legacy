package com.springbootstudy.bbs.controller.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springbootstudy.bbs.domain.MemberVO;

@Controller
public class ErrorPageController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model, HttpSession session) {
        MemberVO loginUser = (MemberVO) session.getAttribute("loginUser");
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
        }

        Object statusCodeObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        if (statusCodeObject != null) {
            try {
                statusCode = Integer.parseInt(statusCodeObject.toString());
            } catch (NumberFormatException ignored) {
                statusCode = 500;
            }
        }

        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String errorTitle;
        String errorMessage;
        switch (status) {
            case NOT_FOUND -> {
                errorTitle = "페이지를 찾을 수 없습니다";
                errorMessage = "요청하신 페이지가 존재하지 않거나, 이동되었을 수 있습니다.";
            }
            case INTERNAL_SERVER_ERROR -> {
                errorTitle = "서비스 점검중입니다";
                errorMessage = "잠시 후 다시 시도해 주세요.";
            }
            default -> {
                errorTitle = "서비스 점검중입니다";
                errorMessage = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.";
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("redirectUrl", status == HttpStatus.NOT_FOUND ? "/" : null);
        model.addAttribute("autoRedirect", status == HttpStatus.NOT_FOUND);
        model.addAttribute("countdownSeconds", 3);

        return "views/error/error";
    }
}
