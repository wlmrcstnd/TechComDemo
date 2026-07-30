# Topic 2 — Apex Triggers and FLS Enforcement (Corrected)

**Verified live (2026-07-30):** the original premise below was wrong, and the correction is the more interesting story.

**Original assumption:** trigger bare SOQL is *always* system mode, versionless, regardless of API version — meaning it should **never** enforce FLS, no matter what.

**What we actually found:** updating an Account as the restricted demo user threw `System.QueryException: No such column 'Confidential_Notes__c' on entity 'Account'` on the trigger's own bare SOQL (line 5, `AccountConfidentialTrigger.trigger`) — not just in the handler. We tested this at both API 67.0 and API 58.0 on the trigger's own `meta.xml`, and the exception fired identically both times. That rules out "the trigger follows its own compiled API version like a class."

**The real rule (as best we can tell from testing):** a trigger's bare SOQL enforces FLS **unconditionally**, regardless of the trigger's own compiled API version. It genuinely is "versionless" — just in the opposite direction from the original assumption: always **enforced**, not always **bypassed**. This is arguably the better talking point, because it means Salesforce closed a well-known blind spot (people assuming trigger bodies are automatically "safe" system-mode code) without waiting for anyone to opt in via API version.

**A second wrinkle:** because the exception fires on the trigger's *first* statement, execution aborts immediately — the handler's own query never even runs for a restricted user. That means there's no clean side-by-side debug-log comparison available for that user; what you actually get live is a hard save failure. (A user with full FLS access sees both lines succeed identically, which isn't interesting either.) The demo is "watch the save fail and explain why," not "diff two debug log lines."

**What this means for the trigger-vs-handler advice:** since FLS enforcement turned out to be equally strict in both places, the practical reason to still prefer a handler class isn't FLS protection anymore — it's:

- **Sharing rules**, which genuinely remain trigger-body-exempt regardless of API version (a separate, longstanding mechanism from FLS/CRUD) — only a `with sharing` handler lets you control row-level visibility.
- Code organization / testability, independent of any security behavior.

## Files

| File | API version | What it shows |
| --- | --- | --- |
| `triggers/AccountConfidentialTrigger.trigger` | 67.0 | Bare SOQL directly in the trigger body. Confirmed: enforces FLS regardless of this API version. |
| `classes/AccountConfidentialHandler.cls` | 67.0 | `with sharing` handler class the trigger delegates to. Never actually reached for a restricted user, since the trigger's own query throws first. |

## Live demo script

1. Log in as the restricted demo user (see Topic 1's `Demo_Restricted_User` permission set).
2. Update any Account as that user (this fires `AccountConfidentialTrigger`).
3. **Expected outcome:** the save fails with an error banner citing `System.QueryException: No such column 'Confidential_Notes__c' on entity 'Account'` from `Trigger.AccountConfidentialTrigger`, line 5.
4. Explain: this is the trigger's own *bare* query throwing — the thing everyone assumes is "safe" from FLS because it's inside a trigger. It isn't, regardless of API version.

This is the punchline: don't assume bare SOQL directly in a trigger body is exempt from FLS enforcement just because it's a trigger. It isn't — not even the "old" pre-Summer-26 version of it. Move sensitive logic to a handler class anyway, but for sharing-rule control and code hygiene, not because it changes the FLS outcome.
