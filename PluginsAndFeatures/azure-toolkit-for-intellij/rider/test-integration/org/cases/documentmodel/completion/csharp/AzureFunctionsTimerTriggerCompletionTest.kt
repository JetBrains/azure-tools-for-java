/**
 * Copyright (c) 2020 JetBrains s.r.o.
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

package org.cases.documentmodel.completion.csharp

import com.jetbrains.rider.test.base.CompletionTestBase
import com.jetbrains.rider.test.scriptingApi.assertLookupContains
import com.jetbrains.rider.test.scriptingApi.assertLookupContainsExact
import com.jetbrains.rider.test.scriptingApi.typeWithLatency
import com.jetbrains.rider.test.scriptingApi.withOpenedEditor
import org.testng.annotations.Test

class AzureFunctionsTimerTriggerCompletionTest : CompletionTestBase() {

    override fun getSolutionDirectoryName(): String = "FunctionApp"

    @Test
    fun testPopup_CompletionList() {
        val expectedCronCompletionList = arrayOf(
                "*/15 * * * * *",
                "* * * * * *",
                "0 */5 * * * *",
                "0 * * * * *",
                "0 0 */6 * * *",
                "0 0 * * * *",
                "0 0 * * * Sun",
                "0 0 * * * 1-5",
                "0 0 0 * * *",
                "0 0 0 * * Sat,Sun",
                "0 0 0 * * 0",
                "0 0 0 * * 6,0",
                "0 0 0 1-7 * Sun",
                "0 0 0 1 * *",
                "0 0 0 1 1 *",
                "0 0 8-18 * * *",
                "0 0 9 * * Mon",
                "0 0 10 * * *",
                "0 30 9 * Jan Mon",
                "11 5 23 * * *"
        )

        withOpenedEditor("Function.cs", "Function.cs") {
            typeWithLatency("\"")
            assertLookupContainsExact(*expectedCronCompletionList)
        }
    }
}
