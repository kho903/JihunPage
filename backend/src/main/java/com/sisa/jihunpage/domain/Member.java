package com.sisa.jihunpage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

	protected Member() {
	}

	public Member(String userId, String password, String username, String tel, String email) {
		this.userId = userId;
		this.password = password;
		this.username = username;
		this.tel = tel;
		this.email = email;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false, unique = true, length = 15)
	private String userId;

	@Column(nullable = false, length = 100)
	private String password;

	@Column(nullable = false, length = 7)
	private String username;

	@Column(nullable = false, length = 13)
	private String tel;

	@Column(nullable = false, length = 255)
	private String email;

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public String getTel() {
		return tel;
	}

	public String getEmail() {
		return email;
	}
}
