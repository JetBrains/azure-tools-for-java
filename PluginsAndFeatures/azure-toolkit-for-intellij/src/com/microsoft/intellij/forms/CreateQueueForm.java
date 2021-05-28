/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Copyright (c) 2021 JetBrains s.r.o.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.forms;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.intellij.helpers.LinkListener;
import com.microsoft.intellij.ui.components.AzureDialogWrapper;
import com.microsoft.intellij.ui.messages.AzureBundle;
import com.microsoft.intellij.util.PluginUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.azure.sdk.StorageClientSDKManager;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;
import com.microsoft.tooling.msservices.model.storage.Queue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CreateQueueForm extends AzureDialogWrapper {
    private JLabel namingGuidelinesLink;
    private JTextField nameTextField;
    private Runnable onCreate;
    private ClientStorageAccount storageAccount;
    private static final String NAME_REGEX = "^[a-z0-9](?!.*--)[a-z0-9-]+[a-z0-9]$";
    private static final int NAME_MAX = 63;
    private static final int NAME_MIN = 3;
    private JPanel contentPane;
    private Project project;

    public CreateQueueForm(Project project) {
        super(project, true);

        this.project = project;

        setTitle("Create Queue");
        namingGuidelinesLink.addMouseListener(new LinkListener("https://go.microsoft.com/fwlink/?LinkId=255557"));

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                changedName();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                changedName();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                changedName();
            }
        });

        init();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return nameTextField;
    }

    private void changedName() {

        setOKActionEnabled(nameTextField.getText().length() > 0);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        final String name = nameTextField.getText();

        if (name.length() < NAME_MIN || name.length() > NAME_MAX || !name.matches(NAME_REGEX)) {
            return new ValidationInfo("Queue names must start with a letter or number, and can contain only letters, numbers, and the dash (-) character.\n" +
                    "Every dash (-) character must be immediately preceded and followed by a letter or number; consecutive dashes are not permitted in container names.\n" +
                    "All letters in a container name must be lowercase.\n" +
                    "Queue names must be from 3 through 63 characters long.", nameTextField);
        }

        return null;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    protected void doOKAction() {
        final String name = nameTextField.getText();

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Creating queue...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    progressIndicator.setIndeterminate(true);

                    for (Queue queue : StorageClientSDKManager.getManager().getQueues(storageAccount)) {
                        if (queue.getName().equals(name)) {
                            DefaultLoader.getIdeHelper().invokeLater(
                                () -> JOptionPane.showMessageDialog(null, "A queue with the specified name already exists.", "Azure Explorer", JOptionPane.ERROR_MESSAGE));

                            return;
                        }
                    }

                    Queue queue = new Queue(name, "", 0);
                    StorageClientSDKManager.getManager().createQueue(storageAccount, queue);

                    if (onCreate != null) {
                        DefaultLoader.getIdeHelper().invokeLater(onCreate);
                    }
                } catch (AzureCmdException e) {
                    String msg = "An error occurred while attempting to create queue." + "\n" + String.format(AzureBundle.message("webappExpMsg"), e.getMessage());
                    PluginUtil.displayErrorDialogAndLog(AzureBundle.message("errTtl"), msg, e);
                }
            }
        });

        sendTelemetry(OK_EXIT_CODE);
        close(DialogWrapper.OK_EXIT_CODE, true);
    }

    public void setOnCreate(Runnable onCreate) {
        this.onCreate = onCreate;
    }

    public void setStorageAccount(ClientStorageAccount storageAccount) {
        this.storageAccount = storageAccount;
    }

}
