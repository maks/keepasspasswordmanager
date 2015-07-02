package com.lukaszgajos.keepassmob;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.keepassdroid.database.PwDatabase;
import com.keepassdroid.database.PwEntry;
import com.keepassdroid.database.PwGroup;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.util.Date;
import java.util.List;
import java.util.Random;


public class EntryEditActivity extends ActionBarActivity implements View.OnClickListener {

    private String mEntryUuid;
    private PwGroup mEntryGroup;
    private PwGroup[] mGroupTab;
    private List<PwGroup> groupList;
    private PwEntry mEntry;


    private EditText mTitle;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mUrl;
    private EditText mNote;

//    private Button mSaveBtn;
//    private Button mCancelBtn;
    private ImageButton mPwdGeneratorBtn;
    private Spinner mGroupSpinner;

//    private Toolbar mToolbar;
    private ActionBar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_edit);

//        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar = getSupportActionBar();
        mToolbar.setDisplayHomeAsUpEnabled(true);

        groupList = PasswordDatabase.getGroups();
        mGroupSpinner = (Spinner) findViewById(R.id.group_spinner);
        mGroupTab = new PwGroup[groupList.size()];
        groupList.toArray(mGroupTab);

        GroupAdapter adapter = new GroupAdapter(getApplicationContext(), mGroupTab);
        mGroupSpinner.setAdapter(adapter);
        mGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEntryGroup = (PwGroup) mGroupSpinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        mGroupSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mEntryGroup = (PwGroup) mGroupSpinner.getSelectedItem();
//            }
//        });


        mTitle = (EditText) findViewById(R.id.value_title);
        mUsername = (EditText) findViewById(R.id.value_username);
        mPassword = (EditText) findViewById(R.id.value_password);
        mUrl = (EditText) findViewById(R.id.value_url);
        mNote = (EditText) findViewById(R.id.value_note);

//        mSaveBtn = (Button) findViewById(R.id.save_btn);
//        mCancelBtn = (Button) findViewById(R.id.cancel_btn);
        mPwdGeneratorBtn = (ImageButton) findViewById(R.id.generate_pwd_btn);

//        mSaveBtn.setOnClickListener(this);
//        mCancelBtn.setOnClickListener(this);
        mPwdGeneratorBtn.setOnClickListener(this);

        mEntryUuid = getIntent().getExtras().getString("entry_uuid", "");
        if (mEntryUuid.length() > 0){
            mEntry = PasswordDatabase.getByUuid(mEntryUuid);
            if (mEntry == null){
                finish();
            }
            mEntryGroup = mEntry.getParent();
            mGroupSpinner.setSelection(groupList.indexOf(mEntry.getParent()));

            mTitle.setText(mEntry.getTitle());
            mUsername.setText(mEntry.getUsername());
            mPassword.setText(mEntry.getPassword());
            mUrl.setText(mEntry.getUrl());
            mNote.setText(mEntry.getNotes());

            getSupportActionBar().setTitle(getString(R.string.title_activity_entry_edit));

        } else {
            int groupHashcode = getIntent().getExtras().getInt("group_hashcode", 0);
            if (groupHashcode != 0){
                mEntryGroup = PasswordDatabase.getByHashCode(groupHashcode);
            }
            if (mEntryGroup == null){
                finish();
            }
            mGroupSpinner.setSelection(groupList.indexOf(mEntryGroup));

            getSupportActionBar().setTitle(getString(R.string.title_activity_entry_add));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entry_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home){
            finish();
            return true;
        }

        if (id == R.id.action_save){
            String title = mTitle.getText().toString().trim();
            if (title.length() == 0){
                Toast.makeText(getApplicationContext(), getString(R.string.error_title_could_not_be_empty), Toast.LENGTH_LONG).show();
                return true;
            }

            String username = mUsername.getText().toString().trim();
            String passwd = mPassword.getText().toString().trim();
            String url = mUrl.getText().toString().trim();
            String notes = mNote.getText().toString().trim();

            PwDatabase db = PasswordDatabase.getDatabase();

            if (mEntryUuid.length() == 0){
                mEntry = PasswordDatabase.getEmptyEntryInstance(mEntryGroup);
            } else {
                mEntry.setParent(mEntryGroup);
                mEntry.setLastModificationTime(new Date());
            }

            mEntry.setTitle(title, db);
            mEntry.setUsername(username, db);
            mEntry.setPassword(passwd, db);
            mEntry.setUrl(url, db);
            mEntry.setNotes(notes, db);



            if (PasswordDatabase.SaveDatabase()){
                Toast.makeText(getApplicationContext(), getString(R.string.entry_saved), Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_could_not_save_database), Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == mCancelBtn.getId()){
//            finish();
//        }
//
//        if (v.getId() == mSaveBtn.getId()){
//
//
//        }

        if (v.getId() == mPwdGeneratorBtn.getId()){
            showPasswordGeneratePopup(this);
        }
    }

    private String generatePassword(int length, boolean upper, boolean lower, boolean numbers, boolean special){
//        string valid = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        String lowerCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbersCharset = "1234567890";
        String specialCharset = "!@#$%^&*()-_=+<>?;:|";
        String alphabet = "";
        if (upper) alphabet += upperCharset;
        if (lower) alphabet += lowerCharset;
        if (numbers) alphabet += numbersCharset;
        if (special) alphabet += specialCharset;


        final int N = alphabet.length();
        Random rd = new Random();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(rd.nextInt(N)));
        }

        return sb.toString();
    }

    private void showPasswordGeneratePopup(final Activity context){

        final Dialog dg = new Dialog(context);
        dg.setContentView(R.layout.password_generator_popup);
        dg.setTitle(getString(R.string.label_password_generator));

        Button cancelBtn = (Button) dg.findViewById(R.id.cancel_btn);
        Button generateBtn = (Button) dg.findViewById(R.id.generate_btn);
        final SeekBar pwdLength = (SeekBar) dg.findViewById(R.id.pwd_length);
        final CheckBox upperChk = (CheckBox) dg.findViewById(R.id.upper_chk);
        final CheckBox lowerChk = (CheckBox) dg.findViewById(R.id.lower_chk);
        final CheckBox numbersChk = (CheckBox) dg.findViewById(R.id.numbers_chk);
        final CheckBox specialChk = (CheckBox) dg.findViewById(R.id.special_chk);
        final TextView lengthLabel = (TextView) dg.findViewById(R.id.length_label);
        lengthLabel.setText(String.format(getString(R.string.label_password_length), pwdLength.getProgress()));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (pref.getBoolean("use_lower_charset", true)) lowerChk.setChecked(true);
        if (pref.getBoolean("use_upper_charset", true)) upperChk.setChecked(true);
        if (pref.getBoolean("use_numbers_charset", true)) numbersChk.setChecked(true);
        if (pref.getBoolean("use_special_charset", true)) specialChk.setChecked(true);


        pwdLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1){
                    seekBar.setProgress(1);
                }
                lengthLabel.setText(String.format(getString(R.string.label_password_length), pwdLength.getProgress()));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.dismiss();
            }
        });

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPassword.setText(generatePassword(pwdLength.getProgress(), upperChk.isChecked(), lowerChk.isChecked(), numbersChk.isChecked(), specialChk.isChecked()));
                dg.dismiss();
            }
        });

        dg.show();


    }
}
