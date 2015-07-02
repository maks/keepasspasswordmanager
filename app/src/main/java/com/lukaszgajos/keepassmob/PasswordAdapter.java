package com.lukaszgajos.keepassmob;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.keepassdroid.database.PwEntry;

public class PasswordAdapter extends ArrayAdapter<PwEntry> {
    private final Context context;
    private final PwEntry[] values;



    public PasswordAdapter(Context context, PwEntry[] values){
        super(context, -1, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.password_entry_row, parent, false);

        boolean letterAppear = false;


        TextView label = (TextView) rowView.findViewById(R.id.entry_name);
        TextView letter = (TextView) rowView.findViewById(R.id.separator_letter);
        PwEntry entry = values[position];
        if (position == 0){
            letterAppear = true;
        } else {
            PwEntry prevEntry = values[position-1];
            letterAppear = true;
            if (entry.getTitle().length() > 1 && prevEntry.getTitle().length() > 1){
                String fletter1 = entry.getTitle().substring(0, 1).toLowerCase();
                String fletter2 = prevEntry.getTitle().substring(0, 1).toLowerCase();
                if (fletter1.equals(fletter2)){
                    letterAppear = false;
                }
            }
        }
        if (entry.getTitle().length() > 1){
            letter.setText(entry.getTitle().substring(0, 1).toUpperCase());
        }


        if (letterAppear){
            letter.setVisibility(View.VISIBLE);
        } else {
            letter.setVisibility(View.INVISIBLE);
        }

        label.setText(entry.getTitle());

        return rowView;
    }
}
