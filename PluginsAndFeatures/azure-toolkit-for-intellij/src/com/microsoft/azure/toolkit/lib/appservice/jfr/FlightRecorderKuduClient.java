/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */


package com.microsoft.azure.toolkit.lib.appservice.jfr;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.toolkit.lib.appservice.ProcessInfo;
import com.microsoft.azuretools.utils.JsonUtils;
import com.microsoft.rest.RestClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.lang3.reflect.FieldUtils;
import retrofit2.http.*;
import rx.Emitter;
import rx.Emitter.BackpressureMode;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class FlightRecorderKuduClient {
    private KuduService service;

    public FlightRecorderKuduClient(WebAppBase webAppBase) {
        if (webAppBase.defaultHostName() == null) {
            throw new UnsupportedOperationException(message("appService.jfr.error.initializeKuduClientFailed"));
        }
        String host = webAppBase.defaultHostName().toLowerCase()
                                .replace("http://", "")
                                .replace("https://", "");
        String[] parts = host.split("\\.", 2);
        host = Joiner.on('.').join(parts[0], "scm", parts[1]);


        try {
            RestClient restClient = (RestClient) FieldUtils.readDeclaredField(webAppBase.manager(), "restClient", true);
            service = restClient.newBuilder()
                                .withBaseUrl("https://" + host)
                                .withConnectionTimeout(3, TimeUnit.MINUTES)
                                .withReadTimeout(3, TimeUnit.MINUTES)
                                .build()
                                .retrofit().create(KuduService.class);
        } catch (IllegalAccessException e) {
            throw new UnsupportedOperationException(message("appService.jfr.error.noKuduService"), e);
        }

    }

    interface KuduService {
        @Headers({
                         "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps listProcesses",
                         "x-ms-body-logging: false"
                 })
        @GET("api/processes")
        @Streaming
        Observable<ResponseBody> listProcess();

        @Headers({
                         "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps command",
                         "x-ms-body-logging: false"
                 })
        @POST("api/command")
        @Streaming
        Observable<ResponseBody> execute(@Body RequestBody jsonBody);

        @Headers({
                         "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps AppServiceTunnelStatus",
                         "x-ms-body-logging: false"
                 })
        @GET("AppServiceTunnel/Tunnel.ashx?GetStatus&GetStatusAPIVer=2")
        @Streaming
        Observable<ResponseBody> getAppServiceTunnelStatus();

        @Headers({
                         "Content-Type: application/json; charset=utf-8",
                         "x-ms-logging-context: com.microsoft.azure.management.appservice.WebApps getFile"
                 })
        @GET("api/vfs/{path}")
        @Streaming
        Observable<ResponseBody> getFileContent(@Path("path") String path);
    }

    Observable<ProcessInfo[]> listProcess() {
        return service.listProcess()
                      .flatMap((Func1<ResponseBody, Observable<ProcessInfo[]>>) responseBody -> {
                          final BufferedSource source = responseBody.source();
                          return streamFromBufferedSource(source).map(FlightRecorderKuduClient::jsonToProcessArray);
                      });
    }

    Observable<String> getAppServiceTunnelStatus() {
        return service.getAppServiceTunnelStatus()
                      .flatMap((Func1<ResponseBody, Observable<String>>) responseBody -> {
                          final BufferedSource source = responseBody.source();
                          return streamFromBufferedSource(source);
                      });
    }

    Observable<CommandOutput> execute(String command, String dir) {
        ExecuteCommandRequest commandRequest = new ExecuteCommandRequest(command, dir);
        String json = JsonUtils.getGson().toJson(commandRequest);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        return service.execute(body).flatMap((Func1<ResponseBody, Observable<CommandOutput>>) responseBody -> {
            final BufferedSource source = responseBody.source();
            return streamFromBufferedSource(source).map(FlightRecorderKuduClient::jsonToCommandOutput);
        });
    }

    private static CommandOutput jsonToCommandOutput(String json) {
        return JsonUtils.fromJson(json, CommandOutput.class);
    }

    private static ProcessInfo[] jsonToProcessArray(String json) {
        Gson gson = JsonUtils.getGson();
        return gson.fromJson(json, new ProcessInfo[0].getClass());
    }

    private Observable<String> streamFromBufferedSource(final BufferedSource source) {
        return Observable.create(stringEmitter -> {
            try {
                while (!source.exhausted()) {
                    stringEmitter.onNext(source.readUtf8Line());
                }
                stringEmitter.onCompleted();
            } catch (IOException e) {
                stringEmitter.onError(e);
            }
        }, BackpressureMode.BUFFER);
    }

    public byte[] getFileContent(final String path) {
        return service.getFileContent(path).flatMap((Func1<ResponseBody, Observable<byte[]>>) responseBody -> {
            final BufferedSource source = responseBody.source();
            return Observable.create((Action1<Emitter<byte[]>>) emitter -> {
                try {
                    while (!source.exhausted()) {
                        emitter.onNext(source.readByteArray());
                    }
                    emitter.onCompleted();
                } catch (final IOException e) {
                    emitter.onError(e);
                }
            }, BackpressureMode.BUFFER);
        }).toBlocking().first();
    }

    @Setter
    @Getter
    @AllArgsConstructor
    private static class ExecuteCommandRequest {
        private String command;
        private String dir;
    }
}
