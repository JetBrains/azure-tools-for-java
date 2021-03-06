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

package com.microsoft.intellij.serviceexplorer

import com.google.common.collect.ImmutableList
import com.microsoft.azure.toolkit.intellij.appservice.action.SSHIntoWebAppAction
import com.microsoft.azure.toolkit.intellij.appservice.action.StartStreamingLogsAction
import com.microsoft.azure.toolkit.intellij.appservice.action.StopStreamingLogsAction
import com.microsoft.intellij.serviceexplorer.azure.database.actions.*
import com.microsoft.intellij.serviceexplorer.azure.functionapp.actions.FunctionAppCreateAction
import com.microsoft.intellij.serviceexplorer.azure.functionapp.actions.FunctionAppCreateDeploymentSlotAction
import com.microsoft.intellij.serviceexplorer.azure.webapp.actions.WebAppCreateAction
import com.microsoft.intellij.serviceexplorer.azure.webapp.actions.WebAppCreateDeploymentSlotAction
import com.microsoft.tooling.msservices.serviceexplorer.Node
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener
import com.microsoft.tooling.msservices.serviceexplorer.azure.database.AzureDatabaseModule
import com.microsoft.tooling.msservices.serviceexplorer.azure.database.sqldatabase.SqlDatabaseNode
import com.microsoft.tooling.msservices.serviceexplorer.azure.database.sqlserver.SqlServerNode
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.FunctionAppNode
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.FunctionModule
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.FunctionNode
import com.microsoft.tooling.msservices.serviceexplorer.azure.function.deploymentslot.FunctionDeploymentSlotModule
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppModule
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppNode
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotModule

class RiderNodeActionsMap : NodeActionsMap() {

    override fun getMap(): Map<Class<out Node>, ImmutableList<Class<out NodeActionListener>>> {
        return node2Actions
    }

    companion object {
        private val node2Actions = HashMap<Class<out Node>, ImmutableList<Class<out NodeActionListener>>>()

        init {
            node2Actions[AzureDatabaseModule::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(SqlServerCreateAction::class.java)
                .build()

            node2Actions[SqlServerNode::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(SqlDatabaseCreateAction::class.java)
                .add(SqlServerOpenInPortalAction::class.java)
                .add(SqlServerAddCurrentIpAddressToFirewallAction::class.java)
                .add(SqlServerConnectDataSourceAction::class.java)
                .build()

            node2Actions[SqlDatabaseNode::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(SqlDatabaseOpenInPortalAction::class.java)
                .add(SqlDatabaseAddCurrentIpAddressToFirewallAction::class.java)
                .add(SqlDatabaseConnectDataSourceAction::class.java)
                .build()

            node2Actions[WebAppModule::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(WebAppCreateAction::class.java)
                .build()

            node2Actions[WebAppNode::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(StartStreamingLogsAction::class.java)
                .add(StopStreamingLogsAction::class.java)
                .add(SSHIntoWebAppAction::class.java)
                .build()

            node2Actions[DeploymentSlotModule::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(WebAppCreateDeploymentSlotAction::class.java)
                .build()

            // TODO: Check for com.microsoft.azure.toolkit.intellij.function.action.CreateFunctionAppAction
            node2Actions[FunctionModule::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(FunctionAppCreateAction::class.java)
                .build()

            node2Actions[FunctionNode::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(StartStreamingLogsAction::class.java)
                .add(StopStreamingLogsAction::class.java)
                .build()

            node2Actions[FunctionAppNode::class.java] = ImmutableList.Builder<Class<out NodeActionListener?>>()
                 .add(StartStreamingLogsAction::class.java)
                 .add(StopStreamingLogsAction::class.java)
                 .build()

            node2Actions[FunctionDeploymentSlotModule::class.java] = ImmutableList.Builder<Class<out NodeActionListener>>()
                .add(FunctionAppCreateDeploymentSlotAction::class.java)
                .build()
        }
    }
}
