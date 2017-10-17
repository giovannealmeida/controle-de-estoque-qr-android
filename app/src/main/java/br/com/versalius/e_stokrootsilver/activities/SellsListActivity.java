package br.com.versalius.e_stokrootsilver.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.adapters.SellsListAdapter;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;

public class SellsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sells_list);
        getSupportActionBar().setTitle("Lista de vendas");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((SwipeRefreshLayout) findViewById(R.id.swiperefresh)).setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Toast.makeText(SellsListActivity.this,"Atualizando lista",Toast.LENGTH_SHORT).show();
                    }
                }
        );

        setupList();
    }

    private void setupList() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1,"Sapato A","0.500 kg",99.9));
        products.add(new Product(1,"Sapato B","0.250 kg",74.9));
        products.add(new Product(1,"Sapato C","0.330 kg",199.9));
        products.add(new Product(1,"Boné","0.500 kg",59.9));

        List<Sell> sells = new ArrayList<>();
        sells.add(new Sell(1,products,"Cliente 1","10/10/2017"));
        sells.add(new Sell(1,products,"Cliente 2","12/10/2017"));
        sells.add(new Sell(1,products,"Cliente 3","13/10/2017"));
        sells.add(new Sell(1,products,"Cliente 4","14/10/2017"));
        sells.add(new Sell(1,products,"Cliente 5","16/10/2017"));

        SellsListAdapter adapter = new SellsListAdapter(this,sells);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
