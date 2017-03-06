package br.com.versalius.iking.network;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import br.com.versalius.iking.utils.SessionHelper;


/**
 * Created by jn18 on 13/01/2017.
 */
public class CustomRequest extends Request<String> {
    private Response.Listener<String> listener;
    private Map<String, String> params;

    public CustomRequest(int method, String url, Map<String, String> params,
                         Response.Listener<String> reponseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    @Override
    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
        return params;
    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<String, String>();
        String creds = String.format("%s:%s", "admin", "1234");
        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
        params.put("Authorization", auth);
        /*if (SessionHelper.getUserKey() != null)
            params.put("X-API-KEY", SessionHelper.getUserKey());*/
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

//            /* Não sei se o que vem é JSONArray ou JSONObject. Encapsula num JSONObject e deixa o destinatário decidir o que fazer */
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("data",jsonString);

            return Response.success(jsonString,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
}
