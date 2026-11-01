package com.gmmdistributors.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.R
import java.text.NumberFormat
import java.util.Locale

fun formatRupees(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        minimumFractionDigits = 0
    }
    return "₹${nf.format(amount)}"
}

/** Resolves a drawable resource name safely with a fallback to the GMM logo. */
@Composable
fun DrawableImage(
    drawableName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val ctx = LocalContext.current
    val resId = remember(drawableName) {
        val id = ctx.resources.getIdentifier(drawableName, "drawable", ctx.packageName)
        if (id != 0) id else R.drawable.prod_premium_jackpot
    }
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

/** Rounded surface card following the Bas1Minute AppCard pattern. */
@Composable
fun AppCard(
    title: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardBg)
            .border(1.dp, PaperLine, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = TextMain, modifier = Modifier.weight(1f))
                trailing?.invoke()
            }
        }
        content()
    }
}

/** Primary full-width pill button (Forest Green or SunGold or WhatsApp Green). */
@Composable
fun BigButton(
    text: String,
    enabled: Boolean = true,
    gold: Boolean = false,
    whatsapp: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val container = when {
        whatsapp -> WhatsAppGreen
        gold -> SunGold
        ThemeState.dark -> MintLite
        else -> Leaf
    }
    val contentColor = when {
        whatsapp -> Color.White
        gold -> SunInk
        ThemeState.dark -> Forest
        else -> Color.White
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = contentColor),
        modifier = modifier.fillMaxWidth().heightIn(min = 54.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun GhostButton(
    text: String,
    onDark: Boolean = false,
    danger: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val borderColor = when {
        danger -> Danger
        onDark -> Color.White.copy(alpha = 0.6f)
        else -> AccentGreen
    }
    val textColor = when {
        danger -> Danger
        onDark -> Color.White
        else -> AccentGreen
    }
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier.fillMaxWidth().heightIn(min = 50.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}

@Composable
fun SmallPill(
    text: String,
    gold: Boolean = false,
    whatsapp: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = when {
        whatsapp -> WhatsAppGreen
        gold -> SunGold
        else -> Paper2
    }
    val fg = when {
        whatsapp -> Color.White
        gold -> SunInk
        else -> AccentGreen
    }
    Box(
        Modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

/** Organic certification / category badge. */
@Composable
fun OrganicBadge(text: String, isPesticide: Boolean = false) {
    val bg = if (isPesticide) Color(0xFFFFF3D6) else MintSoft
    val fg = if (isPesticide) Color(0xFF7A4B00) else Color(0xFF14472C)
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

/** Status badge for orders (PLACED, CONFIRMED, PACKED, DISPATCHED, DELIVERED, CANCELLED). */
@Composable
fun OrderStatusChip(status: String, label: String) {
    val (bg, fg) = when (status) {
        "DELIVERED", "CONFIRMED" -> MintSoft to Color(0xFF14472C)
        "CANCELLED" -> DangerBg to Danger
        else -> WarnBg to WarnInk
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

/** Large accessible quantity stepper (- / qty / +) matching GMM mobile-first design. */
@Composable
fun QuantityStepper(
    qty: Int,
    maxQty: Int = 10,
    onChange: (Int) -> Unit,
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.5.dp, PaperLine, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clickable { if (qty > 0) onChange(qty - 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
        }
        Text(
            qty.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = TextMain,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.Center,
        )
        Box(
            Modifier
                .size(44.dp)
                .clickable { if (qty < maxQty) onChange(qty + 1) },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
        }
    }
}

@Composable
fun AppField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: String? = null,
    placeholder: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    hint: String? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        isError = error != null,
        supportingText = (error ?: hint)?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardBg,
            unfocusedContainerColor = CardBg,
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = PaperLine,
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun AvatarCircle(text: String, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(SunGold),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.trim().take(1).uppercase().ifBlank { "G" },
            color = SunInk,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
