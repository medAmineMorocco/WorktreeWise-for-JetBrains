package com.worktreewise.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import com.worktreewise.idea.IdeaSettingsCopier;
import com.worktreewise.idea.WorktreeOpener;
import com.worktreewise.idea.WorktreeWiseLicense;
import com.worktreewise.model.WorktreeInfo;
import com.worktreewise.notifications.LicenceNotifications;
import com.worktreewise.services.GitWorktreeService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WorktreeWiseSwitchAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        if (!WorktreeWiseLicense.isLicensed()) {
            LicenceNotifications.showLicenseRequired();
            return;
        }

        Project project = e.getProject();
        if (project == null || project.getBasePath() == null) return;

        Path mainWorktree = Path.of(project.getBasePath());
        List<WorktreeInfo> worktrees = GitWorktreeService.listWorktrees(mainWorktree);

        JBList<WorktreeInfo> list = new JBList<>(worktrees);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String currentPath = project.getBasePath();

        // Highlight current worktree
        for (int i = 0; i < worktrees.size(); i++) {
            if (worktrees.get(i).getPath().equals(currentPath)) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                break;
            }
        }

        list.setCellRenderer((lst, value, index, selected, focused) -> {
            JLabel label = new JLabel(value.getBranch() + "  —  " + value.getPath());
            if (value.getPath().equals(currentPath)) {
                label.setOpaque(true);
                label.setBackground(new Color(200, 230, 250)); // custom highlight
                label.setForeground(Color.GRAY); // indicate disabled
                label.setEnabled(false);
            }
            return label;
        });

        IPopupChooserBuilder<WorktreeInfo> builder =
                new PopupChooserBuilder<>(list)
                        .setTitle("Switch Git Worktree")
                        .setItemChosenCallback((WorktreeInfo selected) -> {

                            if (selected == null ||
                                    selected.getPath().equals(currentPath)) return;

                            Path targetWorktree = Path.of(selected.getPath());

                            // ✅ Copy .idea only if it does NOT exist in target
                            Path targetIdea = targetWorktree.resolve(".idea");
                            if (!Files.exists(targetIdea)) {
                                try {
                                    IdeaSettingsCopier.copyAll(mainWorktree, targetWorktree);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }

                            WorktreeOpener.open(project, targetWorktree);
                        });

        builder.createPopup().showInFocusCenter();

    }
}
