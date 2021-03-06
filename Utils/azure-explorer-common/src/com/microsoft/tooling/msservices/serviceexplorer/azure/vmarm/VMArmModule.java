/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Copyright (c) 2018-2021 JetBrains s.r.o.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.vmarm;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.authmanage.SubscriptionManager;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.sdkmanage.AzureManager;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.AzureRefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class VMArmModule extends AzureRefreshableNode {
    private static final String VM_SERVICE_MODULE_ID = com.microsoft.tooling.msservices.serviceexplorer.azure.vmarm.VMArmModule.class.getName();
    private static final String ICON_PATH = "VirtualMachine/VirtualMachine.svg";
    private static final String BASE_MODULE_NAME = "Virtual Machines";
    public static final String MODULE_NAME = "Virtual Machine";

    public VMArmModule(Node parent) {
        super(VM_SERVICE_MODULE_ID, BASE_MODULE_NAME, parent, ICON_PATH);
    }

    @Override
    public @Nullable AzureIconSymbol getIconSymbol() {
        return AzureIconSymbol.VirtualMachine.MODULE;
    }

    @Override
    protected void refreshItems() throws AzureCmdException {
        List<Pair<String, String>> failedSubscriptions = new ArrayList<>();
        try {
            AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
            // not signed in
            if (azureManager == null) {
                return;
            }

            SubscriptionManager subscriptionManager = azureManager.getSubscriptionManager();
            Set<String> sidList = subscriptionManager.getSelectedSubscriptionDetails()
                    .stream()
                    .filter(subscriptionDetail -> Objects.nonNull(subscriptionDetail) && subscriptionDetail.isSelected())
                    .map(subscriptionDetail -> subscriptionDetail.getSubscriptionId())
                    .collect(Collectors.toSet());
            for (String sid : sidList) {
                try {
                    Azure azure = azureManager.getAzure(sid);
                    List<VirtualMachine> virtualMachines = azure.virtualMachines().list();

                    for (VirtualMachine vm : virtualMachines) {
                        addChildNode(new VMNode(this, sid, vm));
                    }

                } catch (Exception ex) {
                    failedSubscriptions.add(new ImmutablePair<>(sid, ex.getMessage()));
                    continue;
                }
            }
        } catch (Exception ex) {
            DefaultLoader.getUIHelper().logError("An error occurred when trying to load Virtual Machines\n\n" + ex.getMessage(), ex);
        }
        if (!failedSubscriptions.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("An error occurred when trying to load Storage Accounts for the subscriptions:\n\n");
            for (Pair error : failedSubscriptions) {
                errorMessage.append(error.getKey()).append(": ").append(error.getValue()).append("\n");
            }
            DefaultLoader.getUIHelper().logError("An error occurred when trying to load Storage Accounts\n\n" + errorMessage.toString(), null);
        }
    }
}
