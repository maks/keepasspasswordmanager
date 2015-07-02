package com.lukaszgajos.keepassmob;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.keepassdroid.database.exception.InvalidKeyFileException;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.io.File;
import java.io.IOException;


public class SetMasterKeyActivity extends ActionBarActivity implements View.OnClickListener {

    CheckBox mUsePassword;
    CheckBox mUseKeyfile;
    EditText mPassword1;
    EditText mPassword2;
    EditText mKeyfile;
    Button mSaveBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_master_key);
        setTitle(getString(R.string.title_activity_set_master_key));

        mUseKeyfile = (CheckBox) findViewById(R.id.key_checkbox);
        mUsePassword = (CheckBox) findViewById(R.id.password_chk);
        mPassword1 = (EditText) findViewById(R.id.pwd1);
        mPassword2 = (EditText) findViewById(R.id.pwd2);
        mKeyfile = (EditText) findViewById(R.id.keyfile);
        mSaveBtn = (Button) findViewById(R.id.save_btn);

        mUsePassword.setOnClickListener(this);
        mUseKeyfile.setOnClickListener(this);
        mKeyfile.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set_master_key, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String result = data.getStringExtra("result_path");
                result = result.replace(Environment.getExternalStorageDirectory().getAbsolutePath() , "");

                mKeyfile.setText(result);
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mUsePassword.getId()){
            if (mUsePassword.isChecked()){
                mPassword1.setVisibility(View.VISIBLE);
                mPassword2.setVisibility(View.VISIBLE);
                mPassword1.setText("");
                mPassword2.setText("");
            } else {
                mPassword1.setVisibility(View.GONE);
                mPassword2.setVisibility(View.GONE);

                if (!mUseKeyfile.isChecked()){
                    mUseKeyfile.performClick();
                }
            }
        }

        if (v.getId() == mUseKeyfile.getId()){
            if (mUseKeyfile.isChecked()){
                mKeyfile.setVisibility(View.VISIBLE);
                mKeyfile.setText("");
            } else {
                mKeyfile.setVisibility(View.GONE);

                if (!mUsePassword.isChecked()){
                    mUsePassword.performClick();
                }
            }
        }

        if (v.getId() == mKeyfile.getId()){
            Intent i = new Intent(this, FileBrowserActivity.class);
            i.putExtra("start_path", Environment.getExternalStorageDirectory().getAbsolutePath());
            startActivityForResult(i, 1);
        }

        if (v.getId() == mSaveBtn.getId()){

            String password = "";
            String keyfile = "";

            if (mUsePassword.isChecked()){
                String pwdTmp1 = mPassword1.getText().toString();
                String pwdTmp2 = mPassword2.getText().toString();
                if (!pwdTmp1.equals(pwdTmp1)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_passwords_not_equal), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pwdTmp1.length() == 0){
                    Toast.makeText(getApplicationContext(), getString(R.string.error_password_could_not_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                password = mPassword1.getText().toString();
            }
            if (mUseKeyfile.isChecked()){
                String tmpPath = Environment.getExternalStorageDirectory().getAbsolutePath() + mKeyfile.getText().toString();
                File f = new File(tmpPath);
                if (!f.exists()){
                    Toast.makeText(getApplicationContext(), getString(R.string.error_key_file_not_exists), Toast.LENGTH_SHORT).show();
                    return;
                }

                keyfile = tmpPath;
            }

            try {
                PasswordDatabase.getDatabase().setMasterKey(password, keyfile);
                PasswordDatabase.SaveDatabase();
            } catch (InvalidKeyFileException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_save_database), Toast.LENGTH_SHORT).show();
                return;
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_save_database), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), getString(R.string.label_master_key_changed), Toast.LENGTH_SHORT).show();
            finish();

        }
    }
}
