package xyz.parti.catan.ui.job;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;


public class CatanJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case NewMessageCheckJob.TAG:
                return new NewMessageCheckJob();
            default:
                return null;
        }
    }
}
