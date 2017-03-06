package br.com.versalius.iking.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.com.versalius.iking.MainActivity;
import br.com.versalius.iking.R;
import br.com.versalius.iking.model.User;
import br.com.versalius.iking.network.NetworkHelper;
import br.com.versalius.iking.network.ResponseCallback;
import br.com.versalius.iking.utils.CustomSnackBar;
import br.com.versalius.iking.utils.ProgressDialogHelper;
import br.com.versalius.iking.utils.SessionHelper;


public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText etEmail;
    private EditText etPassword;
    private CoordinatorLayout coordinatorLayout;
    private HashMap<String, String> formData;
    private final int SIGNUP_CODE = 1;
    private final int FORGOT_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        setUpViews();
    }

    private void setUpViews() {
        Button singup = (Button) findViewById(R.id.btSingup);
        Button forgot = (Button) findViewById(R.id.btForgot);
        Button login = (Button) findViewById(R.id.btLogin);

        /* Instanciando campos */
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        /* Adicionando FocusListener*/
        etEmail.setOnFocusChangeListener(this);
        etPassword.setOnFocusChangeListener(this);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialogHelper progressHelper = new ProgressDialogHelper(LoginActivity.this);

                if (NetworkHelper.isOnline(LoginActivity.this)) {
                    if (isValidForm()) {
                        progressHelper.createProgressSpinner("Aguarde", "Entrando.", true, false);
                        NetworkHelper.getInstance(LoginActivity.this).login(formData, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                try {
                                    progressHelper.dismiss();
                                    JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                    if (jsonObject.getBoolean("status")) {
                                        User user = new User(jsonObject.getJSONObject("data"));
                                        SessionHelper.getInstance(LoginActivity.this.getApplicationContext()).saveUser(user);
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        CustomSnackBar.make(coordinatorLayout, "Email e/ou senha incorreto(s)", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                progressHelper.dismiss();
                                CustomSnackBar.make(coordinatorLayout, "Falha ao entrar", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            }
                        });
                    } else
                        CustomSnackBar.make(coordinatorLayout, "Atenção! Preencha o formulário corretamente.", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.INFO).show();
                } else {
                    CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                }

            }
        });

        singup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, SingupActivity.class), SIGNUP_CODE);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, ForgotPasswordActivity.class), FORGOT_CODE);
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {

        boolean isFocusRequested = false;

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                etEmail.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("email", etEmail.getText().toString());
        }

        /* Verifica o campo de senha*/
        if (!hasValidPassword()) {
            if (!isFocusRequested) {
                etPassword.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("password", etPassword.getText().toString());
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getResources().getString(R.string.err_msg_empty_email));
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getResources().getString(R.string.err_msg_invalid_email));
            return false;
        }

        return true;
    }

    private boolean hasValidPassword() {
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password) || (password.length() < 6) || (password.length() > 22)) {
            etPassword.setError(getResources().getString(R.string.err_msg_short_password));
            return false;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) { /* Verifica somente quando o foco é perdido */
            switch (v.getId()) {
                case R.id.etEmail:
                    hasValidEmail();
                    break;
                case R.id.etPassword:
                    hasValidPassword();
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SIGNUP_CODE:
                if (resultCode == SingupActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Cadastro realizado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
            case FORGOT_CODE:
                if (resultCode == ForgotPasswordActivity.RESULT_OK)
                    CustomSnackBar.make(coordinatorLayout, "Email de redefinição de senha enviado com sucesso", Snackbar.LENGTH_SHORT, CustomSnackBar.SnackBarType.SUCCESS).show();
                break;
        }
    }
}
