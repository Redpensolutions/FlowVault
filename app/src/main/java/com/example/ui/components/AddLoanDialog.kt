package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.Borrower
import com.example.data.model.CalculationResult
import com.example.data.model.LendingCalculations
import com.example.data.model.VirtualAccount
import com.example.ui.components.ProBadge
import com.example.ui.components.ProFeatureLockedDialog
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.FinanceRedLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TealLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(
    virtualAccounts: List<VirtualAccount>,
    borrowers: List<Borrower> = emptyList(),
    currencySymbol: String = "₹",
    defaultInterestType: String = "SIMPLE",
    initialDirection: String = "GIVEN",
    initialBorrowerName: String = "",
    isProPlan: Boolean = false,
    onRequestUpgrade: () -> Unit = {},
    onDismiss: () -> Unit,
    onSaveBorrower: (Borrower) -> Unit = {},
    onConfirm: (
        borrowerName: String,
        phone: String,
        email: String,
        principal: Double,
        rate: Double,
        type: String,
        termMonths: Int,
        disbursementAccountId: Long?,
        notes: String,
        direction: String,
        interestPeriod: String,
        interestBasis: String,
        hasInterest: Boolean,
        tags: String,
        startDate: Long,
        dueDate: Long,
        interestStartDate: Long,
        attachments: String
    ) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }
    val futureDateStr = remember {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.YEAR, 1)
        dateFormat.format(calendar.time)
    }

    // Step 1: Direction & Model choice, Step 2: Detailed Form (Directly open detailed form for chosen loan direction)
    var currentStep by remember { mutableIntStateOf(2) }

    // Core Loan Fields
    val preselectedBorrower = remember(initialBorrowerName, borrowers) {
        if (initialBorrowerName.isNotBlank()) {
            borrowers.firstOrNull { it.name.equals(initialBorrowerName.trim(), ignoreCase = true) }
        } else null
    }
    var direction by remember { mutableStateOf(initialDirection) } // "GIVEN" or "TAKEN"
    var borrowerName by remember { mutableStateOf(initialBorrowerName) }
    var phone by remember { mutableStateOf(preselectedBorrower?.phone ?: "") }
    var email by remember { mutableStateOf(preselectedBorrower?.email ?: "") }
    var principalStr by remember { mutableStateOf("50000") }
    var loanGivenOnStr by remember { mutableStateOf(todayStr) }
    var dueDateStr by remember { mutableStateOf(futureDateStr) }
    var tagsStr by remember { mutableStateOf("") }
    var showSelectBorrowerDialog by remember { mutableStateOf(false) }

    // Cheque / Promissory Note & Attachment Media Pickers
    var showAttachmentSourceDialog by remember { mutableStateOf(false) }
    var attachedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var attachedDocPath by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    attachedBitmap = BitmapFactory.decodeStream(stream)
                }
                val savedPath = saveImageToInternalStorage(context, uri)
                attachedDocPath = savedPath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            attachedBitmap = bitmap
            try {
                val savedPath = saveBitmapToInternalStorage(context, bitmap)
                attachedDocPath = savedPath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is needed to take a picture of the cheque or promissory note", Toast.LENGTH_SHORT).show()
        }
    }

    // Interest Controls
    var showProFeatureLockedDialog by remember { mutableStateOf(false) }
    var lockedFeatureTitle by remember { mutableStateOf("") }
    var lockedFeatureDesc by remember { mutableStateOf("") }

    var addInterestOnThisLoan by remember { mutableStateOf(true) }
    var interestType by remember { mutableStateOf(if (!isProPlan && defaultInterestType == "COMPOUND") "SIMPLE" else defaultInterestType) } // "SIMPLE", "COMPOUND", "FLAT"
    var interestPeriod by remember { mutableStateOf("MONTHLY") } // "MONTHLY", "YEARLY"
    var interestRateStr by remember { mutableStateOf("2.0") }
    var interestStartDateStr by remember { mutableStateOf(todayStr) }
    var interestBasis by remember { mutableStateOf("MONTH_BASED") } // "DAYS_BASED", "MONTH_BASED"

    var selectedAccountId by remember {
        mutableStateOf<Long?>(virtualAccounts.firstOrNull()?.id)
    }
    var accountDropdownExpanded by remember { mutableStateOf(false) }

    var notes by remember { mutableStateOf("") }
    var attachments by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Real-time calculation preview
    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rawRate = interestRateStr.toDoubleOrNull() ?: 0.0
    val annualRate = if (addInterestOnThisLoan) {
        if (interestPeriod == "MONTHLY") rawRate * 12.0 else rawRate
    } else 0.0

    val startDateMillis = try { dateFormat.parse(loanGivenOnStr)?.time ?: System.currentTimeMillis() } catch (e: Exception) { System.currentTimeMillis() }
    val dueDateMillis = try { dateFormat.parse(dueDateStr)?.time ?: (startDateMillis + 365L * 86400000L) } catch (e: Exception) { startDateMillis + 365L * 86400000L }
    val interestStartDateMillis = try { dateFormat.parse(interestStartDateStr)?.time ?: startDateMillis } catch (e: Exception) { startDateMillis }

    val diffDays = ((dueDateMillis - startDateMillis) / (86400000L)).coerceAtLeast(1L)
    val approxMonths = ((diffDays / 30.0).coerceAtLeast(1.0)).toInt()

    val calcResult = remember(principal, rawRate, interestType, approxMonths, addInterestOnThisLoan, interestPeriod, interestBasis, diffDays) {
        if (!addInterestOnThisLoan || rawRate <= 0) {
            CalculationResult(
                principal = principal,
                interestRate = 0.0,
                interestType = "ZERO",
                termMonths = approxMonths,
                totalInterest = 0.0,
                totalAmountDue = principal,
                monthlyPayment = if (approxMonths > 0) principal / approxMonths else principal,
                effectiveAnnualRate = 0.0,
                interestPeriod = interestPeriod,
                interestBasis = interestBasis,
                hasInterest = false
            )
        } else {
            LendingCalculations.calculateLoanExtended(
                principal = principal,
                ratePercent = rawRate,
                interestType = interestType,
                termMonths = approxMonths,
                interestPeriod = interestPeriod,
                interestBasis = interestBasis,
                hasInterest = true,
                daysCount = diffDays.toInt()
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = SurfaceCard
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (currentStep == 1) {
                    // STEP 1: Select Given or Taken Loan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "New Loan Entry",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Step 1 of 2: Select Loan Direction",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Is this loan given or taken?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Direction-specific selection card
                    if (direction == "GIVEN") {
                        // Given Loan (Receivable)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 2.dp,
                                    color = BluePrimary,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("select_given_loan_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = BlueLight.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = FinanceGreen)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Given Loan",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Money lent to someone. You will receive this back with interest (Receivable).",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = BluePrimary)
                            }
                        }
                    } else {
                        // Taken Loan (Payable)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 2.dp,
                                    color = FinanceRed,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .testTag("select_taken_loan_btn"),
                            colors = CardDefaults.cardColors(
                                containerColor = FinanceRedLight.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(FinanceRedLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinanceRed)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Taken Loan",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Money borrowed from someone. You need to repay them according to schedule (Payable).",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = FinanceRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Preferred Interest Calculation Model:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Simple, Compound, Flat Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SIMPLE" to "Simple Interest", "COMPOUND" to "Compound", "FLAT" to "Flat Interest").forEach { (typeKey, label) ->
                            val isSel = interestType == typeKey
                            val isCompoundLocked = typeKey == "COMPOUND" && !isProPlan
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) BluePrimary else BackgroundClean)
                                    .border(1.dp, if (isSel) BluePrimary else BorderSubtle, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isCompoundLocked) {
                                            lockedFeatureTitle = "Compound Interest"
                                            lockedFeatureDesc = "Compound Interest Calculation is a FlowVault Pro feature.\n\nFree Tier supports Simple & Flat interest calculations. Upgrade to Pro (₹99/mo) to unlock Compound Interest with customizable compounding intervals."
                                            showProFeatureLockedDialog = true
                                        } else {
                                            interestType = typeKey
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) Color.White else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isCompoundLocked) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        ProBadge()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryBlueButton(
                        text = "Continue to Loan Details",
                        onClick = { currentStep = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "continue_to_loan_details_button"
                    )

                } else {
                    // STEP 2: Full Detailed Loan Screen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (direction == "GIVEN") "Add Given Loan" else "Add Taken Loan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (direction == "GIVEN") "Money lent to borrower • Receivable" else "Money borrowed from lender • Payable",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Direction Indicator: Given loan screen does not show Taken loan button, and Taken loan screen does not show Given loan button
                    if (direction == "GIVEN") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("given_loan_direction_indicator"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = TealLight),
                            border = BorderStroke(1.dp, FinanceGreen.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(FinanceGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Given Loan (Receivable)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceGreen
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("taken_loan_direction_indicator"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = FinanceRedLight),
                            border = BorderStroke(1.dp, FinanceRed.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(FinanceRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Taken Loan (Payable)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Select Borrower / Lender Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (direction == "GIVEN") "Select Borrower *" else "Select Lender *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (borrowerName.isNotBlank()) {
                            TextButton(
                                onClick = { showSelectBorrowerDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Change Contact", fontSize = 11.sp, color = BluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (borrowerName.isBlank()) {
                        // Prominent Select Borrower Button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showSelectBorrowerDialog = true }
                                .border(1.5.dp, BluePrimary.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .testTag("btn_select_borrower"),
                            colors = CardDefaults.cardColors(containerColor = BlueLight.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(BluePrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonSearch,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (direction == "GIVEN") "Select Borrower" else "Select Lender",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BluePrimary
                                        )
                                        Text(
                                            text = "Tap to choose from saved contacts or phone",
                                            fontSize = 11.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // Selected Borrower Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, FinanceGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .testTag("selected_borrower_card"),
                            colors = CardDefaults.cardColors(containerColor = TealLight.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(FinanceGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = borrowerName.take(1).uppercase(),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = borrowerName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (phone.isNotBlank()) {
                                            Text(
                                                text = "📞 $phone",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                                OutlinedButton(
                                    onClick = { showSelectBorrowerDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp),
                                    border = BorderStroke(1.dp, BluePrimary)
                                ) {
                                    Text("Change", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                }
                            }
                        }
                    }

                    // Quick suggestions from existing borrowers if none selected
                    if (borrowers.isNotEmpty() && borrowerName.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            borrowers.take(3).forEach { b ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueLight)
                                        .clickable {
                                            borrowerName = b.displayName
                                            phone = b.phone
                                            email = b.email
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(b.displayName, fontSize = 11.sp, color = BlueDark, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Loan Amount
                    Text(
                        text = "Loan Amount *",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = { Text("0.00", fontSize = 13.sp) },
                        leadingIcon = {
                            Text(currencySymbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BluePrimary, modifier = Modifier.padding(start = 12.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("loan_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Loan Given On & Loan Due Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (direction == "GIVEN") "Loan Given On" else "Loan Taken On",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = loanGivenOnStr,
                                onValueChange = { loanGivenOnStr = it },
                                placeholder = { Text("YYYY-MM-DD", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Loan Due Date (1 Yr)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = dueDateStr,
                                onValueChange = { dueDateStr = it },
                                placeholder = { Text("YYYY-MM-DD", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🔔 Monthly reminder active",
                                fontSize = 10.sp,
                                color = BluePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Select From Account
                    Text(
                        text = "Select from Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                    ) {
                        val selectedAccount = virtualAccounts.firstOrNull { it.id == selectedAccountId }
                        OutlinedTextField(
                            value = selectedAccount?.let { "${it.name} (${formatCurrency(it.balance, currencySymbol)})" } ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BluePrimary,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = accountDropdownExpanded,
                            onDismissRequest = { accountDropdownExpanded = false }
                        ) {
                            virtualAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(acc.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                            Text(formatCurrency(acc.balance, currencySymbol), color = FinanceGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        accountDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Add Interest on this Loan (Toggle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Add interest on this loan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (addInterestOnThisLoan) "Interest calculations will be applied" else "Zero-interest principal-only loan",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = addInterestOnThisLoan,
                            onCheckedChange = { addInterestOnThisLoan = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BluePrimary
                            )
                        )
                    }

                    if (addInterestOnThisLoan) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Simple / Compound / Flat Interest Buttons
                        Text(
                            text = "Interest Type",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SIMPLE" to "Simple", "COMPOUND" to "Compound", "FLAT" to "Flat").forEach { (typeKey, label) ->
                                val isSel = interestType == typeKey
                                val isCompoundLocked = typeKey == "COMPOUND" && !isProPlan
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) BluePrimary else BackgroundClean)
                                        .border(1.dp, if (isSel) BluePrimary else BorderSubtle, RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isCompoundLocked) {
                                                lockedFeatureTitle = "Compound Interest"
                                                lockedFeatureDesc = "Compound Interest Calculation is a FlowVault Pro feature.\n\nFree Tier supports Simple & Flat interest calculations. Upgrade to Pro (₹99/mo) to unlock Compound Interest with customizable compounding intervals."
                                                showProFeatureLockedDialog = true
                                            } else {
                                                interestType = typeKey
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) Color.White else TextSecondary
                                        )
                                        if (isCompoundLocked) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            ProBadge()
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Monthly / Yearly Interest Buttons & Interest Rate Box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "Interest Period",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BackgroundClean)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .padding(2.dp)
                                ) {
                                    listOf("MONTHLY" to "Monthly", "YEARLY" to "Yearly").forEach { (periodKey, label) ->
                                        val isSel = interestPeriod == periodKey
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) BluePrimary else Color.Transparent)
                                                .clickable { interestPeriod = periodKey }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(0.9f)) {
                                Text(
                                    text = "Interest Rate Box",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = interestRateStr,
                                    onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' } },
                                    placeholder = { Text("0.0", fontSize = 13.sp) },
                                    trailingIcon = { Text("%", fontWeight = FontWeight.Bold, color = BluePrimary, modifier = Modifier.padding(end = 12.dp)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = BorderSubtle
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Starting Date of Interest & Days / Month Based
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.1f)) {
                                Text(
                                    text = "Starting Date of Interest",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = interestStartDateStr,
                                    onValueChange = { interestStartDateStr = it },
                                    placeholder = { Text("YYYY-MM-DD", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = BorderSubtle
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(0.9f)) {
                                Text(
                                    text = "Calculation Basis",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BackgroundClean)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                        .padding(2.dp)
                                ) {
                                    listOf("DAYS_BASED" to "Days", "MONTH_BASED" to "Month").forEach { (basisKey, label) ->
                                        val isSel = interestBasis == basisKey
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) BluePrimary else Color.Transparent)
                                                .clickable { interestBasis = basisKey }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) Color.White else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Calculation Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundClean),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Principal Amount", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCurrency(principal, currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Calculated Interest", fontSize = 11.sp, color = TextSecondary)
                                Text(formatCurrency(calcResult.totalInterest, currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = FinanceGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = BorderSubtle)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Net Amount Due", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    formatCurrency(calcResult.totalAmountDue, currencySymbol),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (direction == "GIVEN") FinanceGreen else FinanceRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Notes
                    Text(
                        text = "Notes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Terms, promissory note reference, purpose...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 8. Attachments
                    Text(
                        text = "Attachments",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Button that triggers the picker dialog
                    OutlinedButton(
                        onClick = {
                            if (!isProPlan) {
                                lockedFeatureTitle = "Photo Attachments"
                                lockedFeatureDesc = "Photo attachments on transactions and loans is a FlowVault Pro feature.\n\nIn Free Tier, photo attachments are disabled. Upgrade to Pro (₹99/mo) to attach cheques, promissory notes, and receipt images to your loans and transactions."
                                showProFeatureLockedDialog = true
                            } else {
                                showAttachmentSourceDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cheque_promissory_note_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, BluePrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BlueLight.copy(alpha = 0.25f),
                            contentColor = BluePrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BluePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AutoShrinkButtonText(
                            text = if (attachedBitmap != null || attachedDocPath != null) {
                                "Change Attachments"
                            } else {
                                "Attachments"
                            },
                            maxFontSize = 13.sp,
                            minFontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        if (!isProPlan) {
                            Spacer(modifier = Modifier.width(6.dp))
                            ProBadge()
                        }
                    }

                    // Selection Dialog: Select from Gallery or Take from Camera
                    if (showAttachmentSourceDialog) {
                        AlertDialog(
                            onDismissRequest = { showAttachmentSourceDialog = false },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Attachments",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Attach document from gallery or take with camera:",
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Option 1: Select from Gallery
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                showAttachmentSourceDialog = false
                                                galleryLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                            .testTag("select_from_gallery_option"),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(BlueLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = BluePrimary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Select from Gallery",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Choose cheque or promissory note from device",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }

                                    // Option 2: Take from Camera
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                showAttachmentSourceDialog = false
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                    cameraLauncher.launch(null)
                                                } else {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            }
                                            .testTag("take_from_camera_option"),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                        border = BorderStroke(1.dp, BorderSubtle),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(TealLight),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PhotoCamera,
                                                    contentDescription = null,
                                                    tint = FinanceGreen,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Take from Camera",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Snap a photo using device camera",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = { showAttachmentSourceDialog = false }) {
                                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                    }

                    // Attached Document Thumbnail Preview
                    if (attachedBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = attachedBitmap!!.asImageBitmap(),
                                    contentDescription = "Attached Cheque / Promissory Note",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FinanceGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Document Attached",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FinanceGreen
                                        )
                                    }
                                    Text(
                                        text = "Cheque / Promissory note photo saved",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        attachedBitmap = null
                                        attachedDocPath = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove attachment",
                                        tint = FinanceRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = FinanceRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 9. Save Button
                    PrimaryBlueButton(
                        text = "Save Loan",
                        onClick = {
                            if (borrowerName.isBlank()) {
                                errorMessage = "Please enter borrower / lender name"
                                return@PrimaryBlueButton
                            }
                            if (principal <= 0) {
                                errorMessage = "Loan amount must be greater than 0"
                                return@PrimaryBlueButton
                            }

                            val finalAttachments = attachedDocPath ?: ""

                            onConfirm(
                                borrowerName,
                                phone,
                                email,
                                principal,
                                annualRate,
                                if (addInterestOnThisLoan) interestType else "ZERO",
                                approxMonths,
                                selectedAccountId,
                                notes,
                                direction,
                                interestPeriod,
                                interestBasis,
                                addInterestOnThisLoan,
                                tagsStr,
                                startDateMillis,
                                dueDateMillis,
                                interestStartDateMillis,
                                finalAttachments
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        testTag = "submit_loan_button"
                    )
                }
            }
        }
    }

    if (showSelectBorrowerDialog) {
        SelectBorrowerDialog(
            borrowers = borrowers,
            direction = direction,
            onDismiss = { showSelectBorrowerDialog = false },
            onBorrowerSelected = { selected ->
                borrowerName = selected.displayName
                phone = selected.phone
                email = selected.email
                showSelectBorrowerDialog = false
            },
            onSaveBorrower = { newBorrower ->
                onSaveBorrower(newBorrower)
            }
        )
    }

    if (showProFeatureLockedDialog) {
        ProFeatureLockedDialog(
            featureTitle = lockedFeatureTitle,
            featureDescription = lockedFeatureDesc,
            onDismiss = { showProFeatureLockedDialog = false },
            onUpgrade = {
                showProFeatureLockedDialog = false
                onRequestUpgrade()
            }
        )
    }
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
    return try {
        val dir = File(context.filesDir, "loan_documents")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "cheque_doc_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        "doc_${System.currentTimeMillis()}.jpg"
    }
}

private fun saveImageToInternalStorage(context: Context, uri: Uri): String {
    return try {
        val dir = File(context.filesDir, "loan_documents")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "cheque_doc_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        uri.toString()
    }
}
