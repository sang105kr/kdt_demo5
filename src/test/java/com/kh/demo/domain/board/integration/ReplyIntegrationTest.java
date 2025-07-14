package com.kh.demo.domain.board.integration;

import com.kh.demo.domain.board.dao.RboardDAO;
import com.kh.demo.domain.board.entity.Replies;
import com.kh.demo.domain.board.svc.RboardSVC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * 댓글 기능 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("댓글 기능 통합 테스트")
class ReplyIntegrationTest {

    @Autowired
    private RboardSVC rboardSVC;

    @Autowired
    private RboardDAO rboardDAO;

    private Replies testReply;

    @BeforeEach
    void setUp() {
        // 테스트용 댓글 데이터 생성
        testReply = new Replies();
        testReply.setBoardId(1L);
        testReply.setEmail("test@example.com");
        testReply.setNickname("테스트사용자");
        testReply.setRcontent("테스트 댓글 내용입니다.");
        testReply.setParentId(null);
        testReply.setRgroup(null);
        testReply.setRstep(0);
        testReply.setRindent(0);
        testReply.setStatus("A");
    }

    @Test
    @DisplayName("댓글 등록 테스트")
    void testSaveReply() {
        // when
        Long replyId = rboardSVC.save(testReply);

        // then
        assertThat(replyId).isNotNull();
        assertThat(replyId).isPositive();

        // 저장된 댓글 조회 확인
        Optional<Replies> savedReply = rboardSVC.findById(replyId);
        assertThat(savedReply).isPresent();
        assertThat(savedReply.get().getBoardId()).isEqualTo(1L);
        assertThat(savedReply.get().getEmail()).isEqualTo("test@example.com");
        assertThat(savedReply.get().getNickname()).isEqualTo("테스트사용자");
        assertThat(savedReply.get().getRcontent()).isEqualTo("테스트 댓글 내용입니다.");
    }

    @Test
    @DisplayName("댓글 조회 테스트")
    void testFindReply() {
        // given
        Long replyId = rboardSVC.save(testReply);

        // when
        Optional<Replies> foundReply = rboardSVC.findById(replyId);

        // then
        assertThat(foundReply).isPresent();
        assertThat(foundReply.get().getReplyId()).isEqualTo(replyId);
    }

    @Test
    @DisplayName("게시글별 댓글 조회 테스트")
    void testFindByBoardId() {
        // given
        Long replyId1 = rboardSVC.save(testReply);

        Replies testReply2 = new Replies();
        testReply2.setBoardId(1L);
        testReply2.setEmail("test2@example.com");
        testReply2.setNickname("테스트사용자2");
        testReply2.setRcontent("테스트 댓글 내용2입니다.");
        testReply2.setParentId(null);
        testReply2.setRgroup(null);
        testReply2.setRstep(0);
        testReply2.setRindent(0);
        testReply2.setStatus("A");
        Long replyId2 = rboardSVC.save(testReply2);

        // when
        List<Replies> replies = rboardSVC.findByBoardId(1L);

        // then
        assertThat(replies).hasSize(2);
        assertThat(replies).extracting("replyId").contains(replyId1, replyId2);
    }

    @Test
    @DisplayName("댓글 페이징 조회 테스트")
    void testFindByBoardIdWithPaging() {
        // given - 여러 댓글 생성
        for (int i = 1; i <= 15; i++) {
            Replies reply = new Replies();
            reply.setBoardId(1L);
            reply.setEmail("test" + i + "@example.com");
            reply.setNickname("테스트사용자" + i);
            reply.setRcontent("테스트 댓글 내용 " + i + "입니다.");
            reply.setParentId(null);
            reply.setRgroup(null);
            reply.setRstep(0);
            reply.setRindent(0);
            reply.setStatus("A");
            rboardSVC.save(reply);
        }

        // when
        List<Replies> firstPage = rboardSVC.findByBoardIdWithPaging(1L, 1, 10);
        List<Replies> secondPage = rboardSVC.findByBoardIdWithPaging(1L, 2, 10);

        // then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5);
    }

    @Test
    @DisplayName("대댓글 등록 테스트")
    void testSaveReplyToReply() {
        // given - 부모 댓글 생성
        Long parentReplyId = rboardSVC.save(testReply);

        // when - 대댓글 생성
        Replies childReply = new Replies();
        childReply.setBoardId(1L);
        childReply.setEmail("child@example.com");
        childReply.setNickname("자식사용자");
        childReply.setRcontent("대댓글 내용입니다.");
        childReply.setParentId(parentReplyId);
        childReply.setRgroup(parentReplyId); // 부모 댓글의 ID를 그룹으로 사용
        childReply.setRstep(1);
        childReply.setRindent(1);
        childReply.setStatus("A");

        Long childReplyId = rboardSVC.save(childReply);

        // then
        assertThat(childReplyId).isNotNull();
        
        Optional<Replies> savedChildReply = rboardSVC.findById(childReplyId);
        assertThat(savedChildReply).isPresent();
        assertThat(savedChildReply.get().getParentId()).isEqualTo(parentReplyId);
        assertThat(savedChildReply.get().getRstep()).isEqualTo(1);
        assertThat(savedChildReply.get().getRindent()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    void testDeleteReply() {
        // given
        Long replyId = rboardSVC.save(testReply);

        // when
        int deletedCount = rboardSVC.deleteById(replyId);

        // then
        assertThat(deletedCount).isEqualTo(1);

        // 삭제 확인
        Optional<Replies> deletedReply = rboardSVC.findById(replyId);
        assertThat(deletedReply).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 댓글 조회 테스트")
    void testFindNonExistentReply() {
        // when
        Optional<Replies> reply = rboardSVC.findById(99999L);

        // then
        assertThat(reply).isEmpty();
    }

    @Test
    @DisplayName("댓글 내용 검증 테스트")
    void testReplyContentValidation() {
        // given - 빈 내용
        Replies emptyContentReply = new Replies();
        emptyContentReply.setBoardId(1L);
        emptyContentReply.setEmail("test@example.com");
        emptyContentReply.setNickname("테스트사용자");
        emptyContentReply.setRcontent("");
        emptyContentReply.setParentId(null);
        emptyContentReply.setRgroup(null);
        emptyContentReply.setRstep(0);
        emptyContentReply.setRindent(0);
        emptyContentReply.setStatus("A");

        // when & then
        assertThatThrownBy(() -> rboardSVC.save(emptyContentReply))
                .isInstanceOf(RuntimeException.class);
    }
} 