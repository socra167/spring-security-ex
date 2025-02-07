package com.springsecurityexam.global;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.springsecurityexam.domain.member.member.entity.Member;
import com.springsecurityexam.domain.member.member.service.MemberService;
import com.springsecurityexam.global.exception.ServiceException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// Request, Response, Session, Cookie, Header
@Component
@RequiredArgsConstructor
@RequestScope
public class Rq {

	private final HttpServletRequest request;
	private final MemberService memberService;

	public Member getAuthenticatedActor() {

		String authorizationValue = request.getHeader("Authorization");
		String apiKey = authorizationValue.substring("Bearer ".length());
		Optional<Member> opActor = memberService.findByApiKey(apiKey);

		if (opActor.isEmpty()) {
			throw new ServiceException("401-1", "잘못된 인증키입니다.");
		}

		return opActor.get();

	}

	public void setLogin(String username) {
		// Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 인증 정보 저장소
		// security는 인증된 사람이 여기 들어 있다고 생각하고 사용한다
		UserDetails user = new User(username, "", List.of());

		// 인증 정보를 수동으로 등록
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
		);
	}

	// 인증 정보를 통해서 회원 정보를 가져오는 메서드
	public Member getActor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // 인증 정보
		if (authentication == null) {
			throw new ServiceException("401-2", "로그인이 필요합니다.");
		}

		UserDetails user = (UserDetails)authentication.getPrincipal();
		if (user == null) {
			throw new ServiceException("401-3", "로그인이 필요합니다.");
		}

		String username = user.getUsername();
		return memberService.findByUsername(username).get();
	}
}
