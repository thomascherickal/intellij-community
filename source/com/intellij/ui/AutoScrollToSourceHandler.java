package com.intellij.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.Alarm;
import com.intellij.util.OpenSourceUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class AutoScrollToSourceHandler {
  private Alarm myAutoScrollAlarm;

  protected AutoScrollToSourceHandler() {
  }

  public void install(final JTree tree) {
    myAutoScrollAlarm = new Alarm();
    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) return;

        TreePath location = tree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        if (location == null) return;

        myAutoScrollAlarm.cancelAllRequests();
        if (isAutoScrollMode()){
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              scrollToSource(tree);
            }
          });
        }
      }
    });
    tree.addTreeSelectionListener(
      new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          if (!isAutoScrollMode()) {
            return;
          }
          if (!tree.hasFocus()) {
            return;
          }
          myAutoScrollAlarm.cancelAllRequests();
          myAutoScrollAlarm.addRequest(
            new Runnable() {
              public void run() {
                scrollToSource(tree);
              }
            },
            500
          );
        }
      }
    );
  }

  protected abstract boolean isAutoScrollMode();
  protected abstract void setAutoScrollMode(boolean state);

  protected void scrollToSource(JTree tree) {
    DataContext dataContext=DataManager.getInstance().getDataContext(tree);
    OpenSourceUtil.openSourcesFrom(dataContext, false);
  }

  public ToggleAction createToggleAction() {
    return new ToggleAction("Autoscroll to Source", "Autoscroll to Source", IconLoader.getIcon("/general/autoscrollToSource.png")) {
      public boolean isSelected(AnActionEvent event) {
        return isAutoScrollMode();
      }

      public void setSelected(AnActionEvent event, boolean flag) {
        setAutoScrollMode(flag);
      }
    };
  }
}

