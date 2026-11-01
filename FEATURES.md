# GMM Django Website → Native Android App Feature Map

Every customer-facing feature of the GMM Distributors platform (`gmm-latest.zip`) plus the requested WhatsApp-first offline payment flow is built natively into `gmm-android`:

| GMM Platform Feature | Native Android Screen (`gmm-android`) | API / Storage Used |
| :--- | :--- | :--- |
| **100% Organic Catalogue (10 Products)** | **Shop Tab** (`ui/ShopTab.kt`): 7 Organic Fertilizers + 3 Organic Bio-Pesticides, Category filter chips, Search bar | `DummyCatalog.kt` + `/api/v1/catalog/` + `LocalDb` cache |
| **Pack Variants (`ProductVariant`)** | **Shop Tab & Product Detail**: Interactive pack size selector (`1 kg`, `5 kg`, `250 ml`, etc.) with live stock & GST/HSN display | `Models.kt` (`ProductVariant`) |
| **Crop Dosage Calculator (`CropDose`)** | **Product Detail Screen** (`ui/ShopTab.kt`): Interactive *"How much do I need?"* calculator by crop and acreage/trees/spray water | `Models.kt` (`CropDose`) |
| **Cart & Order Safety Limits** | **Cart Tab** (`ui/CartCheckoutTab.kt`): Max 10 distinct products, max 10 quantity per pack, GST-inclusive tax split, free delivery ≥ ₹1,000 | `LocalDb.kt` (`cart` table) + `DummyServer.kt` |
| **Serviceable PIN Codes (`ServiceablePin`)** | **Cart & Address Form**: Checks PIN codes (`712601 Arambagh`, `712612 Goghat`, `712413 Khanakul`, etc.) and delivery fees | `DummyCatalog.PINS` |
| **WhatsApp Order Notification (No Gateway)** | **Checkout & Order Detail** (`MainActivity.kt` + `AppController.kt`): Generates formatted WhatsApp order receipt sent to `+91 9083732101` | Native `com.whatsapp` / `com.whatsapp.w4b` / `wa.me` Intent |
| **Manual UPI / Bank / COD Payment** | **Order Detail Screen** (`ui/OrdersTab.kt`): Shows owner's UPI ID & SBI Bank details with 1-tap Copy + UTR reference logger | `/api/v1/orders/utr/` + WhatsApp confirmation |
| **GST Tax Invoice (`apps/invoicing`)** | **Invoice Screen** (`ui/OrdersTab.kt`): Native Tax Invoice (`GMM/2026-27/XXXX`) with HSN, Taxable Value, CGST + SGST split | `InvoiceScreen` in `ui/OrdersTab.kt` |
| **Krishi Paramarsha (`apps/advisory`)** | **Advisory Tab** (`ui/AdvisoryTab.kt`): 5 Crop Guides (Paddy, Potato, Brinjal, Mustard, Jute), 12-Month Crop Calendar, Organic Pest Remedies | `DummyCatalog.CROP_GUIDES` |
| **Thought Card Maker (`apps/thoughts`)** | **Thought Card Screen** (`ThoughtCard.kt` + `ui/AdvisoryTab.kt`): Native Android `Canvas` poster generator shared via `FileProvider` | `ThoughtCard.render(...)` |
| **Bilingual English + Bengali (`বাংলা`)** | **Top Bar & Profile Tab** (`ui/I18n.kt`): Instant 1-tap toggle between `English` and `বাংলা` across all screens & products | `Lang.current` + `Prefs.language` |
| **Light & Dark Theme (`theme.css`)** | **Top Bar & Profile Tab** (`ui/Theme.kt`): Instant toggle between Light (`#F5F7F2`) and Dark (`#0D1410`) organic palettes | `ThemeState.dark` + `Prefs.theme` |
| **Company, Leadership & Visiting Card** | **Profile Tab** (`ui/ProfileTab.kt`): Native screens for Vision & Mission, Board of Directors (Chikur Kumar Ray & Bina Debnath), Digital Visiting Card | Bundled drawables in `res/drawable/` |
| **Django Backend Switcher** | **Profile Tab → Backend & API Mode**: Switch between Built-in Dummy API Mode and Live Python Django Server URL | `Prefs.useDummyApi` + `Api.kt` |
