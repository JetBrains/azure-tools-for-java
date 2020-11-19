// Copyright (c) 2020 JetBrains s.r.o.
// <p/>
// All rights reserved.
// <p/>
// MIT License
// <p/>
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// <p/>
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of
// the Software.
// <p/>
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
// THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
// TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

using JetBrains.Annotations;
using JetBrains.DocumentModel;
using JetBrains.ReSharper.Azure.Psi.FunctionApp;
using JetBrains.ReSharper.Feature.Services.CodeCompletion;
using JetBrains.ReSharper.Feature.Services.CodeCompletion.Infrastructure;
using JetBrains.ReSharper.Feature.Services.CodeCompletion.Infrastructure.LookupItems;
using JetBrains.ReSharper.Features.Intellisense.CodeCompletion.CSharp;
using JetBrains.ReSharper.Plugins.FSharp.Psi;
using JetBrains.ReSharper.Plugins.FSharp.Psi.Features.CodeCompletion;
using JetBrains.ReSharper.Plugins.FSharp.Psi.Impl;
using JetBrains.ReSharper.Plugins.FSharp.Psi.Tree;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Tree;

namespace JetBrains.ReSharper.Azure.FSharp.FunctionApp.CodeCompletion.Rules
{
    [Language(typeof(FSharpLanguage))]
    public class FSharpTimerTriggerCronArgumentsProvider : ItemsProviderOfSpecificContext<FSharpCodeCompletionContext>
    {
        // TODO: Refactor so C# and F# share this
        private readonly struct CronSuggestion
        {
            public readonly string Expression;
            public readonly string Description;

            public CronSuggestion(string expression, string description)
            {
                Expression = expression;
                Description = description;
            }
        }

        // TODO: Refactor so C# and F# share this
        private readonly CronSuggestion[] CronSuggestions =
        {
            new CronSuggestion("* * * * * *", "Every second"),
            new CronSuggestion("0 * * * * *", "Every minute"),
            new CronSuggestion("0 */5 * * * *", "Every 5 minutes"),
            new CronSuggestion("0 0 * * * *", "Every hour"),
            new CronSuggestion("0 0 */6 * * *", "Every 6 hours at minute 0"),
            new CronSuggestion("0 0 8-18 * * *", "Every hour between 08:00 AM and 06:59 PM"),
            new CronSuggestion("0 0 0 * * *", "At 12:00 AM"),
            new CronSuggestion("0 0 10 * * *", "At 10:00 AM"),
            new CronSuggestion("0 0 * * * 1-5", "Every hour, Monday through Friday"),
            new CronSuggestion("0 0 0 * * 0", "At 12:00 AM, only on Sunday"),
            new CronSuggestion("0 0 9 * * Mon", "At 09:00 AM, only on Monday"),
            new CronSuggestion("0 0 0 1 * *", "At 12:00 AM, on day 1 of the month"),
            new CronSuggestion("0 0 0 1 1 *", "At 12:00 AM, on day 1 of the month, only in January"),
            new CronSuggestion("0 0 * * * Sun", "Every hour, only on Sunday"),
            new CronSuggestion("0 0 0 * * Sat,Sun", "At 12:00 AM, only on Saturday and Sunday"),
            new CronSuggestion("0 0 0 * * 6,0", "At 12:00 AM, only on Saturday and Sunday"),
            new CronSuggestion("0 0 0 1-7 * Sun", "At 12:00 AM, between day 1 and 7 of the month, only on Sunday"),
            new CronSuggestion("11 5 23 * * *", "At 11:05:11 PM"),
            new CronSuggestion("*/15 * * * * *", "Every 15 seconds"),
            new CronSuggestion("0 30 9 * Jan Mon", "At 09:30 AM, only on Monday, only in January")
        };

        protected override bool IsAvailable(FSharpCodeCompletionContext context)
        {
            return IsStringLiteral(context) &&
                   IsTimerTriggerAnnotation(context) &&
                   context.BasicContext.CodeCompletionType == CodeCompletionType.BasicCompletion;
        }

        protected override bool AddLookupItems(FSharpCodeCompletionContext context, IItemsCollector collector)
        {
            foreach (var cronSuggestion in CronSuggestions)
            {
                var token = context.TokenAtCaret as ITokenNode;
                if (token == null) return false;
            
                var literalExpression = token.Parent as ILiteralExpr;
                if (literalExpression == null) return false;

                var ranges = GetRangeFor(literalExpression);

                var lookupItem = 
                    CSharpLookupItemFactory.Instance.CreateTextLookupItem(new TextLookupRanges(ranges, ranges),
                        "\"" + cronSuggestion.Expression + "\"");

                lookupItem.Presentation.DisplayName.Text = cronSuggestion.Expression;
                lookupItem.Presentation.DisplayTypeName.Text = cronSuggestion.Description;
        
                // Replace spaces prefixes with nbsp to make sure <space> terminates the lookup.
                // It is required since we need to support case when a user choose to complete by <space>.
                // TODO: Disable until RIDER-45943 is fixed.
                // lookupItem.WithMatcher(item =>
                //     new TextualMatcher<TextualInfo>(item.Info.Text.Replace(" ", "·"), item.Info));
        
                collector.Add(lookupItem);
            }
        
            return true;
        }

        private static DocumentRange GetRangeFor([NotNull] ILiteralExpr expression)
        {
            var underQuotesRange = expression.GetTreeTextRange();
            var containingFile = expression.GetContainingFile();
            return containingFile?.GetDocumentRange(underQuotesRange) ?? DocumentRange.InvalidRange;
        }
        
        private bool IsStringLiteral([NotNull] FSharpCodeCompletionContext context)
        {
            return context.TokenAtCaret is ITokenNode token &&
                   token.GetTokenType().IsStringLiteral;
        }
        
        private bool IsTimerTriggerAnnotation([NotNull] FSharpCodeCompletionContext context)
        {
            var token = context.TokenAtCaret as ITokenNode;
            if (token == null) return false;
            
            var literalExpression = token.Parent as ILiteralExpr;
            var attribute = AttributeNavigator.GetByExpression(literalExpression.IgnoreParentParens());
        
            if (attribute == null) return false;
            if (attribute.Arguments.Count != 1) return false;
        
            var resolveResult = attribute.ReferenceName.Reference.Resolve();
            return resolveResult.DeclaredElement is ITypeElement typeElement &&
                   FunctionAppFinder.IsTimerTriggerAttribute(typeElement);
        }
    }
}
