package co.adrianblan.materialist;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-09.
 */

//This adapter handles our arraylist of taskitems, and creates a listview of them
public class CustomListAdapter extends BaseAdapter {

    private ArrayList<TaskItem> listData;

    private LayoutInflater layoutInflater;

    public Context context;

    public CustomListAdapter(Context context, ArrayList listData) {
        this.context = context;
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Do things with the listview
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listitem, null);

            holder = new ViewHolder();
            holder.buttonView = (ToggleButton) convertView.findViewById(R.id.listbutton);
            holder.textView = (TextView) convertView.findViewById(R.id.listtext);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //We set the tag to the TaskItem, so we can find it in callbacks
        holder.buttonView.setTag(listData.get(position));
        holder.textView.setTag(listData.get(position));

        //Here we set the checkbox of the listview
        if(listData.get(position).getColor() == TaskItem.Color.RED) {
            holder.buttonView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_red, 0, 0, 0);
        }

        else if(listData.get(position).getColor() == TaskItem.Color.BLUE) {
            holder.buttonView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_blue, 0, 0, 0);
        }

        else if(listData.get(position).getColor() == TaskItem.Color.GREEN) {
            holder.buttonView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_green, 0, 0, 0);
        }

        holder.buttonView.setChecked(listData.get(position).getChecked());

        //Padding, 16dp to pixels
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        holder.buttonView.setCompoundDrawablePadding((int)((16 * displayMetrics.density) + 0.5));

        //Set the text of the listview
        holder.textView.setText(listData.get(position).getText());

        if(listData.get(position).getChecked()){
            //Add strike through, set text to gray
            holder.textView.setPaintFlags(holder.buttonView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textView.setTextColor(Color.GRAY);
        } else {
            //Remove strike through, set text to black
            holder.textView.setPaintFlags(holder.buttonView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textView.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    static class ViewHolder {
        ToggleButton buttonView;
        TextView textView;
    }

}
