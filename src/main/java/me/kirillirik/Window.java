package me.kirillirik;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Window {

    private static int width;
    private static int height;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private String glslVersion;
    private long window;

    private final MainWindow mainWindow;

    public Window() {
        glslVersion = null;
        mainWindow = new MainWindow();
    }

    public void init() {
        initWindow();
        initImGui();
        imGuiGlfw.init(window, true);
        imGuiGl3.init(glslVersion);
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
        ImNodes.destroyContext();
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void initWindow() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            System.out.println("Unable to initialize GLFW");
            System.exit(-1);
        }

        glslVersion = "#version 130";
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        final long primMonitor = glfwGetPrimaryMonitor();
        final GLFWVidMode mode = glfwGetVideoMode(primMonitor);

        width = mode.width();
        height = mode.height();
        window = glfwCreateWindow(width, height, "Manul", NULL, NULL);

        if (window == NULL) {
            System.out.println("Unable to create window");
            System.exit(-1);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        GL.createCapabilities();
    }

    private void initImGui() {
        ImGui.createContext();
        ImNodes.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.getFonts().addFontFromFileTTF("C:\\Windows\\Fonts\\Arial.ttf", 20, io.getFonts().getGlyphRangesCyrillic());
    }

    public void run() {
        mainWindow.init();

        while (!glfwWindowShouldClose(window)) {
            glClearColor(0.1f, 0.09f, 0.1f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            mainWindow.update();

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long backupWindowPtr = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(backupWindowPtr);
            }

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}