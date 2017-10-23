package br.com.versalius.e_stokrootsilver.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.adapters.ProductAdapter;
import br.com.versalius.e_stokrootsilver.adapters.SellsListAdapter;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;

public class SellActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        Sell sell = (Sell) getIntent().getExtras().getSerializable("sell");

        ((TextView) findViewById(R.id.tvClient)).setText("Cliente: "+sell.getClient());
        ((TextView) findViewById(R.id.tvDate)).setText("Data: "+sell.getDate());
        ((TextView) findViewById(R.id.tvTotalPrice)).setText("Total: "+sell.getTotalPrice() + "");

        getSupportActionBar().setTitle("Venda - " + sell.getDate());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupList(sell);
    }

    private void setupList(Sell sell) {
        ProductAdapter adapter = new ProductAdapter(this, sell);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
