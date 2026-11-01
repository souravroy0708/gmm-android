# GMM Organic — Native Android E-Commerce App (`gmm-android`)

Native Kotlin + Jetpack Compose Android application for **GMM Distributors Private Limited** (Arambagh, Hooghly, West Bengal), engineered using the exact production architecture of `bas1minute-android` and aligned with the domain models and business rules of the `gmm` Django 5.2 project.

---

## Core Philosophy & Business Rules

1. **100% Organic Fertilizer & Bio-Pesticide (Zero Synthetic Chemicals)**:
   - Curated fixed catalogue of **10 strictly organic products** (7 Organic Fertilizers & Soil Conditioners + 3 Biological/Botanical Pesticides & Bio-Fungicides).
   - Every product includes bilingual **English & Bengali (`বাংলা`)** names, descriptions, organic composition, benefits, pack sizes (`ProductVariant`), GST/HSN codes, and an interactive **Crop Dosage Calculator** (`CropDose`).
2. **Zero Payment Gateway — Free WhatsApp Order Notification & Manual Offline Collection**:
   - No third-party payment gateway (no Razorpay/Stripe fees or SDK bloat).
   - Customers select their preferred offline payment mode at checkout:
     - **Manual UPI Transfer (Outside App)** (`gmmdistributors@sbi`)
     - **Manual Bank Account Transfer / NEFT** (`State Bank of India`, A/C `39876543210`, IFSC `SBIN0000014`)
     - **Cash on Delivery (Manual Doorstep Collection)**
   - Placing an order generates a gap-free `GMM-YYMMDD-XXXX` order number, saves the snapshot in the phone's built-in **SQLite database (`LocalDb.kt`)**, and opens **WhatsApp** (`com.whatsapp` / `com.whatsapp.w4b` or `https://wa.me/919083732101`) with a structured receipt sent directly to the owner (**Chikur Kumar Ray**, `+91 9083732101`).
   - After paying manually via UPI or Bank Transfer outside the app, the farmer can enter the **UTR / Reference Number** in the Order Detail screen and send it to the owner on WhatsApp with one tap.
3. **Built-in Dummy API Now (`DummyServer.kt`) + Turnkey Django `/api/v1/` Later (`Api.kt`)**:
   - By default, `Prefs.useDummyApi = true` routes all `/api/v1/*` requests through `DummyServer.kt` in-app, so the app works 100% standalone offline/online right now.
   - When the Django REST API is ready, toggle **Built-in Dummy API Mode** off in **Profile → Backend & API Mode** (or change `DEFAULT_SERVER`) to point to your live Django backend with HMAC-SHA256 request signing (`X-GMM-Time`, `X-GMM-Nonce`, `X-GMM-Sign`).

---

## Fixed 10-Product 100% Organic Catalogue (`DummyCatalog.kt`)

| # | Product Name (EN / BN) | Category | Type | Packs & GST-Inclusive Price | HSN / GST |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | **Premium Jackpot Soil Booster** (`প্রিমিয়াম জ্যাকপট সয়েল বুস্টার`) | Soil Boosters | Organic Fertilizer | `1 kg` (₹250), `5 kg` (₹1,150) | `3101` (5%) |
| 2 | **Micro Max Jackpot Soil Booster** (`মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার`) | Soil Boosters | Organic Fertilizer | `5 kg` (₹650), `10 kg` (₹1,220) | `3101` (5%) |
| 3 | **Nirmala Agri Vita Green Gold** (`নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড`) | Humic Granules | Organic Fertilizer | `1 kg` (₹180), `10 kg` (₹1,600) | `3101` (5%) |
| 4 | **Jackpot Dhamaka Organic Granules** (`জ্যাকপট ধামাকা জৈব দানা`) | Humic Granules | Organic Fertilizer | `1 kg` (₹220), `5 kg` (₹980) | `3101` (5%) |
| 5 | **Organic Manure Mixture** (`জৈব সার মিশ্রণ`) | Organic Manure | Organic Fertilizer | `5 kg` (₹150), `25 kg` (₹680) | `3101` (0%) |
| 6 | **Bio Organic Manure** (`বায়ো অর্গানিক সার`) | Organic Manure | Organic Fertilizer | `5 kg` (₹200), `20 kg` (₹740) | `3101` (5%) |
| 7 | **GMM Pure Vermicompost Gold** (`জিএমএম পিওর ভার্মিকম্পোস্ট গোল্ড`) | Organic Manure | Organic Fertilizer | `5 kg` (₹140), `25 kg` (₹600) | `3101` (0%) |
| 8 | **GMM Neem Shakti 1500 PPM** (`জিএমএম নিম শক্তি ১৫০০ পিপিএম`) | Bio-Pesticides | Organic Bio-Pesticide | `250 ml` (₹160), `500 ml` (₹290), `1 L` (₹540) | `3808` (5%) |
| 9 | **GMM Tricho-Guard (Trichoderma Viride 1.5% WP)** (`জিএমএম ট্রাইকো-গার্ড`) | Bio-Pesticides | Organic Bio-Fungicide | `500 g` (₹150), `1 kg` (₹270) | `3808` (5%) |
| 10 | **GMM Beauveria Bio-Pest Shield (Liquid)** (`জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড`) | Bio-Pesticides | Organic Bio-Pesticide | `250 ml` (₹190), `500 ml` (₹350) | `3808` (5%) |

---

## Project Layout

```
app/src/main/java/com/gmmdistributors/app/
  MainActivity.kt          Single-Activity entry point; WhatsApp order intents, dialer, clipboard, FileProvider sharing
  AppController.kt         Reactive state holder for Catalog, Cart, Addresses, Orders, Advisory, and WhatsApp message builder
  DummyCatalog.kt          10 fixed organic fertilizers & bio-pesticides + 5 Krishi Paramarsha crop guides + serviceable PINs
  LocalDb.kt               Built-in Android SQLite (`gmm_organic.db`): cart, addresses, orders, cache, outbox
  Prefs.kt                 SharedPreferences: language (en/bn), theme (light/dark/system), Dummy API toggle, Django URL
  ThoughtCard.kt           Native Android Canvas 1080x1350 poster generator for GMM Thought Cards
  api/
    Models.kt              JSON parsers & serializers for Product, ProductVariant, CropDose, Order, OrderItem, Address, SiteConfig
    DummyServer.kt         In-app mock REST server enforcing Django `apps/orders/services.py` tax, stock, and cart limits
    Api.kt                 Dual-mode client: routes to `DummyServer` now or `HttpURLConnection` + HMAC-SHA256 for Django later
    Repo.kt                Repository layer with local SQLite caching & persistence
    SecretStore.kt         Android Keystore AES/GCM secret storage for HMAC request signing
  ui/
    Theme.kt               GMM Forest Green (#0F2F1E), Leaf Green (#1F6B3A), Harvest Gold (#F59A23), Anek/Mukta/Noto Bengali fonts
    I18n.kt                Reactive English + Bengali (`বাংলা`) dictionary (`t(...)`)
    Components.kt          AppCard, BigButton, GhostButton, QuantityStepper, OrganicBadge, OrderStatusChip, DrawableImage
    MainShell.kt           Top brand bar, 5 bottom tabs, floating cart bar, cross-fade screen transitions
    ShopTab.kt             Shop screen + ProductDetailScreen with interactive Crop Dosage Calculator
    CartCheckoutTab.kt     Cart screen, PIN checker, AddressFormScreen, offline payment selector, WhatsApp order dialog
    OrdersTab.kt           Orders list, OrderDetailScreen (manual UPI/Bank box + UTR submission), GST InvoiceScreen
    AdvisoryTab.kt         Krishi Paramarsha 5 crop guides, 12-month calendar, and native ThoughtCardScreen
    ProfileTab.kt          Language/theme switcher, saved addresses, Dummy vs Django API switcher, Leadership & Visiting Card
```

---

## Dummy API Contract for Future Python Django Backend (`/api/v1/`)

When you build the Django REST endpoints in `gmm/apps/shop/` or `gmm/apps/api/`, implement these 5 JSON endpoints (all `POST`, matching `DummyServer.kt`):

1. **`POST /api/v1/catalog/`**
   - Returns `{ "site": {...}, "categories": [...], "products": [...], "serviceable_pins": [...] }`.
2. **`POST /api/v1/orders/`**
   - Returns `{ "orders": [...] }` for the customer.
3. **`POST /api/v1/orders/place/`**
   - Request body:
     ```json
     {
       "address": {
         "full_name": "Ramesh Mondal",
         "phone": "9876543210",
         "line1": "Ward No. 13, Fansi Bagan",
         "village_town": "Arambagh",
         "district": "Hooghly",
         "state": "West Bengal",
         "pincode": "712601",
         "landmark": "Near Krishi Mandi"
       },
       "payment_method": "UPI",
       "note": "Need for 2 acres potato",
       "items": [
         {"variant_id": 101, "quantity": 2},
         {"variant_id": 802, "quantity": 1}
       ]
     }
     ```
   - Returns `200 OK` with `{ "order": { "order_no": "GMM-261101-0001", ... } }`.
4. **`POST /api/v1/orders/utr/`**
   - Request body: `{ "order_no": "GMM-261101-0001", "utr": "430518920144" }`
   - Calls `apps.payments.services.submit_proof` and returns `{ "order": {...} }`.
5. **`POST /api/v1/orders/cancel/`**
   - Request body: `{ "order_no": "GMM-261101-0001" }`
   - Calls `apps.orders.services.cancel_order` and returns `{ "order": {...} }`.

---

## How to Build & Run

### Option A: Android Studio
1. Unzip `gmm-android-latest.zip` and open the `gmm-android` folder in **Android Studio**.
2. Let Gradle sync.
3. Connect your Android phone (or emulator) and press **Run**, or choose **Build → Build APK(s)**.

### Option B: GitHub Actions (No Android Studio Needed)
1. Push this folder to a GitHub repository on the `main` branch.
2. Open the **Actions** tab — `.github/workflows/build-apk.yml` automatically compiles the APK and uploads `GMM-Organic-v1.0.N.apk` as a downloadable artifact.
