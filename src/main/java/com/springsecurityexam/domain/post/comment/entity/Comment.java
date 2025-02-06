package com.springsecurityexam.domain.post.comment.entity;

import com.springsecurityexam.domain.member.member.entity.Member;
import com.springsecurityexam.domain.post.post.entity.Post;
import com.springsecurityexam.global.entity.BaseTime;
import com.springsecurityexam.global.exception.ServiceException;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Comment extends BaseTime {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String content;

    public void modify(String content) {
        this.content = content;
    }

    public void canModify(Member actor) {

        if (actor == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (actor.isAdmin()) return;

        if (actor.equals(this.author)) return;

        throw new ServiceException("403-1", "자신이 작성한 댓글만 수정 가능합니다.");

    }

    public void canDelete(Member actor) {
        if (actor == null) {
            throw new ServiceException("401-1", "인증 정보가 없습니다.");
        }

        if (actor.isAdmin()) return;

        if (actor.equals(this.author)) return;

        throw new ServiceException("403-1", "자신이 작성한 댓글만 삭제 가능합니다.");
    }
}
