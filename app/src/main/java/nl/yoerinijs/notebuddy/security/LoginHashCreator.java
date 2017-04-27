package nl.yoerinijs.notebuddy.security;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import nl.yoerinijs.notebuddy.helpers.StringGenerator;
import nl.yoerinijs.notebuddy.storage.KeyValueDB;

/**
 * This class creates a hash based on parts of the password.
 *
 * Theoretically, it is possible to decrypt the original password based on the 0's and 1's, obviously. In order to prevent decrypting the password,
 * the output is sorted completely, so no structure is visible anymore. Moreover, before the hash is created, the output is combined with random 0's and 1's.
 */
public class LoginHashCreator {

    /**
     * Returns a hash based on parts of the password and a random string
     * @param context
     * @param password
     * @return
     * @throws Exception
     */
    public String getLoginHash(@NonNull Context context, @NonNull String password) throws Exception {
        String randomPasswordString = createRandomPasswordString(context);
        String subPassword = createSubPassword(password);
        EncryptionHandler eh = new EncryptionHandler();
        return eh.hashString(sortStringAlphabetically(randomPasswordString + subPassword)).toString();
    }

    /**
     * This method returns a string of 0's and 1's based on the password input. The string should be quite unique to verify whether one
     * entered the correct password. It was debated whether a few chars were enough, but after some research is was clear that the checker
     * has to use all provided chars in order to increase uniqueness.
     * @param password
     * @return
     */
    private String createSubPassword(@NonNull String password) {

        // For every char of the password string, subtract some information
        // and create a unique string of 0's and 1's
        ArrayList<Integer> subPassword = new ArrayList<>();
        for (char ch : password.toCharArray()) {
            subPassword.add(substringMaskWithVowel(String.valueOf(ch)));
            subPassword.add(substringMaskWithAlphabetA(String.valueOf(ch)));
            subPassword.add(substringMaskWithAlphabetB(String.valueOf(ch)));
        }

        // Use the password length to retrieve more uniqueness
        final Integer passwordLength = changePasswordLength(password.length());
        subPassword.add(passwordLength);

        // Return the complete string of 0's and 1's
        StringBuilder sb = new StringBuilder();
        for (Integer s : subPassword)
        {
            sb.append(s.toString());
        }
        return sb.toString();
    }

    /**
     * Returns 0 or 1 depending on password length
     * @param passwordLength
     * @return
     */
    private int changePasswordLength(int passwordLength) {
        return passwordLength >= 8 ? 0 : 1;
    }

    /**
     * Returns 0 or 1 depending on whether string is in alphabet selection
     * @param subString
     * @return
     */
    private int substringMaskWithAlphabetA(String subString) {
        return isInPartOfAlphabet(subString) ? 0 : 1;
    }

    /**
     * Returns 0 or 1 depending on whether string is in another alphabet selection
     * @param subString
     * @return
     */
    private int substringMaskWithAlphabetB(String subString) {
        return isInFirstPartOfAlphabet(subString) ? 0 : 1;
    }

    /**
     * Returns 0 or 1 depending on whether string is vowel or not
     * @param subString
     * @return
     */
    private int substringMaskWithVowel(String subString) {
        return isVowel(subString) ? 0 : 1;
    }

    /**
     * Returns true if substring is vowel
     * @param substring
     * @return
     */
    private boolean isVowel(final String substring) {
        switch(substring.toLowerCase()) {
            case "a":
                return true;
            case "e":
                return true;
            case "i":
                return true;
            case "o":
                return true;
            case "u":
                return true;
            case "y":
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns true if substring is in alphabet selection
     * @param substring
     * @return
     */
    private boolean isInPartOfAlphabet(final String substring) {
        final String[] partOfAlphabet = {"a", "c", "e", "g", "i", "k", "m", "o", "q", "s", "u", "w", "y"};
        for(String letter : partOfAlphabet) {
            if(letter.equalsIgnoreCase(substring)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if substring is in alphabet selection
     * @param substring
     * @return
     */
    private boolean isInFirstPartOfAlphabet(final String substring) {
        final String[] firstPartOfAlphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        for(String letter : firstPartOfAlphabet) {
            if(letter.equalsIgnoreCase(substring)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that returns a random string
     * @param context
     * @return
     * @throws Exception
     */
    private String createRandomPasswordString(@NonNull Context context) throws Exception {
        KeyValueDB keyValueDB = new KeyValueDB();
        String randomPasswordString = keyValueDB.getRandomPasswordString(context);
        if (randomPasswordString == null) {
            final String randomChars = "01";
            StringGenerator rsg = new StringGenerator();
            Random random = new Random();
            randomPasswordString = rsg.generateString(random, randomChars, 50);
            keyValueDB.setRandomPasswordString(context, randomPasswordString);
            return randomPasswordString;
        }
        return randomPasswordString;
    }

    /**
     * Method that sorts a string alphabetically and returns it
     * @param str
     * @return
     */
    private String sortStringAlphabetically(@NonNull String str) {
        Character[] chars = new Character[str.length()];
        for (int i = 0; i < chars.length; i++)
            chars[i] = str.charAt(i);

        Arrays.sort(chars, new Comparator<Character>() {
            public int compare(Character c1, Character c2) {
                int cmp = Character.compare(
                        Character.toLowerCase(c1.charValue()),
                        Character.toLowerCase(c2.charValue())
                );
                if (cmp != 0) return cmp;
                return Character.compare(c1.charValue(), c2.charValue());
            }
        });

        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

}
