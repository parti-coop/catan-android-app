package xyz.parti.catan.helper;

import io.reactivex.disposables.Disposable;

/**
 * Created by dalikim on 2017. 5. 7..
 */

public class RxHelper {
    public static void unsubscribe(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
