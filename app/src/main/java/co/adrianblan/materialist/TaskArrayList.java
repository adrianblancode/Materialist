package co.adrianblan.materialist;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-12.
 */
public class TaskArrayList extends ArrayList<TaskItem> implements Parcelable{

    public TaskArrayList(){}

    protected TaskArrayList(Parcel in) {

        if(in.dataAvail() > 0) {
            this.addSorted(new TaskItem(in));
        }
    }

    //Adds a task to the appropriate place
    public boolean addSorted(TaskItem t){

        if(t.getColor() == TaskItem.Color.RED){
            this.add(0, t);
        }
        else {
            for(int i = 0; i < this.size(); i++){
                if(t.getColor() == this.get(i).getColor()){
                    this.add(i, t);
                    return true;
                }
            }

            this.add(t);
        }

        return true;
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
