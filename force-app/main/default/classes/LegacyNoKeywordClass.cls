// DEMO: compiled at 58.0, no sharing keyword declared.
// Pre-Summer '26: inherits the caller's sharing mode (commonly ends up "without sharing").
public class LegacyNoKeywordClass {
    @AuraEnabled
    public static List<Opportunity> getAllOpenOpportunities() {
        return [SELECT Id, Name, OwnerId, Amount FROM Opportunity WHERE IsClosed = false];
    }
}
