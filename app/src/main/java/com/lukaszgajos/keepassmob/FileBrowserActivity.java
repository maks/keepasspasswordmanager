package com.lukaszgajos.keepassmob;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;


public class FileBrowserActivity extends AppCompatActivity {

    String startPath;
//    int returnInputId;

    ListView fileList;
    TextView currentPath;
    MenuItem mBackButton;

    File currentFile;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        startPath = getIntent().getExtras().getString("start_path");
//        returnInputId = getIntent().getExtras().getInt("back_input_id");

        fileList = (ListView) findViewById(R.id.file_list);
        currentPath = (TextView) findViewById(R.id.current_path);
        getFileList(startPath);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_browser, menu);
        mBackButton = menu.findItem(R.id.action_back);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_back) {
            String parentPath = currentFile.getParentFile().getAbsolutePath();
            getFileList(parentPath);
        }
        if (id == R.id.action_cancel){
            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFileList(String path){

        String toShowPath = path.replace(Environment.getExternalStorageDirectory().getAbsolutePath() , "");
        if (toShowPath.length() == 0){
            toShowPath = "/";
        }
        currentPath.setText(toShowPath);
        if (mBackButton != null){
            if (toShowPath.equals("/")){
                mBackButton.setVisible(false);
            } else {
                mBackButton.setVisible(true);
            }
        }

        currentFile = new File(path);
        File fileTab[] = currentFile.listFiles();
        Arrays.sort(fileTab);

        FileBrowserAdapter adapter = new FileBrowserAdapter(this, fileTab);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File pickedFile = (File) parent.getItemAtPosition(position);
                if (pickedFile.isDirectory()){
                    getFileList(pickedFile.getAbsolutePath());
                } else {
                    returnPickedFile(pickedFile.getAbsolutePath());
                }
            }
        });
    }

    private void returnPickedFile(String path){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("result_path", path);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
