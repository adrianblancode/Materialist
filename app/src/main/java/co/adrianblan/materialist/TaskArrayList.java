package co.adrianblan.materialist;

import java.util.ArrayList;

/**
 * Created by Adrian on 2014-11-12.
 */
public class TaskArrayList extends ArrayList<TaskItem> {

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
}
