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

    public SiteUser create(String registerId, String username, String email, String password) {
        SiteUser user = new SiteUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRegisterId(registerId);
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

    public void update(SiteUser siteUser, String password) {
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

    public SiteUser socialLogin(String registerId, String username, String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) {
            return siteUser.get();
        } else {
            return this.create(registerId, username, email, "");
        }
    }

}
