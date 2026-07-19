package com.sisa.jihunpage.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sisa.jihunpage.member.domain.Member;

public record MemberSignupResponse(
	Long id,

	@JsonProperty("userid")
	String userId,

	String username,

	String tel,

	String email
) {

	public static MemberSignupResponse from(Member member) {
		return new MemberSignupResponse(
			member.getId(),
			member.getUserId(),
			member.getUsername(),
			member.getTel(),
			member.getEmail()
		);
	}

}
