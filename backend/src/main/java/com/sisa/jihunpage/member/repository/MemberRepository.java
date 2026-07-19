package com.sisa.jihunpage.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sisa.jihunpage.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByUserId(String userId);

	Optional<Member> findByUserId(String  userId);
}
