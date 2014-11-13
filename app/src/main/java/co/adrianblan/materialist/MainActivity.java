package co.adrianblan.materialist;

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
    TinyDB tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasks = new TaskArrayList();

        //Then the application is being reloaded
        if( savedInstanceState != null ) {
            ArrayList<TaskItem> al = savedInstanceState.getParcelableArrayList("TaskArrayList");

            for(TaskItem ti : al) {
                tasks.insert(ti);
            }
        }

        //Restore tasks from preferences
        /*SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        try {
            tasks = (TaskArrayList) ObjectSerializer.deserialize(prefs.getString("taskArrayList", ObjectSerializer.serialize(new TaskArrayList())));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        setContentView(R.layout.main);

        //We bind our arraylist of tasks to the adapter
        final ListView lv1 = (ListView) findViewById(R.id.listview);
        adapter = new CustomListAdapter(this, tasks);
        lv1.setAdapter(adapter);

        //Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        //Set up the floating action button
        ListView listView = (ListView) findViewById(R.id.listview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToListView(listView);

        //TinyDB makes calls to sharedPreferences easier
        tinydb = new TinyDB(this);
    }

    //Debug method to generate tasks
    private TaskArrayList initListData() {
        TaskArrayList results = new TaskArrayList();

        TaskItem li = new TaskItem();
        li.setText("Buy milk");
        li.setColor(TaskItem.Color.RED);
        results.insert(li);

        li = new TaskItem();
        li.setText("Homework");
        li.setColor(TaskItem.Color.BLUE);
        results.insert(li);

        li = new TaskItem();
        li.setText("Watch Breaking Bad");
        li.setColor(TaskItem.Color.GREEN);
        results.insert(li);

        return results;
    }

    // Called when the user completes a task
    public void completeTask(View view){
        TaskItem ti = (TaskItem) view.getTag();
        int index = tasks.indexOf(ti);

        if(index >= 0){
            tasks.get(index).toggleChecked();
            tasks.sort(tasks.get(index));
        }

        adapter.notifyDataSetChanged();
    }

    View positiveAction;
    TaskItem.Color checkedColor;
    String taskTitle;

    // Called when the user clicks the FAB button
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
                    li.setChecked(false);
                    tasks.insert(li);
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
                    if(checkedId == R.id.task_importance_red){
                        checkedColor = TaskItem.Color.RED;
                    } else if(checkedId == R.id.task_importance_blue){
                        checkedColor = TaskItem.Color.BLUE;
                    } else if(checkedId == R.id.task_importance_green){
                        checkedColor = TaskItem.Color.GREEN;
                    } else {
                        checkedColor = null;
                    }

                    System.out.println(checkedColor);
                    positiveAction.setEnabled(taskTitle.trim().length() > 0 && checkedColor != null);
                }
            }
        });

        dialog.show();
        positiveAction.setEnabled(false);
    }

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);
        savedState.putParcelableArrayList("TaskArrayList", tasks);
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}
