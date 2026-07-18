package com.sisa.jihunpage.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sisa.jihunpage.domain.Member;

public record AuthenticatedMemberResponse(
	Long id,

	@JsonProperty("userid")
	String userId,

	String username
) {

	public static AuthenticatedMemberResponse from(Member member) {
		return new AuthenticatedMemberResponse(
			member.getId(),
			member.getUserId(),
			member.getUsername()
		);
	}
}
