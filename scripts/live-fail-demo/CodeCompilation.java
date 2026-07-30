// =====================================================================
// Salesforce Summer '26 (API v67.0) — Before/After Cheat Sheet
// All 5 demo topics in one file, pulled from the actual project source
// (not just release notes — includes the "reality check" corrections
// found during live testing, per presentation-script.md).
// =====================================================================


// =====================================================================
// TOPIC 1 — Apex Security Defaults (System Mode -> User Mode)
// Source: force-app/main/default/classes/{Legacy,Modern}AccountQuery.cls
// =====================================================================

// --- BEFORE: compiled at API 58.0 -> bare SOQL runs in SYSTEM MODE ---
// Ignores the running user's FLS, object permissions, and sharing rules.
public without sharing class LegacyAccountQuery {
    @AuraEnabled
    public static List<Account> getAccounts() {
        return [
            SELECT Id, Name, AnnualRevenue, Confidential_Notes__c
            FROM Account
            ORDER BY Name
            LIMIT 50
        ];
    }
}

// --- AFTER: IDENTICAL code, compiled at API 67.0 -> USER MODE by default ---
// FLS, object permissions, and sharing rules are enforced automatically.
// Only the apiVersion in the .cls-meta.xml changed — zero code changes.
public without sharing class ModernAccountQuery {
    @AuraEnabled
    public static List<Account> getAccounts() {
        return [
            SELECT Id, Name, AnnualRevenue, Confidential_Notes__c
            FROM Account
            ORDER BY Name
            LIMIT 50
        ];
    }
}

// Live result as a restricted user (no FLS on Confidential_Notes__c):
//   Legacy (58.0) -> succeeds, leaks Confidential_Notes__c for every row.
//   Modern (67.0) -> throws System.QueryException:
//       "No such column 'Confidential_Notes__c' on entity 'Account'."
//   User-mode makes an FLS-denied field invisible to the query compiler
//   entirely — not silently filtered, the column "doesn't exist."


// --- TOPIC 1b — sharing-keyword default ---
// Source: force-app/main/default/classes/{Legacy,Modern}NoKeywordClass.cls

// BEFORE (58.0): no sharing keyword declared -> inherits the caller's
// sharing mode (commonly ends up "without sharing").
public class LegacyNoKeywordClass {
    @AuraEnabled
    public static List<Opportunity> getAllOpenOpportunities() {
        return [SELECT Id, Name, OwnerId, Amount FROM Opportunity WHERE IsClosed = false];
    }
}

// AFTER (67.0): no sharing keyword declared -> defaults to "with sharing"
// automatically. Same body, safer by default.
public class ModernNoKeywordClass {
    @AuraEnabled
    public static List<Opportunity> getAllOpenOpportunities() {
        return [SELECT Id, Name, OwnerId, Amount FROM Opportunity WHERE IsClosed = false];
    }
}


// --- TOPIC 1c — WITH SECURITY_ENFORCED is a compile error, not a warning ---
// Source: scripts/live-fail-demo/SecurityEnforcedWillFail.cls.snippet
// (live-only file — paste into a new .cls at API 67.0, watch it fail, delete it)

// BEFORE — this no longer compiles under a "with sharing" class:
public with sharing class SecurityEnforcedWillFail {
    public static List<Account> getAccounts() {
        return [
            SELECT Id, Name
            FROM Account
            WITH SECURITY_ENFORCED
        ];
    }
}

// AFTER — the fix: swap WITH SECURITY_ENFORCED for WITH USER_MODE.
public with sharing class SecurityEnforcedFixed {
    public static List<Account> getAccounts() {
        return [
            SELECT Id, Name
            FROM Account
            WITH USER_MODE
        ];
    }
}


// =====================================================================
// TOPIC 2 — Triggers Always Enforce FLS (CORRECTED during live testing)
// Source: force-app/main/default/triggers/AccountConfidentialTrigger.trigger
//         force-app/main/default/classes/AccountConfidentialHandler.cls
//
// REALITY CHECK: the original assumption was "bare SOQL in a trigger body
// is always system mode, versionless — never enforces FLS." Live testing
// found the OPPOSITE: the trigger's bare SOQL enforces FLS and throws,
// confirmed identically at BOTH API 67.0 and API 58.0 on the trigger's
// own meta.xml. Versionless — but always ON, not always OFF.
// =====================================================================

// "BAD PRACTICE" bare query directly in the trigger body — kept on purpose
// for the demo. It is NOT a system-mode exemption (that was the wrong
// assumption); FLS is enforced here too, unconditionally.
trigger AccountConfidentialTrigger on Account (after update) {
    List<Account> unfiltered = [
        SELECT Id, Name, Confidential_Notes__c
        FROM Account
        WHERE Id IN :Trigger.newMap.keySet()
    ];
    System.debug('Trigger-direct query (FLS enforced, unconditionally): ' + unfiltered);

    // GOOD PRACTICE: hand off to a handler class with explicit "with sharing"
    // — not for FLS (identical outcome now), but for sharing rules (row-level
    // visibility, which genuinely remains trigger-body-exempt) + testability.
    AccountConfidentialHandler.logFiltered(Trigger.newMap.keySet());
}

public with sharing class AccountConfidentialHandler {
    public static List<Account> logFiltered(Set<Id> accountIds) {
        List<Account> filtered = [
            SELECT Id, Name, Confidential_Notes__c
            FROM Account
            WHERE Id IN :accountIds
        ];
        System.debug('Handler query (with sharing, user mode default): ' + filtered);
        return filtered;
    }
}

// Live result as the restricted user editing any Account and saving:
//   AccountConfidentialTrigger: execution of AfterUpdate caused by:
//   System.QueryException: No such column 'Confidential_Notes__c' on entity 'Account'.
//   Trigger.AccountConfidentialTrigger: line 5, column 1
//   -> fails on the trigger's OWN bare query; the handler class is never reached.


// =====================================================================
// TOPIC 3 — LWC Secure Downloads (CORRECTED: Blob MIME-type allowlist,
// not a data:-URI block)
// Source: force-app/main/default/lwc/secureDownload{Old,New}/secureDownload{Old,New}.js
//
// REALITY CHECK: the original premise was "LWS blocks data: URI downloads."
// Tested live (anchor-download + window.open, Chrome + Edge) — never
// blocked. What IS real: LWS enforces a MIME-type allowlist on the Blob
// constructor itself. 'text/csv' is rejected at runtime; 'text/plain' works.
// =====================================================================

// --- BEFORE: 'text/csv' is not on LWS's allowed Blob MIME-type list ---
import { LightningElement } from 'lwc';

export default class SecureDownloadOld extends LightningElement {
    handleDownload() {
        const csvContent = 'Name,Revenue\nAcme Corp,5000000\nGlobex,3200000\n';
        const blob = new Blob([csvContent], { type: 'text/csv' }); // rejected at runtime
        const url = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = 'accounts-old.csv';
        this.template.querySelector('lightning-card').appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(url);
    }
}
// Console: "Lightning Web Security: Unsupported MIME type." Nothing downloads,
// no UI error shown — silent failure.

// --- AFTER: 'text/plain' is LWS-allowed; filename still ends in .csv ---
import { LightningElement } from 'lwc';

export default class SecureDownloadNew extends LightningElement {
    handleDownload() {
        const csvContent = 'Name,Revenue\nAcme Corp,5000000\nGlobex,3200000\n';
        const blob = new Blob([csvContent], { type: 'text/plain' }); // allowed
        const url = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = 'accounts-new.csv';
        this.template.querySelector('lightning-card').appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(url);
    }
}
// Downloads normally; opens fine in Excel/Sheets regardless of the Blob's
// declared MIME type, because the file extension comes from link.download.


// =====================================================================
// TOPIC 4 — Multiline Strings & String.template()
// Source: scripts/apex/multiline-string-{before,after}.apex
// (run via Execute Anonymous — no deployment needed, safest "recovery" demo)
// =====================================================================

// --- BEFORE: '+' concatenation chains ---
Account acc = new Account(Name = 'Acme Corp', AnnualRevenue = 5000000);
Contact ct = new Contact(FirstName = 'Juan', LastName = 'Dela Cruz');

String emailBody_before = 'Hello ' + ct.FirstName + ',\n\n' +
    'Thank you for your interest in ' + acc.Name + '.\n' +
    'Your assigned account value is: ' + acc.AnnualRevenue + '\n\n' +
    'Regards,\nThe Sales Team';

String jsonPayload_before = '{' +
    '"accountName": "' + acc.Name + '",' +
    '"revenue": ' + acc.AnnualRevenue +
    '}';

// --- AFTER: triple-quoted strings + String.template() with ${...} ---
// Note: ${...} only accepts a simple variable, not an arbitrary expression —
// that's why acc.Name / acc.AnnualRevenue get assigned to locals first.
// Note: the newline right after the opening ''' is stripped automatically.
String firstName = ct.FirstName;
String accountName = acc.Name;
Decimal revenue = acc.AnnualRevenue;

String emailBody_after = String.template('''
Hello ${firstName},

Thank you for your interest in ${accountName}.
Your assigned account value is: ${revenue}

Regards,
The Sales Team
''');

String jsonPayload_after = String.template('''
{
    "accountName": "${accountName}",
    "revenue": ${revenue}
}
''');

// Both versions render identical debug-log output — the win is code
// readability, not a data difference.


// =====================================================================
// TOPIC 5 — Zero-JS Grouped <details> Accordion (API 67.0+)
// Source: force-app/main/default/lwc/faqAccordion{Old,New}/*
// =====================================================================

/* --- BEFORE: needs an onclick handler + JS to close sibling panels ---

<template>
    <lightning-card title="OLD: Manual JS Accordion (pre-67.0)">
        <details onclick={handleToggle} data-id="q1" open>
            <summary>What is Salesforce Flow?</summary>
            <p>Flow is Salesforce's point-and-click automation builder.</p>
        </details>
        <details onclick={handleToggle} data-id="q2">
            <summary>What is Apex?</summary>
            <p>Apex is Salesforce's strongly-typed, object-oriented language.</p>
        </details>
        <details onclick={handleToggle} data-id="q3">
            <summary>What is a Lightning Web Component?</summary>
            <p>LWC is the modern JS framework for Salesforce UI.</p>
        </details>
    </lightning-card>
</template>

*/
// import { LightningElement } from 'lwc';
//
// export default class FaqAccordionOld extends LightningElement {
//     handleToggle(event) {
//         const clickedId = event.currentTarget.dataset.id;
//         this.template.querySelectorAll('details').forEach((detail) => {
//             if (detail.dataset.id !== clickedId) {
//                 detail.removeAttribute('open');
//             }
//         });
//     }
// }

/* --- AFTER: a shared name="" attribute does it natively, zero JS ---

<template>
    <lightning-card title="NEW: Zero-JS Grouped Accordion (API 67.0+)">
        <details name="faq-group" open>
            <summary>What is Salesforce Flow?</summary>
            <p>Flow is Salesforce's point-and-click automation builder.</p>
        </details>
        <details name="faq-group">
            <summary>What is Apex?</summary>
            <p>Apex is Salesforce's strongly-typed, object-oriented language.</p>
        </details>
        <details name="faq-group">
            <summary>What is a Lightning Web Component?</summary>
            <p>LWC is the modern JS framework for Salesforce UI.</p>
        </details>
    </lightning-card>
</template>

*/
// import { LightningElement } from 'lwc';
//
// export default class FaqAccordionNew extends LightningElement {
//     // No JavaScript needed — the shared `name` attribute + API 67.0
//     // handle "only one panel open at a time" natively.
// }
