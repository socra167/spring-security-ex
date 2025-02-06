package com.springsecurityexam.domain.member.member.dto;

import java.time.LocalDateTime;

import com.springsecurityexam.domain.member.member.entity.Member;

import lombok.Getter;

@Getter
public class MemberDto {

    private long id;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String nickname;

    public MemberDto(Member member) {
        this.id = member.getId();
        this.createdDate = member.getCreatedDate();
        this.modifiedDate = member.getModifiedDate();

        this.nickname = member.getNickname();
    }
}
