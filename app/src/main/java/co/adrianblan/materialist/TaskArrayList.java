package co.adrianblan.materialist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Adrian on 2014-11-12.
 */
public class TaskArrayList extends ArrayList<TaskItem>{

    public TaskArrayList(){}

    //Removes an item from the list and then sorts it again
    public int sort(TaskItem ti){
        if(ti != null) {
            this.remove(ti);
            return this.insert(ti);
        }

        else return -1;
    }

    //Adds a task to the appropriate place
    //Priority goes first unchecked with red > blue > green, then checked in chronological order
    public int insert(TaskItem ti){

        //Index where we can start adding the task depending if it's checked or not
        int startIndex;

        //Unchecked tasks should be higher up
        if(!ti.getChecked()){
            startIndex = 0;

        } else {

            startIndex = this.size();

            //First occurrence of checked task
            for(int i = 0; i < this.size(); i++){
                if(this.get(i).getChecked()){
                    startIndex = i;
                    break;
                }
            }
        }

        //Place red unchecked, and checked
        if(ti.getColor() == TaskItem.Color.RED || ti.getChecked()){
            this.add(startIndex, ti);
            return startIndex;
        }

        for(int i = startIndex; i < this.size(); i++){

            //Unchecked have priority over checked
            if(!ti.getChecked() && this.get(i).getChecked()){
                this.add(i, ti);
                return i;
            }

            //If it's not red, blue has priority
            if(ti.getColor() == TaskItem.Color.BLUE && this.get(i).getColor() != TaskItem.Color.RED){
                this.add(i, ti);
                return i;
            }

            //Green only has priority over green
            if(ti.getColor() == TaskItem.Color.GREEN && this.get(i).getColor() == TaskItem.Color.GREEN){
                this.add(i, ti);
                return i;
            }
        }

        //Whatever
        this.add(ti);
        return this.size() - 1;
    }

    public void insert(TaskArrayList tal){
        for(TaskItem ti : tal){
            this.insert(ti);
        }
    }

    public boolean hasCompletedTasks(){
        //First occurrence of checked task
        for(int i = 0; i < this.size(); i++){
            if(this.get(i).getChecked()){
                return true;
            }
        }

        return false;
    }

    //Returns a list of all completed tasks
    public TaskArrayList getCompletedTasks(){
        Iterator<TaskItem> it = this.iterator();
        TaskArrayList completed = new TaskArrayList();

        while(it.hasNext()){
            TaskItem ti = it.next();

            if(ti.getChecked()){
                completed.add(ti);
            }
        }

        return completed;
    }

    //Removes all completed tasks, returns a TaskArrayList of completed tasks
    public void removeCompletedTasks(){
        Iterator<TaskItem> it = this.iterator();

        while(it.hasNext()){
            TaskItem ti = it.next();

            if(ti.getChecked()){
                it.remove();
            }
        }
    }
}
