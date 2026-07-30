import { LightningElement } from 'lwc';

export default class SecureDownloadOld extends LightningElement {
    handleDownload() {
        const csvContent = 'Name,Revenue\nAcme Corp,5000000\nGlobex,3200000\n';
        // 'text/csv' is not on LWS's allowed Blob MIME-type list — rejected at runtime.
        const blob = new Blob([csvContent], { type: 'text/csv' });
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
