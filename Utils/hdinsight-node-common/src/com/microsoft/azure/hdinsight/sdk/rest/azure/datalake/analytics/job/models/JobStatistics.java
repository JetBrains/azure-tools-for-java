/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.sdk.rest.azure.datalake.analytics.job.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Data Lake Analytics job execution statistics.
 */
public class JobStatistics {
    /**
     * The last update time for the statistics.
     */
    @JsonProperty(value = "lastUpdateTimeUtc", access = JsonProperty.Access.WRITE_ONLY)
    private String lastUpdateTimeUtc;

    /**
     * The job finalizing start time.
     */
    @JsonProperty(value = "finalizingTimeUtc", access = JsonProperty.Access.WRITE_ONLY)
    private String finalizingTimeUtc;

    /**
     * The list of stages for the job.
     */
    @JsonProperty(value = "stages", access = JsonProperty.Access.WRITE_ONLY)
    private List<JobStatisticsVertexStage> stages;

    /**
     * Get the last update time for the statistics.
     *
     * @return the lastUpdateTimeUtc value
     */
    public String lastUpdateTimeUtc() {
        return this.lastUpdateTimeUtc;
    }

    /**
     * Get the job finalizing start time.
     *
     * @return the finalizingTimeUtc value
     */
    public String finalizingTimeUtc() {
        return this.finalizingTimeUtc;
    }

    /**
     * Get the list of stages for the job.
     *
     * @return the stages value
     */
    public List<JobStatisticsVertexStage> stages() {
        return this.stages;
    }

}
