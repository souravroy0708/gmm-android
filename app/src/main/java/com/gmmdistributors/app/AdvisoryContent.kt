package com.gmmdistributors.app

/**
 * Krishi Paramarsha (crop advisory) content bundled with the app: 5 crop guides, a 12-month
 * calendar and daily organic tips. Products, prices, stock and PIN codes are NOT here any more;
 * they come live from the Django backend (`/api/v1/catalog/`).
 */
object AdvisoryContent {

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
