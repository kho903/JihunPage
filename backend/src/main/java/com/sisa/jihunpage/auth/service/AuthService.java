package com.sisa.jihunpage.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisa.jihunpage.auth.dto.AuthenticatedMemberResponse;
import com.sisa.jihunpage.auth.dto.LoginRequest;
import com.sisa.jihunpage.auth.exception.InvalidCredentialsException;
import com.sisa.jihunpage.auth.exception.UnauthenticatedException;
import com.sisa.jihunpage.member.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public AuthenticatedMemberResponse login(LoginRequest request) {
		Member member = memberRepository.findByUserId(request.userid())
			.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new InvalidCredentialsException();
		}

		return AuthenticatedMemberResponse.from(member);
	}

	public AuthenticatedMemberResponse getCurrentMember(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(UnauthenticatedException::new);

		return AuthenticatedMemberResponse.from(member);
	}
}
