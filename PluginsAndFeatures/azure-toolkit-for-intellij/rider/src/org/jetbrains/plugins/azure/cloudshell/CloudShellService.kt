/**
 * Copyright (c) 2018-2021 JetBrains s.r.o.
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

package org.jetbrains.plugins.azure.cloudshell

import com.intellij.openapi.project.Project
import org.jetbrains.plugins.azure.cloudshell.terminal.AzureCloudProcessTtyConnector

class CloudShellService {

    companion object {
        fun getInstance(project: Project): CloudShellService = project.getService(CloudShellService::class.java)
    }

    private val connectors = mutableListOf<AzureCloudProcessTtyConnector>()

    fun registerConnector(connector: AzureCloudProcessTtyConnector) {
        connectors.add(connector)
    }

    fun unregisterConnector(connector: AzureCloudProcessTtyConnector) {
        connectors.remove(connector)
    }

    fun activeConnector() : AzureCloudProcessTtyConnector? {
        return connectors.firstOrNull { it.isConnected }
    }
}