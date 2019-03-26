/**
 * Copyright (c) 2019 JetBrains s.r.o.
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

package com.microsoft.intellij.configuration.ui

import com.intellij.ide.plugins.newui.TwoLineProgressIndicator
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextComponentAccessor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.panels.OpaquePanel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.microsoft.intellij.configuration.AzureRiderSettings
import org.jetbrains.plugins.azure.functions.coreTools.FunctionsCoreToolsManager
import java.awt.CardLayout
import java.awt.Dimension
import java.io.File
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class AzureFunctionsConfigurationPanel: AzureRiderAbstractConfigurablePanel {

    companion object {
        private const val DISPLAY_NAME = "Functions"
    }

    private val properties = PropertiesComponent.getInstance()

    private val coreToolsPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
                "",
                "Path to Azure Functions Core Tools",
                null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT
        )
    }

    private val currentVersionLabel = JLabel()
    private val latestVersionLabel = JLabel()
    private val installButton = JButton("Download latest version")
            .apply {
                isEnabled = false
            }
    private val installIndicator = TwoLineProgressIndicator()

    private val wrapperLayout = CardLayout()
    private val installActionPanel = OpaquePanel(wrapperLayout)
            .apply {
                add(installButton, "button")
                add(installIndicator.component, "progress")
            }


    init {
        coreToolsPathField.text = properties.getValue(
                AzureRiderSettings.PROPERTY_FUNCTIONS_CORETOOLS_PATH,
                "")

        installIndicator.setCancelRunnable {
            if (installIndicator.isRunning) installIndicator.stop()
            wrapperLayout.show(installActionPanel, "button")
        }

        installButton.addActionListener {
            wrapperLayout.show(installActionPanel, "progress")
            FunctionsCoreToolsManager.downloadLatestRelease(installIndicator) {
                UIUtil.invokeAndWaitIfNeeded(Runnable {
                    coreToolsPathField.text = it
                    installButton.isEnabled = false
                    wrapperLayout.show(installActionPanel, "button")
                })

                updateVersionLabels()
            }
        }

        updateVersionLabels()
    }

    private fun updateVersionLabels() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val local = FunctionsCoreToolsManager.determineVersion(coreToolsPathField.text)
            val remote = FunctionsCoreToolsManager.determineLatestRemote()

            UIUtil.invokeAndWaitIfNeeded(Runnable {
                currentVersionLabel.text = local?.version ?: "<unknown>"
                latestVersionLabel.text = remote?.version ?: "<unknown>"

                installButton.isEnabled = local == null || remote != null && local < remote
            })
        }
    }

    override val panel = FormBuilder
            .createFormBuilder()
            .addLabeledComponent("Azure Functions Core Tools path:", coreToolsPathField)
            .addLabeledComponent("Current version:", currentVersionLabel)
            .addLabeledComponent("Latest available version:", latestVersionLabel)
            .addComponentToRightColumn(OpaquePanel(VerticalFlowLayout(VerticalFlowLayout.TOP, 0, JBUI.scale(4), false, false))
                    .apply {
                        preferredSize = Dimension(JBUI.scale(400), JBUI.scale(100))

                        add(installActionPanel)
                    }
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel

    override val displayName = DISPLAY_NAME

    override fun doOKAction() {
        if (coreToolsPathField.text != "" && File(coreToolsPathField.text).exists()) {
            properties.setValue(
                    AzureRiderSettings.PROPERTY_FUNCTIONS_CORETOOLS_PATH,
                    coreToolsPathField.text)
        }
    }
}