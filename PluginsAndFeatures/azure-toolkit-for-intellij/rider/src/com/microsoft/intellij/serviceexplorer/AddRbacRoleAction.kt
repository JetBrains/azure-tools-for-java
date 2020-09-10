package com.microsoft.intellij.serviceexplorer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager
import com.microsoft.azuretools.authmanage.AuthMethodManager
import com.microsoft.azuretools.authmanage.RefreshableTokenCredentials
import com.microsoft.azuretools.ijidea.actions.AzureSignInAction
import com.microsoft.tooling.msservices.helpers.Name
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageNode
import org.jetbrains.plugins.azure.currentAzureUser
import java.util.*

@Name("Add RBAC role")
class AddRbacRoleAction(private val storageNode: StorageNode) : NodeActionListener() {

    override fun actionPerformed(e: NodeActionEvent?) {

        val project = storageNode.project as? Project ?: return
        if (!AzureSignInAction.doSignIn(AuthMethodManager.getInstance(), project)) return

        val application = ApplicationManager.getApplication()
        val azureManager = AuthMethodManager.getInstance().azureManager ?: return

        application.invokeLater {

            val subscriptions = azureManager.subscriptionManager.subscriptionDetails
                    .asSequence()
                    .filter { it.isSelected }
                    .toList()

            val subscription = subscriptions[0]
            val tokenCredentials = RefreshableTokenCredentials(azureManager, subscription.tenantId)

            val rbacManager = GraphRbacManager.configure().authenticate(tokenCredentials)
            val user = azureManager.currentAzureUser(tokenCredentials)!!

            val roleDefinition = rbacManager.roleDefinitions()
                    .listByScope( storageNode.storageAccount.id() )
                    .first { it.roleName().contains("blob data contributor", true) }

            val assignment = rbacManager.roleAssignments()
                    .define(UUID.randomUUID().toString())
                    .forObjectId(user.objectId)
                    .withRoleDefinition(roleDefinition!!.id())
                    .withResourceScope(storageNode.storageAccount)

            rbacManager.roleAssignments().create(assignment)
        }
    }

}