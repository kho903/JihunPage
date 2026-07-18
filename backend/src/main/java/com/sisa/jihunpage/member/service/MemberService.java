package com.sisa.jihunpage.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisa.jihunpage.domain.Member;
import com.sisa.jihunpage.member.dto.MemberSignupRequest;
import com.sisa.jihunpage.member.dto.MemberSignupResponse;
import com.sisa.jihunpage.member.exception.DuplicateUserIdException;
import com.sisa.jihunpage.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public MemberSignupResponse signup(MemberSignupRequest request) {
		validateDuplicateUserId(request.userId());

		String encodedPassword = passwordEncoder.encode(request.password());

		Member member = new Member(
			request.userId(),
			encodedPassword,
			request.username(),
			request.tel(),
			request.email()
		);
		Member savedMember = memberRepository.save(member);

		return MemberSignupResponse.from(savedMember);
	}

	private void validateDuplicateUserId(String userId) {
		if (memberRepository.existsByUserId(userId)) {
			throw new DuplicateUserIdException();
		}
	}
}
