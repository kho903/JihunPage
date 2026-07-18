package com.sisa.jihunpage.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

	@JsonProperty("userid")
	@NotBlank(message = "아이디를 입력해 주세요.")
	String userid,

	@JsonProperty("userpwd")
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	String password
) {
}
