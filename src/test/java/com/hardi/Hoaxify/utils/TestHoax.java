package com.hardi.Hoaxify.utils;

import com.hardi.Hoaxify.domain.Hoax;

public class TestHoax {

    public static Hoax createHoax() {
        Hoax hoax = new Hoax();
        hoax.setContent("test content for the test hoax");
        return hoax;
    }

}
