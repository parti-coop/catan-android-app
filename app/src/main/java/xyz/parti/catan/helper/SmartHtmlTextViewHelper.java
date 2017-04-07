package xyz.parti.catan.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.parti.catan.Constants;
import xyz.parti.catan.R;

/**
 * Created by dalikim on 2017. 4. 7..
 */

public class SmartHtmlTextViewHelper {
    public static void makeLinkClickable(final Context context, SpannableStringBuilder strBuilder, final URLSpan span, ClickableSpan clickable) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public static void setTextViewHTML(final Context context, TextView text, String html) {
        CharSequence sequence = TextHelper.trimTrailingWhitespace(TextHelper.converToHtml(html));
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);

        processLinks(context, sequence, strBuilder);

        Pattern mentionPattern = Pattern.compile("(@all)");
        Matcher matcher = mentionPattern.matcher(strBuilder);
        final ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.default_text_view_link));
        while (matcher.find()) {
            strBuilder.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void processLinks(final Context context, CharSequence sequence, SpannableStringBuilder strBuilder) {
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            ClickableSpan clickable =  new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(span.getURL())));
                }

                @Override public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(ContextCompat.getColor(context, R.color.default_text_view_link));
                }
            };
            makeLinkClickable(context, strBuilder, span, clickable);
            Log.d(Constants.TAG, String.valueOf(strBuilder) + " / " + String.valueOf(span));
        }
    }
}
