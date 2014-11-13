package co.adrianblan.materialist;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

/**
 * Created by Adrian on 2014-11-09.
 */
public class TaskItem implements Parcelable {

    public static enum Color {
        RED, BLUE, GREEN;
    }

    private String text;
    private Color color;
    private boolean checked;

    public TaskItem(){
        text = "";
        color = null;
        checked = false;
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

    protected TaskItem(Parcel in) {
        text = in.readString();
        color = (Color) in.readValue(Color.class.getClassLoader());
        checked = in.readInt() == 1;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeValue(color);
        dest.writeInt(checked ? 1 : 0 );
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TaskItem> CREATOR = new Parcelable.Creator<TaskItem>() {
        @Override
        public TaskItem createFromParcel(Parcel in) {
            return new TaskItem(in);
        }

        @Override
        public TaskItem[] newArray(int size) {
            return new TaskItem[size];
        }
    };
}