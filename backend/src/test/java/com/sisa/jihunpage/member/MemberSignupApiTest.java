package com.sisa.jihunpage.member;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisa.jihunpage.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class MemberSignupApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();
	}

	@Test
	void signupReturnsCreatedAndSaveMember() throws Exception {
		Map<String, String> request = Map.of(
			"userid", "jihun_01",
			"userpwd", "Password1!",
			"userpwdConfirm", "Password1!",
			"username", "김지훈",
			"tel", "010-1234-5678",
			"email", "jihun@example.com"
		);

		mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.userid").value("jihun_01"))
			.andExpect(jsonPath("$.username").value("김지훈"))
			.andExpect(jsonPath("$.tel").value("010-1234-5678"))
			.andExpect(jsonPath("$.email").value("jihun@example.com"))
			.andExpect(jsonPath("$.userpwd").doesNotExist())
			.andExpect(jsonPath("$.password").doesNotExist());

		Member savedMember = memberRepository
			.findByUserId("jihun_01")
			.orElseThrow();

		assertThat(savedMember.getUsername()).isEqualTo("김지훈");
		assertThat(savedMember.getPassword()).isNotEqualTo("Password1!");
		assertThat(
			passwordEncoder.matches(
				"Password1!",
				savedMember.getPassword()
			)
		).isTrue();
	}

	@Test
	void signupReturnsBadRequestWhenRequestIsInvalid() throws Exception {
		Map<String, String> request = Map.of(
			"userid", "1",
			"userpwd", "1234",
			"userpwdConfirm", "9999",
			"username", "A",
			"tel", "123",
			"email", "invalid-email"
		);

		mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.message").value("입력값을 확인해주세요."))
			.andExpect(jsonPath("$.errors.userid").exists())
			.andExpect(jsonPath("$.errors.userpwd").exists())
			.andExpect(jsonPath("$.errors.userpwdConfirm").exists())
			.andExpect(jsonPath("$.errors.username").exists())
			.andExpect(jsonPath("$.errors.tel").exists())
			.andExpect(jsonPath("$.errors.email").exists());

		assertThat(memberRepository.count()).isZero();
	}

	@Test
	void signupReturnsConflictWhenUserIdAlreadyExists() throws Exception {
		Map<String, String> request = Map.of(
			"userid", "jihun_01",
			"userpwd", "Password1!",
			"userpwdConfirm", "Password1!",
			"username", "김지훈",
			"tel", "010-1234-5678",
			"email", "jihun@example.com"
		);

		String requestBody = objectMapper.writeValueAsString(request);

		mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/members")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DUPLICATE_USER_ID"))
			.andExpect(jsonPath("$.message")
				.value("이미 사용 중인 아이디입니다."))
			.andExpect(jsonPath("$.errors").isEmpty());

		assertThat(memberRepository.count()).isEqualTo(1);
	}
}
