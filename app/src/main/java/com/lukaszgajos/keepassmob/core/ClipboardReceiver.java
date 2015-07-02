package com.lukaszgajos.keepassmob.core;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.keepassdroid.database.PwEntry;
import com.lukaszgajos.keepassmob.R;

/**
 * Created by pedros on 28.06.15.
 */
public class ClipboardReceiver extends BroadcastReceiver {

    PwEntry entry;
    public ClipboardReceiver(PwEntry entry){
        this.entry = entry;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip;
        if (intent.getAction().equals("com.lukaszgajos.COPY_USERNAME")){
            clip = ClipData.newPlainText("PWD", entry.getUsername());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.username_in_clipboard), Toast.LENGTH_SHORT).show();
        }
        if (intent.getAction().equals("com.lukaszgajos.COPY_PASSWORD")){
            clip = ClipData.newPlainText("PWD", entry.getPassword());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.password_in_clipboard), Toast.LENGTH_SHORT).show();
        }
    }
}
