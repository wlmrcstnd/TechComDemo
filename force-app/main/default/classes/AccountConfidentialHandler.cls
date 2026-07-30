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
