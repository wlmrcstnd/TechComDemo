# Salesforce Summer '26 Dev Demo

An SFDX project demoing 5 Summer '26 (API v67.0) developer changes with live before/after comparisons.

## Topics

1. **[Apex Security Defaults (System Mode → User Mode)](docs/topic1-apex-security-defaults/README.md)** — identical Apex code behaves differently depending only on the API version in the class's meta.xml.
2. **[Apex Triggers Always Run in System Mode](docs/topic2-trigger-system-mode/README.md)** — versionless: bare SOQL in a trigger body is always system mode, no matter what.
3. **[LWC Secure Downloads (Blob MIME-type allowlist)](docs/topic3-lwc-secure-downloads/README.md)** — a Lightning Web Security runtime restriction, not gated by component API version.
4. **[Multiline Strings & `String.template()`](docs/topic4-multiline-strings/README.md)** — triple-quoted string literals with `${...}` interpolation, run as Anonymous Apex.
5. **[Zero-JS Grouped `<details>` Accordion](docs/topic5-zero-js-accordion/README.md)** — native accordion grouping via a shared `name` attribute, API 67.0+.

Each topic's folder under `docs/` has the demo script (what to click, what to say) pulled from this spec, plus a table of exactly which files matter and why.

## 0. Prerequisites

- Salesforce CLI (`sf`) installed and authenticated to a Dev Hub
- A scratch org, sandbox, or Developer Edition org that is already running **Summer '26** (so its max API version is 67.0)
- VS Code + Salesforce Extension Pack
- Enough user licenses to create one extra "restricted" demo user (only needed for the optional deep-dive in Topic 1/2)

If your org isn't upgraded to Summer '26 yet, everything gated by "class compiled at API 67.0" won't show a difference yet — the class will just run at whatever the org's actual ceiling is. Check your instance's release date before presenting.

## Project skeleton

```
sf-summer26-demo/
├── sfdx-project.json
├── config/
│   └── project-scratch-def.json
├── force-app/main/default/
│   ├── classes/
│   │   ├── LegacyAccountQuery.cls / .cls-meta.xml           (Topic 1 — API 58.0)
│   │   ├── ModernAccountQuery.cls / .cls-meta.xml            (Topic 1 — API 67.0)
│   │   ├── LegacyNoKeywordClass.cls / .cls-meta.xml          (Topic 1b — API 58.0)
│   │   ├── ModernNoKeywordClass.cls / .cls-meta.xml          (Topic 1b — API 67.0)
│   │   └── AccountConfidentialHandler.cls / .cls-meta.xml    (Topic 2)
│   ├── triggers/
│   │   └── AccountConfidentialTrigger.trigger / -meta.xml    (Topic 2)
│   ├── lwc/
│   │   ├── accountSecurityDemo/                              (Topic 1 — test harness UI)
│   │   ├── secureDownloadOld/                                (Topic 3)
│   │   ├── secureDownloadNew/                                (Topic 3)
│   │   ├── faqAccordionOld/                                  (Topic 5)
│   │   └── faqAccordionNew/                                  (Topic 5)
│   ├── objects/Account/fields/
│   │   └── Confidential_Notes__c.field-meta.xml
│   └── permissionsets/
│       └── Demo_Restricted_User.permissionset-meta.xml
├── scripts/
│   ├── apex/
│   │   ├── setup-sample-data.apex
│   │   ├── multiline-string-before.apex
│   │   └── multiline-string-after.apex
│   └── live-fail-demo/
│       └── SecurityEnforcedWillFail.cls.snippet   (NOT deployed — see Topic 1 notes)
└── docs/
    ├── topic1-apex-security-defaults/README.md
    ├── topic2-trigger-system-mode/README.md
    ├── topic3-lwc-secure-downloads/README.md
    ├── topic4-multiline-strings/README.md
    └── topic5-zero-js-accordion/README.md
```

## Build & deploy commands

```bash
# 1. Create (or reuse) a scratch org already on Summer '26
sf org create scratch -f config/project-scratch-def.json -a demoOrg -d -y 7

# 2. Deploy everything EXCEPT the live-fail-demo snippet (it's not .cls, so it's already excluded)
sf project deploy start -o demoOrg

# 3. Load sample data
sf apex run -o demoOrg -f scripts/apex/setup-sample-data.apex

# 4. (Optional deep dive) create a restricted second user and assign the permission set
sf org create user -o demoOrg   # follow CLI prompts, or use Setup > Users > New User
sf force user permset assign -o demoOrg -n Demo_Restricted_User
```

CLI flags shift between `sf` versions — run `sf COMMAND --help` if any of the above don't match your installed CLI.

Add `accountSecurityDemo`, `secureDownloadOld`, `secureDownloadNew`, `faqAccordionOld`, and `faqAccordionNew` to a Lightning App Page via App Builder so they're all one click away during the talk.

## Demo run order (maps to the 5 presentation topics)

1. **Topic 1:** Paste `SecurityEnforcedWillFail.cls.snippet` into a new class → show compile error → delete it → show the fix with `WITH USER_MODE`. Then (if time allows) log in as the restricted user and click both buttons on `accountSecurityDemo`.
2. **Topic 2:** Update an Account as the restricted user, open debug logs, compare the trigger-direct debug line vs the handler-class debug line.
3. **Topic 3:** Click "Download CSV (OLD way)" → nothing happens, open console (LWS rejects the `text/csv` Blob MIME type). Click "Download CSV (NEW way)" → file downloads (`text/plain` is LWS-allowed).
4. **Topic 4:** Run `multiline-string-before.apex`, then `multiline-string-after.apex`, compare the debug log output side by side.
5. **Topic 5:** Open `faqAccordionOld`, click two panels open at once (or show the extra JS needed). Switch to `faqAccordionNew`, click around — only one open at a time, zero JS.

## Cleanup / reset

```bash
sf org delete scratch -o demoOrg -p   # scratch org
# or, for a sandbox/dev org: manually delete the two demo Accounts,
# deactivate the Demo_Restricted_User permission set assignment,
# and remove the demo Lightning App Page.
```
