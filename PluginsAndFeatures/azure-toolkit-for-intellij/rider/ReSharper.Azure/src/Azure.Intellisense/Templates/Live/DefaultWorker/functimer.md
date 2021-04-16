---
guid: cde7ab6b-9766-45ea-a6aa-09e325e85275
type: Live
reformat: True
shortenReferences: True
scopes: InCSharpFile(minimumLanguageVersion=2.0)
parameterOrder: CLASS, SCHEDULE
CLASS-expression: getAlphaNumericFileNameWithoutExtension()
SCHEDULE-expression: constant("")
---

# functimer

Creates an Azure Function method with a timer trigger.
0 */5 * * * *
```
[FunctionName("$CLASS$")]
public static async Task RunAsync([TimerTrigger("$SCHEDULE$")]TimerInfo myTimer, ILogger log)
{
    log.LogInformation($"C# Timer trigger function executed at: {DateTime.UtcNow}");$END$
}
```
