package xyz.parti.catan.ui.adapter;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
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
import xyz.parti.catan.models.PartiAccessToken;
import xyz.parti.catan.services.PostsService;

/**
 * Created by dalikim on 2017. 4. 25..
 */

class DownloadFilesTask extends AsyncTask<String, String, Long> {
    public final static long RESUT_CANCEL = -1L;
    public final static long RESUT_NO_DATA = -2L;
    public final static long RESUT_ERROR = -3L;

    private final PartiAccessToken partiAccessToken;
    private final long postId;
    private final long fileSourceId;
    private ProgressDialog downloadProgressDialog;
    private Context context;
    private final String fileName;
    private File outputFile;

    public DownloadFilesTask(Context context, ProgressDialog downloadProgressDialog, PartiAccessToken partiAccessToken, long postId, long fileSourceId, String fileName) {
        this.partiAccessToken = partiAccessToken;
        this.postId = postId;
        this.fileSourceId = fileSourceId;
        this.context = context;
        this.fileName = fileName;
        this.downloadProgressDialog = downloadProgressDialog;
    }


    //파일 다운로드를 시작하기 전에 프로그레스바를 화면에 보여줍니다.
    @Override
    protected void onPreExecute() { //2
        super.onPreExecute();

        downloadProgressDialog.setMessage("다운로드 중");
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setCancelable(true);
        downloadProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                DownloadFilesTask.this.cancel(true);
            }
        });
        downloadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                DownloadFilesTask.this.cancel(true);
            }
        });

        downloadProgressDialog.show();
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
        downloadProgressDialog.setIndeterminate(false);
        downloadProgressDialog.setMax(100);
        downloadProgressDialog.setProgress(Integer.parseInt(progress[0]));
        downloadProgressDialog.setMessage(progress[1]);
    }

    //파일 다운로드 완료 후
    @Override
    protected void onPostExecute(Long size) { //5
        super.onPostExecute(size);

        downloadProgressDialog.dismiss();

        if(size <= 0) {
            String message = "";
            if(size == RESUT_CANCEL) {
                message = "다운로드가 취소되었습니다.";
            } else if(size == RESUT_ERROR) {
                message = "오류가 발생했습니다";
            } else if(size == RESUT_NO_DATA) {
                message = "해당 파일이 없습니다";
            }
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        } else {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(getExtension());
            newIntent.setDataAndType(Uri.fromFile(outputFile), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "다운로드된 파일을 열 수 있는 프로그램이 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getExtension() {
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
}
