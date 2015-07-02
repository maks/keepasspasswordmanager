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

//import com.lukaszgajos.keepassmob.core.Loader;


/**
 * Created by pedros on 01.06.15.
 */
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




//        getWindow().getWindow().getAttributes().token;
//
//
//        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        im.getCurrentInputMethodSubtype();
//        im.setInputMethodAndSubtype();
//        InputMethodSubtype s = im.getCurrentInputMethodSubtype();
//        im.setCurrentInputMethodSubtype(s);

//        List<InputMethodInfo> m = im.getEnabledInputMethodList();
//        List<InputMethodInfo> m2 = im.getInputMethodList();

//        im.setCurrentInputMethodSubtype();
//
//        im.showInputMethodPicker();

        return keyboardView;


//        Button btn = (Button) v.findViewById(R.id.button2);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                InputConnection ic = getCurrentInputConnection();
//                ic.commitText("BUTTON",1);
//
//
//
//            }
//        });

//        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
//        keyboard = new Keyboard(this, R.xml.qwerty);
//        kv.setKeyboard(keyboard);
//        kv.setOnKeyboardActionListener(this);

//        return kv;

    }

    public KeepSession getKeepSession(){
        return mSession;
    }


    @Override
    public void onStartInputView (EditorInfo attribute, boolean restarting){

        if (mAndroidId == null || mPref == null){
            return;
        }

//        mSession = new KeepSession(mPref, mAndroidId);
        if (!mSession.isActive()){
            showDbLocked();
            return;
        }

        File databaseFile = mSession.getDatabase();

        String password = mSession.getPassword();
        File keyFile = mSession.getKey();
//        Loader l = new Loader(databaseFile);
//        if (password != null && password instanceof String){
//            l.setPassword(password);
//        }
//        if (keyFile != null && keyFile instanceof File){
//            l.setKeyFile(keyFile);
//        }

//        PasswordDatabase.LoadDatabase(l);


//        File dbPath = session.getDatabase();
//        String pwd = session.getPassword();
//        File keyPath = session.getKey();
//        if (dbPath == null || !dbPath.exists()){
//            return;
//        }
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
