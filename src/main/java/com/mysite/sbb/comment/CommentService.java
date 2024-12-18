package com.mysite.sbb.comment;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment getComment(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("Comment not found");
        }
    }

    public Comment create(Question question, Answer answer, SiteUser siteUser, String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setQuestion(question);
        comment.setAnswer(answer);
        comment.setAuthor(siteUser);
        comment.setCreateDate(LocalDateTime.now());
        this.commentRepository.save(comment);
        return comment;
    }

    public Comment modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        commentRepository.save(comment);
        return comment;
    }

    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }

    public Page<Comment> getMyList(SiteUser siteUser, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sorts));
        return this.commentRepository.findByAuthor(siteUser, pageable);
    }

}
