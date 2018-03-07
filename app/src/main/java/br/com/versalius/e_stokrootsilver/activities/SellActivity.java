package br.com.versalius.e_stokrootsilver.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.versalius.e_stokrootsilver.MainActivity;
import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.adapters.SellAdapter;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.PreferencesHelper;

public class SellActivity extends AppCompatActivity {

    private Sell currentSell;
    private ProgressBar progressBar;
    private SellAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        /*Verifica se existe algum extra*/
        if (getIntent().getExtras() != null) {
            /* Verifica se nesse extra tem venda.. */
            currentSell = (Sell) getIntent().getExtras().getSerializable("sell");
            if (currentSell != null) {
                /* Se tiver, carrega*/
                getSupportActionBar().setTitle("Histórico de venda");
            } else {
                /*Se não tiver uma venda, só pode ter um produto. Sendo assim, é criada uma nova venda ou carregada uma venda existente*/
                loadCurrentSell();
                getSupportActionBar().setTitle("Nova venda");

                Product product = (Product) getIntent().getExtras().getSerializable("product");
                if (product != null) {
                    currentSell.addProduct(product);
                    saveCurrentSell();
                } else {
                    finish();
                }
            }
        } else {
            /* Se não existe extra, o usuário escolheu continuar uma venda pendente*/
            loadCurrentSell();
            getSupportActionBar().setTitle("Venda pendente");
        }

        ((TextView) findViewById(R.id.tvClient)).setText("Cliente: " + currentSell.getClient());
        ((TextView) findViewById(R.id.tvDate)).setText("Data: " + currentSell.getFormattedDate());
        if (currentSell.getProducts().size() > 1) {
            ((TextView) findViewById(R.id.tvQtdItems)).setText(currentSell.getProducts().size() + " itens");
        } else {
            ((TextView) findViewById(R.id.tvQtdItems)).setText(currentSell.getProducts().size() + " item");
        }
        ((TextView) findViewById(R.id.tvTotalPrice)).setText("Total: " + currentSell.getTotalPrice() + "");
        (findViewById(R.id.btScan)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new IntentIntegrator(SellActivity.this).setPrompt("Alinhe o leito com o código de barras do produto.").initiateScan(); // `this` is the current Activity
            }
        });

        (findViewById(R.id.btCheckout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Todo: dados a serem enviados na confirmação da compra:
                 *
                 * CPF do cliente - @Nullable
                 * Data
                 * Array de ids de produtos (o webservice consulta os produtos, soma os preços e obtém o valor final da compra)
                 * Valor total da compra [Opcional]
                 */
            }
        });

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
            currentSell.setClient("Não cadastrado");
        } else {
            currentSell = gson.fromJson(sellJson, new TypeToken<Sell>() {
            }.getType());
        }
    }

    private void setupList(Sell sell) {
        adapter = new SellAdapter(this, sell);
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

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
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
                    currentSell.addProduct(product);
                    if (currentSell.getProducts().size() > 1) {
                        ((TextView) findViewById(R.id.tvQtdItems)).setText(currentSell.getProducts().size() + " itens");
                    } else {
                        ((TextView) findViewById(R.id.tvQtdItems)).setText(currentSell.getProducts().size() + " item");
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        setupList(currentSell);
                    }
                    ((TextView) findViewById(R.id.tvTotalPrice)).setText("Total: " + currentSell.getTotalPrice() + "");
                    saveCurrentSell();
                } catch (JSONException e) {
                    Toast.makeText(SellActivity.this, "Falha ao obter dados do produto", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFail(VolleyError error) {
                Toast.makeText(SellActivity.this, "Falha ao se conectar com o servidor. Tente mais tarde.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
