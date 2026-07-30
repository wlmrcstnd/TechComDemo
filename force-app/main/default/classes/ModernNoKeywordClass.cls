// DEMO: compiled at 67.0, no sharing keyword declared.
// Summer '26: automatically defaults to "with sharing".
public class ModernNoKeywordClass {
    @AuraEnabled
    public static List<Opportunity> getAllOpenOpportunities() {
        return [SELECT Id, Name, OwnerId, Amount FROM Opportunity WHERE IsClosed = false];
    }
}
