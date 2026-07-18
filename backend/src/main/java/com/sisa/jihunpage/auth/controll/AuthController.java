package com.sisa.jihunpage.auth.controll;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisa.jihunpage.auth.dto.AuthenticatedMemberResponse;
import com.sisa.jihunpage.auth.dto.LoginRequest;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticatedMemberResponse> login(@Valid @RequestBody LoginRequest request,
		HttpServletRequest httpRequest) {
		AuthenticatedMemberResponse response = authService.login(request);

		HttpSession previousSession = httpRequest.getSession(false);

		if (previousSession != null) {
			previousSession.invalidate();
		}

		HttpSession newSession = httpRequest.getSession(true);

		newSession.setAttribute(LOGIN_MEMBER_ID, response.id());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);

		if (session != null) {
			session.invalidate();
		}

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<AuthenticatedMemberResponse> getCurrentMember(HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession(false);

		if (session == null) {
			throw new UnauthenticatedException();
		}

		Object memberIdValue = session.getAttribute(LOGIN_MEMBER_ID);

		if (!(memberIdValue instanceof Long memberId)) {
			throw new UnauthenticatedException();
		}

		AuthenticatedMemberResponse response = authService.getCurrentMember(memberId);
		return ResponseEntity.ok(response);
	}
}
