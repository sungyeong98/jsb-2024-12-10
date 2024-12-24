package com.mysite.sbb.social;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialService extends DefaultOAuth2UserService {
    
    private final UserService userService;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = null;
        String name = null;

        if ("google".equals(registrationId)) {
            email = oauth2User.getAttribute("email");
            name = oauth2User.getAttribute("name");
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = oauth2User.getAttribute("response");
            if (response != null) {
                email = (String) response.get("email");
                name = (String) response.get("name");
            }
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                email = (String) kakaoAccount.get("email");
                name = profile != null ? (String) profile.get("nickname") : null;
            }
        }

        SiteUser user = userService.socialLogin(registrationId, name, email);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        httpSession.setAttribute("user", new SocialUser(user));

        return new DefaultOAuth2User(
            authorities,
            oauth2User.getAttributes(),
            "email".equals(registrationId) ? "email" : 
            "naver".equals(registrationId) ? "response" : 
            "kakao".equals(registrationId) ? "id" : "sub"
        );
    }
} 