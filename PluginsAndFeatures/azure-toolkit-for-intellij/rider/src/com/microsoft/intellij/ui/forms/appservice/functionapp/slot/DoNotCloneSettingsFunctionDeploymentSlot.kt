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

package com.microsoft.intellij.ui.forms.appservice.functionapp.slot

import com.microsoft.azure.management.appservice.FunctionApp
import com.microsoft.azure.management.appservice.FunctionDeploymentSlot
import com.microsoft.intellij.ui.forms.appservice.slot.DoNotCloneSettingsDeploymentSlotBase
import rx.Observable

/**
 * This class is used to include the "Do not clone settings" item into a DeploymentSlot ComboBox.
 * This is just a fake Deployment Slot with the purpose of displaying an extra option in the ComboBox.
 *
 * The only API that we use here is name(). Other are just placeholders.
 */
class DoNotCloneSettingsFunctionDeploymentSlot : DoNotCloneSettingsDeploymentSlotBase<FunctionDeploymentSlot>(), FunctionDeploymentSlot {
    override fun refresh(): FunctionDeploymentSlot { TODO("Not yet implemented") }

    override fun refreshAsync(): Observable<FunctionDeploymentSlot> { TODO("Not yet implemented") }

    override fun parent(): FunctionApp { TODO("Not yet implemented") }
}
