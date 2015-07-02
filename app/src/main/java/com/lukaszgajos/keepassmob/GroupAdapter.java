package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keepassdroid.database.PwGroup;
//import com.lukaszgajos.keepassmob.core.PasswordGroup;


/**
 * Created by pedros on 04.06.15.
 */
public class GroupAdapter extends ArrayAdapter<PwGroup> {
    private final Context context;
    private final PwGroup[] values;

    public GroupAdapter(Context context, PwGroup[] values){
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
        View rowView = inflater.inflate(R.layout.group_row, parent, false);


        TextView label = (TextView) rowView.findViewById(R.id.group_name_label);
        PwGroup grp = values[position];

        int level = 0;
        PwGroup parentGroup = grp.getParent();
        do{
            if (parentGroup == null){
                break;
            }
            level++;
            parentGroup = parentGroup.getParent();

        } while (true);


        label.setText(grp.getName());
        label.setPadding(30 * level, 0, 0, 0);

        return rowView;
    }
}
