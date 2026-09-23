package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.LedgerEntry
import com.example.data.model.Loan
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportHelper {

    const val BRAND_NAME = "FlowVault"
    const val BRAND_TAGLINE = "Personal Lending & Debt Ledger Management Platform"
    const val APP_URL = "https://ais-pre-sn3l23ryfhmgx5sdn7a6bx-647701570309.asia-east1.run.app"

    // Colors for PDF rendering
    private val COLOR_PRIMARY_NAVY = Color.rgb(30, 53, 69)      // #1E3545
    private val COLOR_ACCENT_BLUE = Color.rgb(30, 136, 229)     // #1E88E5
    private val COLOR_TEAL = Color.rgb(43, 182, 163)            // #2BB6A3
    private val COLOR_GREEN = Color.rgb(22, 163, 74)            // #16A34A
    private val COLOR_RED = Color.rgb(220, 38, 38)              // #DC2626
    private val COLOR_AMBER = Color.rgb(217, 119, 6)            // #D97706
    private val COLOR_BG_LIGHT = Color.rgb(248, 250, 252)       // #F8FAFC
    private val COLOR_BG_BOX = Color.rgb(241, 245, 249)         // #F1F5F9
    private val COLOR_BORDER = Color.rgb(203, 213, 225)         // #CBD5E1
    private val COLOR_TEXT_DARK = Color.rgb(15, 23, 42)         // #0F172A
    private val COLOR_TEXT_MUTED = Color.rgb(100, 116, 139)     // #64748B

    /**
     * Export all loans and ledger transactions for a person/borrower into an official PDF statement.
     */
    fun exportBorrowerPdf(
        context: Context,
        borrowerName: String,
        loans: List<Loan>,
        ledgerEntries: List<LedgerEntry>,
        currencySymbol: String = "₹"
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val generatedTimestamp = dateFormat.format(Date())

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
            // Top branding banner
            paint.color = COLOR_PRIMARY_NAVY
            paint.style = Paint.Style.FILL
            c.drawRect(0f, 0f, pageWidth.toFloat(), 64f, paint)

            // Accent bar
            paint.color = COLOR_TEAL
            c.drawRect(0f, 64f, pageWidth.toFloat(), 67f, paint)

            // Brand Title
            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText(BRAND_NAME.uppercase(Locale.ROOT), 36f, 32f, paint)

            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(203, 213, 225)
            c.drawText(BRAND_TAGLINE, 36f, 48f, paint)

            // Top Right Statement Title
            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.RIGHT
            c.drawText("CUSTOMER LEDGER STATEMENT", (pageWidth - 36).toFloat(), 28f, paint)

            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(203, 213, 225)
            c.drawText("Generated: $generatedTimestamp", (pageWidth - 36).toFloat(), 44f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Footer bar
            paint.color = COLOR_BORDER
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            c.drawLine(36f, (pageHeight - 32).toFloat(), (pageWidth - 36).toFloat(), (pageHeight - 32).toFloat(), paint)

            paint.style = Paint.Style.FILL
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 7.5f
            c.drawText("$BRAND_NAME • Cloud & Local Debt Management Ledger • $APP_URL", 36f, (pageHeight - 18).toFloat(), paint)

            paint.textAlign = Paint.Align.RIGHT
            c.drawText("Page $pNum", (pageWidth - 36).toFloat(), (pageHeight - 18).toFloat(), paint)
            paint.textAlign = Paint.Align.LEFT
        }

        // Draw header on first page
        drawHeaderAndFooter(canvas, pageNumber)

        var y = 85f

        // Borrower Summary Card
        paint.color = COLOR_BG_BOX
        paint.style = Paint.Style.FILL
        val summaryBox = RectF(36f, y, (pageWidth - 36).toFloat(), y + 74f)
        canvas.drawRoundRect(summaryBox, 6f, 6f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(summaryBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Customer details inside summary
        paint.color = COLOR_TEXT_DARK
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ACCOUNT HOLDER: $borrowerName", 48f, y + 22f, paint)

        // Totals calculation
        val pendingLoans = loans.filter { it.status != "SETTLED" }
        val settledLoans = loans.filter { it.status == "SETTLED" }
        val totalGiven = if (pendingLoans.isNotEmpty()) {
            pendingLoans.filter { it.direction == "GIVEN" }.sumOf { it.balanceRemaining }
        } else {
            loans.filter { it.direction == "GIVEN" }.sumOf { it.principalAmount }
        }
        val totalTaken = if (pendingLoans.isNotEmpty()) {
            pendingLoans.filter { it.direction == "TAKEN" }.sumOf { it.balanceRemaining }
        } else {
            loans.filter { it.direction == "TAKEN" }.sumOf { it.principalAmount }
        }
        val netBalance = totalGiven - totalTaken

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("Total Active Loans: ${pendingLoans.size}   |   Settled Loans: ${settledLoans.size}   |   Total Loans: ${loans.size}", 48f, y + 40f, paint)

        // Net balance display
        val balanceStr = formatCurrency(Math.abs(netBalance), currencySymbol)
        val balanceLabel = if (netBalance >= 0) "Net Balance: $balanceStr (Receivable)" else "Net Balance: $balanceStr (Payable)"
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (netBalance >= 0) COLOR_GREEN else COLOR_RED
        canvas.drawText(balanceLabel, 48f, y + 60f, paint)

        y += 92f

        // SECTION 1: LOANS TABLE
        paint.color = COLOR_PRIMARY_NAVY
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("1. LOAN ACCOUNTS SUMMARY (${loans.size})", 36f, y, paint)
        y += 8f

        // Table Header
        paint.color = COLOR_PRIMARY_NAVY
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LOAN NO", 42f, y + 13f, paint)
        canvas.drawText("TYPE", 112f, y + 13f, paint)
        canvas.drawText("PRINCIPAL", 160f, y + 13f, paint)
        canvas.drawText("INT RATE", 226f, y + 13f, paint)
        canvas.drawText("START DATE", 286f, y + 13f, paint)
        canvas.drawText("DUE DATE", 352f, y + 13f, paint)
        canvas.drawText("TOTAL DUE", 418f, y + 13f, paint)
        canvas.drawText("BALANCE", 480f, y + 13f, paint)
        canvas.drawText("STATUS", 528f, y + 13f, paint)

        y += 20f

        if (loans.isEmpty()) {
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("No loan accounts recorded for this person.", 42f, y + 14f, paint)
            y += 20f
        } else {
            loans.forEachIndexed { index, loan ->
                if (y > pageHeight - 80) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeaderAndFooter(canvas, pageNumber)
                    y = 80f
                }

                // Row background striping
                paint.color = if (index % 2 == 0) COLOR_BG_LIGHT else Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 18f, paint)

                paint.color = COLOR_BORDER
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(36f, y + 18f, (pageWidth - 36).toFloat(), y + 18f, paint)
                paint.style = Paint.Style.FILL

                // Columns
                paint.color = COLOR_TEXT_DARK
                paint.textSize = 7.5f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(loan.loanNumber.take(12), 42f, y + 12f, paint)

                paint.color = if (loan.direction == "GIVEN") COLOR_GREEN else COLOR_AMBER
                canvas.drawText(if (loan.direction == "GIVEN") "Given" else "Taken", 112f, y + 12f, paint)

                paint.color = COLOR_TEXT_DARK
                canvas.drawText(formatCurrency(loan.principalAmount, currencySymbol), 160f, y + 12f, paint)
                canvas.drawText("${loan.annualInterestRate}% ${loan.interestPeriod.take(3)}", 226f, y + 12f, paint)
                canvas.drawText(formatDate(loan.startDate), 286f, y + 12f, paint)
                canvas.drawText(formatDate(loan.dueDate), 352f, y + 12f, paint)
                canvas.drawText(formatCurrency(loan.totalAmountDue, currencySymbol), 418f, y + 12f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (loan.balanceRemaining > 0) COLOR_RED else COLOR_GREEN
                canvas.drawText(formatCurrency(loan.balanceRemaining, currencySymbol), 480f, y + 12f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.color = when (loan.status) {
                    "SETTLED" -> COLOR_GREEN
                    "OVERDUE" -> COLOR_RED
                    else -> COLOR_ACCENT_BLUE
                }
                canvas.drawText(loan.status, 528f, y + 12f, paint)

                y += 18f
            }
        }

        y += 16f

        // SECTION 2: LEDGER TRANSACTIONS TABLE
        if (y > pageHeight - 120) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            drawHeaderAndFooter(canvas, pageNumber)
            y = 80f
        }

        paint.color = COLOR_PRIMARY_NAVY
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("2. LEDGER TRANSACTIONS & PAYMENT HISTORY (${ledgerEntries.size})", 36f, y, paint)
        y += 8f

        // Table Header
        paint.color = COLOR_PRIMARY_NAVY
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE", 42f, y + 13f, paint)
        canvas.drawText("REF CODE", 112f, y + 13f, paint)
        canvas.drawText("TYPE", 180f, y + 13f, paint)
        canvas.drawText("CATEGORY", 232f, y + 13f, paint)
        canvas.drawText("DESCRIPTION", 330f, y + 13f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT", (pageWidth - 44).toFloat(), y + 13f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 20f

        if (ledgerEntries.isEmpty()) {
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("No transaction or repayment entries logged for this account.", 42f, y + 14f, paint)
            y += 20f
        } else {
            val sortedEntries = ledgerEntries.sortedByDescending { it.timestamp }
            sortedEntries.forEachIndexed { index, entry ->
                if (y > pageHeight - 50) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeaderAndFooter(canvas, pageNumber)
                    y = 80f

                    // Repeat table header on new page
                    paint.color = COLOR_PRIMARY_NAVY
                    paint.style = Paint.Style.FILL
                    canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 18f, paint)
                    paint.color = Color.WHITE
                    paint.textSize = 7.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("DATE", 42f, y + 12f, paint)
                    canvas.drawText("REF CODE", 112f, y + 12f, paint)
                    canvas.drawText("TYPE", 180f, y + 12f, paint)
                    canvas.drawText("CATEGORY", 232f, y + 12f, paint)
                    canvas.drawText("DESCRIPTION", 330f, y + 12f, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("AMOUNT", (pageWidth - 44).toFloat(), y + 12f, paint)
                    paint.textAlign = Paint.Align.LEFT
                    y += 18f
                }

                paint.color = if (index % 2 == 0) COLOR_BG_LIGHT else Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 17f, paint)

                paint.color = COLOR_BORDER
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(36f, y + 17f, (pageWidth - 36).toFloat(), y + 17f, paint)
                paint.style = Paint.Style.FILL

                paint.color = COLOR_TEXT_DARK
                paint.textSize = 7.5f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(formatDate(entry.timestamp), 42f, y + 11f, paint)
                canvas.drawText(entry.referenceCode.take(12), 112f, y + 11f, paint)

                val isCredit = entry.entryType.equals("CREDIT", ignoreCase = true)
                paint.color = if (isCredit) COLOR_GREEN else COLOR_RED
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(if (isCredit) "CREDIT (Cr)" else "DEBIT (Dr)", 180f, y + 11f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.color = COLOR_TEXT_MUTED
                canvas.drawText(entry.category.replace("_", " ").take(18), 232f, y + 11f, paint)
                canvas.drawText(entry.description.take(28), 330f, y + 11f, paint)

                paint.color = if (isCredit) COLOR_GREEN else COLOR_RED
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.RIGHT
                val sign = if (isCredit) "+" else "-"
                canvas.drawText("$sign${formatCurrency(entry.amount, currencySymbol)}", (pageWidth - 44).toFloat(), y + 11f, paint)
                paint.textAlign = Paint.Align.LEFT

                y += 17f
            }
        }

        document.finishPage(page)

        // Write to cache directory file
        val sanitizedName = borrowerName.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val pdfFile = File(exportDir, "Ledger_${sanitizedName}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Export a single loan's complete ledger and agreement details into an official PDF document.
     */
    fun exportSingleLoanPdf(
        context: Context,
        loan: Loan,
        ledgerEntries: List<LedgerEntry>,
        currencySymbol: String = "₹"
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val generatedTimestamp = dateFormat.format(Date())

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawHeaderAndFooter(c: Canvas, pNum: Int) {
            paint.color = COLOR_PRIMARY_NAVY
            paint.style = Paint.Style.FILL
            c.drawRect(0f, 0f, pageWidth.toFloat(), 64f, paint)

            paint.color = COLOR_ACCENT_BLUE
            c.drawRect(0f, 64f, pageWidth.toFloat(), 67f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText(BRAND_NAME.uppercase(Locale.ROOT), 36f, 32f, paint)

            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(203, 213, 225)
            c.drawText(BRAND_TAGLINE, 36f, 48f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.RIGHT
            c.drawText("LOAN ACCOUNT STATEMENT", (pageWidth - 36).toFloat(), 28f, paint)

            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(203, 213, 225)
            c.drawText("Generated: $generatedTimestamp", (pageWidth - 36).toFloat(), 44f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Footer
            paint.color = COLOR_BORDER
            paint.strokeWidth = 1f
            paint.style = Paint.Style.STROKE
            c.drawLine(36f, (pageHeight - 32).toFloat(), (pageWidth - 36).toFloat(), (pageHeight - 32).toFloat(), paint)

            paint.style = Paint.Style.FILL
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 7.5f
            c.drawText("$BRAND_NAME • Official Loan Statement • $APP_URL", 36f, (pageHeight - 18).toFloat(), paint)

            paint.textAlign = Paint.Align.RIGHT
            c.drawText("Page $pNum", (pageWidth - 36).toFloat(), (pageHeight - 18).toFloat(), paint)
            paint.textAlign = Paint.Align.LEFT
        }

        drawHeaderAndFooter(canvas, pageNumber)

        var y = 84f

        // Loan Particulars Card (2-column key figures)
        paint.color = COLOR_BG_BOX
        paint.style = Paint.Style.FILL
        val cardRect = RectF(36f, y, (pageWidth - 36).toFloat(), y + 140f)
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)

        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Header inside card
        paint.color = COLOR_TEXT_DARK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LOAN NO: ${loan.loanNumber}", 48f, y + 22f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("Borrower / Account: ${loan.borrowerName}", 48f, y + 38f, paint)

        // Status Badge
        paint.color = when (loan.status) {
            "SETTLED" -> COLOR_GREEN
            "OVERDUE" -> COLOR_RED
            else -> COLOR_ACCENT_BLUE
        }
        val statusRect = RectF((pageWidth - 120).toFloat(), y + 12f, (pageWidth - 48).toFloat(), y + 32f)
        canvas.drawRoundRect(statusRect, 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(loan.status, statusRect.centerX(), statusRect.centerY() + 3f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Divider
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.5f
        canvas.drawLine(48f, y + 48f, (pageWidth - 48).toFloat(), y + 48f, paint)

        // Left Column (Loan Terms)
        paint.textSize = 9f
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_TEXT_MUTED
        val col1X = 48f
        val col2X = 300f

        canvas.drawText("Loan Direction:", col1X, y + 64f, paint)
        canvas.drawText("Principal Amount:", col1X, y + 80f, paint)
        canvas.drawText("Interest Rate & Type:", col1X, y + 96f, paint)
        canvas.drawText("Tenure / Period:", col1X, y + 112f, paint)
        canvas.drawText("Start & Due Date:", col1X, y + 128f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dirText = if (loan.direction == "GIVEN") "Given (Lent to customer)" else "Taken (Borrowed by you)"
        canvas.drawText(dirText, col1X + 90f, y + 64f, paint)
        canvas.drawText(formatCurrency(loan.principalAmount, currencySymbol), col1X + 90f, y + 80f, paint)
        canvas.drawText("${loan.annualInterestRate}% ${loan.interestPeriod.lowercase()} (${loan.interestType})", col1X + 90f, y + 96f, paint)
        canvas.drawText("${loan.termMonths} Months (${loan.interestBasis.replace("_", " ")})", col1X + 90f, y + 112f, paint)
        canvas.drawText("${formatDate(loan.startDate)}  ➔  ${formatDate(loan.dueDate)}", col1X + 90f, y + 128f, paint)

        // Right Column (Balances & Collections)
        paint.typeface = Typeface.DEFAULT
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("Total Interest:", col2X, y + 64f, paint)
        canvas.drawText("Total Amount Due:", col2X, y + 80f, paint)
        canvas.drawText("Amount Repaid:", col2X, y + 96f, paint)
        canvas.drawText("Balance Remaining:", col2X, y + 116f, paint)

        paint.color = COLOR_TEXT_DARK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatCurrency(loan.totalInterest, currencySymbol), col2X + 95f, y + 64f, paint)
        canvas.drawText(formatCurrency(loan.totalAmountDue, currencySymbol), col2X + 95f, y + 80f, paint)
        paint.color = COLOR_GREEN
        canvas.drawText(formatCurrency(loan.amountPaid, currencySymbol), col2X + 95f, y + 96f, paint)

        paint.color = if (loan.balanceRemaining > 0) COLOR_RED else COLOR_GREEN
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatCurrency(loan.balanceRemaining, currencySymbol), col2X + 95f, y + 116f, paint)

        y += 156f

        // SECTION: REPAYMENT & TRANSACTION LEDGER
        paint.color = COLOR_PRIMARY_NAVY
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("REPAYMENT TRANSACTIONS & LEDGER ENTRIES (${ledgerEntries.size})", 36f, y, paint)
        y += 8f

        // Table Header
        paint.color = COLOR_PRIMARY_NAVY
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DATE", 44f, y + 13f, paint)
        canvas.drawText("REFERENCE", 124f, y + 13f, paint)
        canvas.drawText("TYPE", 204f, y + 13f, paint)
        canvas.drawText("CATEGORY / DETAILS", 270f, y + 13f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT", (pageWidth - 44).toFloat(), y + 13f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 20f

        if (ledgerEntries.isEmpty()) {
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText("No repayment records logged for this loan.", 44f, y + 14f, paint)
            y += 24f
        } else {
            val sortedEntries = ledgerEntries.sortedByDescending { it.timestamp }
            sortedEntries.forEachIndexed { index, entry ->
                if (y > pageHeight - 50) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    drawHeaderAndFooter(canvas, pageNumber)
                    y = 80f
                }

                paint.color = if (index % 2 == 0) COLOR_BG_LIGHT else Color.WHITE
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, y, (pageWidth - 36).toFloat(), y + 18f, paint)

                paint.color = COLOR_BORDER
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 0.5f
                canvas.drawLine(36f, y + 18f, (pageWidth - 36).toFloat(), y + 18f, paint)
                paint.style = Paint.Style.FILL

                paint.color = COLOR_TEXT_DARK
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT
                canvas.drawText(formatDate(entry.timestamp), 44f, y + 12f, paint)
                canvas.drawText(entry.referenceCode, 124f, y + 12f, paint)

                val isCredit = entry.entryType.equals("CREDIT", ignoreCase = true)
                paint.color = if (isCredit) COLOR_GREEN else COLOR_RED
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(if (isCredit) "CREDIT" else "DEBIT", 204f, y + 12f, paint)

                paint.typeface = Typeface.DEFAULT
                paint.color = COLOR_TEXT_MUTED
                val note = if (entry.description.isNotBlank()) entry.description else entry.category.replace("_", " ")
                canvas.drawText(note.take(40), 270f, y + 12f, paint)

                paint.color = if (isCredit) COLOR_GREEN else COLOR_RED
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textAlign = Paint.Align.RIGHT
                val sign = if (isCredit) "+" else "-"
                canvas.drawText("$sign${formatCurrency(entry.amount, currencySymbol)}", (pageWidth - 44).toFloat(), y + 12f, paint)
                paint.textAlign = Paint.Align.LEFT

                y += 18f
            }
        }

        // Additional Notes if any
        if (loan.notes.isNotBlank() && y < pageHeight - 90) {
            y += 14f
            paint.color = COLOR_TEXT_MUTED
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NOTES / SPECIAL CONDITIONS:", 36f, y, paint)
            y += 12f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(loan.notes.take(120), 36f, y, paint)
            y += 14f
        }

        document.finishPage(page)

        val sanitizedNo = loan.loanNumber.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val pdfFile = File(exportDir, "Loan_${sanitizedNo}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    /**
     * Creates a high-resolution, visually rich WhatsApp Statement Card Bitmap with branding and figures.
     */
    fun createWhatsAppCardBitmap(
        context: Context,
        loan: Loan,
        currencySymbol: String = "₹"
    ): File {
        val width = 720
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background: Premium deep slate
        paint.color = Color.rgb(15, 23, 42) // #0F172A
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Card Container
        val cardMargin = 28f
        val cardRect = RectF(cardMargin, cardMargin, width - cardMargin, height - cardMargin)
        paint.color = Color.rgb(30, 41, 59) // #1E293B
        canvas.drawRoundRect(cardRect, 20f, 20f, paint)

        // Top Brand Header inside card
        val headerRect = RectF(cardMargin, cardMargin, width - cardMargin, cardMargin + 90f)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(headerRect, 20f, 20f, paint)
        canvas.drawRect(cardMargin, cardMargin + 40f, width - cardMargin, cardMargin + 90f, paint)

        // Accent top border
        paint.color = COLOR_TEAL
        canvas.drawRect(cardMargin + 20f, cardMargin, width - cardMargin - 20f, cardMargin + 4f, paint)

        // Brand Text
        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(BRAND_NAME.uppercase(Locale.ROOT), cardMargin + 24f, cardMargin + 50f, paint)

        paint.color = COLOR_TEAL
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("SMART LENDING & DEBT LEDGER", cardMargin + 24f, cardMargin + 72f, paint)

        // Right side badge: LOAN STATEMENT
        paint.color = Color.rgb(43, 182, 163)
        val badgeRect = RectF(width - cardMargin - 150f, cardMargin + 32f, width - cardMargin - 20f, cardMargin + 62f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("LOAN SLIP", badgeRect.centerX(), badgeRect.centerY() + 4f, paint)
        paint.textAlign = Paint.Align.LEFT

        var y = cardMargin + 120f

        // Borrower Name & Loan Number
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("BORROWER / ACCOUNT NAME", cardMargin + 24f, y, paint)

        y += 28f
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(loan.borrowerName, cardMargin + 24f, y, paint)

        // Loan Number + Status
        y += 26f
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(148, 163, 184)
        canvas.drawText("Loan ID: #${loan.loanNumber}   •   Direction: ${if (loan.direction == "GIVEN") "Given (Lent)" else "Taken (Borrowed)"}", cardMargin + 24f, y, paint)

        y += 28f

        // 4 Key Grid Metric Boxes
        fun drawMetricBox(rect: RectF, title: String, value: String, valueColor: Int) {
            paint.color = Color.rgb(15, 23, 42)
            canvas.drawRoundRect(rect, 12f, 12f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(rect, 12f, 12f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 11f
            paint.typeface = Typeface.DEFAULT
            canvas.drawText(title, rect.left + 16f, rect.top + 28f, paint)

            paint.color = valueColor
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(value, rect.left + 16f, rect.top + 58f, paint)
        }

        val boxWidth = (width - (cardMargin * 2) - 48f - 16f) / 2
        val boxHeight = 76f

        // Row 1: Principal & Interest
        val b1 = RectF(cardMargin + 24f, y, cardMargin + 24f + boxWidth, y + boxHeight)
        val b2 = RectF(b1.right + 16f, y, b1.right + 16f + boxWidth, y + boxHeight)
        drawMetricBox(b1, "PRINCIPAL AMOUNT", formatCurrency(loan.principalAmount, currencySymbol), Color.WHITE)
        drawMetricBox(b2, "INTEREST RATE", "${loan.annualInterestRate}% ${loan.interestPeriod.lowercase()}", Color.rgb(56, 189, 248))

        y += boxHeight + 14f

        // Row 2: Total Due & Amount Paid
        val b3 = RectF(cardMargin + 24f, y, cardMargin + 24f + boxWidth, y + boxHeight)
        val b4 = RectF(b3.right + 16f, y, b3.right + 16f + boxWidth, y + boxHeight)
        drawMetricBox(b3, "TOTAL DUE", formatCurrency(loan.totalAmountDue, currencySymbol), Color.WHITE)
        drawMetricBox(b4, "AMOUNT REPAID", formatCurrency(loan.amountPaid, currencySymbol), Color.rgb(34, 197, 94))

        y += boxHeight + 18f

        // Prominent Remaining Balance Box
        val balanceBox = RectF(cardMargin + 24f, y, width - cardMargin - 24f, y + 90f)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(balanceBox, 14f, 14f, paint)

        // Accent border
        paint.color = if (loan.balanceRemaining > 0) Color.rgb(239, 68, 68) else Color.rgb(34, 197, 94)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(balanceBox, 14f, 14f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("BALANCE OUTSTANDING", balanceBox.left + 20f, balanceBox.top + 32f, paint)

        paint.color = if (loan.balanceRemaining > 0) Color.rgb(248, 113, 113) else Color.rgb(74, 222, 128)
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(formatCurrency(loan.balanceRemaining, currencySymbol), balanceBox.left + 20f, balanceBox.top + 70f, paint)

        // Status on the right of balance box
        val statusText = loan.status
        paint.color = when (statusText) {
            "SETTLED" -> Color.rgb(34, 197, 94)
            "OVERDUE" -> Color.rgb(239, 68, 68)
            else -> Color.rgb(56, 189, 248)
        }
        paint.textSize = 14f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS: $statusText", balanceBox.right - 20f, balanceBox.top + 52f, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 114f

        // Dates Row
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 11.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Start Date: ${formatDate(loan.startDate)}     •     Due Date: ${formatDate(loan.dueDate)}", cardMargin + 24f, y, paint)

        y += 36f

        // Bottom App Branding & Link Footer
        val footerBox = RectF(cardMargin + 16f, height - cardMargin - 95f, width - cardMargin - 16f, height - cardMargin - 16f)
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRoundRect(footerBox, 10f, 10f, paint)

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Powered by $BRAND_NAME Personal Debt Ledger", footerBox.left + 16f, footerBox.top + 30f, paint)

        paint.color = COLOR_TEAL
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Track, verify and manage loans: $APP_URL", footerBox.left + 16f, footerBox.top + 52f, paint)

        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val cardFile = File(shareDir, "loan_card_${loan.loanNumber}.png")
        FileOutputStream(cardFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return cardFile
    }

    /**
     * Share single loan directly to WhatsApp with visual card image + formatted text + app link.
     */
    fun shareSingleLoanToWhatsApp(
        context: Context,
        loan: Loan,
        currencySymbol: String = "₹"
    ) {
        try {
            val cardFile = createWhatsAppCardBitmap(context, loan, currencySymbol)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cardFile)

            val dirText = if (loan.direction == "GIVEN") "Given (Lent)" else "Taken (Borrowed)"
            val messageText = buildString {
                appendLine("📄 *${BRAND_NAME.uppercase(Locale.ROOT)} LOAN STATEMENT*")
                appendLine("══════════════════════════")
                appendLine("👤 *Borrower:* ${loan.borrowerName}")
                appendLine("📋 *Loan No:* #${loan.loanNumber}")
                appendLine("💳 *Type:* $dirText")
                appendLine("💰 *Principal Amount:* ${formatCurrency(loan.principalAmount, currencySymbol)}")
                appendLine("📈 *Interest Rate:* ${loan.annualInterestRate}% ${loan.interestPeriod.lowercase()} (${loan.interestType})")
                appendLine("📅 *Start Date:* ${formatDate(loan.startDate)}")
                appendLine("🎯 *Due Date:* ${formatDate(loan.dueDate)}")
                appendLine("📊 *Total Due:* ${formatCurrency(loan.totalAmountDue, currencySymbol)}")
                appendLine("✅ *Amount Repaid:* ${formatCurrency(loan.amountPaid, currencySymbol)}")
                appendLine("⚖️ *Balance Outstanding:* ${formatCurrency(loan.balanceRemaining, currencySymbol)}")
                appendLine("📌 *Status:* ${loan.status}")
                appendLine("══════════════════════════")
                appendLine("_Official statement from FlowVault Personal Debt Ledger_")
                appendLine()
                appendLine("📲 *Manage or view this loan in FlowVault:*")
                appendLine(APP_URL)
            }

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, messageText)
                putExtra(Intent.EXTRA_SUBJECT, "Loan Statement - #${loan.loanNumber}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }

            try {
                context.startActivity(whatsappIntent)
            } catch (e: ActivityNotFoundException) {
                // If standard WhatsApp is not installed, open system chooser (WhatsApp Business, Telegram, etc.)
                val chooserIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    putExtra(Intent.EXTRA_SUBJECT, "Loan Statement - #${loan.loanNumber}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(chooserIntent, "Share Loan Statement"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error generating WhatsApp card: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Share any generated PDF statement using Android FileProvider & system chooser.
     */
    fun sharePdfFile(
        context: Context,
        pdfFile: File,
        subject: String
    ) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the official $BRAND_NAME financial ledger statement.\n\nManage your personal loans and debt accounts: $APP_URL"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share PDF Statement via")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
