package com.mysite.sbb.user;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "PasswordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public String profile(@RequestParam(value = "questionPage", defaultValue = "0") int questionPage,
                          @RequestParam(value = "answerPage", defaultValue = "0") int answerPage,
                          @RequestParam(value = "commentPage", defaultValue = "0") int commentPage,
                          Principal principal, Model model) {

        // 현재 로그인한 사용자 정보 가져오기
        SiteUser siteUser = this.userService.getUser(principal.getName());

        // 사용자의 질문, 답변, 댓글 데이터 가져오기
        Page<Question> questionPageData = this.questionService.getMyList(siteUser, questionPage);
        Page<Answer> answerPageData = this.answerService.getMyList(siteUser, answerPage);
        Page<Comment> commentPageData = this.commentService.getMyList(siteUser, commentPage);

        // Model에 데이터 추가
        model.addAttribute("siteUser", siteUser); // 사용자 정보
        model.addAttribute("questionPageData", questionPageData); // 질문 데이터
        model.addAttribute("answerPageData", answerPageData); // 답변 데이터
        model.addAttribute("commentPageData", commentPageData); // 댓글 데이터

        return "profile";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify")
    public String modifyPassword(UserModifyForm userModifyForm, Principal principal) {
        return "password_change_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String modifyPassword(@Valid UserModifyForm userModifyForm, BindingResult bindingResult, Principal principal) {
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            return "password_change_form";
        }
        if (!userModifyForm.getPassword1().equals(userModifyForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "새 비밀번호가 일치하지 않습니다.");
            return "password_change_form";
        }
        if (!this.passwordEncoder.matches(userModifyForm.getOriginPassword(), siteUser.getPassword())) {
            bindingResult.rejectValue("originPassword", "passwordInCorrect", "기존 비밀번호가 일치하지 않습니다.");
            return "password_change_form";
        }

        try {
            this.userService.modify(siteUser, userModifyForm.getPassword1());
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("modifyFailed", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/find")
    public String findPassword(Model model) {
        return "password_find_form";
    }

    @PostMapping("/find")
    public String findPassword(Model model, @RequestParam("email") String email) {
        return "redirect:/user/login";
    }

}
