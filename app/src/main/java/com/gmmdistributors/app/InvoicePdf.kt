package com.gmmdistributors.app

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.gmmdistributors.app.api.Invoice
import com.gmmdistributors.app.ui.fmtRate
import java.io.File
import java.text.NumberFormat
import java.util.Locale

/** Draws the server-issued tax invoice as an A4 PDF with Android's built-in PdfDocument. */
object InvoicePdf {
    private const val W = 595  // A4 at 72 dpi
    private const val H = 842
    private const val M = 36f

    private fun rs(v: Double): String = "Rs. " + NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(v)

    fun write(context: Context, inv: Invoice): File {
        val doc = PdfDocument()
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9.5f; color = 0xFF1B1B1B.toInt() }
        val bold = Paint(text).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val title = Paint(bold).apply { textSize = 16f; color = 0xFF0F2F1E.toInt() }
        val muted = Paint(text).apply { color = 0xFF5F6B60.toInt(); textSize = 8.5f }
        val line = Paint().apply { color = 0xFFCCD5CC.toInt(); strokeWidth = 0.8f }

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
        var c = page.canvas
        var y = M + 10f

        fun wrap(s: String, paint: Paint, width: Float): List<String> {
            val out = mutableListOf<String>()
            s.split("\n").forEach { para ->
                var cur = ""
                para.split(" ").forEach { w ->
                    val next = if (cur.isEmpty()) w else "$cur $w"
                    if (paint.measureText(next) > width && cur.isNotEmpty()) { out.add(cur); cur = w } else cur = next
                }
                if (cur.isNotEmpty()) out.add(cur)
            }
            return out
        }

        fun newPageIfNeeded(need: Float) {
            if (y + need < H - M) return
            doc.finishPage(page)
            pageNo += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            c = page.canvas
            y = M + 10f
        }

        fun right(s: String, x: Float, p: Paint) = c.drawText(s, x - p.measureText(s), y, p)

        // Header
        c.drawText("TAX INVOICE", (W - title.measureText("TAX INVOICE")) / 2f, y, title); y += 22f
        c.drawText(inv.seller.name, M, y, bold)
        right("Invoice No: ${inv.invoiceNo}", W - M, bold); y += 13f
        val sellerLines = wrap(inv.seller.address, text, 300f) + listOfNotNull(
            inv.seller.gstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" },
            inv.seller.cin.takeIf { it.isNotBlank() }?.let { "CIN: $it" },
            "State: ${inv.seller.state} (Code ${inv.seller.stateCode})",
        )
        val rightLines = listOf("Date: ${inv.issuedDate}", "Order: ${inv.orderNo}", "Order date: ${inv.orderDate}")
        for (i in 0 until maxOf(sellerLines.size, rightLines.size)) {
            sellerLines.getOrNull(i)?.let { c.drawText(it, M, y, text) }
            rightLines.getOrNull(i)?.let { right(it, W - M, text) }
            y += 12f
        }
        y += 4f; c.drawLine(M, y, W - M, y, line); y += 16f

        c.drawText("Bill to / Ship to", M, y, muted); y += 12f
        c.drawText("${inv.buyer.fullName}  ·  ${inv.buyer.phone}", M, y, bold); y += 12f
        wrap(inv.buyer.formattedLine(), text, W - 2 * M).forEach { c.drawText(it, M, y, text); y += 12f }
        y += 6f

        // Items table
        val cols = floatArrayOf(M, 290f, 340f, 375f, 445f, 490f, W - M)  // item | HSN | qty | rate | taxable | GST% | amount
        fun header() {
            c.drawLine(M, y - 10f, W - M, y - 10f, line)
            c.drawText("Item", cols[0], y, bold); c.drawText("HSN", cols[1], y, bold)
            right("Qty", cols[2] + 25f, bold); right("Rate", cols[3] + 60f, bold)
            right("Taxable", cols[4] + 40f, bold); right("GST%", cols[5] + 30f, bold); right("Amount", cols[6], bold)
            y += 6f; c.drawLine(M, y, W - M, y, line); y += 13f
        }
        header()
        inv.items.forEach { it0 ->
            val name = wrap("${it0.productName} (${it0.variantLabel})", text, 245f)
            newPageIfNeeded(14f * name.size + 6f)
            c.drawText(name.first(), cols[0], y, text)
            c.drawText(it0.hsnCode, cols[1], y, text)
            right(it0.quantity.toString(), cols[2] + 25f, text)
            right(rs(it0.unitPrice), cols[3] + 60f, text)
            right(rs(it0.taxableValue), cols[4] + 40f, text)
            right(fmtRate(it0.gstRate), cols[5] + 30f, text)
            right(rs(it0.lineTotal), cols[6], text)
            name.drop(1).forEach { y += 11f; c.drawText(it, cols[0], y, text) }
            y += 14f
        }
        c.drawLine(M, y - 8f, W - M, y - 8f, line); y += 6f

        // Totals
        newPageIfNeeded(120f)
        val tx = W - M - 200f
        fun total(label: String, value: String, p: Paint = text) { c.drawText(label, tx, y, p); right(value, W - M, p); y += 14f }
        total("Taxable value", rs(inv.taxableTotal))
        if (inv.isInterstate) total("IGST", rs(inv.igst)) else { total("CGST", rs(inv.cgst)); total("SGST", rs(inv.sgst)) }
        total("Delivery", if (inv.deliveryFee <= 0.0) "FREE" else rs(inv.deliveryFee))
        c.drawLine(tx, y - 9f, W - M, y - 9f, line); y += 4f
        total("Grand total", rs(inv.total), Paint(bold).apply { textSize = 12f })
        y += 10f
        c.drawText("Prices include GST. Payment: ${inv.paymentMethodLabel} (${inv.paymentStatusLabel}).", M, y, muted); y += 12f
        c.drawText("This is a computer-generated invoice.", M, y, muted)

        doc.finishPage(page)
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "GMM-Invoice-${inv.invoiceNo.replace('/', '-')}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
