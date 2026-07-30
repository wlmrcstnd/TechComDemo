# Topic 1 — Apex Security Defaults (System Mode → User Mode)

**The point:** identical Apex code behaves differently depending only on the API version baked into the class's `meta.xml`. This is the single most important thing to get across.

## Files

| File | API version | What it shows |
|---|---|---|
| `classes/LegacyAccountQuery.cls` | 58.0 | Bare SOQL, no `WITH` clause → runs in **system mode**. Ignores FLS, object permissions, sharing rules. `without sharing` is pinned explicitly so this isolates *only* the database-operation-mode change. |
| `classes/ModernAccountQuery.cls` | 67.0 | Identical bare SOQL → now runs in **user mode** by default. FLS/object perms/sharing rules enforced automatically. |
| `classes/LegacyNoKeywordClass.cls` (Topic 1b) | 58.0 | No sharing keyword declared → pre-Summer '26 inherits the caller's sharing mode (commonly ends up "without sharing"). |
| `classes/ModernNoKeywordClass.cls` (Topic 1b) | 67.0 | No sharing keyword declared → Summer '26 automatically defaults to `with sharing`. Only shows a visible difference if Opportunity is Private OWD and records are owned by different users (see deep dive below). |
| `lwc/accountSecurityDemo/` | 67.0 | Test-harness UI with two buttons to run the legacy vs. modern query side by side and diff the JSON output. |
| `objects/Account/fields/Confidential_Notes__c.field-meta.xml` | — | Custom text field used to prove FLS enforcement. |
| `permissionsets/Demo_Restricted_User.permissionset-meta.xml` | — | Read access to Account, but explicitly **no** access to `Confidential_Notes__c`. Assign to a second demo user to prove enforcement live. |
| `scripts/apex/setup-sample-data.apex` | — | Inserts two demo Accounts (Acme Corp, Globex Inc) with `Confidential_Notes__c` populated. |
| `scripts/live-fail-demo/SecurityEnforcedWillFail.cls.snippet` | — | **Not deployed** — kept as `.snippet` on purpose. Live-demo-only compile-error walkthrough (see below). |

## Live demo script

1. **The compile-error demo (do this live, don't pre-deploy it).**
   Copy the contents of `scripts/live-fail-demo/SecurityEnforcedWillFail.cls.snippet` into a **new** `.cls` file inside `force-app/main/default/classes/` at API 67.0, save, and watch VS Code's Problems panel flag the compile error immediately (`WITH SECURITY_ENFORCED` can't be combined with an explicitly-sharing-enforcing class the way `WITH USER_MODE` can — the snippet's second class shows the fix). Then delete the file so your deployable source stays clean.
   Show the fix right after: swap `WITH SECURITY_ENFORCED` for `WITH USER_MODE` (see `SecurityEnforcedFixed` in the same snippet file).
2. *(If time allows)* Log in as the restricted demo user and click both buttons on the `accountSecurityDemo` component:
   - **Legacy** → succeeds, `Confidential_Notes__c` is right there in the JSON (system mode ignores FLS).
   - **Modern** → throws `System.QueryException: No such column 'Confidential_Notes__c' on entity 'Account'`. This is the actual live behavior we verified (2026-07-30), not just a silently-filtered field: user mode makes the inaccessible field invisible to the query compiler entirely, as if it doesn't exist in the schema for that user. It's a more dramatic and clearer "wow" moment than a quiet omission — lean into it live.

   **Setup note:** this requires the restricted user to have a real CRM-capable license (a "Salesforce Platform" license user works fine in a Developer Edition org — Platform licenses do get Account/Contact access by default here). The permission set needs explicit `classAccesses` entries for `LegacyAccountQuery`/`ModernAccountQuery` in addition to the object/field permissions, since Apex class access isn't implicit.

## Deep-dive setup (optional)

- Requires one extra "restricted" demo user (see prerequisites in the root README).
- After deploying, run `scripts/apex/setup-sample-data.apex` to create sample Accounts.
- Create the second user and assign `Demo_Restricted_User`:
  ```bash
  sf org create user -o demoOrg
  sf force user permset assign -o demoOrg -n Demo_Restricted_User
  ```
- For Topic 1b to show a visible difference, Opportunity needs Private OWD with records owned by different users.
