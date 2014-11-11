package co.adrianblan.materialist;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Adrian on 2014-11-09.
 */
public class MainActivity extends ActionBarActivity{

    private Toolbar toolbar;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ArrayList image_details = getListData();
        final ListView lv1 = (ListView) findViewById(R.id.listview);
        lv1.setAdapter(new CustomListAdapter(this, image_details));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ListView listView = (ListView) findViewById(R.id.listview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);
    }

    private ArrayList getListData() {
        ArrayList results = new ArrayList();

        ListItem li = new ListItem();
        li.setText("Buy milk");
        li.setColor(ListItem.Color.RED);
        results.add(li);

        li = new ListItem();
        li.setText("Homework");
        li.setColor(ListItem.Color.BLUE);
        results.add(li);

        li = new ListItem();
        li.setText("Watch Breaking Bad");
        li.setColor(ListItem.Color.GREEN);
        results.add(li);

        return results;
    }

    /** Called when the user clicks the FAB button */
    public void addTask(View view) {

        new MaterialDialog.Builder(this)
                .title("Add Task")
                .customView(R.layout.addtask)
                .negativeText("Cancel")
                .positiveText("Add")
                .negativeColor(Color.parseColor("#2196F3"))
                .positiveColor(Color.parseColor("#2196F3"))
                .build()
                .show();
    }
}
