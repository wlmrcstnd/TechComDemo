/**
 * DEMO: compiled at API 67.0 (Summer '26 default).
 * IDENTICAL bare SOQL -> now runs in USER MODE by default.
 * FLS, object permissions, and sharing rules are enforced automatically.
 */
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
