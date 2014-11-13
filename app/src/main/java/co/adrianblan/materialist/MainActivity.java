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
    FloatingActionButton fab_add;
    FloatingActionButton fab_remove;
    TinyDB tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Arraylist to save all our tasks
        tasks = new TaskArrayList();

        //Then the application is being reloaded
        if( savedInstanceState != null ) {
            ArrayList<TaskItem> al = savedInstanceState.getParcelableArrayList("TaskArrayList");

            for(TaskItem ti : al) {
                tasks.insert(ti);
            }
        }

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

        //Set up the floating action buttons
        ListView listView = (ListView) findViewById(R.id.listview);

        //For adding new tasks
        fab_add = (FloatingActionButton) findViewById(R.id.fab_add);

        //For removing completed tasks
        fab_remove = (FloatingActionButton) findViewById(R.id.fab_remove);

        //Hax to make the two fabs scroll together
        fab_add.attachToListView(listView, new FloatingActionButton.FabOnScrollListener() {
            @Override
            public void onScrollDown() {
                super.onScrollDown();
                if(tasks.hasCompletedTasks()) {
                    fab_remove.show();
                }
            }

            @Override
            public void onScrollUp() {
                super.onScrollUp();
                fab_remove.hide();
            }
        });



        //Shows the remove fab if we have tasks to remove
        if(tasks.hasCompletedTasks()) {
            fab_remove.show(false);
        } else {
            fab_remove.hide(false);
        }

        //TinyDB makes calls to sharedPreferences easier
        //tinydb = new TinyDB(this);
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

    // Called when the user completes a task by pressing the checkbox
    public void completeTask(View view){
        TaskItem ti = (TaskItem) view.getTag();
        int index = tasks.indexOf(ti);

        //Check for if we get a null object
        if(index >= 0){
            tasks.get(index).toggleChecked();
            tasks.sort(tasks.get(index));
        }

        //Shows the second fab depending if we have tasks to remove
        //WARNING .isVisible() is a hacked method, must re-add if updated
        if(tasks.hasCompletedTasks() && fab_add.isVisible()){
            fab_remove.show();
        } else {
            fab_remove.hide();
        }

        adapter.notifyDataSetChanged();
    }

    View positiveAction;
    TaskItem.Color checkedColor;
    String taskTitle;

    //Called when the user clicks the remove task FAB button
    public void removeCompletedTasks(View view){

        //Removes all completed tasks and notifies the view
        tasks.removeCompletedTasks();
        adapter.notifyDataSetChanged();

        fab_remove.hide();

        Toast.makeText(getApplicationContext(), "Removed completed tasks", Toast.LENGTH_SHORT).show();
    }

    // Called when the user clicks the add task FAB button
    public void addTask(View view) {

        EditText taskTitleText;
        RadioGroup taskPriority;

        taskTitle = "";
        checkedColor = null;

        //Creates a dialog for adding a new task
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
                }
            })
            .build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        //If we name a task and it has a priority, enable positive button
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

        //If we set a priority and the task has a name, enable positive button
        taskPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.task_importance);
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
