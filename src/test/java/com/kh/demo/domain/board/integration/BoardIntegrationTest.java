package com.kh.demo.domain.board.integration;

import com.kh.demo.domain.board.dao.BoardDAO;
import com.kh.demo.domain.board.entity.Boards;
import com.kh.demo.domain.board.svc.BoardSVC;
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
 * 게시글 기능 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("게시글 기능 통합 테스트")
class BoardIntegrationTest {

    @Autowired
    private BoardSVC boardSVC;

    @Autowired
    private BoardDAO boardDAO;

    private Boards testBoard;

    @BeforeEach
    void setUp() {
        // 테스트용 게시글 데이터 생성
        testBoard = new Boards();
        testBoard.setBcategory(1L);
        testBoard.setTitle("테스트 게시글 제목");
        testBoard.setNickname("테스트작성자");
        testBoard.setEmail("test@example.com");
        testBoard.setBcontent("테스트 게시글 내용입니다."); // String 타입으로 수정
        testBoard.setStatus("A");
    }

    @Test
    @DisplayName("게시글 등록 테스트")
    void testSaveBoard() {
        // when
        Long boardId = boardSVC.save(testBoard);

        // then
        assertThat(boardId).isNotNull();
        assertThat(boardId).isPositive();

        // 저장된 게시글 조회 확인
        Optional<Boards> savedBoard = boardSVC.findById(boardId);
        assertThat(savedBoard).isPresent();
        assertThat(savedBoard.get().getBcategory()).isEqualTo(1L);
        assertThat(savedBoard.get().getTitle()).isEqualTo("테스트 게시글 제목");
        assertThat(savedBoard.get().getNickname()).isEqualTo("테스트작성자");
        assertThat(savedBoard.get().getEmail()).isEqualTo("test@example.com");
        
        // String 내용 확인
        String content = savedBoard.get().getBcontent();
        assertThat(content).isEqualTo("테스트 게시글 내용입니다.");
    }

    @Test
    @DisplayName("게시글 조회 테스트")
    void testFindBoard() {
        // given
        Long boardId = boardSVC.save(testBoard);

        // when
        Optional<Boards> foundBoard = boardSVC.findById(boardId);

        // then
        assertThat(foundBoard).isPresent();
        assertThat(foundBoard.get().getBoardId()).isEqualTo(boardId);
    }

    @Test
    @DisplayName("게시글 목록 조회 테스트")
    void testFindAllBoards() {
        // given - 여러 게시글 생성
        for (int i = 1; i <= 5; i++) {
            Boards board = new Boards();
            board.setBcategory(1L);
            board.setTitle("테스트 게시글 제목 " + i);
            board.setNickname("테스트작성자" + i);
            board.setEmail("test" + i + "@example.com");
            board.setBcontent("테스트 게시글 내용 " + i + "입니다."); // String 타입으로 수정
            board.setStatus("A");
            boardSVC.save(board);
        }

        // when
        List<Boards> boards = boardSVC.findAll();

        // then
        assertThat(boards).hasSize(5);
        assertThat(boards).extracting("title").contains("테스트 게시글 제목 1", "테스트 게시글 제목 2");
    }

    @Test
    @DisplayName("게시글 페이징 조회 테스트")
    void testFindAllWithPaging() {
        // given - 여러 게시글 생성
        for (int i = 1; i <= 15; i++) {
            Boards board = new Boards();
            board.setBcategory(1L);
            board.setTitle("테스트 게시글 제목 " + i);
            board.setNickname("테스트작성자" + i);
            board.setEmail("test" + i + "@example.com");
            board.setBcontent("테스트 게시글 내용 " + i + "입니다."); // String 타입으로 수정
            board.setStatus("A");
            boardSVC.save(board);
        }

        // when
        List<Boards> firstPage = boardSVC.findAllWithPaging(0, 10);
        List<Boards> secondPage = boardSVC.findAllWithPaging(10, 10);

        // then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5);
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 테스트")
    void testFindByBcategory() {
        // given - 카테고리 1 게시글
        Long boardId1 = boardSVC.save(testBoard);

        // 카테고리 2 게시글
        Boards board2 = new Boards();
        board2.setBcategory(2L);
        board2.setTitle("카테고리 2 게시글");
        board2.setNickname("테스트작성자2");
        board2.setEmail("test2@example.com");
        board2.setBcontent("카테고리 2 게시글 내용입니다."); // String 타입으로 수정
        board2.setStatus("A");
        Long boardId2 = boardSVC.save(board2);

        // when
        List<Boards> category1Boards = boardSVC.findByBcategory(1L);
        List<Boards> category2Boards = boardSVC.findByBcategory(2L);

        // then
        assertThat(category1Boards).hasSize(1);
        assertThat(category1Boards.get(0).getBoardId()).isEqualTo(boardId1);
        assertThat(category2Boards).hasSize(1);
        assertThat(category2Boards.get(0).getBoardId()).isEqualTo(boardId2);
    }

    @Test
    @DisplayName("카테고리별 게시글 페이징 조회 테스트")
    void testFindByBcategoryWithPaging() {
        // given - 카테고리 1에 여러 게시글 생성
        for (int i = 1; i <= 15; i++) {
            Boards board = new Boards();
            board.setBcategory(1L);
            board.setTitle("카테고리 1 게시글 " + i);
            board.setNickname("테스트작성자" + i);
            board.setEmail("test" + i + "@example.com");
            board.setBcontent("카테고리 1 게시글 내용 " + i + "입니다."); // String 타입으로 수정
            board.setStatus("A");
            boardSVC.save(board);
        }

        // when
        List<Boards> firstPage = boardSVC.findByBcategoryWithPaging(1L, 0, 10);
        List<Boards> secondPage = boardSVC.findByBcategoryWithPaging(1L, 10, 10);

        // then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void testUpdateBoard() {
        // given
        Long boardId = boardSVC.save(testBoard);

        // when
        Boards updateBoard = new Boards();
        updateBoard.setBoardId(boardId);
        updateBoard.setBcategory(1L);
        updateBoard.setTitle("수정된 게시글 제목");
        updateBoard.setNickname("수정된작성자");
        updateBoard.setEmail("updated@example.com");
        updateBoard.setBcontent("수정된 게시글 내용입니다."); // String 타입으로 수정
        updateBoard.setStatus("A");

        int updatedCount = boardSVC.update(updateBoard);

        // then
        assertThat(updatedCount).isEqualTo(1);

        // 수정 확인
        Optional<Boards> updatedBoard = boardSVC.findById(boardId);
        assertThat(updatedBoard).isPresent();
        assertThat(updatedBoard.get().getTitle()).isEqualTo("수정된 게시글 제목");
        assertThat(updatedBoard.get().getNickname()).isEqualTo("수정된작성자");
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void testDeleteBoard() {
        // given
        Long boardId = boardSVC.save(testBoard);

        // when
        int deletedCount = boardSVC.deleteById(boardId);

        // then
        assertThat(deletedCount).isEqualTo(1);

        // 삭제 확인
        Optional<Boards> deletedBoard = boardSVC.findById(boardId);
        assertThat(deletedBoard).isEmpty();
    }

    @Test
    @DisplayName("전체 게시글 수 조회 테스트")
    void testCountAll() {
        // given - 여러 게시글 생성
        for (int i = 1; i <= 3; i++) {
            Boards board = new Boards();
            board.setBcategory(1L);
            board.setTitle("테스트 게시글 " + i);
            board.setNickname("테스트작성자" + i);
            board.setEmail("test" + i + "@example.com");
            board.setBcontent("테스트 게시글 내용 " + i); // String 타입으로 수정
            board.setStatus("A");
            boardSVC.save(board);
        }

        // when
        int totalCount = boardSVC.countAll();

        // then
        assertThat(totalCount).isEqualTo(3);
    }

    @Test
    @DisplayName("카테고리별 게시글 수 조회 테스트")
    void testCountByBcategory() {
        // given - 카테고리 1에 2개, 카테고리 2에 1개 게시글 생성
        for (int i = 1; i <= 2; i++) {
            Boards board = new Boards();
            board.setBcategory(1L);
            board.setTitle("카테고리 1 게시글 " + i);
            board.setNickname("테스트작성자" + i);
            board.setEmail("test" + i + "@example.com");
            board.setBcontent("카테고리 1 게시글 내용 " + i); // String 타입으로 수정
            board.setStatus("A");
            boardSVC.save(board);
        }

        Boards board3 = new Boards();
        board3.setBcategory(2L);
        board3.setTitle("카테고리 2 게시글");
        board3.setNickname("테스트작성자3");
        board3.setEmail("test3@example.com");
        board3.setBcontent("카테고리 2 게시글 내용"); // String 타입으로 수정
        board3.setStatus("A");
        boardSVC.save(board3);

        // when
        int category1Count = boardSVC.countByBcategory(1L);
        int category2Count = boardSVC.countByBcategory(2L);

        // then
        assertThat(category1Count).isEqualTo(2);
        assertThat(category2Count).isEqualTo(1);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 테스트")
    void testFindNonExistentBoard() {
        // when
        Optional<Boards> board = boardSVC.findById(99999L);

        // then
        assertThat(board).isEmpty();
    }
} 