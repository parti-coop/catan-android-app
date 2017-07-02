package xyz.parti.catan.helper;

import io.reactivex.disposables.Disposable;


public class RxHelper {
    public static void unsubscribe(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
