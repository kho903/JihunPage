package com.sisa.jihunpage.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisa.jihunpage.member.dto.MemberSignupRequest;
import com.sisa.jihunpage.member.dto.MemberSignupResponse;
import com.sisa.jihunpage.member.service.MemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping
	public ResponseEntity<MemberSignupResponse> signup(
		@Valid @RequestBody MemberSignupRequest request
	) {
		MemberSignupResponse response = memberService.signup(request);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(response);
	}
}
