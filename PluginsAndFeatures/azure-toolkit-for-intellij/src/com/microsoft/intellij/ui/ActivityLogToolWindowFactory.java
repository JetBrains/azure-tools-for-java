/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Copyright (c) 2021 JetBrains s.r.o.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.*;
import com.intellij.ui.BrowserHyperlinkListener;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.PlatformColors;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.PluginUtil;
import icons.CommonIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class ActivityLogToolWindowFactory implements ToolWindowFactory {
    public static final String ACTIVITY_LOG_WINDOW = "Azure Activity Log";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.getDefault());

    private TableView<DeploymentTableItem> table;
    private HashMap<String, DeploymentTableItem> rows = new HashMap<String, DeploymentTableItem>();
    private Project project;

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return !AzurePlugin.IS_ANDROID_STUDIO;
    }

    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        this.project = project;
        initToolWindowContent(toolWindow);
        registerDeploymentListener(project);
    }

    public void initToolWindowContent(@NotNull final ToolWindow toolWindow) {
        table = new TableView<DeploymentTableItem>(new ListTableModel<DeploymentTableItem>(DESC, PROGRESS, STATUS, START_TIME));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // add mouse listener for links in table
        table.addMouseListener(createTableMouseListener());

        toolWindow.getComponent().add(new JBScrollPane(table));
    }

    private MouseListener createTableMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (table.getSelectedColumn() == 2) {
                    DeploymentTableItem item = table.getSelectedObject();
                    if (item != null && item.link != null) {
                        try {
                            Desktop.getDesktop().browse(URI.create(item.link));
                        } catch (IOException e1) {
                            PluginUtil.displayErrorDialogAndLog(message("error"), message("error"), e1);
                        }
                    }
                }
            }
        };
    }

    private ToolWindow getOrRegisterToolWindow(Project project) {
        final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        final ToolWindow toolWindow = toolWindowManager.getToolWindow(ACTIVITY_LOG_WINDOW);
        if (toolWindow != null) {
            return toolWindow;
        }

        final ToolWindow newToolWindow = toolWindowManager.registerToolWindow(new RegisterToolWindowTask(
            ACTIVITY_LOG_WINDOW,
            ToolWindowAnchor.BOTTOM,
            null,
            false,
            true,
            false,
            !AzurePlugin.IS_ANDROID_STUDIO,
            null,
            CommonIcons.ToolWindow.AzureLog,
            null));

        initToolWindowContent(newToolWindow);
        return newToolWindow;
    }

    public void registerDeploymentListener(Project project) {
        AzurePlugin.addDeploymentEventListener(
            args -> {
                ApplicationManager.getApplication().invokeLater(() -> {

                    final ToolWindow toolWindow = getOrRegisterToolWindow(project);

                    // unique identifier for deployment
                    final String key = args.getId() + args.getStartTime().getTime();
                    if (rows.containsKey(key)) {
                        final DeploymentTableItem item = rows.get(key);
                        item.progress = args.getDeployCompleteness();
                        if (args.getDeployMessage().equalsIgnoreCase(message("runStatus"))) {
                            final String html = String.format("%s%s%s%s", "  ", "<html><a href=\"" + args.getDeploymentURL() + "\">", message("runStatusVisible"), "</a></html>");
                            item.description = message("runStatusVisible");
                            item.link = args.getDeploymentURL();
                            if (!toolWindow.isVisible()) {
                                ToolWindowManager.getInstance(project).notifyByBalloon(ACTIVITY_LOG_WINDOW, MessageType.INFO, html, null, new BrowserHyperlinkListener());
                            }
                        } else {
                            item.description = args.getDeployMessage();
                        }
                        table.getListTableModel().fireTableDataChanged();
                    } else {
                        final DeploymentTableItem item = new DeploymentTableItem(args.getId(), args.getDeployMessage(), dateFormat.format(args.getStartTime()), args.getDeployCompleteness());
                        rows.put(key, item);
                        table.getListTableModel().addRow(item);
                    }
                });
            });
    }

    private class ProgressBarRenderer implements TableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar();
        private final JLabel label = new JLabel();

        public ProgressBarRenderer() {
            progressBar.setMaximum(100);
        }

        @Override
        public Component getTableCellRendererComponent(@NotNull JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if ((Integer) value < 100) {
                progressBar.setValue((Integer) value);
                return progressBar;
            } else {
                label.setText("");
                return label;
            }
        }
    }

    private class LinkRenderer implements TableCellRenderer {
        private final JLabel label = new JLabel();

        public LinkRenderer() {
            label.setForeground(PlatformColors.BLUE);
            Font font = label.getFont();
            Map attributes = font.getAttributes();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            label.setFont(font.deriveFont(attributes));
        }

        @Override
        public Component getTableCellRendererComponent(@NotNull JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            label.setText((String) value);
            return label;
        }
    }

    private final ColumnInfo<DeploymentTableItem, String> DESC = new ColumnInfo<DeploymentTableItem, String>(message("desc")) {
        public String valueOf(DeploymentTableItem object) {
            return object.deploymentId;
        }
    };

    private final ColumnInfo<DeploymentTableItem, Integer> PROGRESS = new ColumnInfo<DeploymentTableItem, Integer>("Progress") {
        private TableCellRenderer renderer = new ProgressBarRenderer();

        public Integer valueOf(DeploymentTableItem object) {
            return object.progress;
        }

        public TableCellRenderer getRenderer(DeploymentTableItem object) {
            return renderer;
        }
    };

    private final ColumnInfo<DeploymentTableItem, String> STATUS = new ColumnInfo<DeploymentTableItem, String>(message("status")) {
        private TableCellRenderer renderer = new LinkRenderer();

        public String valueOf(DeploymentTableItem object) {
            return object.description;
        }

        public TableCellRenderer getRenderer(DeploymentTableItem object) {
            if (object.link != null) {
                return renderer;
            } else {
                return super.getRenderer(object);
            }
        }
    };

    private final ColumnInfo<DeploymentTableItem, String> START_TIME = new ColumnInfo<DeploymentTableItem, String>(message("startTime")) {
        public String valueOf(DeploymentTableItem object) {
            return object.startDate;
        }
    };

    private class DeploymentTableItem {
        private String deploymentId;
        private String description;
        private String startDate;
        private String link;
        private int progress;

        public DeploymentTableItem(String deploymentId, String description, String startDate, int progress) {
            this.deploymentId = deploymentId;
            this.description = description;
            this.startDate = startDate;
            this.progress = progress;
        }
    }
}
