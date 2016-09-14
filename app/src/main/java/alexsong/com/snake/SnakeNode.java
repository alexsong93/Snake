package alexsong.com.snake;

import android.widget.TextView;

public class SnakeNode {
    private SnakeNode next;
    private TextView textView;

    public SnakeNode(TextView t) {
        textView = t;
    }

    public TextView getTextView() {
        return textView;
    }
    public SnakeNode getNext() {
        return next;
    }
    public void setTextView(TextView t) {
        textView = t;
    }
    public void setNext(SnakeNode n) {
        next = n;
    }
}
