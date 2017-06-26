package xyz.parti.catan.helper;

import android.text.TextUtils;

import com.facebook.stetho.common.StringUtil;

import org.w3c.dom.Text;

import java.util.Comparator;

/**
 * Created by dalikim on 2017. 6. 26..
 */

public class OrderingByKoreanEnglishNumbuerSpecial {
    private static final int REVERSE  = -1;
    private static final int LEFT_FIRST  = -1;
    private static final int RIGHT_FIRST  = 1;

    public static Comparator<String> getComparator() {
        return  new Comparator<String>() {
            public int compare(String left, String right) {
                return OrderingByKoreanEnglishNumbuerSpecial.compare(left, right);
            }
        };
    }

    public static int compare(String left, String right) {
        if(TextUtils.isEmpty(left)) left = "";
        if(TextUtils.isEmpty(right)) right = "";

        left = left.toUpperCase().replaceAll(" ", "");
        right = right.toUpperCase().replaceAll(" ", "");

        int leftLen = left.length();
        int rightLen = right.length();
        int minLen = Math.min(leftLen, rightLen);

        for(int i = 0; i < minLen; ++i) {
            char leftChar = left.charAt(i);
            char rightChar = right.charAt(i);

            if (leftChar != rightChar) {
                if (isKoreanAndEnglish(leftChar, rightChar)
                        || isKoreanAndNumber(leftChar, rightChar)
                        || isEnglishAndNumber(leftChar, rightChar)
                        || isKoreanAndSpecial(leftChar, rightChar)) {
                    return (leftChar - rightChar) * REVERSE;
                } else if (isEnglishAndSpecial(leftChar, rightChar)
                        || isNumberAndSpecial(leftChar, rightChar)) {
                    if (isEnglish(leftChar) || isNumber(leftChar)) {
                        return LEFT_FIRST;
                    } else {
                        return RIGHT_FIRST;
                    }
                } else {
                    return leftChar - rightChar;
                }
            }
        }

        return leftLen - rightLen;
    }

    private static boolean isKoreanAndEnglish(char ch1, char ch2) {
        return (isEnglish(ch1) && isKorean(ch2))
                || (isKorean(ch1) && isEnglish(ch2));
    }

    private static boolean isKoreanAndNumber(char ch1, char ch2) {
        return (isNumber(ch1) && isKorean(ch2))
                || (isKorean(ch1) && isNumber(ch2));
    }

    private static boolean isEnglishAndNumber(char ch1, char ch2) {
        return (isNumber(ch1) && isEnglish(ch2))
                || (isEnglish(ch1) && isNumber(ch2));
    }

    private static boolean isKoreanAndSpecial(char ch1, char ch2) {
        return (isKorean(ch1) && isSpecial(ch2))
                || (isSpecial(ch1) && isKorean(ch2));
    }

    private static boolean isEnglishAndSpecial(char ch1, char ch2) {
        return (isEnglish(ch1) && isSpecial(ch2))
                || (isSpecial(ch1) && isEnglish(ch2));
    }

    private static boolean isNumberAndSpecial(char ch1, char ch2) {
        return (isNumber(ch1) && isSpecial(ch2))
                || (isSpecial(ch1) && isNumber(ch2));
    }

    public static boolean isEnglish(char ch){
        return (ch >= (int)'A' && ch <= (int)'Z')
                || (ch >= (int)'a' && ch <= (int)'z');
    }

    public static boolean isKorean(char ch) {
        return ch >= Integer.parseInt("AC00", 16)
                && ch <= Integer.parseInt("D7A3", 16);
    }

    public static boolean isNumber(char ch) {
        return ch >= (int)'0' && ch <= (int)'9';
    }

    public static boolean isSpecial(char ch) {
        return (ch >= (int)'!' && ch <= (int)'/') // !"#$%&'()*+,-./
                || (ch >= (int)':' && ch <= (int)'@') //:;<=>?@
                || (ch >= (int)'[' && ch <= (int)'`') //[\]^_`
                || (ch >= (int)'{' && ch <= (int)'~'); //{|}~
    }
}
