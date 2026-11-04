package com.gmmdistributors.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gmmdistributors.app.AppController
import com.gmmdistributors.app.R

/**
 * Sign in / create account with mobile number + password. It is the same account as the
 * GMM website (shop.gmmdistributors.xyz), so orders placed on either show up on both.
 */
@Composable
fun AuthScreen(app: AppController, onBack: () -> Unit) {
    val bn = app.language == "bn"
    var register by rememberSaveable { mutableStateOf(app.authRegister) }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var password2 by remember { mutableStateOf("") }
    var showPw by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val shownError = error ?: app.authError

    fun submit() {
        val digits = phone.filter { it.isDigit() }.takeLast(10)
        error = when {
            register && name.isBlank() -> if (bn) "আপনার নাম লিখুন।" else "Please enter your name."
            digits.length != 10 || digits.first() !in '6'..'9' -> if (bn) "সঠিক ১০ সংখ্যার মোবাইল নম্বর দিন।" else "Enter a valid 10-digit mobile number."
            password.length < 8 -> if (bn) "পাসওয়ার্ড অন্তত ৮ অক্ষরের হতে হবে।" else "Password must be at least 8 characters."
            register && password != password2 -> if (bn) "দুটি পাসওয়ার্ড মেলেনি।" else "The two passwords do not match."
            else -> null
        }
        if (error != null) return
        keyboard?.hide()  // so the result is visible
        app.authError = null
        if (register) app.register(name.trim(), digits, password) else app.login(digits, password)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(CardBg)
                .border(1.dp, PaperLine, RoundedCornerShape(50))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (bn) "ফিরে যান" else "Back", style = MaterialTheme.typography.labelLarge)
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Forest).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.gmm_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(CircleShape).border(2.dp, SunGold, CircleShape),
            )
            Text(
                if (register) (if (bn) "নতুন অ্যাকাউন্ট খুলুন" else "Create your account")
                else (if (bn) "সাইন ইন করুন" else "Sign in"),
                color = Color.White, style = MaterialTheme.typography.titleLarge,
            )
            Text(
                if (bn) "অর্ডার করতে, অর্ডারের খবর দেখতে ও ইনভয়েস পেতে সাইন ইন দরকার।"
                else "Sign in to place orders, track them and get your GST invoices.",
                color = MutedOnDark, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
            )
        }

        AppCard {
            if (register) {
                AppField(
                    label = if (bn) "আপনার পুরো নাম" else "Full name",
                    value = name,
                    onChange = { name = it.take(80); error = null; app.authError = null },
                )
            }
            AppField(
                label = if (bn) "মোবাইল নম্বর (১০ সংখ্যা)" else "Mobile number (10 digits)",
                value = phone,
                onChange = { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(15); error = null; app.authError = null },
                keyboard = KeyboardType.Phone,
                placeholder = "98765 43210",
            )
            PasswordField(
                label = if (bn) "পাসওয়ার্ড" else "Password",
                value = password,
                show = showPw,
                onToggle = { showPw = !showPw },
                onChange = { password = it.take(128); error = null },
                hint = if (register) (if (bn) "অন্তত ৮ অক্ষর, শুধু সংখ্যা নয়" else "At least 8 characters, not only numbers") else null,
            )
            if (register) {
                PasswordField(
                    label = if (bn) "আবার পাসওয়ার্ড" else "Confirm password",
                    value = password2,
                    show = showPw,
                    onToggle = { showPw = !showPw },
                    onChange = { password2 = it.take(128); error = null },
                )
            }
            shownError?.let {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(DangerBg).padding(12.dp)) {
                    Text(it, color = Danger, style = MaterialTheme.typography.bodyMedium)
                }
            }

            BigButton(
                text = when {
                    app.busy -> if (bn) "অপেক্ষা করুন…" else "Please wait…"
                    register -> if (bn) "অ্যাকাউন্ট খুলুন" else "Create account"
                    else -> if (bn) "সাইন ইন" else "Sign in"
                },
                enabled = !app.busy,
                onClick = { submit() },
            )
            GhostButton(
                text = if (register) (if (bn) "আগে থেকেই অ্যাকাউন্ট আছে? সাইন ইন" else "Already have an account? Sign in")
                else (if (bn) "নতুন? অ্যাকাউন্ট খুলুন" else "New here? Create an account"),
                onClick = { register = !register; error = null },
            )
            if (!register) {
                Text(
                    if (bn) "পাসওয়ার্ড ভুলে গেছেন? ${app.siteConfig.supportPhone.ifBlank { "আমাদের" }} নম্বরে ফোন করুন, আমরা রিসেট করে দেব।"
                    else "Forgot your password? Call us${app.siteConfig.supportPhone.let { if (it.isBlank()) "" else " on $it" }} and we will reset it.",
                    style = MaterialTheme.typography.bodySmall, color = Muted,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    show: Boolean,
    onToggle: () -> Unit,
    onChange: (String) -> Unit,
    hint: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        supportingText = hint?.let { { Text(it) } },
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            Icon(
                if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.clickable(onClick = onToggle),
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg,
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = PaperLine,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}
