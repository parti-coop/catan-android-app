package xyz.parti.catan.helper;

import android.text.Html;
import android.text.Spanned;

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

    public static Spanned converToHtml(String txt) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(txt, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(txt);
        }
        return result;
    }
}
