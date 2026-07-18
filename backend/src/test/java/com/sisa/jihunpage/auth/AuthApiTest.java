package com.sisa.jihunpage.auth;

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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisa.jihunpage.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthApiTest {

	public static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Member savedMember;

	@BeforeEach
	void setUp() {
		memberRepository.deleteAll();

		Member member = new Member(
			"jihun_01",
			passwordEncoder.encode("Password1!"),
			"김지훈",
			"010-1234-5678",
			"jihun@example.com"
		);

		savedMember = memberRepository.save(member);
	}

	@Test
	void loginReturnsOkAndCreatesAuthenticatedSession() throws Exception {
		Map<String, String> request = createLoginRequest(
			"jihun_01",
			"Password1!"
		);

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedMember.getId()))
			.andExpect(jsonPath("$.userid").value("jihun_01"))
			.andExpect(jsonPath("$.username").value("김지훈"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.userpwd").doesNotExist())
			.andReturn();

		MockHttpSession session =
			(MockHttpSession)result.getRequest().getSession(false);

		assertThat(session).isNotNull();
		assertThat(session.getAttribute(LOGIN_MEMBER_ID))
			.isEqualTo(savedMember.getId());
	}

	@Test
	void loginReturnsUnauthorizedWhenUserIdIsIncorrect() throws Exception {
		Map<String, String> request = createLoginRequest(
			"wrong_user",
			"Password1!"
		);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code")
				.value("INVALID_CREDENTIALS"))
			.andExpect(jsonPath("$.message")
				.value("아이디 또는 비밀번호가 올바르지 않습니다."))
			.andExpect(jsonPath("$.errors").isEmpty());
	}

	@Test
	void loginReturnsUnauthorizedWhenPasswordIsIncorrect() throws Exception {
		Map<String, String> request = createLoginRequest(
			"jihun_01",
			"WrongPassword1!"
		);

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code")
				.value("INVALID_CREDENTIALS"))
			.andExpect(jsonPath("$.message")
				.value("아이디 또는 비밀번호가 올바르지 않습니다."))
			.andExpect(jsonPath("$.errors").isEmpty());
	}

	@Test
	void currentMemberReturnsAuthenticatedMember() throws Exception {
		MockHttpSession session = loginAndGetSession();

		mockMvc.perform(get("/api/auth/me")
				.session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedMember.getId()))
			.andExpect(jsonPath("$.userid").value("jihun_01"))
			.andExpect(jsonPath("$.username").value("김지훈"));
	}

	@Test
	void currentMemberReturnsUnauthorizedWithoutSession() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code")
				.value("UNAUTHENTICATED"))
			.andExpect(jsonPath("$.message")
				.value("로그인이 필요합니다."))
			.andExpect(jsonPath("$.errors").isEmpty());
	}

	@Test
	void logoutInvalidatesAuthenticatedSession() throws Exception {
		MockHttpSession session = loginAndGetSession();

		mockMvc.perform(post("/api/auth/logout")
				.session(session))
			.andExpect(status().isNoContent());

		assertThat(session.isInvalid()).isTrue();

		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code")
				.value("UNAUTHENTICATED"));
	}

	private MockHttpSession loginAndGetSession() throws Exception {
		Map<String, String> request = createLoginRequest(
			"jihun_01",
			"Password1!"
		);

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andReturn();

		MockHttpSession session =
			(MockHttpSession)result.getRequest().getSession(false);

		assertThat(session).isNotNull();

		return session;
	}

	private Map<String, String> createLoginRequest(
		String userId,
		String password
	) {
		return Map.of(
			"userid", userId,
			"userpwd", password
		);
	}
}
