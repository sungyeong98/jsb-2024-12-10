package com.mysite.sbb.user;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.security.SecureRandom;
import java.util.Random;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

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
    public String findPassword(Model model, @RequestParam("emailOrUsername") String emailOrUsername, RedirectAttributes redirectAttributes) {
        try {
            // 사용자 찾기
            SiteUser siteUser = this.userService.getUser(emailOrUsername);
            
            // 이메일 메시지 준비
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(siteUser.getEmail()); // 항상 사용자의 이메일 주소를 사용
            message.setSubject("임시 비밀번호가 발급되었습니다.");
            
            String token = PasswordGenerator.generateRandomPassword();
            StringBuilder sb = new StringBuilder();
            sb.append(siteUser.getUsername()).append("님의 비밀번호를 초기화하였습니다.\n")
              .append("새로운 비밀번호는 ").append(token).append(" 입니다.\n")
              .append("로그인 후 반드시 비밀번호를 변경해주세요.");
            message.setText(sb.toString());
            
            this.userService.update(siteUser, token);
            
            // 이메일 전송
            new Thread(() -> mailSender.send(message)).start();
            
            // 성공 메시지 설정
            redirectAttributes.addFlashAttribute("message", 
                String.format("'%s' 이메일로 임시 비밀번호가 전송되었습니다.", siteUser.getEmail()));
            return "redirect:/user/login";
            
        } catch (DataNotFoundException e) {
            model.addAttribute("error", "입력하신 정보와 일치하는 사용자가 없습니다.");
            return "password_find_form";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 재설정 중 오류가 발생했습니다.");
            return "password_find_form";
        }
    }

    public static class PasswordGenerator {
        private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        private static final String NUMBER = "0123456789";
        private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

        private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
        private static final int PASSWORD_LENGTH = 12;

        public static String generateRandomPassword() {
            if (PASSWORD_LENGTH < 1) throw new IllegalArgumentException("Password length must be at least 1");

            StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
            Random random = new SecureRandom();
            for (int i = 0; i < PASSWORD_LENGTH; i++) {
                int rndCharAt = random.nextInt(PASSWORD_ALLOW_BASE.length());
                char rndChar = PASSWORD_ALLOW_BASE.charAt(rndCharAt);
                sb.append(rndChar);
            }

            return sb.toString();
        }
    }

}
