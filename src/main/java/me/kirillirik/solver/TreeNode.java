package me.kirillirik.solver;

import java.util.ArrayList;
import java.util.List;

public final class TreeNode extends Node {

    private final List<TreeNode> childs = new ArrayList<>();

    public TreeNode(String title, int x, int y) {
        super(title, x, y);
    }

    public List<TreeNode> getChilds() {
        return childs;
    }

    public TreeNode addChild(String title) {
        final var newChild = new TreeNode(title, x + X_OFFSET, y);
        addLink(newChild.getID());

        childs.add(newChild);

        if (childs.size() <= 1) {
            return newChild;
        }

        int cursor = y - (childs.size() / 2) * Y_OFFSET;
        for (final var child : childs) {
            child.setY(cursor);

            cursor += Y_OFFSET;
            if (cursor == y && childs.size() % 2 == 0) {
                cursor += Y_OFFSET;
            }
        }

        return newChild;
    }
}
