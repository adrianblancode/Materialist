package co.adrianblan.materialist;

/**
 * Created by Adrian on 2014-11-09.
 */
public class TaskItem {

    public static enum Color {
        RED, BLUE, GREEN;
    }

    private String text;
    private Color color;

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

    @Override
    public String toString() {
        return "[ text: " + text + ",  color: " + color + "]";
    }
}
