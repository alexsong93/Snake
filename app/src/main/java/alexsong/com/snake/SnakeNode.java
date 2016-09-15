package alexsong.com.snake;

import android.widget.ImageView;

public class SnakeNode {
    private SnakeNode next;
    private ImageView view;

    public SnakeNode(ImageView v) {
        view = v;
    }

    public ImageView getView() {
        return view;
    }
    public SnakeNode getNext() {
        return next;
    }
    public void setView(ImageView v) {
        view = v;
    }
    public void setNext(SnakeNode n) {
        next = n;
    }
}
