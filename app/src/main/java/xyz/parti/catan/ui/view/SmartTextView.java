package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

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
    boolean dontConsumeNonUrlClicks = true;
    private boolean linkHit;

    public SmartTextView(Context context) {
        this(context, null);
    }

    public SmartTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        linkHit = false;
        super.onTouchEvent(event);
        return linkHit;
    }

    public void setRichText(String originalHtml, String truncatedHtml) {
        final CharSequence originalText = processHtml(originalHtml, true);

        if(!TextUtils.isEmpty(truncatedHtml)) {
            String expandText = getContext().getResources().getString(R.string.view_more);
            CharSequence truncatedText = processHtml(truncatedHtml.replace("<read-more></read-more>", "<a href='action://view_more'>" + expandText + "</a>"), true);
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
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    public void setNoImageRichText(String html) {
        super.setText(getSmartSpannableStringBuilder(processHtml(html, false), null));
        setMovementMethod(LocalLinkMovementMethod.getInstance());
    }

    private CharSequence processHtml(String html, boolean parseImage) {
        Html.ImageGetter asyncImageGetter = null;
        if (parseImage) {
            asyncImageGetter = new SmartHtmlImageGetter(getContext(), this);
        }
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

    public static class LocalLinkMovementMethod extends LinkMovementMethod{
        static LocalLinkMovementMethod sInstance;

        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();
            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget,
                                    Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(
                        off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else if (action == MotionEvent.ACTION_DOWN) {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }

                    if (widget instanceof SmartTextView){
                        ((SmartTextView) widget).linkHit = true;
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }
    }
}
