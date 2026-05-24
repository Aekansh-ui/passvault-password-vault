# Password Vault — Product Requirements Document v2

| | |
|---|---|
| **Product** | Pass Vault (Android) |
| **Version** | 2.0 (UI-grounded revision) |
| **Supersedes** | PRD.md (v1.0 — written from spec alone) |
| **Status** | Updated after visual review of all 12 PNG mockups |
| **Last updated** | 2026-05-24 |
| **Platform** | Android (native, Jetpack Compose + Material 3) |
| **App type** | Offline-first, local-only, zero-network |
| **App name** | **Pass Vault** |
| **Tagline** | "The only password manager you'll ever need." |

---

## Revision Notes (v1 → v2)

The following material changes were made after visually reviewing all 12 PNG mockups in `./ui_files/png/`:

| # | Area | v1 assumption | v2 correction (from mockups) |
|---|---|---|---|
| 1 | App name | "Password Vault" | **"Pass Vault"** (startup screen) |
| 2 | Account Details mockup | Missing — listed as open design item | **Exists**: `group_home_page_hidden.png` + `group_home_page_show_password.png` |
| 3 | Group list screen | Password show/hide toggle on the group list | Toggle is on the **Account Details** screen only; group list shows email addresses with no password |
| 4 | Group card content | Icon + name + account count | Mockup shows icon + name only (no count visible) |
| 5 | Version dropdown | Top-right of Account Details | Confirmed: **"v1 ▼"** chip top-right, same screen as delete/update |
| 6 | Profile menu items | Display name + profile image only | Mockup shows: Update Profile, **Change Master Password**, **Autofill Settings**, **Switch to Dark Mode**, **Logout**, **v 0.1.2** |
| 7 | Add New form | Generic fields | Confirmed field labels: NAME, URL, EMAIL / USERNAME, PASSWORD + **"GENERATE NEW"** button |
| 8 | Update form | Same as Add | Confirmed: "UPDATE" heading, pre-filled, **"SAVE CHANGES"** CTA + "GENERATE NEW" |
| 9 | Bottom navigation | FAB only | 3-item persistent nav: Home icon | **"+" FAB (coral)** | Profile icon |
| 10 | Search placeholder | "Search Websites" | Confirmed: **"Search Websites..."** |
| 11 | Search end state | Not specified | Shows **"end of the line."** footer after last result |
| 12 | No-results state | Generic empty | Confirmed: detective illustration + **"NO RESULTS"** heading + helper text |
| 13 | Startup | Logo only | Matisse-style abstract art background + centered white card with logo |

---

## 1. Product Overview

### 1.1 Summary
**Pass Vault** is a fully offline Android application that lets a single device-owner securely store, organise, and recover credentials with full version history. Every account retains its **last 10 password versions** so users can roll back when a password change goes wrong.

The app holds **no server, no account sign-up, and no network calls**. Identity is the device itself: users authenticate with **the same method they use to unlock their phone** — PIN/pattern/password, fingerprint, or face — via `BiometricPrompt`. All data is **encrypted at rest** with a key sealed in the **Android Keystore**.

### 1.2 Goals
- Provide a trustworthy, private credential store that **never leaves the device**.
- Make **password version history** (up to 10) a first-class feature.
- Require **zero account creation** — device auth is the only gate.
- Keep everything **encrypted at rest** and locked on every resume.

### 1.3 Non-Goals (v1.0)
- ❌ Cloud sync / multi-device sync
- ❌ Cross-account sharing or team vaults
- ❌ Password autofill in other apps (deferred — Autofill Settings in Profile is a placeholder for v2)
- ❌ Breach checks or any feature requiring network
- ❌ Multiple in-app user profiles

### 1.4 Success Metrics
| Metric | Target |
|---|---|
| Cold-start to authenticated Home | < 1.5 s on mid-tier device |
| Crash-free sessions | > 99.5% |
| Biometric auth success rate | > 98% |
| Data loss incidents | 0 |
| Network egress (manifest + traffic capture) | 0 bytes |

---

## 2. User Personas

### Persona A — "Priya, privacy-first individual"
- 32, marketing manager. Non-technical.
- ~30–60 logins; distrusts cloud vaults.
- Needs version history: frequently forced to change work passwords and forgets which variant she last used.

### Persona B — "Arjun, security-conscious developer"
- 28, software engineer. Technical.
- Wants verifiable offline guarantee, strong encryption, zero telemetry.
- Rotates credentials often; treats the 10-version log as a lightweight audit trail.

### Persona C — "Meera, household manager"
- 45, manages family and household accounts.
- Needs quick recovery of "the password I set last month" without triggering a full reset.

---

## 3. Functional Requirements

Requirements are tagged MUST / SHOULD / MAY (MoSCoW).

---

### 3.1 Authentication & Session

| ID | Priority | Requirement |
|---|---|---|
| FR-AUTH-1 | MUST | On every cold launch, the user MUST authenticate via `BiometricPrompt` before any vault data is decrypted or shown. |
| FR-AUTH-2 | MUST | Support **Class 3 (strong) biometrics** (fingerprint, face) **and** device credential (PIN/pattern/password) as fallback: `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`. |
| FR-AUTH-3 | MUST | If the device has no secure lock set, show a blocking screen instructing the user to enable one; vault stays inaccessible. |
| FR-AUTH-4 | MUST | First launch = provisioning. A successful device auth generates the master key in Keystore and initialises the encrypted DB. No separate sign-up screen. |
| FR-AUTH-5 | MUST | Auto-lock: re-require auth after the app moves to background or after a configurable idle timeout (default **60 s**). |
| FR-AUTH-6 | MUST | Set `FLAG_SECURE` on all screens that render secrets. No screenshots, no recents thumbnail of sensitive content. |
| FR-AUTH-7 | SHOULD | Logout (visible in Profile screen) clears the in-memory session; the encrypted DB on disk is retained. Next launch re-authenticates normally. |
| FR-AUTH-8 | MAY | Allow configurable idle timeout: 15 s / 30 s / 60 s / 5 min / immediate. |

---

### 3.2 Startup Screen

> **Mockup:** `startup_page.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-START-1 | MUST | Display a full-screen splash with the **Matisse-style abstract art background** (coral, orange, pink, purple-slate colour blocks with organic blob/cutout shapes). |
| FR-START-2 | MUST | Show a centred white rounded card containing the **Pass Vault logo** (two decorative mark elements + "PASS VAULT" wordmark in coral/red using Bebas font). |
| FR-START-3 | MUST | Display tagline at the bottom: **"The only password manager you'll ever need."** in SinkinSans, grey. |
| FR-START-4 | MUST | Trigger `BiometricPrompt` on splash; on success navigate to Home. On failure or cancel, stay on splash or exit. |

---

### 3.3 Home Screen (Groups)

> **Mockups:** `home_page_initial.png`, `home_page_all_group.png`, `home_page_search_result.png`, `home_page_no_result_found.png`

#### 3.3.1 Layout (all home states)
- **Top-left:** Small Pass Vault logo mark (the decorative icon, coral + slate).
- **Search bar:** Full-width rounded pill, placeholder **"Search Websites..."**, coral/accent border when focused; shows a clear (×) button while text is present.
- **Content area:** Group list or state-specific content (see below).
- **Bottom navigation bar** (persistent across Home, and Profile screens):
  - Left: Home icon
  - Centre: **"+" FAB** — large coral circle
  - Right: Profile icon

#### 3.3.2 Group list

| ID | Priority | Requirement |
|---|---|---|
| FR-HOME-1 | MUST | Display a scrollable list of **groups**; each row is a rounded card with a **slate-coloured square icon** on the left and the group name (SinkinSans) on the right. |
| FR-HOME-2 | MUST | Group names MUST be **unique** (case-insensitive normalisation). Example groups in mockup: Facebook, Amazon, Apple, Netflix. |
| FR-HOME-3 | MUST | Groups are **auto-created** from the "NAME" field entered on Add New; no manual group creation step. |
| FR-HOME-4 | MUST | Group rows show **icon + name only** (no account count badge). *(v1 PRD assumed a count — removed after viewing mockup.)* |
| FR-HOME-5 | SHOULD | Groups sorted alphabetically by default. |
| FR-HOME-6 | MUST | When the last account in a group is deleted, the group is removed automatically. |

#### 3.3.3 Empty state (first-run)

> **Mockup:** `home_page_initial.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-HOME-7 | MUST | If no credentials exist: show only the logo mark, search bar (inactive/empty), and the centre-of-screen text: **"Click on '+' icon below to save your password"** (grey, SinkinSans). |
| FR-HOME-8 | MUST | The "+" FAB is always visible even in the empty state, inviting the first entry. |

#### 3.3.4 Search states

> **Mockups:** `home_page_search_result.png`, `home_page_no_result_found.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-HOME-9 | MUST | As the user types in the search bar, filter the group list live. Matching groups (by name prefix/substring) are shown as cards. |
| FR-HOME-10 | MUST | After the last matching result, show the footer text **"end of the line."** (grey, lowercase) below the list. |
| FR-HOME-11 | MUST | If no groups match: show an **illustration** (detective with magnifying glass examining a phone), bold heading **"NO RESULTS"** (Bebas, slate), and helper text "We couldn't find anything. Try searching for something else." |
| FR-HOME-12 | MUST | A clear (×) button appears inside the search bar while text is present; tapping it clears the query and restores the full group list. |

---

### 3.4 Group Details Screen (Accounts in a Group)

> **Mockup:** `group_home_page.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-GRP-1 | MUST | Top-left: **back arrow (<)**. |
| FR-GRP-2 | MUST | Screen heading: the **group name in full-caps Bebas font** (e.g., "FACEBOOK"), large, slate colour. |
| FR-GRP-3 | MUST | List accounts belonging to the group as full-width rows, each showing a **person icon** on the left and the **email/username** (SinkinSans). |
| FR-GRP-4 | MUST | Tapping an account row navigates to the Account Details screen. |
| FR-GRP-5 | MUST | No inline password display on this screen. Passwords are only visible on the Account Details screen. |
| FR-GRP-6 | SHOULD | Accounts are unique by (group, email/username). Adding a duplicate routes to Update instead of creating a second identical entry. |
| FR-GRP-7 | MAY | The bottom nav bar is hidden on this screen (mockup shows only the home indicator, no nav bar). |

---

### 3.5 Account Details Screen

> **Mockups:** `group_home_page_hidden.png` (password masked), `group_home_page_show_password.png` (password revealed)
> *(Note: these files were mislabelled as "group home page" variants; they are the Account Details screen.)*

#### 3.5.1 Layout
- Top-left: back arrow (**<**).
- Screen heading: **group name in full-caps Bebas** (e.g., "FACEBOOK").
- **Top-right: version dropdown chip** — displays current version label (e.g., "v1") with a downward triangle (▼). Opens a picker for up to 10 versions.
- Detail fields (each preceded by a small icon):
  | Icon | Field | Example value |
  |---|---|---|
  | 📅 Calendar | Date created/updated | 25 May 2022 |
  | 🔗 Link | Website URL | www.facebook.com |
  | 👤 Person | Email / Username | james.smith@mail.gg |
  | 🔒 Lock | Password | `********` (masked) or `l2@f5_R2` (revealed) |
- Password row has two action icons on the right: **👁 eye (show/hide toggle)** + **📋 copy icon** — both in coral.
- Bottom: two full-width buttons — **DELETE** (outlined, coral text) on the left | **UPDATE** (filled coral) on the right.

#### 3.5.2 Requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-ACC-1 | MUST | Show all four fields (date, URL, email, password) for the **currently selected version**. |
| FR-ACC-2 | MUST | **Version dropdown** (top-right "v1 ▼" chip): tap to open a picker listing up to 10 versions, each labelled by save date (newest first). |
| FR-ACC-3 | MUST | Selecting an older version renders that version's data in **read-only** mode; DELETE and UPDATE buttons are hidden/disabled. |
| FR-ACC-4 | MUST | DELETE and UPDATE buttons appear only when the **latest** version is selected. |
| FR-ACC-5 | MUST | **Eye toggle**: tap to unmask → password shown in plaintext (e.g., "l2@f5_R2"); tap again to re-mask. |
| FR-ACC-6 | MUST | **Copy icon**: copies the current version's password to clipboard; auto-clears after 30–60 s; on Android 13+ marks clipboard entry as sensitive. |
| FR-ACC-7 | SHOULD | Copied value MUST NOT appear in the clipboard preview UI on Android 13+. |
| FR-ACC-8 | MUST | `FLAG_SECURE` applied; no screenshot of this screen possible. |

---

### 3.6 Versioning Rules

| ID | Priority | Requirement |
|---|---|---|
| FR-VER-1 | MUST | Tapping **UPDATE** on Account Details opens the Update screen; saving creates a **new version**, which becomes current. |
| FR-VER-2 | MUST | The system retains a **maximum of 10 versions** per account. On the 11th save, the **oldest version** is automatically evicted (FIFO ring buffer). |
| FR-VER-3 | MUST | Only the latest version is editable. Historical versions are read-only everywhere (UI + repository layer). |
| FR-VER-4 | MUST | **DELETE** removes only the current/latest version. |
| FR-VER-5 | MUST | After deletion, the **previous version automatically becomes current**. If no previous version exists, the account is deleted. |
| FR-VER-6 | MUST | Deleting the only version of the only account in a group removes the group. |
| FR-VER-7 | SHOULD | Deletion SHOULD show a confirmation dialog explaining that the prior version will become active (or the account/group will be removed). |

---

### 3.7 Add New Password Screen

> **Mockup:** `add_new _password.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-ADD-1 | MUST | Screen heading: **"ADD NEW"** (Bebas, large, slate). |
| FR-ADD-2 | MUST | Four form fields (full-width rounded inputs, label above each): **NAME** ("Website/App Name"), **URL** ("Website/App Link"), **EMAIL / USERNAME** ("Email / Username"), **PASSWORD** ("Password"). |
| FR-ADD-3 | MUST | **"GENERATE NEW"** button (outlined, coral text, right-aligned, below the Password field): generates a random secure password and populates the Password field. Fully offline. |
| FR-ADD-4 | MUST | **"ADD PASSWORD"** — full-width filled coral button at the bottom: validates fields and saves the credential. |
| FR-ADD-5 | MUST | On save: match the NAME (case-insensitive) to an existing group or auto-create a new one; create version 1 for the new account. |
| FR-ADD-6 | SHOULD | Inline validation: NAME and PASSWORD are required; URL should be a valid-format link (no reachability check); EMAIL / USERNAME required. |
| FR-ADD-7 | SHOULD | If (group name, email/username) duplicates an existing account, route to the Update screen instead of creating a duplicate. |
| FR-ADD-8 | MAY | Password field toggles between masked and visible while editing. |

---

### 3.8 Update Password Screen

> **Mockup:** `update_page.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-UPD-1 | MUST | Screen heading: **"UPDATE"** (Bebas, large, slate). |
| FR-UPD-2 | MUST | All four fields pre-populated with the current version's data (NAME locked/read-only to prevent silent group change; URL, EMAIL / USERNAME, PASSWORD editable). |
| FR-UPD-3 | MUST | **"GENERATE NEW"** button identical in function and placement to the Add screen. |
| FR-UPD-4 | MUST | **"SAVE CHANGES"** — full-width filled coral button at the bottom: saves changes as a **new version** (creating a version entry, not overwriting). |
| FR-UPD-5 | MUST | Saving triggers the 10-version cap logic (FR-VER-2). |
| FR-UPD-6 | SHOULD | Password field shows masked (`********`) by default; user can reveal before/while editing. |

---

### 3.9 Profile Screen

> **Mockup:** `Profile needed.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-PROF-1 | MUST | Screen heading: **"PROFILE"** (Bebas, large, slate). No back arrow; accessed via bottom-nav profile icon. |
| FR-PROF-2 | MUST | Display the user's **profile photo** (square, rounded corners, coral border) and below it the **display name** (Bebas, bold) and **email/username** (SinkinSans, grey). |
| FR-PROF-3 | MUST | Menu items (each with a coral icon + label, full-width, tappable rows): |
| | | • **Update Profile** → navigates to Edit Profile screen |
| | | • **Change Master Password** → flow to update the vault's master credential (see FR-PROF-8) |
| | | • **Autofill Settings** → placeholder for v2 Autofill Framework integration |
| | | • **Switch to Dark Mode** → toggles app-wide theme |
| | | • **Logout** → ends the in-memory session; encrypted DB retained; next launch re-authenticates |
| FR-PROF-4 | MUST | App version shown at the very bottom: **"v 0.1.2"** style label (grey, SinkinSans). |
| FR-PROF-5 | MUST | The bottom navigation bar is present on this screen (profile icon is the active state). |
| FR-PROF-6 | MUST | Profile photo stored encrypted in app-private files (`EncryptedFile`). |
| FR-PROF-7 | SHOULD | "Switch to Dark Mode" persists across launches (DataStore preference). |
| FR-PROF-8 | SHOULD | **Change Master Password** re-authenticates via `BiometricPrompt`, then generates a new Keystore key and re-encrypts the DB passphrase. The DB data itself is not re-encrypted; only the key-wrap changes. |

---

### 3.10 Edit Profile Screen

> **Mockup:** `Edit Profile needed.png`

| ID | Priority | Requirement |
|---|---|---|
| FR-EDIT-1 | MUST | Screen heading: **"EDIT PROFILE"** (Bebas, large, slate). Back arrow top-left. |
| FR-EDIT-2 | MUST | Profile photo displayed (square, rounded, coral border). Below it: **"Change Picture"** link in coral text; tapping opens the system Photo Picker / Camera chooser. |
| FR-EDIT-3 | MUST | **NAME** field (label above, full-width rounded input). Placeholder: "John Doe". |
| FR-EDIT-4 | MUST | Two buttons at the bottom: **CANCEL** (outlined, coral text) and **SAVE** (filled coral). |
| FR-EDIT-5 | MUST | CANCEL discards changes and navigates back to Profile. SAVE persists changes (name + encrypted image). |
| FR-EDIT-6 | SHOULD | Downscale/compress large images before encrypting and storing. |

---

## 4. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Offline** | `INTERNET` permission MUST NOT be declared in the manifest. Verifiable by manifest inspection and traffic capture (0 bytes egress). |
| **Security** | All credential data encrypted at rest (SQLCipher AES-256). Profile image encrypted via `EncryptedFile`. Keystore key non-exportable, hardware-backed where available. |
| **Performance** | Cold-start to authenticated Home < 1.5 s; 60 fps list scrolling for up to 1,000 accounts. |
| **Reliability** | All version transitions atomic (Room `@Transaction`). No data loss on update/delete. |
| **Screen security** | `FLAG_SECURE` on Account Details and any screen displaying a secret. |
| **Backup** | `android:allowBackup` MUST be `false` or `data_extraction_rules.xml` scoped to exclude DB and keys. *(Current manifest has `allowBackup="true"` — must fix before v1 release.)* |
| **Accessibility** | TalkBack labels on all interactive elements; 48×48 dp minimum touch targets; respect system font scale; WCAG AA contrast on `#545974` / `#FF6464` palette. |
| **Compatibility** | minSdk **34** (Android 14), targetSdk **36**; portrait phone form factor. |
| **Localisation** | All UI strings in `strings.xml` (English v1; structured for future locales). |
| **Privacy** | No analytics, crash reporting, or telemetry that transmits data off-device (v1). |
| **Theme** | Light mode default; Dark mode toggled via Profile → Switch to Dark Mode (persisted). |

---

## 5. User Flows

### 5.1 First launch
```
Splash (abstract art + logo) ──► Device has secure lock?
  ├─ No  ──► "Enable device lock to continue" blocking screen
  └─ Yes ──► BiometricPrompt
               ├─ Success ──► Generate Keystore key, init encrypted DB ──► Home (empty state)
               └─ Fail    ──► Retry or exit
```

### 5.2 Returning launch
```
Splash ──► BiometricPrompt ──► Success ──► Home (groups list)
                            └─ Fail   ──► stay locked, retry
```

### 5.3 Add a credential
```
Home ("+" FAB) ──► Add New screen
  ──► Fill NAME / URL / EMAIL / PASSWORD (or tap GENERATE NEW)
  ──► ADD PASSWORD ──► match/create group ──► create version 1 ──► back to Home
```

### 5.4 View an account + password history
```
Home ──► tap group card ──► Group Details (email list)
  ──► tap email row ──► Account Details (latest version, password masked)
  ──► tap 👁 to reveal password
  ──► tap "v1 ▼" dropdown ──► pick older version ──► read-only view
```

### 5.5 Update (creates a new version)
```
Account Details (latest) ──► UPDATE button ──► Update screen (pre-filled)
  ──► edit fields (or GENERATE NEW password) ──► SAVE CHANGES
  ──► new version becomes current; version count checked (evict oldest if > 10)
  ──► back to Account Details (v{n+1} selected)
```

### 5.6 Delete current version
```
Account Details (latest) ──► DELETE button ──► Confirmation dialog
  ├─ >1 version: remove latest ──► previous version is now current ──► stay on screen
  └─ only 1 version: remove account ──► if group now empty, remove group ──► back to Home/Group
```

### 5.7 Profile → Edit Profile
```
Home (Profile icon) ──► Profile screen
  ──► Update Profile ──► Edit Profile screen
      ──► Change Picture (Photo Picker) | edit NAME
      ──► SAVE ──► back to Profile | CANCEL ──► back to Profile
```

### 5.8 Logout
```
Profile ──► Logout ──► clear in-memory session ──► Splash / BiometricPrompt
```

---

## 6. Database / Storage Design

### 6.1 Engine
- **Room** (SQLite abstraction) layered over **SQLCipher** for transparent encryption.
- Single encrypted DB file in app-private storage.
- DB passphrase = random 256-bit key, wrapped/sealed by an **Android Keystore key**, unlocked only after successful `BiometricPrompt`.

### 6.2 Schema

**`groups`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | autoincrement |
| name | TEXT | display name (original casing) |
| name_normalized | TEXT | lowercased + trimmed — UNIQUE index |
| created_at | INTEGER | epoch millis |

**`accounts`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | |
| group_id | INTEGER FK → groups.id | ON DELETE CASCADE |
| username | TEXT | email/username |
| website_url | TEXT | |
| current_version_id | INTEGER FK → password_versions.id | active version pointer |
| created_at | INTEGER | |
| updated_at | INTEGER | |
| | | UNIQUE(group_id, username) |

**`password_versions`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | |
| account_id | INTEGER FK → accounts.id | ON DELETE CASCADE |
| password | TEXT | stored encrypted-at-rest via DB encryption |
| version_no | INTEGER | monotonically increasing per account |
| created_at | INTEGER | timestamp used in version dropdown label |
| | | INDEX(account_id, created_at DESC) |

**`profile`** (single row, id = 1)
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK = 1 | |
| display_name | TEXT | |
| username | TEXT | shown below name on Profile screen |
| image_path | TEXT? | path to encrypted image in app-private files |

### 6.3 Versioning invariants (all enforced inside a single `@Transaction`)

1. `accounts.current_version_id` always points to the newest version.
2. **Insert (new version):** add `password_versions` row → set as current → if `COUNT(versions) > 10`, delete oldest (`MIN(version_no)`).
3. **Delete current version:** remove newest → set `current_version_id` to next-newest → if no rows remain, delete account → if group empty, delete group.
4. The repository rejects any edit call targeting a non-current version ID (defense in depth).

### 6.4 Profile image
- Stored in app-private `files/` with a randomised filename.
- Encrypted with Jetpack Security `EncryptedFile` (Keystore-backed AES-GCM).
- Large images are downscaled/compressed before storage.

---

## 7. Security Considerations

### 7.1 Key hierarchy
```
Device auth (PIN / biometric)
        │  gates access to
        ▼
Android Keystore master key   [StrongBox/TEE, non-exportable,
        │                      setUserAuthenticationRequired = true]
        │  wraps/unwraps
        ▼
256-bit DB passphrase ──► SQLCipher-encrypted vault.db
                      └─► EncryptedFile (profile image)
```

### 7.2 Controls
| Control | Implementation |
|---|---|
| At-rest encryption | SQLCipher AES-256 (DB) + Jetpack Security `EncryptedFile` (image) |
| Key binding to auth | `setUserAuthenticationRequired(true)` on Keystore key |
| Screen capture block | `FLAG_SECURE` on Account Details + any secret-bearing screen |
| Clipboard hygiene | Auto-clear after 30–60 s; `ClipDescription.EXTRA_IS_SENSITIVE` on Android 13+ |
| Network barrier | No `INTERNET` permission; physically cannot exfiltrate |
| Backup exclusion | `allowBackup=false` / scoped `data_extraction_rules.xml` |
| Auto-lock | Re-auth on background / idle timeout |
| Change Master Password | Re-auth → new Keystore key → re-wrap DB passphrase |
| Logout | Clear session key from memory; encrypted DB stays on disk |

### 7.3 Threat model
| Threat | Mitigation |
|---|---|
| Lost/stolen locked phone | Keystore key bound to device auth → data stays ciphertext |
| Malware (no root) | App-private storage + OS sandbox |
| Rooted file pull | SQLCipher + EncryptedFile → ciphertext only |
| Shoulder-surfing | Default masked passwords + explicit reveal toggle |
| Clipboard sniffing | Auto-clear + sensitive-clipboard flag |
| Screenshots / recents | `FLAG_SECURE` |
| Cloud backup leak | Backup disabled/scoped |
| Network exfiltration | No `INTERNET` permission |

---

## 8. Screen-Wise Breakdown

All mockups are in `./ui_files/png/`. Dimensions: **375×812 px**. Fonts: **Bebas** (headings) + **SinkinSans** (body). Colour palette: primary **`#545974`** (slate-indigo), accent/CTA **`#FF6464`** (coral), neutral backgrounds `#F1F1F1` / `#F4F2F2`, dividers `#BABABA`.

| # | Screen name | Mockup file | Description |
|---|---|---|---|
| 1 | **Startup / Splash** | `startup_page.png` | Full-screen abstract art (Matisse-style blobs) + centred white card with logo + tagline at bottom. Triggers BiometricPrompt. |
| 2 | **Home — empty state** | `home_page_initial.png` | Logo mark top-left, search bar, empty content, "Click on '+' icon below" grey text, bottom nav (Home/+/Profile). |
| 3 | **Home — groups list** | `home_page_all_group.png` | Logo mark, search bar, scrollable group cards (slate icon + name). Bottom nav. |
| 4 | **Home — search results** | `home_page_search_result.png` | Active search bar (coral border, × clear button), filtered group cards, "end of the line." footer. |
| 5 | **Home — no results** | `home_page_no_result_found.png` | Active search bar, detective illustration, "NO RESULTS" heading (Bebas), helper text. |
| 6 | **Group Details** | `group_home_page.png` | Back arrow, group name (Bebas large, e.g. "FACEBOOK"), email rows (person icon + address). No bottom nav. |
| 7 | **Account Details — password hidden** | `group_home_page_hidden.png` | Back arrow, group name, "v1 ▼" version chip top-right, four fields (calendar/link/person/lock icons + data), password masked (********), eye + copy icons, DELETE + UPDATE buttons. |
| 8 | **Account Details — password revealed** | `group_home_page_show_password.png` | Same as #7 but password shown in plaintext (e.g., "l2@f5_R2"); eye icon in "hide" state. |
| 9 | **Add New Password** | `add_new _password.png` | Back arrow, "ADD NEW" heading, NAME/URL/EMAIL USERNAME/PASSWORD fields, "GENERATE NEW" outlined button, "ADD PASSWORD" filled CTA. |
| 10 | **Update Password** | `update_page.png` | Back arrow, "UPDATE" heading, same form pre-filled with current data, "GENERATE NEW" button, "SAVE CHANGES" filled CTA. |
| 11 | **Profile** | `Profile needed.png` | "PROFILE" heading, profile photo (coral border), name + email, 5 menu items (Update Profile / Change Master Password / Autofill Settings / Switch to Dark Mode / Logout), app version, bottom nav. |
| 12 | **Edit Profile** | `Edit Profile needed.png` | Back arrow, "EDIT PROFILE" heading, photo + "Change Picture" link, NAME field, CANCEL + SAVE buttons. |

---

## 9. Application Architecture

### 9.1 Layers (MVVM + Repository)
```
┌────────────────────────────────────────────────────────┐
│  UI  — Jetpack Compose + Material 3                     │
│  Screens, Navigation-Compose, composable components     │
└──────────▲────────────────────────────┬────────────────┘
           │ StateFlow / UiState         │ user events
┌──────────┴────────────────────────────▼────────────────┐
│  ViewModels  (per screen group)                         │
│  AuthVM | HomeVM | GroupVM | AccountVM | ProfileVM      │
└──────────▲────────────────────────────┬────────────────┘
           │                            │
┌──────────┴────────────────────────────▼────────────────┐
│  Repositories (single source of truth)                  │
│  VaultRepository | ProfileRepository | AuthManager      │
└──────────▲────────────────────────────┬────────────────┘
    ┌──────┴──────┐       ┌─────────────┴──────┐
    ▼             ▼       ▼                    ▼
 Room DAOs   Keystore  BiometricPrompt    EncryptedFile
 (SQLCipher)  (key)      (auth)            (profile img)
```

### 9.2 Recommended libraries
| Purpose | Library |
|---|---|
| DI | Hilt |
| DB | Room + `net.zetetic:android-database-sqlcipher` |
| Crypto / keys | Android Keystore + `androidx.security:security-crypto` |
| Biometric | `androidx.biometric:biometric` |
| Navigation | Navigation-Compose |
| Async | Kotlin Coroutines + Flow |
| Image loading | Coil (Compose) |
| Preferences | Jetpack DataStore (dark mode, idle timeout) |
| Testing | Turbine (Flow), MockK, Compose UI test |

### 9.3 Repository interface (illustrative)
```kotlin
interface VaultRepository {
    fun observeGroups(): Flow<List<GroupSummary>>
    fun searchGroups(query: String): Flow<List<GroupSummary>>
    fun observeAccounts(groupId: Long): Flow<List<AccountRow>>
    fun observeAccountDetail(accountId: Long): Flow<AccountDetail>  // includes all versions
    suspend fun addCredential(name: String, url: String,
                              username: String, password: String)
    suspend fun updateCurrent(accountId: Long, url: String,
                              username: String, newPassword: String) // creates new version
    suspend fun deleteCurrentVersion(accountId: Long)                // promotes previous or deletes
    fun generatePassword(): String                                    // offline, pure function
}
```

---

## 10. Edge Cases

| Area | Edge case | Expected behaviour |
|---|---|---|
| Auth | No device lock | Blocking screen; vault inaccessible. |
| Auth | Biometrics removed/changed after provisioning | Keystore key invalidated → guide user through re-provision. |
| Auth | App backgrounded during an edit | Lock on resume; unsaved changes discarded. |
| Auth | Logout | In-memory session cleared; DB retained; next launch re-authenticates. |
| Groups | Same name, different casing ("Google" vs "google") | `name_normalized` → same group (FR-HOME-2). |
| Groups | Last account in group deleted | Group auto-removed. |
| Versions | 11th save | Oldest version evicted; user sees max 10 in dropdown. |
| Versions | Delete only remaining version | Account deleted; group cleaned if empty. |
| Versions | Edit on historical version attempted | Rejected in both UI (buttons hidden) and repository (guard clause). |
| Versions | Two versions same millisecond timestamp | Tie-break by `version_no` DESC. |
| Add | Duplicate (group, username) pair | Route to Update screen (FR-ADD-7 / FR-GRP-6). |
| Add | Empty required fields | Inline validation; block save. |
| Add / Update | GENERATE NEW tapped | Offline random password generator fills Password field. |
| Profile | Very large image selected | Downscale + compress before encrypting and storing. |
| Profile | Image picker cancelled | Previous image retained; no change. |
| Clipboard | Copied secret, app killed before auto-clear | Best-effort; use `ClipboardManager` clear; mark sensitive. |
| Storage | DB decrypt failure (corruption / key loss) | Fail safe: do not crash-loop; show explicit recovery/reset path with data-loss warning. |
| Scale | 1,000+ accounts | Paged/lazy lists; Room query uses index on `name_normalized`, `account_id`. |
| Search | Query matches account email but not group | Decision needed: search groups only (current mockup) or also accounts? Default to group-name search; see §11. |
| Theme | Dark mode toggle | Persisted in DataStore; applied via `AppTheme` composable; system setting NOT automatically followed (user controls explicitly via Profile). |

---

## 11. Open Design Items

| # | Item | Status |
|---|---|---|
| 1 | **Account Details re-labelling** | `group_home_page_hidden/show_password.png` are the Account Details screen. File renames recommended for clarity. |
| 2 | **Search scope** | Current mockup (`home_page_search_result.png`) filters only by group name. Should search also match account usernames/emails? Recommend group-name search in v1 with email search in v2. |
| 3 | **"Restore this version"** | Selecting a historical version is currently read-only. Should a "Restore" action (copy old password into a new current version) be added? Strongly recommended for v2 fast-follow. |
| 4 | **Change Master Password flow** | Visible in Profile mockup but no dedicated mockup exists. Design needed before implementation. |
| 5 | **Autofill Settings** | Listed in Profile menu; treat as a v2 placeholder. Tap should show "Coming soon" in v1 to avoid dead-end navigation. |
| 6 | **`allowBackup` in manifest** | Currently `true`. **Must** be changed to `false` or properly scoped before release. |
| 7 | **Group icon source** | Mockup shows a plain slate square icon for each group. Should these auto-fetch a favicon (requires network — contradicts offline constraint) or use platform-initial avatars? Recommend: locally generated initial/colour avatar, no network. |

---

## 12. Acceptance Checklist (v1 Release Gate)

- [ ] No `INTERNET` permission in merged manifest; zero network egress under traffic capture.
- [ ] Fresh install blocks until device auth succeeds.
- [ ] App name "Pass Vault" and tagline appear correctly on startup screen.
- [ ] Bottom nav (Home / "+" / Profile) persistent on Home and Profile screens; hidden on Group Details / Account Details / Add / Update / Edit Profile.
- [ ] Each account caps at exactly 10 versions; 11th save evicts the oldest.
- [ ] Version dropdown shows correct date labels; selecting a historical version is read-only (no DELETE / UPDATE visible).
- [ ] Eye toggle correctly masks/unmasks password; copy auto-clears clipboard.
- [ ] Only the latest version is editable via Update screen; "SAVE CHANGES" creates a new version.
- [ ] DELETE removes latest only; previous becomes current; last-version deletion removes account (+ empty group auto-removed).
- [ ] Group names are unique (case-insensitive); empty groups auto-removed.
- [ ] Search: live filter, "end of the line." footer, detective "NO RESULTS" illustration.
- [ ] Profile screen shows all 5 menu items + app version; Autofill Settings shows "Coming soon" in v1.
- [ ] Dark mode toggle persists across sessions (DataStore).
- [ ] DB file pulled off device is unreadable (SQLCipher verified).
- [ ] Profile image stored encrypted in app-private files.
- [ ] `FLAG_SECURE` blocks screenshots on Account Details.
- [ ] `allowBackup` is `false` or properly scoped.
- [ ] Fonts: Bebas for all headings; SinkinSans for body text.

---

## 13. Future Enhancements

| Theme | Idea |
|---|---|
| Versioning | "Restore this version" one-tap (copies old value into a new current version) |
| Autofill | Android Autofill Framework integration (Autofill Settings placeholder is already in Profile v1) |
| Search | Search by account email/username, not just group name |
| Portability | Encrypted local export/import for device migration |
| Security | Per-entry biometric re-auth for high-value items |
| Hygiene | Offline password-strength meter; reuse detection |
| Organisation | Tags, favourites, notes per account, TOTP/2FA storage |
| Group icons | Platform favicon fetching (opt-in, offline cache only) |
| Sync | Optional E2EE sync (zero-knowledge, opt-in) |
| UX | Tablet/landscape layouts; Wear OS quick-read companion |
| Trust | Reproducible builds + published offline-verification steps |

---

## 14. Technical Recommendations

Grounded in the existing repo: Jetpack Compose + Material 3, Kotlin 2.2.10, AGP 9.2.1, Compose BOM 2026.02.01, minSdk **34**, targetSdk **36**.

### 14.1 Dependencies to add (`gradle/libs.versions.toml` + `app/build.gradle.kts`)
```toml
# Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version = "2.8.x" }

# Room + SQLCipher
androidx-room-runtime  = { group = "androidx.room", name = "room-runtime" }
androidx-room-ktx      = { group = "androidx.room", name = "room-ktx" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler" }          # via KSP
sqlcipher              = { group = "net.zetetic", name = "android-database-sqlcipher", version = "4.5.x" }
androidx-sqlite        = { group = "androidx.sqlite", name = "sqlite-framework" }

# Security / Biometric
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version = "1.1.x" }
androidx-biometric       = { group = "androidx.biometric", name = "biometric", version = "1.2.x" }

# DI
hilt-android          = { group = "com.google.dagger", name = "hilt-android", version = "2.x" }
hilt-compiler         = { group = "com.google.dagger", name = "hilt-android-compiler" }  # KSP
hilt-navigation       = { group = "androidx.hilt", name = "hilt-navigation-compose" }

# Image
coil-compose = { group = "io.coil-kt", name = "coil-compose", version = "2.x" }

# Preferences
datastore-prefs = { group = "androidx.datastore", name = "datastore-preferences" }
```

### 14.2 Font registration
Place font files from `./fonts/` into `app/src/main/res/font/`:
- `bebas_regular.ttf` — headings / screen titles
- `sinkin_sans_400_regular.otf` — body
- `sinkin_sans_700_bold.otf` — subheadings, labels

Define a `FontFamily` for each in `Type.kt`; apply via Material3 `Typography`.

### 14.3 Colour registration (`Color.kt`)
```kotlin
val SlatePrimary   = Color(0xFF545974)
val CoralAccent    = Color(0xFFFF6464)
val NeutralBg      = Color(0xFFF1F1F1)
val NeutralCard    = Color(0xFFF4F2F2)
val DividerGrey    = Color(0xFFBABABA)
val CoralDark      = Color(0xFFBB4951)   // pressed/dark state
val LinkBlue       = Color(0xFF3A6798)   // URLs if displayed as links
```

### 14.4 Mandatory manifest/config changes
1. Add `android:allowBackup="false"` (or scope `data_extraction_rules.xml`).
2. Do **not** add `android.permission.INTERNET`.
3. Apply `android:windowSecure="true"` (or `FLAG_SECURE` programmatically) on Account Details activity/window.

### 14.5 Testing strategy
| Layer | What to test |
|---|---|
| Unit | 10-version cap eviction; delete-promotes-previous; unique-group normalisation; password generator output range |
| Repository | All versioning transitions as `@Transaction` integration tests using Room in-memory DB |
| ViewModel | UiState transitions for each screen; error states |
| Compose UI | Empty state, search/no-results, version dropdown read-only enforcement, button visibility |
| Security | Assert no `INTERNET` permission via test; manual DB-pull verification with SQLCipher |
| Instrumented | BiometricPrompt with test fake; Room + SQLCipher DB open on device |

### 14.6 Build hygiene
- Enable **R8 full-mode** for release; add ProGuard keep rules for SQLCipher and Room.
- Add a Lint rule / manifest-check CI step asserting no `INTERNET` permission in merged manifest.
- Consider upgrading `compileOptions` from Java 11 to **Java 17** (supported by current AGP 9.x).

---

### Appendix A — Asset inventory

| Asset | Location | Used for |
|---|---|---|
| 12 PNG mockups | `./ui_files/png/` | Visual spec for all screens |
| 12 SVG source files | `./ui_files/` | Original design files |
| `Logo.svg` | `./ui_files/Logo.svg` | 24×24 app icon mark |
| `Bebas-Regular.ttf` | `./fonts/bebas/` | All screen headings |
| `SinkinSans-*` (19 weights/styles) | `./fonts/sinkin-sans/` | Body, labels, subtitles |
| Existing Compose scaffold | `app/src/main/java/…/MainActivity.kt` + `ui/theme/` | Starting point |

### Appendix B — Screen-name disambiguation

| PNG filename | Actual screen |
|---|---|
| `group_home_page.png` | Group Details (account email list) |
| `group_home_page_hidden.png` | **Account Details — password hidden** |
| `group_home_page_show_password.png` | **Account Details — password revealed** |
