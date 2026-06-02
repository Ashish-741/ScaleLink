package com.scalelink.util;

/**
 * Base62 Encoder Utility
 *
 * WHAT IS BASE62?
 * A numeral system that uses 62 characters:
 * - 26 lowercase letters (a-z)
 * - 26 uppercase letters (A-Z)
 * - 10 digits (0-9)
 * Total: 26 + 26 + 10 = 62 characters.
 *
 * WHY BASE62 FOR URL SHORTENERS?
 * 1. URL Safe: All 62 characters can be used safely in URLs without encoding.
 * 2. High Density: With just 7 characters, you get 62^7 = 3.5 TRILLION unique URLs.
 * 3. Case Sensitive: 'a' and 'A' are different, maximizing combinations.
 *
 * HOW IT WORKS (Algorithm):
 * It's just like converting base-10 (decimal) to base-2 (binary), but using 62 instead of 2.
 * 1. Take a large unique number (like database ID or a Snowflake ID)
 * 2. Divide by 62, get the remainder, map it to our character set
 * 3. Repeat with the quotient until the quotient is 0
 * 4. Reverse the string (because we calculated from least significant digit)
 *
 * WHY ARE WE USING A SECURE RANDOM GENERATOR INSTEAD OF DB IDs?
 * If we use Database IDs (1 -> 'b', 2 -> 'c'), users can guess our URLs
 * (this is called an Insecure Direct Object Reference or IDOR vulnerability).
 * E.g., if you share a private doc at /b, someone can guess /c.
 * So, we generate random strings using SecureRandom.
 */
import java.security.SecureRandom;

public class Base62Encoder {

    private static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALLOWED_CHARACTERS.length();
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a random random Base62 string of a specific length.
     */
    public static String generateRandom(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(BASE);
            sb.append(ALLOWED_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
