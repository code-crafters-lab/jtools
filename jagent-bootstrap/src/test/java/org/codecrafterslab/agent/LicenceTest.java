package org.codecrafterslab.agent;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class LicenceTest {

    @Test
    void match() {
        String licence = "zEyamwEBAAAAAADqAAABZAAAAAE=";
        byte[] bytes = Base64.getDecoder().decode(licence);
        boolean match = Licence.match(bytes);
        assertFalse(match);
    }
}
