package com.sisa.jihunpage.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberSignupRequest(

	@JsonProperty("userid")
	@NotBlank(message = "아이디를 입력해 주세요.")
	@Pattern(
		regexp = "^[A-Za-z][A-Za-z0-9_]{5,14}$",
		message = "아이디는 영문자로 시작하는 6~15자의 영문, 숫자, 언더바만 사용할 수 있습니다."
	)
	String userId,


	@JsonProperty("userpwd")
	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]{8,}$",
		message = "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*()_+=-)를 각각 하나 이상 포함하여 8자 이상이어야 합니다."
	)
	String password,

	@JsonProperty("userpwdConfirm")
	@NotBlank(message = "비밀번호를 다시 입력해 주세요.")
	String passwordConfirm,

	@NotBlank(message = "이름을 입력해 주세요.")
	@Pattern(
		regexp = "^[가-힣]{2,7}$",
		message = "이름은 한글 2~7자로 입력해 주세요."
	)
	String username,

	@NotBlank(message = "전화번호를 입력해 주세요.")
	@Pattern(
		regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$",
		message = "전화번호는 010-1234-5678과 같은 형식으로 입력해 주세요."
	)
	String tel,

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일 형식으로 입력해 주세요.")
	String email

	) {

	@JsonIgnore
	@AssertTrue(message = "비밀번호가 일치하지 않습니다.")
	public boolean isPasswordConfirmed() {
		if (password == null || passwordConfirm == null)
			return  true;

		return  password.equals(passwordConfirm);
	}

}
