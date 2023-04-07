package me.kirillirik;

import imgui.ImGui;
import me.kirillirik.solver.Solver;

public final class MainWindow {

    private Solver solver = null;

    public void update() {
        if (solver != null) {
            solver.update();
            if (solver.isNeedClose()) {
                solver = null;
            } else {
                return;
            }
        }

        ImGui.begin("MainWindow");

        if (ImGui.button("WidthFind")) {
            solver = new Solver(0);
        }

        if (ImGui.button("DepthFind")) {
            solver = new Solver(1);
        }

        if (ImGui.button("AStar")) {
            solver = new Solver(2);
        }

        ImGui.end();
    }
}
