package xyz.parti.catan.ui.presenter;

import java.io.File;

import xyz.parti.catan.models.FileSource;
import xyz.parti.catan.models.Option;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.models.Post;
import xyz.parti.catan.models.User;
import xyz.parti.catan.ui.task.DownloadFilesTask;

/**
 * Created by dalikim on 2017. 5. 3..
 */
public interface PostFeedPresenter {
    void onClickLinkSource(String url);
    void onClickDocFileSource(Post post, FileSource fileSource);
    void onClickImageFileSource(Post post);
    void onClickMoreComments(Post post);
    void onClickSurveyOption(Post post, Option option, boolean isChecked);
    void onClickPollAgree(Post post);
    void onClickPollDisgree(Post post);
    void onPreDownloadDocFileSource(DownloadFilesTask task);
    void onProgressUpdateDownloadDocFileSource(int percentage, String message);
    void onPostDownloadDocFileSource();
    void onSuccessDownloadDocFileSource(File outputFile, String fileName);

    User getCurrentUser();
    PartiAccessToken getPartiAccessToken();
}
