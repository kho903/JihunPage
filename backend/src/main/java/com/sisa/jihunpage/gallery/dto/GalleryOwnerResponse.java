package com.sisa.jihunpage.gallery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sisa.jihunpage.member.domain.Member;

public record GalleryOwnerResponse(

	@JsonProperty("userid")
	String userId,

	String username
) {

	public static GalleryOwnerResponse from(Member member) {
		return new GalleryOwnerResponse(
			member.getUserId(),
			member.getUsername()
		);
	}
}
