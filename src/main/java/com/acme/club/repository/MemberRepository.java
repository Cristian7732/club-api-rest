package com.acme.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.club.model.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}