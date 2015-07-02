package com.lukaszgajos.keepassmob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lukaszgajos.keepassmob.core.KeepSession;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.io.File;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mFilename;
    private EditText mPassword;
    private EditText mKeyfile;
    private Button mOpenDatabase;
    private ImageView mPickDb;
    private ImageView mPickKey;

    private ImageButton mClearDbBtn;
    private ImageButton mClearPwdBtn;
    private ImageButton mClearKeyBtn;


    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        mFilename = (EditText) findViewById(R.id.db_file_path_input);
        mPassword = (EditText) findViewById(R.id.password_input);
        mKeyfile = (EditText) findViewById(R.id.key_file_input);
        mOpenDatabase = (Button) findViewById(R.id.open_db_btn);
        mClearDbBtn = (ImageButton) findViewById(R.id.clear_db_btn);
        mClearPwdBtn = (ImageButton) findViewById(R.id.clear_pwd_btn);
        mClearKeyBtn = (ImageButton) findViewById(R.id.clear_keyfile_btn);


        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mFilename.setText(pref.getString(getString(R.string.preferences_db_filename), ""));

        mOpenDatabase.setOnClickListener(this);
        mFilename.setOnClickListener(this);
        mKeyfile.setOnClickListener(this);
        mClearDbBtn.setOnClickListener(this);
        mClearPwdBtn.setOnClickListener(this);
        mClearKeyBtn.setOnClickListener(this);

        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (mPassword.getText().length() > 0){
                    mClearPwdBtn.setVisibility(View.VISIBLE);
                } else {
                    mClearPwdBtn.setVisibility(View.INVISIBLE);
                }
            }
        });

        checkIfSessionActive();

//        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        im.showInputMethodPicker();
    }

    @Override
    public void onResume(){
        super.onResume();

        if (mFilename.getText().length() > 0){
            mClearDbBtn.setVisibility(View.VISIBLE);
        } else {
            mClearDbBtn.setVisibility(View.GONE);
        }

        if (mPassword.getText().length() > 0){
            mClearPwdBtn.setVisibility(View.VISIBLE);
        } else {
            mClearPwdBtn.setVisibility(View.GONE);
        }

        if (mKeyfile.getText().length() > 0){
            mClearKeyBtn.setVisibility(View.VISIBLE);
        } else {
            mClearKeyBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.open_db_btn){
            prepareOpenDatabase();
        } else if (v.getId() == R.id.db_file_path_input){
            chooseFileActivity(Environment.getExternalStorageDirectory().toString(), R.id.db_file_path_input);
        } else if (v.getId() == R.id.key_file_input){
            chooseFileActivity(Environment.getExternalStorageDirectory().toString(), R.id.key_file_input);
        } else if (v.getId() == R.id.clear_db_btn){
            mFilename.setText("");
            mClearDbBtn.setVisibility(View.GONE);
        } else if (v.getId() == R.id.clear_pwd_btn){
            mPassword.setText("");
            mClearPwdBtn.setVisibility(View.GONE);
        } else if (v.getId() == R.id.clear_keyfile_btn){
            mKeyfile.setText("");
            mClearKeyBtn.setVisibility(View.GONE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result_path");
                result = result.replace(Environment.getExternalStorageDirectory().getAbsolutePath() , "");

                int toInput = data.getIntExtra("back_input_id", 0);
                EditText input = (EditText)findViewById(toInput);
                input.setText(result);
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private void prepareOpenDatabase(){
        String filename = mFilename.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String keyfile = mKeyfile.getText().toString().trim();

        String p = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (filename.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_db_not_filled), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0 && keyfile.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_fill_form), Toast.LENGTH_SHORT).show();
            return;
        }
        if (filename.length() > 0) filename = Environment.getExternalStorageDirectory().getAbsolutePath() + filename;
        if (keyfile.length() > 0) keyfile = Environment.getExternalStorageDirectory().getAbsolutePath() + keyfile;

        File filenameFile = new File(filename);
        if (!filenameFile.exists()){
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_db_not_exists), Toast.LENGTH_SHORT).show();
            return;
        }

//        Loader loader = null;
        File dbFilename = new File(filename);
        File keyFilename = null;
        if (keyfile.length() > 0){
            keyFilename = new File(keyfile);
            if (!keyFilename.exists()){
                Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_key_not_exists), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.preferences_db_filename), mFilename.getText().toString().trim());
        editor.commit();

        PasswordDatabase.LoadDatabase(dbFilename, password, keyFilename);
        if (!PasswordDatabase.isCredentialsCorrect()){
            Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(pref, androidId);
        if (dbFilename.exists()){
            session.storeDatabase(dbFilename);
        }
        if (password.length() > 0){
            session.storePassword(password);
        }
        if (keyFilename != null && keyFilename.exists()){
            session.storeKey(keyFilename);
        }

        afterUnlock();
    }

    private void checkIfSessionActive(){
        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(pref, androidId);

        if (!session.isActive()){
            return;
        }

        File dbPath = session.getDatabase();
        String pwd = session.getPassword();
        File keyPath = session.getKey();
        if (dbPath == null || !dbPath.exists()){
            return;
        }
        PasswordDatabase.LoadDatabase(dbPath, pwd, keyPath);

        if (!PasswordDatabase.isCredentialsCorrect()){
            return;
        }

        Toast.makeText(getApplicationContext(), getString(R.string.msg_auto_login_from_session), Toast.LENGTH_SHORT).show();

        afterUnlock();
    }

    private void afterUnlock(){

        mPassword.setText("");
        if (!pref.getBoolean("save_keyfile_location", false)){
            mKeyfile.setText("");
        }

        boolean finish = false;
        if (getIntent().hasExtra("finish_after_open")){
            finish = getIntent().getExtras().getBoolean("finish_after_open", false);
        }

        if (finish){
            moveTaskToBack(true);
        } else {
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            startActivity(mainActivityIntent);
        }
    }

    private void chooseFileActivity(String startPath, int backInputId){
        Intent i = new Intent(this, FileBrowserActivity.class);
        i.putExtra("back_input_id", backInputId);
        i.putExtra("start_path", startPath);

        startActivityForResult(i, 1);
    }
}
