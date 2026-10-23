# Capture payment notifications now, parse them later

Automatic expense logging from Yape, Plin and bank notifications needs a parser for each app's text format, and those formats are unknown without real samples. v1 therefore runs a `NotificationListenerService` that stores only the raw title, text and timestamp of notifications from apps the user selects, and parses nothing; v1.1 writes the parser against those stored samples.

## Consequences

- The capture table is a stable input contract for v1.1: keep the raw text unmodified.
- The user can view, delete and switch off captured data at any time; capture is off until an app is selected.
