# Password Vault — Product Requirements Document (PRD)

| | |
|---|---|
| **Product** | Password Vault (Android) |
| **Version** | 1.0 |
| **Status** | Draft for engineering kickoff |
| **Last updated** | 2026-05-24 |
| **Platform** | Android (native, Jetpack Compose) |
| **App type** | Offline-first, local-only, zero-network |
| **Owner** | aekanshgupta@gmail.com |

---

## 1. Product Overview

### 1.1 Summary
Password Vault is a **fully offline** Android application that lets a single device-owner store, organize, and recover credentials with **full version history**. Every account keeps its **last 10 password versions** so a user can roll back when a password change goes wrong or when they need a previously used value.

The app holds **no server, no account sign-up, and no network calls**. Identity is the device itself: the user authenticates with the **same security they already use to unlock the phone** — device PIN/pattern/password, fingerprint, or face biometrics — via the Android `BiometricPrompt` API. All credential data is **encrypted at rest** with a key sealed in the **Android Keystore (StrongBox / TEE-backed)**.

### 1.2 Goals
- Give users a trustworthy, **private** place to store credentials that never leaves the device.
- Make **password history** a first-class feature (the differentiator vs. typical vaults).
- Require **zero account setup** — open the app, authenticate with the device, start using it.
- Keep credentials **encrypted at rest** and gated behind device authentication at every launch.

### 1.3 Non-Goals (v1.0)
- ❌ Cloud sync / multi-device sync.
- ❌ Cross-account sharing or team vaults.
- ❌ Password autofill into other apps/browser (deferred — see §13).
- ❌ Password strength scoring, breach checks, or any feature requiring network.
- ❌ Multiple in-app user accounts (the device owner is the only "user").

### 1.4 Success Metrics
| Metric | Target |
|---|---|
| Cold-start to authenticated home | < 1.5 s on mid-tier device |
| Crash-free sessions | > 99.5% |
| Biometric auth success rate | > 98% |
| Data loss incidents | 0 |
| Network egress (verified by manifest + traffic capture) | 0 bytes |

---

## 2. User Personas

### Persona A — "Priya, the privacy-first individual"
- **Age/role:** 32, marketing manager. Non-technical.
- **Needs:** A simple, safe place for ~30–60 logins without trusting a cloud service.
- **Pain point:** Reuses passwords and forgets which variant she used after a forced reset.
- **Why version history matters:** She frequently changes passwords for work compliance and needs the prior one when a system "didn't actually save the new one."

### Persona B — "Arjun, the security-conscious power user"
- **Age/role:** 28, software developer. Technical.
- **Needs:** Strong encryption guarantees, biometric lock, no telemetry, no network.
- **Pain point:** Distrusts vaults that phone home; wants to verify the app is offline.
- **Why version history matters:** Rotates credentials often and wants an audit-like trail of the last 10 values.

### Persona C — "Meera, the shared-family-device parent"
- **Age/role:** 45, manages household + kids' accounts on one phone.
- **Needs:** Quick recovery of "the password I set last month."
- **Pain point:** Constantly resetting forgotten passwords across many platforms.
- **Why version history matters:** Recovers the previous working password instead of resetting again.

---

## 3. Functional Requirements

Requirements are tagged **MUST / SHOULD / MAY** (MoSCoW) and grouped by feature.

### 3.1 Authentication & Session
| ID | Priority | Requirement |
|---|---|---|
| FR-AUTH-1 | MUST | On every cold launch, the user MUST authenticate via `BiometricPrompt` before any vault data is decrypted or shown. |
| FR-AUTH-2 | MUST | Support **Class 3 (strong) biometrics** (fingerprint, face) **and** device credential (PIN/pattern/password) as fallback via `setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)`. |
| FR-AUTH-3 | MUST | If the device has **no** secure lock set, the app MUST show a blocking screen instructing the user to enable a device lock; the vault stays inaccessible until one is set. |
| FR-AUTH-4 | MUST | "Registration" = first launch. There is no separate sign-up; the first successful device authentication provisions the vault (generates the master key in Keystore). |
| FR-AUTH-5 | MUST | Auto-lock: the vault MUST re-require authentication after the app moves to background or after a configurable idle timeout (default **60 s**). |
| FR-AUTH-6 | SHOULD | Decrypted data MUST NOT be written to logs, screenshots, or the recents thumbnail. Set `FLAG_SECURE` on credential-bearing screens. |
| FR-AUTH-7 | MAY | Allow the user to choose the idle-timeout (15 s / 30 s / 60 s / 5 min / immediate). |

### 3.2 Home Screen (Groups)
| ID | Priority | Requirement |
|---|---|---|
| FR-HOME-1 | MUST | Display a scrollable list of **groups**, where a group represents a website/platform (e.g., Google, Facebook, Amazon). |
| FR-HOME-2 | MUST | Group names MUST be **unique** (case-insensitive). Creating an account whose platform matches an existing group adds to that group rather than creating a duplicate. |
| FR-HOME-3 | MUST | Groups are **auto-created** from the website/platform name entered when adding a credential. No manual "create group" step. |
| FR-HOME-4 | MUST | Each group row SHOULD show: group name, an icon/initial, and the count of accounts inside. |
| FR-HOME-5 | MUST | Provide **search** that filters groups (and/or accounts) by name; show an explicit **empty/"no result found"** state. |
| FR-HOME-6 | MUST | Show a distinct **first-run empty state** (no credentials yet) prompting the user to add their first password. |
| FR-HOME-7 | MUST | A primary **"Add"** action (FAB or header button) opens the Add-New-Password flow. |
| FR-HOME-8 | SHOULD | Groups SHOULD be sorted alphabetically by default; MAY offer recently-updated sort. |
| FR-HOME-9 | MUST | When the last account in a group is deleted, the empty group MUST be removed automatically. |

### 3.3 Group Details Screen (Accounts)
| ID | Priority | Requirement |
|---|---|---|
| FR-GRP-1 | MUST | List all **accounts** (email IDs / usernames) belonging to the selected group. |
| FR-GRP-2 | MUST | Each row shows the email/username and a masked password indicator; tapping a row opens the Account Details screen. |
| FR-GRP-3 | MUST | Support a **show/hide password** toggle at the group level (per the `*_show_password` / `*_hidden` mockups) that reveals the current password inline; default state is **hidden**. |
| FR-GRP-4 | MUST | Provide a quick **copy password / copy username** action; copied secret MUST auto-clear from the clipboard after ~30–60 s. |
| FR-GRP-5 | SHOULD | An account within a group is unique by (group, email/username); adding a duplicate routes to update instead of creating a second identical entry. |

### 3.4 Account Details Screen
| ID | Priority | Requirement |
|---|---|---|
| FR-ACC-1 | MUST | Display for the **currently active version**: date created/updated, website URL, email/username, password (masked by default with reveal toggle). |
| FR-ACC-2 | MUST | A **version dropdown in the top-right corner** lists up to the last **10** saved versions, labeled by their save date/time (newest first). |
| FR-ACC-3 | MUST | Selecting an older version shows that version's password and metadata in **read-only** mode. |
| FR-ACC-4 | MUST | Only the **latest** version is editable; older versions MUST NOT expose edit/save controls. |
| FR-ACC-5 | MUST | Editing controls (Update / Delete) MUST be visible **only** when the latest version is selected. |
| FR-ACC-6 | SHOULD | Show which version is "current/active" with a clear badge. |
| FR-ACC-7 | MUST | Copy-to-clipboard MUST be available for the selected version's password (with auto-clear per FR-GRP-4). |

### 3.5 Update & Delete (Versioning Rules)
| ID | Priority | Requirement |
|---|---|---|
| FR-VER-1 | MUST | An **update** to a credential creates a **new version** and marks it current. The website URL and username MAY be edited on update; the password change is what creates a version. |
| FR-VER-2 | MUST | The system retains a **maximum of 10** versions per account. On the 11th save, the **oldest** version is evicted (FIFO ring buffer). |
| FR-VER-3 | MUST | Users may update **only** the latest version; historical versions are immutable. |
| FR-VER-4 | MUST | **Delete** removes only the **current/latest** version. |
| FR-VER-5 | MUST | After deleting the latest version, the **previous version automatically becomes current/active**. |
| FR-VER-6 | MUST | Deleting the **only remaining** version deletes the account entirely; if that empties the group, the group is removed (FR-HOME-9). |
| FR-VER-7 | SHOULD | Deletion of the current version SHOULD require a confirmation dialog explaining that the prior version becomes active. |

### 3.6 Add New Password
| ID | Priority | Requirement |
|---|---|---|
| FR-ADD-1 | MUST | Capture: platform/website name (drives group), website URL, email/username, password. |
| FR-ADD-2 | MUST | On save, auto-assign to the matching group (create the group if none matches, case-insensitively). |
| FR-ADD-3 | MUST | Creating the first credential for a (group, username) pair creates version 1. |
| FR-ADD-4 | SHOULD | Provide a built-in **password generator** (length + character-class options) — fully offline. |
| FR-ADD-5 | SHOULD | Validate required fields; show inline errors. URL format validated leniently (offline, no reachability check). |

### 3.7 User Profile
| ID | Priority | Requirement |
|---|---|---|
| FR-PROF-1 | MUST | Provide a profile section to **update display name**. |
| FR-PROF-2 | MUST | Allow the user to **set/change a profile image** (from gallery/camera). |
| FR-PROF-3 | MUST | The profile image MUST be stored **locally** in app-private storage (encrypted), never uploaded. |
| FR-PROF-4 | SHOULD | Profile screen MAY expose settings: auto-lock timeout, password-generator defaults, app version. |

---

## 4. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Offline** | App MUST function with **no INTERNET permission** declared in the manifest. Verifiable by inspecting `AndroidManifest.xml` and by zero network egress under traffic capture. |
| **Security** | All credential data and the profile image MUST be encrypted at rest (see §7). The DB key MUST be non-exportable and hardware-backed where available. |
| **Performance** | Cold start to authenticated home < 1.5 s; list scrolling at 60 fps for up to ~1,000 accounts. |
| **Reliability** | No data loss across updates/deletes; all version transitions transactional (atomic). |
| **Availability** | Works with no connectivity, in airplane mode, indefinitely. |
| **Usability** | Single-handed reachable primary actions; reveal/copy obvious; matches provided mockups. |
| **Accessibility** | TalkBack labels on all interactive elements; min touch target 48×48 dp; respect system font scaling; sufficient contrast on `#545974`/`#FF6464` palette. |
| **Compatibility** | minSdk **34** (Android 14), targetSdk **36**; phone form factor (portrait, 360–420 dp width per 375×812 mockups). |
| **Maintainability** | MVVM + Repository, single source of truth via Room; unit + instrumentation tests. |
| **Localization** | All strings externalized to `strings.xml` (English v1; structured for future locales). |
| **Privacy** | No analytics, no telemetry, no crash reporting that transmits data off-device (v1). |

---

## 5. User Flows

### 5.1 First launch (registration)
```
Splash (Logo) ─▶ Device has secure lock?
                   ├─ No  ─▶ "Set a device lock to continue" (blocking)
                   └─ Yes ─▶ BiometricPrompt (biometric OR device credential)
                              ├─ Success ─▶ Generate master key in Keystore,
                              │             init encrypted DB ─▶ Home (empty state)
                              └─ Fail/Cancel ─▶ Retry / Exit
```

### 5.2 Returning launch
```
Splash ─▶ BiometricPrompt ─▶ Success ─▶ Home (groups list)
                             Fail ─▶ Retry; repeated fail ─▶ stay locked
```

### 5.3 Add a credential
```
Home (Add) ─▶ Add New Password form
   ─▶ Enter platform/URL/email/password (optional: generate)
   ─▶ Save ─▶ match/create group ─▶ create version 1 ─▶ back to Home/Group
```

### 5.4 View history & roll back
```
Home ─▶ Group ─▶ Account Details (latest version)
   ─▶ Open version dropdown (top-right) ─▶ pick older version (read-only)
   ─▶ (to make it active) user updates latest with that value, OR
   ─▶ deletes newer versions one-by-one until desired version is current
```

### 5.5 Update (creates a new version)
```
Account Details (latest) ─▶ Edit ─▶ change password (and/or URL/username)
   ─▶ Save ─▶ new version becomes current
   ─▶ if version count > 10 ─▶ evict oldest
```

### 5.6 Delete (latest version only)
```
Account Details (latest) ─▶ Delete ─▶ Confirm
   ├─ >1 version remains ─▶ remove latest ─▶ previous becomes current
   └─ only 1 version    ─▶ remove account ─▶ if group now empty, remove group
```

### 5.7 Profile
```
Profile ─▶ edit display name ─▶ save
        ─▶ change image (gallery/camera) ─▶ crop ─▶ save (encrypted, local)
```

---

## 6. Database / Storage Design

### 6.1 Engine
- **Room** (SQLite abstraction) layered over **SQLCipher** for a transparently encrypted database file.
- Single encrypted DB file in app-private storage (`/data/data/<pkg>/databases/vault.db`).
- DB passphrase = random 256-bit key, **wrapped/sealed by an Android Keystore key** and unlocked only after successful `BiometricPrompt` (see §7).

### 6.2 Schema (logical)

**`groups`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | autoincrement |
| name | TEXT | platform/website name |
| name_normalized | TEXT | lowercased/trimmed, **UNIQUE** index (enforces FR-HOME-2) |
| icon_ref | TEXT? | optional asset/initial |
| created_at | INTEGER | epoch millis |

**`accounts`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | |
| group_id | INTEGER FK → groups.id | ON DELETE CASCADE |
| username | TEXT | email/username (stored encrypted-at-rest via DB encryption) |
| website_url | TEXT | |
| current_version_id | INTEGER FK → password_versions.id | the active version |
| created_at | INTEGER | |
| updated_at | INTEGER | |
| | | UNIQUE(group_id, username) — FR-GRP-5 |

**`password_versions`**
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK | |
| account_id | INTEGER FK → accounts.id | ON DELETE CASCADE |
| password | TEXT | secret value (DB encrypted) |
| version_no | INTEGER | monotonically increasing per account |
| created_at | INTEGER | timestamp shown in dropdown |
| | | INDEX(account_id, created_at DESC) |

**`profile`** (single row)
| Column | Type | Notes |
|---|---|---|
| id | INTEGER PK = 1 | |
| display_name | TEXT | |
| image_path | TEXT? | path to encrypted image in app-private files |

### 6.3 Versioning invariants (enforced in repository + DB transaction)
1. `accounts.current_version_id` always points to the **newest** version for that account.
2. **Insert (update)**: add new `password_versions` row → set it as current → if `COUNT(versions for account) > 10`, delete the **oldest** (`MIN(created_at)`).
3. **Delete current**: remove the newest version → set current to the next-newest → if none remain, delete the account → if group now empty, delete the group.
4. Only the row equal to `current_version_id` is editable; the UI and repository both reject edits to non-current versions (defense in depth).
5. All of the above run inside a single Room `@Transaction` for atomicity.

### 6.4 Profile image storage
- Image copied into app-private `files/` (not external storage), filename randomized.
- Encrypted with **Jetpack Security `EncryptedFile`** (Keystore-backed), so it is unreadable even with root + file pull.

---

## 7. Security Considerations

### 7.1 Key hierarchy
```
Device unlock (PIN/biometric)
        │  (gates)
        ▼
Android Keystore master key  ──(StrongBox/TEE, non-exportable,
        │                        setUserAuthenticationRequired=true)
        │  wraps/unwraps
        ▼
256-bit DB passphrase  ──▶ SQLCipher-encrypted vault.db
                       └─▶ EncryptedFile (profile image)
```

### 7.2 Controls
- **Authentication binding:** The Keystore key uses `setUserAuthenticationRequired(true)` so the DB key can only be unwrapped within a valid auth window (`setUserAuthenticationParameters(timeout, BIOMETRIC_STRONG | DEVICE_CREDENTIAL)`). A lost/stolen device with a locked screen yields no plaintext.
- **At-rest encryption:** SQLCipher (AES-256) for the DB; Jetpack Security for the profile image. No plaintext secrets ever hit disk.
- **In-memory hygiene:** Decrypt secrets only on demand; avoid holding plaintext in long-lived state; clear `CharArray`/`ByteArray` buffers after use where the platform permits.
- **No network:** Manifest declares **no `android.permission.INTERNET`** — the app physically cannot exfiltrate.
- **Screen capture:** `FLAG_SECURE` on screens that render secrets → blocks screenshots and hides content in the app switcher.
- **Clipboard:** Auto-clear copied secrets after 30–60 s; on Android 13+ mark clipboard content sensitive so the OS hides the preview.
- **Backup exclusion:** Set `android:allowBackup="false"` (or scope `data_extraction_rules.xml` / `backup_rules.xml`) so the encrypted DB and keys are **excluded** from cloud/ADB backup. *(Note: current manifest has `allowBackup="true"` — this MUST be changed for v1.)*
- **Tamper/root:** Out of scope to fully defeat, but Keystore + SQLCipher mean root file access still yields only ciphertext; document residual risk.
- **Auto-lock:** Re-authenticate on background/idle (FR-AUTH-5).

### 7.3 Threat model (summary)
| Threat | Mitigation |
|---|---|
| Lost/stolen locked device | Keystore key bound to device auth; data stays ciphertext |
| Malware reading app storage (no root) | App-private storage + OS sandbox |
| Rooted-device file pull | SQLCipher + EncryptedFile → ciphertext only |
| Shoulder-surfing | Default masked passwords + reveal toggle |
| Clipboard sniffing | Auto-clear + sensitive-clipboard flag |
| Screenshot/recents leak | `FLAG_SECURE` |
| Cloud-backup leak | Backup disabled/scoped |
| Network exfiltration | No INTERNET permission |

---

## 8. Screen-Wise Breakdown

Mapped to assets in `./ui_files/` (all mockups are **375×812**). Palette: primary **`#545974`** (slate-indigo), accent **`#FF6464`** (coral), neutrals `#F1F1F1` / `#BABABA` / `#F4F2F2`. Fonts: **Bebas** (display/headers) + **SinkinSans** (body), in `./fonts/`.

| # | Screen | Source asset(s) | Key elements & behavior |
|---|---|---|---|
| 1 | **Splash / Startup** | `startup_page.svg`, `Logo.svg` | App logo; triggers auth; routes to lock screen or Home. |
| 2 | **Home — first run (empty)** | `home_page_initial.svg` | Empty state + prompt + Add action (FR-HOME-6). |
| 3 | **Home — groups list** | `home_page_all_group.svg` | Unique groups, counts, search, Add (FR-HOME-1..8). |
| 4 | **Home — search results** | `home_page_search_result.svg` | Filtered groups/accounts (FR-HOME-5). |
| 5 | **Home — no results** | `home_page_no_result_found.svg` | Explicit empty-search state. |
| 6 | **Group details** | `group_home_page.svg` | Accounts in group; rows open details (FR-GRP-1..2). |
| 7 | **Group — password hidden** | `group_home_page_hidden.svg` | Default masked passwords (FR-GRP-3). |
| 8 | **Group — password shown** | `group_home_page_show_password.svg` | Reveal toggle + copy (FR-GRP-3..4). |
| 9 | **Add new password** | `add_new _password.svg` | Platform/URL/email/password + generator; auto-group (FR-ADD-*). |
| 10 | **Update password** | `update_page.svg` | Edit latest only → creates new version (FR-VER-1). |
| 11 | **Profile** | `Profile needed.svg` | Display name + avatar (FR-PROF-1..2). |
| 12 | **Edit profile** | `Edit Profile needed.svg` | Change name/image; save locally encrypted (FR-PROF-3). |

> **Account Details screen** (date, URL, email, password + top-right **version dropdown**, FR-ACC-*) is specified in §3.4. No dedicated mockup is present in `./ui_files/` — design to match the group/update screens' visual language. **Open design item — see §11.**

---

## 9. Application / Local-Storage Architecture

> "API" here = **local data API**. There is no remote/HTTP API by design.

### 9.1 Layers (MVVM + Repository)
```
┌──────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose, Material3)                           │
│  Screens + Composables, Navigation-Compose                 │
└───────────────▲───────────────────────┬───────────────────┘
                │ state (StateFlow)      │ events
┌───────────────┴───────────────────────▼───────────────────┐
│  ViewModels (per screen)                                   │
│  Home / Group / AccountDetails / AddEdit / Profile / Auth  │
└───────────────▲───────────────────────┬───────────────────┘
                │                        │
┌───────────────┴───────────────────────▼───────────────────┐
│  Repository (single source of truth, versioning logic)     │
│  VaultRepository, ProfileRepository, AuthManager           │
└───────────────▲───────────────────────┬───────────────────┘
        ┌───────┴────────┐       ┌───────┴─────────┐
        ▼                ▼       ▼                 ▼
   Room DAOs        Keystore   BiometricPrompt   EncryptedFile
   (SQLCipher DB)   (key mgmt)  (auth)            (profile img)
```

### 9.2 Recommended components
- **DI:** Hilt.
- **DB:** Room + `net.zetetic:android-database-sqlcipher` (or `androidx.sqlite` + SQLCipher community).
- **Crypto/keys:** Android Keystore + `androidx.security:security-crypto` (Jetpack Security) for `EncryptedFile`.
- **Auth:** `androidx.biometric:biometric` (`BiometricPrompt`).
- **Nav:** Navigation-Compose.
- **Async:** Kotlin Coroutines + Flow.
- **Image:** Coil for rendering; pick via Photo Picker / `ActivityResultContracts`.

### 9.3 Repository API (illustrative)
```kotlin
interface VaultRepository {
    fun observeGroups(): Flow<List<GroupWithCount>>
    fun searchGroups(query: String): Flow<List<GroupWithCount>>
    fun observeAccounts(groupId: Long): Flow<List<AccountSummary>>
    fun observeAccount(accountId: Long): Flow<AccountDetail>            // includes versions
    suspend fun addCredential(platform: String, url: String,
                              username: String, password: String)       // FR-ADD
    suspend fun updateCurrent(accountId: Long, url: String,
                              username: String, newPassword: String)     // FR-VER-1/2
    suspend fun deleteCurrentVersion(accountId: Long)                    // FR-VER-4/5/6
}
```

---

## 10. Edge Cases

| Area | Edge case | Expected behavior |
|---|---|---|
| Auth | No device lock set | Block vault; prompt to enable a lock (FR-AUTH-3). |
| Auth | Biometrics removed/changed after enrollment | Keystore key invalidated → require re-provision/re-auth; surface a clear recovery message. |
| Auth | App backgrounded mid-session | Lock on resume (FR-AUTH-5). |
| Groups | Same platform different casing ("Google"/"google") | Treated as one group via `name_normalized` (FR-HOME-2). |
| Groups | Last account deleted | Group auto-removed (FR-HOME-9). |
| Versions | 11th save | Oldest version evicted (FR-VER-2). |
| Versions | Delete when only 1 version | Account removed; group cleaned up if empty (FR-VER-6). |
| Versions | Attempt to edit a historical version | Disallowed in UI **and** repository (FR-VER-3, FR-ACC-4). |
| Versions | Two versions share a timestamp | Tie-break dropdown ordering by `version_no` desc. |
| Add | Duplicate (group, username) | Route to update of existing account, not a new duplicate (FR-GRP-5/FR-ADD-2). |
| Add | Empty required fields / malformed URL | Inline validation; block save. |
| Profile | Very large image | Downscale/compress before encrypting + storing. |
| Profile | User cancels image pick | Keep previous image; no change. |
| Clipboard | Secret copied then app killed | Best-effort auto-clear; mark clipboard sensitive. |
| Storage | DB decrypt fails (corruption/key loss) | Fail safe: do not crash-loop; show recovery/reset path with explicit data-loss warning. |
| Scale | ~1,000+ accounts | Paged/lazy lists; indexed queries keep scrolling smooth. |
| i18n | RTL / large font scale | Layouts reflow; `supportsRtl` already enabled. |

---

## 11. Open Design Items / Decisions Needed

1. **Account Details mockup is missing** in `./ui_files/`. The version-dropdown screen (FR-ACC-*) needs a design or sign-off to reuse the update/group visual language.
2. **Roll-back semantics:** Selecting an old version is read-only (per spec). Should we add an explicit **"Restore this version"** action (which copies the old value into a new current version) for ergonomics? Spec currently implies manual update/delete only. *Recommend adding Restore as a SHOULD in a fast-follow.*
3. **Manifest `allowBackup`** is currently `true` — must flip to `false`/scoped before v1 (§7.2).
4. **Auto-lock default** (proposed 60 s) — confirm.
5. **Username sensitivity:** usernames/emails are stored in the encrypted DB; confirm they need no separate masking in the UI.

---

## 12. Edge Cases of Compliance / Verification (acceptance)

The build is "done" for v1 when:
- [ ] No `INTERNET` permission in merged manifest; traffic capture shows 0 bytes.
- [ ] Fresh install requires device auth before any data is shown.
- [ ] Each account caps at exactly 10 versions; 11th save evicts oldest.
- [ ] Only the latest version is editable; historical versions are read-only everywhere.
- [ ] Deleting current promotes previous; deleting last removes account (+ empty group).
- [ ] Group names are unique (case-insensitive); empty groups auto-removed.
- [ ] DB file pulled off device is unreadable (SQLCipher verified).
- [ ] Profile image stored encrypted in app-private storage.
- [ ] `FLAG_SECURE` blocks screenshots on secret-bearing screens.
- [ ] Backup disabled/scoped.

---

## 13. Future Enhancements

| Theme | Idea |
|---|---|
| Recovery | **"Restore this version"** one-tap (copies an old value to a new current version). |
| Convenience | **Autofill service** integration (Android Autofill Framework) for browsers/apps. |
| Hygiene | Offline password-strength meter and reuse detection (purely local). |
| Portability | **Encrypted local export/import** (e.g., password-protected file) for device migration — still no cloud. |
| Sync | Optional **end-to-end-encrypted** sync (zero-knowledge) as an opt-in module. |
| Organization | Tags/favorites, custom group icons, notes field per account, TOTP/2FA code storage. |
| Security | Decoy/duress mode; per-entry biometric re-auth for high-value items. |
| UX | Dark theme variant; tablet/landscape layouts; widget for quick copy. |
| Trust | Reproducible builds + published verification steps for the offline/no-network claim. |

---

## 14. Technical Recommendations for Android Development

Aligned with the **existing repo** (Jetpack Compose + Material3, Kotlin 2.2.10, AGP 9.x, Compose BOM 2026.02.01, minSdk 34, targetSdk 36).

### 14.1 Stack
- **Language/UI:** Kotlin + Jetpack Compose + **Material 3** (already wired).
- **Architecture:** MVVM + Repository + Clean-ish layering; unidirectional data flow with `StateFlow`.
- **DI:** Hilt.
- **Persistence:** Room + **SQLCipher** (encrypted DB). Keep all version logic inside transactional DAO/repository methods.
- **Crypto:** Android Keystore (StrongBox where available) + Jetpack Security `EncryptedFile`.
- **Auth:** AndroidX `BiometricPrompt` with `BIOMETRIC_STRONG or DEVICE_CREDENTIAL`.
- **Navigation:** Navigation-Compose.
- **Images:** Coil + Android Photo Picker.

### 14.2 Dependencies to add (to `gradle/libs.versions.toml` + `app/build.gradle.kts`)
- `androidx.navigation:navigation-compose`
- `androidx.room:room-runtime`, `room-ktx`, KSP `room-compiler`
- SQLCipher: `net.zetetic:android-database-sqlcipher` (+ `androidx.sqlite`)
- `androidx.security:security-crypto`
- `androidx.biometric:biometric`
- Hilt: `com.google.dagger:hilt-android` (+ KSP compiler) and `androidx.hilt:hilt-navigation-compose`
- `io.coil-kt:coil-compose`
- Tests: Room testing, Turbine (Flow), MockK, Compose UI test (already present).

### 14.3 Resources & theming
- Register fonts from `./fonts/`: **Bebas-Regular** (display) and the **SinkinSans** weights (body) into Compose `Type.kt` (place `.ttf/.otf` under `res/font/`).
- Define the palette in `Color.kt`: `#545974` (primary), `#FF6464` (accent/CTA, also destructive states), neutrals `#F1F1F1`/`#BABABA`/`#F4F2F2`.
- Convert `./ui_files/*.svg` to vector drawables (Android Studio "Vector Asset") where reusable as icons/illustrations; `Logo.svg` → app/launcher illustration.

### 14.4 Manifest/config changes required
- Remove any `INTERNET` permission (do not add it).
- Set `android:allowBackup="false"` (or tighten `data_extraction_rules.xml` + `backup_rules.xml` to exclude the DB and keys).
- Apply `FLAG_SECURE` on secret-bearing Activities/screens.
- Keep `supportsRtl="true"` (already set).

### 14.5 Testing strategy
- **Unit:** versioning invariants (10-cap eviction, delete→promote, unique groups) in repository; ViewModel state.
- **Instrumentation:** Room migrations + SQLCipher open; biometric flow (with test fakes); navigation.
- **Security checks:** automated test asserting no INTERNET permission; manual DB-pull verification.
- **UI:** Compose tests for empty/search/no-result states and the version dropdown read-only enforcement.

### 14.6 Build hygiene
- Enable R8/minify for release; keep ProGuard rules for SQLCipher/Room.
- Consider `kotlinOptions` and lint rules forbidding network libraries to protect the offline guarantee.
- Bump `compileOptions` to Java 17 if toolchain allows (current is 11) for modern AGP.

---

### Appendix A — Asset inventory
- **UI mockups** (`./ui_files/`, 375×812): startup, home (initial/all-group/search/no-result), group (default/hidden/show-password), add-new-password, update, profile, edit-profile, logo. *(Account-details mockup absent — §11.)*
- **Fonts** (`./fonts/`): `bebas/Bebas-Regular.ttf`; `sinkin-sans/SinkinSans-*` (Thin→XBlack, regular + italics). Licenses included (Apache for SinkinSans; Flat-it for Bebas).
- **Existing code:** Compose scaffold (`MainActivity.kt`, `ui/theme/`), Material3, version catalog configured.
