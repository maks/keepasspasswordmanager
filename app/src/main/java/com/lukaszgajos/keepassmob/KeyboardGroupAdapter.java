package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keepassdroid.database.PwGroup;



/**
 * Created by pedros on 04.06.15.
 */
public class KeyboardGroupAdapter extends ArrayAdapter<PwGroup> {
    private final Context context;
    private final PwGroup[] values;
    private AdapterView.OnItemClickListener listener;

    public KeyboardGroupAdapter(Context context, PwGroup[] values){
        super(context, -1, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.keyboard_group_row, parent, false);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(null, null, position, 1);
            }
        });


        TextView label = (TextView) rowView.findViewById(R.id.group_name_label);
        PwGroup grp = values[position];
//        Button btn = (Button) rowView.findViewById(R.id.button2);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(parent.getContext(), "BTN KLICKED", Toast.LENGTH_SHORT).show();
//            }
//        });

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

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        this.listener = listener;

    }

}
