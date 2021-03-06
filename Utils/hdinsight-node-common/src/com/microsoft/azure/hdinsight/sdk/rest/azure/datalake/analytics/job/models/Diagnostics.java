/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.sdk.rest.azure.datalake.analytics.job.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error diagnostic information for failed jobs.
 */
public class Diagnostics {
    /**
     * The error message.
     */
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * The severity of the error. Possible values include: 'Warning', 'Error', 'Info', 'SevereWarning', 'Deprecated',
     * 'UserWarning'.
     */
    @JsonProperty(value = "severity", access = JsonProperty.Access.WRITE_ONLY)
    private SeverityTypes severity;

    /**
     * The line number the error occured on.
     */
    @JsonProperty(value = "lineNumber", access = JsonProperty.Access.WRITE_ONLY)
    private Integer lineNumber;

    /**
     * The column where the error occured.
     */
    @JsonProperty(value = "columnNumber", access = JsonProperty.Access.WRITE_ONLY)
    private Integer columnNumber;

    /**
     * The starting index of the error.
     */
    @JsonProperty(value = "start", access = JsonProperty.Access.WRITE_ONLY)
    private Integer start;

    /**
     * The ending index of the error.
     */
    @JsonProperty(value = "end", access = JsonProperty.Access.WRITE_ONLY)
    private Integer end;

    /**
     * Get the error message.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Get the severity of the error. Possible values include: 'Warning', 'Error', 'Info', 'SevereWarning', 'Deprecated', 'UserWarning'.
     *
     * @return the severity value
     */
    public SeverityTypes severity() {
        return this.severity;
    }

    /**
     * Get the line number the error occured on.
     *
     * @return the lineNumber value
     */
    public Integer lineNumber() {
        return this.lineNumber;
    }

    /**
     * Get the column where the error occured.
     *
     * @return the columnNumber value
     */
    public Integer columnNumber() {
        return this.columnNumber;
    }

    /**
     * Get the starting index of the error.
     *
     * @return the start value
     */
    public Integer start() {
        return this.start;
    }

    /**
     * Get the ending index of the error.
     *
     * @return the end value
     */
    public Integer end() {
        return this.end;
    }

}
