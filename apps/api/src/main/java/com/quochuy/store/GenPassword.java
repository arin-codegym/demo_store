package com.quochuy.store;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder brtyp = new BCryptPasswordEncoder();
        String rs = brtyp.encode("huy123123");
        System.out.println(rs);
   
    }
}
