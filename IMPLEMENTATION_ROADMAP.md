# Focus Reset implementation roadmap

## Milestone 1 — Local Android MVP

- [x] Native Compose project and visual theme
- [x] Solo onboarding without an account
- [x] Optional Android Usage Access entry point
- [x] Free 7-day challenge enrollment and local persistence
- [x] One scored Daily Focus Run containing three seeded games
- [x] Five playable mind-game prototypes
- [x] Clarity Score and deterministic leaderboard ordering
- [x] Perfect-day check-in and recovery-day Reset Round
- [x] Local Room history plus DataStore settings/challenge state
- [x] Daily practice cap
- [x] Core deterministic unit tests
- [ ] Compile and run on an Android emulator/device
- [ ] Add Compose UI tests for the complete milestone flow

## Milestone 2 — Product-quality challenge experience

- [ ] Improve game timing, difficulty curves, pause/background handling, and accessibility
- [ ] Add selected-app budget configuration and manufacturer-specific usage-data states
- [ ] Add missed-day handling, challenge completion, restart, and history screens
- [ ] Add reminders, notification permission flow, and WorkManager retry behavior
- [ ] Add local share-card image rendering
- [ ] Move all user-facing Android text into localized resources

## Milestone 3 — Accounts, squads, and trusted competition

- [ ] Configure the production Firebase project and environment files
- [ ] Add Google sign-in only when squads or backup are requested
- [ ] Build squad creation, joining, invites, reactions, group streaks, and leaderboards
- [ ] Issue daily seeds from Cloud Functions and validate submitted scores
- [ ] Enforce Firestore rules, App Check, Play Integrity, duplicate-attempt prevention, and deletion flows
- [ ] Connect Android App Links and the web invite page to real challenge data

## Milestone 4 — Premium and launch

- [ ] Implement 14-day and 30-day programs
- [ ] Configure Play Billing, entitlements, restoration, and backend validation
- [ ] Add premium analytics, history, themes, and future game-pack hooks
- [ ] Complete privacy policy, terms, support, account deletion, and Play data-safety forms
- [ ] Run device, accessibility, security, billing, offline, and closed-beta testing
- [ ] Finalize trademark-cleared branding and release progressively
