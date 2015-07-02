package com.lukaszgajos.keepassmob;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.keepassdroid.database.PwEntry;
import com.keepassdroid.database.PwGroup;
import com.lukaszgajos.keepassmob.core.KeepSession;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    private Toolbar mToolbar;
    private ActionBar mToolbar;
    private ListView mCategoryList;
    private ListView mPasswordEntryList;
    private PwGroup[] mGroupTab;
    private PwEntry[] mPasswordEntryTab;
    private PwEntry[] mPasswordEntryFiltered;
    private PwGroup mCurrentGroup;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SharedPreferences mPref;

    private boolean mSearchOpened = false;
    private TextView mSearchQ;
    private MenuItem mSearchBtn;

    private ImageButton mAddEntryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PasswordDatabase.isLoadedDb()){
            finish();
        }
        mPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        mCategoryList = (ListView) findViewById(R.id.category_list);
        mPasswordEntryList = (ListView) findViewById(R.id.pw_entry_list);
        mSearchQ = (TextView) findViewById(R.id.search_q);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToolbar = getSupportActionBar();
        mToolbar.setHomeButtonEnabled(true);
        mToolbar.setDisplayHomeAsUpEnabled(true);

        mToolbar.setTitle(R.string.app_title);

        mToolbar.setHomeAsUpIndicator(R.drawable.ic_menu_indigo_50_24dp);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open,R.string.close){

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        List<PwGroup> group = PasswordDatabase.getGroups();
        if (group.size() > 0){
            mCurrentGroup = group.get(0);
        }

        mGroupTab = new PwGroup[group.size()];
        int counter = 0;
        for (PwGroup g : group){
            mGroupTab[counter] = g;
            counter++;
        }

        GroupAdapter adapter = new GroupAdapter(this, mGroupTab);
        mCategoryList.setAdapter(adapter);
        mCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mGroupTab.length) {
                    mCurrentGroup = mGroupTab[position];
                    updatePasswordListForGroup();

                }
                mDrawerLayout.closeDrawers();
            }
        });

        mAddEntryBtn = (ImageButton) findViewById(R.id.add_entry_btn);
        mAddEntryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EntryEditActivity.class);
                i.putExtra("group_hashcode", mCurrentGroup.getId().hashCode());
                startActivity(i);
            }
        });

        Button settingsBtn = (Button)mDrawerLayout.findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);

                mDrawerLayout.closeDrawers();
            }
        });
        Button changeMasterKeyBtn = (Button)mDrawerLayout.findViewById(R.id.change_master_key_btn);
        changeMasterKeyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SetMasterKeyActivity.class);
                startActivity(i);

                mDrawerLayout.closeDrawers();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        KeepSession session = new KeepSession(mPref, androidId);
        if (!session.isActive()){
            finish();
            return;
        }
        session.ping();

        mPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        updatePasswordListForGroup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSearchBtn = menu.findItem(R.id.action_search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_lock) {
            PasswordDatabase.close();

            String androidId = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            KeepSession session = new KeepSession(mPref, androidId);
            session.clear();

            finish();
        }
        if (id == R.id.action_search){
            if (mSearchOpened){
                closeSearchBar();
            } else {
                openSearchBar();
            }
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePasswordListForGroup(){

        mToolbar.setSubtitle(mCurrentGroup.getName());

        boolean searchInUser = mPref.getBoolean("search_username", false);
        boolean searchInUrl = mPref.getBoolean("search_url", false);
        boolean searchInNotes = mPref.getBoolean("search_notes", false);

        ArrayList<PwEntry> tmp = new ArrayList<>();
        for (PwEntry e : PasswordDatabase.getEntries()){

            if (e.getParent().getId().hashCode() == mCurrentGroup.getId().hashCode()){
                if (mSearchOpened){

                    if (e.getTitle().toLowerCase().contains(mSearchQ.getText())){
                        tmp.add(e);
                    } else if (searchInUser && e.getUsername().toLowerCase().contains(mSearchQ.getText())){
                        tmp.add(e);
                    } else if (searchInUrl && e.getUrl().toLowerCase().contains(mSearchQ.getText())){
                        tmp.add(e);
                    } else if (searchInNotes && e.getNotes().toLowerCase().contains(mSearchQ.getText())){
                        tmp.add(e);
                    }
                } else {
                    tmp.add(e);
                }
            }
        }

        Collections.sort(tmp, new Comparator<PwEntry>() {
            @Override
            public int compare(PwEntry lhs, PwEntry rhs) {
                return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
            }
        });

        mPasswordEntryTab = tmp.toArray(new PwEntry[tmp.size()]);
        PasswordAdapter adapter = new PasswordAdapter(this, mPasswordEntryTab);
        mPasswordEntryList.setAdapter(adapter);
        mPasswordEntryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent entryView = new Intent(MainActivity.this, EntryViewActivity.class);
                entryView.putExtra("entry_uuid", mPasswordEntryTab[position].getUUID().toString());
                startActivity(entryView);
            }
        });
    }

    private void openSearchBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search_bar);

        mSearchQ = (EditText) findViewById(R.id.search_q);
        mSearchQ.addTextChangedListener(new SearchInputWatcher());
        mSearchQ.requestFocus();

        mSearchBtn.setIcon(getResources().getDrawable(R.drawable.ic_clear_indigo_50_24dp));
        mSearchOpened = true;

        InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(mSearchQ.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    private void closeSearchBar() {
        getSupportActionBar().setDisplayShowCustomEnabled(false);

        mSearchBtn.setIcon(getResources().getDrawable(R.drawable.ic_search_indigo_50_24dp));
        mSearchOpened = false;

        InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(mSearchQ.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        mSearchQ.setText("");
    }


    private class SearchInputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updatePasswordListForGroup();
        }
    }
}
