package me.kirillirik.solver;

import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation;
import imgui.flag.ImGuiCol;

import java.io.*;
import java.util.*;

public final class Solver {

    private final List<Node> nodes = new ArrayList<>();


    private int citiesAmount;
    private int[] cities;
    private int linkID = 0;
    private TreeNode root = null;
    private boolean positioned = false;

    private boolean needClose = false;


    public Solver(int type) {
        Node.NODE_COUNTER = 0;

        cities();
        generateNodes();

        switch (type) {
            case 0 -> startWidthFind();
            case 1 -> startDeptFind();
            default -> startAStar();
        }
    }

    /**
     * Загрузка входных данных из файла Cities
     */
    private void cities() {
        try (final var reader = new BufferedReader(new FileReader("cities.txt"))) {
            final var data = reader.readLine();

            final String[] citiesData = data.split(";");
            citiesAmount = citiesData.length;
            cities = new int[citiesAmount * citiesAmount];

            int id = 0;
            for (final String cityData : citiesData) {

                for (final String cityInfo : cityData.split(":")) {
                    final String[] linkData = cityInfo.split(",");
                    final int intCityID = Integer.parseInt(linkData[0]);
                    final int distance = Integer.parseInt(linkData[1]);

                    cities[id + intCityID * citiesAmount] = distance;
                    cities[intCityID + id * citiesAmount] = distance;
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
    private void generateCities() {
        final Random random = new Random();

        citiesAmount = 5;
        cities = new int[citiesAmount * citiesAmount];


        try (final var writer = new BufferedWriter(new FileWriter("cities.txt"))) {
            for (int i = 0; i < citiesAmount; i++) {
                for (int j = 0; j < citiesAmount; j++) {
                    if (i == j) {
                        continue;
                    }

                    writer.write(j + ",");

                    final int distance = random.nextInt(1, 128);
                    cities[i + j * citiesAmount] = distance;
                    cities[j + i * citiesAmount] = distance;

                    writer.write(String.valueOf(distance));
                    if (j + 1 != citiesAmount) {
                        writer.write(":");
                    }
                }

                if (i + 1 != citiesAmount) {
                    writer.write(";");
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateNodes() {
        for (int i = 0; i < citiesAmount; i++) {
            final Node node = new Node("City " + i, i, i * Node.X_OFFSET, i * Node.Y_OFFSET);
            nodes.add(node);

            for (int j = 0; j < citiesAmount; j++) {
                if (i == j || isCityDisconnected(cityDistance(i, j))) {
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

    private void startAStar() {
        root = new TreeNode(null,"City " + 0, 0, (citiesAmount + 2) * Node.X_OFFSET, 0);
        final Set<TreeNode> ends = new HashSet<>();
        aStar(root, ends);

        final TreeNode lastEnd = ends.stream().reduce((treeNode, treeNode2) -> {
            if (treeNode.getTraveledDistance() < treeNode2.getTraveledDistance()) {
                return treeNode;
            }
            return treeNode2;
        }).get();

        colorPath(0, 1, 0, lastEnd.getChildren().get(0));

        root.updateWidth();
        root.updatePos();
    }

    private int aStar(TreeNode currentNode, Set<TreeNode> ends) {
        final List<Node> childNodes = new ArrayList<>();

        for (int i = 0; i < citiesAmount; i++) {
            final int cityID = currentNode.getDataID();
            final int distance = cityDistance(i, cityID);

            if (i == currentNode.getDataID() || isCityDisconnected(distance) || currentNode.getParentsIDs().contains(i)) {
                continue;
            }

            childNodes.add(nodes.get(i));
        }

        final int cityID = currentNode.getDataID();
        childNodes.sort(Comparator.comparingInt(childNode -> cityDistance(cityID, childNode.getDataID())));

        for (final var node : childNodes) {
            final int nodeID = node.getID();
            final int distance = cityDistance(nodeID, cityID);

            final TreeNode subChild = currentNode.addChild("City " + nodeID, nodeID);
            subChild.setTraveledDistance(currentNode.getTraveledDistance() + distance);
            subChild.getColor().set(1.0f, 0, 0);

            boolean skip = false;
            for (final TreeNode end : ends) {
                if (subChild.getTraveledDistance() > end.getTraveledDistance()) {
                    skip = true;
                    break;
                }
            }

            if (skip) {
                continue;
            }

            final int subResult = aStar(subChild, ends);
            for (final TreeNode end : ends) {
                if (subResult > end.getTraveledDistance()) {
                    skip = true;
                    break;
                }
            }

            if (skip) {
                continue;
            }

            if (subChild.getParentsIDs().size() + 1 == citiesAmount) {
                ends.add(subChild);
                final var endOfPath = subChild.addChild("City 0", 0);
                endOfPath.setTraveledDistance(subChild.getTraveledDistance() + cityDistance(subChild.getDataID(), endOfPath.dataID));
                endOfPath.getColor().set(1.0f, 0, 0);
                return endOfPath.getTraveledDistance();
            }
        }

        return 0;
    }

    private void startDeptFind() {
        root = new TreeNode(null,"City 0", 0, (citiesAmount + 2) * Node.X_OFFSET, 0);
        final var ends = new HashSet<TreeNode>();
        depthFind(root, ends);

        final TreeNode lastEnd = ends.stream().reduce((treeNode, treeNode2) -> {
            if (treeNode.getTraveledDistance() < treeNode2.getTraveledDistance()) {
                return treeNode;
            }
            return treeNode2;
        }).get();

        colorPath(0, 1, 0, lastEnd.getChildren().get(0));

        root.updateWidth();
        root.updatePos();
    }

    private int depthFind(TreeNode node, Set<TreeNode> ends) {
        final int cityID = node.getDataID();

        for (int i = 0; i < citiesAmount; i++) {
            final int distance = cityDistance(i, cityID);

            if (i == node.getDataID() || isCityDisconnected(distance) || node.getParentsIDs().contains(i)) {
                continue;
            }

            final TreeNode subChild = node.addChild("City " + i, i);
            subChild.setTraveledDistance(node.getTraveledDistance() + distance);
            subChild.getColor().set(1.0f, 0, 0);

            boolean skip = false;
            for (final TreeNode end : ends) {
                if (subChild.getTraveledDistance() > end.getTraveledDistance()) {
                    skip = true;
                    break;
                }
            }

            if (skip) {
                continue;
            }

            final int subResult = depthFind(subChild, ends);
            for (final TreeNode end : ends) {
                if (subResult > end.getTraveledDistance()) {
                    skip = true;
                    break;
                }
            }

            if (skip) {
                continue;
            }

            if (subChild.getParentsIDs().size() + 1 == citiesAmount) {
                ends.add(subChild);
                final var endOfPath = subChild.addChild("City 0", 0);
                endOfPath.setTraveledDistance(subChild.getTraveledDistance() + cityDistance(subChild.getDataID(), endOfPath.dataID));
                endOfPath.getColor().set(1.0f, 0, 0);
                return endOfPath.getTraveledDistance();
            }
        }

        node.getColor().set(1.0f, 0, 0);

        return 0;
    }

    private void startWidthFind() {
        root = new TreeNode(null,"City 0", 0, (citiesAmount + 2) * Node.X_OFFSET, 0);
        final Set<TreeNode> ends = new HashSet<>();
        widthFind(Collections.singleton(root), ends);

        final TreeNode lastEnd = ends.stream().reduce((treeNode, treeNode2) -> {
            if (treeNode.getTraveledDistance() < treeNode2.getTraveledDistance()) {
                return treeNode;
            }
            return treeNode2;
        }).get();

        colorPath(0, 1, 0, lastEnd.getChildren().get(0));

        root.updateWidth();
        root.updatePos();
    }

    private void widthFind(Set<TreeNode> nodes, Set<TreeNode> ends) {
        if (nodes.isEmpty()) {
            return;
        }

        final Set<TreeNode> newNodes = new HashSet<>();

        for (final TreeNode node : nodes) {
            final int cityID = node.getDataID();

            for (int i = 0; i < citiesAmount; i++) {
                final int distance = cityDistance(i, cityID);

                if (i == node.getDataID() || isCityDisconnected(distance) || node.getParentsIDs().contains(i)) {
                    continue;
                }

                final TreeNode subChild = node.addChild("City " + i, i);
                subChild.setTraveledDistance(node.getTraveledDistance() + distance);
                subChild.getColor().set(1.0f, 0, 0);
                newNodes.add(subChild);

                boolean skip = false;
                for (final TreeNode end : ends) {
                    if (subChild.getTraveledDistance() > end.getTraveledDistance()) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }

                if (subChild.getParentsIDs().size() + 1 == citiesAmount) {
                    ends.add(subChild);
                    final var endOfPath = subChild.addChild("City 0", 0);
                    endOfPath.setTraveledDistance(subChild.getTraveledDistance() + cityDistance(subChild.getDataID(), endOfPath.dataID));
                    endOfPath.getColor().set(1.0f, 0, 0);
                }
            }
        }

        widthFind(newNodes, ends);
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

        ImNodes.beginNodeEditor();

        displayTreeNode(root);
        linkTreeNode(root);

        ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor();

        if (!positioned) {
            positioned = true;
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

        ImNodes.pushColorStyle(ImNodesColorStyle.NodeOutline,
                ImColor.floatToColor(color.getR(), color.getG(), color.getB()));

        final int id = node.getID();
        ImNodes.beginNode(id);
        if (!positioned) {
            ImNodes.setNodeGridSpacePos(id, node.getX(), node.getY());
        }

        ImNodes.beginNodeTitleBar();
        ImGui.pushStyleColor(ImGuiCol.FrameBg, 0, 0, 0, 0);
        ImGui.text(node.getTitle());
        ImGui.popStyleColor();
        ImNodes.endNodeTitleBar();

        final String nodeInfo = node.getNodeInfo();
        if (nodeInfo != null) {
            ImGui.text(nodeInfo);
        }

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

    public int cityDistance(int first, int second) {
        return cities[first + second * citiesAmount];
    }

    public boolean isCityDisconnected(int distance) {
        return distance <= 0;
    }

    public boolean isNeedClose() {
        return needClose;
    }
}
