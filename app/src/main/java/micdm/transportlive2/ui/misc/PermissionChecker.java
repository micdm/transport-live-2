package micdm.transportlive2.ui.misc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class PermissionChecker {

    private static final int REQUEST_CODE = 0;

    @Inject
    Activity activity;
    @Inject
    Context context;

    private final BehaviorSubject<Map<String, Integer>> results = BehaviorSubject.createDefault(Collections.emptyMap());

    PermissionChecker() {}

    public Observable<Boolean> checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return Observable.just(true);
        }
        ActivityCompat.requestPermissions(activity, new String[] {permission}, REQUEST_CODE);
        return results
            .filter(results -> results.containsKey(permission))
            .map(result -> result.get(permission) == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isResultAcceptable(int code) {
        return code == REQUEST_CODE;
    }

    public void setResults(String[] permissions, int[] grantResults) {
        Map<String, Integer> results = new HashMap<>(this.results.getValue());
        for (int i = 0; i < permissions.length; i += 1) {
            results.put(permissions[i], grantResults[i]);
        }
        this.results.onNext(results);
    }
}
