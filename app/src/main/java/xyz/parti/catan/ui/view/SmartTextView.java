package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.parti.catan.R;
import xyz.parti.catan.helper.TextHelper;

/**
 * Created by dalikim on 2017. 6. 23..
 */

public class SmartTextView extends android.support.v7.widget.AppCompatTextView {
    private OnImageClickListener onImageClickListener;

    public SmartTextView(Context context) {
        this(context, null);
    }

    public SmartTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setRichText(String originalHtml, String truncatedHtml) {
        final CharSequence originalText = processHtml(originalHtml);

        if(!TextUtils.isEmpty(truncatedHtml)) {
            String expandText = getContext().getResources().getString(R.string.view_more);
            CharSequence truncatedText = processHtml(truncatedHtml.replace("<read-more></read-more>", "<a href='action://view_more'>" + expandText + "</a>"));
            ViewMoreListener viewMoreListener = new ViewMoreListener() {
                @Override
                public void onClick() {
                    setText(getSmartSpannableStringBuilder(originalText, null));
                }
            };
            setText(getSmartSpannableStringBuilder(truncatedText, viewMoreListener));
        } else {
            setText(getSmartSpannableStringBuilder(originalText, null));
        }
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setRichText(String html) {
        super.setText(processHtml(html));
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    private CharSequence processHtml(String html) {
        Html.ImageGetter asyncImageGetter = new SmartHtmlImageGetter(getContext(), this);
        Spanned spanned = TextHelper.converToHtml(html, asyncImageGetter);
        SpannableStringBuilder spannableStringBuilder;
        if (spanned instanceof SpannableStringBuilder) {
            spannableStringBuilder = (SpannableStringBuilder) spanned;
        } else {
            spannableStringBuilder = new SpannableStringBuilder(spanned);
        }

        ImageSpan[] imageSpans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), ImageSpan.class);
        final List<String> imageUrls = new ArrayList<>();

        for (int i = 0, size = imageSpans.length; i < size; i++) {
            ImageSpan imageSpan = imageSpans[i];
            String imageUrl = imageSpan.getSource();
            int start = spannableStringBuilder.getSpanStart(imageSpan);
            int end = spannableStringBuilder.getSpanEnd(imageSpan);
            imageUrls.add(imageUrl);

            final int finalI = i;
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (onImageClickListener != null) {
                        onImageClickListener.imageClicked(imageUrls, finalI);
                    }
                }
            };
            ClickableSpan[] clickableSpans = spannableStringBuilder.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan cs : clickableSpans) {
                    spannableStringBuilder.removeSpan(cs);
                }
            }
            spannableStringBuilder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return TextHelper.trimTrailingWhitespace(spanned);
    }

    private SpannableStringBuilder getSmartSpannableStringBuilder(CharSequence sequence, SmartTextView.ViewMoreListener viewMoreListener) {
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        processSmartLinks(sequence, strBuilder, viewMoreListener);

        Pattern mentionPattern = Pattern.compile("(@all)");
        Matcher matcher = mentionPattern.matcher(strBuilder);
        final ForegroundColorSpan span = new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.default_text_view_link));
        while (matcher.find()) {
            strBuilder.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return strBuilder;
    }

    private void processSmartLinks(CharSequence sequence, SpannableStringBuilder strBuilder, final SmartTextView.ViewMoreListener viewMoreListener) {
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
                        getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(span.getURL())));
                    }
                }

                @Override public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if("action://view_more".equals(span.getURL())) {
                        ds.setColor(ContextCompat.getColor(getContext(), R.color.brand_gray_less_dark));
                    } else {
                        ds.setColor(ContextCompat.getColor(getContext(), R.color.default_text_view_link));
                    }
                }
            };
            TextHelper.makeLinkClickable(strBuilder, span, clickable);
        }
    }

    interface ViewMoreListener {
        void onClick();
    }

    public interface OnImageClickListener {
        void imageClicked(List<String> imageUrls, int position);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
    }
}
