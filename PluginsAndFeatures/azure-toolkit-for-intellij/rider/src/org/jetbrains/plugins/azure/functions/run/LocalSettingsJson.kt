/**
 * Copyright (c) 2021 JetBrains s.r.o.
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

package org.jetbrains.plugins.azure.functions.run

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.intellij.openapi.diagnostic.Logger
import java.io.File

object LocalSettingsJson {

    private val logger = Logger.getInstance(LocalSettingsJson::class.java)

    private fun determineLocalSettingsJsonFile(workingDirectory: String): File? {
        val candidates = listOf(
                File(workingDirectory, "local.settings.json"),
                File(workingDirectory, "../local.settings.json"),
                File(workingDirectory, "../../local.settings.json")
        )

        return candidates.firstOrNull { it.exists() }?.canonicalFile
    }

    fun tryDetermineWorkerRuntime(workingDirectory: String): String? {

        val localSettingsJsonFile = determineLocalSettingsJsonFile(workingDirectory)

        if (localSettingsJsonFile == null) {
            logger.warn("Could not find local.settings.json file.")
            return null
        }

        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val localSettingsJson = gson.fromJson(localSettingsJsonFile.readText(), JsonElement::class.java).asJsonObject

            return localSettingsJson["Values"].asJsonObject["FUNCTIONS_WORKER_RUNTIME"].asString
        } catch (e: JsonParseException) {
            logger.error(e)
        }

        return null
    }
}