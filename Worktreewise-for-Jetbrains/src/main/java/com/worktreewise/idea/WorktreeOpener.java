package com.worktreewise.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.jdom.JDOMException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorktreeOpener {

    /**
     * Opens a worktree/project at the given path.
     * - If the project is already open, it brings it to focus.
     * - Otherwise, opens it in a new window if possible.
     */
    public static void open(Project currentProject, Path path) {
        File dir = path.toFile();
        if (!dir.exists()) return;

        // Refresh project directory
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);

        Path target = path.toAbsolutePath().normalize();

        // Check if project is already open
        for (Project openProject : ProjectManagerEx.getInstanceEx().getOpenProjects()) {
            String basePath = openProject.getBasePath();
            if (basePath == null) continue;

            try {
                Path open = Path.of(basePath).toAbsolutePath().normalize();
                if (Files.isSameFile(target, open)) {
                    focusProject(openProject);
                    return;
                }
            } catch (IOException ignored) {
                // Fallback for filesystem comparison issues
                if (target.toString().replace('\\', '/')
                          .equalsIgnoreCase(basePath.replace('\\', '/'))) {
                    focusProject(openProject);
                    return;
                }
            }
        }

        // Open project in a new window (2021.x compatible)
        try {
            Project opened = ProjectManagerEx.getInstanceEx().loadAndOpenProject(path.toString());

            if (opened != null) {
                VirtualFileManager.getInstance().syncRefresh();
                focusProject(opened);
            }
        } catch (IOException | JDOMException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Brings the given project to the front.
     */
    private static void focusProject(Project project) {
        IdeFrame frame = WindowManager.getInstance().getIdeFrame(project);
        if (frame == null) return;

        Component c = frame.getComponent();
        if (c instanceof JFrame) {
            JFrame jFrame = (JFrame) c;
            jFrame.setState(Frame.NORMAL);
            jFrame.toFront();
            jFrame.requestFocus();
        } else {
            c.requestFocus();
        }
    }
}
