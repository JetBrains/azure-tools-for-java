/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.hdinsight.sdk.rest.azure.datalake.analytics.job.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for SeverityTypes.
 */
public enum SeverityTypes {
    /** Enum value Warning. */
    WARNING("Warning"),

    /** Enum value Error. */
    ERROR("Error"),

    /** Enum value Info. */
    INFO("Info"),

    /** Enum value SevereWarning. */
    SEVERE_WARNING("SevereWarning"),

    /** Enum value Deprecated. */
    DEPRECATED("Deprecated"),

    /** Enum value UserWarning. */
    USER_WARNING("UserWarning");

    /** The actual serialized value for a SeverityTypes instance. */
    private String value;

    SeverityTypes(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a SeverityTypes instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed SeverityTypes object, or null if unable to parse.
     */
    @JsonCreator
    public static SeverityTypes fromString(String value) {
        SeverityTypes[] items = SeverityTypes.values();
        for (SeverityTypes item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
