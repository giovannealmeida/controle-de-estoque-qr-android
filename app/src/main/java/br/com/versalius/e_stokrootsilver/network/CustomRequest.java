package br.com.versalius.e_stokrootsilver.network;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Giovanne on 22/05/2016.
 */
public class CustomRequest extends Request<String> {

    private Listener<String> listener;
    private Map<String, String> params;
    private String APIKEY;

    public CustomRequest(String APIKEY, int method, String url, Map<String, String> params,
                         Listener<String> reponseListener, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.APIKEY = APIKEY;
        this.listener = reponseListener;
        this.params = params;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
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
//        String creds = String.format("%s:%s","admin","1234");
//        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
//        params.put("Authorization", auth);
        if(APIKEY != null) {
            params.put("X-API-KEY", APIKEY);
        }
        return params;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

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