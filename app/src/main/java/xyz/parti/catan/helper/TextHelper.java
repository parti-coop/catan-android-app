package xyz.parti.catan.helper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;

import com.pixplicity.htmlcompat.HtmlCompat;

import xyz.parti.catan.R;


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

    public static Spanned converToHtml(Context context, String txt) {
        return converToHtml(context, txt, null);
    }

    public static Spanned converToHtml(Context context, String txt, HtmlCompat.ImageGetter getter) {
        Spannable result = new SpannableStringBuilder(HtmlCompat.fromHtml(context, txt, HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING
                | HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM
                | HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST
                | HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV
                | HtmlCompat.FROM_HTML_OPTION_USE_CSS_COLORS, getter, null));
        replaceQuoteSpans(context, result);
        return result;
    }

    private static void replaceQuoteSpans(Context context, Spannable spannable) {
        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpans) {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomQuoteSpan(
                            ContextCompat.getColor(context, R.color.blockquote_background),
                            ContextCompat.getColor(context, R.color.blockquote_stripe),
                            context.getResources().getDimensionPixelSize(R.dimen.blockquote_width),
                            context.getResources().getDimensionPixelSize(R.dimen.blockquote_gap)),
                            start,
                            end,
                            flags);
        }
    }

    public static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, ClickableSpan clickable) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }


}
