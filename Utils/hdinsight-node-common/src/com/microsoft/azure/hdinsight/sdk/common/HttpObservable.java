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

package com.microsoft.azure.hdinsight.sdk.common;

import com.microsoft.azure.hdinsight.common.StreamUtil;
import com.microsoft.azure.hdinsight.sdk.rest.ObjectConvertUtils;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import rx.Observable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpObservable {
    @NotNull
    private RequestConfig defaultRequestConfig;

    @Nullable
    private String userAgent = null;

    @NotNull
    private HeaderGroup defaultHeaders;

    @NotNull
    private CookieStore cookieStore;

    @NotNull
    private HttpContext httpContext;

    @NotNull
    private CloseableHttpClient httpClient;


    /*
     * Constructors
     */

    public HttpObservable() {
        this.defaultHeaders = new HeaderGroup();

        // set default headers
        this.defaultHeaders.setHeaders(new Header[] {
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("User-Agent", "HDInsight SDK Http Observable client"),
                new BasicHeader("X-Requested-By", "ambari")
        });

        this.cookieStore = new BasicCookieStore();
        this.httpContext = new BasicHttpContext();
        this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        // Create global request configuration
        this.defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
                .setProxyPreferredAuthSchemes(Collections.singletonList(AuthSchemes.BASIC))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(getCookieStore())
                .setDefaultRequestConfig(getDefaultRequestConfig())
                .build();
    }

    /**
     * Constructor with basic authentication
     *
     * @param username Basic authentication user name
     * @param password Basic authentication password
     */
    public HttpObservable(@NotNull final String username, @NotNull final String password) {
        this();

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials(username, password));

        this.httpClient = HttpClients.custom()
                .setDefaultCookieStore(getCookieStore())
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(getDefaultRequestConfig())
                .build();
    }

    /*
     * Getter / Setter
     */

    @NotNull
    public RequestConfig getDefaultRequestConfig() {
        return defaultRequestConfig;
    }

    public void setDefaultRequestConfig(@NotNull RequestConfig defaultRequestConfig) {
        this.defaultRequestConfig = defaultRequestConfig;
    }

    @NotNull
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(@NotNull CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @NotNull
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    @Nullable
    public Header[] getDefaultHeaders() {
        return defaultHeaders.getAllHeaders();
    }

    public void setDefaultHeader(@Nullable Header defaultHeader) {
        this.defaultHeaders.updateHeader(defaultHeader);
    }

    @NotNull
    public HttpContext getHttpContext() {
        return httpContext;
    }

    @Nullable
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(@Nullable String userAgent) {
        this.userAgent = userAgent;

        // Update the default headers
        setDefaultHeader(new BasicHeader("User-Agent", getUserAgent()));
    }

    /*
     * Helper functions
     */
    public static Observable<HttpResponse> toStringResponse(CloseableHttpResponse closeableHttpResponse) {
        return Observable.using(
                // Resource factory
                () -> closeableHttpResponse,
                // Observable factory
                streamResp -> {
                    try {
                        return Observable.just(StreamUtil.getResultFromHttpResponse(streamResp));
                    } catch (IOException e) {
                        return Observable.error(e);
                    }
                },
                // Resource dispose
                streamResp -> {
                    try {
                        streamResp.close();
                    } catch (IOException ignore) {
                        // The connection will be closed automatically after timeout,
                        // the exception in closing can be ignored.
                    }
                });
    }

    /*
     * Operations
     */
    public Observable<CloseableHttpResponse> request(@NotNull final HttpRequestBase httpRequest,
                                                     @Nullable final HttpEntity entity,
                                                     @Nullable final List<NameValuePair> parameters,
                                                     @Nullable final List<Header> addOrReplaceHeaders) {
        return Observable.fromCallable(() -> {
            URIBuilder builder = new URIBuilder(httpRequest.getURI());

            // Add parameters
            builder.addParameters(Optional.ofNullable(parameters).orElse(Collections.emptyList()));

            httpRequest.setURI(builder.build());

            // Set the default headers and update Headers
            httpRequest.setHeaders(getDefaultHeaders());
            Optional.ofNullable(addOrReplaceHeaders)
                    .ifPresent(headers -> headers.forEach(httpRequest::setHeader));

            // Set entity for non-entity
            if (httpRequest instanceof HttpEntityEnclosingRequestBase && entity != null) {
                ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity);

                // Update the content type by entity
                httpRequest.setHeader(entity.getContentType());
            }

            return getHttpClient().execute(httpRequest, getHttpContext());
        });
    }

    public Observable<CloseableHttpResponse> head(@NotNull final String uri,
                                                  @NotNull final List<NameValuePair> parameters,
                                                  @NotNull final List<Header> addOrReplaceHeaders) {
        return request(new HttpHead(uri), null, parameters, addOrReplaceHeaders);
    }

    public Observable<CloseableHttpResponse> get(@NotNull final String uri,
                                                 @Nullable final List<NameValuePair> parameters,
                                                 @Nullable final List<Header> addOrReplaceHeaders) {
        return request(new HttpGet(uri), null, parameters, addOrReplaceHeaders);
    }

    public Observable<CloseableHttpResponse> put(@NotNull final String uri,
                                                 @Nullable final HttpEntity entity,
                                                 @Nullable final List<NameValuePair> parameters,
                                                 @Nullable final List<Header> addOrReplaceHeaders) {
        return request(new HttpPut(uri), entity, parameters, addOrReplaceHeaders);
    }

    public Observable<CloseableHttpResponse> post(@NotNull final String uri,
                                                  @Nullable final HttpEntity entity,
                                                  @Nullable final List<NameValuePair> parameters,
                                                  @Nullable final List<Header> addOrReplaceHeaders) {
        return request(new HttpPost(uri), entity, parameters, addOrReplaceHeaders);
    }

    public Observable<CloseableHttpResponse> delete(@NotNull final String uri,
                                                    @Nullable final List<NameValuePair> parameters,
                                                    @Nullable final List<Header> addOrReplaceHeaders) {
        return request(new HttpDelete(uri), null, parameters, addOrReplaceHeaders);
    }
}