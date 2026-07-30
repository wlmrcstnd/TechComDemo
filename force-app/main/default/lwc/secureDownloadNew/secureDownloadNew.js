import { LightningElement } from 'lwc';

export default class SecureDownloadNew extends LightningElement {
    handleDownload() {
        const csvContent = 'Name,Revenue\nAcme Corp,5000000\nGlobex,3200000\n';
        const blob = new Blob([csvContent], { type: 'text/plain' });
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
