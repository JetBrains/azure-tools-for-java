/*
 * Copyright (c) Microsoft Corporation
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

package com.microsoft.intellij.runner.webapp.webappconfig;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.PublishingProfile;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azuretools.azurecommons.util.FileUtil;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.core.mvp.model.webapp.WebAppSettingModel;
import com.microsoft.azuretools.utils.AzureUIRefreshCore;
import com.microsoft.azuretools.utils.AzureUIRefreshEvent;
import com.microsoft.azuretools.utils.WebAppUtils;
import com.microsoft.intellij.runner.AzureRunProfileState;
import com.microsoft.intellij.runner.RunProcessHandler;
import com.microsoft.intellij.util.MavenRunTaskUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import org.apache.commons.net.ftp.FTPClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;

public class WebAppRunState extends AzureRunProfileState<WebApp> {

    private static final String CREATE_WEBAPP = "Creating new Web App...";
    private static final String CREATE_FAILED = "Failed to create Web App. Error: %s ...";
    private static final String GETTING_DEPLOYMENT_CREDENTIAL = "Getting Deployment Credential...";
    private static final String CONNECTING_FTP = "Connecting to FTP server...";
    private static final String UPLOADING_ARTIFACT = "Uploading artifact to: %s ...";
    private static final String UPLOADING_WEB_CONFIG = "Uploading web.config (check more details at: https://aka.ms/spring-boot)...";
    private static final String UPLOADING_SUCCESSFUL = "Uploading successfully...";
    private static final String STOP_WEB_APP = "Stop Web App...";
    private static final String START_WEB_APP = "Start Web App...";
    private static final String LOGGING_OUT = "Logging out of FTP server...";
    private static final String DEPLOY_SUCCESSFUL = "Deploy successfully!";
    private static final String STOP_DEPLOY = "Deploy Failed!";
    private static final String NO_WEB_APP = "Cannot get webapp for deploy.";
    private static final String NO_TARGET_FILE = "Cannot find target file: %s.";
    private static final String FAIL_FTP_STORE = "FTP client can't store the artifact, reply code: %s.";

    private static final String WEB_CONFIG_PACKAGE_PATH = "/webapp/web.config";
    private static final String BASE_PATH = "/site/wwwroot/";
    private static final String WEB_APP_BASE_PATH = BASE_PATH + "webapps/";
    private static final String CONTAINER_ROOT_PATH = WEB_APP_BASE_PATH + "ROOT";
    private static final String TEMP_FILE_PREFIX = "azuretoolkit";

    private static final int SLEEP_TIME = 5000; // milliseconds
    private static final int UPLOADING_MAX_TRY = 3;

    private final WebAppSettingModel webAppSettingModel;
    /**
     * Place to execute the Web App deployment task.
     */
    public WebAppRunState(Project project, WebAppSettingModel webAppSettingModel) {
        super(project);
        this.webAppSettingModel = webAppSettingModel;
    }

    @Nullable
    @Override
    public WebApp executeSteps(@NotNull RunProcessHandler processHandler
            , @NotNull Map<String, String> telemetryMap) throws Exception {
        File file = new File(webAppSettingModel.getTargetPath());
        if (!file.exists()) {
            throw new FileNotFoundException(String.format(NO_TARGET_FILE, webAppSettingModel.getTargetPath()));
        }
        WebApp webApp = getWebAppAccordingToConfiguration(processHandler);

        processHandler.setText(STOP_WEB_APP);
        webApp.stop();

        int indexOfDot = webAppSettingModel.getTargetName().lastIndexOf(".");
        String fileName = webAppSettingModel.getTargetName().substring(0, indexOfDot);
        String fileType = webAppSettingModel.getTargetName().substring(indexOfDot + 1);

        switch (fileType) {
            case MavenConstants.TYPE_WAR:
                try (FileInputStream input = new FileInputStream(webAppSettingModel.getTargetPath())) {
                    uploadWarArtifact(input, webApp, fileName, processHandler, telemetryMap);
                }
                break;
            case MavenConstants.TYPE_JAR:
                uploadJarArtifact(webAppSettingModel.getTargetPath(), webApp, processHandler, telemetryMap);
                break;
            default:
                break;
        }

        processHandler.setText(START_WEB_APP);
        webApp.start();

        String url = getUrl(webApp, fileName, fileType);
        processHandler.setText(DEPLOY_SUCCESSFUL);
        processHandler.setText("URL: " + url);
        return webApp;
    }

    @Override
    protected void onSuccess(WebApp result, @NotNull RunProcessHandler processHandler) {
        processHandler.notifyComplete();
        if (webAppSettingModel.isCreatingNew() && AzureUIRefreshCore.listeners != null) {
            AzureUIRefreshCore.execute(new AzureUIRefreshEvent(AzureUIRefreshEvent.EventType.REFRESH,null));
        }
        updateConfigurationDataModel(result);
        AzureWebAppMvpModel.getInstance().listAllWebAppsOnWindows(true /*force*/);
    }

    @Override
    protected void onFail(@NotNull String errMsg, @NotNull RunProcessHandler processHandler) {
        processHandler.println(errMsg, ProcessOutputTypes.STDERR);
        processHandler.notifyComplete();
    }

    @Override
    protected String getDeployTarget() {
        return "Webapp";
    }

    @Override
    protected void updateTelemetryMap(@NotNull Map<String, String> telemetryMap){
        telemetryMap.put("SubscriptionId", webAppSettingModel.getSubscriptionId());
        telemetryMap.put("CreateNewApp", String.valueOf(webAppSettingModel.isCreatingNew()));
        telemetryMap.put("CreateNewSP", String.valueOf(webAppSettingModel.isCreatingAppServicePlan()));
        telemetryMap.put("CreateNewRGP", String.valueOf(webAppSettingModel.isCreatingResGrp()));
        telemetryMap.put("FileType", MavenRunTaskUtil.getFileType(webAppSettingModel.getTargetName()));
    }

    @NotNull
    private WebApp getWebAppAccordingToConfiguration(@NotNull RunProcessHandler processHandler) throws Exception {
        WebApp webApp;
        if (webAppSettingModel.isCreatingNew()) {
            processHandler.setText(CREATE_WEBAPP);
            try {
                webApp = AzureWebAppMvpModel.getInstance().createWebApp(webAppSettingModel);
            } catch (Exception e) {
                processHandler.setText(STOP_DEPLOY);
                throw new Exception(String.format(CREATE_FAILED, e.getMessage()));
            }
        } else {
            webApp = AzureWebAppMvpModel.getInstance()
                    .getWebAppById(webAppSettingModel.getSubscriptionId(), webAppSettingModel.getWebAppId());
        }
        if (webApp == null) {
            processHandler.setText(STOP_DEPLOY);
            throw new Exception(NO_WEB_APP);
        }
        return webApp;
    }

    private void prepareWebConfig(@NotNull final WebApp webApp, @NotNull final RunProcessHandler processHandler,
                                  @NotNull final Map<String, String> telemetryMap) throws Exception {
        processHandler.setText(UPLOADING_WEB_CONFIG);
        try (InputStream inputStream = getClass().getResourceAsStream(WEB_CONFIG_PACKAGE_PATH)) {
            final File tempFolder = Files.createTempDirectory(TEMP_FILE_PREFIX).toFile();
            final File tempWebConfigFile = new File(tempFolder.getPath() + "/web.config");
            Files.copy(inputStream, tempWebConfigFile.toPath());
            int count = uploadFileViaZipDeploy(webApp, tempWebConfigFile, processHandler);
            telemetryMap.put("webConfigCount", String.valueOf(count));
        }
    }

    private void uploadWarArtifact(@NotNull final FileInputStream input, @NotNull final WebApp webApp,
                                   @NotNull final String fileName, @NotNull final RunProcessHandler processHandler,
                                   @NotNull final Map<String, String> telemetryMap) throws IOException {
        processHandler.setText(GETTING_DEPLOYMENT_CREDENTIAL);
        final PublishingProfile profile = webApp.getPublishingProfile();
        processHandler.setText(CONNECTING_FTP);
        final FTPClient ftp = WebAppUtils.getFtpConnection(profile);
        int uploadCount;

        if (webAppSettingModel.isDeployToRoot()) {
            WebAppUtils.removeFtpDirectory(ftp, CONTAINER_ROOT_PATH, processHandler);
            processHandler.setText(String.format(UPLOADING_ARTIFACT, CONTAINER_ROOT_PATH + ".war"));
            uploadCount = uploadFileToFtp(ftp, CONTAINER_ROOT_PATH + ".war", input, processHandler);
        } else {
            WebAppUtils.removeFtpDirectory(ftp, WEB_APP_BASE_PATH + fileName, processHandler);
            processHandler.setText(String.format(UPLOADING_ARTIFACT,
                WEB_APP_BASE_PATH + webAppSettingModel.getTargetName()));
            uploadCount = uploadFileToFtp(ftp, WEB_APP_BASE_PATH + webAppSettingModel.getTargetName(),
                input, processHandler);
        }
        telemetryMap.put("artifactUploadCount", String.valueOf(uploadCount));

        processHandler.setText(LOGGING_OUT);
        ftp.logout();
        if (ftp.isConnected()) {
            ftp.disconnect();
        }
    }

    private void uploadJarArtifact(@NotNull String file, @NotNull WebApp webApp,
                                  @NotNull RunProcessHandler processHandler,
                                  @NotNull Map<String, String> telemetryMap) throws Exception {
        if (webApp.operatingSystem() == OperatingSystem.WINDOWS) {
            prepareWebConfig(webApp, processHandler, telemetryMap);
        } else {
            // to align with previous telemetry date, always track the count of uploading web config
            telemetryMap.put("webConfigCount", "0");
        }

        final File originalJarFile = new File(file);
        final String jarFileName = "ROOT.jar";
        final File jarFile = new File(originalJarFile.getPath().replace(originalJarFile.getName(), jarFileName));
        originalJarFile.renameTo(jarFile);
        processHandler.setText(String.format(UPLOADING_ARTIFACT, BASE_PATH + jarFileName));
        final int uploadCount = uploadFileViaZipDeploy(webApp, jarFile, processHandler);
        telemetryMap.put("artifactUploadCount", String.valueOf(uploadCount));
    }

    // Add retry logic here to avoid Kudu's socket timeout issue.
    // More details: https://github.com/Microsoft/azure-maven-plugins/issues/339
    private int uploadFileViaZipDeploy(@NotNull WebApp webapp, @NotNull File sourceFile,
                                       @NotNull RunProcessHandler processHandler) throws Exception {
        int uploadCount = 0;
        final File targetZipFile = File.createTempFile(TEMP_FILE_PREFIX, ".zip");
        FileUtil.zipFile(sourceFile, targetZipFile);

        while (uploadCount < UPLOADING_MAX_TRY) {
            uploadCount += 1;
            try {
                webapp.zipDeploy(targetZipFile);
                processHandler.setText(UPLOADING_SUCCESSFUL);
                return uploadCount;
            } catch (Exception e) {
                processHandler.setText(
                    String.format("Upload file via zip deploy met exception: %s, retry immediately...", e.getMessage()));
            }
        }
        throw new Exception(String.format("Upload failed after %d times of retry.", UPLOADING_MAX_TRY));
    }

    /**
     * when upload file to FTP, the plugin will retry 3 times in case of unexpected errors.
     * For each try, the method will wait 5 seconds.
     */
    private int uploadFileToFtp(@NotNull FTPClient ftp, @NotNull String path,
                                 @NotNull InputStream stream, RunProcessHandler processHandler) throws IOException {
        boolean success = false;
        int count = 0;
        while (!success && ++count < UPLOADING_MAX_TRY) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            success = ftp.storeFile(path, stream);
        }
        if (!success) {
            int rc = ftp.getReplyCode();
            throw new IOException(String.format(FAIL_FTP_STORE, rc));
        }
        processHandler.setText(UPLOADING_SUCCESSFUL);
        return count;
    }

    @NotNull
    private String getUrl(@NotNull WebApp webApp, @NotNull String fileName, @NotNull String fileType) {
        String url = "https://" + webApp.defaultHostName();
        if (Comparing.equal(fileType, MavenConstants.TYPE_WAR)
                && !webAppSettingModel.isDeployToRoot()) {
            url += "/" + fileName;
        }
        return url;
    }

    private void updateConfigurationDataModel(@NotNull WebApp app) {
        webAppSettingModel.setCreatingNew(false);
        webAppSettingModel.setWebAppId(app.id());
        webAppSettingModel.setWebAppName("");
        webAppSettingModel.setResourceGroup("");
        webAppSettingModel.setAppServicePlanName("");
    }
}