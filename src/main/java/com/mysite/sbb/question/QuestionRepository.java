package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Question findBySubject(String subject);
    Question findBySubjectAndContent(String subject, String content);
    List<Question> findBySubjectLike(String subject);
    Page<Question> findAll(Pageable pageable);
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);

    @Query("select "
            + "distinct q "
            + "from Question q "
            + "left outer join SiteUser u1 on q.author=u1 "
            + "left outer join Answer a on a.question=q "
            + "left outer join SiteUser u2 on a.author=u2 "
            + "where "
            + "   q.subject like %:kw% "
            + "   or q.content like %:kw% "
            + "   or u1.username like %:kw% "
            + "   or a.content like %:kw% "
            + "   or u2.username like %:kw% ")
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
    Page<Question> findAllByCategory(Category category, Pageable pageable);

    @Query("SELECT q FROM Question q WHERE q.category = :category AND (q.subject LIKE %:kw% OR q.content LIKE %:kw%)")
    Page<Question> findAllByCategoryAndKeyword(@Param("category") Category category, @Param("kw") String kw, Pageable pageable);

    Page<Question> findByAuthor(SiteUser author, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN q.answerList a " +
            "LEFT JOIN q.commentList c " +  // commentList 추가
            "WHERE " +
            "   q.subject LIKE %:kw% OR " +
            "   q.content LIKE %:kw% OR " +
            "   a.content LIKE %:kw% OR " +
            "   c.content LIKE %:kw% " + // commentList의 content도 검색에 포함
            "ORDER BY " +
            "   CASE WHEN :sortType = 'answerDate' THEN a.createDate END DESC, " +
            "   CASE WHEN :sortType = 'createDate' THEN q.createDate END DESC, " +
            "   CASE WHEN :sortType = 'commentDate' THEN c.createDate END DESC")
    Page<Question> findAllWithSort(@Param("kw") String kw, @Param("sortType") String sortType, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN q.answerList a " +
            "LEFT JOIN q.commentList c " +  // commentList 추가
            "WHERE q.category = :category AND (" +
            "   q.subject LIKE %:kw% OR " +
            "   q.content LIKE %:kw% OR " +
            "   a.content LIKE %:kw% OR " +
            "   c.content LIKE %:kw%) " +
            "ORDER BY " +
            "   CASE WHEN :sortType = 'answerDate' THEN a.createDate END DESC, " +
            "   CASE WHEN :sortType = 'createDate' THEN q.createDate END DESC, " +
            "   CASE WHEN :sortType = 'commentDate' THEN c.createDate END DESC")
    Page<Question> findAllByCategoryWithSort(@Param("category") Category category, @Param("kw") String kw, @Param("sortType") String sortType, Pageable pageable);

}
