package com.microsoft.intellij.runner.webapp.webappconfig.runstate

import com.microsoft.azure.management.sql.SqlDatabase
import com.microsoft.azure.management.sql.SqlServer
import com.microsoft.azuretools.authmanage.AuthMethodManager
import com.microsoft.intellij.runner.RunProcessHandler
import com.microsoft.intellij.runner.db.AzureDatabaseMvpModel
import com.microsoft.intellij.runner.webapp.AzureDotNetWebAppSettingModel
import com.microsoft.intellij.runner.webapp.webappconfig.UiConstants
import java.net.URI

object DatabaseRunState {

    fun getOrCreateSqlDatabaseFromConfig(model: AzureDotNetWebAppSettingModel.DatabaseModel,
                                                 processHandler: RunProcessHandler): SqlDatabase {
        if (model.isCreatingSqlDatabase) {
            val sqlServer = getOrCreateSqlServerFromConfiguration(model, processHandler)
            return createDatabase(sqlServer, model, processHandler)
        }

        val database = model.database ?: throw Exception(UiConstants.SQL_DATABASE_NOT_DEFINED)

        processHandler.setText(String.format(UiConstants.SQL_DATABASE_GET_EXISTING, database.name()))
        return database
    }

    fun getSqlDatabaseUri(subscriptionId: String, database: SqlDatabase): URI? {
        val azureManager = AuthMethodManager.getInstance().azureManager
        val portalUrl = azureManager.portalUrl

        // Note: [SubscriptionManager.getSubscriptionTenant()] method does not update Subscription to TenantId map while
        //       [SubscriptionManager.getSubscriptionDetails()] force to update and get the correct value
        val tenantId = azureManager.subscriptionManager.subscriptionDetails
                .find { it.subscriptionId == subscriptionId }?.tenantId ?: return null

        val path = "/#@$tenantId/resource/${database.id()}/overview".replace("/+".toRegex(), "/")
        return URI.create("$portalUrl/$path").normalize()
    }

    private fun createDatabase(sqlServer: SqlServer,
                               model: AzureDotNetWebAppSettingModel.DatabaseModel,
                               processHandler: RunProcessHandler): SqlDatabase {

        processHandler.setText(String.format(UiConstants.SQL_DATABASE_CREATE, model.databaseName))

        if (model.databaseName.isEmpty()) throw Exception(UiConstants.SQL_DATABASE_NAME_NOT_DEFINED)
        val database = AzureDatabaseMvpModel.createSqlDatabase(sqlServer, model.databaseName, model.collation)

        processHandler.setText(String.format(UiConstants.SQL_DATABASE_CREATE_SUCCESSFUL, database.id()))
        return database
    }

    private fun getOrCreateSqlServerFromConfiguration(model: AzureDotNetWebAppSettingModel.DatabaseModel,
                                                      processHandler: RunProcessHandler): SqlServer {

        val subscriptionId = model.subscription?.subscriptionId() ?: throw RuntimeException(UiConstants.SUBSCRIPTION_NOT_DEFINED)

        if (model.isCreatingSqlServer) {
            processHandler.setText(String.format(UiConstants.SQL_SERVER_CREATE, model.sqlServerName))

            if (model.sqlServerName.isEmpty()) throw Exception(UiConstants.SQL_SERVER_NAME_NOT_DEFINED)
            if (model.sqlServerLocation.isEmpty()) throw Exception(UiConstants.SQL_SERVER_REGION_NOT_DEFINED)
            if (model.dbResourceGroupName.isEmpty()) throw Exception(UiConstants.SQL_SERVER_RESOURCE_GROUP_NAME_NOT_DEFINED)
            if (model.sqlServerAdminLogin.isEmpty()) throw Exception(UiConstants.SQL_SERVER_ADMIN_LOGIN_NOT_DEFINED)
            if (model.sqlServerAdminPassword.isEmpty()) throw Exception(UiConstants.SQL_SERVER_ADMIN_PASSWORD_NOT_DEFINED)

            val sqlServer = AzureDatabaseMvpModel.createSqlServer(
                    subscriptionId,
                    model.sqlServerName,
                    model.sqlServerLocation,
                    model.isCreatingDbResourceGroup,
                    model.dbResourceGroupName,
                    model.sqlServerAdminLogin,
                    model.sqlServerAdminPassword)

            processHandler.setText(String.format(UiConstants.SQL_SERVER_CREATE_SUCCESSFUL, sqlServer.id()))

            return sqlServer
        }

        processHandler.setText(String.format(UiConstants.SQL_SERVER_GET_EXISTING, model.sqlServerId))

        if (model.sqlServerId.isEmpty()) throw Exception(UiConstants.SQL_SERVER_ID_NOT_DEFINED)
        return AzureDatabaseMvpModel.getSqlServerById(subscriptionId, model.sqlServerId)
    }
}