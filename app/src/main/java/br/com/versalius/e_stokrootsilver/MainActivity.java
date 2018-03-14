package br.com.versalius.e_stokrootsilver;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.versalius.e_stokrootsilver.activities.AccountSettingsActivity;
import br.com.versalius.e_stokrootsilver.activities.LoginActivity;
import br.com.versalius.e_stokrootsilver.activities.NewClientActivity;
import br.com.versalius.e_stokrootsilver.activities.SellActivity;
import br.com.versalius.e_stokrootsilver.activities.SellHistoryActivity;
import br.com.versalius.e_stokrootsilver.model.Product;
import br.com.versalius.e_stokrootsilver.model.Sell;
import br.com.versalius.e_stokrootsilver.model.User;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.CustomSnackBar;
import br.com.versalius.e_stokrootsilver.utils.PreferencesHelper;
import br.com.versalius.e_stokrootsilver.utils.SessionHelper;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private View llPendingSellOptions;
    private int typeSaleId = 0; //Determina se a venda é atacado ou varejo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        llPendingSellOptions = findViewById(R.id.llPendingSellOptions);

        if (!new SessionHelper(this).isLogged()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        User user = (User) getIntent().getExtras().getSerializable("user");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vendedor: " + user.getFirstName() + " " + user.getLastName());

        (findViewById(R.id.btScan)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (new SessionHelper(MainActivity.this).getUserTypeSaleId()) {
                    case Sell.TYPE_WHOLESALE_AND_RETAIL:
                        final RadioGroup rgSaleType = new RadioGroup(MainActivity.this);
                        final RadioButton rbRetail = new RadioButton(MainActivity.this);
                        rbRetail.setText("Varejo");
                        final RadioButton rbWholesale = new RadioButton(MainActivity.this);
                        rbWholesale.setText("Atacado");
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(40, 40, 0, 0);
                        rgSaleType.addView(rbRetail, layoutParams);
                        layoutParams.setMargins(40, 0, 0, 0);
                        rgSaleType.addView(rbWholesale, layoutParams);
                        rgSaleType.check(rbRetail.getId());

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Qual o tipo da venda?")
                                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (rgSaleType.getCheckedRadioButtonId() == rbRetail.getId()) {
                                            typeSaleId = 2;
                                        } else if (rgSaleType.getCheckedRadioButtonId() == rbWholesale.getId()) {
                                            typeSaleId = 1;
                                        }
                                        new IntentIntegrator(MainActivity.this).setPrompt("Alinhe o leitor com o código de barras do produto.").initiateScan(); // `this` is the current Activity
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .setCancelable(false)
                                .setView(rgSaleType)
                                .show();
                        break;
                    case Sell.TYPE_RETAIL:
                        typeSaleId = 2;
                        break;
                    case Sell.TYPE_WHOLESALE:
                        typeSaleId = 1;
                        break;
                }

                if(typeSaleId != 0){
                    new IntentIntegrator(MainActivity.this).setPrompt("Alinhe o leitor com o código de barras do produto.").initiateScan(); // `this` is the current Activity
                }

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
                startActivity(new Intent(MainActivity.this, SellHistoryActivity.class));
            }
        });

        (findViewById(R.id.btCancelSell)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CancelSellDialog dialog = new CancelSellDialog();
                dialog.show(MainActivity.this.getSupportFragmentManager(), "dialog");
            }
        });

        (findViewById(R.id.btContinueSell)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SellActivity.class));
            }
        });
    }

    private boolean existsPendingSell() {
        return !PreferencesHelper.getInstance(this).load(PreferencesHelper.CURRENT_SELL_LIST+"_"+new SessionHelper(this).getUserId()).isEmpty();
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
        String userId = new SessionHelper(this).getUserId().toString();
        NetworkHelper.getInstance(this).getProductByBarcode(code, userId != null ? userId : "", String.valueOf(typeSaleId), new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                    if (jsonObject.getBoolean("status")) {
                        Product product = new Product(jsonObject.getJSONObject("data"));
                        startActivity(new Intent(MainActivity.this, SellActivity.class).putExtra("product", product).putExtra("type_sale_id", typeSaleId));
                        typeSaleId = 0;
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

    @Override
    protected void onResume() {
        if (existsPendingSell()) {
            llPendingSellOptions.setVisibility(View.VISIBLE);
            (findViewById(R.id.btScan)).setVisibility(View.GONE);
        } else {
            llPendingSellOptions.setVisibility(View.GONE);
            (findViewById(R.id.btScan)).setVisibility(View.VISIBLE);
        }
        super.onResume();
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

    public static class CancelSellDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_default, null);
            ((TextView) view.findViewById(R.id.tvMessage)).setText("Os dados da venda serão perdidos.");

            return new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setCancelable(true)
                    .setTitle("Tem certeza?")
                    .setPositiveButton(getString(R.string.dialog_action_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    PreferencesHelper.getInstance(getActivity()).remove(PreferencesHelper.CURRENT_SELL_LIST+"_"+new SessionHelper(getActivity()).getUserId());
                                    ((MainActivity) getActivity()).onResume();
//                                            .findViewById(R.id.llPendingSellOptions).setVisibility(View.GONE);
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.dialog_action_no), null)
                    .create();
        }
    }
}