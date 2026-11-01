package com.gmmdistributors.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * English + Bengali (`bn`) reactive translation dictionary.
 * Follows the Bas1Minute `t("English")` pattern so any screen string automatically updates
 * when the farmer toggles `বাংলা` / `English` in the top bar or Profile tab.
 */
object Lang {
    var current by mutableStateOf("en")

    private val BN = mapOf(
        // Navigation
        "Shop" to "দোকান",
        "Advisory" to "পরামর্শ",
        "Cart" to "কার্ট",
        "Orders" to "অর্ডার",
        "Profile" to "প্রোফাইল",

        // Header & Hero
        "Organic farming, healthy living" to "জৈব চাষাবাদ, সুস্থ জীবন",
        "100% Organic · Zero Chemicals" to "১০০% জৈব · রাসায়নিকমুক্ত",
        "Organic farming, a promise of good health" to "জৈব চাষাবাদ, সুস্বাস্থ্যের অঙ্গীকার",
        "100% Organic Fertilizers & Bio-Pesticides from GMM Distributors, delivered to your door." to
            "জিএমএম ডিস্ট্রিবিউটরস-এর ১০০% জৈব সার ও জৈব কীটনাশক, সরাসরি আপনার দুয়ারে।",
        "No Payment Gateway · Order on WhatsApp" to "কোনো পেমেন্ট গেটওয়ে নেই · হোয়াটসঅ্যাপে অর্ডার করুন",
        "All Products" to "সব পণ্য",
        "Fertilizers" to "জৈব সার",
        "Bio-Pesticides" to "জৈব কীটনাশক",
        "Search organic fertilizer or bio-pesticide…" to "জৈব সার বা কীটনাশক খুঁজুন…",

        // Trust badges
        "100% Organic" to "১০০% জৈব",
        "No synthetic chemicals" to "কোনো রাসায়নিক নেই",
        "WhatsApp Order" to "হোয়াটসঅ্যাপ অর্ডার",
        "Direct to owner" to "সরাসরি মালিকের কাছে",
        "Manual UPI / Bank" to "ম্যানুয়াল UPI / ব্যাংক",
        "Pay offline safely" to "নিরাপদ অফলাইন পেমেন্ট",
        "GST Invoice" to "GST বিল",
        "Proper tax bill" to "সঠিক ট্যাক্স ইনভয়েস",

        // Product Card & Detail
        "Featured" to "বিশেষ পণ্য",
        "Organic Fertilizer" to "জৈব সার",
        "Organic Bio-Pesticide" to "জৈব কীটনাশক",
        "Choose pack size" to "প্যাকের মাপ বেছে নিন",
        "Prices include GST" to "দামের মধ্যে GST অন্তর্ভুক্ত",
        "Add to Cart" to "কার্টে রাখুন",
        "In Cart" to "কার্টে আছে",
        "View Details & Crop Dose Calculator" to "বিস্তারিত ও ফসলের মাত্রা ক্যালকুলেটর",
        "Crop Dosage Calculator" to "ফসল অনুযায়ী মাত্রা ক্যালকুলেটর",
        "How much do I need?" to "আমার জমির জন্য কতটুকু লাগবে?",
        "Key Benefits" to "প্রধান উপকারিতা",
        "Organic Composition" to "জৈব উপাদানসমূহ",

        // Cart & Checkout
        "Your Cart" to "আপনার কার্ট",
        "Your cart is empty" to "আপনার কার্ট খালি",
        "Browse our 100% organic fertilizers and bio-pesticides to start your order." to
            "অর্ডার শুরু করতে আমাদের ১০০% জৈব সার ও জৈব কীটনাশক দেখুন।",
        "Browse Organic Products" to "জৈব পণ্য দেখুন",
        "Items Subtotal (incl. GST)" to "পণ্যের মোট মূল্য (GST সহ)",
        "Included GST" to "অন্তর্ভুক্ত GST",
        "Delivery Fee" to "ডেলিভারি চার্জ",
        "Free" to "ফ্রি",
        "Grand Total" to "সর্বমোট দেত্তয়া",
        "1. Delivery Address" to "১. ডেলিভারির ঠিকানা",
        "Add New Address" to "নতুন ঠিকানা যোগ করুন",
        "2. Offline Payment Mode (No Gateway)" to "২. পেমেন্ট পদ্ধতি (অফলাইন / গেটওয়ে ছাড়া)",
        "How offline payment works: Place your order and tap 'Send on WhatsApp'. Our owner (Chikur Kumar Ray) receives your order directly on WhatsApp and shares/confirms UPI, Bank Transfer, or Cash on Delivery." to
            "কীভাবে পেমেন্ট করবেন: অর্ডার করার পর 'হোয়াটসঅ্যাপে পাঠান' বাটনে চাপুন। আমাদের কর্ণধার সরাসরি হোয়াটসঅ্যাপে আপনার অর্ডার পাবেন এবং ম্যানুয়ালি UPI, ব্যাংক বা ক্যাশ অন ডেলিভারির মাধ্যমে পেমেন্ট নেবেন।",
        "Place Order & Inform Owner on WhatsApp" to "অর্ডার করুন ও হোয়াটসঅ্যাপে মালিককে জানান",

        // Orders
        "My Orders" to "আমার অর্ডারসমূহ",
        "No orders placed yet" to "এখনও কোনো অর্ডার করা হয়নি",
        "Send Order to Owner on WhatsApp" to "হোয়াটসঅ্যাপে মালিককে অর্ডার পাঠান",
        "Re-send Order on WhatsApp" to "আবার হোয়াটসঅ্যাপে অর্ডার পাঠান",
        "Manual UPI / Bank Payment Details" to "ম্যানুয়াল UPI / ব্যাংক পেমেন্টের বিবরণ",
        "View GST Invoice" to "GST ইনভয়েস দেখুন",
        "Cancel Order" to "অর্ডার বাতিল করুন",

        // Advisory & Thought Card
        "Krishi Paramarsha" to "কৃষি পরামর্শ",
        "Organic Crop Guides & Thought Card" to "জৈব চাষের গাইড ও ভাবনা কার্ড",
        "Make a GMM Thought Card" to "GMM ভাবনা কার্ড বানান",

        // Profile
        "Appearance & Language" to "ভাষা ও থিম",
        "Saved Delivery Addresses" to "সংরক্ষিত ঠিকানা",
        "Backend & API Mode (Django Ready)" to "ব্যাকএন্ড ও API সেটিংস (Django)",
        "About GMM Distributors" to "GMM ডিস্ট্রিবিউটরস সম্পর্কে",
        "Board of Directors" to "পরিচালনা পর্ষদ",
        "Save" to "সেভ করুন",
        "Call Us" to "কল করুন",
    )

    fun t(en: String): String = if (current == "bn") BN[en] ?: en else en
}

fun t(en: String): String = Lang.t(en)
