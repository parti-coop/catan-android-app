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
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.parti.catan.R;
import xyz.parti.catan.ui.view.GlideImageGetter;

/**
 * Created by dalikim on 2017. 3. 31..
 */

public class TextHelper {
    private static final int TAG_ORIGIN = 2030;
    private Context context;

    public TextHelper(Context context) {
        this.context = context;
    }

    public CharSequence trimTrailingWhitespace(CharSequence source) {
        if(source == null)
            return "";
        int i = source.length();
        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }
        return source.subSequence(0, i+1);
    }

    public Spanned converToHtml(String txt) {
        return converToHtml(txt, null);
    }

    public Spanned converToHtml(String txt, Html.ImageGetter getter) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(txt, Html.FROM_HTML_MODE_LEGACY, getter, null);
        } else {
            result = Html.fromHtml(txt, getter, null);
        }
        return result;
    }

    public void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, ClickableSpan clickable) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    public void setTextViewHTML(TextView textView, String html) {
        setTextViewHTML(textView, html, null);
    }

    public void setTextViewHTML(TextView textView, String html, String truncatedHtml) {
        CharSequence originSequence = trimTrailingWhitespace(converToHtml(html, new GlideImageGetter(context, textView)));
        if(!TextUtils.isEmpty(truncatedHtml)) {
            String expandText = context.getResources().getString(R.string.view_more);
            CharSequence truncatedSequence = converToHtml(truncatedHtml.replace("<read-more></read-more>", "<a href='action://view_more'>" + expandText + "</a>"),
                    new GlideImageGetter(context, textView));
            ViewMoreListener viewMoreListener = () -> textView.setText(getSmartSpannableStringBuilder(originSequence, null));
            textView.setText(getSmartSpannableStringBuilder(trimTrailingWhitespace(truncatedSequence), viewMoreListener));
        } else {
            textView.setText(getSmartSpannableStringBuilder(originSequence, null));
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @NonNull
    private SpannableStringBuilder getSmartSpannableStringBuilder(CharSequence sequence, ViewMoreListener viewMoreListener) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        processLinks(sequence, strBuilder, viewMoreListener);

        Pattern mentionPattern = Pattern.compile("(@all)");
        Matcher matcher = mentionPattern.matcher(strBuilder);
        final ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(context, R.color.default_text_view_link));
        while (matcher.find()) {
            strBuilder.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return strBuilder;
    }

    private void processLinks(CharSequence sequence, SpannableStringBuilder strBuilder, ViewMoreListener viewMoreListener) {
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            ClickableSpan clickable =  new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if("action://view_more".equals(span.getURL())) {
                        if(viewMoreListener != null) {
                            viewMoreListener.onClick();
                        }
                    } else {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(span.getURL())));
                    }
                }

                @Override public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if("action://view_more".equals(span.getURL())) {
                        ds.setColor(ContextCompat.getColor(context, R.color.brand_gray_less_dark));
                    } else {
                        ds.setColor(ContextCompat.getColor(context, R.color.default_text_view_link));
                    }
                }
            };
            makeLinkClickable(strBuilder, span, clickable);
        }
    }

    interface ViewMoreListener {
        void onClick();
    }
}
