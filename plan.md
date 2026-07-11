# Focus Reset: Anti-Brainrot Challenge and Mind-Game App

## Summary

Build an Android app for ages 13–30 that combines 7-, 14-, and 30-day short-video reduction challenges with finite daily mind games. The product will be challenge-first, scientifically styled, socially competitive, ad-free, and careful not to claim it heals, diagnoses, or improves intelligence.

The internal working title will be **Focus Reset** because “Unrot” is already occupied. Branding will remain isolated in Android resources so it can be renamed before release.

## Product Experience

- Let solo users begin without an account; require Google sign-in only for squads, backup, and cross-device progress.
- Request Android Usage Access optionally to measure total time in selected apps. Keep detailed usage locally and upload only voluntary aggregate challenge progress.
- Ship three programs:
  - **7-Day No-Reels Reset:** free entry challenge with daily app budgets, Focus Runs, and check-ins.
  - **14-Day Focus Builder:** premium progressive reduction and longer attention exercises.
  - **30-Day Attention Reboot:** premium observe, reduce, replace, and maintain phases.
- Treat Reels-specific compliance as self-reported because Usage Stats cannot distinguish Reels from normal Instagram usage. Do not use Accessibility Service, VPN, or hard blocking in v1.
- Allow recovery after a missed target through a short Reset Round; preserve challenge progress while recording perfect and recovery days.
- Provide private squads, daily raw-score leaderboards, group streaks, emoji reactions, and static share cards. Never reveal individual screen time to squad members by default.
- Cap scored play at one five-minute Focus Run daily and practice at 15 minutes daily. End sessions with a clear “You’re done—leave your phone” screen.

## Mind Games and Scoring

- Launch with five games:
  - **Color Clash:** response inhibition using conflicting color and word rules.
  - **Memory Grid:** reconstruct briefly displayed spatial patterns.
  - **Signal Watch:** detect rare targets without premature taps.
  - **Rule Shift:** alternate between color, shape, and direction rules.
  - **Story Recall:** read once and answer delayed comprehension questions.
- Each Daily Focus Run uses three games and a server-issued daily seed. Competitive runs use identical sequences and timing for every player; practice mode adapts difficulty.
- Calculate a 0–100 **Clarity Score** from normalized accuracy, missed targets, incorrect responses, and response consistency. Describe it only as game performance.
- Rank squad members by highest raw daily score, with deterministic tie-breaking by fewer mistakes and then shorter valid completion time.
- Use a clean scientific interface: restrained animation, high contrast, no flashing rewards, one color per game domain, accessible typography, and reduced-motion support.

## Technical Design and Interfaces

- Build natively with Kotlin, Jetpack Compose, Room, DataStore, WorkManager, and `UsageStatsManager`; support Android 8+ and target the current Google Play-required SDK.
- Use Firebase Authentication, Firestore, Cloud Functions, Cloud Messaging, Remote Config, Crashlytics, App Check/Play Integrity, and Google Play Billing.
- Store solo profiles, game history, usage budgets, and detailed usage records locally. Sync only account profile, challenge membership, squad progress, submitted scores, purchases, and backup data.
- Define core models: `ChallengeProgram`, `ChallengeDay`, `UsageBudget`, `DailyGameSeed`, `FocusRun`, `GameResult`, `ClarityScore`, `Squad`, `SquadMember`, `LeaderboardEntry`, and `SubscriptionEntitlement`.
- Validate score submissions server-side against the issued seed, permitted timing, attempt count, score bounds, and Play Integrity/App Check status.
- Provide Android App Links using `/challenge/{inviteCode}`. A lightweight web page will show the inviter, challenge details, and one playable Color Clash demo before directing visitors to Google Play.
- Render share cards locally without private usage details. Include challenge name, day, score, improvement, squad milestone, and invitation link.
- Keep all brand text in localized resources; release in English first with infrastructure ready for French and Arabic.
- Monetize through an ad-free subscription: daily Focus Runs, squads, practice basics, and the seven-day challenge remain free; premium unlocks 14-/30-day programs, extended history, advanced analytics, additional themes, and future game packs.

## Testing and Launch

- Unit-test every game engine with seeded deterministic rounds, scoring boundaries, tie-breakers, challenge progression, recovery logic, usage aggregation, and subscription entitlements.
- UI-test onboarding with and without Usage Access, solo play, practice limits, challenge check-ins, Google sign-in, squad invitations, leaderboards, purchases, reduced motion, offline behavior, and account deletion.
- Integration-test Firestore security rules, score tampering, duplicate submissions, expired seeds, notification retries, App Link routing, web-demo handoff, and purchase restoration.
- Test usage reporting across Samsung, Pixel, Xiaomi, and other Android variants; clearly display “data unavailable” when manufacturers restrict background access.
- Conduct a closed beta measuring onboarding completion, first Focus Run completion, day-1/day-7 retention, challenge completion, invite conversion, squad participation, crash-free sessions, and premium conversion.
- Release progressively after privacy, Play policy, billing, accessibility, and data-deletion reviews pass.

## Assumptions

- Target audience is 13–30; children under 13 are excluded to avoid Google Play Families and COPPA scope.
- The product promises an attention-reset routine and healthier scrolling replacement—not treatment, healing, diagnosis, IQ improvement, or guaranteed cognitive benefit.
- Usage Access is optional; users without it can complete challenges using manual check-ins.
- Version one tracks and nudges rather than blocking apps or specific Reels screens.
- Final branding and trademark clearance occur before the store listing, without changing the internal architecture.
