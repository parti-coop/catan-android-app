package xyz.parti.catan.ui.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.dao.MessagesStatusDAO;
import xyz.parti.catan.data.model.MessagesStatus;
import xyz.parti.catan.data.model.PartiAccessToken;
import xyz.parti.catan.data.services.MessagesService;
import xyz.parti.catan.helper.CatanLog;


public class NewMessageCheckJob extends Job {
    public static final String TAG = "xyz.parti.catan.job.NewMessageCheckJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        try {
            SessionManager session = new SessionManager(getContext());
            if(!session.isLoggedIn()) {
                CatanLog.d("NewMessageCheckJob : Not Login");
                return Result.SUCCESS;
            }

            PartiAccessToken partiAccessToken = session.getPartiAccessToken();
            MessagesService messagesService = ServiceBuilder.createNoRefreshService(MessagesService.class, partiAccessToken);
            Call<MessagesStatus> lastCreatedAtCall = messagesService.getStatus();
            Response<MessagesStatus> response = lastCreatedAtCall.execute();
            if(response.isSuccessful()) {
                Realm realm = Realm.getDefaultInstance();
                try {
                    MessagesStatusDAO messagesStatusDAO = new MessagesStatusDAO(realm);
                    messagesStatusDAO.saveServerStatusSync(session.getCurrentUser(), response.body());
                    CatanLog.d("NewMessageCheckJob Saved : last created message id  " + response.body().last_created_message_id);
                } finally {
                    if(realm != null) realm.close();
                }
            } else {
                CatanLog.d("NewMessageCheckJob Error : " + response.code());
            }
        } catch (Throwable e) {
            CatanLog.e(e);
            /* ignored */
        }
        return Result.SUCCESS;
    }

    public static void scheduleJob() {
        new JobRequest.Builder(NewMessageCheckJob.TAG)
//                .setExact(10_000L)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();
    }

    public static void cancelJob() {
        JobManager.instance().cancelAllForTag(TAG);
    }
}
