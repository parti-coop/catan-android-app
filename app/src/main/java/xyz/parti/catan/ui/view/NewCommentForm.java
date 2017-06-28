package xyz.parti.catan.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.joanzapata.iconify.widget.IconButton;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Comment;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by dalikim on 2017. 6. 16..
 */

public class NewCommentForm extends FrameLayout {
    private WeakReference<Presenter> presenter = new WeakReference<Presenter>(null);

    @BindView(R.id.edittext_new_comment_input)
    EditText newCommentInputEditText;
    @BindView(R.id.button_new_comment_create)
    IconButton newCommentCreateButton;
    @BindView(R.id.layout_comment_form)
    LinearLayout commentFormLayout;

    public NewCommentForm(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_new_comment_form, this);
        ButterKnife.bind(this);
        this.presenter = new WeakReference<>(null);

        setupCommentForm();
    }

    public void focusForm(Comment comment) {
        commentFormLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        newCommentInputEditText.post(new Runnable() {
            @Override
            public void run() {
                newCommentInputEditText.setFocusableInTouchMode(true);
                newCommentInputEditText.requestFocus();
                newCommentInputEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(newCommentInputEditText, 0);
                    }
                });
            }
        });
        if(comment != null) {
            String defaultComment = String.format(Locale.getDefault(), "@%s ", comment.user.nickname);
            newCommentInputEditText.setText(defaultComment);
            newCommentInputEditText.setSelection(defaultComment.length());
        }
    }
    public void unfocusForm() {
        commentFormLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    private void setupCommentForm() {
        disableCommentCreateButton();
        newCommentInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(TextUtils.isEmpty(charSequence)) {
                    disableCommentCreateButton();
                } else {
                    enableCommentCreateButton();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    void enableCommentCreateButton() {
        newCommentCreateButton.setEnabled(true);
        newCommentCreateButton.setTextColor(ContextCompat.getColor(getContext(), R.color.style_color_primary));
    }

    void disableCommentCreateButton() {
        newCommentCreateButton.setEnabled(false);
        newCommentCreateButton.setTextColor(ContextCompat.getColor(getContext(), R.color.text_muted));
    }

    public void attachPresenter(Presenter realPresenter) {
        presenter = new WeakReference<Presenter>(realPresenter);
        newCommentCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(presenter.get() == null) return;
                presenter.get().onClickCommentCreateButton(newCommentInputEditText.getText().toString());
            }
        });
    }

    public void setSending() {
        disableCommentCreateButton();
        newCommentInputEditText.setEnabled(false);
        newCommentCreateButton.setText("{fa-circle-o-notch spin}");
    }

    public void setSendCompleted() {
        newCommentInputEditText.setText(null);
        newCommentInputEditText.setEnabled(true);
        newCommentCreateButton.setText("{fa-send}");
        enableCommentCreateButton();
    }

    public interface Presenter {
        void onClickCommentCreateButton(String body);
    }
}
