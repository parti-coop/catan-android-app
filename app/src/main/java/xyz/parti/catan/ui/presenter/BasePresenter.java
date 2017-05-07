package xyz.parti.catan.ui.presenter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import xyz.parti.catan.Constants;
import xyz.parti.catan.helper.RxUtil;

/**
 * Created by dalikim on 2017. 5. 7..
 */

abstract class BasePresenter<T> {
    private T view;
    private List<Disposable> subscriptions = new ArrayList<>();

    public void attachView(T view) {
        Log.d(Constants.TAG_TEST, "ATTACHED!!");
        this.view = view;
    }

    public void detachView() {
        for(Disposable subscription : subscriptions) {
            Log.d(Constants.TAG_TEST, "unsubscribing!!");
            RxUtil.unsubscribe(subscription);
        }
        this.subscriptions = new ArrayList<>();
        this.view = null;
    }

    public void guardDisposable(Disposable disposable) {
        if(disposable == null) {
            return;
        }
        subscriptions.add(disposable);
        cleanUp();
    }

    private void cleanUp() {
        Iterator<Disposable> iter = subscriptions.iterator();
        while (iter.hasNext()) {
            Disposable p = iter.next();
            if (p.isDisposed()) iter.remove();
        }
    }

    public T getView() {
        return view;
    }

    public boolean isViewAttached() {
        return view != null;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new ViewNotAttachedException();
    }

    public static class ViewNotAttachedException extends RuntimeException {
        public ViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }
}
