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

import com.intellij.ide.IdeBundle
import com.intellij.ide.util.PropertiesComponent
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.javascript.nodejs.util.NodePackageField
import com.intellij.javascript.nodejs.util.NodePackageRef
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel
import com.intellij.util.ui.SwingHelper
import com.intellij.webcore.ui.PathShortener
import com.microsoft.intellij.configuration.AzureRiderSettings
import com.microsoft.intellij.configuration.ui.AzureRiderAbstractConfigurablePanel
import org.jetbrains.plugins.azure.RiderAzureBundle
import org.jetbrains.plugins.azure.orWhenNullOrEmpty
import sun.net.util.IPAddressUtil
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.JTextField

@Suppress("UNUSED_LAMBDA_EXPRESSION")
class AzuriteConfigurationPanel(private val project: Project) : AzureRiderAbstractConfigurablePanel {

    private val disposable = Disposer.newDisposable()
    private val properties = PropertiesComponent.getInstance(project)

    private fun createPanel(): DialogPanel =
            panel {
                titledRow(RiderAzureBundle.message("settings.azurite.row.package")) {
                    // Node interpreter
                    val nodeInterpreterField = NodeJsInterpreterField(project, false)
                    row(JLabel(NodeJsInterpreterField.getLabelTextForComponent()).apply { labelFor = nodeInterpreterField }) {
                        nodeInterpreterField().withBinding(
                                { it.interpreterRef },
                                { nodeJsInterpreterField, interpreterRef -> nodeJsInterpreterField.interpreterRef = interpreterRef },
                                PropertyBinding(
                                        {
                                            NodeJsInterpreterRef.create(properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_NODE_INTERPRETER)
                                                    ?: "project")
                                        },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_NODE_INTERPRETER, it.referenceName) })
                        )
                    }

                    // Azurite package
                    val packageField = NodePackageField(nodeInterpreterField, Azurite.PackageName)
                    row(JLabel(RiderAzureBundle.message("settings.azurite.row.package.path")).apply { labelFor = packageField }) {
                        packageField().withBinding(
                                { it.selectedRef },
                                { nodePackageField, nodePackageRef -> nodePackageField.selectedRef = nodePackageRef },
                                PropertyBinding(
                                        {
                                            val packagePath = properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_NODE_PACKAGE)
                                            if (!packagePath.isNullOrEmpty()) {
                                                val nodePackage = Azurite.PackageDescriptor.createPackage(packagePath)
                                                return@PropertyBinding NodePackageRef.create(nodePackage)
                                            }
                                            return@PropertyBinding NodePackageRef.create(Azurite.PackageDescriptor.createPackage(""))
                                        },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_NODE_PACKAGE, it.constantPackage!!.systemDependentPath) })
                        ).withValidationOnInput {
                            val selected = it.selected
                            if (selected.version != null && selected.version!!.major < 3) {
                                ValidationInfo(RiderAzureBundle.message("settings.azurite.validation.invalid.package_version"), it)
                            } else {
                                null
                            }
                        }
                    }
                }

                titledRow(RiderAzureBundle.message("settings.azurite.row.general")) {
                    // Workspace folder
                    val locationField = TextFieldWithBrowseButton().apply {
                        SwingHelper.installFileCompletionAndBrowseDialog(project, this, RiderAzureBundle.message("settings.azurite.row.general.workspace.browse"), FileChooserDescriptorFactory.createSingleFolderDescriptor())
                        PathShortener.enablePathShortening(this.getTextField(), null as JTextField?)
                    }
                    row(JLabel(RiderAzureBundle.message("settings.azurite.row.general.workspace")).apply { labelFor = locationField }) {
                        locationField(comment = RiderAzureBundle.message("settings.azurite.row.general.workspace.comment")).withBinding(
                                { it.textField.text },
                                { _, _ -> { } },
                                PropertyBinding(
                                        { properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_LOCATION) },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_LOCATION, it) })
                        )
                    }

                    // Loose mode
                    row("") {
                        cell {
                            checkBox(RiderAzureBundle.message("settings.azurite.row.general.loosemode"),
                                    { properties.getBoolean(AzureRiderSettings.PROPERTY_AZURITE_LOOSE_MODE) },
                                    { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_LOOSE_MODE, it) })
                        }
                    }
                }

                // Host/port settings
                titledRow(RiderAzureBundle.message("settings.azurite.row.host")) {
                    row {
                        val blobHostLabel = JLabel(RiderAzureBundle.message("settings.azurite.row.host.blob"))
                        blobHostLabel()

                        cell {
                            textField({ properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_BLOB_HOST).orWhenNullOrEmpty(AzureRiderSettings.PROPERTY_AZURITE_BLOB_HOST_DEFAULT) },
                                    { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_BLOB_HOST, it) })
                                    .withValidationOnInput {
                                        if (it.text.isNullOrEmpty() || !IPAddressUtil.isIPv4LiteralAddress(it.text)) {
                                            ValidationInfo(RiderAzureBundle.message("settings.azurite.validation.invalid.ip"), it)
                                        } else {
                                            null
                                        }
                                    }
                                    .growPolicy(GrowPolicy.SHORT_TEXT)
                                    .component.apply { blobHostLabel.labelFor = this }
                        }

                        cell {
                            textField({ properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_BLOB_PORT).orWhenNullOrEmpty(AzureRiderSettings.PROPERTY_AZURITE_BLOB_PORT_DEFAULT) },
                                    { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_BLOB_PORT, it) })
                                    .withValidationOnInput {
                                        if (it.text.toIntOrNull() == null) {
                                            ValidationInfo(RiderAzureBundle.message("settings.azurite.validation.invalid.port"), it)
                                        } else {
                                            null
                                        }
                                    }
                                    .growPolicy(GrowPolicy.SHORT_TEXT)
                                    .component.apply { blobHostLabel.labelFor = this }
                        }
                    }

                    row {
                        val queueHostLabel = JLabel(RiderAzureBundle.message("settings.azurite.row.host.queue"))
                        queueHostLabel()

                        cell {
                            textField({ properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_HOST).orWhenNullOrEmpty(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_HOST_DEFAULT) },
                                    { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_HOST, it) })
                                    .withValidationOnInput {
                                        if (it.text.isNullOrEmpty() || !IPAddressUtil.isIPv4LiteralAddress(it.text)) {
                                            ValidationInfo(RiderAzureBundle.message("settings.azurite.validation.invalid.ip"), it)
                                        } else {
                                            null
                                        }
                                    }
                                    .growPolicy(GrowPolicy.SHORT_TEXT)
                                    .component.apply { queueHostLabel.labelFor = this }
                        }

                        cell {
                            textField({ properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_PORT).orWhenNullOrEmpty(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_PORT_DEFAULT) },
                                    { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_QUEUE_PORT, it) })
                                    .withValidationOnInput {
                                        if (it.text.toIntOrNull() == null) {
                                            ValidationInfo(RiderAzureBundle.message("settings.azurite.validation.invalid.port"), it)
                                        } else {
                                            null
                                        }
                                    }
                                    .growPolicy(GrowPolicy.SHORT_TEXT)
                                    .component.apply { queueHostLabel.labelFor = this }
                        }
                    }
                }

                // Certificate settings
                titledRow(RiderAzureBundle.message("settings.azurite.row.certificate")) {
                    val certPathField = TextFieldWithBrowseButton().apply {
                        SwingHelper.installFileCompletionAndBrowseDialog(project, this, IdeBundle.message("dialog.title.select.0", "*.pem"), FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
                        PathShortener.enablePathShortening(this.textField, null as JTextField?)
                    }
                    row(JLabel(RiderAzureBundle.message("settings.azurite.row.certificate.path")).apply { labelFor = certPathField }) {
                        certPathField(comment = RiderAzureBundle.message("settings.azurite.row.certificate.path.comment")).withBinding(
                                { it.textField.text },
                                { _, _ -> { } },
                                PropertyBinding(
                                        { properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_PATH) },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_PATH, it) })
                        )
                    }

                    val certKeyPathField = TextFieldWithBrowseButton().apply {
                        SwingHelper.installFileCompletionAndBrowseDialog(project, this, IdeBundle.message("dialog.title.select.0", "*.key"), FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
                        PathShortener.enablePathShortening(this.textField, null as JTextField?)
                    }
                    row(JLabel(RiderAzureBundle.message("settings.azurite.row.certificate.keypath")).apply { labelFor = certKeyPathField }) {
                        certKeyPathField(comment = RiderAzureBundle.message("settings.azurite.row.certificate.keypath.comment")).withBinding(
                                { it.textField.text },
                                { _, _ -> { } },
                                PropertyBinding(
                                        { properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_KEY_PATH) },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_KEY_PATH, it) })
                        )
                    }

                    val certPasswordField = JPasswordField()
                    row(JLabel(RiderAzureBundle.message("settings.azurite.row.certificate.password")).apply { labelFor = certPasswordField }) {
                        certPasswordField(growPolicy = GrowPolicy.SHORT_TEXT, comment = RiderAzureBundle.message("settings.azurite.row.certificate.password.comment")).withBinding(
                                { String(it.password) },
                                { _, _ -> { } },
                                PropertyBinding(
                                        { properties.getValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_PASSWORD) },
                                        { properties.setValue(AzureRiderSettings.PROPERTY_AZURITE_CERT_PASSWORD, it) })
                        )
                    }
                }
            }

    override val panel = createPanel().apply {
        registerValidators(disposable)
        reset()
    }

    override val displayName: String = RiderAzureBundle.message("settings.azurite.name")

    override fun doOKAction() {
        panel.apply()

        Disposer.dispose(disposable)
    }
}