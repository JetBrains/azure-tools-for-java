/**
 * Copyright (c) 2020 JetBrains s.r.o.
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.function.deploymentslot

import com.microsoft.azure.management.appservice.FunctionDeploymentSlot
import com.microsoft.azuretools.core.mvp.model.functionapp.AzureFunctionAppMvpModel
import com.microsoft.tooling.msservices.serviceexplorer.azure.appservice.slot.DeploymentSlotNodePresenterBase
import com.microsoft.tooling.msservices.serviceexplorer.azure.appservice.slot.DeploymentSlotNodeView

class FunctionDeploymentSlotNodePresenter<TView : DeploymentSlotNodeView>
    : DeploymentSlotNodePresenterBase<FunctionDeploymentSlot, TView>() {

    override fun getStartDeploymentSlotAction(subscriptionId: String, appId: String, slotName: String) {
        AzureFunctionAppMvpModel.startDeploymentSlot(subscriptionId, appId, slotName)
    }

    override fun getStopDeploymentSlotAction(subscriptionId: String, appId: String, slotName: String) {
        AzureFunctionAppMvpModel.stopDeploymentSlot(subscriptionId, appId, slotName)
    }

    override fun getRestartDeploymentSlotAction(subscriptionId: String, appId: String, slotName: String) {
        AzureFunctionAppMvpModel.restartDeploymentSlot(subscriptionId, appId, slotName)
    }

    override fun getDeploymentSlotAction(subscriptionId: String, appId: String, slotName: String): FunctionDeploymentSlot =
        AzureFunctionAppMvpModel.getDeploymentSlotByName(subscriptionId, appId, slotName)

    override fun onSwapWithProduction(subscriptionId: String, appId: String, slotName: String) {
        AzureFunctionAppMvpModel.swapSlotWithProduction(subscriptionId, appId, slotName)
    }
}
