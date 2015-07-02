package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class FileBrowserAdapter extends ArrayAdapter<File> {

    private final Context context;
    private final File[] values;

    public FileBrowserAdapter(Context context, File[] values){
        super(context, -1, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_browser_row, parent, false);

        ImageView icon = (ImageView) rowView.findViewById(R.id.file_browser_row_icon);
        TextView label = (TextView) rowView.findViewById(R.id.file_browser_row_label);
        label.setText(values[position].getName());
        if (values[position].isDirectory()){
            icon.setImageResource(R.drawable.ic_folder_indigo_600_24dp);
        } else {
            icon.setVisibility(View.GONE);
        }

        return rowView;
    }
}
