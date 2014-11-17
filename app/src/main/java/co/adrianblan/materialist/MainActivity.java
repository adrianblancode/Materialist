package co.adrianblan.materialist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarStyle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melnykov.fab.FloatingActionButton;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-09.
 */
public class MainActivity extends ActionBarActivity{

    public static final String SHARED_PREFS_FILE = "MaterialistPreferences";

    TaskArrayList tasks;
    TaskArrayList removed;
    CustomListAdapter adapter;
    private Toolbar toolbar;
    FloatingActionButton fab_add;
    FloatingActionButton fab_remove;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Arraylist to save all our tasks
        tasks = new TaskArrayList();

        //Completed tasks we have removed
        removed = new TaskArrayList();

        //Gson to serialize our objects to Json to save
        gson = new Gson();

        //Then the application is being reloaded
        if( savedInstanceState != null ) {
            //TODO save dialog
        }

        SharedPreferences preferencesReader = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        //Return null if preference doesn't exist
        String serializedDataFromPreference = preferencesReader.getString("TaskArrayList", null);

        Type taskArrayListType = new TypeToken<TaskArrayList>() {}.getType();
        TaskArrayList temp = gson.fromJson(serializedDataFromPreference, taskArrayListType);

        //If we successfully loaded a TaskArrayList from SharedPreferences, take it
        if(temp != null){
            tasks = temp;
        }

        System.out.println("Loading from: " + serializedDataFromPreference);

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
        removed = tasks.removeCompletedTasks();
        adapter.notifyDataSetChanged();

        fab_remove.hide();

        //Create a toast, when the undo button is pressed re-add all removed tasks
        new UndoBarController.UndoBar(this).message("Removed completed tasks").listener(new UndoBarController.UndoListener() {

            public void onUndo(Parcelable p){
                tasks.insert(removed);
                adapter.notifyDataSetChanged();

                //We assume since the tasks will be restored as checked, we can reintroduce the remove FAB
                fab_remove.show();
            }
        }).noicon(true).show();

        //Toast.makeText(getApplicationContext(), "Removed completed tasks", Toast.LENGTH_SHORT).show();
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

        taskTitleText = (EditText) dialog.getCustomView().findViewById(R.id.task_title);

        //If we name a task and it has a priority, enable positive button
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

        //We want to bring up the keyboard for the title
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        positiveAction.setEnabled(false);
    }

    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
    }

    @Override
    protected void onStop(){
        super.onStop();

        //Serialize our TaskArrayList to Json
        Type taskArrayListType = new TypeToken<TaskArrayList>(){}.getType();
        String serializedData = gson.toJson(tasks, taskArrayListType);

        System.out.println("Saving: " + serializedData);

        //Save tasks in SharedPreferences
        SharedPreferences preferencesReader = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencesReader.edit();
        editor.putString("TaskArrayList", serializedData);
        editor.commit();
    }
}
