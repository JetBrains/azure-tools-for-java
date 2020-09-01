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

package com.microsoft.intellij.ui.forms.base

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.defineNestedLifetime
import com.jetbrains.rd.util.lifetime.LifetimeDefinition
import com.microsoft.intellij.ui.component.AzureComponent
import com.microsoft.intellij.ui.components.AzureDialogWrapper
import org.jetbrains.plugins.azure.RiderAzureBundle.message
import java.awt.Dimension

abstract class AzureCreateDialogBase(private val lifetimeDef: LifetimeDefinition,
                                     private val project: Project) :
        AzureDialogWrapper(project),
        AzureComponent {

    abstract val azureHelpUrl: String

    open val dialogMinWidth: Int = 300

    protected val lifetime = project.defineNestedLifetime()

    init {
        setOKButtonText(message("dialog.create.ok_button.label"))
    }

    override fun getPreferredSize() = Dimension(dialogMinWidth, -1)

    override fun doHelpAction() = BrowserUtil.open(azureHelpUrl)

    override fun doValidateAll() = validateComponent()

    override fun dispose() {
        lifetimeDef.terminate()
        super.dispose()
    }
}
