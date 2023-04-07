package me.kirillirik.solver;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public final class Solver {

    private int personsAmount;
    private boolean[] persons;

    private final List<Node> nodes = new ArrayList<>();
    private final List<Node> sortedNodes = new ArrayList<>();

    private int linkID = 0;
    private TreeNode root = null;
    private boolean positioned = false;

    private boolean needClose = false;


    public Solver(int type) {
        Node.NODE_COUNTER = 0;

        loadPersons();
        generateNodes();

        switch (type) {
            case 0 -> startWidthFind();
            case 1 -> startDeptFind();
            default -> startAStar();
        }
    }

    /**
     * Загрузка входных данных из файла Persons
     */
    private void loadPersons() {
        try (final var reader = new BufferedReader(new FileReader("persons.txt"))) {
            final var data = reader.readLine();

            final String[] personsData = data.split(":");
            personsAmount = personsData.length;
            persons = new boolean[personsAmount * personsAmount];

            int id = 0;
            for (final String personData : personsData) {

                for (final String personID : personData.split(",")) {
                    final int intPersonID = Integer.parseInt(personID);

                    persons[id + intPersonID * personsAmount] = true;
                    persons[intPersonID + id * personsAmount] = true;
                }

                id++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Генерация случайных входных данных
     */
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

        // Сортировка персон по количеству их знакомств
        sortedNodes.addAll(sortNodes(nodes));
    }

    private void startAStar() {
        final Node node = sortedNodes.get(0);

        final int id = node.getID();
        root = new TreeNode(null,"Person " + id, id, (personsAmount + 2) * Node.X_OFFSET, 0);
        aStar(root);

        root.updateWidth();
        root.updatePos();
    }

    private boolean aStar(TreeNode currentNode) {
        final List<Node> childNodes = new ArrayList<>();

        for (int i = 0; i < personsAmount; i++) {
            final int personID = currentNode.getDataID();
            if (i == currentNode.getDataID() || !persons[i + personID * personsAmount] || currentNode.getParentIDs().contains(i)) {
                continue;
            }

            childNodes.add(nodes.get(i));
        }
        childNodes.sort(Comparator.comparingInt(childNode -> -childNode.getLinks().size()));

        final int personID = currentNode.getDataID();
        for (final var node : childNodes) {
            final int nodeID = node.getID();
            if (nodeID == currentNode.getDataID() || !persons[nodeID + personID * personsAmount]) {
                continue;
            }

            final TreeNode subChild = currentNode.addChild("Person " + nodeID, nodeID);

            if (subChild.getParentIDs().size() + 1 == personsAmount || depthFind(subChild)) {
                subChild.getColor().set(0, 1.0f, 0);
                return true;
            }
        }
        currentNode.getColor().set(1.0f, 0, 0);

        return false;
    }

    private void startDeptFind() {
        root = new TreeNode(null,"Person 0", 0, (personsAmount + 2) * Node.X_OFFSET, 0);
        depthFind(root);

        root.updateWidth();
        root.updatePos();
    }

    private boolean depthFind(TreeNode node) {
        final int personID = node.getDataID();

        for (int i = 0; i < personsAmount; i++) {
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

    private void startWidthFind() {
        root = new TreeNode(null,"Person 0", 0, (personsAmount + 2) * Node.X_OFFSET, 0);
        widthFind(Collections.singleton(root));

        root.updateWidth();
        root.updatePos();
    }

    private void widthFind(Set<TreeNode> nodes) {
        if (nodes.isEmpty()) {
            return;
        }

        final Set<TreeNode> newNodes = new HashSet<>();

        for (final TreeNode node : nodes) {
            final int personID = node.getDataID();

            for (int i = 0; i < personsAmount; i++) {
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

        if (ImGui.button("Return")) {
            needClose = true;
        }

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

    private List<Node> sortNodes(List<Node> nodes) {
        return nodes.stream().sorted(Comparator.comparing(node -> -node.getLinks().size())).toList();
    }

    public boolean isNeedClose() {
        return needClose;
    }
}
