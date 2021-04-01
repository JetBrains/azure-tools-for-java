/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Copyright (c) 2018-2021 JetBrains s.r.o.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer.azure.storagearm;

import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.intellij.actions.AzureSignInAction;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.forms.CreateArmStorageAccountForm;
import com.microsoft.intellij.util.AzureLoginHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.AzureActionEnum;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageModule;

@Name("New Storage Account...")
public class CreateStorageAccountAction extends NodeActionListener {

    public static final String ERROR_CREATING_STORAGE_ACCOUNT = "Error creating storage account";
    private StorageModule storageModule;

    public CreateStorageAccountAction(StorageModule storageModule) {
        this.storageModule = storageModule;
    }

    @Override
    public AzureActionEnum getAction() {
        return AzureActionEnum.CREATE;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        Project project = (Project) storageModule.getProject();
        AzureSignInAction.doSignIn(AuthMethodManager.getInstance(), project).subscribe((isSuccess) -> {
            this.doActionPerformed(e, isSuccess, project);
        });
    }

    private void doActionPerformed(NodeActionEvent e, boolean isLoggedIn, Project project) {
        try {
            if (!isLoggedIn) {
                return;
            }
            if (!AzureLoginHelper.isAzureSubsAvailableOrReportError(ERROR_CREATING_STORAGE_ACCOUNT)) {
                return;
            }
            CreateArmStorageAccountForm createStorageAccountForm = new CreateArmStorageAccountForm((Project) storageModule.getProject());
            createStorageAccountForm.fillFields(null, null);

            createStorageAccountForm.setOnCreate(new Runnable() {
                @Override
                public void run() {
                    storageModule.load(false);
                }
            });
            createStorageAccountForm.show();
        } catch (Throwable ex) {
            AzurePlugin.log(ERROR_CREATING_STORAGE_ACCOUNT, ex);
            throw new RuntimeException("Error creating storage account", ex);
        }
    }

    @Override
    protected @Nullable String getIconPath() {
        return "AddEntity.svg";
    }
}
