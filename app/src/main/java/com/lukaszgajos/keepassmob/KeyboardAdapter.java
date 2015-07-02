package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.keepassdroid.database.PwEntry;
import com.keepassdroid.database.PwGroup;
import com.lukaszgajos.keepassmob.core.PasswordDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KeyboardAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private Context mContext;
    private LayoutInflater inflater;

    private PwGroup mCurrentGroup;
    private PwEntry mCurrentEntry;
    private PwGroup[] mCurrentGroupList;
    private PwEntry[] mCurrentEntryList;
    private View mCurrentEntryDetails;


    private ListView groupList;
    private ListView entryList;

    private ViewPager mPager;

    private PasswdKeyboard mKeyboardInstance;



    public KeyboardAdapter(Context context, PasswdKeyboard kybrd){
        mContext = context;
        mKeyboardInstance = kybrd;


        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public boolean isGroupSet(){
        return mCurrentGroup != null;
    }

    public boolean isEntrySet(){
        return mCurrentEntry != null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View res = null;
        if (position == 0){
            res = getStep1Layout(container);
        }
        if (position == 1){
            res = getStep2Layout(container);
        }
        if (position == 2){
            res = getStep3Layout(container);
        }

        ((ViewPager) container).addView(res);
        if (mPager == null){
            mPager = (ViewPager) container;
            mPager.setOnPageChangeListener(this);
        }

        return res;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }

    private View getStep1Layout(final ViewGroup container){
        final View res = inflater.inflate(R.layout.keyboard_step1, container, false);

        ImageButton hideBtn = (ImageButton) res.findViewById(R.id.hide_kybrd_btn);
        ImageButton startAppBtn = (ImageButton) res.findViewById(R.id.start_app_btn);
        ImageButton lockDbBtn = (ImageButton) res.findViewById(R.id.lock_db_btn);
        hideBtn.setOnClickListener(this);
        startAppBtn.setOnClickListener(this);
        lockDbBtn.setOnClickListener(this);

        groupList = (ListView)res.findViewById(R.id.category_list);

        refreshGroupList();

        return res;
    }

    private View getStep2Layout(final ViewGroup container){
        View res = inflater.inflate(R.layout.keyboard_step2, container, false);

        entryList = (ListView) res.findViewById(R.id.pwd_entry_list);
        refreshEntriesList();

        ImageButton hideBtn = (ImageButton) res.findViewById(R.id.hide_kybrd_btn);
        ImageButton startAppBtn = (ImageButton) res.findViewById(R.id.start_app_btn);
        ImageButton lockDbBtn = (ImageButton) res.findViewById(R.id.lock_db_btn);
        hideBtn.setOnClickListener(this);
        startAppBtn.setOnClickListener(this);
        lockDbBtn.setOnClickListener(this);

        return res;
    }

    private View getStep3Layout(final ViewGroup container){
        mCurrentEntryDetails = inflater.inflate(R.layout.keyboard_step3, container, false);
        refreshEntryDetails();
        return mCurrentEntryDetails;
    }

    private void refreshGroupList(){
        List<PwGroup> group = PasswordDatabase.getGroups();
        mCurrentGroupList = group.toArray(new PwGroup[group.size()]);

        KeyboardGroupAdapter adapter = new KeyboardGroupAdapter(mContext, mCurrentGroupList);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentGroup = mCurrentGroupList[position];

                mPager.setCurrentItem(1);
            }
        });

        groupList.setAdapter(adapter);


    }

    private void refreshEntriesList(){
        ArrayList<PwEntry> tmp = new ArrayList<>();
        for (PwEntry e : PasswordDatabase.getEntries()){
            if (mCurrentGroup != null){
                if (e.getParent().getId().hashCode() == mCurrentGroup.getId().hashCode()){
                    tmp.add(e);
                }
            } else {
                tmp.add(e);
            }
        }
        Collections.sort(tmp, new Comparator<PwEntry>() {
            @Override
            public int compare(PwEntry lhs, PwEntry rhs) {
                return lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());
            }
        });

        mCurrentEntryList = tmp.toArray(new PwEntry[tmp.size()]);

        KeyboardPasswordAdapter adapter = new KeyboardPasswordAdapter(mContext, mCurrentEntryList);
        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentEntry = mCurrentEntryList[position];
                mPager.setCurrentItem(2);
            }
        });
        entryList.setAdapter(adapter);
    }

    private void refreshEntryDetails(){

        if (mCurrentEntry == null){
            return;
        }

        TextView entryName = (TextView) mCurrentEntryDetails.findViewById(R.id.separator_letter);
        TextView entryNotes = (TextView) mCurrentEntryDetails.findViewById(R.id.entry_notes);
        ImageButton pasteUsername = (ImageButton)mCurrentEntryDetails.findViewById(R.id.paste_username);
        ImageButton pastePassword = (ImageButton) mCurrentEntryDetails.findViewById(R.id.paste_password);
        ImageButton pasteUrl = (ImageButton) mCurrentEntryDetails.findViewById(R.id.paste_url);
        ImageButton hideBtn = (ImageButton) mCurrentEntryDetails.findViewById(R.id.hide_kybrd_btn);
        ImageButton startAppBtn = (ImageButton) mCurrentEntryDetails.findViewById(R.id.start_app_btn);
        ImageButton lockDbBtn = (ImageButton) mCurrentEntryDetails.findViewById(R.id.lock_db_btn);

        if (mCurrentEntry.getUsername().length() == 0){
            pasteUsername.setVisibility(View.GONE);
        } else {
            pasteUsername.setVisibility(View.VISIBLE);
        }
        if (mCurrentEntry.getPassword().length() == 0){
            pastePassword.setVisibility(View.GONE);
        } else {
            pasteUsername.setVisibility(View.VISIBLE);
        }
        if (mCurrentEntry.getUrl().length() == 0){
            pasteUrl.setVisibility(View.GONE);
        } else {
            pasteUrl.setVisibility(View.VISIBLE);
        }
        if (mCurrentEntry.getNotes().length() == 0){
            entryNotes.setVisibility(View.GONE);
        } else {
            entryNotes.setVisibility(View.VISIBLE);
        }

        entryName.setText(mCurrentEntry.getTitle());
        entryNotes.setText(mCurrentEntry.getNotes());
        pasteUsername.setOnClickListener(this);
        pastePassword.setOnClickListener(this);
        pasteUrl.setOnClickListener(this);
        hideBtn.setOnClickListener(this);
        startAppBtn.setOnClickListener(this);
        lockDbBtn.setOnClickListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if (!mKeyboardInstance.getKeepSession().isActive()){
            mKeyboardInstance.showDbLocked();
        }

        if (position == 0){
            refreshGroupList();
        }
        if (position == 1){
            refreshEntriesList();
        }
        if (position == 2){
            if (mCurrentEntry == null){
                mPager.setCurrentItem(1);
            } else {
                refreshEntryDetails();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        InputConnection ic = mKeyboardInstance.getCurrentInputConnection();

        if (R.id.paste_username == v.getId()){
            ic.commitText(mCurrentEntry.getUsername(), 1);
        } else if (R.id.paste_password == v.getId()){
            ic.commitText(mCurrentEntry.getPassword(), 1);
        } else if (R.id.paste_url == v.getId()){
            ic.commitText(mCurrentEntry.getUrl(), 1);
        } else if (R.id.hide_kybrd_btn == v.getId()){

            InputMethodManager im = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

            final IBinder token = mKeyboardInstance.getWindow().getWindow().getAttributes().token;
            try{
                im.switchToLastInputMethod(token);
            } catch (Throwable t){
                InputMethodSubtype last = im.getLastInputMethodSubtype();
                InputMethodInfo lastMethodInfo = null;

                List<InputMethodInfo> enabledInputMethodList = im.getEnabledInputMethodList();

                for (InputMethodInfo inputInfo : enabledInputMethodList){
                    boolean found = false;
                    for (int i = 0; i < inputInfo.getSubtypeCount(); i++){
                        InputMethodSubtype subtype = inputInfo.getSubtypeAt(i);
                        if (subtype.getMode().equals(last.getMode()) && subtype.getLocale().equals(last.getLocale())){
                            lastMethodInfo = inputInfo;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }

                if (lastMethodInfo == null){
                    return;
                }

                im.setInputMethodAndSubtype(mKeyboardInstance.getWindow().getWindow().getAttributes().token, lastMethodInfo.getId(), last);
            }

        } else if (R.id.start_app_btn == v.getId()){
            Intent openDbIntent = new Intent();
            openDbIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            openDbIntent.setClassName(mContext.getPackageName(), MainActivity.class.getName());
            mContext.startActivity(openDbIntent);

        } else if (R.id.lock_db_btn == v.getId()){

            mKeyboardInstance.getKeepSession().clear();
            mKeyboardInstance.showDbLocked();
        }

    }
}
