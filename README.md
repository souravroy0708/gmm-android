# GMM Organic — Native Android App (`gmm-android`)

Kotlin + Jetpack Compose app for **GMM Distributors Private Limited** (Arambagh, Hooghly, West Bengal).
Same architecture as `bas1minute-android` (single Activity, `AppController` state holder, `Repo` + SQLite cache,
HMAC-signed API calls). Everything the customer buys comes **live from the GMM Django backend** (`gmm`):
products, prices, stock, delivery PIN codes, addresses, orders, payments and GST invoices.

## What the customer can do

1. Browse in Bengali (default) or English: three picture categories (organic fertilizer, bio-pesticide,
   organic food), search, 2-column product grid with real stock, crop dose calculator.
   Returning customers get a one-tap **"Order the same again"** card.
2. Order **without signing in** (and without internet: the order is sent when the phone reconnects).
   Optionally sign in / create an account with mobile number + password (same account as the website).
3. Delivery details typed once and remembered; members can keep up to 10 saved addresses.
4. Place an order: GST-inclusive prices, delivery fee from the PIN code, one active order at a time.
5. Pay **without a payment gateway**:
   - **UPI**: "Pay with UPI app" opens GPay / PhonePe / Paytm / BHIM with GMM's UPI ID and the amount filled in
     (`upi://pay`), then the customer types the 12-digit UTR in the app.
   - **Bank transfer** (NEFT/IMPS): account details with one-tap copy, then the reference number.
   - **Cash on delivery** where the PIN code and COD limits allow it.
6. Track the order (Placed → Confirmed → Packed → Dispatched → Delivered) and payment status
   (awaiting / UTR sent / verified / rejected with reason).
7. Open the **GST tax invoice** issued by the server (real `GMM/2026-27/0001` numbers, CGST+SGST or IGST)
   and share or save it as a **PDF**.
8. Optionally message GMM on WhatsApp; edit details; sign out; **delete account** (Play Store requirement).

Payment options only appear once the owner fills UPI / bank details in the admin (*Business settings*).
No payment details are hard-coded in the app.

## Offline-first (built for slow / no internet)

| Situation | What the app does |
| --- | --- |
| First launch, no internet | Shows the full catalogue from `assets/catalog_seed.json` (bundled) |
| Later launches | Uses the SQLite snapshot of the last download; photos cached on the phone |
| Back online | Sends the snapshot version; downloads only if prices/stock/products changed |
| Order without sign-in | Name, mobile and address typed once (kept on the phone); a guest account is created silently |
| Order online | Live price/stock check (`quote/`); if changed, cart is updated and the customer confirms once |
| Order offline | Saved as "waiting for internet"; sent automatically by `SyncWorker` (WorkManager) when any network appears, even with the app closed; "Send by SMS" fallback |
| Send fails (e.g. sold out) | Order shows "could not be sent" with the reason: *Fix in cart* or *Delete* |
| Signs in later | Orders and address from this phone move into the account |

**Release step:** refresh the bundled catalogue from the live server before building:
`python manage.py export_app_catalog <path>/gmm-android/app/src/main/assets/catalog_seed.json`

## Brand, icon and polish

- **App icon**: the GMM badge as an adaptive icon (white background, logo inside the 66dp safe zone so
  circle / squircle / rounded-square launchers never clip the ring), a monochrome layer for Android 13+
  themed icons, round icon and legacy PNGs for every density (`res/mipmap-*`).
- **Splash screen**: GMM badge on forest green (`drawable-xxxhdpi/splash_logo.png`, Android 12+ splash API).
- **App name** under the icon: "GMM Organic", or "জিএমএম অর্গানিক" on Bengali phones.
- **Play Store icon**: `GMM-PlayStore-icon-512.png` (512×512, upload in Play Console > Store listing).
- Motion and feel: skeleton shimmer while loading, press-scale on product cards, haptic tick on
  add to cart, full-screen "Order placed" moment with one clear next step (Pay now / View order).

## Mandatory updates
Google Play in-app updates (IMMEDIATE) on every app open/resume, plus a server minimum version
(Admin → Business settings → android min version). Offline the app keeps working; the update is enforced once online.
See `GMM-PlayStore-Launch.md` for the full launch sheet.

## Building for Google Play (same as Bas1Minute)
One-time, on your computer inside this repo:
```bash
./scripts/install-gh.sh        # only if `gh --version` is older than 2.20
./scripts/setup-signing.sh     # creates the signing key in ~/gmm-signing and uploads the 4 GMM_KEYSTORE_* secrets
```
**Back up `~/gmm-signing`** (password manager / private cloud). Then every push to `main` produces:
| Artifact | Use |
|---|---|
| `GMM-Organic-play-bundle-v1.0.N` | the **.aab** to upload to Google Play |
| `GMM-Organic-mapping-v1.0.N` | `mapping.txt` for readable crash reports in Play Console |
| `GMM-Organic-v1.0.N` | APK for test phones (also published as a GitHub Release `GMMOrganic.apk`) |
`./scripts/get-play-bundle.sh` starts a build and downloads all three into `./dist`.
Update the repo from a new zip with `./update-from-zip.sh ~/Downloads/gmm-android-latest.zip` (it also deletes removed files).

## Release checklist (Play Store)

1. Repository secrets for signing: `GMM_KEYSTORE_PATH`, `GMM_KEYSTORE_PASSWORD`, `GMM_KEY_ALIAS`, `GMM_KEY_PASSWORD`
   (`scripts/setup-signing.sh`). Keep the keystore backed up: losing it means you can never update the app.
2. Build `./gradlew bundleRelease` (or the GitHub Actions workflow) → upload the `.aab`.
3. Play Console: store listing (Bengali + English), 512 icon, screenshots, privacy policy URL,
   Data safety form (collects: name, phone number, address, purchase history; no location, no contacts),
   account-deletion URL or in-app path (Profile → My details → Delete account).
4. Internal testing track with 5–10 real customers first, then production.
5. Raise *Business settings → android min version* on the server only when an old build must stop working.

## Owner side (Django admin)

Set prices, GST, HSN and stock in **Catalogue > Prices & stock**; switch packs or products on/off;
upload product photos; confirm / pack / dispatch / deliver / cancel orders; verify or reject UTRs in
**Payments**; issue invoices. The app picks up every change on its next refresh (app start / resume / pull).

## Servers

| Build | Server | Change it |
| --- | --- | --- |
| Debug | `http://10.0.2.2:8000` (your laptop from the emulator) | Profile → *Developer: server*, or `-PGMM_DEBUG_SERVER=http://192.168.1.5:8000` |
| Release | `https://shop.gmmdistributors.xyz` | `./gradlew assembleRelease -PGMM_SERVER=https://your.domain` |

Release builds are HTTPS-only and cannot change server. For a real phone on Wi-Fi run Django with
`python manage.py runserver 0.0.0.0:8000` and add your laptop's IP to `ALLOWED_HOSTS`.

## Project layout

```
app/src/main/java/com/gmmdistributors/app/
  MainActivity.kt      UPI pay intent, PDF invoice sharing, WhatsApp, dialer, clipboard
  AppController.kt     App state: account, catalogue, cart (stock-checked), addresses, orders, invoice
  AdvisoryContent.kt   Krishi Paramarsha: 5 crop guides, calendar, daily tips (bundled content)
  InvoicePdf.kt        A4 tax invoice drawn with Android's PdfDocument
  LocalDb.kt           SQLite: cart, catalogue snapshot, orders, orders waiting to be sent, cached API responses
  SyncWorker.kt        WorkManager job: sends saved orders and refreshes the catalogue when a network appears
  Prefs.kt             Language, theme, debug server URL
  ThoughtCard.kt       1080x1350 thought-card poster
  api/
    Api.kt             HTTP client, Bearer token + HMAC-SHA256 signing (X-GMM-Time/Nonce/Sign)
    Repo.kt            All /api/v1/ calls + offline cache
    Models.kt          JSON models (SiteConfig, Product, Variant, Address, Order, Invoice, Me)
    SecretStore.kt     Token and signing secret encrypted with Android Keystore (AES-GCM)
  ui/
    AuthScreen.kt      Sign in / create account
    ShopTab.kt         Catalogue, product detail, dose calculator, stock-aware add to cart
    CartCheckoutTab.kt Cart, addresses, payment method, checkout checks
    OrdersTab.kt       Orders, order detail (pay + UTR + timeline), invoice
    ProfileTab.kt      Account, addresses, language/theme, company pages, delete account
    AdvisoryTab.kt, MainShell.kt, Components.kt (remote product photos), Theme.kt, I18n.kt
```

The API contract is documented in the backend README (`gmm/README.md`, section *Android app API*).

## How to Build & Run

### Option A: Android Studio
1. Unzip `gmm-android-latest.zip` and open the `gmm-android` folder in **Android Studio**.
2. Let Gradle sync.
3. Connect your Android phone (or emulator) and press **Run**, or choose **Build → Build APK(s)**.

### Option B: GitHub Actions (No Android Studio Needed)
1. Push this folder to a GitHub repository on the `main` branch.
2. Open the **Actions** tab — `.github/workflows/build-apk.yml` automatically compiles the APK and uploads `GMM-Organic-v1.0.N.apk` as a downloadable artifact.
