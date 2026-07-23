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
import com.sisa.jihunpage.member.domain.Member;
import com.sisa.jihunpage.member.repository.MemberRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthApiTest {

	public static final String LOGIN_MEMBER_ID = "LOGIN_MEMBER_ID";
	private static final String SESSION_COOKIE_NAME = "SESSION";

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
			.andExpect(cookie().exists(SESSION_COOKIE_NAME))
			.andExpect(jsonPath("$.id").value(savedMember.getId()))
			.andExpect(jsonPath("$.userid").value("jihun_01"))
			.andExpect(jsonPath("$.username").value("김지훈"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.userpwd").doesNotExist())
			.andReturn();

		Cookie sessionCookie =
			result.getResponse().getCookie(SESSION_COOKIE_NAME);

		assertThat(sessionCookie).isNotNull();

		mockMvc.perform(get("/api/auth/me")
				.cookie(sessionCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedMember.getId()))
			.andExpect(jsonPath("$.userid").value("jihun_01"));
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
		Cookie sessionCookie = loginAndGetSessionCookie();

		mockMvc.perform(get("/api/auth/me")
				.cookie(sessionCookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(savedMember.getId()))
			.andExpect(jsonPath("$.userid").value("jihun_01"))
			.andExpect(jsonPath("$.username").value("김지훈"))
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.userpwd").doesNotExist());
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
		Cookie sessionCookie = loginAndGetSessionCookie();

		mockMvc.perform(post("/api/auth/logout")
				.cookie(sessionCookie))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/auth/me")
				.cookie(sessionCookie))
			.andExpect(status().isUnauthorized());
	}

	private Cookie loginAndGetSessionCookie() throws Exception {
		Map<String, String> request = createLoginRequest(
			"jihun_01",
			"Password1!"
		);

		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(cookie().exists(SESSION_COOKIE_NAME))
			.andReturn();

		Cookie sessionCookie =
			result.getResponse().getCookie(SESSION_COOKIE_NAME);

		assertThat(sessionCookie).isNotNull();

		return sessionCookie;
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
