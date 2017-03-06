package br.com.versalius.iking.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.com.versalius.iking.R;
import br.com.versalius.iking.network.NetworkHelper;
import br.com.versalius.iking.network.ResponseCallback;
import br.com.versalius.iking.utils.CustomSnackBar;
import br.com.versalius.iking.utils.ProgressDialogHelper;


public class ForgotPasswordActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private static final int ACTION_RESULT_GET_IMAGE = 1000;
    private static final int REQUEST_PERMISSION_CODE = 1001;

    private TextInputLayout tilEmail;
    private EditText etEmail;
    private CoordinatorLayout coordinatorLayout;
    private HashMap<String, String> formData;
    private MaterialDialog mMaterialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        getSupportActionBar().setLogo(R.drawable.toolbar_logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_forgot_password);
        setUpViews();
    }

    private void setUpViews() {

        /* Instanciando layouts */
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);

        /* Instanciando campos */
        etEmail = (EditText) findViewById(R.id.etEmail);


        /* Adicionando FocusListener*/
        etEmail.setOnFocusChangeListener(this);

        Button btSend = (Button) findViewById(R.id.btSend);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(ForgotPasswordActivity.this);

                if (NetworkHelper.isOnline(ForgotPasswordActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner("Aguarde", "Enviando email para redefinição se senha", true, false);
                        NetworkHelper.getInstance(ForgotPasswordActivity.this).forgotPassword(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        setResult(RESULT_OK,null);
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Falha ao enviar email de redefinição de senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao enviar email de redefinição de senha", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
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

    private boolean isValidForm() {
        boolean isFocusRequested = false;

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                tilEmail.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("email", etEmail.getText().toString());
        }


        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getResources().getString(R.string.err_msg_empty_email));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
            return false;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbEmailCheck);
        findViewById(R.id.ivEmailCheck).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).emailExists(email, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O email existe */
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(ForgotPasswordActivity.this, R.drawable.ic_check));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                    } else {
                        tilEmail.setError(getResources().getString(R.string.err_msg_no_existing_email));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(ForgotPasswordActivity.this, R.drawable.ic_close_circle));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                tilEmail.setError(getResources().getString(R.string.err_msg_server_fail));
                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
            }
        });

        tilEmail.setErrorEnabled(false);
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
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (v.getId()) {
                case R.id.etEmail:
                    hasValidEmail();
                    break;
            }
        }
    }

    private void callDialog(String message, final String[] permissions) {
        mMaterialDialog = new MaterialDialog.Builder(this)
                .title(R.string.title_dialog_permission)
                .content(message)
                .positiveText(R.string.dialog_permission_agree_button)
                .negativeText(R.string.dialog_permission_disagree_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ActivityCompat.requestPermissions(ForgotPasswordActivity.this, permissions, REQUEST_PERMISSION_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mMaterialDialog.dismiss();
                    }
                })
                .show();
    }
}
