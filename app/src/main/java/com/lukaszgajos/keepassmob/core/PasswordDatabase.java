package com.lukaszgajos.keepassmob.core;

import com.keepassdroid.database.PwDatabase;
import com.keepassdroid.database.PwDatabaseV3;
import com.keepassdroid.database.PwEntry;
import com.keepassdroid.database.PwEntryV3;
import com.keepassdroid.database.PwEntryV4;
import com.keepassdroid.database.PwGroup;
import com.keepassdroid.database.PwGroupV3;
import com.keepassdroid.database.PwGroupV4;
import com.lukaszgajos.keepassdroidlibrary.KPDatabase;

import java.io.File;
import java.util.List;

/**
 * Created by pedros on 02.06.15.
 */
public class PasswordDatabase {

//    private static Loader database;
    private static KPDatabase db;
    private static boolean loadedDb = false;

    private static File databaseFile;


    public static void LoadDatabase(File dbFile, String password, File keyFile){
//        db = new KPDb(dbFile, password, keyFile);
        String dbFilename = dbFile.getAbsolutePath();
        String pwd = password == null ? "" : password;
        String key = keyFile == null ? "" : keyFile.getAbsolutePath();


        db = new KPDatabase(dbFilename, pwd, key);
        loadedDb = db.isCredencialCorrect();
        databaseFile = dbFile;
    }

    public static boolean SaveDatabase(){
        return SaveDatabase(databaseFile.getAbsolutePath());
    }

    public static boolean isLoadedDb(){
        return db != null;
    }

    public static boolean SaveDatabase(String filename){
        return db.saveDatabase(filename);
    }

    public static PwDatabase getDatabase(){
        return db.getDbInstance();
    }

    public static PwEntry getEmptyEntryInstance(PwGroup addToGroup){
        PwEntry entry;

        if (db.getDbInstance() instanceof PwDatabaseV3){
            entry = new PwEntryV3((PwGroupV3)addToGroup);
        } else {
            entry = new PwEntryV4((PwGroupV4)addToGroup);
        }

        db.getDbInstance().addEntryTo(entry, addToGroup);

        return entry;
    }

    public static boolean isCredentialsCorrect(){
        return loadedDb;
    }

    public static void close(){
        db = null;
    }

    public static List<PwGroup> getGroups(){
        return db.getGroups();
    }

    public static List<PwEntry> getEntries(){

        return db.getEntries();
    }

    public static PwEntry getByUuid(String uuid){
        for (PwEntry e : db.getEntries()){
            if (e.getUUID().toString().equals(uuid)){
                return e;
            }
        }

        return null;
    }

    public static void deleteEntry(PwEntry d){
        db.getDbInstance().deleteEntry(d);
    }

    public static PwGroup getByHashCode(int hashcode){
        for (PwGroup g : db.getGroups()){
            if (g.getId().hashCode() == hashcode){
                return g;
            }
        }
        return null;
    }


}
