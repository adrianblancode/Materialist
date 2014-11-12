package co.adrianblan.materialist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-09.
 */
public class MainActivity extends ActionBarActivity{

    public static final String SHARED_PREFS_FILE = "MaterialistPreferences";

    TaskArrayList tasks;
    CustomListAdapter adapter;
    private Toolbar toolbar;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( savedInstanceState != null ) {
            //Then the application is being reloaded
        }

        //Restore tasks from preferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            tasks = (TaskArrayList) ObjectSerializer.deserialize(prefs.getString("taskArrayList", ObjectSerializer.serialize(new TaskArrayList())));
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.main);

        tasks = initListData();
        final ListView lv1 = (ListView) findViewById(R.id.listview);
        adapter = new CustomListAdapter(this, tasks);
        lv1.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ListView listView = (ListView) findViewById(R.id.listview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);
    }

    private TaskArrayList initListData() {
        TaskArrayList results = new TaskArrayList();

        TaskItem li = new TaskItem();
        li.setText("Buy milk");
        li.setColor(TaskItem.Color.RED);
        results.addSorted(li);

        li = new TaskItem();
        li.setText("Homework");
        li.setColor(TaskItem.Color.BLUE);
        results.addSorted(li);

        li = new TaskItem();
        li.setText("Watch Breaking Bad");
        li.setColor(TaskItem.Color.GREEN);
        results.addSorted(li);

        return results;
    }


    View positiveAction;
    TaskItem.Color checkedColor;
    String taskTitle;

    /** Called when the user clicks the FAB button */
    public void addTask(View view) {

        EditText taskTitleText;
        RadioGroup taskPriority;

        taskTitle = "";
        checkedColor = null;

        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .title("Add Task")
            .customView(R.layout.addtask)
            .negativeText("Cancel")
            .positiveText("Add")
            .negativeColor(Color.parseColor("#2196F3"))
            .positiveColor(Color.parseColor("#2196F3"))
            .callback(new MaterialDialog.SimpleCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {

                    //Creating a new TaskItem for the task
                    TaskItem li = new TaskItem();
                    li.setText(taskTitle);
                    li.setColor(checkedColor);
                    tasks.addSorted(li);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "Created: " + taskTitle + " with priority " + checkedColor.toString(), Toast.LENGTH_SHORT).show();
                }
            })
            .build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        taskTitleText = (EditText) dialog.getCustomView().findViewById(R.id.task_title);
        taskTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                taskTitle = s.toString();
                positiveAction.setEnabled(taskTitle.trim().length() > 0 && checkedColor != null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        taskPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.task_importance);
        //RadioButton checkedRadioButton = (RadioButton) taskPriority.findViewById(taskPriority.getCheckedRadioButtonId());

        taskPriority.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup taskPriority, int checkedId) {

            RadioButton checkedRadioButton = (RadioButton) taskPriority.findViewById(checkedId);

            if (checkedRadioButton.isChecked()) {

                //We save the color value of the radio button
                //Subtract one to make it zero indexed, mod 3 since it seems to count previous dialogs too
                checkedColor = TaskItem.Color.values()[(checkedId - 1) % 3];
                positiveAction.setEnabled(taskTitle.trim().length() > 0 && checkedColor != null);
            }
            }
        });

        dialog.show();
        positiveAction.setEnabled(false);
    }

    @Override
    protected void onStop(){
        super.onStop();

        //save the task list to preferences
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("taskArrayList", ObjectSerializer.serialize(tasks));
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.commit();
    }
}
