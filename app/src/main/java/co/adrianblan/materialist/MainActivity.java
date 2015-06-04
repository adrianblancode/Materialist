package co.adrianblan.materialist;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cocosw.undobar.UndoBarController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melnykov.fab.FloatingActionButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.reflect.Type;

/**
 * Created by Adrian on 2014-11-09.
 */
public class MainActivity extends ActionBarActivity{

    public static final String SHARED_PREFS_FILE = "MaterialistPreferences";

    TaskArrayList tasks;
    TaskArrayList removed;
    CustomListAdapter adapter;
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

        //If the application is being reloaded
        //if( savedInstanceState != null ) {}

        //SharedPreferences stores all data which we want to be permanent
        SharedPreferences preferencesReader = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        //Return null if preference doesn't exist
        String serializedDataFromPreference = preferencesReader.getString("TaskArrayList", null);

        //Deserializes any taskarraylist we have saved
        Type taskArrayListType = new TypeToken<TaskArrayList>() {}.getType();
        TaskArrayList temp = gson.fromJson(serializedDataFromPreference, taskArrayListType);

        //If we successfully loaded a TaskArrayList from SharedPreferences, take it
        if(temp != null){
            tasks = temp;
        }

        System.out.println("Loading: " + serializedDataFromPreference);

        setContentView(R.layout.main);


        ListView lv = (ListView) findViewById(R.id.listview);

        //We bind our arraylist of tasks to the adapter
        adapter = new CustomListAdapter(this, tasks);
        lv.setAdapter(adapter);

        //Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        //For adding new tasks
        fab_add = (FloatingActionButton) findViewById(R.id.fab_add);

        //For removing completed tasks
        fab_remove = (FloatingActionButton) findViewById(R.id.fab_remove);

        //Hax to make the two fabs scroll together
        fab_add.attachToListView(lv, new FloatingActionButton.FabOnScrollListener() {
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

        // Only set the tint if the device is running KitKat or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    // Called when the user completes a task by pressing the checkbox
    public void completeTask(View view) {
        final TaskItem ti = (TaskItem) view.getTag();
        final int index = tasks.indexOf(ti);

        //Check for if we get a null object
        if (index < 0) {
            System.out.println("Weird index?");
            return;
        }

        //First we fade out the animation, then we fade it in
        final Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        final Animation fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        fade_in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {}
        });

        fade_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tasks.get(index).toggleChecked();
                tasks.sort(tasks.get(index));

                //Shows the second fab depending if we have tasks to remove
                //WARNING .isVisible() is a hacked method, must re-add if updated
                if(tasks.hasCompletedTasks() && fab_add.isVisible()){
                    fab_remove.show();
                } else {
                    fab_remove.hide();
                }

                adapter.notifyDataSetChanged();

                //Sorts the task, then plays the animation for the new one
                View v = findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview));
                if(v != null){
                    v.startAnimation(fade_in);
                }
            }
        });

        //Check != null
        View current = findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview));
        if(current != null) {
            current.startAnimation(fade_out);
        } else {
            System.out.println("NULL, DO STUFF!");
        }
    }

    /*This will handle the first time call*/
    public void fadeIn(final View notificationView, final float startY, final float endY, final WindowManager.LayoutParams params, final WindowManager mWindowManager){
        final long startTime = System.currentTimeMillis();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                fadeInHandler(notificationView, startY, endY, params, mWindowManager, startTime);
            }
        }, 16);
    }

    /*This will handle the entire animation*/
    public void fadeInHandler(final View notificationView, final float startY, final float endY, final WindowManager.LayoutParams params, final WindowManager mWindowManager, final long startTime){
        long timeNow = System.currentTimeMillis();

        float currentY = startY + ((timeNow - startTime)/300.0f) * (endY - startY);

        //If the animation has gone too far

        /*if (Math.abs(currentY - startY) >= Math.abs(endY - startY)){
            currentY = endY;
        }

        params.y = (int) currentY;
        */

        params.alpha = 0.0f;

        mWindowManager.updateViewLayout(notificationView, params);
        if (timeNow-startTime < 300){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){
                public void run(){
                    fadeInHandler(notificationView, startY, endY, params, mWindowManager, startTime);
                }
            }, 16);
        } else {
            mWindowManager.removeView(notificationView);
        }
    }

    View positiveAction;
    TaskItem ti_temp;
    TaskItem.Color checkedColor;
    String taskTitle;

    boolean undoIsVisible = false;

    //Called when the user clicks the remove task FAB button
    public void removeCompletedTasks(View view){

        final int add_margin = ((RelativeLayout.LayoutParams) findViewById(R.id.fab_add).getLayoutParams()).bottomMargin;
        final int remove_margin = ((RelativeLayout.LayoutParams) findViewById(R.id.fab_remove).getLayoutParams()).bottomMargin;

        //Removes all completed tasks and notifies the view
        removed = tasks.getCompletedTasks();

        final Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fade_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tasks.removeCompletedTasks();
                adapter.notifyDataSetChanged();
            }
        });

        for(TaskItem ti : removed){
            View v = findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview));

            if(v != null){
                v.startAnimation(fade_out);
            } else {
                tasks.remove(ti);
                adapter.notifyDataSetChanged();
            }
        }

        fab_remove.hide();

        //When the toast appears, we need to move the fabs up
        final Animation fab_in = AnimationUtils.loadAnimation(this, R.anim.fab_in);

        fab_in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                //The animation only moves the view, so we need to change the touch target too
                ((RelativeLayout.LayoutParams) findViewById(R.id.fab_add).getLayoutParams()).bottomMargin = (int) (remove_margin * 0.6f);
                ((RelativeLayout.LayoutParams) findViewById(R.id.fab_remove).getLayoutParams()).bottomMargin = remove_margin - add_margin + (int) (remove_margin * 0.6f);

                fab_add.requestLayout();
                fab_remove.requestLayout();
            }
        });

        final Animation fab_out = AnimationUtils.loadAnimation(this, R.anim.fab_out);

        fab_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                ((RelativeLayout.LayoutParams) findViewById(R.id.fab_add).getLayoutParams()).bottomMargin = add_margin;
                ((RelativeLayout.LayoutParams) findViewById(R.id.fab_remove).getLayoutParams()).bottomMargin = remove_margin;

                fab_add.requestLayout();
                fab_remove.requestLayout();
            }
        });

        final Context ct = this;

        //Create a snackbar, when the undo button is pressed: re-add all removed tasks
        UndoBarController.UndoBar ub = new UndoBarController.UndoBar(this).message("Removed completed tasks").listener(new UndoBarController.AdvancedUndoListener() {

            public void onUndo(Parcelable p) {

                tasks.insert(removed);
                adapter.notifyDataSetChanged();

                //Fade in the animation fancily when added
                //DOES NOT WORK CORRECTLY
                final Animation fade_in = AnimationUtils.loadAnimation(ct, R.anim.fade_in);

                for(TaskItem li : removed) {
                    View v = findViewByIndex(tasks.indexOf(li), (ListView) findViewById(R.id.listview));

                    if (v != null) {
                        v.startAnimation(fade_in);
                    }
                }

                //We assume since the tasks will be restored as checked, we can reintroduce the remove FAB
                fab_remove.show();
                findViewById(R.id.fab_add).startAnimation(fab_out);
                findViewById(R.id.fab_remove).startAnimation(fab_out);
                undoIsVisible = false;
            }

            public void onHide(Parcelable p) {
                findViewById(R.id.fab_add).startAnimation(fab_out);
                findViewById(R.id.fab_remove).startAnimation(fab_out);
                undoIsVisible = false;
            }

            public void onClear(Parcelable[] p) {
            }

        }).noicon(true);

        if(!undoIsVisible) {

            ub.show();

            //The task is removed, so the fabs must go up
            findViewById(R.id.fab_add).startAnimation(fab_in);
            findViewById(R.id.fab_remove).startAnimation(fab_in);
            undoIsVisible = true;
        }

    }

    // Called when the user clicks the add task FAB button
    public void addTask(View view) {

        EditText taskTitleText;
        RadioGroup taskPriority;

        taskTitle = "";
        checkedColor = TaskItem.Color.BLUE;
        final Context ct = this;

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
                    final TaskItem li = new TaskItem();
                    li.setText(taskTitle);
                    li.setColor(checkedColor);
                    li.setChecked(false);

                    tasks.insert(li);
                    adapter.notifyDataSetChanged();

                    View v = findViewByIndex(tasks.indexOf(li), (ListView) findViewById(R.id.listview));

                    //Fade in the animation fancily when added
                    final Animation fade_in = AnimationUtils.loadAnimation(ct, R.anim.fade_in);

                    if(v != null) {
                        v.startAnimation(fade_in);
                    }
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
        taskPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.task_priority);
        taskPriority.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup taskPriority, int checkedId) {

                RadioButton checkedRadioButton = (RadioButton) taskPriority.findViewById(checkedId);

                if (checkedRadioButton.isChecked()) {

                    //We save the color value of the radio button
                    if(checkedId == R.id.task_priority_red){
                        checkedColor = TaskItem.Color.RED;
                    } else if(checkedId == R.id.task_priority_blue){
                        checkedColor = TaskItem.Color.BLUE;
                    } else if(checkedId == R.id.task_priority_green){
                        checkedColor = TaskItem.Color.GREEN;
                    } else {
                        checkedColor = null;
                    }

                    positiveAction.setEnabled(taskTitle.trim().length() > 0 && checkedColor != null);
                }
            }
        });

        //We want to bring up the keyboard for the title
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        //Lastly, default value for positive action should be false
        positiveAction.setEnabled(false);
    }

    // Called when the user clicks the add task FAB button
    public void updateTask(View view) {

        EditText taskTitleText;
        RadioGroup taskPriority;

        ti_temp = (TaskItem) view.getTag();
        int index = tasks.indexOf(ti_temp);

        taskTitle = ti_temp.getText();
        checkedColor = ti_temp.getColor();
        final TaskItem.Color originalColor = ti_temp.getColor();

        //Check for if we get a null object
        if(index < 0){
            System.out.println("Weird index?");
            return;
        }

        //Creates a dialog for adding a new task
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Update Task")
                .customView(R.layout.addtask)
                .negativeText("Cancel")
                .positiveText("Update")
                .negativeColor(Color.parseColor("#2196F3"))
                .positiveColor(Color.parseColor("#2196F3"))
                .callback(new MaterialDialog.SimpleCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        //Modifying the taskitem
                        ti_temp.setText(taskTitle);
                        ti_temp.setColor(checkedColor);

                        //If we change the priority, we need to sort it again
                        if(!originalColor.equals(checkedColor)){
                            tasks.sort(ti_temp);
                        }

                        adapter.notifyDataSetChanged();
                    }
                })
                .build();

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(true);

        taskTitleText = (EditText) dialog.getCustomView().findViewById(R.id.task_title);
        taskTitleText.append(taskTitle);

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
        taskPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.task_priority);

        //Set the color
        if(checkedColor == TaskItem.Color.RED) {
            taskPriority.check(R.id.task_priority_red);
        } else if(checkedColor == TaskItem.Color.BLUE) {
            taskPriority.check(R.id.task_priority_blue);
        } else if(checkedColor == TaskItem.Color.GREEN) {
            taskPriority.check(R.id.task_priority_green);
        }

        taskPriority.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup taskPriority, int checkedId) {

                RadioButton checkedRadioButton = (RadioButton) taskPriority.findViewById(checkedId);

                if (checkedRadioButton.isChecked()) {

                    //We save the color value of the radio button
                    if(checkedId == R.id.task_priority_red){
                        checkedColor = TaskItem.Color.RED;
                    } else if(checkedId == R.id.task_priority_blue){
                        checkedColor = TaskItem.Color.BLUE;
                    } else if(checkedId == R.id.task_priority_green){
                        checkedColor = TaskItem.Color.GREEN;
                    } else {
                        checkedColor = null;
                    }

                    positiveAction.setEnabled(taskTitle.trim().length() > 0 && checkedColor != null);
                }
            }
        });

        //We want to bring up the keyboard for the title
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        //Lastly, default value for positive action should be false
        positiveAction.setEnabled(false);
    }

    //We should save our instance here, currently we do nothing
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
    }

    //Here is where we save permanent data
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
        editor.apply();
    }

    public View findViewByIndex(int index, ListView listView) {

        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (index < firstListItemPosition || index > lastListItemPosition) {
            System.out.println("Invisible view!");
            return null;
            //return listView.getAdapter().getView(index, null, listView);

            //return null;
        } else {
            final int childIndex = index - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    //Clones a task view, it's inflated and filled with the same values
    public View cloneView(View view){
        View result = LayoutInflater.from(this).inflate(R.layout.listitem, null);

        //Sets the text to be the same
        TextView newTextView = (TextView) result.findViewById(R.id.listtext);
        TextView oldTextView = (TextView) view.findViewById(R.id.listtext);
        newTextView.setText(oldTextView.getText());

        //Sets the checkbox to be the same
        ToggleButton newToggleButton = (ToggleButton) result.findViewById(R.id.listbutton);
        ToggleButton oldToggleButton = (ToggleButton) view.findViewById(R.id.listbutton);

        newToggleButton.setCompoundDrawables(oldToggleButton.getCompoundDrawables()[0], null, null, null);
        newToggleButton.setCompoundDrawablePadding(oldToggleButton.getCompoundDrawablePadding());
        //newToggleButton.setPadding(oldToggleButton.getPaddingLeft(), oldToggleButton.getPaddingTop(), oldToggleButton.getPaddingRight(), oldToggleButton.getPaddingBottom());

        return result;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }
}
