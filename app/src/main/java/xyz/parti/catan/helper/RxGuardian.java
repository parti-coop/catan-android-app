package xyz.parti.catan.helper;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import xyz.parti.catan.Constants;

/**
 * Created by dalikim on 2017. 5. 10..
 */

public class RxGuardian {
    private List<Disposable> subscriptions = new ArrayList<>();

    public void unsubscribeAll() {
        for(Disposable subscription : subscriptions) {
            RxHelper.unsubscribe(subscription);
        }
        this.subscriptions = new ArrayList<>();
    }

    private void add(Disposable disposable) {
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

    public <T> Disposable subscribe(Disposable oldDisposable, Flowable<? extends T> newFlowable) {
        return subscribe(oldDisposable, newFlowable, Functions.emptyConsumer(), Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION);
    }

    public <T> Disposable subscribe(Disposable oldDisposable, Flowable<? extends T> newFlowable, Consumer<? super T> onNext) {
        return subscribe(oldDisposable, newFlowable, onNext, Functions.ON_ERROR_MISSING, Functions.EMPTY_ACTION);
    }

    public <T> Disposable subscribe(Disposable oldDisposable, Flowable<? extends T> newFlowable, Consumer<? super T> onNext, Consumer<? super Throwable> onError) {
        return subscribe(oldDisposable, newFlowable, onNext, onError, Functions.EMPTY_ACTION);
    }

    public <T> Disposable subscribe(Disposable oldDisposable, Flowable<? extends T> newFlowable, Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {
        RxHelper.unsubscribe(oldDisposable);
        Disposable newisposable = newFlowable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(onNext, onError, onComplete);
        add(newisposable);

        return newisposable;
    }
}
