package com.microsoft.intellij.runner.webapp.webappconfig.ui

import com.intellij.openapi.project.Project
import com.jetbrains.rider.model.PublishableProjectModel
import com.jetbrains.rider.model.publishableProjectsModel
import com.jetbrains.rider.projectView.solution
import com.microsoft.azure.management.appservice.AppServicePlan
import com.microsoft.azure.management.appservice.WebApp
import com.microsoft.azure.management.resources.Location
import com.microsoft.azure.management.resources.ResourceGroup
import com.microsoft.azure.management.resources.Subscription
import com.microsoft.azure.management.sql.SqlDatabase
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel
import com.microsoft.azuretools.core.mvp.model.ResourceEx
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter
import com.microsoft.azuretools.core.mvp.ui.base.SchedulerProviderFactory
import com.microsoft.intellij.runner.db.AzureDatabaseMvpModel
import com.microsoft.tooling.msservices.components.DefaultLoader
import rx.Observable

class DotNetWebAppDeployViewPresenter<V : DotNetWebAppDeployMvpView> : MvpPresenter<V>() {

    companion object {

        private const val CANNOT_LIST_WEB_APP = "Failed to list web apps."
        private const val CANNOT_LIST_RES_GRP = "Failed to list resource groups."
        private const val CANNOT_LIST_APP_SERVICE_PLAN = "Failed to list app service plan."
        private const val CANNOT_LIST_SUBSCRIPTION = "Failed to list subscriptions."
        private const val CANNOT_LIST_LOCATION = "Failed to list locations."
        private const val CANNOT_LIST_PRICING_TIER = "Failed to list databaseEdition tier."
        private const val CANNOT_LIST_SQL_DATABASE = "Failed to list SQL Database."
    }

    fun onRefresh() {
        loadWebApps(true)
    }

    fun onLoadWebApps() {
        loadWebApps(false)
    }

    /**
     * Load subscriptions from model.
     */
    fun onLoadSubscription() {
        Observable.fromCallable<List<Subscription>> { AzureMvpModel.getInstance().selectedSubscriptions }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ subscriptions ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.fillSubscription(subscriptions)
                    }
                }, { e -> errorHandler(CANNOT_LIST_SUBSCRIPTION, e as Exception) })
    }

    /**
     * Load resource groups from model.
     */
    fun onLoadResourceGroups(sid: String) {
        Observable.fromCallable<List<ResourceGroup>> { AzureMvpModel.getInstance().getResourceGroupsBySubscriptionId(sid) }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ resourceGroups ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.fillResourceGroup(resourceGroups)
                    }
                }, { e -> errorHandler(CANNOT_LIST_RES_GRP, e as Exception) })
    }

    /**
     * Load app service plan by resource group from model.
     */
    fun onLoadAppServicePlan(sid: String, group: String) {
        Observable.fromCallable<List<AppServicePlan>> {
            AzureWebAppMvpModel.getInstance()
                    .listAppServicePlanBySubscriptionIdAndResourceGroupName(sid, group)
        }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ appServicePlans ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.fillAppServicePlan(appServicePlans)
                    }
                }, { e -> errorHandler(CANNOT_LIST_APP_SERVICE_PLAN, e as Exception) })
    }

    /**
     * Load app service plan from model.
     */
    fun onLoadAppServicePlan(sid: String) {
        Observable.fromCallable<List<AppServicePlan>> {
            AzureWebAppMvpModel.getInstance()
                    .listAppServicePlanBySubscriptionId(sid)
        }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ appServicePlans ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.fillAppServicePlan(appServicePlans)
                    }
                }, { e -> errorHandler(CANNOT_LIST_APP_SERVICE_PLAN, e as Exception) })
    }

    /**
     * Load locations from model.
     */
    fun onLoadLocation(sid: String) {
        Observable.fromCallable<List<Location>> { AzureMvpModel.getInstance().listLocationsBySubscriptionId(sid) }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ locations ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.fillLocation(locations)
                    }
                }, { e -> errorHandler(CANNOT_LIST_LOCATION, e as Exception) })
    }

    /**
     * Load databaseEdition tier from model.
     */
    fun onLoadPricingTier() {
        try {
            mvpView.fillPricingTier(AzureMvpModel.getInstance().listPricingTier())
        } catch (e: IllegalAccessException) {
            errorHandler(CANNOT_LIST_PRICING_TIER, e)
        }

    }

    fun onLoadSqlDatabase() {
        Observable.fromCallable<List<SqlDatabase>> { AzureDatabaseMvpModel.listSqlDatabases() }
                .subscribeOn(schedulerProvider.io())
                .subscribe( { databases ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) return@invokeLater
                        mvpView.fillSqlDatabase(databases)
                    }
                }, { e -> errorHandler(CANNOT_LIST_SQL_DATABASE, e as Exception) })
    }

    private fun loadWebApps(forceRefresh: Boolean) {
        Observable.fromCallable<List<ResourceEx<WebApp>>> { AzureWebAppMvpModel.getInstance().listWebApps(forceRefresh) }
                .subscribeOn(schedulerProvider.io())
                .subscribe({ webAppList ->
                    DefaultLoader.getIdeHelper().invokeLater {
                        if (isViewDetached) {
                            return@invokeLater
                        }
                        mvpView.renderWebAppsTable(webAppList)
                    }
                }, { e -> errorHandler(CANNOT_LIST_WEB_APP, e as Exception) })
    }

    private fun errorHandler(msg: String, e: Exception) {
        DefaultLoader.getIdeHelper().invokeLater {
            if (isViewDetached) {
                return@invokeLater
            }
            mvpView.onErrorWithException(msg, e)
        }
    }
}
