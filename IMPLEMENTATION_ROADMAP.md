# Focus Reset implementation roadmap

## Milestone 1 — Local Android MVP

- [x] Native Compose project and visual theme
- [x] Solo onboarding without an account
- [x] Optional Android Usage Access entry point
- [x] Free 7-day challenge enrollment and local persistence
- [x] One scored Daily Focus Run containing three seeded games
- [x] Five Braincup-derived finite games with adapted Focus Reset UI and scoring
- [x] Clarity Score and deterministic leaderboard ordering
- [x] Perfect-day check-in and recovery-day Reset Round
- [x] Challenge-first dark dashboard with seven-day check-in history
- [x] Daily No-Reels check-in independent from optional mind-game play
- [x] Ghost Grid, Flash Crowd, Path Finder, Schulte Table, and Digit Memory
- [x] Apache 2.0 attribution and bundled Braincup license
- [x] Local Room history plus DataStore settings/challenge state
- [x] Daily practice cap
- [x] Core deterministic unit tests
- [x] Explicit Android system-back navigation with route tests
- [x] Compile the debug APK and run Android unit tests
- [x] Install and usability-test the APK on a physical Android device
- [ ] Add Compose UI tests for the complete milestone flow

## Milestone 2 — Product-quality challenge experience

- [x] Add finite-game introductions and explain each game’s difficulty progression
- [x] Protect active rounds when the app is backgrounded and require a fair restart
- [x] Add TalkBack board semantics and restrained completion haptics
- [x] Add independent optional completion-sound and haptic controls
- [ ] Complete TalkBack, large-font, reduced-motion, and OEM accessibility device testing
- [x] Add selectable local Usage Stats apps and honest disabled/no-data/manufacturer messaging
- [x] Add automatic missed-day handling, challenge completion, and restart controls
- [x] Add a full challenge history and day-by-day record screen
- [x] Add configurable check-in reminders, notification permission, and check-in-aware WorkManager scheduling
- [x] Add private per-day reflection notes with Room 1→2 migration and history excerpts
- [x] Add in-app privacy disclosure and confirmed deletion of all local user data
- [x] Export and version Room schemas for safer future migrations
- [x] Add privacy-safe local share-card image rendering and FileProvider sharing
- [ ] Move all user-facing Android text into localized resources

## Milestone 3 — Accounts, squads, and trusted competition

- [ ] Configure the production Firebase project and environment files
- [ ] Add Google sign-in only when squads or backup are requested
- [ ] Build squad creation, joining, invites, reactions, group streaks, and leaderboards
- [ ] Issue daily seeds from Cloud Functions and validate submitted scores
- [ ] Enforce Firestore rules, App Check, Play Integrity, duplicate-attempt prevention, and deletion flows
- [ ] Connect Android App Links and the web invite page to real challenge data

## Milestone 4 — Premium and launch

- [x] Implement 14-day and 30-day local challenge logic with rolling-week debug previews
- [ ] Connect premium 14-day/30-day release access to verified billing entitlements
- [ ] Configure Play Billing, entitlements, restoration, and backend validation
- [ ] Add premium analytics, history, themes, and future game-pack hooks
- [ ] Complete privacy policy, terms, support, account deletion, and Play data-safety forms
- [ ] Run device, accessibility, security, billing, offline, and closed-beta testing
- [ ] Finalize trademark-cleared branding and release progressively
