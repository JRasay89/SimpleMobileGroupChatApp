package com.simplegroupchatapp.john.simplemobilegroupchatapp.app;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by John on 4/17/2015.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();
    private RequestQueue queue;
    private static AppController appControllerInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        appControllerInstance = this;
    }

    public static synchronized AppController getInstance() {
        return appControllerInstance;
    }

    public RequestQueue getRequestQueue() {
        if (queue == null) {
            queue = Volley.newRequestQueue(this);
        }

        return queue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);

    }

    public void cancelPendingRequests(Object tag) {
        if(queue != null) {
            queue.cancelAll(tag);
        }
    }
}
