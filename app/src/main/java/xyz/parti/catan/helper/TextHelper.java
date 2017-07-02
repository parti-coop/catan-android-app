package xyz.parti.catan.helper;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.widget.TextView;


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
        return converToHtml(txt, null);
    }

    public static Spanned converToHtml(String txt, Html.ImageGetter getter) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(txt, Html.FROM_HTML_MODE_LEGACY, getter, null);
        } else {
            result = Html.fromHtml(txt, getter, null);
        }
        return result;
    }

    public static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, ClickableSpan clickable) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }


}
