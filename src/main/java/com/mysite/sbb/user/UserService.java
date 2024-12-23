package com.mysite.sbb.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.mysite.sbb.DataNotFoundException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(user);
        return user;
    }

    public SiteUser getUser(String emailOrUsername) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(emailOrUsername);
        if (siteUser.isPresent()) {
            return siteUser.get();
        }
        siteUser = this.userRepository.findByEmail(emailOrUsername);
        if (siteUser.isPresent()) {
            return siteUser.get();
        }
        throw new DataNotFoundException("사용자를 찾을 수 없습니다.");
    }

    public void modify(SiteUser siteUser, String password) {
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

    public boolean checkUser(String emailOrUsername) {
        return this.userRepository.findByUsername(emailOrUsername).isPresent() 
            || this.userRepository.findByEmail(emailOrUsername).isPresent();
    }

    public void update(SiteUser siteUser, String password) {
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

}
