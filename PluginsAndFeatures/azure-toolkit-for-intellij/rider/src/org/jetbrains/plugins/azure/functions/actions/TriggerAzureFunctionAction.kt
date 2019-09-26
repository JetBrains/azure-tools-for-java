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

package org.jetbrains.plugins.azure.functions.actions

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.ws.http.request.HttpRequestLanguage
import icons.RestClientIcons

class TriggerAzureFunctionAction(private val functionName: String) : AnAction("Trigger '$functionName' function...", "Create trigger function HTTP request...", RestClientIcons.Http_requests_filetype) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val scratchFileName = "Trigger_$functionName.http"
        val scratchFileService = ScratchFileService.getInstance()
        val scratchRootType = ScratchRootType.getInstance()
        val scratchFile = scratchFileService.findFile(scratchRootType, scratchFileName, ScratchFileService.Option.existing_only)
                ?: scratchRootType.createScratchFile(
                        project,
                        scratchFileName,
                        HttpRequestLanguage.INSTANCE,
                        "# Trigger Azure Function - $functionName\n" +
                                "#\n" +
                                "# More information can be found in the Azure Functions documentation,\n" +
                                "# as well as examples on how to pass test data to a function:\n" +
                                "# https://docs.microsoft.com/en-us/azure/azure-functions/functions-run-local#passing-test-data-to-a-function\n" +
                                "#\n" +
                                "# Make sure to update the below calls to suit your function host and port.\n" +
                                "#\n" +
                                "# You call the following endpoint to locally run HTTP and webhook triggered functions:\n" +
                                "\n" +
                                "GET http://localhost:7071/api/$functionName\n" +
                                "\n" +
                                "###\n" +
                                "\n" +
                                "# You call the following administrator endpoint to trigger non-HTTP functions:\n" +
                                "\n" +
                                "POST http://localhost:7071/admin/functions/$functionName\n" +
                                "Content-Type: application/json\n" +
                                "\n" +
                                "{}\n",
                        ScratchFileService.Option.create_if_missing)

        if (scratchFile != null) {
            FileEditorManager.getInstance(project).openFile(scratchFile, true)
        }
    }
}
