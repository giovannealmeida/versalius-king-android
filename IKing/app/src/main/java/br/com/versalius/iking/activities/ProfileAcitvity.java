package br.com.versalius.iking.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.versalius.iking.R;
import br.com.versalius.iking.model.User;
import br.com.versalius.iking.network.NetworkHelper;
import br.com.versalius.iking.network.ResponseCallback;
import br.com.versalius.iking.utils.CustomSnackBar;
import br.com.versalius.iking.utils.ProgressDialogHelper;
import br.com.versalius.iking.utils.SessionHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileAcitvity extends AppCompatActivity implements View.OnFocusChangeListener {
    private String DOMINIO = "http://checkhotel.versalius.com.br/";
    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilIdentification;
    private TextInputLayout tilShippingAgent;
    private TextInputLayout tilCPF;
    private TextInputLayout tilStreet;
    private TextInputLayout tilPhone;
    private TextInputLayout tilProfession;
    private TextInputLayout tilNationality;

    private EditText etName;
    private EditText etEmail;
    private EditText etBirthday;
    private EditText etIdentification;
    private EditText etShippingAgent;
    private EditText etCPF;
    private EditText etNationality;
    private EditText etProfession;
    private EditText etStreet;
    private EditText etNumber;
    private EditText etNeighborhood;
    private EditText etZipCode;
    private EditText etPhone;

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private RadioGroup rgIdentification;
    private RadioButton rbRG;
    private RadioButton rbPassport;

    private Spinner spCity;
    private Spinner spContinent;
    private Spinner spCountry;
    private Spinner spState;

    //Localização do usuário

    private int continent_id;
    private String country;
    private String state;
    private String city;

    private ImageView ivCpfCheck;

    private ArrayAdapter<String> spCountryArrayAdapter;
    private ArrayList<String> spCountryListData;
    private HashMap<String, String> countryIdList;

    private ArrayAdapter<String> spStateArrayAdapter;
    private ArrayList<String> spStateListData;
    private HashMap<String, String> stateIdList;

    private ArrayAdapter<String> spCityArrayAdapter;
    private ArrayList<String> spCityListData;
    private HashMap<String, String> cityIdList;

    private CoordinatorLayout coordinatorLayout;

    private HashMap<String, String> formData;

    private MaterialDialog mMaterialDialog;
    private CircleImageView ivProfile;
    private ImageButton btGetImage;

    private static final int ACTION_RESULT_GET_IMAGE = 1000;
    private static final int REQUEST_PERMISSION_CODE = 1001;

    private Pattern pat;
    private Matcher mat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_acitvity);
        EventBus.getDefault().register(this);
        formData = new HashMap<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        getSupportActionBar().setLogo(R.drawable.toolbar_logo);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_profile);
        setUpViews();
    }

    private void setUpViews() {
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        /* Pegar imagem */
        btGetImage = (ImageButton) findViewById(R.id.btGetImage);
        btGetImage.setEnabled(false);
        btGetImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ProfileAcitvity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType("image/*");
                    startActivityForResult(i, ACTION_RESULT_GET_IMAGE);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileAcitvity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        callDialog("O Check Hotel precisa de permissão para acessar os arquivos do dispositivo", new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE});
                    } else {
                        ActivityCompat.requestPermissions(ProfileAcitvity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                    }
                }
            }
        });

        /* Instanciando layouts */
        tilName = (TextInputLayout) findViewById(R.id.tilName);
        tilEmail = (TextInputLayout) findViewById(R.id.tilEmail);
        tilIdentification = (TextInputLayout) findViewById(R.id.tilIdentification);
        tilShippingAgent = (TextInputLayout) findViewById(R.id.tilShippingAgent);
        tilCPF = (TextInputLayout) findViewById(R.id.tilCpf);
        tilStreet = (TextInputLayout) findViewById(R.id.tilStreet);
        tilPhone = (TextInputLayout) findViewById(R.id.tilPhone);
        tilProfession = (TextInputLayout) findViewById(R.id.tilProfession);
        tilNationality = (TextInputLayout) findViewById(R.id.tilNationality);

        /* Instanciando campos */
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etBirthday = (EditText) findViewById(R.id.etBirthday);
        etIdentification = (EditText) findViewById(R.id.etIdentification);
        etShippingAgent = (EditText) findViewById(R.id.etShippingAgent);
        etCPF = (EditText) findViewById(R.id.etCpf);
        etNationality = (EditText) findViewById(R.id.etNationality);
        etProfession = (EditText) findViewById(R.id.etProfession);
        etStreet = (EditText) findViewById(R.id.etStreet);
        etNumber = (EditText) findViewById(R.id.etNumber);
        etNeighborhood = (EditText) findViewById(R.id.etNeighborhood);
        etZipCode = (EditText) findViewById(R.id.etZipCode);
        etPhone = (EditText) findViewById(R.id.etPhone);

        etName.setEnabled(false);
        etEmail.setEnabled(false);
        etBirthday.setEnabled(false);
        etIdentification.setEnabled(false);
        etShippingAgent.setEnabled(false);
        etCPF.setEnabled(false);
        etNationality.setEnabled(false);
        etProfession.setEnabled(false);
        etStreet.setEnabled(false);
        etNumber.setEnabled(false);
        etNeighborhood.setEnabled(false);
        etZipCode.setEnabled(false);
        etPhone.setEnabled(false);


        /* Instanciando ImageVIew */
        ivCpfCheck = (ImageView) findViewById(R.id.ivCpfCheck);

        /* Adicionando FocusListener*/
        etName.setOnFocusChangeListener(this);
        etEmail.setOnFocusChangeListener(this);
        etPhone.setOnFocusChangeListener(this);
        etIdentification.setOnFocusChangeListener(this);
        etShippingAgent.setOnFocusChangeListener(this);
        etCPF.setOnFocusChangeListener(this);
        etNationality.setOnFocusChangeListener(this);
        etProfession.setOnFocusChangeListener(this);
        etStreet.setOnFocusChangeListener(this);
        etNumber.setOnFocusChangeListener(this);
        etNeighborhood.setOnFocusChangeListener(this);
        etZipCode.setOnFocusChangeListener(this);

        spCountry = (Spinner) findViewById(R.id.spCountry);
        spCountry.setEnabled(false);
        spCountryListData = new ArrayList<>();
        spCountryListData.add("Selecione um país...");
        spCountryArrayAdapter = new ArrayAdapter<>(ProfileAcitvity.this, android.R.layout.simple_spinner_dropdown_item, spCountryListData);
        spCountry.setAdapter(spCountryArrayAdapter);

        spState = (Spinner) findViewById(R.id.spState);
        spState.setEnabled(false);
        spStateListData = new ArrayList<>();
        spStateListData.add("Selecione um estado...");
        spStateArrayAdapter = new ArrayAdapter<>(ProfileAcitvity.this, android.R.layout.simple_spinner_dropdown_item, spStateListData);
        spState.setAdapter(spStateArrayAdapter);


        spCity = (Spinner) findViewById(R.id.spCity);
        spCity.setEnabled(false);
        spCityListData = new ArrayList<>();
        spCityListData.add("Selecione uma cidade...");
        spCityArrayAdapter = new ArrayAdapter<>(ProfileAcitvity.this, android.R.layout.simple_spinner_dropdown_item, spCityListData);
        spCity.setAdapter(spCityArrayAdapter);

        spCountry = (Spinner) findViewById(R.id.spCountry);
        spState = (Spinner) findViewById(R.id.spState);
        spCity = (Spinner) findViewById(R.id.spCity);

        spContinent.setEnabled(false);



        /* Carregas os dados dos campos */
        final ProgressDialogHelper progressHelper = new ProgressDialogHelper(ProfileAcitvity.this);
        progressHelper.createProgressSpinner("Aguarde", "Carregando dados", true, false);
        if (NetworkHelper.isOnline(ProfileAcitvity.this)) {
            NetworkHelper.getInstance(ProfileAcitvity.this).getUser((int) SessionHelper.getUserId(), new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    try {
                        progressHelper.dismiss();
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if (jsonObject.getBoolean("status")) {
                            User user = new User(jsonObject);
                            if (!SessionHelper.getAvatar().equals("null")) {
                                new DownloadImageTask(ivProfile).execute(DOMINIO + SessionHelper.getAvatar());
                            }
                            etName.setText(user.getName());
                            etEmail.setText(user.getEmail());
                            etBirthday.setText(user.getBirthday());
                            if (!user.getRg().equals("null")) {
                                rgIdentification.check(R.id.rbRG);
                                etIdentification.setText(user.getRg());
                                etShippingAgent.setText(user.getShipping_agent());
                                etCPF.setText(user.getCpf());
                            } else {
                                rgIdentification.check(R.id.rbPassport);
                                etIdentification.setText(user.getPassport());
                            }
                            etNationality.setText(user.getNationality());
                            etProfession.setText(user.getProfession());
                            if (user.getGender_id() == 1) {
                                rgGender.check(R.id.rbMale);
                            } else {
                                rgGender.check(R.id.rbFemale);
                            }
                            etStreet.setText(user.getStreet());
                            etNumber.setText(user.getNumber());
                            etNeighborhood.setText(user.getNeighborhood());
                            etZipCode.setText(user.getZip_code());
                            etPhone.setText(user.getPhone());
                        } else {
                            CustomSnackBar.make(coordinatorLayout, "Não foi possível resgatar seus dados, tente novamente!", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    Log.i("RESPONSE-FAIL", error.getMessage());
                    progressHelper.dismiss();
                    finish();
                }
            });
        } else {
            CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
            finish();
        }

        //pega a localização completa do usuário
        if (NetworkHelper.isOnline(ProfileAcitvity.this)) {
            NetworkHelper.getInstance(ProfileAcitvity.this).getGeoFull(SessionHelper.getCityId(), new ResponseCallback() {
                @Override
                public void onSuccess(String jsonStringResponse) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                        if (jsonObject.getBoolean("status")) {
                            continent_id = Integer.parseInt(jsonObject.getJSONObject("data").getString("continent_id"));
                            country = jsonObject.getJSONObject("data").getString("country_name");
                            state = jsonObject.getJSONObject("data").getString("state_name");
                            city = jsonObject.getJSONObject("data").getString("city_name");
                            spContinent.setSelection(continent_id);
                        } else {
                            CustomSnackBar.make(coordinatorLayout, "Não foi possível carregar sua localização", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFail(VolleyError error) {
                    Log.i("RESPONSE-FAIL", error.getMessage());
                    finish();
                }
            });
        } else {
            CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
            finish();
        }


        /* Adicionando máscara para o CPF*/
        etCPF.addTextChangedListener(new TextWatcher() {
            boolean isErasing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Se depois da mudança não serão acrescidos caracteres, está apagando */
                isErasing = (after == 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String lastChar = "";
                int digits = etCPF.getText().toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
                if (!isErasing) {
                    if (digits > 0) {
                        lastChar = etCPF.getText().toString().substring(digits - 1);
                    }
                    switch (digits) {
                        case 4:
                            if (!lastChar.equals(".")) {
                                String currentDigits = etCPF.getText().toString().substring(0, digits - 1);
                                etCPF.setText("");
                                etCPF.append(currentDigits + "." + lastChar);
                                break;
                            }

                        case 8:
                            if (!lastChar.equals(".")) {
                                String currentDigits = etCPF.getText().toString().substring(0, digits - 1);
                                etCPF.setText("");
                                etCPF.append(currentDigits + "." + lastChar);
                                break;
                            }
                        case 12:
                            if (!lastChar.equals("-")) {
                                String currentDigits = etCPF.getText().toString().substring(0, digits - 1);
                                etCPF.setText("");
                                etCPF.append(currentDigits + "-" + lastChar);
                                break;
                            }
                    }
                }
            }
        });

        /* Limpa o campo CEP para valores inválidos e incompletos */
        etZipCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hasValidZipCode();
                }
            }
        });

        /* Verifica se o campo foi preenchid corretamente */
        etProfession.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hasValidProfession();
                }
            }
        });

        /* Verifica se o campo foi preenchid corretamente */
        etNationality.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hasValidNationality();
                }
            }
        });

        /* Adicionando máscara para o CEP*/
        etZipCode.addTextChangedListener(new TextWatcher() {
            boolean isErasing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Se depois da mudança não serão acrescidos caracteres, está apagando */
                isErasing = (after == 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String lastChar = "";
                int digits = etZipCode.getText().toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
                if (!isErasing) {
                    if (digits > 0) {
                        lastChar = etZipCode.getText().toString().substring(digits - 1);
                    }
                    switch (digits) {
                        case 6:
                            if (!lastChar.equals("-")) {
                                String currentDigits = etZipCode.getText().toString().substring(0, digits - 1);
                                etZipCode.setText("");
                                etZipCode.append(currentDigits + "-" + lastChar);
                                break;
                            }
                    }
                }
            }
        });

        /* Adicionando máscara para o telefone*/
        etPhone.addTextChangedListener(new TextWatcher() {
            boolean isErasing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                /* Se depois da mudança não serão acrescidos caracteres, está apagando */
                isErasing = (after == 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String lastChar = "";
                int digits = etPhone.getText().toString().length();
                /* Se não está apagando, verifica se algo precisa ser adicionado */
                if (!isErasing) {
                    if (digits > 0) {
                        lastChar = etPhone.getText().toString().substring(digits - 1);
                    }
                    switch (digits) {
                        case 1:
                            String digit = etPhone.getText().toString();
                            etPhone.setText("");
                            etPhone.append("(" + digit);
                            break;
                        case 3:
                            etPhone.append(")");
                            break;
                        /* Quando o ")" é apagado */
                        case 4:
                            if (!lastChar.equals(")")) {
                                String currentDigits = etPhone.getText().toString().substring(0, digits - 1);
                                etPhone.setText("");
                                etPhone.append(currentDigits + ")" + lastChar);
                            }
                            break;
                        /* Assumindo números no formatp (99)9999-9999*/
                        case 8:
                            etPhone.append("-");
                            break;
                        /* Quando o "-" é apagado */
                        case 9:
                            if (!lastChar.equals("-")) {
                                String currentDigits = etPhone.getText().toString().substring(0, digits - 1);
                                etPhone.setText("");
                                etPhone.append(currentDigits + "-" + lastChar);
                            }
                            break;
                        /* Assumindo números no formatp (99)99999-9999*/
                        case 14:
                            try {
                                String currentDigits[] = etPhone.getText().toString().split("-");
                                if (currentDigits[0].length() == 8) {
                                    currentDigits[1] = new StringBuilder(currentDigits[1]).insert(1, "-").toString();
                                    etPhone.setText("");
                                    etPhone.append(currentDigits[0] + currentDigits[1]);
                                }
                            } catch (Exception e) {
                                //TODO: Lançar exceção
                            }
                            break;
                    }
                } else { /* Se apagou o último dígito deixando o número no formato (99)9999-9999 */
                    if (digits == 13) {
                        try {
                            String currentDigits[] = etPhone.getText().toString().split("-");
                            if (currentDigits[1].length() == 3) {
                                currentDigits[0] = new StringBuilder(currentDigits[0]).insert(currentDigits[0].length() - 1, "-").toString();
                                etPhone.setText("");
                                etPhone.append(currentDigits[0] + currentDigits[1]);
                            }
                        } catch (Exception e) {
                            //TODO: Lançar exceção
                        }
                    }
                }
            }
        });

        /* Radio buttons*/
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);
        rbMale.setEnabled(false);
        rbFemale.setEnabled(false);

        rgIdentification = (RadioGroup) findViewById(R.id.rgIdentification);
        rbRG = (RadioButton) findViewById(R.id.rbRG);
        rbPassport = (RadioButton) findViewById(R.id.rbPassport);
        rbRG.setEnabled(false);
        rbPassport.setEnabled(false);

        /** Setei o valor do Hint do EditText de identificação aqui, pois eu não conseguia apagar
         * setando diretamente no layout **/

        etIdentification.setHint("RG");

        /** Alterna entre os campos RG e Passporte ao selecionar uma opção do RadioGroup **/

        rgIdentification.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbPassport) {
                    tilShippingAgent.setVisibility(View.GONE);
                    etShippingAgent.setVisibility(View.GONE);
                    tilCPF.setVisibility(View.GONE);
                    etCPF.setVisibility(View.GONE);
                    ivCpfCheck.setVisibility(View.GONE);
                    etIdentification.setHint("Passaporte");
                } else {
                    tilShippingAgent.setVisibility(View.VISIBLE);
                    etShippingAgent.setVisibility(View.VISIBLE);
                    tilCPF.setVisibility(View.VISIBLE);
                    etCPF.setVisibility(View.VISIBLE);
                    ivCpfCheck.setVisibility(View.VISIBLE);
                    etIdentification.setHint("RG");

                }
            }
        });

        /**** Seta o comportamento do DatePicker ****/
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar nowCalendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                etBirthday.setText(dateFormatter.format(newDate.getTime()));
            }

        }, nowCalendar.get(Calendar.YEAR), nowCalendar.get(Calendar.MONTH), nowCalendar.get(Calendar.DAY_OF_MONTH));

        nowCalendar.set(nowCalendar.get(Calendar.YEAR) - 18, 11, 31);
        datePickerDialog.getDatePicker().setMaxDate(nowCalendar.getTimeInMillis());

        etBirthday.setInputType(InputType.TYPE_NULL);
        etBirthday.setText(dateFormatter.format(nowCalendar.getTime()));
        //Abre o Date Picker com click (só funciona se o campo tiver foco)
        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
        //Abre o Date Picker assim que o campo receber foco
        etBirthday.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePickerDialog.show();
            }
        });


        /*
        ** Carrega as cidades do estado
         */

        spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    int selectedCountryId = Integer.valueOf(stateIdList.get(spState.getSelectedItem().toString()));
                    cityIdList = new HashMap<>();
                /* Se o valor do item selecionado é 0, o item selecionado é "Selecione um estado...". Logo, não há seleção válida*/
                    if (selectedCountryId == 0) {
                        spCityListData.clear();
                        spCityListData.add(getResources().getString(R.string.hint_city_spinner));
                        cityIdList.put(getResources().getString(R.string.hint_city_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                        spCity.setEnabled(false);
                        spCityArrayAdapter.notifyDataSetChanged();
                        return;
                    }

                    final ProgressDialogHelper progressHelper = new ProgressDialogHelper(ProfileAcitvity.this);
                    //progressHelper.createProgressSpinner("Aguarde", "Atualizando cidades", true, false);

                    NetworkHelper.getInstance(ProfileAcitvity.this).getCities(selectedCountryId, new ResponseCallback() {
                        @Override
                        public void onSuccess(String jsonStringResponse) {
                            try {
                                spCityListData.clear();
                                spCityListData.add(getResources().getString(R.string.hint_city_spinner));
                                cityIdList.put(getResources().getString(R.string.hint_city_spinner), "0"); /* O id do primeiro item do spinner é nulo (ou seja, é zero)*/
                                JSONArray jArray = new JSONArray(jsonStringResponse);
                                if (jArray != null) {
                                    for (int i = 0; i < jArray.length(); i++) {
                                        spCityListData.add(jArray.getJSONObject(i).getString("name"));
                                        cityIdList.put(jArray.getJSONObject(i).getString("name"), jArray.getJSONObject(i).getString("id"));
                                    }
                                }
                                //spCity.setEnabled(true);
                                spCityArrayAdapter.notifyDataSetChanged();
                                //progressHelper.dismiss();
                                SpinnerAdapter adapter = spCity.getAdapter();
                                for (int iPos = 0; iPos < spCity.getCount(); iPos++) {
                                    if (city.trim().equals(adapter.getItem(iPos).toString().trim())) {
                                        spCity.setSelection(iPos);
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(VolleyError error) {
                            Log.i("RESPONSE-FAIL", error.getMessage());
                            progressHelper.dismiss();
                        }
                    });
                } catch (Exception e) {
                }
                /* Através da posição do estado selecionado no spinner, descobre-se o id dele */


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Button btEdit = (Button) findViewById(R.id.btEdit);
        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btEdit.getText().toString().equals("Editar")) {
                    btGetImage.setEnabled(true);
                    etName.setEnabled(true);
                    etEmail.setEnabled(true);
                    etBirthday.setEnabled(true);
                    etIdentification.setEnabled(true);
                    etShippingAgent.setEnabled(true);
                    etCPF.setEnabled(true);
                    etNationality.setEnabled(true);
                    etProfession.setEnabled(true);
                    etStreet.setEnabled(true);
                    etNumber.setEnabled(true);
                    etNeighborhood.setEnabled(true);
                    etZipCode.setEnabled(true);
                    etPhone.setEnabled(true);
                    rbMale.setEnabled(true);
                    rbFemale.setEnabled(true);
                    rbRG.setEnabled(true);
                    rbPassport.setEnabled(true);
                    spContinent.setEnabled(true);
                    spCountry.setEnabled(true);
                    spState.setEnabled(true);
                    spCity.setEnabled(true);
                    btEdit.setText("Atualizar");
                } else {
                    final ProgressDialogHelper progressHelper = new ProgressDialogHelper(ProfileAcitvity.this);

                    if (NetworkHelper.isOnline(ProfileAcitvity.this)) {
                        if (isValidForm()) {
                            progressHelper.createProgressSpinner("Aguarde", "Realizando atualização.", true, false);
                            NetworkHelper.getInstance(ProfileAcitvity.this).userUpdate(formData, new ResponseCallback() {
                                @Override
                                public void onSuccess(String jsonStringResponse) {
                                    try {
                                        progressHelper.dismiss();
                                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                        if (jsonObject.getBoolean("status")) {
                                            NetworkHelper.getInstance(ProfileAcitvity.this).getSession(new ResponseCallback() {
                                                @Override
                                                public void onSuccess(String jsonStringResponse) {
                                                    try {
                                                        progressHelper.dismiss();
                                                        JSONObject jsonObject = new JSONObject(jsonStringResponse);
                                                        if (jsonObject.getBoolean("status")) {
                                                            User user = new User(jsonObject.getJSONObject("data"));
                                                            SessionHelper.saveUser(user);
                                                            setResult(RESULT_OK, null);
                                                            finish();
                                                        } else {
                                                            CustomSnackBar.make(coordinatorLayout, "Falha ao atualizar", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onFail(VolleyError error) {
                                                    progressHelper.dismiss();
                                                    //CustomSnackBar.make(coordinatorLayout, "Falha ao realizar cadastro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                                }
                                            });
                                        } else {
                                            CustomSnackBar.make(coordinatorLayout, "Falha ao atualizar", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFail(VolleyError error) {
                                    progressHelper.dismiss();
                                    //CustomSnackBar.make(coordinatorLayout, "Falha ao realizar cadastro", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                                }
                            });
                        } else
                            CustomSnackBar.make(coordinatorLayout, "Atenção! Preencha o formulário corretamente.", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.INFO).show();

                    } else {
                        CustomSnackBar.make(coordinatorLayout, "Você está offline", Snackbar.LENGTH_LONG, CustomSnackBar.SnackBarType.ERROR).show();
                    }
                }
            }
        });
    }

    /**
     * Valida os campos do formulário setando mensagens de erro
     */
    private boolean isValidForm() {
        formData.put("user_id", String.valueOf(SessionHelper.getUserId()));
        formData.put("key", SessionHelper.getUserKey());
        formData.put("birthday", etBirthday.getText().toString());
        formData.put("number", etNumber.getText().toString());
        formData.put("neighborhood", etNeighborhood.getText().toString());
        formData.put("zipcode", etZipCode.getText().toString());
        formData.put("nationality", etNationality.getText().toString());
        formData.put("profession", etProfession.getText().toString());

        boolean isFocusRequested = false;

        /* Verifica se o campo Nome */
        if (!hasValidName()) {
            tilName.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("name", etName.getText().toString());
        }

        /* Verifica o campo de e-mail*/
        if (!hasValidEmail()) {
            if (!isFocusRequested) {
                tilEmail.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("email", etEmail.getText().toString());
        }

        /* Verifica se os radio buttons estão descelecionados*/
        if (!hasValidGender()) {
            if (!isFocusRequested) {
                rgGender.requestFocus();
                isFocusRequested = true;
            }
        } else {
            if (rbFemale.isChecked()) {
                formData.put("gender_id", "2");
            } else {
                formData.put("gender_id", "1");
            }
        }

        /* Verifica se o campo de identificação foi preenchido
         * e qual tipo de documento fornecido */
        if (!hasValidIdentification()) {
            tilIdentification.requestFocus();
            isFocusRequested = true;
        } else {
            if (rbRG.isChecked()) {
                formData.put("rg", etIdentification.getText().toString());
                /* Verifica se o campo Orgão Expedidor foi preenchido */
                if (!hasValidShippingAgent()) {
                    tilShippingAgent.requestFocus();
                    isFocusRequested = true;
                } else {
                    formData.put("shipping_agent", etShippingAgent.getText().toString());
                }

        /* Verifica se o campo CPF foi preenchido */
                if (!hasValidCpf()) {
                    tilCPF.requestFocus();
                    isFocusRequested = true;
                } else {
                    formData.put("cpf", etCPF.getText().toString());
                }

            } else {
                formData.put("passport", etIdentification.getText().toString());
            }
        }

        /* Verifica se o campo Rua foi preenchido */
        if (!hasValidStreet()) {
            tilStreet.requestFocus();
            isFocusRequested = true;
        } else {
            formData.put("street", etStreet.getText().toString());
        }

        /* Verifica o campo de telefone*/
        if (!hasValidPhone()) {
            if (!isFocusRequested) {
                tilPhone.requestFocus();
                isFocusRequested = true;
            }
        } else {
            formData.put("phone", etPhone.getText().toString());
        }

        /* Verifica o spinner de estado*/
        if (!hasValidState()) {
            if (!isFocusRequested) {
                spState.requestFocus();
                isFocusRequested = true;
            }
        } else {
            /* Verifica o spinner de cidade*/
            if (!hasValidCity()) {
                if (!isFocusRequested) {
                    spCity.requestFocus();
                    isFocusRequested = true;
                }
            } else {
                formData.put("city_id", cityIdList.get(spCityListData.get(spCity.getSelectedItemPosition())));
            }
        }

        /* Se ninguém pediu foco então tá tudo em ordem */
        return !isFocusRequested;
    }

    private boolean hasValidPhone() {
        String phone = etPhone.getText().toString().trim();
        String phoneNumber[] = phone.split("-");

        if ((TextUtils.isEmpty(phone) ||
                (phone.length() < 13)) ||
                (phoneNumber.length < 2) ||
                (phoneNumber[1].length() != 4) ||
                (phoneNumber[0].length() != 8 &&
                        phoneNumber[0].length() != 9)) {
            tilPhone.setError(getResources().getString(R.string.err_msg_invalid_phone));
            return false;
        }
        tilPhone.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidGender() {
        if (!rbMale.isChecked() && !rbFemale.isChecked()) {
            (findViewById(R.id.tvRgErrMessage)).setVisibility(View.VISIBLE);
            return false;
        }
        (findViewById(R.id.tvRgErrMessage)).setVisibility(View.GONE);
        return true;
    }


    private boolean hasValidEmail() {
        final String email = etEmail.getText().toString().trim();
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
        NetworkHelper.getInstance(this).emailBelongs(String.valueOf(SessionHelper.getUserId()), email, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O email existe */
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_check));
                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                    } else {
                        NetworkHelper.getInstance(ProfileAcitvity.this).emailExists(email, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                findViewById(R.id.ivEmailCheck).setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                try {
                                    JSONObject json = new JSONObject(jsonStringResponse);
                                    if (json.getBoolean("status")) { /* O email existe */
                                        tilEmail.setError(getResources().getString(R.string.err_msg_existing_email));
                                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_close_circle));
                                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                                    } else {
                                        ((ImageView) findViewById(R.id.ivEmailCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_check));
                                        ((ImageView) findViewById(R.id.ivEmailCheck)).setColorFilter(Color.argb(255, 0, 192, 96));

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

    private boolean hasValidCpf() {
        final String cpf = etCPF.getText().toString().trim();
        pat = Pattern.compile("^[0-9]{3}[\\.][0-9]{3}[\\.][0-9]{3}[\\-][0-9]{2}$");
        mat = pat.matcher(cpf);
        if (TextUtils.isEmpty(cpf)) {
            tilCPF.setError(getResources().getString(R.string.err_msg_empty_cpf));
            return false;
        } else if (!mat.find()) {
            tilCPF.setError(getResources().getString(R.string.err_msg_invalid_cpf));
            return false;
        } else if (!isCPF(cpf)) {
            tilCPF.setError(getResources().getString(R.string.err_msg_invalid_cpf));
            return false;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbCpfCheck);
        findViewById(R.id.ivCpfCheck).setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        NetworkHelper.getInstance(this).cpfBelongs(String.valueOf(SessionHelper.getUserId()), cpf, new ResponseCallback() {
            @Override
            public void onSuccess(String jsonStringResponse) {
                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                try {
                    JSONObject json = new JSONObject(jsonStringResponse);
                    if (json.getBoolean("status")) { /* O cpf existe */
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_check));
                        ((ImageView) findViewById(R.id.ivCpfCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                    } else {
                        NetworkHelper.getInstance(ProfileAcitvity.this).cpfExists(cpf, new ResponseCallback() {
                            @Override
                            public void onSuccess(String jsonStringResponse) {
                                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                try {
                                    JSONObject json = new JSONObject(jsonStringResponse);
                                    if (json.getBoolean("status")) { /* O cpf existe */
                                        tilCPF.setError(getResources().getString(R.string.err_msg_existing_cpf));
                                        ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_close_circle));
                                        ((ImageView) findViewById(R.id.ivCpfCheck)).setColorFilter(Color.argb(255, 239, 83, 80));
                                    } else {
                                        ((ImageView) findViewById(R.id.ivCpfCheck)).setImageDrawable(ContextCompat.getDrawable(ProfileAcitvity.this, R.drawable.ic_check));
                                        ((ImageView) findViewById(R.id.ivCpfCheck)).setColorFilter(Color.argb(255, 0, 192, 96));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(VolleyError error) {
                                tilCPF.setError(getResources().getString(R.string.err_msg_server_fail));
                                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(VolleyError error) {
                tilCPF.setError(getResources().getString(R.string.err_msg_server_fail));
                findViewById(R.id.ivCpfCheck).setVisibility(View.VISIBLE);
            }
        });

        tilCPF.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidName() {
        String name = etName.getText().toString().trim();
        pat = Pattern.compile("^[^\\d]+$");
        mat = pat.matcher(name);
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getResources().getString(R.string.err_msg_empty_name));
            return false;
        } else if (!mat.find()) {
            tilName.setError(getResources().getString(R.string.err_msg_invalid_name));
            return false;
        }
        tilName.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidIdentification() {
        if (TextUtils.isEmpty(etIdentification.getText().toString().trim())) {
            tilIdentification.setError(getResources().getString(R.string.err_msg_empty_identification));
            return false;
        }
        tilIdentification.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidShippingAgent() {
        String sa = etShippingAgent.getText().toString().trim();
        pat = Pattern.compile("^[^\\d]+$");
        mat = pat.matcher(sa);
        if (TextUtils.isEmpty(sa)) {
            tilShippingAgent.setError(getResources().getString(R.string.err_msg_empty_shipping_agent));
            return false;
        } else if (!mat.find()) {
            tilShippingAgent.setError(getResources().getString(R.string.err_msg_invalid_shipping_agent));
            return false;
        }

        tilShippingAgent.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidStreet() {
        if (TextUtils.isEmpty(etStreet.getText().toString().trim())) {
            tilStreet.setError(getResources().getString(R.string.err_msg_empty_street));
            return false;
        }
        tilStreet.setErrorEnabled(false);
        return true;
    }

    private boolean hasValidState() {
        if (spState.getSelectedItemPosition() == 0) {
            (findViewById(R.id.tvSpStateErrMessage)).setVisibility(View.VISIBLE);
            return false;
        }
        (findViewById(R.id.tvSpStateErrMessage)).setVisibility(View.GONE);
        return true;
    }

    private boolean hasValidCity() {
        if (spCity.getSelectedItemPosition() == 0) {
            (findViewById(R.id.tvSpCityErrMessage)).setVisibility(View.VISIBLE);
            return false;
        }
        (findViewById(R.id.tvSpCityErrMessage)).setVisibility(View.GONE);
        return true;
    }

    private void hasValidZipCode() {

        String zipcode = etZipCode.getText().toString().trim();
        pat = Pattern.compile("^[0-9]{5}[\\-][0-9]{3}$");
        mat = pat.matcher(zipcode);
        if (!mat.find() || zipcode.length() < 8)
            etZipCode.setText("");
    }

    private void hasValidProfession() {
        String profession = etProfession.getText().toString().trim();
        pat = Pattern.compile("^[^\\d]*$");
        mat = pat.matcher(profession);
        if (!mat.find())
            tilProfession.setError(getResources().getString(R.string.err_msg_invalid_profession));
        else
            tilProfession.setErrorEnabled(false);
    }

    private void hasValidNationality() {

        String nationality = etNationality.getText().toString().trim();
        pat = Pattern.compile("^[^\\d]*$");
        mat = pat.matcher(nationality);
        if (!mat.find())
            tilNationality.setError(getResources().getString(R.string.err_msg_invalid_nationality));
        else
            tilNationality.setErrorEnabled(false);
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
                case R.id.etName:
                    hasValidName();
                    break;
                case R.id.etEmail:
                    hasValidEmail();
                    break;

                case R.id.etIdentification:
                    hasValidIdentification();
                    break;

                case R.id.etShippingAgent:
                    hasValidShippingAgent();
                    break;

                case R.id.etCpf:
                    hasValidCpf();
                    break;

                case R.id.etStreet:
                    hasValidStreet();
                    break;

                case R.id.etPhone:
                    hasValidPhone();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_RESULT_GET_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            //Com base na URI da imagem selecionada, prepara o acesso ao banco de dados interno pra pegar a imagem
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, columns, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            //Passa o caminho da imagem pra activity que vai fazer o crop
            startActivity(new Intent(this, CropActivity.class).putExtra("imagePath", imagePath));
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
                        ActivityCompat.requestPermissions(ProfileAcitvity.this, permissions, REQUEST_PERMISSION_CODE);
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

    /* O EventBus é usado somente pra trazer a imagem cortada de volta pra cá */
    @Subscribe
    public void onEvent(Bitmap bitmap) {
        /* Preview da imagem */
        ivProfile.setImageBitmap(bitmap);

        /* Codifica a imagem pra envio */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        formData.put("avatar", Base64.encodeToString(imageBytes, Base64.DEFAULT));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public static boolean isCPF(String CPF) {
        CPF = CPF.replaceAll("\\D", "");

        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") || CPF.equals("11111111111") ||
                CPF.equals("22222222222") || CPF.equals("33333333333") ||
                CPF.equals("44444444444") || CPF.equals("55555555555") ||
                CPF.equals("66666666666") || CPF.equals("77777777777") ||
                CPF.equals("88888888888") || CPF.equals("99999999999") ||
                (CPF.length() != 11))
            return (false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char) (r + 48); // converte no respectivo caractere numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char) (r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
                return (true);
            else return (false);
        } catch (Exception erro) {
            return (false);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
