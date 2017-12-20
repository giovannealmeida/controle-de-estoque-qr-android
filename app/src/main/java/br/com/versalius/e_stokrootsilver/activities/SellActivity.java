package br.com.versalius.e_stokrootsilver.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.adapters.SellAdapter;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;
import br.com.versalius.e_stokrootsilver.utils.PreferencesHelper;

public class SellActivity extends AppCompatActivity {

    private Sell currentSell;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        currentSell = (Sell) getIntent().getExtras().getSerializable("sell");
        if (currentSell != null) {
            getSupportActionBar().setTitle("Histórico de venda");
            ((TextView) findViewById(R.id.tvClient)).setText("Cliente: " + currentSell.getClient());
            ((TextView) findViewById(R.id.tvDate)).setText("Data: " + currentSell.getFormattedDate());
        } else {
            /* Todo: verificar se tem produto nos extras. Se tiver, adicionar à lista da venda atual (se houver alguma, caso contrário, cria-se)*/
            Product product = (Product) getIntent().getExtras().getSerializable("product");
            if (product != null) {
                loadCurrentSell();
                currentSell.addProduct(product);
                saveCurrentSell();
                ((TextView) findViewById(R.id.tvDate)).setText("Data: " + currentSell.getFormattedDate());
                getSupportActionBar().setTitle("Nova venda");
            } else {
                finish();
            }
        }

        ((TextView) findViewById(R.id.tvTotalPrice)).setText("Total: " + currentSell.getTotalPrice() + "");

        setupList(currentSell);
    }

    private void saveCurrentSell() {
        try {
            Gson gson = new Gson();
            String sellJson = gson.toJson(currentSell, new TypeToken<Sell>() {
            }.getType());
            PreferencesHelper.getInstance(this).save(PreferencesHelper.CURRENT_SELL_LIST, sellJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCurrentSell() {
        Gson gson = new Gson();
        String sellJson = PreferencesHelper.getInstance(this).load(PreferencesHelper.CURRENT_SELL_LIST);
        if (sellJson.isEmpty()) {
            currentSell = new Sell();
            currentSell.setDate(Calendar.getInstance());
        } else {
            currentSell = gson.fromJson(sellJson, new TypeToken<Sell>() {
            }.getType());
        }
    }

    private void setupList(Sell sell) {
        SellAdapter adapter = new SellAdapter(this, sell);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
