/**
 * Copyright (c) 2019-2020 JetBrains s.r.o.
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

package org.jetbrains.plugins.azure.functions.run

import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import com.jetbrains.rd.ide.model.EnvironmentVariable
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rd.util.reactive.adviseOnce
import com.jetbrains.rider.model.*
import com.jetbrains.rider.run.configurations.controls.*
import com.jetbrains.rider.run.configurations.controls.startBrowser.BrowserSettings
import com.jetbrains.rider.run.configurations.controls.startBrowser.BrowserSettingsEditor
import com.jetbrains.rider.run.configurations.dotNetExe.DotNetExeConfigurationViewModel
import com.jetbrains.rider.run.configurations.project.DotNetStartBrowserParameters
import org.apache.http.client.utils.URIBuilder
import org.jetbrains.plugins.azure.functions.run.localsettings.FunctionLocalSettings
import org.jetbrains.plugins.azure.functions.run.localsettings.FunctionLocalSettingsUtil
import org.jetbrains.plugins.azure.functions.run.localsettings.FunctionsWorkerRuntime
import java.io.File

class AzureFunctionsHostConfigurationViewModel(
        private val lifetime: Lifetime,
        private val project: Project,
        private val runnableProjectsModel: RunnableProjectsModel,
        val projectSelector: ProjectSelector,
        val tfmSelector: StringSelector,
        programParametersEditor: ProgramParametersEditor,
        workingDirectorySelector: PathSelector,
        val functionNamesEditor: TextEditor,
        environmentVariablesEditor: EnvironmentVariablesEditor,
        useExternalConsoleEditor: FlagEditor,
        separator: ViewSeparator,
        val urlEditor: TextEditor,
        val dotNetBrowserSettingsEditor: BrowserSettingsEditor
) : DotNetExeConfigurationViewModel(
        lifetime,
        project,
        PathSelector("", null, lifetime),
        programParametersEditor,
        workingDirectorySelector,
        environmentVariablesEditor,
        FlagEditor(""),
        ProgramParametersEditor("", lifetime),
        false,
        useExternalConsoleEditor) {

    override val controls: List<ControlBase> = listOf(
            projectSelector,
            tfmSelector,
            programParametersEditor,
            workingDirectorySelector,
            functionNamesEditor,
            environmentVariablesEditor,
            useExternalConsoleEditor,
            separator,
            urlEditor,
            dotNetBrowserSettingsEditor
    )

    private var isLoaded = false
    private val type = AzureFunctionsHostConfigurationType()

    var trackProjectExePath = true
    var trackProjectArguments = true
    var trackProjectWorkingDirectory = true

    private val portRegex = Regex("(--port|-p) (\\d+)", RegexOption.IGNORE_CASE)
    private var functionLocalSettings: FunctionLocalSettings? = null

    init {
        disable()

        projectSelector.bindTo(
                runnableProjectsModel = runnableProjectsModel,
                lifetime = lifetime,
                projectFilter = { runnableProject: RunnableProject -> type.isApplicable(runnableProject.kind) },
                onLoad = ::enable,
                onSelect = ::handleProjectSelection
        )

        tfmSelector.string.advise(lifetime) { handleChangeTfmSelection() }
        exePathSelector.path.advise(lifetime) { recalculateTrackProjectOutput() }
        programParametersEditor.parametersString.advise(lifetime) { recalculateTrackProjectOutput() }
        workingDirectorySelector.path.advise(lifetime) { recalculateTrackProjectOutput() }

        // Please make sure it is executed after you enable controls through [RunConfigurationViewModelBase::enable]
        disableBrowserUrl()
    }

    private fun disableBrowserUrl() {
        urlEditor.isEnabled.set(false)
    }

    private fun handleChangeTfmSelection() {
        projectSelector.project.valueOrNull?.projectOutputs
                ?.singleOrNull { it.tfm?.presentableName == tfmSelector.string.valueOrNull }
                ?.let { projectOutput ->
                    val shouldChangeExePath = trackProjectExePath
                    val shouldChangeWorkingDirectory = trackProjectWorkingDirectory
                    if (shouldChangeExePath) {
                        exePathSelector.path.set(projectOutput.exePath)
                    }
                    if (shouldChangeWorkingDirectory) {
                        workingDirectorySelector.path.set(projectOutput.workingDirectory)
                    }
                    exePathSelector.defaultValue.set(projectOutput.exePath)

                    // Ensure we have the defaults if needed...
                    if (programParametersEditor.parametersString.value.isEmpty()) {
                        if (projectOutput.defaultArguments.isNotEmpty()) {
                            programParametersEditor.parametersString.set(ParametersListUtil.join(projectOutput.defaultArguments))
                            programParametersEditor.defaultValue.set(ParametersListUtil.join(projectOutput.defaultArguments))
                        } else {
                            programParametersEditor.parametersString.set("")
                            programParametersEditor.defaultValue.set("")
                        }
                    }
                    workingDirectorySelector.defaultValue.set(projectOutput.workingDirectory)
                }
    }

    // TODO: FIX_WHEN. Add integration tests when enabled in the plugin.
    private fun recalculateTrackProjectOutput() {
        val selectedProject = projectSelector.project.valueOrNull ?: return
        val selectedTfm = tfmSelector.string.valueOrNull ?: return

        val programParameters = programParametersEditor.parametersString.value

        selectedProject.projectOutputs.singleOrNull { it.tfm?.presentableName == selectedTfm }?.let { projectOutput ->
            trackProjectExePath = exePathSelector.path.value == projectOutput.exePath

            val defaultArguments = projectOutput.defaultArguments
            trackProjectArguments = (defaultArguments.isEmpty() ||
                    ParametersListUtil.parse(programParameters).let { params -> params.containsAll(defaultArguments) && params.size == defaultArguments.size })

            trackProjectWorkingDirectory = workingDirectorySelector.path.value == projectOutput.workingDirectory
        }

        composeUrlString(getPortValue())
    }

    private fun composeUrlString(port: Int?) {
        port ?: return
        val currentUrl = urlEditor.text.value
        val originalUrl = if (currentUrl.isNotEmpty()) currentUrl else "http://localhost"

        val updatedUrl = URIBuilder(originalUrl).setPort(port).build().toString()

        urlEditor.text.set(updatedUrl)
        urlEditor.defaultValue.set(updatedUrl)
    }

    private fun handleProjectSelection(runnableProject: RunnableProject) {
        if (!isLoaded) return
        reloadTfmSelector(runnableProject)
        readLocalSettingsForProject(runnableProject)
        composeUrlString(getPortValue())

        val startBrowserUrl = runnableProject.customAttributes.singleOrNull { it.key == Key.StartBrowserUrl }?.value ?: ""
        val launchBrowser = runnableProject.customAttributes.singleOrNull { it.key == Key.LaunchBrowser }?.value?.toBoolean() ?: false
        if (startBrowserUrl.isNotEmpty()) {
            urlEditor.defaultValue.set(startBrowserUrl)
            urlEditor.text.set(startBrowserUrl)
            dotNetBrowserSettingsEditor.settings.set(
                    BrowserSettings(
                            startAfterLaunch = launchBrowser,
                            withJavaScriptDebugger = dotNetBrowserSettingsEditor.settings.value.withJavaScriptDebugger,
                            myBrowser = dotNetBrowserSettingsEditor.settings.value.myBrowser))
        }

        environmentVariablesEditor.envs.set(runnableProject.environmentVariables.associate { it.key to it.value })
    }

    private fun reloadTfmSelector(runnableProject: RunnableProject) {
        tfmSelector.stringList.clear()
        runnableProject.projectOutputs.map { it.tfm?.presentableName ?: "" }.sorted().forEach {
            tfmSelector.stringList.add(it)
        }
        if (tfmSelector.stringList.isNotEmpty()) {
            tfmSelector.string.set(tfmSelector.stringList.first())
        }
        handleChangeTfmSelection()
    }

    private fun readLocalSettingsForProject(runnableProject: RunnableProject) {
        functionLocalSettings = FunctionLocalSettingsUtil.readFunctionLocalSettings(project, runnableProject)
    }

    private fun getPortValue(): Int? =
            getPortFromCommandArguments() ?: getPortFromLocalSettingsFile() ?: 7071

    private fun getPortFromCommandArguments(): Int? {
        val programParameters = programParametersEditor.parametersString.value
        val parametersPortMatch = portRegex.find(programParameters)
        return parametersPortMatch?.groupValues?.getOrNull(2)?.toIntOrNull()
    }

    private fun getPortFromLocalSettingsFile(): Int? = functionLocalSettings?.host?.localHttpPort

    fun reset(projectFilePath: String,
              trackProjectExePath: Boolean,
              trackProjectArguments: Boolean,
              trackProjectWorkingDirectory: Boolean,
              projectTfm: String,
              exePath: String,
              programParameters: String,
              workingDirectory: String,
              functionNames: String,
              envs: Map<String, String>,
              passParentEnvs: Boolean,
              useExternalConsole: Boolean,
              isUnloadedProject: Boolean,
              dotNetStartBrowserParameters: DotNetStartBrowserParameters) {
        fun resetProperties(exePath: String, programParameters: String, workingDirectory: String, workerRuntimeSupportsExternalConsole: Boolean) {
            super.reset(
                    exePath,
                    programParameters,
                    workingDirectory,
                    envs,
                    passParentEnvs,
                    false,
                    "",
                    workerRuntimeSupportsExternalConsole && useExternalConsole
            )
        }

        isLoaded = false

        this.trackProjectExePath = trackProjectExePath
        this.trackProjectArguments = trackProjectArguments
        this.trackProjectWorkingDirectory = trackProjectWorkingDirectory

        this.functionNamesEditor.defaultValue.value = ""
        this.functionNamesEditor.text.value = functionNames

        this.dotNetBrowserSettingsEditor.settings.set(BrowserSettings(
                startAfterLaunch = dotNetStartBrowserParameters.startAfterLaunch,
                withJavaScriptDebugger = dotNetStartBrowserParameters.withJavaScriptDebugger,
                myBrowser = dotNetStartBrowserParameters.browser))

        this.urlEditor.defaultValue.value = dotNetStartBrowserParameters.url
        this.urlEditor.text.value = dotNetStartBrowserParameters.url

        runnableProjectsModel.projects.adviseOnce(lifetime) { projectList ->

            if (projectFilePath.isEmpty() || projectList.none {
                        it.projectFilePath == projectFilePath && AzureFunctionsHostConfigurationType.isTypeApplicable(it.kind)
                    }) {
                // Case when project didn't selected otherwise we should generate fake project to avoid drop user settings.
                if (projectFilePath.isEmpty() || !isUnloadedProject) {
                    projectList.firstOrNull { type.isApplicable(it.kind) }?.let { project ->
                        projectSelector.project.set(project)
                        isLoaded = true
                        handleProjectSelection(project)
                    }
                } else {
                    val fakeProjectName = File(projectFilePath).name
                    val fakeProject = RunnableProject(
                            fakeProjectName, fakeProjectName, projectFilePath, RunnableProjectKind.Unloaded,
                            listOf(ProjectOutput(RdTargetFrameworkId("", projectTfm, false, false), exePath,
                                    ParametersListUtil.parse(programParameters), workingDirectory, "", null)),
                            envs.map { EnvironmentVariable(it.key, it.value) }.toList(), null, listOf()
                    )
                    projectSelector.projectList.apply {
                        clear()
                        addAll(projectList + fakeProject)
                    }
                    projectSelector.project.set(fakeProject)
                    reloadTfmSelector(fakeProject)
                    resetProperties(
                            exePath = exePath,
                            programParameters = programParameters,
                            workingDirectory = workingDirectory,
                            workerRuntimeSupportsExternalConsole = true)
                }
            } else {
                projectList.singleOrNull {
                    it.projectFilePath == projectFilePath && AzureFunctionsHostConfigurationType.isTypeApplicable(it.kind)
                }?.let { runnableProject ->
                    projectSelector.project.set(runnableProject)

                    // Load Function Local Settings
                    functionLocalSettings = FunctionLocalSettingsUtil.readFunctionLocalSettings(project, runnableProject)

                    // Set TFM
                    reloadTfmSelector(runnableProject)
                    val projectTfmExists = runnableProject.projectOutputs.any { it.tfm?.presentableName == projectTfm }
                    val selectedTfm = if (projectTfmExists) projectTfm else runnableProject.projectOutputs.firstOrNull()?.tfm?.presentableName ?: ""
                    tfmSelector.string.set(selectedTfm)

                    // Set Project Output
                    val projectOutput = runnableProject.projectOutputs.singleOrNull { it.tfm?.presentableName == selectedTfm }
                    val effectiveExePath = if (trackProjectExePath && projectOutput != null) projectOutput.exePath else exePath
                    val effectiveProgramParameters =
                            if (trackProjectArguments && projectOutput != null && projectOutput.defaultArguments.isNotEmpty())
                                ParametersListUtil.join(projectOutput.defaultArguments).replace("\\\"", "\"")
                            else if (programParameters.isNotEmpty())
                                programParameters
                            else
                                // Handle the case when program parameters were set by changing TFM above and make sure it is not reset to empty.
                                programParametersEditor.defaultValue.value

                    programParametersEditor.defaultValue.set(effectiveProgramParameters)

                    val effectiveWorkingDirectory = if (trackProjectWorkingDirectory && projectOutput != null)
                        projectOutput.workingDirectory else workingDirectory


                    // Disable "Use external console" for Isolated worker
                    val workerRuntime = functionLocalSettings?.values?.workerRuntime ?: FunctionsWorkerRuntime.DotNetDefault
                    val workerRuntimeSupportsExternalConsole = workerRuntime != FunctionsWorkerRuntime.DotNetIsolated
                    useExternalConsoleEditor.isVisible.set(workerRuntimeSupportsExternalConsole)

                    // Reset properties
                    resetProperties(
                            exePath = effectiveExePath,
                            programParameters = effectiveProgramParameters,
                            workingDirectory = effectiveWorkingDirectory,
                            workerRuntimeSupportsExternalConsole = workerRuntimeSupportsExternalConsole)

                    // Set Browser info
                    composeUrlString(getPortValue())
                }
            }
            isLoaded = true
        }
    }
}
