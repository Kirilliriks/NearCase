package me.kirillirik;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.ImNodesStyle;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.kirillirik.solver.Solver;

public final class MainWindow {

    private Solver solver = null;

    public void init() {
        ImGui.styleColorsLight();

        final ImGuiStyle style = ImGui.getStyle();
        style.setWindowRounding(1.0f);

        final ImNodesStyle nodesStyle = ImNodes.getStyle();
        nodesStyle.setGridSpacing(0.0f);
    }

    public void update() {
        int windowFlags = ImGuiWindowFlags.MenuBar | ImGuiWindowFlags.NoDocking;

        ImGui.setNextWindowPos(0.0f, 0.0f, ImGuiCond.Always);
        ImGui.setNextWindowSize(Window.getWidth(), Window.getHeight());
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0.0f);
        windowFlags |= ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus;

        ImGui.begin("City solver", new ImBoolean(true), windowFlags);
        ImGui.popStyleVar(2);

        ImGui.beginMenuBar();

        if (ImGui.menuItem("WidthFind")) {
            solver = new Solver(0);
        }

        if (ImGui.menuItem("DepthFind")) {
            solver = new Solver(1);
        }

        if (ImGui.menuItem("AStar")) {
            solver = new Solver(2);
        }

        ImGui.endMenuBar();

        if (solver != null) {
            solver.update();
            if (solver.isNeedClose()) {
                solver = null;
            }
        }

        ImGui.end();
    }
}
