package br.com.versalius.e_stokrootsilver.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.network.NetworkHelper;
import br.com.versalius.e_stokrootsilver.network.ResponseCallback;
import br.com.versalius.e_stokrootsilver.utils.CustomSnackBar;

public class NewClientActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private Spinner spCity;
    private TextInputEditText etName, etCPF, etEmail, etPhone;
    private RadioGroup rbGender;
    private SparseArray<String> citiesMap;
    private ArrayList<String> citiesList;
    private ProgressBar progressBar;
    private HashMap<String, String> newClientData;
    private boolean isCPFOk = false;
    private boolean isReadyForSignUp;
    private boolean isCheckingCPF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_client);
        getSupportActionBar().setTitle("Novo cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        rbGender = (RadioGroup) findViewById(R.id.rgGender);

        etName = (TextInputEditText) findViewById(R.id.etName);
        etName.setOnFocusChangeListener(this);

        etCPF = (TextInputEditText) findViewById(R.id.etCpf);
        etCPF.setOnFocusChangeListener(this);

        if(getIntent().getExtras() != null){
            etCPF.setText(getIntent().getExtras().getString("cpf"));
        }


        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etEmail.setOnFocusChangeListener(this);
        etPhone = (TextInputEditText) findViewById(R.id.etPhone);

        spCity = (Spinner) findViewById(R.id.spCity);
        spCity.setEnabled(false);
        citiesList = new ArrayList<>();
        final CustomSpinnerAdapter citySpinnerAdapter = new CustomSpinnerAdapter(this, citiesList);
        spCity.setAdapter(citySpinnerAdapter);

        List<String> stateList = Arrays.asList(getResources().getStringArray(R.array.array_states));
        final Spinner spState = (Spinner) findViewById(R.id.spState);
        CustomSpinnerAdapter stateSpinnerAdapter = new CustomSpinnerAdapter(this, stateList);
        spState.setAdapter(stateSpinnerAdapter);
        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    spCity.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    NetworkHelper.getInstance(NewClientActivity.this).getCityByStateId(position, new ResponseCallback() {
                        @Override
                        public void onSuccess(String jsonStringResponse) {
                            try {
                                JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                citiesMap = new SparseArray<>();
                                citiesList.clear();
                                Iterator<String> keys = jsonObject.keys();
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    citiesMap.put(Integer.parseInt(key), jsonObject.getString(key));
                                    citiesList.add(jsonObject.getString(key));
                                }

                                ((CustomSpinnerAdapter) spCity.getAdapter()).notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);
                                spCity.setEnabled(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(NewClientActivity.this, "Falha ao obter lista de cidades", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            Toast.makeText(NewClientActivity.this, "Falha ao obter lista de cidades", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            spState.setSelection(0);
                        }
                    });
//                    String item = parent.getItemAtPosition(position).toString();
//                    Toast.makeText(parent.getContext(), "Item n: " + position + "\nName: " + item, Toast.LENGTH_LONG).show();
                } else {
                    citiesList.clear();
                    citySpinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.btSingup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReadyForSignUp = true;
                progressBar.setVisibility(View.VISIBLE);
                if (isFormValid()) {
                    newClientData = new HashMap<>();
                    newClientData.put("name", etName.getText().toString());
                    newClientData.put("email", etEmail.getText().toString());
                    rbGender.getCheckedRadioButtonId();
                    newClientData.put("gender_id", rbGender.findViewById(rbGender.getCheckedRadioButtonId()).getTag().toString());
                    newClientData.put("phone", etPhone.getText().toString());
                    if (citiesMap != null && citiesMap.size() > 0) {
                        newClientData.put("city_id", String.valueOf(citiesMap.keyAt(spCity.getSelectedItemPosition())));
                    }
                    newClientData.put("cpf", etCPF.getText().toString());

                    /* Se estiver chegando CPF, espera a verificação. Assim que o CPF for validado o cadastro continua*/
                    if(isCheckingCPF){
                        return;
                    }

                    NetworkHelper.getInstance(NewClientActivity.this).registerClient(newClientData, new ResponseCallback() {
                        @Override
                        public void onSuccess(String jsonStringResponse) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                final JSONObject jsonObject = new JSONObject(jsonStringResponse);

                                if (!jsonObject.getBoolean("status")) {
                                    CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao cadastrar cliente", Snackbar.LENGTH_LONG, CustomSnackBar.ERROR).show();
                                } else {
                                    Snackbar snackbar = CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Cliente cadastrado com sucesso", Snackbar.LENGTH_LONG, CustomSnackBar.SUCCESS);
                                    snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            super.onDismissed(transientBottomBar, event);

                                            try {
                                                setResult(RESULT_OK, new Intent()
                                                        .putExtra("client_id", jsonObject.getString("client_id"))
                                                        .putExtra("name", jsonObject.getString("name")));
                                                finish();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                                    snackbar.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            progressBar.setVisibility(View.GONE);
                            CustomSnackBar.make((RelativeLayout) findViewById(R.id.parentView), "Falha ao cadastrar cliente", Snackbar.LENGTH_LONG, CustomSnackBar.ERROR).show();
                        }
                    });
                }
            }
        });
    }

    private boolean isFormValid() {
        View focusRequested = null;

        etName.setError(null);
        etCPF.setError(null);
        etEmail.setError(null);

        if (etName.getText().toString().isEmpty()) {
            etName.setError("Insira um nome válido");
            focusRequested = etName;
        }

        if (!isCPFOk) {
            if (etCPF.getText().toString().isEmpty()) {
                etCPF.setError("Insira um CPF válido");
                focusRequested = etCPF;
            } else {
                isCheckingCPF = true;
                checkCPF();
            }
        }

        String email = etEmail.getText().toString().trim();
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("E-mail inválido");
            focusRequested = etEmail;
        }

        if (focusRequested != null) {
            focusRequested.requestFocus();
            return false;
        } else {
            return true;
        }

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


    private boolean checkCPF() {
        String cpf = etCPF.getText().toString().trim();
        if (cpf.isEmpty() || cpf.length() < 11 || !calcDigVerif(cpf.substring(0, 9)).equals(cpf.substring(9, 11))) {
            etCPF.setError("Insira um CPF válido");
            ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(NewClientActivity.this, R.drawable.ic_check_circle));
            return false;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbCpfCheck);
        findViewById(R.id.ivCpfCheck).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).cpfExists(cpf, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                isCheckingCPF = false;
                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O CPF existe */
                        isCPFOk = false;
                        etCPF.setError("Este CPF já está cadastrado");
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(NewClientActivity.this, R.drawable.ic_close_circle));
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                    } else {
                        isCPFOk = true;
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(NewClientActivity.this, R.drawable.ic_check_circle));
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setColorFilter(Color.argb(255, 0, 192, 96));

                        if(isReadyForSignUp){
                            findViewById(R.id.btSingup).performClick();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                etCPF.setError("Falha ao se conectar com o servidor");
                isCPFOk = false;
                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });

        etCPF.setError(null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (view.getId()) {
                case R.id.etName:
                    if (etName.getText().toString().isEmpty()) {
                        etName.setError("Insira um nome válido");
                    }
                    break;
                case R.id.etEmail:
                    if (!etEmail.getText().toString().isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
                        etEmail.setError("E-mail inválido");
                    }
                    break;
                case R.id.etCpf:
                    isCPFOk = false;
                    checkCPF();
                    break;
            }
        }
    }

    public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private List<String> asr;

        public CustomSpinnerAdapter(Context context, List<String> asr) {
            this.asr = asr;
        }

        public int getCount() {
            return asr.size();
        }

        public Object getItem(int i) {
            return asr.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(NewClientActivity.this);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(18);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(asr.get(position));
            txt.setTextColor(Color.parseColor("#000000"));
            return txt;
        }

        public View getView(int i, View view, ViewGroup viewgroup) {
            TextView txt = new TextView(NewClientActivity.this);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(16);
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
            txt.setText(asr.get(i));
            txt.setTextColor(Color.parseColor("#000000"));
            return txt;
        }
    }
}
