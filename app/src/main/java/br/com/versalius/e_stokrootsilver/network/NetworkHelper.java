package br.com.versalius.e_stokrootsilver.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

/**
 * Created by Giovanne on 28/06/2016.
 */
public class NetworkHelper {
    private static final String TAG = NetworkHelper.class.getSimpleName();

    private static NetworkHelper instance;
    private static Context context;
    private RequestQueue requestQueue;

//    public static final String DOMINIO = "http://192.168.1.107/controle-de-estoque-qr-web/"; //local
    public static final String DOMINIO = "http://www.rootssilver.com.br/"; //remoto

    private final String API = "api/";
    /*UserService*/
    private final String LOGIN = API + "Login_controller";
    private final String GET_USER = API + "User_controller";
    private final String GET_PRODUCT = API + "Estoque_controller";
    private final String GET_CITY = API + "Cities_controller";
    private final String REGISTER_CLIENT = API + "Cliente_controller/register";
    private final String CHECK_CPF = API + "Cliente_controller/client_exists";
    private final String GET_CLIENT = API + "Cliente_controller/client";
    private final String CHECKOUT = API + "Venda_controller";

    private NetworkHelper(Context context) {
        this.context = context;
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // Pegar o contexto da aplicação garante que a requestQueue vai ser singleton e só
            // morre quando a aplicação parar
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    //Retorna uma instância estática de NetworkHelper
    public static synchronized NetworkHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    public void doLogin(String email, String password, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        execute(Request.Method.POST, params, TAG, DOMINIO + LOGIN, callback);
    }

    public void registerClient(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST, params, TAG, DOMINIO + REGISTER_CLIENT, callback);
    }

    public void checkout(HashMap<String, String> params, ResponseCallback callback) {
        execute(Request.Method.POST, params, TAG, DOMINIO + CHECKOUT, callback);
    }

    public void getUserById(String id, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_USER, params),
                callback);
    }

    public void cpfExists(String cpf, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cpf", cpf);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + CHECK_CPF, params),
                callback);
    }

    public void getClient(String cpf, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("cpf", cpf);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_CLIENT, params),
                callback);
    }

    public void getProductByBarcode(String code, String userId, String typeSellId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("user_id", userId);
        params.put("type_level_id", typeSellId);
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_PRODUCT, params),
                callback);
    }

    public void getCityByStateId(int stateId, ResponseCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("state_id", String.valueOf(stateId));
        execute(Request.Method.GET,
                null,
                TAG,
                buildGetURL(DOMINIO + GET_CITY, params),
                callback);
    }

    private void execute(int method, final HashMap params, String tag, String url, final ResponseCallback callback) {
        final CustomRequest request = new CustomRequest(
                new SessionHelper(context).getUserToken(),
                method,
                url,
                params,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("onResponse - LOG", "response: " + response);
                        if (callback != null) {
                            callback.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("onResponse - LOG", "response: " + error.getMessage());
                        if (callback != null) {
                            callback.onFail(error);
                        }
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(tag);
        getRequestQueue().add(request);

    }

    private String buildGetURL(String url, HashMap<String, String> params) {
        url += "?";
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            url += pair.getKey() + "=" + pair.getValue();
            it.remove(); // avoids a ConcurrentModificationException
            if (it.hasNext()) {
                url += "&";
            }
        }
        return url;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
