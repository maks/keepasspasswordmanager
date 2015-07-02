package com.lukaszgajos.keepassmob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.lukaszgajos.keepassmob.core.KeepSession;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.io.File;

public class PasswdKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private boolean caps = false;

    private ViewPager mPager;
    private View keyboardView;

    private SharedPreferences mPref;
    private String mAndroidId;
    private KeepSession mSession;

    private KeyboardAdapter mKeyboardAdapter;

    @Override
    public View onCreateInputView() {
        mAndroidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        mPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        keyboardView = (RelativeLayout)getLayoutInflater().inflate(R.layout.keyboard, null);

        mKeyboardAdapter = new KeyboardAdapter(getBaseContext(), this);

        mSession = new KeepSession(mPref, mAndroidId);


        return keyboardView;
    }

    public KeepSession getKeepSession(){
        return mSession;
    }


    @Override
    public void onStartInputView (EditorInfo attribute, boolean restarting){

        if (mAndroidId == null || mPref == null){
            return;
        }

        if (!mSession.isActive()){
            showDbLocked();
            return;
        }

        File databaseFile = mSession.getDatabase();

        String password = mSession.getPassword();
        File keyFile = mSession.getKey();

        if (databaseFile == null || !databaseFile.exists()){
            showDbLocked();
            return;
        }
        PasswordDatabase.LoadDatabase(databaseFile, password, keyFile);
        if (!PasswordDatabase.isCredentialsCorrect()){
            mSession.clear();
            showDbLocked();
            return;
        }

        mPager = (ViewPager) keyboardView.findViewById(R.id.viewpager);
        mPager.setAdapter(mKeyboardAdapter);
        if (mKeyboardAdapter.isEntrySet()){
            mPager.setCurrentItem(2);
        }
        else if (mKeyboardAdapter.isGroupSet()){
            mPager.setCurrentItem(1);
        }
        

        showDbUnlocked();
    }

    public void showDbLocked(){
        ViewPager p = (ViewPager) keyboardView.findViewById(R.id.viewpager);
        RelativeLayout locked = (RelativeLayout) keyboardView.findViewById(R.id.db_locked);

        p.setVisibility(View.INVISIBLE);
        locked.setVisibility(View.VISIBLE);

        Button unlockDb = (Button) locked.findViewById(R.id.button);
        unlockDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openDbIntent = new Intent();
                openDbIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openDbIntent.putExtra("finish_after_open", true);

                openDbIntent.setClassName(getApplicationContext().getPackageName(), LoginActivity.class.getName());
                startActivity(openDbIntent);
            }
        });
    }

    public void showDbUnlocked(){
        ViewPager p = (ViewPager) keyboardView.findViewById(R.id.viewpager);
        RelativeLayout locked = (RelativeLayout) keyboardView.findViewById(R.id.db_locked);

        p.setVisibility(View.VISIBLE);
        locked.setVisibility(View.INVISIBLE);

    }
    


    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

}
