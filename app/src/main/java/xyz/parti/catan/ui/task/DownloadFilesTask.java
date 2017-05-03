package xyz.parti.catan.ui.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import xyz.parti.catan.Constants;
import xyz.parti.catan.api.ServiceGenerator;
import xyz.parti.catan.helper.ReportHelper;
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.services.PostsService;
import xyz.parti.catan.ui.presenter.PostFeedPresenter;

/**
 * Created by dalikim on 2017. 4. 25..
 */

public class DownloadFilesTask extends AsyncTask<String, String, Long> {
    private final static long RESUT_CANCEL = -1L;
    private final static long RESUT_NO_DATA = -2L;
    private final static long RESUT_ERROR = -3L;

    private PostFeedPresenter presenter;
    private final PartiAccessToken partiAccessToken;
    private final long postId;
    private final long fileSourceId;
    private Context context;
    private final String fileName;
    private File outputFile;

    public DownloadFilesTask(PostFeedPresenter presenter, Context context, long postId, long fileSourceId, String fileName) {
        this.presenter = presenter;
        this.partiAccessToken = presenter.getPartiAccessToken();
        this.postId = postId;
        this.fileSourceId = fileSourceId;
        this.context = context;
        this.fileName = fileName;
    }


    //파일 다운로드를 시작하기 전에 프로그레스바를 화면에 보여줍니다.
    @Override
    protected void onPreExecute() { //2
        super.onPreExecute();
        presenter.onPreDownloadDocFileSource(this);
    }

    //파일 다운로드를 진행합니다.
    @Override
    protected Long doInBackground(String... args) {
        PostsService postsService = ServiceGenerator.createNoRefreshService(PostsService.class, partiAccessToken);
        Call<ResponseBody> request = postsService.downloadFile(postId, fileSourceId);
        try {
            ResponseBody body = request.execute().body();
            if(body == null) {
                return RESUT_NO_DATA;
            }

            return saveFile(body);
        } catch (IOException e) {
            Log.e(Constants.TAG, e.getMessage(), e);
            return RESUT_ERROR;
        }
    }

    private long saveFile(ResponseBody body) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        OutputStream output = new FileOutputStream(outputFile);

        long downloadedSize = 0;
        while ((count = bis.read(data)) != -1) {
            //사용자가 BACK 버튼 누르면 취소가능
            if (this.isCancelled()) {
                bis.close();
                return RESUT_CANCEL;
            }

            downloadedSize += count;
            if (fileSize > 0) {
                float percentage = ((float) downloadedSize / fileSize) * 100;
                String message = "다운로드 " + downloadedSize + "KB / " + fileSize + "KB (" + (int)percentage + "%)";
                publishProgress("" + (int) ((downloadedSize * 100) / fileSize), message);
            }
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        bis.close();

        return downloadedSize;
    }

    //다운로드 중 프로그레스바 업데이트
    @Override
    protected void onProgressUpdate(String... progress) {
        super.onProgressUpdate(progress);

        // if we get here, length is known, now set indeterminate to false
        presenter.onProgressUpdateDownloadDocFileSource(Integer.parseInt(progress[0]), progress[1]);
    }

    //파일 다운로드 완료 후
    @Override
    protected void onPostExecute(Long size) { //5
        super.onPostExecute(size);

        presenter.onPostDownloadDocFileSource();
        if(size <= 0) {
            String message = "";
            if(size == RESUT_CANCEL) {
                message = "다운로드가 취소되었습니다.";
            } else if(size == RESUT_ERROR) {
                message = "오류가 발생했습니다";
            } else if(size == RESUT_NO_DATA) {
                message = "해당 파일이 없습니다";
            }
            presenter.onFailDownloadDocFileSource(message);
        } else {
            presenter.onSuccessDownloadDocFileSource(outputFile, fileName);
        }
    }

}