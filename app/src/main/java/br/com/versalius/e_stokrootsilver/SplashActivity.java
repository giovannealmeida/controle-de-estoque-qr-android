package br.com.versalius.e_stokrootsilver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.e_stokrootsilver.activities.LoginActivity;
import br.com.versalius.e_stokrootsilver.model.User;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        final SessionHelper session = new SessionHelper(this);
        if(session.isLogged()){
            NetworkHelper.getInstance(this).getUserById(session.getUserId().toString(), new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if(jsonObject.getBoolean("status")){
                            User user = new User(jsonObject.getJSONObject("data"));
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("user",user);

                            //Atualiza usuário
                            new SessionHelper(SplashActivity.this).updateUser(user);

                            startActivity(new Intent(SplashActivity.this, MainActivity.class).putExtras(bundle));
                        } else {
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            intent.putExtra("message",jsonObject.getString("message"));
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finish();
                }

                @Override
                public void onFail(VolleyError error) {
                    //Força logout
                    session.logout();
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    if(NetworkHelper.isOnline(getApplicationContext())) {
                        intent.putExtra("message", "Não foi possível realizar login. Tente novamente mais tarde.");
                    } else {
                        intent.putExtra("message", "Você está offline!");
                    }
                    startActivity(intent);
                    finish();
                }
            });


        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
