// Copyright (c) 2021 JetBrains s.r.o.
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

using System.Collections.Generic;
using JetBrains.Annotations;
using JetBrains.ProjectModel;
using JetBrains.ProjectModel.Assemblies.Interfaces;
using JetBrains.ProjectModel.MSBuild;
using JetBrains.ProjectModel.Properties;
using JetBrains.ProjectModel.Properties.Managed;
using JetBrains.ReSharper.Host.Features.ProjectModel.TargetFrameworks;
using JetBrains.Rider.Model;
using JetBrains.Util;
using JetBrains.Util.Dotnet.TargetFrameworkIds;

namespace JetBrains.ReSharper.Azure.Project.FunctionApp
{
    public static class FunctionAppProjectDetector
    {
        public static class DefaultWorker
        {
            public static readonly NugetId ExpectedFunctionsNuGetPackageId = new NugetId("Microsoft.NET.Sdk.Functions");
            
            public static bool HasFunctionsPackageReference(IProject project, [CanBeNull] TargetFrameworkId targetFrameworkId)
            {
                return project.GetPackagesReference(ExpectedFunctionsNuGetPackageId, targetFrameworkId) != null;
            }
        }
        
        public static class IsolatedWorker
        {
            public static readonly NugetId ExpectedFunctionsNuGetPackageId = new NugetId("Microsoft.Azure.Functions.Worker");
            
            public static bool HasFunctionsPackageReference(IProject project, [CanBeNull] TargetFrameworkId targetFrameworkId)
            {
                return project.GetPackagesReference(ExpectedFunctionsNuGetPackageId, targetFrameworkId) != null;
            }
        }
        
        public static List<ProjectOutput> GetAzureFunctionsCompatibleProjectOutputs(
            [NotNull] IProject project, 
            [CanBeNull] out string problems, 
            [CanBeNull] ILogger logger = null)
        {
            problems = null;
            var projectOutputs = new List<ProjectOutput>();
      
            foreach (var tfm in project.TargetFrameworkIds)
            {
                if (!IsAzureFunctionsProject(project, tfm, out problems, logger))
                {
                    logger?.Trace($"Configuration for target framework does not have \"AzureFunctionsVersion\" property set: ${tfm.PresentableString}");
                    continue;
                }

                var configuration = project.ProjectProperties.TryGetConfiguration<IManagedProjectConfiguration>(tfm);
                if (configuration == null || (configuration.OutputType != ProjectOutputType.LIBRARY && configuration.OutputType != ProjectOutputType.CONSOLE_EXE))
                {
                    logger?.Trace("Project OutputType = {0}, skip configuration", configuration?.OutputType);
                    continue;
                }

                var projectOutputPath = project.GetOutputFilePath(tfm);
                projectOutputs.Add(new ProjectOutput(
                    tfm: tfm.ToRdTargetFrameworkInfo(),
                    exePath: projectOutputPath.NormalizeSeparators(FileSystemPathEx.SeparatorStyle.Unix),
                    defaultArguments: new List<string> { "host", "start", "--pause-on-error" },
                    
                    // Azure Functions host needs the tfm folder, not the bin folder
                    workingDirectory: projectOutputPath.Directory.NormalizeSeparators(FileSystemPathEx.SeparatorStyle.Unix).TrimFromEnd("/bin"),
                    dotNetCorePlatformRoot: string.Empty,
                    configuration: null));
            }

            return projectOutputs;
        }
        
        public static bool IsAzureFunctionsProject([NotNull] IProject project)
        {
            foreach (var tfm in project.TargetFrameworkIds)
            {
                var configuration = project.ProjectProperties.TryGetConfiguration<IManagedProjectConfiguration>(tfm);
                if (configuration == null || (configuration.OutputType != ProjectOutputType.LIBRARY && configuration.OutputType != ProjectOutputType.CONSOLE_EXE)) return false;

                if (IsAzureFunctionsProject(project, tfm, out _, null))
                {
                    return true;
                }
            }

            return false;
        }

        private static bool IsAzureFunctionsProject(IProject project, TargetFrameworkId targetFrameworkId,
            out string problems, [CanBeNull] ILogger logger)
        {
            // Support .NET Core/Standard, NetCoreApp, NetFx
            if (!(targetFrameworkId.IsNetCore ||
                  targetFrameworkId.IsNetStandard ||
                  targetFrameworkId.IsNetCoreApp ||
                  targetFrameworkId.IsNetFramework))
            {
                logger?.Trace(
                    $"Target framework not supported by Azure Functions: ${targetFrameworkId.PresentableString}");
                problems = null;
                return false;
            }

            // 1) Check MSBuild properties. When property is defined but is empty, this will yield false.
            var hasMsBuildProperty = !string.IsNullOrEmpty(project
                .GetRequestedProjectProperties(MSBuildProjectUtil.AzureFunctionsVersion)
                .FirstNotNull());

            // 2) Check expected package reference.
            //
            //    Azure Functions has two types of runtimes for .NET:
            //    * Default worker, which has a package reference to Microsoft.NET.Sdk.Functions
            //    * Isolated worker, which has a package reference to Microsoft.Azure.Functions.Worker
            //
            //    The default worker runs your application in the Azure Functions host process,
            //    the isolated worker runs your application in a separate process that is spawned by
            //    the Azure Functions host.
            var hasExpectedPackageReference = 
                DefaultWorker.HasFunctionsPackageReference(project, targetFrameworkId) || 
                IsolatedWorker.HasFunctionsPackageReference(project, targetFrameworkId);

            // 3) Check existence of host.json in the project
            var hasHostJsonFile = project
                .GetSubItems("host.json")
                .Any();

            // Build problem description
            if (!hasHostJsonFile)
            {
                problems =
                    "Consider adding missing host.json file required by Azure Functions runtime to your project.";
            }
            else
            {
                problems = null;
            }

            return hasMsBuildProperty || hasExpectedPackageReference || hasHostJsonFile;
        }
    }
}
