// Copyright (c) 2020-2021 JetBrains s.r.o.
//
// All rights reserved.
//
// MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of
// the Software.
//
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
// TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

using JetBrains.Application.Settings;
using JetBrains.ReSharper.Azure.Project.FunctionApp;
using JetBrains.ReSharper.Azure.Psi.FunctionApp;
using JetBrains.ReSharper.Feature.Services.Daemon;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Caches.SymbolCache;
using JetBrains.ReSharper.Psi.CSharp;
using JetBrains.ReSharper.Psi.CSharp.Tree;
using JetBrains.ReSharper.Psi.Tree;
using JetBrains.Rider.Backend.Features.RunMarkers;
using IMethodDeclaration = JetBrains.ReSharper.Psi.CSharp.Tree.IMethodDeclaration;

namespace JetBrains.ReSharper.Azure.Daemon.RunMarkers
{
    [Language(typeof(CSharpLanguage))]
    public class FunctionAppRunMarkerProvider : IRunMarkerProvider
    {
        public double Priority => RunMarkerProviderPriority.DEFAULT;

        public void CollectRunMarkers(IFile file, IContextBoundSettingsStore settings, IHighlightingConsumer consumer)
        {
            if (!(file is ICSharpFile csharpFile)) return;

            var project = file.GetProject();
            if (project == null || !project.IsValid()) return;
            if (!FunctionAppProjectDetector.IsAzureFunctionsProject(project)) return;

            foreach (var declaration in CachedDeclarationsCollector.Run<IMethodDeclaration>(csharpFile))
            {
                if (!(declaration.DeclaredElement is IMethod method)) continue;
                if (!FunctionAppFinder.IsSuitableFunctionAppMethod(method)) continue;

                var range = declaration.GetNameDocumentRange();

                var highlighting = new RunMarkerHighlighting(
                    method: declaration.DeclaredElement,
                    declaration: declaration,
                    attributeId: FunctionAppRunMarkerAttributeIds.FUNCTION_APP_RUN_METHOD_MARKER_ID,
                    range: range,
                    targetFrameworkId: file.GetPsiModule().TargetFrameworkId);

                consumer.AddHighlighting(highlighting, range);
            }
        }
    }
}
