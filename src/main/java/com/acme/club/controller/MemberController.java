package com.acme.club.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.acme.club.exception.ResourceNotFoundException;
import com.acme.club.model.Member;
import com.acme.club.repository.MemberRepository;

@RestController
@RequestMapping("/api")
public class MemberController {

    public static final String HELLO_TEXT = "Hello from Spring Boot Backend!";

    private MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @ResponseBody
    @RequestMapping(path = "/hello")
    public String sayHello() {
        return HELLO_TEXT;
    }

    @PostMapping("/saveMember")
    public ResponseEntity<String> saveMember(@RequestBody Member member) {
        memberRepository.save(member);
        return ResponseEntity.ok(member.toString());
    }

    @GetMapping("/getMembers")
    public ResponseEntity<List<Member>> getMembers() {
        List<Member> members = memberRepository.findAll();
        return ResponseEntity.ok(members);
    }

    @PutMapping("/updateMember/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        member.setName(memberDetails.getName());
        member.setPhone(memberDetails.getPhone());
        member.setDocument(memberDetails.getDocument());

        Member updatedMember = memberRepository.save(member);
        return ResponseEntity.ok(updatedMember);
    }

    @DeleteMapping("/deleteMember/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
