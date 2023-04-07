package me.kirillirik.solver;

import java.util.HashSet;
import java.util.Set;

public class Node {
    protected static final int X_OFFSET = 120;
    protected static final int Y_OFFSET = 40;

    public static int NODE_COUNTER = 0;

    protected final int id;
    protected final int dataID;
    protected final String title;
    protected final int inputID;
    protected final int outputID;
    protected final Color color;
    protected final Set<Integer> links = new HashSet<>();

    protected int x, y;

    public Node(String title, int dataID, int x, int y) {
        this.id = NODE_COUNTER++;
        this.dataID = dataID;
        this.title = title;
        this.x = x;
        this.y = y;
        this.inputID = id * 2;
        this.outputID = id * 2 + 1;
        this.color = new Color(0, 0, 0);
    }

    public void addLink(int id) {
        links.add(id * 2);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getID() {
        return id;
    }

    public int getDataID() {
        return dataID;
    }

    public String getTitle() {
        return title;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getInputID() {
        return inputID;
    }

    public int getOutputID() {
        return outputID;
    }

    public Color getColor() {
        return color;
    }

    public Set<Integer> getLinks() {
        return links;
    }
}
