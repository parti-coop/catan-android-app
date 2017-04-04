package xyz.parti.catan.helper;

/**
 * Created by dalikim on 2017. 3. 31..
 */

public class TextHelper {
    public static CharSequence trimTrailingWhitespace(CharSequence source) {
        if(source == null)
            return "";
        int i = source.length();
        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }
        return source.subSequence(0, i+1);
    }
}
