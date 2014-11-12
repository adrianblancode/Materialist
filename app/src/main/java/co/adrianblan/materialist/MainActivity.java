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

    TaskArrayList tasks;
    private Toolbar toolbar;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tasks = initListData();
        final ListView lv1 = (ListView) findViewById(R.id.listview);
        lv1.setAdapter(new CustomListAdapter(this, tasks));

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
}
