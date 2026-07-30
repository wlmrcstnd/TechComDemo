# Topic 3 — LWC Secure Downloads (Blob MIME-type allowlist)

**Note:** unlike the Apex changes, this is a runtime Lightning Web Security (LWS) restriction, not gated by the component's `apiVersion`. It just depends on whether Summer '26 is live in your org.

**Verified live (2026-07-30):** `data:` URI hrefs (via `<a download>` and via `window.open()`) were NOT blocked by LWS in this org/browser combination, contrary to the original assumption behind this topic. What LWS *does* enforce is a MIME-type allowlist on the `Blob` constructor — `new Blob(..., { type: 'text/csv' })` is rejected at runtime with `Lightning Web Security: Unsupported MIME type.`, while `text/plain` is accepted. This topic is built around that confirmed behavior instead.

## Files

| File | What it shows |
|---|---|
| `lwc/secureDownloadOld/` | Builds a CSV and creates `new Blob(csvContent, { type: 'text/csv' })`. LWS rejects the MIME type at runtime. |
| `lwc/secureDownloadNew/` | Same CSV, but `new Blob(csvContent, { type: 'text/plain' })` — an LWS-allowed MIME type. The `.csv` filename (via `link.download`) still makes it open correctly in Excel/Sheets regardless of the Blob's declared type. Cleans up with `URL.revokeObjectURL(...)` after the click. |

## Live demo script

1. Click **"Download CSV (OLD way)"** → nothing happens. Open the browser console to show the `Lightning Web Security: Unsupported MIME type.` error.
2. Click **"Download CSV (NEW way)"** → the file downloads normally.

Both components need to be added to a Lightning App Page (see root README) so they're one click away during the talk.
