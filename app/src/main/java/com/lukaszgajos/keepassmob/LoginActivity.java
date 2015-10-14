package com.lukaszgajos.keepassmob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.keepassdroid.database.PwGroup;
import com.lukaszgajos.keepassmob.core.KeepSession;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


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

    private Spinner mFileSource;
    private Integer mFileSourceMethod = FileSourceAdapter.LOCAL;
    private Spinner mKeySource;
    private Integer mKeySourceMethod = FileSourceAdapter.LOCAL;

    private static int SAF_REQUEST_KEY_CODE = 3;
    private static int SAF_REQUEST_DB_CODE = 4;
    private static int CONTENT_REQUEST_DB_CODE = 5;
    private static int CONTENT_REQUEST_KEY_CODE = 6;
    private static int FS_REQUEST_DB_CODE = 1;
    private static int FS_REQUEST_KEY_CODE = 2;

    private Uri safDbUri;
    private Uri safKeyUri;

    private SharedPreferences pref;

    private static boolean mFileSourceFirstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mFilename = (EditText) findViewById(R.id.db_file_path_input);
        mPassword = (EditText) findViewById(R.id.password_input);
        mKeyfile = (EditText) findViewById(R.id.key_file_input);
        mOpenDatabase = (Button) findViewById(R.id.open_db_btn);
        mClearDbBtn = (ImageButton) findViewById(R.id.clear_db_btn);
        mClearPwdBtn = (ImageButton) findViewById(R.id.clear_pwd_btn);
        mClearKeyBtn = (ImageButton) findViewById(R.id.clear_keyfile_btn);
        mFileSource = (Spinner) findViewById(R.id.file_source_list);
        mKeySource = (Spinner) findViewById(R.id.key_source_list);

        mFileSource.setAdapter(new FileSourceAdapter(getApplicationContext(), FileSourceAdapter.getAvailableSource()));

        mFileSourceMethod = pref.getInt(getString(R.string.preferences_db_source), FileSourceAdapter.LOCAL);
        mFileSource.setSelection(mFileSourceMethod);
        mKeySource.setAdapter(new FileSourceAdapter(getApplicationContext(), FileSourceAdapter.getAvailableSource()));

        mFileSource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (mFileSourceFirstRun){
                    mFileSourceFirstRun = false;
                    return;
                }
                mFileSourceMethod = (int) mFileSource.getSelectedItem();
                mFilename.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mKeySource.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mKeySourceMethod = (int) mKeySource.getSelectedItem();
                mKeyfile.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        String lastDbUri = pref.getString(getString(R.string.preferences_db_filename), "");
        if (lastDbUri.length() > 0){
            setDbUri(Uri.parse(lastDbUri));
            if(mPassword.requestFocus()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mPassword, InputMethodManager.SHOW_IMPLICIT);
            }
        }


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

        Uri fromFileSystemDb = getIntent().getData();
        if (fromFileSystemDb != null){
            setDbUri(fromFileSystemDb);
        } else {
            checkIfSessionActive();
        }

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
        if (safDbUri != null){
            setDbUri(safDbUri);
        }
        if (safKeyUri != null){
            setKeyUri(safKeyUri);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_login, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.open_db_btn){
            prepareOpenDatabase();
        } else if (v.getId() == R.id.db_file_path_input){

            if (mFileSourceMethod == FileSourceAdapter.LOCAL){
                chooseFileActivity(Environment.getExternalStorageDirectory().toString(), FS_REQUEST_DB_CODE);
            } else {
                chooseGetContentActivity(CONTENT_REQUEST_DB_CODE);
            }
        } else if (v.getId() == R.id.key_file_input){
            if (mKeySourceMethod == FileSourceAdapter.LOCAL){
                chooseFileActivity(Environment.getExternalStorageDirectory().toString(), FS_REQUEST_KEY_CODE);
            } else {
                chooseGetContentActivity(CONTENT_REQUEST_KEY_CODE);
            }
        } else if (v.getId() == R.id.clear_db_btn){
            mFilename.setText("");
            mClearDbBtn.setVisibility(View.GONE);
            safDbUri = null;
        } else if (v.getId() == R.id.clear_pwd_btn){
            mPassword.setText("");
            mClearPwdBtn.setVisibility(View.GONE);
        } else if (v.getId() == R.id.clear_keyfile_btn){
            mKeyfile.setText("");
            mClearKeyBtn.setVisibility(View.GONE);
            safKeyUri = null;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FS_REQUEST_DB_CODE || requestCode == FS_REQUEST_KEY_CODE) {
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result_path");

                if (requestCode == FS_REQUEST_DB_CODE){
                    setDbUri(Uri.fromFile(new File(result)));
                }
                if (requestCode == FS_REQUEST_KEY_CODE){
                    setKeyUri(Uri.fromFile(new File(result)));
                }
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }

        if (requestCode == CONTENT_REQUEST_DB_CODE && resultCode == RESULT_OK){
            if (data != null){
                setDbUri(data.getData());
            }
        }
        if (requestCode == CONTENT_REQUEST_KEY_CODE && resultCode == RESULT_OK){
            if (data != null){
                setKeyUri(data.getData());
            }
        }

        if ( requestCode == SAF_REQUEST_DB_CODE && resultCode == RESULT_OK){
            if (data != null){
                setDbUri(data.getData());


                if (Build.VERSION .SDK_INT>= Build.VERSION_CODES.KITKAT){
                    getContentResolver().takePersistableUriPermission(safDbUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(safDbUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }


//                if ("file:".equals(safDbUri.toString().substring(0, 5))){
//                    fsDbFile = new File(safDbUri.getPath());
//                    mFilename.setText(fsDbFile.getName());
//                } else {
//                    mFilename.setText(safDbUri.getPath());
//                }
//
//                Cursor c = getContentResolver().query(safDbUri, null, null, null, null);
//                c.moveToFirst();
//
//                c.close();
            }
        }
        if (requestCode == SAF_REQUEST_KEY_CODE && resultCode == RESULT_OK){
            if (data != null){
                setKeyUri(data.getData());
            }
        }
    }

    private void prepareOpenDatabase(){
        String filename = mFilename.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String keyfile = mKeyfile.getText().toString().trim();

        if (filename.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_db_not_filled), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0 && keyfile.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_fill_form), Toast.LENGTH_SHORT).show();
            return;
        }

//        if (!fsDbFile.exists()){
//            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_db_not_exists), Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (fsKeyFile != null && !fsKeyFile.exists()){
//            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_key_not_exists), Toast.LENGTH_SHORT).show();
//            return;
//        }

//        SharedPreferences.Editor editor = pref.edit();
//        editor.putString(getString(R.string.preferences_db_filename), safDbUri.toString());
//        editor.putString(getString(R.string.preferences_db_filename), safDbUri.toString());
//        editor.commit();

//        PasswordDatabase.LoadDatabase(fsDbFile, password, fsKeyFile);

        try {

            InputStream ins = getContentResolver().openInputStream(safDbUri);
            PasswordDatabase.LoadDatabase(ins, password, safKeyUri == null ? "" : safKeyUri.getPath());
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_login_activity_db_not_exists), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PasswordDatabase.isCredentialsCorrect()){
            Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_credentials), Toast.LENGTH_SHORT).show();
            return;
        }

        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(pref, androidId);
        session.storeDatabase(safDbUri.toString());
        if (mFileSourceMethod == FileSourceAdapter.CLOUD && !safDbUri.toString().contains("com.dropbox.android")){
            PasswordDatabase.setReadOnly(true);
        } else {
            PasswordDatabase.setReadOnly(false);
        }

        if (password.length() > 0){
            session.storePassword(password);
        }
        if (safKeyUri != null){
            session.storeKey(safKeyUri.toString());
        }
        PasswordDatabase.setSession(session);

        afterUnlock();
    }

    private void checkIfSessionActive(){
        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(pref, androidId);

        if (!session.isActive()){
            return;
        }

        setDbUri(session.getDatabase());
        String pwd = session.getPassword();
        setKeyUri(session.getKey());


//        if (Build.VERSION .SDK_INT >= Build.VERSION_CODES.KITKAT){
//            getContentResolver().takePersistableUriPermission(safDbUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            getContentResolver().takePersistableUriPermission(safDbUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        }

        if (safDbUri == null){
            return;
        }
        InputStream ins = null;
        try {
            ins = getContentResolver().openInputStream(safDbUri);
            PasswordDatabase.LoadDatabase(ins, pwd, safKeyUri == null ? "" : safKeyUri.getPath());
            PasswordDatabase.setSession(session);
            if (mFileSourceMethod == FileSourceAdapter.CLOUD && !safDbUri.toString().contains("com.dropbox.android")){
                PasswordDatabase.setReadOnly(true);
            } else {
                PasswordDatabase.setReadOnly(false);
            }
        } catch (FileNotFoundException e) {
            return;
        }

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

    private void chooseFileActivity(String startPath, int requestCode){
        Intent i = new Intent(this, FileBrowserActivity.class);

        i.putExtra("start_path", startPath);

        startActivityForResult(i, requestCode);
    }

    private void chooseGetContentActivity(int requestCode){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setDataAndType(Uri.parse("/"), "application/octet-stream");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.app_name)), requestCode);
    }

    private void chooseOpenDocumentActivity(int requestCode){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setDataAndType(Uri.parse("/"), "*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.app_name)), requestCode);
    }

    private void setDbUri(Uri uri){
        safDbUri = uri;
        if (uri == null){
            return;
        }

        mFilename.setText(safDbUri.getLastPathSegment());

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.preferences_db_filename), safDbUri.toString());
        editor.putInt(getString(R.string.preferences_db_source), mFileSourceMethod);
        editor.commit();
    }

    private void setKeyUri(Uri uri){
        safKeyUri = uri;
        if (uri == null){
            return;
        }

        mKeyfile.setText(safKeyUri.getLastPathSegment());
    }
}
