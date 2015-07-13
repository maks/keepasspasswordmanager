package com.lukaszgajos.keepassmob.core;

import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;

/**
 * Created by pedros on 07.06.15.
 */
public class KeepSession {

    private SharedPreferences pref;
    private String key;
    private Crypter crypter;

    private static String DB_KEY = "db_file_path";
    private static String PWD_KEY = "db_pwd_value";
    private static String KEY_KEY = "db_key_path";
    private static String TS_KEY = "db_ts_key";
    public static String TIMEOUT_KEY = "autolock_timeout";



    public KeepSession(SharedPreferences pref, String key){
        this.pref = pref;
        this.key = key;

        crypter = new Crypter(key);
    }

    public void clear(){
        SharedPreferences.Editor editor = pref.edit();

        editor.remove(DB_KEY);
        editor.remove(PWD_KEY);
        editor.remove(KEY_KEY);
        editor.remove(TS_KEY);

        editor.commit();
    }

    public void ping(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(TS_KEY, System.currentTimeMillis());
        editor.commit();
    }

    public boolean isActive(){

        long saveTs = pref.getLong(TS_KEY, 0);
        if (saveTs == 0){
            return false;
        }

        long currentTs = System.currentTimeMillis();
        String tmp = pref.getString(KeepSession.TIMEOUT_KEY, "600");
        if (tmp == null) tmp = "600";
        int sessionTimeout = Integer.parseInt(tmp);

        if (currentTs - saveTs > sessionTimeout * 1000){
            return false;
        }

        return true;
    }

    public void storeDatabase(String path){
//        String path = f.getAbsolutePath();
        path = crypter.encrypt(path);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KeepSession.DB_KEY, path);
        editor.putLong(TS_KEY, System.currentTimeMillis());
        editor.commit();
    }

    public void storePassword(String pwd){
        pwd = crypter.encrypt(pwd);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KeepSession.PWD_KEY, pwd);
        editor.commit();
    }

    public void storeKey(String path){
//        String path = f.getAbsolutePath();
        path = crypter.encrypt(path);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KeepSession.KEY_KEY, path);
        editor.commit();
    }

    public Uri getDatabase(){
        String filePath = pref.getString(DB_KEY, "");

        if (filePath.length() > 0){
            filePath = crypter.decrypt(filePath);

            return Uri.parse(filePath);
        }
        return null;
    }

    public String getPassword(){
        String pwd = pref.getString(PWD_KEY, "");
        if (pwd.length() > 0){
            return crypter.decrypt(pwd);
        }

        return null;
    }

    public Uri getKey(){
        String keyFile = pref.getString(KEY_KEY, "");
        if (keyFile.length() > 0){
            keyFile = crypter.decrypt(keyFile);

            return Uri.parse(keyFile);
        }
        return null;
    }
}
