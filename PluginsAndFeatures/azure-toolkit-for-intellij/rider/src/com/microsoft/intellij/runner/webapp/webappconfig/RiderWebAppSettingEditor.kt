/**
 * Copyright (c) 2018 JetBrains s.r.o.
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.intellij.runner.webapp.webappconfig

import com.intellij.openapi.project.Project
import com.jetbrains.rider.protocol.IPermittedModalities
import com.jetbrains.rider.util.idea.lifetime
import com.microsoft.intellij.runner.AzureRiderSettingPanel
import com.microsoft.intellij.runner.AzureRiderSettingsEditor
import com.microsoft.intellij.runner.webapp.webappconfig.ui.RiderWebAppSettingPanel
import javax.swing.JComponent

class RiderWebAppSettingEditor(project: Project,
                               webAppConfiguration: RiderWebAppConfiguration)
    : AzureRiderSettingsEditor<RiderWebAppConfiguration>() {

    private val lifetimeDef = project.lifetime.createNestedDef()

    private val myPanel: RiderWebAppSettingPanel = RiderWebAppSettingPanel(lifetimeDef.lifetime, project, webAppConfiguration)

    override val panel: AzureRiderSettingPanel<RiderWebAppConfiguration>
        get() = myPanel

    override fun createEditor(): JComponent {
        IPermittedModalities.getInstance().allowPumpProtocolUnderCurrentModality()
        return super.createEditor()
    }

    override fun disposeEditor() {
        super.disposeEditor()
        lifetimeDef.terminate()
    }
}
