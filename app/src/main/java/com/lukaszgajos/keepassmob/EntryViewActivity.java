package com.lukaszgajos.keepassmob;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.keepassdroid.database.PwEntry;
import com.lukaszgajos.keepassmob.core.ClipboardReceiver;
import com.lukaszgajos.keepassmob.core.KeepSession;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class EntryViewActivity extends AppCompatActivity implements View.OnClickListener {

    private PwEntry mEntry;
    private String mEntryUuid;

    private TextView mUsername;
    private TextView mPassword;
    private TextView mUrl;
    private TextView mNote;
    private TextView mCreation;
    private TextView mModification;
    private TextView mAccess;
    private TextView mExpiration;

    private ImageButton mToggleUsername;
    private ImageButton mTogglePassword;
    private ImageButton mEditEntry;

    private View mUsernameRow;
    private View mPasswordRow;
    private View mUrlRow;
    private View mNoteRow;

    private ActionBar mToolbar;

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mUsernameNotification;
    private NotificationCompat.Builder mPasswordNotification;

    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_view);

        mEntryUuid = getIntent().getExtras().getString("entry_uuid", "");
        if (mEntryUuid.length() == 0){
            finish();
        }
        mEntry = PasswordDatabase.getByUuid(mEntryUuid);
        if (mEntry == null){
            finish();
        }

        mToolbar = getSupportActionBar();
        mToolbar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mEntry.getTitle());

        mUsername = (TextView) findViewById(R.id.value_username);
        mPassword = (TextView) findViewById(R.id.value_password);
        mUrl = (TextView) findViewById(R.id.value_url);
        mNote = (TextView) findViewById(R.id.value_note);

        mUsernameRow = (View) findViewById(R.id.username_row);
        mPasswordRow = (View) findViewById(R.id.password_row);
        mUrlRow = (View) findViewById(R.id.url_row);
        mNoteRow = (View)findViewById(R.id.note_row);

        mCreation = (TextView) findViewById(R.id.value_creation);
        mModification = (TextView) findViewById(R.id.value_modification);
        mAccess = (TextView) findViewById(R.id.value_access);
        mExpiration = (TextView) findViewById(R.id.value_expiration);

        mToggleUsername = (ImageButton) findViewById(R.id.toggle_user_btn);
        mTogglePassword = (ImageButton) findViewById(R.id.toggle_pwd_btn);
        mEditEntry = (ImageButton) findViewById(R.id.edit_entry_btn);

        mToggleUsername.setOnClickListener(this);
        mTogglePassword.setOnClickListener(this);
        mEditEntry.setOnClickListener(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (mPref.getBoolean("show_username", false)){
            mUsername.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }

    }

    @Override
    public void onResume(){
        super.onResume();

        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(mPref, androidId);
        if (!session.isActive()){
            finish();
            return;
        }
        session.ping();

        mEntry = PasswordDatabase.getByUuid(mEntryUuid);

        mUsername.setText(mEntry.getUsername());
        mPassword.setText(mEntry.getPassword());
        mUrl.setText(mEntry.getUrl());
        mNote.setText(mEntry.getNotes());

        mCreation.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEntry.getCreationTime()));
        mModification.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEntry.getLastModificationTime()));
        mAccess.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEntry.getLastAccessTime()));
        mExpiration.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(mEntry.getExpiryTime()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(mEntry.getExpiryTime());
        if (cal.get(Calendar.YEAR) > 2100){
            mExpiration.setText(getText(R.string.never));
        }

        if (mEntry.getUsername().length() == 0) mUsernameRow.setVisibility(View.GONE); else mUsernameRow.setVisibility(View.VISIBLE);
        if (mEntry.getPassword().length() == 0) mPasswordRow.setVisibility(View.GONE); else mPasswordRow.setVisibility(View.VISIBLE);
        if (mEntry.getUrl().length() == 0) mUrlRow.setVisibility(View.GONE); else mUrlRow.setVisibility(View.VISIBLE);
        if (mEntry.getNotes().length() == 0) mNoteRow.setVisibility(View.GONE); else mNoteRow.setVisibility(View.VISIBLE);

        if (mPref.getBoolean("use_clipboard", true)){
            showUserPassClipboardNotifications();
        }
    }

    private void showUserPassClipboardNotifications(){
        if (mEntry.getPassword().length() > 0){
            mPasswordNotification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_lock_indigo_50_24dp)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notofication_copy_password));

            final int requestIDPwd = (int) System.currentTimeMillis();

            Intent passNotificationIntent = new Intent("com.lukaszgajos.COPY_PASSWORD");
            PendingIntent passNotification = PendingIntent.getBroadcast(this, requestIDPwd, passNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mPasswordNotification.setContentIntent(passNotification);

            notificationManager.notify(requestIDPwd, mPasswordNotification.build());

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(60000);
                        notificationManager.cancel(requestIDPwd);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }

        if (mEntry.getUsername().length() > 0){
            mUsernameNotification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_lock_indigo_50_24dp)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_copy_username));

            final int requestID = (int) System.currentTimeMillis();

            Intent userNotificationIntent = new Intent("com.lukaszgajos.COPY_USERNAME");
            PendingIntent userNotification = PendingIntent.getBroadcast(this, requestID, userNotificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            mUsernameNotification.setContentIntent(userNotification);

            notificationManager.notify(requestID, mUsernameNotification.build());

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(60000);
                        notificationManager.cancel(requestID);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.lukaszgajos.COPY_USERNAME");
        filter.addAction("com.lukaszgajos.COPY_PASSWORD");
        registerReceiver(new ClipboardReceiver(mEntry), filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_entry_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            showConfirmDeleteDialog();
            return true;
        }
        if (id == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mToggleUsername.getId()){
            int variation = (mUsername.getInputType() & InputType.TYPE_MASK_VARIATION);
            boolean isPassword = ((variation  == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                    ||(variation  == InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    ||(variation  == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD));
            if ( isPassword ){
                mUsername.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mUsername.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

        if (v.getId() == mTogglePassword.getId()){
            int variation = (mPassword.getInputType() & InputType.TYPE_MASK_VARIATION);
            boolean isPassword = ((variation  == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                    ||(variation  == InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    ||(variation  == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD));
            if ( isPassword ){
                mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }

        if (v.getId() == R.id.edit_entry_btn){
            Intent i = new Intent(this, EntryEditActivity.class);
            i.putExtra("entry_uuid", mEntryUuid);
            startActivity(i);
        }
    }

    private void showConfirmDeleteDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_confirm_title))
                .setMessage(getString(R.string.delete_confirm_msg))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PasswordDatabase.deleteEntry(mEntry);
                        PasswordDatabase.SaveDatabase();
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
