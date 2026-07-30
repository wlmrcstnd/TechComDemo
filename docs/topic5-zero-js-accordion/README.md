# Topic 5 — Zero-JS Grouped `<details>` Accordion (API 67.0)

## Files

| File | API version | What it shows |
|---|---|---|
| `lwc/faqAccordionOld/` | 66.0 | Three `<details>` elements with manual `onclick` JS to close siblings when one opens. |
| `lwc/faqAccordionNew/` | 67.0 | Same three `<details>` elements, but sharing a `name="faq-group"` attribute — the browser groups and manages "only one open" natively. Zero JS in the class. |

## Live demo script

1. Open `faqAccordionOld`, click two panels open at once (or point out the extra JS needed to prevent that).
2. Switch to `faqAccordionNew`, click around — only one panel open at a time, with zero JavaScript in the component.

This pairs well right after Topic 4 (multiline strings) as a second "less code, same result" example.
