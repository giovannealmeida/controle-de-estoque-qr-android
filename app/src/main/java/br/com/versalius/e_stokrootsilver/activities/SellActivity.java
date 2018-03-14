package br.com.versalius.e_stokrootsilver.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.adapters.SellAdapter;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.CustomSnackBar;
import br.com.versalius.e_stokrootsilver.utils.PreferencesHelper;
import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

public class SellActivity extends AppCompatActivity {

    private final int NEW_CLIENT_REQUEST_CODE = 1001;
    private Sell currentSell;
    private ProgressBar progressBar;
    private SellAdapter adapter;
    private String clientId = "";

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
                findViewById(R.id.btScan).setVisibility(View.GONE);
                findViewById(R.id.btCheckout).setVisibility(View.GONE);
            } else {
                /*Se não tiver uma venda, só pode ter um produto. Sendo assim, é criada uma nova venda ou carregada uma venda existente*/
                loadCurrentSell();
                getSupportActionBar().setTitle("Nova venda");

                Product product = (Product) getIntent().getExtras().getSerializable("product");
                currentSell.setSaleType(getIntent().getExtras().getInt("type_sale_id"));
                ;
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

        ((TextView) findViewById(R.id.tvTypeSale)).setText("Tipo: " + (currentSell.getSaleType() == Sell.TYPE_RETAIL ? "Varejo" : "Atacado"));
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

        (findViewById(R.id.tvAddClient)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddClientDialog();
            }
        });

        (findViewById(R.id.btCheckout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final HashMap<String, String> data = new HashMap<>();
                data.put("type_sale_id", String.valueOf(currentSell.getSaleType()));
                data.put("client_id", clientId);
                data.put("user_id", new SessionHelper(SellActivity.this).getUserId().toString());
                Collections.sort(currentSell.getProducts(), new Comparator<Product>() {
                    @Override
                    public int compare(Product p1, Product p2) {
                        return p1.getId().compareTo(p2.getId());
                    }
                });

                List<ProductItem> listProductItems = new ArrayList<>();
                ProductItem productItem = new ProductItem();
                productItem.product_id = currentSell.getProducts().get(0).getId();
                productItem.amount = 0;
                productItem.value = currentSell.getProducts().get(0).getValue();

                listProductItems.add(productItem);

                for (Product p : currentSell.getProducts()) {
                    if (p.getId().equals(productItem.product_id)) {
                        productItem.amount++;
                    } else {
                        productItem = new ProductItem();
                        productItem.product_id = p.getId();
                        productItem.amount = 1;
                        productItem.value = p.getValue();
                        listProductItems.add(productItem);
                    }
                }

                data.put("products", new Gson().toJson(listProductItems));
                if (clientId.isEmpty()) {
                    new AlertDialog.Builder(SellActivity.this)
                            .setTitle("Nenhum cliente adicionado")
                            .setMessage("Deseja adicionar um cliente à compra?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    openAddClientDialog();
                                }
                            })
                            .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doCheckout(data);
                                }
                            })
                            .show();
                } else {
                    doCheckout(data);
                }
            }
        });

        setupList(currentSell);
    }

    private void doCheckout(HashMap<String, String> data) {
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(SellActivity.this).checkout(data, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                progressBar.setVisibility(View.GONE);
                CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Compra realizada com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SUCCESS)
                        .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                PreferencesHelper.getInstance(SellActivity.this).remove(PreferencesHelper.CURRENT_SELL_LIST + "_" + new SessionHelper(SellActivity.this).getUserId());
                                finish();
                            }
                        })
                        .show();
            }

            @Override
            public void onFail(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao fechar venda", Snackbar.LENGTH_LONG, CustomSnackBar.ERROR).show();
            }
        });
    }

    private void openAddClientDialog() {
        /* Campo de CPF */
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(11);
        editText.setFilters(fArray);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(100, 0, 100, 0);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        editText.setLayoutParams(lp);

        /* Progress bar */
        final ProgressBar pb = new ProgressBar(this);
        pb.setVisibility(View.GONE);
        pb.setLayoutParams(lp);

        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        relativeLayout.setLayoutParams(rlParams);
        relativeLayout.addView(editText);
        relativeLayout.addView(pb);

        final AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Informe o CPF do cliente")
                .setPositiveButton("Adicionar", null)
                .setNeutralButton("Cancelar", null)
                .create();

        alert.setView(relativeLayout);

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pb.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.GONE);

                        final String cpf = editText.getText().toString().trim();
                        if (cpf.isEmpty() || cpf.length() < 11 || !calcDigVerif(cpf.substring(0, 9)).equals(cpf.substring(9, 11))) {
                            editText.setError("Insira um CPF válido");
                            return;
                        }

                        NetworkHelper.getInstance(SellActivity.this).getClient(cpf, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                pb.setVisibility(View.GONE);
                                editText.setVisibility(View.VISIBLE);
                                if (jsonStringResponse.equals("false")) {
                                    new AlertDialog.Builder(SellActivity.this)
                                            .setTitle("Cliente não cadastado")
                                            .setMessage("Deseja cadastrá-lo agora?")
                                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    alert.dismiss();
                                                    startActivityForResult(new Intent(SellActivity.this, NewClientActivity.class).putExtra("cpf", cpf), NEW_CLIENT_REQUEST_CODE);
                                                }
                                            })
                                            .setNegativeButton("Não", null)
                                            .show();
                                } else {
                                    try {
                                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                        ((TextView) findViewById(R.id.tvClient)).setText("Cliente: " + jsonObject.getString("name"));
                                        clientId = jsonObject.getString("id");
                                        alert.dismiss();
                                        CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), jsonObject.getString("name").split(" ")[0] + " adicionado", Snackbar.LENGTH_SHORT, CustomSnackBar.SUCCESS).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                pb.setVisibility(View.GONE);
                                editText.setVisibility(View.VISIBLE);
                                alert.dismiss();
                                CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao adicionar cliente", Snackbar.LENGTH_LONG, CustomSnackBar.ERROR).show();
                            }
                        });
                    }
                });
            }
        });

        alert.show();
    }

    /**
     * Calcula o dígito verificador do CPF
     *
     * @param prefix - Prefixo do CPF (999.999.999)
     * @return
     */
    private String calcDigVerif(String prefix) {
        prefix = prefix.replace(".", "");
        Integer primDig, segDig;
        int soma = 0, peso = 10;
        for (int i = 0; i < prefix.length(); i++)
            soma += Integer.parseInt(prefix.substring(i, i + 1)) * peso--;

        if (soma % 11 == 0 | soma % 11 == 1)
            primDig = 0;
        else
            primDig = 11 - (soma % 11);

        soma = 0;
        peso = 11;
        for (int i = 0; i < prefix.length(); i++)
            soma += Integer.parseInt(prefix.substring(i, i + 1)) * peso--;

        soma += primDig * 2;
        if (soma % 11 == 0 | soma % 11 == 1)
            segDig = 0;
        else
            segDig = 11 - (soma % 11);

        return primDig.toString() + segDig.toString();
    }

    private void saveCurrentSell() {
        try {
            Gson gson = new Gson();
            String sellJson = gson.toJson(currentSell, new TypeToken<Sell>() {
            }.getType());
            PreferencesHelper.getInstance(this).save(PreferencesHelper.CURRENT_SELL_LIST + "_" + new SessionHelper(this).getUserId(), sellJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCurrentSell() {
        Gson gson = new Gson();
        String sellJson = PreferencesHelper.getInstance(this).load(PreferencesHelper.CURRENT_SELL_LIST + "_" + new SessionHelper(this).getUserId());
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    getProduct(result.getContents());
                }
            }
        } else if (requestCode == NEW_CLIENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    clientId = extras.getString("client_id");
                    ((TextView) findViewById(R.id.tvClient)).setText("Cliente: " + extras.getString("name"));
                    CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), extras.getString("name").split(" ")[0] + " adicionado", Snackbar.LENGTH_SHORT, CustomSnackBar.SUCCESS).show();
                }
            }
        }
    }

    private void getProduct(String code) {
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).getProductByBarcode(code, new SessionHelper(this).getUserId().toString(), String.valueOf(currentSell.getSaleType()), new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);

                    if (jsonObject.getBoolean("status")) {

                        Product product = new Product(jsonObject.getJSONObject("data"));
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
                    } else {
                        CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), jsonObject.getString("message"), Snackbar.LENGTH_SHORT, CustomSnackBar.ERROR).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao obter dados do produto", Snackbar.LENGTH_SHORT, CustomSnackBar.ERROR).show();
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFail(VolleyError error) {
                CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao se conectar com o servidor", Snackbar.LENGTH_SHORT, CustomSnackBar.ERROR).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private class ProductItem {
        public int product_id;
        public int amount;
        public double value;
    }
}
