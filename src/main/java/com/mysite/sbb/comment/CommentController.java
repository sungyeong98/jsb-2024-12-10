package com.mysite.sbb.comment;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/question/{id}")
    public String createQuestionCommentForm(CommentForm commentForm, @PathVariable("id") Integer id) {
        return "comment_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/question/{id}")
    public String createQuestionComment(Model model, @PathVariable("id") Integer id, @Valid CommentForm commentForm, Principal principal, BindingResult bindingResult) {
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_detail";
        }
        Comment comment = this.commentService.create(question, null, siteUser, commentForm.getContent());
        return String.format("redirect:/question/detail/%s#comment_%s", comment.getQuestion().getId(), comment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/answer/{id}")
    public String createAnswerCommentForm(CommentForm commentForm, @PathVariable("id") Integer id) {
        return "comment_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/answer/{id}")
    public String createAnswerComment(Model model, @PathVariable("id") Integer id, @Valid CommentForm commentForm, Principal principal, BindingResult bindingResult) {
        Answer answer = this.answerService.getAnswer(id);
        Question question = answer.getQuestion();
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_detail";
        }
        this.commentService.create(null, answer, siteUser, commentForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s", question.getId(), id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String modifyComment(CommentForm commentForm, @PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        commentForm.setContent(comment.getContent());
        return "comment_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String modifyComment(@Valid CommentForm commentForm, BindingResult bindingResult, @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "comment_form";
        }
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        Integer targetId = this.getTargetId(comment);
        this.commentService.modify(comment, commentForm.getContent());
        return String.format("redirect:/question/detail/%s#question_%s", targetId, comment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String deleteComment(@PathVariable("id") Integer id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        Integer targetId = this.getTargetId(comment);
        this.commentService.delete(comment);
        return String.format("redirect:/question/detail/%s", targetId);
    }

    private Integer getTargetId(Comment comment) {
        if (comment.getQuestion() != null) {
            return comment.getQuestion().getId();
        } else if (comment.getAnswer() != null) {
            return comment.getAnswer().getQuestion().getId();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "에러");
        }
    }

}
