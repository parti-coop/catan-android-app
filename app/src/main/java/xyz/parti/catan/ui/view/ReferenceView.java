package xyz.parti.catan.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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
        bindFileSources(presenter, post);
        bindLinkSources(presenter, post);
        bindPoll(presenter, post);
        bindSurvey(presenter, post);
    }

    private void bindLinkSources(final PostBinder.PostBindablePresenter presenter, final Post post) {
        if(linkSourceBinder == null) {
            linkSourceBinder = new LinkSourceBinder(this);
        }

        if(post.link_source != null) {
            linkSourceBinder.bind(post.link_source);
            linkSourceBinder.getRootView().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.onClickLinkSource(post);
                }
            });
            linkSourceBinder.setVisibility(View.VISIBLE);
        } else {
            linkSourceBinder.getRootView().setOnClickListener(null);
            linkSourceBinder.setVisibility(GONE);
        }
    }

    private void bindFileSources(PostBinder.PostBindablePresenter presenter, Post post) {
        if(fileSourcesBinder == null) {
            fileSourcesBinder = new FileSourcesBinder(this);
        }

        if(post.file_sources != null && !post.expired()) {
            fileSourcesBinder.bind(presenter, post);
            fileSourcesBinder.setVisibility(View.VISIBLE);
        } else {
            if(!post.isReloading()) {
                post.setReloading();
                presenter.reloadPost(post, null);
            }
            fileSourcesBinder.setVisibility(View.GONE);
        }
    }

    private void bindPoll(PostBinder.PostBindablePresenter presenter, final Post post) {
        if(pollBinder == null) {
            pollBinder = new PollBinder(this);
        }

        if(post.poll != null) {
            pollBinder.bind(presenter, post);
            pollBinder.setVisibility(View.VISIBLE);
        } else {
            pollBinder.setVisibility(GONE);
        }
    }

    private void bindSurvey(PostBinder.PostBindablePresenter presenter, final Post post) {

        if(surveyBinder == null) {
            surveyBinder = new SurveyBinder(this);
        }
        if(post.survey != null) {
            surveyBinder.bind(presenter, post);
            surveyBinder.setVisibility(View.VISIBLE);
        } else {
            surveyBinder.setVisibility(View.GONE);
        }
    }

    public void unbind() {
        if(linkSourceBinder != null && linkSourceBinder.getRootView() != null) {
            linkSourceBinder.getRootView().setOnClickListener(null);
        }
        if(linkSourceBinder != null) {
            linkSourceBinder.unbind();
        }
        if(fileSourcesBinder != null) {
            fileSourcesBinder.unbind();
        }
        if(pollBinder != null) {
            pollBinder.unbind();
        }
        if(surveyBinder != null) {
            surveyBinder.unbind();
        }
    }
}
