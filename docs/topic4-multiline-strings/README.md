# Topic 4 — Multiline Strings & `String.template()`

No deployment needed — run these directly as Anonymous Apex (VS Code: right-click → "Execute Anonymous Apex", or `sf apex run`).

## Files

| File | What it shows |
|---|---|
| `scripts/apex/multiline-string-before.apex` | The old way: string concatenation with `+` and manual `\n` escapes to build an email body and a JSON payload. |
| `scripts/apex/multiline-string-after.apex` | The new way: triple-quoted `'''...'''` multiline literals passed to `String.template()`, with `${...}` placeholders. |

## Live demo script

1. Run `multiline-string-before.apex`.
2. Run `multiline-string-after.apex`.
3. Compare the debug log output side by side — same result, far less noisy source.

## Two things to call out live

- The newline right after the opening `'''` is trimmed — the string starts at `Hello`, not a blank line.
- `${...}` takes a simple variable in scope, not an arbitrary expression — that's why the script pulls `ct.FirstName` into `firstName` first (same for `acc.Name` → `accountName` and `acc.AnnualRevenue` → `revenue`).
