package com.mysite.sbb.social;

import com.mysite.sbb.user.SiteUser;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SocialUser implements Serializable {

    private String username;
    private String password;
    private String email;

    public SocialUser(SiteUser siteUser) {
        this.username = siteUser.getUsername();
        this.password = siteUser.getPassword();
        this.email = siteUser.getEmail();
    }

}
