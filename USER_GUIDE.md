# Pass Vault — User Guide

**Version 2.0**

Pass Vault is an offline-only password manager. Your data never leaves your device — there is no cloud, no sync, no account required. Everything is stored in an encrypted database that only unlocks with your biometric or device PIN.

---

## Table of Contents

1. [First Launch & Authentication](#1-first-launch--authentication)
2. [Home Screen](#2-home-screen)
3. [Adding a Password](#3-adding-a-password)
4. [Viewing a Saved Password](#4-viewing-a-saved-password)
5. [Updating a Password](#5-updating-a-password)
6. [Password Version History](#6-password-version-history)
7. [Deleting a Password](#7-deleting-a-password)
8. [Groups](#8-groups)
9. [Periodic Reminders](#9-periodic-reminders)
10. [Profile](#10-profile)
11. [Settings](#11-settings)
12. [Backup & Restore](#12-backup--restore)
13. [Security Features](#13-security-features)
14. [About App](#14-about-app)

---

## 1. First Launch & Authentication

When you open Pass Vault you will see the lock screen with the app logo in the centre.

**To unlock:**
- Tap the logo card in the middle of the screen
- Your device's biometric prompt will appear (fingerprint, face, or PIN)
- Authenticate to enter the app

> If you press cancel or fail authentication, the app closes. This is intentional — Pass Vault cannot be accessed without authentication.

---

## 2. Home Screen

After authentication you land on the **Home Screen**. This is your main vault view.

### What you see

- **Groups** — Every website or service you save becomes a group (e.g. "Google", "Netflix"). Groups are listed alphabetically.
- **Red border on a group card** — Means one or more passwords in that group are due for a change based on your reminder settings.
- **Search bar** — Filters groups by name in real time. While text is present, a clear (×) button appears inside the bar to reset the search. After the last result, a footer reads **"end of the line."** If nothing matches, a **"NO RESULTS"** illustration is shown.

### Navigation bar (bottom)

| Button | Action |
|--------|--------|
| House icon (left) | Home screen |
| **+** button (centre, coral) | Add a new password |
| Person icon (right) | Profile screen |

---

## 3. Adding a Password

Tap the **+** button at the bottom of any screen.

### Fields

| Field | What to enter |
|-------|--------------|
| **URL** | The website address (e.g. `google.com`). Pass Vault extracts the site name automatically from the URL to create or find the group. If no URL is entered, your ID is used as the group name. |
| **ID** | Your email address or username for that account. |
| **Password** | Your password. Tap the eye icon to show/hide it while typing. |

### Generate a password

Tap **GENERATE NEW** to instantly create a strong random password. You can tap it multiple times until you like the result.

- **Default mode** — Generates a 16-character password mixing uppercase, lowercase, numbers, and symbols.
- **Word-based mode** — If you have set custom words in **Settings → Password Words**, the generator uses two of those words combined with numbers and a symbol to build a memorable-but-strong password.

### Periodic Reminder *(optional)*

Turn on **YES** under Periodic Reminder if you want Pass Vault to remind you to change this password after a set time.

1. Choose the unit: **DAYS**, **WEEKS**, or **MONTHS**
2. Choose the number (1 – 180) from the dropdown

### Saving

Tap **ADD PASSWORD**. Pass Vault will:
- Create a new group if the site name doesn't exist yet
- Add the account inside that group

> **Duplicate detected?** If you add an ID that already exists in the same group, Pass Vault automatically takes you to the Update screen instead.

---

## 4. Viewing a Saved Password

From the Home Screen, tap a **group** → tap an **account row** to open the detail view.

### What you see

- **Date** — When this password version was created
- **Website** — The URL you saved
- **Email / Username** — Your login ID
- **Password** — Hidden by default (shown as dots)

### Version chip (top-right)

A small chip (e.g. **v1 ▼**) in the top-right corner shows the current version. Tap it to switch to an older version — see [Password Version History](#6-password-version-history).

### Revealing the password

Tap the **eye icon** next to the password field. Your biometric prompt will appear. Authenticate to reveal the password in plain text.

Tap the eye icon again to hide it immediately.

### Copying the password

Tap the **copy icon** next to the password. The password is copied to your clipboard and will **automatically clear after 30 seconds**. A notification bar at the bottom confirms the copy.

---

## 5. Updating a Password

1. Open the account detail screen
2. Tap **UPDATE** — your biometric prompt will appear; authenticate to proceed
3. Edit the URL, ID, password, or reminder settings as needed
4. Tap **SAVE CHANGES**

Each update creates a new password version. The previous password is kept in history (up to 10 versions per account).

---

## 6. Password Version History

Pass Vault keeps up to **10 previous versions** of each password. This lets you go back if you made a mistake or need to reference an old password.

### Switching versions

On the account detail screen, tap the version chip in the top-right corner (e.g. **v3 ▼**). A dropdown lists all saved versions. Tap any version to view it.

> The **Update** and **Delete** buttons are only shown when you are viewing the latest (current) version. Historical versions are read-only.

---

## 7. Deleting a Password

1. Open the account detail screen
2. Tap **DELETE**
3. A confirmation dialog explains what will happen:
   - **If older versions exist** — the current version is removed and the previous version becomes active
   - **If this is the only version** — the entire account is permanently deleted
4. Tap **Confirm** to proceed or **Cancel** to go back

> If deleting an account leaves its group empty, the group is also deleted automatically.

---

## 8. Groups

Groups are created and managed automatically — you don't create them manually.

- When you **add** a password, Pass Vault extracts the site name from the URL (or uses your ID if no URL is given) as the group name
- When you **delete** all accounts in a group, the group disappears
- Tapping a group on the Home Screen shows all accounts inside it
- Each group screen has its own **search bar** to filter accounts by username

### Adding a password to an existing group

When adding a new password, enter a URL for the same site as an existing group. Pass Vault will place the new account inside that existing group.

---

## 9. Periodic Reminders

If you set a reminder on a password, Pass Vault will notify you when it is time to change it.

### How it works

- The countdown starts from the **last time the password was changed**
- When the deadline is within **5 days**, the group and account cards show a **red border**
- A background check also sends a **push notification** to remind you

### Changing or disabling a reminder

Open the account → tap **UPDATE** → authenticate → scroll to **Periodic Reminder** → switch to **NO** (or change the value) → tap **SAVE CHANGES**.

---

## 10. Profile

Tap the **person icon** at the bottom right to open the Profile screen.

### Profile screen

Displays your name, username, and profile photo (if set). Three menu items are available:

| Menu item | What it does |
|-----------|-------------|
| **Update Profile** | Opens the Edit Profile screen to change your display name and photo |
| **Settings** | Opens the Settings screen (session timeout, backup/restore, password words) |
| **About App** | Opens the in-app user guide |

The app version is shown at the very bottom of the screen.

### Editing your profile

Tap **Update Profile** to open the Edit Profile screen:

- **Profile Photo** — Tap the photo to pick an image from your gallery. Tap **Change Picture** below the photo to open the picker.
- **Name** — Your display name shown on the profile screen.

Tap **SAVE** to save changes or **CANCEL** to discard and go back.

---

## 11. Settings

Tap the **person icon** → **Settings**.

### Session Timeout

Controls how long Pass Vault waits before locking itself when you are idle.

| Option | Description |
|--------|-------------|
| 1 minute | Locks after 1 minute of no interaction |
| 2 minutes | |
| 5 minutes *(default)* | |
| 10 minutes | |
| 15 minutes | |
| 30 minutes | Locks after 30 minutes of no interaction |

The timer runs even when you switch to another app. When you return after the timeout period, you will be taken to the lock screen and must authenticate again.

### Password Words

Lets you provide a personal word list that the **GENERATE NEW** button uses when building passwords.

- Tap **SET WORDS** to open the word editor
- Enter any words separated by spaces, commas, or new lines (e.g. `apple tiger ocean sunrise`)
- Pass Vault picks two words at random, capitalises them, inserts a 2-digit number, and appends a special character — creating a password that is both strong and easier to remember
- Leave the list empty to use the default fully-random 16-character generator

---

## 12. Backup & Restore

### Creating a backup

1. Go to **Settings** → tap **EXPORT BACKUP**
2. Your biometric prompt appears — authenticate to confirm the export
3. Choose where to save the file on your device (any folder, Google Drive, etc.)
4. The file is saved as `passvault_backup_YYYYMMDD_HHMMSS.json`

> The backup file contains all your groups, accounts, and complete password history in plain JSON. **Store it somewhere safe.** Anyone with the file can read your passwords.

### Restoring from a backup

1. Go to **Settings** → tap **IMPORT BACKUP**
2. Browse to your backup `.json` file and select it
3. Pass Vault reads the file and imports all data
4. A summary message tells you how many accounts were restored and how many were skipped (duplicates already in the vault are never overwritten)

> Restore is **additive** — it never deletes existing data. If an account with the same site + username already exists, it is left untouched.

---

## 13. Security Features

Pass Vault is built with several layers of protection.

| Feature | What it does |
|---------|-------------|
| **Encrypted database** | All data is stored using SQLCipher. The database key is generated randomly and stored in Android's encrypted key store — it cannot be read without your device credentials. |
| **Biometric gate on launch** | The app cannot be opened without fingerprint, face, or PIN authentication. |
| **Biometric gate on password reveal** | Even after you are inside the app, revealing a password requires a second biometric check. |
| **Biometric gate on update** | Tapping UPDATE on the account detail screen requires biometric authentication before the edit form opens. |
| **Biometric gate on export** | Exporting a backup requires biometric authentication before the file is written. |
| **Session timeout** | The app auto-locks after a configurable idle period (even in background). |
| **Screenshot protection** | The password detail screen blocks screenshots and is hidden in the Recent Apps view. |
| **Clipboard auto-clear** | Copied passwords are erased from the clipboard after 30 seconds. |
| **No internet permission** | Pass Vault has no network access at all. Your data cannot be sent anywhere. |
| **No cloud backup** | Android's auto-backup is disabled for Pass Vault. Your vault never leaves the device through the OS. |

---

## 14. About App

Tap the **person icon** → **About App** to open the in-app guide. The guide loads locally with no internet connection required.

---

## Tips

- **Lost your passwords?** If you change or reset your device without making a backup first, your vault data is gone — there is no recovery mechanism. **Export a backup regularly.**
- **Backup file security** — The JSON file is not encrypted on its own. If you share the file or store it in cloud storage, make sure access is protected.
- **Password generator** — Use the word list in Settings to create passwords that are easier to type but still strong. Leave it blank for fully random 16-character passwords.
- **Reminder timing** — Set reminders based on how sensitive an account is. Banking passwords: 1–3 months. Less critical accounts: 6–12 months.
- **Red border** — A red border on a group or account card means a reminder is due within 5 days. Tap the account, update the password, and the border clears automatically.

---

*Pass Vault v2.0 — offline, encrypted, yours.*
