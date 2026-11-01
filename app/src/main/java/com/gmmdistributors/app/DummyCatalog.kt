package com.gmmdistributors.app

import com.gmmdistributors.app.api.CatalogPayload
import com.gmmdistributors.app.api.Category
import com.gmmdistributors.app.api.CropDose
import com.gmmdistributors.app.api.Product
import com.gmmdistributors.app.api.ProductVariant
import com.gmmdistributors.app.api.ServiceablePin
import com.gmmdistributors.app.api.SiteConfig

/**
 * GMM Distributors Pvt. Ltd. — 100% Organic Fertilizer & Bio-Pesticide Fixed Catalogue (10 Products).
 * Strictly zero synthetic chemicals. Matches the Django models in `apps/catalog/models.py` and `apps/advisory/data.py`.
 */
object DummyCatalog {

    val SITE_CONFIG = SiteConfig()

    val CATEGORIES = listOf(
        Category(1, "soil-boosters", "Organic Soil Boosters", "জৈব মাটি বুস্টার", 1, "fertilizer"),
        Category(2, "humic-granules", "Humic & Seaweed Granules", "হিউমিক ও শৈবাল দানা", 2, "fertilizer"),
        Category(3, "organic-manure", "Bio & Organic Manure", "জৈব ও কেঁচো সার", 3, "fertilizer"),
        Category(4, "organic-pesticides", "Organic Bio-Pesticides", "জৈব কীটনাশক ও ছত্রাকনাশক", 4, "pesticide"),
    )

    val PINS = listOf(
        ServiceablePin("712601", "Arambagh, Hooghly", "আরামবাগ, হুগলি", 0.0, true),
        ServiceablePin("712602", "Gourhati / Arambagh Rural", "গৌরহাটি / আরামবাগ গ্রামীণ", 0.0, true),
        ServiceablePin("712612", "Goghat, Hooghly", "গোঘাট, হুগলি", 30.0, true),
        ServiceablePin("712613", "Kamarpukur, Hooghly", "কামারপুকুর, হুগলি", 30.0, true),
        ServiceablePin("712413", "Khanakul, Hooghly", "খানাকুল, হুগলি", 40.0, true),
        ServiceablePin("712409", "Pursurah, Hooghly", "পুরশুড়া, হুগলি", 35.0, true),
        ServiceablePin("712410", "Tarakeswar, Hooghly", "তারকেশ্বর, হুগলি", 40.0, true),
        ServiceablePin("721212", "Ghatal, Paschim Medinipur", "ঘাটাল, পশ্চিম মেদিনীপুর", 50.0, true),
        ServiceablePin("713101", "Burdwan / Purba Bardhaman", "বর্ধমান সদর", 60.0, true),
        ServiceablePin("722141", "Kotulpur, Bankura", "কোতুলপুর, বাঁকুড়া", 50.0, true),
    )

    private const val NOTE_EN = "100% Organic & Chemical-Free. Use as directed on the product label."
    private const val NOTE_BN = "১০০% জৈব ও রাসায়নিকমুক্ত। প্যাকেটের গায়ে লেখা নিয়ম মেনে ব্যবহার করুন।"

    val PRODUCTS = listOf(
        // 1. Premium Jackpot Soil Booster (Real GMM product)
        Product(
            id = 1,
            slug = "premium-jackpot-soil-booster",
            categorySlug = "soil-boosters",
            kind = "fertilizer",
            name = "Premium Jackpot Soil Booster",
            nameBn = "প্রিমিয়াম জ্যাকপট সয়েল বুস্টার",
            shortDescription = "100% organic soil booster for vegetables, potato, paddy and fruit crops.",
            shortDescriptionBn = "সবজি, আলু, ধান ও ফলের চাষের জন্য ১০০% জৈব মাটি বুস্টার।",
            description = "Premium Jackpot Soil Booster revitalizes tired farm soil naturally. Enriched with organic humic substances, natural plant enzymes, and beneficial soil conditioning minerals, it boosts white root growth and water retention without a single drop of synthetic chemicals.",
            descriptionBn = "প্রিমিয়াম জ্যাকপট সয়েল বুস্টার প্রাকৃতিকভাবে মাটির উর্বরতা ও সাদা শিকড়ের বৃদ্ধি ঘটায়। কোনো রাসায়নিক ছাড়াই এটি মাটির জল ধারণ ক্ষমতা ও ফসলের ফলন বাড়ায়।",
            composition = "Organic Humic & Fulvic Complex (18%), Seaweed Extract (5%), Natural Roasted Bentonite Granules",
            compositionBn = "জৈব হিউমিক ও ফুলভিক কমপ্লেক্স (১৮%), সামুদ্রিক শৈবাল নির্যাস (৫%), প্রাকৃতিক বেন্টোনাইট দানা",
            benefits = listOf(
                "Stimulates rapid white root development in 7–10 days",
                "Improves soil aeration and moisture retention",
                "100% chemical-free — safe for earthworms and soil microbes"
            ),
            benefitsBn = listOf(
                "৭–১০ দিনের মধ্যে দ্রুত সাদা শিকড় বৃদ্ধি করে",
                "মাটি ঝুরঝুরে রাখে ও জল ধরে রাখার ক্ষমতা বাড়ায়",
                "১০০% রাসায়নিকমুক্ত — কেঁচো ও উপকারী জীবাণুর জন্য নিরাপদ"
            ),
            drawableName = "prod_premium_jackpot",
            isFeatured = true,
            sortOrder = 1,
            variants = listOf(
                ProductVariant(101, 1, "1 kg Pack", "১ কেজি প্যাক", "PJSB-1KG", 1.0, 250.0, 5.0, "3101", 100),
                ProductVariant(102, 1, "5 kg Pack", "৫ কেজি প্যাক", "PJSB-5KG", 5.0, 1150.0, 5.0, "3101", 45),
            ),
            cropDoses = listOf(
                CropDose("Vegetables", "সবজি", 4.0, 6.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Potato", "আলু", 5.0, 8.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Paddy", "ধান", 4.0, 5.0, "KG_ACRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 2. Micro Max Jackpot Soil Booster (Real GMM product)
        Product(
            id = 2,
            slug = "micro-max-jackpot-soil-booster",
            categorySlug = "soil-boosters",
            kind = "fertilizer",
            name = "Micro Max Jackpot Soil Booster",
            nameBn = "মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার",
            shortDescription = "Organic micronutrient-rich soil booster with a crop-wise dosage schedule.",
            shortDescriptionBn = "ফসল অনুযায়ী মাত্রা দেওয়া জৈব অনুখাদ্য সমৃদ্ধ মাটি বুস্টার।",
            description = "Micro Max Jackpot Soil Booster delivers naturally chelated organic micronutrients alongside organic matter. Ideal for correcting leaf yellowing, stunted growth, and poor flowering in paddy, potato, brinjal, tomato, and orchard trees.",
            descriptionBn = "মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার ফসলের পাতা হলুদ হওয়া রোধ করে এবং ফুল ও ফলের বৃদ্ধি ত্বরান্বিত করে। ধান, আলু, বেগুন ও ফলের গাছের জন্য বিশেষ কার্যকর।",
            composition = "Organic Amino Chelated Bio-Minerals, Natural Humates, Composted Botanical Base",
            compositionBn = "জৈব অ্যামিনো চিলেটেড বায়ো-মিনারেল, প্রাকৃতিক হিউমেট ও উদ্ভিজ্জ জৈব উপাদান",
            benefits = listOf(
                "Prevents leaf yellowing and micronutrient deficiency naturally",
                "Enhances flowering, fruit setting, and tuber size in potato",
                "Tailored crop-wise dosage for West Bengal soils"
            ),
            benefitsBn = listOf(
                "প্রাকৃতিকভাবে পাতা হলুদ হওয়া ও অনুখাদ্যের ঘাটতি দূর করে",
                "আলুর আকার এবং সবজির ফুল-ফল ধারণ ক্ষমতা বাড়ায়",
                "পশ্চিমবঙ্গের মাটির জন্য ফসলভিত্তিক সুনির্দিষ্ট মাত্রা"
            ),
            drawableName = "prod_micro_max",
            isFeatured = true,
            sortOrder = 2,
            variants = listOf(
                ProductVariant(201, 2, "5 kg Bag", "৫ কেজি ব্যাগ", "MMJSB-5KG", 5.0, 650.0, 5.0, "3101", 30),
                ProductVariant(202, 2, "10 kg Bag", "১০ কেজি ব্যাগ", "MMJSB-10KG", 10.0, 1220.0, 5.0, "3101", 20),
            ),
            cropDoses = listOf(
                CropDose("Rice / Paddy", "ধান", 3.0, 5.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Potato", "আলু", 4.0, 6.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Brinjal & Tomato", "বেগুন ও টমেটো", 4.0, 5.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Fruit Trees", "ফল গাছ", 70.0, 80.0, "G_TREE", NOTE_EN, NOTE_BN),
            )
        ),

        // 3. Nirmala Agri Vita Green Gold (Real GMM product)
        Product(
            id = 3,
            slug = "nirmala-agri-vita-green-gold",
            categorySlug = "humic-granules",
            kind = "fertilizer",
            name = "Nirmala Agri Vita Green Gold",
            nameBn = "নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড",
            shortDescription = "Granular organic soil conditioner with humic, fulvic, amino and seaweed.",
            shortDescriptionBn = "হিউমিক, ফুলভিক, অ্যামিনো ও শৈবালযুক্ত দানাদার জৈব মাটি উন্নয়নকারী।",
            description = "Nirmala Agri Vita Green Gold is GMM's flagship bio-granular conditioner. Combining four powerhouse organic biostimulants—Humic Acid, Fulvic Acid, Plant Amino Acids, and Marine Seaweed (Ascophyllum nodosum)—it unlocks bound soil nutrients and multiplies tillering in paddy and branching in vegetables.",
            descriptionBn = "নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড হলো হিউমিক, ফুলভিক, অ্যামিনো অ্যাসিড ও সামুদ্রিক শৈবালের এক অনন্য দানাদার জৈব মিশ্রণ। ধানের কুশি বৃদ্ধি ও সবজির ডালপালা বিস্তারে এটি দারুণ কাজ করে।",
            composition = "Humic Acid 12%, Fulvic Acid 3%, Marine Seaweed Extract 5%, L-Amino Acids 4%, Organic Carrier q.s.",
            compositionBn = "হিউমিক অ্যাসিড ১২%, ফুলভিক অ্যাসিড ৩%, সামুদ্রিক শৈবাল ৫%, অ্যামিনো অ্যাসিড ৪%, জৈব বাহক",
            benefits = listOf(
                "Increases paddy tillers and grain filling weight",
                "Boosts crop immunity against drought and cold stress",
                "Slow-release granules feed crops steadily for 30+ days"
            ),
            benefitsBn = listOf(
                "ধানের কুশির সংখ্যা ও দানার ওজন বৃদ্ধি করে",
                "খরা ও শীতকালীন কুয়াশার ধকল সইবার ক্ষমতা বাড়ায়",
                "ধীরে ধীরে কাজ করা দানা ৩০ দিনেরও বেশি সময় পুষ্টি জোগায়"
            ),
            drawableName = "prod_green_gold_1",
            isFeatured = true,
            sortOrder = 3,
            variants = listOf(
                ProductVariant(301, 3, "1 kg Pouch", "১ কেজি পাউচ", "NAVGG-1KG", 1.0, 180.0, 5.0, "3101", 80),
                ProductVariant(302, 3, "10 kg Bucket", "১০ কেজি বালতি", "NAVGG-10KG", 10.0, 1600.0, 5.0, "3101", 50),
            ),
            cropDoses = listOf(
                CropDose("Vegetables & Fruit Crops", "সবজি ও ফলের ফসল", 10.0, 12.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Paddy (Basal / Top Dress)", "ধান (চাপান সার)", 8.0, 10.0, "KG_ACRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 4. Jackpot Dhamaka Organic Bio-Stimulant (Real GMM asset)
        Product(
            id = 4,
            slug = "jackpot-dhamaka-bio-granules",
            categorySlug = "humic-granules",
            kind = "fertilizer",
            name = "Jackpot Dhamaka Organic Granules",
            nameBn = "জ্যাকপট ধামাকা জৈব দানা",
            shortDescription = "Concentrated organic seaweed & fulvic root activator for bumper harvest.",
            shortDescriptionBn = "বাম্পার ফলনের জন্য ঘন সামুদ্রিক শৈবাল ও ফুলভিক শিকড় সক্রিয়কারী দানা।",
            description = "Jackpot Dhamaka Organic Granules are formulated for farmers seeking vigorous vegetative growth and heavy flowering without chemical fertilizers. Ideal during active growth stages of potato, mustard, jute, and seasonal vegetables.",
            descriptionBn = "রাসায়নিক সার ছাড়াই গাছের দ্রুত বৃদ্ধি এবং প্রচুর ফুল-ফলের জন্য জ্যাকপট ধামাকা জৈব দানা তৈরি করা হয়েছে। আলু, সরিষা, পাট ও সবজি চাষের জন্য অত্যন্ত উপযোগী।",
            composition = "Seaweed Sargassum Extract (8%), Organic Fulvic & Humic Potash (15%), Neem Cake Powder Base",
            compositionBn = "সামুদ্রিক শৈবাল নির্যাস (৮%), জৈব ফুলভিক ও হিউমিক পটাশ (১৫%), নিম খোল ভিত্তিক দানা",
            benefits = listOf(
                "Promotes lush dark-green foliage and strong stems",
                "Natural neem base deters soil-borne grubs naturally",
                "Improves oil content in mustard and fiber quality in jute"
            ),
            benefitsBn = listOf(
                "গাছের পাতা গাঢ় সবুজ ও কাণ্ড মজবুত করে",
                "নিম খোল থাকায় মাটির ক্ষতিকর পোকা প্রাকৃতিকভাবে দূরে থাকে",
                "সরিষায় তেলের পরিমাণ ও পাটের আঁশের মান উন্নত করে"
            ),
            drawableName = "prod_jackpot_dhamaka",
            isFeatured = true,
            sortOrder = 4,
            variants = listOf(
                ProductVariant(401, 4, "1 kg Pack", "১ কেজি প্যাক", "JD-1KG", 1.0, 220.0, 5.0, "3101", 65),
                ProductVariant(402, 4, "5 kg Pack", "৫ কেজি প্যাক", "JD-5KG", 5.0, 980.0, 5.0, "3101", 40),
            ),
            cropDoses = listOf(
                CropDose("Mustard & Oilseeds", "সরিষা ও তেলবীজ", 5.0, 6.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Jute", "পাট", 5.0, 8.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Potato", "আলু", 6.0, 8.0, "KG_ACRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 5. Organic Manure Mixture (Real GMM product)
        Product(
            id = 5,
            slug = "organic-manure-mixture",
            categorySlug = "organic-manure",
            kind = "fertilizer",
            name = "Organic Manure Mixture",
            nameBn = "জৈব সার মিশ্রণ",
            shortDescription = "Balanced composted organic manure with neem cake, bone meal & farm humus.",
            shortDescriptionBn = "নিম খোল, কম্পোস্ট ও প্রাকৃতিক হিউমাস সমৃদ্ধ সুষম জৈব সার মিশ্রণ।",
            description = "GMM's Organic Manure Mixture is a rich, fully matured basal manure made from composted cow dung, crushed neem seed cake, castor cake, and rock phosphate. It rebuilds organic carbon in depleted soils and provides slow, steady macro and micro nutrition across the entire crop cycle.",
            descriptionBn = "পচা গোবর সার, নিম খোল, রেড়ি খোল এবং প্রাকৃতিক খনিজ উপাদানের সমন্বয়ে তৈরি সুষম জৈব সার মিশ্রণ। জমি তৈরির সময় ব্যবহার করলে মাটির জৈব কার্বন ও উর্বরতা দীর্ঘস্থায়ী হয়।",
            composition = "Composted Farmyard Humus (60%), Neem Seed Cake (20%), Castor & Mustard Cake (15%), Natural Rock Minerals (5%)",
            compositionBn = "কম্পোস্ট হিউমাস (৬০%), নিম খোল (২০%), সরিষা ও রেড়ি খোল (১৫%), প্রাকৃতিক খনিজ (৫%)",
            benefits = listOf(
                "Restores natural soil pH and organic carbon levels",
                "Zero weed seeds or harmful pathogens — fully thermophilic composted",
                "Ideal basal dressing before sowing paddy, potato, or vegetables"
            ),
            benefitsBn = listOf(
                "মাটির স্বাভাবিক pH এবং জৈব কার্বনের মাত্রা ফিরিয়ে আনে",
                "সম্পূর্ণ প্রক্রিয়াজাত হওয়ায় আগাছার বীজ বা ক্ষতিকর জীবাণু থাকে না",
                "ধান, আলু বা সবজি রোপণের আগে জমি তৈরির সেরা জৈব সার"
            ),
            drawableName = "prod_organic_manure",
            isFeatured = false,
            sortOrder = 5,
            variants = listOf(
                ProductVariant(501, 5, "5 kg Bag", "৫ কেজি ব্যাগ", "OMM-5KG", 5.0, 150.0, 0.0, "3101", 330),
                ProductVariant(502, 5, "25 kg Sack", "২৫ কেজি বস্তা", "OMM-25KG", 25.0, 680.0, 0.0, "3101", 120),
            ),
            cropDoses = listOf(
                CropDose("All Field Crops (Basal)", "সব ধরনের মাঠ ফসল", 50.0, 100.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Kitchen Garden / Orchards", "সবজি বাগান ও ফল গাছ", 500.0, 1000.0, "G_TREE", NOTE_EN, NOTE_BN),
            )
        ),

        // 6. Bio Organic Manure (Real GMM product)
        Product(
            id = 6,
            slug = "bio-organic-manure",
            categorySlug = "organic-manure",
            kind = "fertilizer",
            name = "Bio Organic Manure",
            nameBn = "বায়ো অর্গানিক সার",
            shortDescription = "Microbial-enriched bio-organic manure with N-fixing & PSB bacteria.",
            shortDescriptionBn = "নাইট্রোজেন ও ফসফেট দ্রবীভূতকারী উপকারী জীবাণু সমৃদ্ধ বায়ো-অর্গানিক সার।",
            description = "Bio Organic Manure combines high-grade organic compost with live beneficial soil microbes (Azotobacter, Phosphate Solubilizing Bacteria, and Potash Mobilizing Bacteria). It fixes atmospheric nitrogen naturally and makes locked soil phosphorus available to crop roots.",
            descriptionBn = "বায়ো অর্গানিক সারে উচ্চমানের জৈব সারের পাশাপাশি রয়েছে অ্যাজোটোব্যাক্টর ও ফসফেট দ্রবীভূতকারী উপকারী জীবাণু। এটি বাতাস থেকে নাইট্রোজেন সংবন্ধন করে এবং মাটির আবদ্ধ পুষ্টি গাছের শিকড়ে পৌঁছে দেয়।",
            composition = "Enriched Organic Compost + Azotobacter + Bacillus megaterium (PSB) + Frateuria aurantia (KMB) (10^8 CFU/g)",
            compositionBn = "সমৃদ্ধ জৈব কম্পোস্ট + অ্যাজোটোব্যাক্টর + ফসফেট ও পটাশ দ্রবীভূতকারী উপকারী জীবাণু",
            benefits = listOf(
                "Fixes nitrogen naturally and unlocks locked soil phosphate",
                "Reduces fertilizer input cost by 30–40% over two seasons",
                "Improves soil crumb structure and earthworm activity"
            ),
            benefitsBn = listOf(
                "প্রাকৃতিকভাবে নাইট্রোজেন জোগায় ও মাটির ফসফেট সচল করে",
                "চাষের সার খরচ ৩০–৪০% পর্যন্ত কমাতে সাহায্য করে",
                "মাটিতে কেঁচো ও উপকারী অণুজীবের সংখ্যা বৃদ্ধি করে"
            ),
            drawableName = "prod_bio_organic",
            isFeatured = false,
            sortOrder = 6,
            variants = listOf(
                ProductVariant(601, 6, "5 kg Bag", "৫ কেজি ব্যাগ", "BOM-5KG", 5.0, 200.0, 5.0, "3101", 500),
                ProductVariant(602, 6, "20 kg Sack", "২০ কেজি বস্তা", "BOM-20KG", 20.0, 740.0, 5.0, "3101", 150),
            ),
            cropDoses = listOf(
                CropDose("Paddy & Jute", "ধান ও পাট", 20.0, 40.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Potato & Vegetables", "আলু ও সবজি", 30.0, 50.0, "KG_ACRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 7. GMM Pure Vermicompost Gold (Organic Fertilizer)
        Product(
            id = 7,
            slug = "gmm-pure-vermicompost-gold",
            categorySlug = "organic-manure",
            kind = "fertilizer",
            name = "GMM Pure Vermicompost Gold",
            nameBn = "জিএমএম পিওর ভার্মিকম্পোস্ট গোল্ড (কেঁচো সার)",
            shortDescription = "100% pure Eisenia fetida earthworm castings rich in natural humus & enzymes.",
            shortDescriptionBn = "প্রাকৃতিক হিউমাস, এনজাইম ও পুষ্টি সমৃদ্ধ ১০০% খাঁটি কেঁচো সার।",
            description = "Produced in shaded vermi-beds using pure cow dung and Eisenia fetida earthworms, GMM Pure Vermicompost Gold is sieved, moisture-balanced, and odor-free. Packed with plant growth hormones (auxins and cytokinins) and worm mucus that holds moisture around tender roots.",
            descriptionBn = "ছায়াযুক্ত বেডে খাঁটি গোবর ও কেঁচোর সাহায্যে তৈরি জিএমএম পিওর ভার্মিকম্পোস্ট গোল্ড। এতে রয়েছে প্রাকৃতিক বৃদ্ধি হরমোন ও হিউমাস যা চারা গাছের শিকড়কে সতেজ ও আর্দ্র রাখে।",
            composition = "100% Sieved Earthworm Castings (Organic Carbon >18%, N 1.8%, P 1.2%, K 1.5%, Beneficial Actinomycetes)",
            compositionBn = "১০০% বাছাই করা খাঁটি কেঁচো সার (জৈব কার্বন >১৮%, নাইট্রোজেন ১.৮%, ফসফরাস ১.২%, পটাশ ১.৫%)",
            benefits = listOf(
                "5x richer in available NPK than ordinary farmyard manure",
                "Safe for seedlings, nursery beds, vegetables, and fruit orchards",
                "Zero chemical residue — improves vegetable taste and shelf life"
            ),
            benefitsBn = listOf(
                "সাধারণ গোবর সারের চেয়ে ৫ গুণ বেশি সহজপাচ্য পুষ্টি সমৃদ্ধ",
                "বীজতলা, সবজি চারা এবং ফলের বাগানের জন্য সম্পূর্ণ নিরাপদ",
                "সবজির প্রাকৃতিক স্বাদ, উজ্জ্বলতা ও সংরক্ষণ ক্ষমতা বাড়ায়"
            ),
            drawableName = "prod_vermicompost",
            isFeatured = false,
            sortOrder = 7,
            variants = listOf(
                ProductVariant(701, 7, "5 kg Pack", "৫ কেজি প্যাক", "VCG-5KG", 5.0, 140.0, 0.0, "3101", 220),
                ProductVariant(702, 7, "25 kg Farm Bag", "২৫ কেজি বস্তা", "VCG-25KG", 25.0, 600.0, 0.0, "3101", 90),
            ),
            cropDoses = listOf(
                CropDose("Vegetables & Nursery", "সবজি ও বীজতলা", 40.0, 80.0, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Fruit Trees", "ফল গাছ", 1000.0, 2000.0, "G_TREE", NOTE_EN, NOTE_BN),
            )
        ),

        // 8. GMM Neem Shakti 1500 PPM (Organic Bio-Pesticide)
        Product(
            id = 8,
            slug = "gmm-neem-shakti-1500ppm",
            categorySlug = "organic-pesticides",
            kind = "pesticide",
            name = "GMM Neem Shakti 1500 PPM (Organic Bio-Pesticide)",
            nameBn = "জিএমএম নিম শক্তি ১৫০০ পিপিএম (জৈব কীটনাশক)",
            shortDescription = "Cold-pressed Azadirachtin 1500 PPM organic neem oil for aphids, whitefly & borers.",
            shortDescriptionBn = "জাব পোকা, সাদা মাছি ও মাজরা পোকা দমনে ১৫০০ পিপিএম আজাদিরাক্টিন সমৃদ্ধ জৈব নিম তেল।",
            description = "GMM Neem Shakti is a 100% botanical, cold-pressed neem kernel oil formulation standardized to 1500 PPM Azadirachtin with natural soap nut (reetha) emulsifier. It acts as a powerful antifeedant, repellent, and insect growth regulator against sucking pests and caterpillars without harming honeybees or natural predators.",
            descriptionBn = "জিএমএম নিম শক্তি হলো ১৫০০ পিপিএম আজাদিরাক্টিন সমৃদ্ধ ১০০% প্রাকৃতিক নিম তেল। এটি জাব পোকা (মাহু), সাদা মাছি, থ্রিপস ও লেদা পোকার প্রজনন ও খাদ্য গ্রহণ বন্ধ করে। মৌমাছি ও উপকারী পোকামাকড়ের কোনো ক্ষতি করে না।",
            composition = "Azadirachtin 0.15% EC (1500 PPM) min., Cold-Pressed Neem Seed Kernel Oil 90%, Natural Herbal Emulsifier q.s.",
            compositionBn = "আজাদিরাক্টিন ০.১৫% (১৫০০ পিপিএম), ঠান্ডা চাপে নিষ্কাশিত নিম বীজের তেল ৯০%, প্রাকৃতিক ইমালসিফায়ার",
            benefits = listOf(
                "Controls aphids (mahu), whiteflies, jassids, mites, and stem borers",
                "Zero waiting period — harvest vegetables safely anytime",
                "Safe for honeybees and pollinators when sprayed in late afternoon"
            ),
            benefitsBn = listOf(
                "জাব পোকা (মাহু), সাদা মাছি, শোষক পোকা ও মাজরা পোকা দমন করে",
                "কোনো বিষক্রিয়া নেই — স্প্রে করার পর সবজি নিরাপদে তোলা যায়",
                "বিকেলে স্প্রে করলে মৌমাছি ও পরাগায়নকারী পতঙ্গের কোনো ক্ষতি হয় না"
            ),
            drawableName = "prod_neem_shakti",
            isFeatured = true,
            sortOrder = 8,
            variants = listOf(
                ProductVariant(801, 8, "250 ml Bottle", "২৫০ মিলি বোতল", "NS-250ML", 0.25, 160.0, 5.0, "3808", 120),
                ProductVariant(802, 8, "500 ml Bottle", "৫০০ মিলি বোতল", "NS-500ML", 0.5, 290.0, 5.0, "3808", 85),
                ProductVariant(803, 8, "1 Litre Tin", "১ লিটার ক্যান", "NS-1L", 1.0, 540.0, 5.0, "3808", 50),
            ),
            cropDoses = listOf(
                CropDose("Brinjal, Chilli & Vegetables (Foliar)", "বেগুন, লঙ্কা ও সবজি (স্প্রে)", 3.0, 5.0, "ML_LITRE", NOTE_EN, NOTE_BN),
                CropDose("Mustard (Aphid / Mahu Control)", "সরিষা (মাহু পোকা দমন)", 3.0, 4.0, "ML_LITRE", NOTE_EN, NOTE_BN),
                CropDose("Paddy (Stem Borer & BPH)", "ধান (মাজরা ও বাদামি ফড়িং)", 4.0, 5.0, "ML_LITRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 9. GMM Tricho-Guard Bio-Fungicide (Organic Bio-Pesticide / Bio-Fungicide)
        Product(
            id = 9,
            slug = "gmm-tricho-guard-bio-fungicide",
            categorySlug = "organic-pesticides",
            kind = "pesticide",
            name = "GMM Tricho-Guard (Trichoderma Viride 1.5% WP)",
            nameBn = "জিএমএম ট্রাইকো-গার্ড (জৈব ছত্রাকনাশক)",
            shortDescription = "Organic beneficial fungus for seed treatment, root rot, wilt & blight prevention.",
            shortDescriptionBn = "বীজ শোধন, গোড়া পচা, ঢলে পড়া ও ধসা রোগ প্রতিরোধে জৈব ট্রাইকোডার্মা ছত্রাকনাশক।",
            description = "GMM Tricho-Guard contains live spores of the beneficial antagonistic fungus Trichoderma viride (1.5% WP, min 2×10^6 CFU/g). It colonizes the root zone (rhizosphere) and forms a natural biological shield against Fusarium wilt, Pythium damping-off, collar rot, and potato tuber rot.",
            descriptionBn = "জিএমএম ট্রাইকো-গার্ড হলো উপকারী ট্রাইকোডার্মা ভিরিডি (১.৫% WP) ছত্রাক। এটি গাছের শিকড়ের চারপাশে প্রাকৃতিক সুরক্ষা বলয় তৈরি করে এবং বেগুনের ঢলে পড়া রোগ, আলুর পচন ও বীজতলার গোড়া পচা রোগ নির্মূল করে।",
            composition = "Trichoderma viride 1.5% WP (2×10^6 CFU/g min.) in Sterile Organic Talc & Humic Carrier",
            compositionBn = "ট্রাইকোডার্মা ভিরিডি ১.৫% ডব্লিউপি (প্রতি গ্রামে ন্যূনতম ২×১০^৬ জীবিত স্পোর)",
            benefits = listOf(
                "Stops root rot, collar rot, and bacterial/fungal wilt in brinjal & potato",
                "Excellent for organic seed treatment before sowing paddy or potato",
                "Enriches compost when mixed with 100 kg farmyard manure"
            ),
            benefitsBn = listOf(
                "বেগুন, টমেটো ও আলুর গোড়া পচা এবং ঢলে পড়া রোগ প্রতিরোধ করে",
                "ধান ও বীজ-আলু বপনের আগে জৈব বীজ শোধনের সেরা উপাদান",
                "গোবর সারের সাথে মিশিয়ে জমিতে দিলে মাটিবাহিত ছত্রাক ধ্বংস হয়"
            ),
            drawableName = "prod_tricho_guard",
            isFeatured = false,
            sortOrder = 9,
            variants = listOf(
                ProductVariant(901, 9, "500 g Pack", "৫০০ গ্রাম প্যাক", "TG-500G", 0.5, 150.0, 5.0, "3808", 95),
                ProductVariant(902, 9, "1 kg Pack", "১ কেজি প্যাক", "TG-1KG", 1.0, 270.0, 5.0, "3808", 75),
            ),
            cropDoses = listOf(
                CropDose("Potato & Paddy Soil Treatment", "আলু ও ধানের জমি শোধন", 2.0, 2.5, "KG_ACRE", NOTE_EN, NOTE_BN),
                CropDose("Brinjal & Vegetable Root Drench", "বেগুন ও সবজির গোড়ায় প্রয়োগ", 5.0, 8.0, "ML_LITRE", NOTE_EN, NOTE_BN),
            )
        ),

        // 10. GMM Beauveria & Panchagavya Bio-Pest Shield (Organic Bio-Pesticide)
        Product(
            id = 10,
            slug = "gmm-beauveria-bio-pest-shield",
            categorySlug = "organic-pesticides",
            kind = "pesticide",
            name = "GMM Beauveria Bio-Pest Shield (Liquid)",
            nameBn = "জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড (জৈব কীটনাশক)",
            shortDescription = "Entomopathogenic organic bio-pesticide for fruit borer, thrips, mealybugs & mites.",
            shortDescriptionBn = "ফল ও ডগা ছিদ্রকারী পোকা, থ্রিপস, দোয়রা পোকা ও মাকড় দমনে তরল জৈব কীটনাশক।",
            description = "GMM Beauveria Bio-Pest Shield is an advanced liquid biological insecticide containing Beauveria bassiana spores suspended in organic herbal oil. When sprayed on crops, the natural fungal spores attach to soft-bodied insects and caterpillars, neutralizing brinjal shoot & fruit borers, paddy leaf folders, and jute hairy caterpillars without toxic chemical residues.",
            descriptionBn = "জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড হলো বিউভেরিয়া ব্যাসিয়ানা সমৃদ্ধ তরল জৈব কীটনাশক। বেগুনের ডগা ও ফল ছিদ্রকারী পোকা, ধানের পাতা মোড়ানো পোকা, পাটের বিছা পোকা এবং থ্রিপস দমনে এটি অত্যন্ত কার্যকর ও সম্পূর্ণ বিষমুক্ত।",
            composition = "Beauveria bassiana 1.15% SC (1×10^8 CFU/ml min.) + Herbal Karanja & Tulsi Adjuvant",
            compositionBn = "বিউভেরিয়া ব্যাসিয়ানা ১.১৫% এসসি (১×১০^৮ CFU/মিলি) + করঞ্জ ও তুলসী ভেষজ নির্যাস",
            benefits = listOf(
                "Targets brinjal shoot/fruit borer, paddy leaf folder, and jute caterpillars",
                "Insects cannot develop resistance unlike synthetic chemical pesticides",
                "100% organic, residue-free, and safe for farm workers"
            ),
            benefitsBn = listOf(
                "বেগুনের ডগা-ফল ছিদ্রকারী পোকা, ধানের পাতা মোড়ানো ও পাটের বিছা পোকা দমন করে",
                "রাসায়নিক ওষুধের মতো পোকার দেহে প্রতিরোধ ক্ষমতা তৈরি হয় না",
                "১০০% জৈব, বিষমুক্ত এবং চাষি ভাইদের স্বাস্থ্যের জন্য সম্পূর্ণ নিরাপদ"
            ),
            drawableName = "prod_beauveria",
            isFeatured = true,
            sortOrder = 10,
            variants = listOf(
                ProductVariant(1001, 10, "250 ml Bottle", "২৫০ মিলি বোতল", "BB-250ML", 0.25, 190.0, 5.0, "3808", 80),
                ProductVariant(1002, 10, "500 ml Bottle", "৫০০ মিলি বোতল", "BB-500ML", 0.5, 350.0, 5.0, "3808", 60),
            ),
            cropDoses = listOf(
                CropDose("Brinjal & Vegetable Borers", "বেগুন ও সবজির ছিদ্রকারী পোকা", 4.0, 5.0, "ML_LITRE", NOTE_EN, NOTE_BN),
                CropDose("Paddy & Jute Caterpillars", "ধান ও পাটের পাতা খেকো পোকা", 4.0, 5.0, "ML_LITRE", NOTE_EN, NOTE_BN),
            )
        ),
    )

    fun payload(): CatalogPayload = CatalogPayload(
        config = SITE_CONFIG,
        categories = CATEGORIES,
        products = PRODUCTS,
        pins = PINS,
    )

    // ---------- Krishi Paramarsha (100% Organic Crop Advisory Data from GMM) ----------

    data class CropGuide(
        val slug: String,
        val nameBn: String,
        val nameEn: String,
        val seasonBn: String,
        val seasonEn: String,
        val kind: String, // "kharif", "rabi", "veg"
        val drawableName: String,
        val taglineBn: String,
        val taglineEn: String,
        val durationBn: String,
        val durationEn: String,
        val soilBn: String,
        val soilEn: String,
        val seedBn: String,
        val seedEn: String,
        val sowMonths: List<Int>,
        val growMonths: List<Int>,
        val harvestMonths: List<Int>,
        val varietiesBn: List<String>,
        val varietiesEn: List<String>,
        val organicFertPlanBn: String,
        val organicFertPlanEn: String,
        val stages: List<Pair<String, String>>,
        val stagesEn: List<Pair<String, String>>,
        val organicPests: List<Triple<String, String, String>>,
        val organicPestsEn: List<Triple<String, String, String>>,
        val recommendedProductSlugs: List<String>,
    )

    val DAILY_ORGANIC_TIPS_BN = listOf(
        "সার দেওয়ার আগে মাটি পরীক্ষা করুন — রাসায়নিকের বদলে জৈব সার মাটির জল ধরে রাখার ক্ষমতা ও কেঁচোর সংখ্যা বাড়ায়।",
        "বীজ বপনের আগে জিএমএম ট্রাইকো-গার্ড (ট্রাইকোডার্মা) দিয়ে জৈব বীজ শোধন করলে গোড়া পচা রোগ গোড়াতেই আটকানো যায়।",
        "সকাল বা বিকেলে জমি ঘুরে দেখুন; পোকার আক্রমণ শুরুতে ধরলে নিম শক্তি (১৫০০ পিপিএম) স্প্রে করেই নিয়ন্ত্রণ করা সহজ।",
        "বেগুন বা সবজিতে ফেরোমন ফাঁদ ও হলুদ আঠালো ফাঁদ ব্যবহার করুন এবং বিকেলে বিউভেরিয়া জৈব কীটনাশক স্প্রে করুন।",
        "ফুল ফোটার সময় দুপুরে কখনো স্প্রে করবেন না — বিকেলে জৈব নিম তেল স্প্রে করলে মৌমাছি ও পরাগায়ন সুরক্ষিত থাকে।",
    )

    val DAILY_ORGANIC_TIPS_EN = listOf(
        "Test your soil before fertilizing — replacing chemicals with organic manure boosts water retention and earthworm populations.",
        "Treat seeds with GMM Tricho-Guard (Trichoderma viride) before sowing to stop root rot and wilt before they start.",
        "Scout fields morning and evening; early aphid or whitefly attacks are easily controlled with Neem Shakti 1500 PPM.",
        "Use pheromone and yellow sticky traps in vegetables along with evening sprays of GMM Beauveria Bio-Pest Shield.",
        "Never spray during midday bloom — spray organic neem oil in late afternoon to protect honeybees and natural pollinators.",
    )

    val CROP_GUIDES = listOf(
        CropGuide(
            slug = "paddy",
            nameBn = "ধান",
            nameEn = "Paddy (Rice)",
            seasonBn = "খরিফ (আমন) ও বোরো",
            seasonEn = "Kharif (Aman) & Boro",
            kind = "kharif",
            drawableName = "crop_paddy",
            taglineBn = "আমন ও বোরো — জৈব পদ্ধতিতে দুই মরসুমেই সুস্থ মাটি ও ভালো ফলন",
            taglineEn = "Aman & Boro — healthy soil and heavy panicles through 100% organic nutrition",
            durationBn = "১২০–১৪৫ দিন",
            durationEn = "120–145 days",
            soilBn = "এঁটেল / দোআঁশ, জল ধরে রাখে এমন",
            soilEn = "Clay / loamy soil with good water retention",
            seedBn = "২০–৩০ কেজি/হেক্টর (রোপণে)",
            seedEn = "20–30 kg/hectare (transplanting)",
            sowMonths = listOf(6, 7, 12),
            growMonths = listOf(8, 9, 10, 1, 2, 3),
            harvestMonths = listOf(11, 4, 5),
            varietiesBn = listOf("স্বর্ণা / স্বর্ণা সাব-১ (বন্যা সহনশীল)", "এমটিইউ-১০১০", "আইআর-৬৪ (বোরো)", "শতাব্দী (আউশ)"),
            varietiesEn = listOf("Swarna / Swarna Sub-1 (flood tolerant)", "MTU-1010", "IR-64 (Boro)", "Shatabdi (Aus)"),
            organicFertPlanBn = "জমি তৈরির সময় একর প্রতি ৫০–১০০ কেজি জৈব সার মিশ্রণ ও ২ কেজি ট্রাইকো-গার্ড দিন। রোপণের ১৫–২০ দিনে নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড (৮–১০ কেজি/একর) এবং কুশি অবস্থায় মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার (৫ কেজি/একর) প্রয়োগ করুন।",
            organicFertPlanEn = "Apply 50–100 kg/acre Organic Manure Mixture + 2 kg Tricho-Guard at final land preparation. At 15–20 DAT apply Nirmala Agri Vita Green Gold (8–10 kg/acre) and at tillering apply Micro Max Jackpot Soil Booster (5 kg/acre).",
            stages = listOf(
                "জৈব বীজতলা ও বীজ শোধন" to "প্রতি কেজি বীজে ৫ গ্রাম ট্রাইকো-গার্ড মিশিয়ে বীজ শোধন করুন। বীজতলায় কেঁচো সার দিন; ২১–২৫ দিনের চারা রোপণ করুন।",
                "জমি তৈরি ও রোপণ" to "কাদা করার সময় জৈব সার মিশ্রণ ও বায়ো অর্গানিক সার মেশান। সারি ২০ সেমি, গোছা ১৫ সেমি দূরত্বে রোপণ করুন।",
                "কুশি ও শিকড় বৃদ্ধি" to "রোপণের ১৫–২০ দিনে নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড দানা ছিটিয়ে দিন; এতে সাদা শিকড় ও কুশির সংখ্যা বাড়ে।",
                "জৈব রোগ-পোকা নিয়ন্ত্রণ" to "মাজরা পোকা ও পাতা মোড়ানো পোকা দেখা দিলে একর প্রতি আলোর ফাঁদ ও নিম শক্তি ১৫০০ পিপিএম (৪ মিলি/লিটার জলে) স্প্রে করুন।",
                "কাটা ও মাড়াই" to "৮০–৮৫% দানা সোনালি হলে কাটুন এবং ১৪% আর্দ্রতায় শুকিয়ে সংরক্ষণ করুন।"
            ),
            stagesEn = listOf(
                "Organic Seed Treatment & Nursery" to "Treat seeds with 5g/kg GMM Tricho-Guard. Enrich nursery bed with Pure Vermicompost; transplant at 21–25 days.",
                "Puddling & Transplanting" to "Incorporate Organic Manure Mixture & Bio Organic Manure during puddling. Keep 20 cm row x 15 cm hill spacing.",
                "Tillering & White Root Boost" to "Broadcast Nirmala Agri Vita Green Gold at 15–20 DAT to multiply white roots and productive tillers.",
                "Organic Pest Management" to "Install light/pheromone traps and spray GMM Neem Shakti 1500 PPM (4 ml/L) + Beauveria against stem borer.",
                "Harvest & Storage" to "Harvest when 80–85% grains turn golden and dry to 14% moisture."
            ),
            organicPests = listOf(
                Triple("মাজরা পোকা (Stem Borer)", "গাছের মাঝের পাতা শুকিয়ে যায় (মরা ডিগ), শীষ সাদা হয়।", "ফেরোমন ফাঁদ লাগান এবং জিএমএম নিম শক্তি ১৫০০ পিপিএম (৪ মিলি/লিটার) ও বিউভেরিয়া স্প্রে করুন।"),
                Triple("ব্লাস্ট ও গোড়া পচা রোগ", "পাতায় চোখের মতো দাগ ও গোড়া পচে যায়।", "জমি তৈরির সময় ট্রাইকো-গার্ড ব্যবহার করুন এবং অতিরিক্ত রাসায়নিক ইউরিয়া সম্পূর্ণ বর্জন করুন।"),
                Triple("বাদামি গাছ ফড়িং (BPH)", "গোড়ায় ফড়িং জমে গাছ শুকিয়ে যায়।", "সারির মাঝে বাতাস চলাচলের ফাঁক রাখুন এবং গাছের গোড়ায় নিম তেল স্প্রে করুন।")
            ),
            organicPestsEn = listOf(
                Triple("Stem Borer", "Central shoot dries up (dead heart) and white earheads appear.", "Use pheromone traps and spray GMM Neem Shakti 1500 PPM (4 ml/L) + Beauveria Bio-Pest Shield."),
                Triple("Blast & Sheath Rot", "Spindle-shaped spots on leaves and rotting at base.", "Eliminate chemical urea; apply GMM Tricho-Guard (Trichoderma viride) in soil and foliar drench."),
                Triple("Brown Plant Hopper (BPH)", "Hoppers suck sap at plant base causing hopper burn.", "Maintain alleyways for airflow and direct Neem Shakti 1500 PPM spray toward the plant base.")
            ),
            recommendedProductSlugs = listOf("nirmala-agri-vita-green-gold", "micro-max-jackpot-soil-booster", "gmm-neem-shakti-1500ppm", "gmm-tricho-guard-bio-fungicide")
        ),

        CropGuide(
            slug = "potato",
            nameBn = "আলু",
            nameEn = "Potato",
            seasonBn = "রবি (শীতকালীন)",
            seasonEn = "Rabi (Winter)",
            kind = "rabi",
            drawableName = "crop_potato",
            taglineBn = "হুগলির প্রধান অর্থকরী ফসল — জৈব সয়েল বুস্টারে নিটোল ও বড় আলুর ফলন",
            taglineEn = "Hooghly's flagship cash crop — uniform,blemish-free tubers with organic soil boosters",
            durationBn = "৯০–১১০ দিন",
            durationEn = "90–110 days",
            soilBn = "ভালো নিকাশি বেলে-দোআঁশ বা দোআঁশ",
            soilEn = "Well-drained sandy loam to loamy soil",
            seedBn = "২৫–৩০ কুইন্টাল বীজ-আলু/হেক্টর",
            seedEn = "25–30 quintals seed tubers/hectare",
            sowMonths = listOf(10, 11),
            growMonths = listOf(12, 1),
            harvestMonths = listOf(1, 2, 3),
            varietiesBn = listOf("কুফরি জ্যোতি", "কুফরি পোখরাজ", "কুফরি চন্দ্রমুখী", "কুফরি চিপসোনা-১"),
            varietiesEn = listOf("Kufri Jyoti", "Kufri Pukhraj", "Kufri Chandramukhi", "Kufri Chipsona-1"),
            organicFertPlanBn = "বপনের সময় একর প্রতি ৫০ কেজি বায়ো অর্গানিক সার, ৫ কেজি প্রিমিয়াম জ্যাকপট সয়েল বুস্টার ও ২ কেজি ট্রাইকো-গার্ড দিন। মাটি তোলার সময় (২৫–৩০ দিনে) মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার (৫ কেজি/একর) ও জ্যাকপট ধামাকা প্রয়োগ করুন।",
            organicFertPlanEn = "At planting apply 50 kg Bio Organic Manure, 5 kg Premium Jackpot Soil Booster, and 2 kg Tricho-Guard per acre. During earthing up (25–30 DAP), top-dress Micro Max Jackpot Soil Booster (5 kg/acre) & Jackpot Dhamaka.",
            stages = listOf(
                "জমি তৈরি ও বীজ-আলু শোধন" to "জমি ঝুরঝুরে করে জৈব সার মিশ্রণ মেশান। কাটা বা গোটা বীজ-আলু ট্রাইকো-গার্ড জলে শোধন করে ছায়ায় শুকিয়ে বুনুন।",
                "বপন ও প্রারম্ভিক পুষ্টি" to "অক্টোবরের শেষ থেকে নভেম্বরের মাঝামাঝি বপন করুন। সারিতে প্রিমিয়াম জ্যাকপট সয়েল বুস্টার দিন।",
                "মাটি তোলা (Earthing Up)" to "বপনের ২৫–৩০ দিনে মাইক্রো ম্যাক্স জ্যাকপট সয়েল বুস্টার দিয়ে সারিতে মাটি তুলে দিন; এতে আলু সবুজ হয় না ও আকার বড় হয়।",
                "ধসা ও জাব পোকা রোধ" to "কুয়াশা পড়লে ট্রাইকো-গার্ড স্প্রে করুন এবং জাব পোকা এড়াতে নিম শক্তি ১৫০০ পিপিএম (৩ মিলি/লিটার) স্প্রে করুন।",
                "খোঁড়া ও সংরক্ষণ" to "খোঁড়ার ১২ দিন আগে সেচ বন্ধ করুন। জৈব আলুর খোসা শক্ত ও চকচকে হয়, হিমঘরে দীর্ঘদিন ভালো থাকে।"
            ),
            stagesEn = listOf(
                "Soil Prep & Tuber Treatment" to "Plough soil fine with Organic Manure Mixture. Treat seed tubers with GMM Tricho-Guard slurry and shade-dry.",
                "Planting & Basal Boost" to "Plant late Oct to mid-Nov (60x20 cm). Apply Premium Jackpot Soil Booster along furrows.",
                "Earthing Up (25–30 DAP)" to "Apply Micro Max Jackpot Soil Booster and earth up ridges so tubers expand rapidly without greening.",
                "Blight & Aphid Shield" to "Use Tricho-Guard preventively in foggy weather and spray Neem Shakti 1500 PPM (3 ml/L) against aphids.",
                "Harvest & Cold Storage" to "Stop irrigation 12 days before harvest. Organically grown potatoes have firmer skin and superior storage life."
            ),
            organicPests = listOf(
                Triple("নাবি ও আর্লি ধসা (Blight) ও কন্দ পচা", "ঠান্ডা কুয়াশায় পাতায় বাদামি দাগ ও আলু পচন।", "বীজ শোধন ও মাটিতে জিএমএম ট্রাইকো-গার্ড ব্যবহার করুন; জমিতে জল জমতে দেবেন না।"),
                Triple("জাব পোকা (Aphids)", "পাতার নিচে ছোট সবুজ পোকা রস চুষে খায় ও ভাইরাস ছড়ায়।", "জিএমএম নিম শক্তি ১৫০০ পিপিএম (৩–৪ মিলি/লিটার জলে) বিকেলে স্প্রে করুন।"),
                Triple("মাটির কাটুই পোকা (Cutworm)", "রাতের বেলা চারা গাছের গোড়া কেটে দেয়।", "জমি তৈরির সময় নিম খোল সমৃদ্ধ জ্যাকপট ধামাকা ও জৈব সার মিশ্রণ ব্যবহার করুন।")
            ),
            organicPestsEn = listOf(
                Triple("Tuber Rot & Blight", "Dark lesions in foggy weather and rotting tubers.", "Treat seed and soil with GMM Tricho-Guard and avoid waterlogging."),
                Triple("Aphids & Whiteflies", "Sap-sucking insects under leaves that spread viral curl.", "Spray GMM Neem Shakti 1500 PPM (3–4 ml/L) + yellow sticky traps."),
                Triple("Soil Cutworms & Grubs", "Larvae cut young stems and bore into tubers.", "Apply neem-cake enriched Jackpot Dhamaka & Beauveria Bio-Pest Shield.")
            ),
            recommendedProductSlugs = listOf("premium-jackpot-soil-booster", "micro-max-jackpot-soil-booster", "jackpot-dhamaka-bio-granules", "gmm-tricho-guard-bio-fungicide")
        ),

        CropGuide(
            slug = "brinjal",
            nameBn = "বেগুন ও সবজি",
            nameEn = "Brinjal & Vegetables",
            seasonBn = "সারা বছর (শীতে সেরা)",
            seasonEn = "Year-round (Best in Winter)",
            kind = "veg",
            drawableName = "crop_brinjal",
            taglineBn = "বিষমুক্ত সতেজ সবজি — জৈব কীটনাশকে ডগা ও ফল ছিদ্রকারী পোকা থেকে মুক্তি",
            taglineEn = "Residue-free fresh vegetables — beat shoot & fruit borer with biological pest shield",
            durationBn = "১২০–১৫০ দিন (কয়েক মাস ফলন)",
            durationEn = "120–150 days (multi-month picking)",
            soilBn = "উর্বর দোআঁশ, জৈব পদার্থ-সমৃদ্ধ",
            soilEn = "Fertile loamy soil rich in organic humus",
            seedBn = "৪০০–৫০০ গ্রাম/হেক্টর",
            seedEn = "400–500 g/hectare",
            sowMonths = listOf(9, 10),
            growMonths = listOf(11, 12),
            harvestMonths = listOf(1, 2, 3, 4),
            varietiesBn = listOf("পুসা পার্পল লং", "পুসা ক্লাস্টার", "মুক্তকেশী ও স্থানীয় উন্নত জাত"),
            varietiesEn = listOf("Pusa Purple Long", "Pusa Cluster", "Muktakeshi & local hybrids"),
            organicFertPlanBn = "রোপণের গর্তে খাঁটি কেঁচো সার ও ট্রাইকো-গার্ড দিন। প্রতি ২০ দিন অন্তর প্রিমিয়াম জ্যাকপট সয়েল বুস্টার ও নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড ঘুরিয়ে ফিরিয়ে প্রয়োগ করলে টানা ৪–৫ মাস চকচকে বেগুন পাওয়া যায়।",
            organicFertPlanEn = "Mix GMM Pure Vermicompost + Tricho-Guard in transplant pits. Side-dress Premium Jackpot Soil Booster and Nirmala Agri Vita Green Gold every 20 days for glossy, continuous pickings.",
            stages = listOf(
                "উঁচু বীজতলা ও চারা শোধন" to "কেঁচো সার ও ট্রাইকো-গার্ড মিশিয়ে উঁচু বীজতলা করুন। চারার শিকড় ট্রাইকো-গার্ড জলে ডুবিয়ে বিকেলে রোপণ করুন।",
                "বৃদ্ধি ও ফুল আসা" to "রোপণের ২০ ও ৪০ দিনে প্রিমিয়াম জ্যাকপট সয়েল বুস্টার (৪–৬ কেজি/একর) ও নির্মলা গ্রিন গোল্ড দিন।",
                "ডগা ও ফল ছিদ্রকারী পোকা দমন" to "একর প্রতি ফেরোমন ফাঁদ লাগান এবং ১০ দিন অন্তর জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড (৪ মিলি/লিটার) ও নিম শক্তি স্প্রে করুন।",
                "বিষমুক্ত ফল তোলা" to "জৈব কীটনাশক ব্যবহারের ফলে কোনো বিষক্রিয়া থাকে না — ফল নরম ও চকচকে থাকতে নিয়মিত তুলুন।"
            ),
            stagesEn = listOf(
                "Raised Nursery & Root Dip" to "Prepare raised beds with Pure Vermicompost. Dip seedling roots in Tricho-Guard solution before evening transplanting.",
                "Vegetative Growth & Flowering" to "Apply Premium Jackpot Soil Booster and Nirmala Agri Vita Green Gold at 20 and 40 DAT.",
                "Bio-Control of Shoot & Fruit Borer" to "Install pheromone traps and alternate sprays of GMM Beauveria Bio-Pest Shield (4 ml/L) and Neem Shakti 1500 PPM every 10 days.",
                "Zero-Residue Picking" to "Zero chemical waiting period — pick tender, glossy vegetables safely for market."
            ),
            organicPests = listOf(
                Triple("ডগা ও ফল ছিদ্রকারী পোকা", "ডগা ঢলে পড়ে এবং বেগুনে ছিদ্র দেখা যায়।", "আক্রান্ত ডগা কেটে ফেলুন, ফেরোমন ফাঁদ লাগান এবং জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড (৪–৫ মিলি/লিটার) স্প্রে করুন।"),
                Triple("ঢলে পড়া রোগ (Wilt)", "গাছ হঠাৎ ঢলে পড়ে মারা যায়।", "চারা রোপণের সময় ও মাসে একবার ট্রাইকো-গার্ড (৫ গ্রাম/লিটার জলে) গাছের গোড়ায় ঢালুন।"),
                Triple("সাদা মাছি ও শোষক পোকা", "পাতা কুঁকড়ে হলদে হয়ে যায়।", "হলুদ আঠালো ফাঁদ ও জিএমএম নিম শক্তি ১৫০০ পিপিএম (৪ মিলি/লিটার) স্প্রে করুন।")
            ),
            organicPestsEn = listOf(
                Triple("Shoot & Fruit Borer", "Drooping shoots and bored holes in fruits.", "Prune affected shoots, install pheromone traps, and spray GMM Beauveria Bio-Pest Shield (4–5 ml/L)."),
                Triple("Fusarium / Bacterial Wilt", "Sudden wilting of whole plant.", "Drench root zone with GMM Tricho-Guard (5 g/L) at planting and monthly."),
                Triple("Whitefly & Jassids", "Upward leaf curling and yellowing.", "Use yellow sticky traps and spray GMM Neem Shakti 1500 PPM (4 ml/L).")
            ),
            recommendedProductSlugs = listOf("gmm-beauveria-bio-pest-shield", "gmm-neem-shakti-1500ppm", "gmm-tricho-guard-bio-fungicide", "premium-jackpot-soil-booster")
        ),

        CropGuide(
            slug = "mustard",
            nameBn = "সরিষা",
            nameEn = "Mustard",
            seasonBn = "রবি (শীতকালীন)",
            seasonEn = "Rabi (Winter)",
            kind = "rabi",
            drawableName = "crop_mustard",
            taglineBn = "মৌমাছি বাঁচিয়ে মাহু পোকা দমন — জৈব পুষ্টিতে দানায় ভরপুর তেল",
            taglineEn = "Pollinator-safe aphid control — higher oil content with organic humic & neem care",
            durationBn = "৯০–১১০ দিন",
            durationEn = "90–110 days",
            soilBn = "দোআঁশ, ভালো নিকাশি",
            soilEn = "Well-drained loamy soil",
            seedBn = "৫–৬ কেজি/হেক্টর",
            seedEn = "5–6 kg/hectare",
            sowMonths = listOf(10, 11),
            growMonths = listOf(12, 1),
            harvestMonths = listOf(1, 2),
            varietiesBn = listOf("বিনয় (বি-৯)", "অগ্রণী", "টোরি-৭"),
            varietiesEn = listOf("Binoy (B-9)", "Agrani", "Tori-7"),
            organicFertPlanBn = "বপনের সময় একর প্রতি ৩০ কেজি বায়ো অর্গানিক সার ও ৫ কেজি জ্যাকপট ধামাকা জৈব দানা দিন। প্রথম সেচের সময় (৩০ দিনে) নির্মলা অ্যাগ্রি ভিটা গ্রিন গোল্ড দিলে ফুল ও শুঁটির সংখ্যা বাড়ে।",
            organicFertPlanEn = "At sowing apply 30 kg Bio Organic Manure + 5 kg Jackpot Dhamaka Organic Granules per acre. Top-dress Nirmala Agri Vita Green Gold at first irrigation (30 DAS).",
            stages = listOf(
                "জমি তৈরি ও বপন" to "অক্টোবরের শেষ থেকে নভেম্বরের মাঝামাঝি লাইনে বুনুন। ট্রাইকো-গার্ড দিয়ে বীজ শোধন করুন।",
                "পাতলা করা ও প্রথম সেচ" to "১৫–২০ দিনে চারা পাতলা করুন। ৩০ দিনে হালকা সেচ ও জ্যাকপট ধামাকা দানা দিন।",
                "মৌমাছি-বান্ধব মাহু দমন" to "জানুয়ারিতে মাহু (জাব) পোকা দেখা দিলে বিকেলে (৪টার পর) জিএমএম নিম শক্তি ১৫০০ পিপিএম স্প্রে করুন — এতে মৌমাছি নিরাপদ থাকে।",
                "কাটা ও মাড়াই" to "৭৫% শুঁটি হলদে হলে সকালে কেটে রোদে শুকিয়ে মাড়াই করুন।"
            ),
            stagesEn = listOf(
                "Land Prep & Sowing" to "Sow in lines late Oct to mid-Nov after treating seed with GMM Tricho-Guard.",
                "Thinning & First Irrigation" to "Thin plants at 15–20 DAS. Irrigate lightly at 30 DAS with Jackpot Dhamaka Organic Granules.",
                "Bee-Safe Aphid (Mahu) Control" to "Spray GMM Neem Shakti 1500 PPM only after 4 PM when honeybees return to hives.",
                "Harvesting" to "Harvest in the morning when 75% pods turn golden yellow."
            ),
            organicPests = listOf(
                Triple("মাহু বা জাব পোকা (Mustard Aphid)", "ফুল ও শুঁটির ডগায় ঝাঁকে ঝাঁকে সবুজ পোকা।", "বিকেল ৪টার পর জিএমএম নিম শক্তি ১৫০০ পিপিএম (৩–৪ মিলি/লিটার) স্প্রে করুন; মৌমাছি রক্ষা পাবে ও মাহু দমন হবে।"),
                Triple("সাদা মরিচা ও পাতা দাগ", "পাতার নিচে সাদা ফোসকা বা কালচে দাগ।", "ট্রাইকো-গার্ড দিয়ে বীজ শোধন করুন এবং বেশি ঘন করে বুনবেন না।")
            ),
            organicPestsEn = listOf(
                Triple("Mustard Aphid (Mahu)", "Clusters of green aphids on flowering shoots and pods.", "Spray GMM Neem Shakti 1500 PPM (3–4 ml/L) after 4 PM to protect honeybees while controlling aphids."),
                Triple("White Rust & Alternaria Blight", "White pustules under leaves and dark pod spots.", "Treat seed with GMM Tricho-Guard and maintain proper plant spacing.")
            ),
            recommendedProductSlugs = listOf("jackpot-dhamaka-bio-granules", "gmm-neem-shakti-1500ppm", "bio-organic-manure")
        ),

        CropGuide(
            slug = "jute",
            nameBn = "পাট",
            nameEn = "Jute",
            seasonBn = "গ্রীষ্ম-খরিফ",
            seasonEn = "Summer / Pre-Kharif",
            kind = "kharif",
            drawableName = "crop_jute",
            taglineBn = "সোনালি আঁশ — জৈব পুষ্টিতে লম্বা গাছ ও উজ্জ্বল মজবুত আঁশ",
            taglineEn = "Golden fiber — taller stalks and lustrous fiber with organic bio-nutrition",
            durationBn = "১০০–১২০ দিন",
            durationEn = "100–120 days",
            soilBn = "উর্বর দোআঁশ বা বেলে-দোআঁশ",
            soilEn = "Fertile alluvial or sandy-loam soil",
            seedBn = "৪–৬ কেজি/হেক্টর",
            seedEn = "4–6 kg/hectare",
            sowMonths = listOf(3, 4, 5),
            growMonths = listOf(6, 7),
            harvestMonths = listOf(7, 8),
            varietiesBn = listOf("জেআরও-৫২৪ (নবীন)", "জেআরও-২০৪", "জেআরসি-৬৯৮"),
            varietiesEn = listOf("JRO-524 (Navin)", "JRO-204", "JRC-698"),
            organicFertPlanBn = "জমি তৈরির সময় জৈব সার মিশ্রণ ও ট্রাইকো-গার্ড দিন। বপনের ২৫–৩০ দিনে জ্যাকপট ধামাকা জৈব দানা (৫–৮ কেজি/একর) দিলে গাছ দ্রুত লম্বা হয় ও আঁশ মোটা হয়।",
            organicFertPlanEn = "Apply Organic Manure Mixture + Tricho-Guard at land prep. Top-dress Jackpot Dhamaka Organic Granules (5–8 kg/acre) at 25–30 DAS for tall, thick-barked stalks.",
            stages = listOf(
                "মিহি জমি তৈরি ও বীজ শোধন" to "মাটি মিহি করে জৈব সার মেশান। কাণ্ড পচা রোধে ট্রাইকো-গার্ড দিয়ে বীজ শোধন করুন।",
                "পাতলা করা ও বৃদ্ধি" to "১৫–২০ দিনে আগাছা পরিষ্কার করে ২৫ দিনে জ্যাকপট ধামাকা দানা ছিটিয়ে দিন।",
                "বিছা পোকা ও হলুদ মাইট দমন" to "বিছা পোকা ও মাইট দমনে জিএমএম বিউভেরিয়া ও নিম শক্তি ১৫০০ পিপিএম স্প্রে করুন।",
                "কাটা ও পচানো" to "১০০–১২০ দিনে কেটে পরিষ্কার ধীর জলে পচান; জৈব পাটের আঁশ উজ্জ্বল সোনালি হয়।"
            ),
            stagesEn = listOf(
                "Fine Seedbed & Seed Treatment" to "Prepare fine tilth with Organic Manure Mixture and treat seeds with Tricho-Guard to prevent stem rot.",
                "Thinning & Vegetative Boost" to "Thin plants at 15–20 DAS and broadcast Jackpot Dhamaka Organic Granules at 25 DAS.",
                "Bihar Hairy Caterpillar & Mite Control" to "Spray GMM Beauveria Bio-Pest Shield + Neem Shakti 1500 PPM at first sign of leaf feeding.",
                "Harvest & Retting" to "Harvest at 100–120 days and ret in clean, slow-moving water for golden, high-grade fiber."
            ),
            organicPests = listOf(
                Triple("বিছা পোকা (Hairy Caterpillar)", "পাতা খেয়ে ঝাঁঝরা করে দেয়।", "ডিমের গুচ্ছ নষ্ট করুন এবং জিএমএম বিউভেরিয়া বায়ো-পেস্ট শিল্ড (৪ মিলি/লিটার) স্প্রে করুন।"),
                Triple("হলুদ মাইট (Yellow Mite)", "ডগার পাতা কুঁকড়ে তামাটে হয়ে যায়।", "জিএমএম নিম শক্তি ১৫০০ পিপিএম (৪ মিলি/লিটার জলে) স্প্রে করুন।"),
                Triple("কাণ্ড ও গোড়া পচা (Stem Rot)", "গোড়া কালচে হয়ে গাছ মারা যায়।", "ট্রাইকো-গার্ড দিয়ে বীজ ও মাটি শোধন করুন এবং জল নিকাশি ঠিক রাখুন।")
            ),
            organicPestsEn = listOf(
                Triple("Bihar Hairy Caterpillar", "Defoliates leaves rapidly in clusters.", "Remove egg masses and spray GMM Beauveria Bio-Pest Shield (4 ml/L)."),
                Triple("Yellow Mite", "Terminal leaves curl downward and turn bronze.", "Spray GMM Neem Shakti 1500 PPM (4 ml/L) on upper canopy."),
                Triple("Stem & Root Rot", "Dark collar lesions causing plant death.", "Treat seed and soil with GMM Tricho-Guard and ensure drainage.")
            ),
            recommendedProductSlugs = listOf("jackpot-dhamaka-bio-granules", "gmm-beauveria-bio-pest-shield", "gmm-tricho-guard-bio-fungicide")
        ),
    )
}
