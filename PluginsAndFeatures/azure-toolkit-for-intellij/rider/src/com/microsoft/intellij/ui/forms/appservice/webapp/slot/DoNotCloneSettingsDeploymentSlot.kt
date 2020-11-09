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

package com.microsoft.intellij.ui.forms.appservice.webapp.slot

import com.microsoft.azure.management.appservice.*
import com.microsoft.azure.management.appservice.implementation.AppServiceManager
import com.microsoft.azure.management.appservice.implementation.SiteInner
import com.microsoft.azure.management.resources.fluentcore.arm.Region
import com.microsoft.intellij.ui.forms.appservice.slot.DoNotCloneSettingsDeploymentSlotBase
import org.jetbrains.plugins.azure.RiderAzureBundle
import org.joda.time.DateTime
import rx.Completable
import rx.Observable
import java.io.File
import java.io.InputStream

/**
 * This class is used to include the "Do not clone settings" item into a DeploymentSlot ComboBox.
 * This is just a fake Deployment Slot with the purpose of displaying an extra option in the ComboBox.
 *
 * The only API that we use here is name(). Other are just placeholders.
 */
class DoNotCloneSettingsDeploymentSlot : DoNotCloneSettingsDeploymentSlotBase<DeploymentSlot>(), DeploymentSlot {
    override fun refresh(): DeploymentSlot { TODO("Not yet implemented") }

    override fun refreshAsync(): Observable<DeploymentSlot> { TODO("Not yet implemented") }

    override fun parent(): WebApp { TODO("Not yet implemented") }

    override fun warDeploy(p0: File?) { TODO("Not yet implemented") }

    override fun warDeploy(p0: InputStream?) { TODO("Not yet implemented") }

    override fun warDeploy(p0: File?, p1: String?) { TODO("Not yet implemented") }

    override fun warDeploy(p0: InputStream?, p1: String?) { TODO("Not yet implemented") }

    override fun warDeployAsync(p0: File?): Completable { TODO("Not yet implemented") }

    override fun warDeployAsync(p0: InputStream?): Completable { TODO("Not yet implemented") }

    override fun warDeployAsync(p0: File?, p1: String?): Completable { TODO("Not yet implemented") }

    override fun warDeployAsync(p0: InputStream?, p1: String?): Completable { TODO("Not yet implemented") }
}
