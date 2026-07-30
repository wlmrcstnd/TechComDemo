trigger AccountConfidentialTrigger on Account (after update) {
    // BAD PRACTICE (kept here on purpose for the live demo):
    // bare SOQL written directly in the trigger body.
    // This ALWAYS executes in system mode, no matter what.
    List<Account> unfiltered = [
        SELECT Id, Name, Confidential_Notes__c
        FROM Account
        WHERE Id IN :Trigger.newMap.keySet()
    ];
    System.debug('Trigger-direct query (always system mode): ' + unfiltered);

    // GOOD PRACTICE: hand off to a handler class that explicitly
    // declares "with sharing" so user-mode defaults actually apply.
    AccountConfidentialHandler.logFiltered(Trigger.newMap.keySet());
}
