package co.adrianblan.materialist;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.colorPrimaryDark));
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
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });

        fade_out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Shows the second fab depending if we have tasks to remove
                //WARNING .isVisible() is a hacked method, must re-add if updated
                if (tasks.hasCompletedTasks() && fab_add.isVisible()) {
                    fab_remove.show();
                } else {
                    fab_remove.hide();
                }

                adapter.notifyDataSetChanged();

                //Sorts the task, then plays the animation for the new one
                findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview)).startAnimation(fade_in);
            }
        });

        View current = findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview));

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

        //Getting the height of the statusbar
        Rect rect = new Rect();
        Window win = this.getWindow();
        win.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;

        //Parameters so we can overlay our animation target over the item
        windowParams.gravity = Gravity.TOP | Gravity.RIGHT;
        windowParams.x = current.getLeft();
        windowParams.y = current.getTop() + findViewById(R.id.toolbar).getHeight() - titleBarHeight;
        windowParams.height = current.getHeight();
        windowParams.width = current.getWidth();
        windowParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;

        View hooveredView = cloneView(current);

        // Add the hoovered view to the window manager, as a new view in the screen
        final WindowManager mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(hooveredView, windowParams);

        float startY = current.getTop() + findViewById(R.id.toolbar).getHeight() - titleBarHeight;

        ExpandAnimation expandAni = new ExpandAnimation(current, 500);

        tasks.get(index).toggleChecked();
        int newIndex = tasks.sort(tasks.get(index));
        adapter.notifyDataSetChanged();
        //current.startAnimation(expandAni);

        View newView = findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview));
        float endY = newView.getTop() + findViewById(R.id.toolbar).getHeight() - titleBarHeight;

        //current.startAnimation(fade_out);

        fadeIn(hooveredView, startY, endY, windowParams, mWindowManager);
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
        if (Math.abs(currentY - startY) >= Math.abs(endY - startY)){
            currentY = endY;
        }

        params.y = (int) currentY;

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

    //Called when the user clicks the remove task FAB button
    public void removeCompletedTasks(View view){

        //Removes all completed tasks and notifies the view
        removed = tasks.getCompletedTasks();

        final Animation fade_out = AnimationUtils.loadAnimation(this, R.anim.scale_down);
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
            findViewByIndex(tasks.indexOf(ti), (ListView) findViewById(R.id.listview)).startAnimation(fade_out);
        }

        fab_remove.hide();

        //When the toast appears, we need to move the fabs up
        final Animation fab_in = AnimationUtils.loadAnimation(this, R.anim.fab_in);
        fab_in.setFillAfter(true);

        final Animation fab_out = AnimationUtils.loadAnimation(this, R.anim.fab_out);
        fab_out.setFillAfter(true);

        //Create a snackbar, when the undo button is pressed: re-add all removed tasks
        new UndoBarController.UndoBar(this).message("Removed completed tasks").listener(new UndoBarController.AdvancedUndoListener() {

            public void onUndo(Parcelable p) {
                tasks.insert(removed);
                adapter.notifyDataSetChanged();

                //We assume since the tasks will be restored as checked, we can reintroduce the remove FAB
                fab_remove.show();
                findViewById(R.id.fab_add).startAnimation(fab_out);
                findViewById(R.id.fab_remove).startAnimation(fab_out);
            }

            public void onHide(Parcelable p) {
                findViewById(R.id.fab_add).startAnimation(fab_out);
                findViewById(R.id.fab_remove).startAnimation(fab_out);
            }

            public void onClear(Parcelable[] p) {
            }

        }).noicon(true).show();
        findViewById(R.id.fab_add).startAnimation(fab_in);
        findViewById(R.id.fab_remove).startAnimation(fab_in);

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
        taskPriority = (RadioGroup) dialog.getCustomView().findViewById(R.id.task_importance);

        //Set the color
        if(checkedColor == TaskItem.Color.RED) {
            taskPriority.check(R.id.task_importance_red);
        } else if(checkedColor == TaskItem.Color.BLUE) {
            taskPriority.check(R.id.task_importance_blue);
        } else if(checkedColor == TaskItem.Color.GREEN) {
            taskPriority.check(R.id.task_importance_green);
        }

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
            return listView.getAdapter().getView(index, null, listView);
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
}
