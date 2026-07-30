import { LightningElement } from 'lwc';

export default class FaqAccordionOld extends LightningElement {
    handleToggle(event) {
        const clickedId = event.currentTarget.dataset.id;
        this.template.querySelectorAll('details').forEach((detail) => {
            if (detail.dataset.id !== clickedId) {
                detail.removeAttribute('open');
            }
        });
    }
}
