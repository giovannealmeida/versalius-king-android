package br.com.versalius.iking.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.com.versalius.iking.R;
import br.com.versalius.iking.network.NetworkHelper;
import br.com.versalius.iking.network.ResponseCallback;
import br.com.versalius.iking.utils.CustomSnackBar;
import br.com.versalius.iking.utils.ProgressDialogHelper;


public class AlterPasswordActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private HashMap<String, String> formData;
    private CoordinatorLayout coordinatorLayout;

    private TextInputLayout tilOldPassword;
    private TextInputLayout tilPassword;
    private TextInputLayout tilPasswordAgain;

    private EditText etOldPassword;
    private EditText etPassword;
    private EditText etPasswordAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alter_password);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        getSupportActionBar().setLogo(R.drawable.toolbar_logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_alter_password);
        setUpViews();
    }

    private void setUpViews() {

        tilOldPassword = (TextInputLayout) findViewById(R.id.tilOldPassword);
        tilPassword = (TextInputLayout) findViewById(R.id.tilPassword);
        tilPasswordAgain = (TextInputLayout) findViewById(R.id.tilPasswordAgain);

        etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordAgain = (EditText) findViewById(R.id.etPasswordAgain);

        etOldPassword.setOnFocusChangeListener(this);
        etPassword.setOnFocusChangeListener(this);
        etPasswordAgain.setOnFocusChangeListener(this);

        Button btAlter = (Button) findViewById(R.id.btAlter);
        btAlter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(AlterPasswordActivity.this);

                if (NetworkHelper.isOnline(AlterPasswordActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner("Aguarde", "Alterando senha.", true, false);
                        NetworkHelper.getInstance(AlterPasswordActivity.this).userAlterPassword(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        setResult(RESULT_OK, null);
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao alterar senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao alterar senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    } else
                        CustomSnackBar.make(coordinatorLayout, "Atenção! Preencha o formulário corretamente.", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.INFO).show();

                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {

        boolean isFocusRequested = false;
        /* Verifica o campo de senha*/
        if (!hasValidOldPassword()) {
            if (!isFocusRequested) {
                tilOldPassword.requestFocus();
                isFocusRequested = true;
            }
        }


        /* Verifica o campo de senha*/
        if (!hasValidPassword()) {
            if (!isFocusRequested) {
                tilPassword.requestFocus();
                isFocusRequested = true;
            }
        }

        /* Verifica o campo de senha repetida*/
        if (!hasValidRepeatedPassword()) {
            if (!isFocusRequested) {
                tilPasswordAgain.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("password", etPassword.getText().toString());
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidOldPassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        if (TextUtils.isEmpty(oldPassword)) {
            tilOldPassword.setError(getResources().getString(R.string.err_msg_empty_old_password));
            return false;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbOldPasswordCheck);
        findViewById(R.id.ivOldPasswordCheck).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).validOldPassword(oldPassword, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                findViewById(R.id.ivOldPasswordCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O email existe */
                        ((ImageView) findViewById(R.id.ivOldPasswordCheck)).setImageDrawable(ContextCompat.getDrawable(AlterPasswordActivity.this, R.drawable.ic_check));
                        ((ImageView) findViewById(R.id.ivOldPasswordCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                    } else {
                        tilOldPassword.setError(getResources().getString(R.string.err_msg_password_invalid));
                        ((ImageView) findViewById(R.id.ivOldPasswordCheck)).setImageDrawable(ContextCompat.getDrawable(AlterPasswordActivity.this, R.drawable.ic_close_circle));
                        ((ImageView) findViewById(R.id.ivOldPasswordCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                tilOldPassword.setError(getResources().getString(R.string.err_msg_server_fail));
                findViewById(R.id.ivOldPasswordCheck).setVisibility(View.VISIBLE);
            }
        });

        tilOldPassword.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidPassword() {
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || (password.length() < 6) || (password.length() > 22)) {
            tilPassword.setError(getResources().getString(R.string.err_msg_short_password));
            return false;
        }
        if (!TextUtils.isEmpty(etPasswordAgain.getText().toString().trim())) {
            hasValidRepeatedPassword();
        }
        tilPassword.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidRepeatedPassword() {
        String passwordAgain = etPasswordAgain.getText().toString().trim();
        if (!etPassword.getText().toString().trim().equals(passwordAgain)) {
            tilPasswordAgain.setError(getResources().getString(R.string.err_msg_dont_match_password));
            return false;
        }
        tilPasswordAgain.setErrorEnabled(false);
        return true;
    }

    /**
     * NÃO REMOVER DE NOVO!!!!
     * Basicamente seta a ação de fechar a activity ao selecionar a seta na toolbar
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (view.getId()) {
                case R.id.etOldPassword:
                    hasValidOldPassword();
                    break;
                case R.id.etPassword:
                    hasValidPassword();
                    break;
                case R.id.etPasswordAgain:
                    hasValidRepeatedPassword();
                    break;
            }
        }
    }
}
