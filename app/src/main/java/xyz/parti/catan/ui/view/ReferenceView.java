package xyz.parti.catan.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.ui.binder.FileSourcesBinder;
import xyz.parti.catan.ui.binder.LinkSourceBinder;
import xyz.parti.catan.ui.binder.PollBinder;
import xyz.parti.catan.ui.binder.PostBinder;
import xyz.parti.catan.ui.binder.SurveyBinder;

/**
 * Created by dalikim on 2017. 6. 14..
 */

public class ReferenceView extends FrameLayout {
    private LinkSourceBinder linkSourceBinder;
    private FileSourcesBinder fileSourcesBinder;
    private PollBinder pollBinder;
    private SurveyBinder surveyBinder;

    public ReferenceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_reference, this);
    }

    public void bindData(PostBinder.PostBindablePresenter presenter, Post post) {
        reset();
        if(post.file_sources != null) {
            bindFileSources(presenter, post);
        }
        if(post.link_source != null) {
            bindLinkSources(presenter, post);
        }
        if(post.poll != null) {
            bindPoll(presenter, post);
        }
        if(post.survey != null) {
            bindSurvey(presenter, post);
        }
    }

    private void bindLinkSources(final PostBinder.PostBindablePresenter presenter, final Post post) {
        if(linkSourceBinder == null) {
            linkSourceBinder = new LinkSourceBinder(this);
        }

        if(post.link_source != null) {
            linkSourceBinder.bindData(post.link_source);
            linkSourceBinder.getRootView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickLinkSource(post);
                }
            });
            linkSourceBinder.setVisibility(View.VISIBLE);
        }
    }

    private void bindFileSources(PostBinder.PostBindablePresenter presenter, Post post) {
        if(fileSourcesBinder == null) {
            fileSourcesBinder = new FileSourcesBinder(presenter, this);
        }

        fileSourcesBinder.bindData(post);
        fileSourcesBinder.setVisibility(View.VISIBLE);
    }

    private void bindPoll(PostBinder.PostBindablePresenter presenter, final Post post) {
        if(pollBinder == null) {
            pollBinder = new PollBinder(presenter, this);
        }

        pollBinder.bindData(post);
        pollBinder.setVisibility(View.VISIBLE);
    }

    private void bindSurvey(PostBinder.PostBindablePresenter presenter, final Post post) {
        if(surveyBinder == null) {
            surveyBinder = new SurveyBinder(presenter, this);
        }

        surveyBinder.bindData(post);
        surveyBinder.setVisibility(View.VISIBLE);
    }

    private void reset() {
        if(linkSourceBinder != null) {
            linkSourceBinder.setVisibility(ViewGroup.GONE);
        }
        if(fileSourcesBinder != null) {
            fileSourcesBinder.setVisibility(ViewGroup.GONE);
        }
        if(pollBinder != null) {
            pollBinder.setVisibility(GONE);
        }
        if(surveyBinder != null) {
            surveyBinder.setVisibility(GONE);
        }
    }
}
