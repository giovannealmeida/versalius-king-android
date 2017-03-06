package br.com.versalius.iking.network;

import com.android.volley.VolleyError;

/**
 * Created by jn18 on 13/01/2017.
 */
public interface ResponseCallback {
    void onSuccess(String jsonStringResponse);
    void onFail(VolleyError error);
}
