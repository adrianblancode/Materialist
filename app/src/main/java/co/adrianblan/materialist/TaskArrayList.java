package co.adrianblan.materialist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Adrian on 2014-11-12.
 */
public class TaskArrayList extends ArrayList<TaskItem> implements Parcelable{

    public TaskArrayList(){}

    protected TaskArrayList(Parcel in) {

        if(in.dataAvail() > 0) {
            this.insert(new TaskItem(in));
        }
    }

    //If you
    public void sort(TaskItem ti){
        this.remove(ti);
        this.insert(ti);
    }

    //Adds a task to the appropriate place
    //Priority goes first unchecked with red > blue > green, then checked in chronological order
    public void insert(TaskItem ti){

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
            return;
        }

        for(int i = startIndex; i < this.size(); i++){

            //Unchecked have priority over checked
            if(!ti.getChecked() && this.get(i).getChecked()){
                this.add(i, ti);
                return;
            }

            //If it's not red, blue has priority
            if(ti.getColor() == TaskItem.Color.BLUE && this.get(i).getColor() != TaskItem.Color.RED){
                this.add(i, ti);
                return;
            }

            //Green only has priority over green
            if(ti.getColor() == TaskItem.Color.GREEN && this.get(i).getColor() == TaskItem.Color.GREEN){
                this.add(i, ti);
                return;
            }
        }

        //Whatever
        this.add(ti);
        return;
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

    //Removes all completed tasks
    public void removeCompletedTasks(){
        Iterator<TaskItem> it = this.iterator();

        while(it.hasNext()){
            TaskItem ti = it.next();

            if(ti.getChecked()){
                it.remove();
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        for(int i = 0; i < this.size(); i++){
            this.get(i).writeToParcel(dest, flags);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TaskArrayList> CREATOR = new Parcelable.Creator<TaskArrayList>() {
        @Override
        public TaskArrayList createFromParcel(Parcel in) {
            return new TaskArrayList(in);
        }

        @Override
        public TaskArrayList[] newArray(int size) {
            return new TaskArrayList[size];
        }
    };

}
