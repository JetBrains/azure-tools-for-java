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

package org.cases.markup.functionapp.csharp

import com.jetbrains.rdclient.daemon.util.attributeId
import com.jetbrains.rider.test.base.BaseTestWithMarkup
import org.testng.annotations.Test

class AzureFunctionRunMarkerTest : BaseTestWithMarkup() {

    override fun getSolutionDirectoryName(): String = "FunctionApp"

    // TODO: Write tests when migrate gutter marks logic to backend.
    @Test(enabled = false)
    fun testMethod_FunctionClass_Detected() = verifyLambdaGutterMark()

    private fun verifyLambdaGutterMark() {
        doTestWithMarkupModel(
                testFilePath = "FunctionApp/Function.cs",
                sourceFileName = "Function.cs",
                goldFileName = "Function.gold"
        ) {
            dumpHighlightersTree(
                    valueFilter = { it.attributeId.contains("TODO: backend markup id") }
            )
        }
    }
}
