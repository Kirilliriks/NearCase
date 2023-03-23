package me.kirillirik.solver;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;

import java.util.*;

public final class Solver {

    private final int personsAmount;
    private final boolean[] persons;

    private final List<Node> nodes = new ArrayList<>();

    private int linkID = 0;
    private TreeNode root = null;
    private boolean positioned = false;


    public Solver() {
        this.personsAmount = 6;
        persons = new boolean[personsAmount * personsAmount];

        generatePersons();
        generateNodes();
        generateTree();

        //depthFind(root);
        //widthFind(Collections.singleton(root));
        root.updateWidth();
        root.updatePos();
    }

    private void generatePersons() {
        final Random random = new Random();

        for (int i = 0; i < personsAmount; i++) {

            int linksCount = personsAmount  - personsAmount / 2;
            for (int j = 0; j < linksCount; j++) {
                final int link = random.nextInt(0, linksCount);
                if (i == link) {
                    continue;
                }

                persons[i + link * personsAmount] = true;
                persons[link + i * personsAmount] = true;
            }
        }
    }

    private void generateNodes() {
        for (int i = 0; i < personsAmount; i++) {
            final Node node = new Node("Person " + i, i, i * Node.X_OFFSET, i * Node.Y_OFFSET);
            nodes.add(node);

            for (int j = 0; j < personsAmount; j++) {
                if (i == j || !persons[i + j * personsAmount]) {
                    continue;
                }

                if (i < j) {
                    nodes.get(i).addLink(j);
                } else {
                    nodes.get(j).addLink(i);
                }
            }
        }
    }

    private void generateTree() {
        root = new TreeNode(null,"Person 0", 0, (personsAmount + 2) * Node.X_OFFSET, 0);
    }

    private boolean depthFind(TreeNode node) {
        for (int i = 0; i < personsAmount; i++) {
            final int personID = node.getDataID();
            if (i == node.getDataID() || !persons[i + personID * personsAmount] || node.getParentIDs().contains(i)) {
                continue;
            }

            final TreeNode subChild = node.addChild("Person " + i, i);

            if (subChild.getParentIDs().size() + 1 == personsAmount || depthFind(subChild)) {
                subChild.getColor().set(0, 1.0f, 0);
                return true;
            }
        }
        node.getColor().set(1.0f, 0, 0);

        return false;
    }

    private void widthFind(Set<TreeNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }

        final Set<TreeNode> newNodes = new HashSet<>();

        for (final TreeNode node : nodes) {
            for (int i = 0; i < personsAmount; i++) {
                final int personID = node.getDataID();
                if (i == node.getDataID() || !persons[i + personID * personsAmount] || node.getParentIDs().contains(i)) {
                    continue;
                }

                final TreeNode subChild = node.addChild("Person " + i, i);
                subChild.getColor().set(1.0f, 0, 0);
                newNodes.add(subChild);

                if (subChild.getParentIDs().size() + 1 == personsAmount) {
                    colorPath(0, 1.0f, 0, subChild);
                    return;
                }
            }
        }

        widthFind(newNodes);
    }

    private void colorPath(float r, float g, float b, TreeNode node) {
        if (node == null) {
            return;
        }

        node.getColor().set(r, g, b);
        colorPath(r, g, b, node.getParent());
    }

    public void update() {
        linkID = 0;

        ImGui.begin("Test");

        ImNodes.beginNodeEditor();

        displayPersons();
        displayTreeNode(root);
        linkTreeNode(root);

        ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        ImGui.end();

        if (!positioned) {
            positioned = true;
        }
    }

    private void displayPersons() {
        for (final var node : nodes) {
            displayNode(node);
        }

        for (final var node : nodes) {
            linkNode(node);
        }
    }

    private void linkNode(Node node) {
        for (final int id : node.getLinks()) {
            ImNodes.link(linkID++, node.getOutputID(), id);
        }
    }

    private void displayNode(Node node) {
        final Color color = node.getColor();
        ImNodes.pushColorStyle(ImNodesColorStyle.NodeBackground,
                ImColor.floatToColor(color.getR(), color.getG(), color.getB()));

        final int id = node.getID();
        ImNodes.beginNode(id);
        if (!positioned) {
            ImNodes.setNodeGridSpacePos(id, node.getX(), node.getY());
        }

        ImNodes.beginNodeTitleBar();
        ImGui.text(node.getTitle());
        ImNodes.endNodeTitleBar();

        ImNodes.beginInputAttribute(node.getInputID());
        ImNodes.endInputAttribute();

        ImNodes.beginOutputAttribute(node.getOutputID());
        ImNodes.endOutputAttribute();

        ImNodes.endNode();
    }

    private void linkTreeNode(TreeNode node) {
        linkNode(node);
        for (final var child : node.getChildren()) {
            linkTreeNode(child);
        }
    }

    private void displayTreeNode(TreeNode node) {
        displayNode(node);
        for (final var child : node.getChildren()) {
            displayTreeNode(child);
        }
    }
}
