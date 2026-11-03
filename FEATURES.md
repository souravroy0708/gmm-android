# GMM platform → Android app feature map

| Platform feature (Django `gmm`) | Android screen | API |
| :--- | :--- | :--- |
| Catalogue, packs, live price & stock | Shop tab, product detail | `catalog/` |
| Crop dose (`CropDose`) | Product detail calculator | `catalog/` |
| Customer account (mobile login) | Sign in / create account, Profile | `auth/*`, `me/` |
| Addresses (max 10, one default) | Cart, Profile, address form | `addresses/*` |
| Serviceable PIN codes & delivery fee | Cart, address form | `catalog/` |
| Order rules (stock lock, max 10, one active order, idempotency) | Checkout | `orders/place/` |
| UPI / bank proof (`submit_proof`) | Order detail: Pay with UPI app + UTR | `orders/utr/` |
| Order status & cancel | Orders list, order detail timeline | `orders/`, `orders/detail/`, `orders/cancel/` |
| GST invoice (`issue_invoice`) | Invoice screen + PDF share | `orders/invoice/` |
| Krishi Paramarsha, thought cards | Advisory tab | bundled content |
| English / বাংলা | Everywhere (server errors too, `X-GMM-Lang`) | |
| Account deletion | Profile → My details | `me/delete/` |
