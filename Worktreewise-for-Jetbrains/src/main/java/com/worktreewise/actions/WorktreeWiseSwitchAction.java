package com.worktreewise.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.components.JBList;
import com.worktreewise.idea.IdeaSettingsCopier;
import com.worktreewise.idea.WorktreeOpener;
import com.worktreewise.model.WorktreeInfo;
import com.worktreewise.services.GitWorktreeService;

import javax.swing.*;
import java.nio.file.Path;
import java.util.List;

public class WorktreeWiseSwitchAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null || project.getBasePath() == null) return;

        Path mainWorktree = Path.of(project.getBasePath());
        List<WorktreeInfo> worktrees =
                GitWorktreeService.listWorktrees(mainWorktree);

        JBList<WorktreeInfo> list = new JBList<>(worktrees);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.setCellRenderer((lst, value, index, selected, focused) ->
                new JLabel(value.getBranch() + "  —  " + value.getPath())
        );

        JBPopupFactory.getInstance()
                      .createListPopupBuilder(list)
                      .setTitle("Switch Git Worktree")
                      .setItemChoosenCallback(() -> {
                          WorktreeInfo selected = list.getSelectedValue();
                          if (selected == null) return;

                          Path targetWorktree = Path.of(selected.getPath());

                          // ✅ Copy .idea ONLY when chosen
                          try {
                              IdeaSettingsCopier.copyAll(mainWorktree, targetWorktree);
                          } catch (Exception ignored) {}

                          // ✅ Open the worktree (single execution guaranteed)
                          WorktreeOpener.open(project, targetWorktree);
                      })
                      .createPopup()
                      .showInFocusCenter();
    }
}
