/**
 * DEMO: compiled at API 58.0 (pre-Summer '26 default).
 * Bare SOQL, no WITH clause -> runs in SYSTEM MODE.
 * Ignores the running user's FLS, object permissions, and sharing rules.
 * "without sharing" is pinned explicitly so this demo isolates ONLY the
 * database-operation-mode change, not the class-sharing-default change
 * (that one is demoed separately below as "1b").
 */
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
