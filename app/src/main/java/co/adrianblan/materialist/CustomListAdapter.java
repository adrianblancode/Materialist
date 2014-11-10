package co.adrianblan.materialist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-09.
 */
public class CustomListAdapter extends BaseAdapter {

    private ArrayList<ListItem> listData;

    private LayoutInflater layoutInflater;

    public CustomListAdapter(Context context, ArrayList listData) {
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

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listitem, null);
            holder = new ViewHolder();
            holder.checkBoxView = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.checkBoxView.setText(listData.get(position).getText());

        if(listData.get(position).getColor() == ListItem.Color.RED) {
            holder.checkBoxView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_red, 0, 0, 0);
        }

        else if(listData.get(position).getColor() == ListItem.Color.BLUE) {
            holder.checkBoxView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_blue, 0, 0, 0);
        }

        else if(listData.get(position).getColor() == ListItem.Color.GREEN) {
            holder.checkBoxView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_selector_green, 0, 0, 0);
        }

        holder.checkBoxView.setCompoundDrawablePadding(25);

        return convertView;
    }

    static class ViewHolder {
        CheckBox checkBoxView;
    }

}
