package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keepassdroid.database.PwGroup;

public class FileSourceAdapter extends ArrayAdapter<Integer> {
    private final Context context;
    private final Integer[] values;

    public static int LOCAL = 0;
    public static int CLOUD = 1;

    public static Integer[] getAvailableSource(){
        Integer[] data = new Integer[2];

        data[0] = LOCAL;
        data[1] = CLOUD;

        return data;
    }

    public FileSourceAdapter(Context context, Integer[] values){
        super(context, -1, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.file_source_row, parent, false);

        ImageView icon = (ImageView) rowView.findViewById(R.id.source_icon);
        Integer grp = values[position];
        if (grp.intValue() == LOCAL){
            icon.setImageResource(R.drawable.ic_folder_open_grey_900_24dp);
        } else if (grp.intValue() == CLOUD){
            icon.setImageResource(R.drawable.ic_cloud_queue_grey_900_24dp);
        }

        return rowView;
    }
}
