package me.kirillirik.solver;

import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;

import java.util.*;

public final class Solver {

    private final int personsAmount;
    private final boolean[] persons;

    private final List<Node> nodes = new ArrayList<>();

    private int linkID = 0;
    private TreeNode root = null;
    private boolean positioned = false;


    public Solver(int personsAmount) {
        this.personsAmount = personsAmount;
        persons = new boolean[personsAmount * personsAmount];

        generatePersons();
        generateNodes();
        generateTree();
    }

    private void generatePersons() {
        final Random random = new Random();

        for (int i = 0; i < personsAmount; i++) {

            int linksCount = personsAmount - 1;
            for (int j = 0; j < linksCount; j++) {
                final int link = random.nextInt(0, linksCount);
                if (i == link) {
                    continue;
                }

                persons[i + link * personsAmount] = true;
                persons[link + i * personsAmount] = true;
            }
        }

//        for (int i = 0; i < personsAmount; i++) {
//            for (int j = 0; j < personsAmount; j++) {
//                System.out.print(persons[i + j * personsAmount] + " | ");
//            }
//
//            System.out.println();
//        }
    }

    private void generateNodes() {
        for (int i = 0; i < personsAmount; i++) {
            final Node node = new Node("Person " + i, i * 100, i * 100);
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
        root = new TreeNode("Root", 0, 0);
        depthFind(0, root, new HashSet<>());
    }

    private boolean depthFind(int personID, TreeNode node, Set<Integer> visited) {
        for (int i = 0; i < personsAmount; i++) {
            if (i == personID || !persons[i + personID * personsAmount] || visited.contains(i)) {
                continue;
            }

            node.addChild("Person " + i);
            final Set<Integer> subVisited = new HashSet<>(visited);
            subVisited.add(i);
            //depthFind(personID)
        }

        return false;
    }

    public void update() {
        linkID = 0;

        ImGui.begin("Test");

        ImNodes.beginNodeEditor();


        displayTreeNode(root);
        linkTreeNode(root);


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
        for (final var child : node.getChilds()) {
            linkTreeNode(child);
        }
    }

    private void displayTreeNode(TreeNode node) {
        displayNode(node);
        for (final var child : node.getChilds()) {
            displayTreeNode(child);
        }
    }
}
