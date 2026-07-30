import { LightningElement } from 'lwc';
import getLegacyAccounts from '@salesforce/apex/LegacyAccountQuery.getAccounts';
import getModernAccounts from '@salesforce/apex/ModernAccountQuery.getAccounts';

export default class AccountSecurityDemo extends LightningElement {
    legacyResults;
    modernResults;

    async runLegacy() {
        try {
            this.legacyResults = JSON.stringify(await getLegacyAccounts(), null, 2);
        } catch (error) {
            this.legacyResults = 'ERROR: ' + JSON.stringify(error?.body ?? error, null, 2);
        }
    }

    async runModern() {
        try {
            this.modernResults = JSON.stringify(await getModernAccounts(), null, 2);
        } catch (error) {
            this.modernResults = 'ERROR: ' + JSON.stringify(error?.body ?? error, null, 2);
        }
    }
}
