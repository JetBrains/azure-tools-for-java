/**
 * Copyright (c) 2020 JetBrains s.r.o.
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

package org.jetbrains.plugins.azure.storage.azurite

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.services.SimpleServiceViewDescriptor
import com.intellij.execution.ui.ConsoleView
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanelWithEmptyText
import com.microsoft.icons.CommonIcons
import org.jetbrains.plugins.azure.RiderAzureBundle
import org.jetbrains.plugins.azure.storage.azurite.actions.StartAzuriteAction
import org.jetbrains.plugins.azure.storage.azurite.actions.ShowAzuriteSettingsAction
import org.jetbrains.plugins.azure.storage.azurite.actions.StopAzuriteAction
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JPanel

open class AzuriteServiceViewSessionDescriptor(private val project: Project)
    : SimpleServiceViewDescriptor(RiderAzureBundle.message("service.azurite.name"), CommonIcons.Azurite) {

    companion object {
        val defaultToolbarActions = DefaultActionGroup(
                StartAzuriteAction(),
                StopAzuriteAction(),
                ShowAzuriteSettingsAction()
        )
    }

    private val azuriteService = service<AzuriteService>()
    private var processHandler: ColoredProcessHandler? = null
    private var workspace: String? = null

    private val consoleView: ConsoleView = TextConsoleBuilderFactory.getInstance()
            .createBuilder(project).apply { setViewer(true) }.console

    protected val panel = createEmptyComponent()

    override fun getToolbarActions() = defaultToolbarActions

    override fun getPresentation(): ItemPresentation {
        val superPresentation = super.getPresentation()
        if (workspace != null) {
            return object : ItemPresentation {
                override fun getLocationString(): String? {
                    return workspace
                }

                override fun getIcon(p: Boolean) = superPresentation.getIcon(p)

                override fun getPresentableText() = superPresentation.presentableText
            }
        }
        return superPresentation
    }

    override fun getContentComponent(): JComponent? {

        azuriteService.processHandler?.let { activeProcessHandler ->

            if (processHandler != activeProcessHandler) {
                processHandler?.detachProcess()
                processHandler = activeProcessHandler
                workspace = azuriteService.workspace

                consoleView.attachToProcess(activeProcessHandler)
            }
        }

        if (processHandler != null && panel.components.isEmpty()) {
            panel.add(consoleView.component, BorderLayout.CENTER)
        }

        return panel
    }

    private fun createEmptyComponent(): JPanel {
        val panel: JPanel = JBPanelWithEmptyText(BorderLayout())
                .withEmptyText(RiderAzureBundle.message("service.azurite.not_started"))
                .withBorder(BorderFactory.createEmptyBorder())
        panel.isFocusable = true
        return panel
    }
}