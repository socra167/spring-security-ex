package com.springsecurityexam.domain.post.comment.controller;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springsecurityexam.domain.member.member.entity.Member;
import com.springsecurityexam.domain.post.comment.dto.CommentDto;
import com.springsecurityexam.domain.post.comment.entity.Comment;
import com.springsecurityexam.domain.post.post.entity.Post;
import com.springsecurityexam.domain.post.post.service.PostService;
import com.springsecurityexam.global.Rq;
import com.springsecurityexam.global.dto.RsData;
import com.springsecurityexam.global.exception.ServiceException;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts/{postId}/comments")
public class ApiV1CommentController {

	private final PostService postService;
	private final Rq rq;
	private final EntityManager em;

	@GetMapping
	@Transactional(readOnly = true) // 조회만 하는 메서드라면 readOnly를 적용하는게 낫다
	public List<CommentDto> getItems(@PathVariable long postId) {

		Post post = postService.getItem(postId).orElseThrow(
			() -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
		);

		// OSIV(Open Session In View) 설정 (default: true)
		// 원래대로라면 아래의 코드는 영속성 컨텍스트가 닫혀 getComments()에 실패해야 한다.
		// 하지만 정상적으로 작동하는 이유는, SpringBoot가 컨트롤러에 한해서 영속성 컨텍스트를 유지시켜주기 때문이다.

		// Repository
		// Service
		// Controller
		// View Layer (ThymeLeaf)
		// - Entity 사용
		// 엔티티를 사용하거나, Lazy로딩 데이터들이 그동안은 어떻게 됐는지 모르면 막 쓰는건데
		// RestFul하게 작성하면, 뷰 레이어를 구축할 필요가 없음

		// REST하게 하면, 사실상 OSIV를 켤 필요가 없다.
		// OSIV는 서버에서 뷰 레이어까지 다룰 때 언제 엔티티를 사용할지 모르기 때문에 영속성 컨텍스트를 유지시켜주는 설정
		// 언제 DB 조회를 시도할지 모르기 때문에, 뷰 레이어까지 영속성 컨텍스트를 유지시켜준다.

		// 타임리프를 사용한다면, OSIV를 켜고 하는게 편하다.
		// 하지만, RESTful로 만든다면 View를 신경쓰지 않아도 되므로 불필요한 자원 소모를 줄이도록 끄는게 낫다.(물론 켜도 잘되긴 한다)
		return post.getComments()
			.stream()
			.map(CommentDto::new)
			.toList();
	}

	@GetMapping("{id}")
	@Transactional(readOnly = true)
	public CommentDto getItem(@PathVariable long postId, @PathVariable long id) {

		Post post = postService.getItem(postId).orElseThrow(
			() -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
		);

		Comment comment = post.getCommentById(id);

		return new CommentDto(comment);
	}


	record WriteReqBody(String content) {
	}

	@PostMapping
	@Transactional // DB 반영을 위한 Transactional
	public RsData<Void> write(@PathVariable long postId, @RequestBody WriteReqBody reqBody) {
		Member actor = rq.getAuthenticatedActor();
		Comment comment = _write(postId, actor, reqBody.content());

		postService.flush();

		return new RsData<>(
			"201-1",
			"%d번 댓글 작성이 완료되었습니다.".formatted(comment.getId())
		);

	}


	record ModifyReqBody(String content) {}

	@PutMapping("{id}")
	@Transactional
	public RsData<Void> modify(@PathVariable long postId, @PathVariable long id, @RequestBody ModifyReqBody reqBody) {

		Member actor = rq.getAuthenticatedActor();

		Post post = postService.getItem(postId).orElseThrow(
			() -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
		);

		Comment comment = post.getCommentById(id);

		comment.canModify(actor);
		comment.modify(reqBody.content());

		return new RsData<>(
			"200-1",
			"%d번 댓글 수정이 완료되었습니다.".formatted(id)
		);
	}


	@DeleteMapping("{id}")
	@Transactional
	public RsData<Void> delete(@PathVariable long postId, @PathVariable long id) {

		Member actor = rq.getAuthenticatedActor();
		Post post = postService.getItem(postId).orElseThrow(
			() -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
		);

		Comment comment = post.getCommentById(id);

		comment.canDelete(actor);
		post.deleteComment(comment);

		return new RsData<>(
			"200-1",
			"%d번 댓글 삭제가 완료되었습니다.".formatted(id)
		);
	}


	public Comment _write(long postId, Member actor, String content) {

		Post post = postService.getItem(postId).orElseThrow(
			() -> new ServiceException("404-1", "존재하지 않는 게시글입니다.")
		);

		Comment comment = post.addComment(actor, content);

		return comment;
	}


}
