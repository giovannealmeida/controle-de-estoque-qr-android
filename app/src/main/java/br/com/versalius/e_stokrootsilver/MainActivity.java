package br.com.versalius.e_stokrootsilver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;

import br.com.versalius.e_stokrootsilver.activities.AccountSettingsActivity;
import br.com.versalius.e_stokrootsilver.activities.LoginActivity;
import br.com.versalius.e_stokrootsilver.activities.NewClientActivity;
import br.com.versalius.e_stokrootsilver.activities.SellActivity;
import br.com.versalius.e_stokrootsilver.activities.SellsListActivity;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.User;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        if(!new SessionHelper(this).isLogged()){
//            startActivity(new Intent(this, LoginActivity.class));
//            finish();
//        }

//        User user = (User) getIntent().getExtras().getSerializable("user");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Vendedor: "+user.getFirstName()+" "+user.getLastName());

        (findViewById(R.id.btScan)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new IntentIntegrator(MainActivity.this).initiateScan(); // `this` is the current Activity
            }
        });

        (findViewById(R.id.btNewUser)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NewClientActivity.class));
            }
        });

        (findViewById(R.id.btSellList)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SellsListActivity.class));
            }
        });
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                getProduct(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getProduct(String code) {
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).getProductByBarcode(code, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                /* A resposta vem como um array de uma posição contendo o único produto.
                TODO: Trocar a resposta por JSONObject.*/
                try {
                    JSONArray jsonArray = new JSONArray(jsonStringResponse);
                    Product product = new Product(jsonArray.getJSONObject(0));
                    startActivity(new Intent(MainActivity.this, SellActivity.class).putExtra("product", product));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFail(VolleyError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
            startActivity(new Intent(this, AccountSettingsActivity.class));
        } else if (id == R.id.action_logout) {
            new SessionHelper(this).logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}