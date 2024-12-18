package com.mysite.sbb.comment;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Integer> {
    List<Question> findByQuestion(Question question);
    Page<Comment> findByAuthor(SiteUser author, Pageable pageable);
}
