package br.com.versalius.e_stokrootsilver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import br.com.versalius.e_stokrootsilver.activities.AccountSettingsActivity;
import br.com.versalius.e_stokrootsilver.activities.LoginActivity;
import br.com.versalius.e_stokrootsilver.activities.NewClientActivity;
import br.com.versalius.e_stokrootsilver.activities.SellsListActivity;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!new SessionHelper(this).isLogged()){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vendedor: John Doe");

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
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
//            showCloseDialog();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}