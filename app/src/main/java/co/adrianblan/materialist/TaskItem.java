package co.adrianblan.materialist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

/**
 * Created by Adrian on 2014-11-09.
 */
public class TaskItem {

    public static enum Color {
        RED, BLUE, GREEN;
    }

    private String text;
    private Color color;
    private boolean checked;

    public TaskItem(){
        this.text = "";
        this.color = Color.BLUE;
        this.checked = false;
    }

    public TaskItem(TaskItem ti){
        this.text = ti.getText();
        this.color = ti.getColor();
        this.checked = ti.getChecked();
    }

    static public TaskItem create(String serializedData) {
        // Use GSON to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, TaskItem.class);
    }

    public String serialize() {
        // Serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean getChecked(){
        return checked;
    }

    public void setChecked(boolean c){
        checked = c;
    }

    public void toggleChecked(){
        checked = !checked;
    }

    @Override
    public String toString() {
        return "[ text: " + text + ",  color: " + color + ", checked: " + checked + "]";
    }
}