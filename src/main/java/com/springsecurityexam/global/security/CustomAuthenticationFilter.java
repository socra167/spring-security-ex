package com.springsecurityexam.global.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.springsecurityexam.global.Rq;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component // 컴포넌트 스캔 적용
public class CustomAuthenticationFilter extends OncePerRequestFilter { // 필터 역할을 수행하도록 OncePerRequestFilter 구현
	private final Rq rq;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		rq.setLogin("user1"); // user1이 로그인했다고 security에게 알려주면 security는 user1로 인식
		filterChain.doFilter(request, response); // 다음 필터, 없다면 컨트롤러 등으로 넘어간다
	}
}
