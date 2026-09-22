package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderCard
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceInnerCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel
import kotlinx.coroutines.launch

data class GoogleAccountItem(
    val email: String,
    val displayName: String,
    val initial: String
)

fun getPhoneGoogleAccounts(context: Context): List<GoogleAccountItem> {
    val result = mutableListOf<GoogleAccountItem>()
    val seenEmails = mutableSetOf<String>()

    try {
        val am = AccountManager.get(context)
        val accounts = am.getAccountsByType("com.google")
        for (acc in accounts) {
            val email = acc.name?.trim() ?: continue
            if (email.contains("@") && seenEmails.add(email.lowercase())) {
                val name = email.substringBefore("@")
                    .replace('.', ' ')
                    .replace('_', ' ')
                    .split(' ')
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                    .ifBlank { "Google User" }
                val initial = name.firstOrNull()?.uppercase() ?: "G"
                result.add(GoogleAccountItem(email, name, initial))
            }
        }
    } catch (_: Exception) {
        // Ignored or permission restricted
    }

    // Always ensure the project's primary user account is in the list
    if (seenEmails.add("admin@redpensolutions.in")) {
        result.add(
            0,
            GoogleAccountItem(
                email = "admin@redpensolutions.in",
                displayName = "Admin User",
                initial = "A"
            )
        )
    }

    return result
}

@Composable
fun GoogleGLogo(modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val sizePx = size.minDimension
        val stroke = sizePx * 0.22f
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = (sizePx - stroke) / 2f
        val rect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)

        // Google Red (top curve)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 190f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Google Yellow (left curve)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 70f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Google Green (bottom curve)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Google Blue (right curve)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -35f,
            sweepAngle = 65f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Butt),
            topLeft = rect.topLeft,
            size = rect.size
        )
        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x - 2f, center.y),
            end = Offset(center.x + radius + stroke / 2f, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Square
        )
    }
}

@Composable
fun LoginScreen(
    viewModel: LoanViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    // Mobile Phone input state
    var countryCode by remember { mutableStateOf("+91") }
    var showCountryDropdown by remember { mutableStateOf(false) }
    var mobileInput by remember { mutableStateOf("9876543210") }

    // Google Account Picker dialog state
    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var showCustomEmailField by remember { mutableStateOf(false) }
    var customEmailInput by remember { mutableStateOf("") }

    // If Google account is selected before mobile number is entered
    var pendingGoogleAccount by remember { mutableStateOf<GoogleAccountItem?>(null) }
    var showMobilePromptDialog by remember { mutableStateOf(false) }
    var mobilePromptInput by remember { mutableStateOf("") }

    // Function to perform Google login
    fun performGoogleLogin(account: GoogleAccountItem, phone: String) {
        val cleanDigits = phone.filter { it.isDigit() }
        val fullPhone = if (cleanDigits.isNotEmpty()) "$countryCode $cleanDigits" else null

        isLoading = true
        scope.launch {
            viewModel.loginWithGoogle(
                email = account.email,
                name = account.displayName,
                phone = fullPhone
            )
            isLoading = false
            Toast.makeText(context, "Logged in as ${account.email}", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    // Handler when an account is clicked
    fun onAccountSelected(account: GoogleAccountItem) {
        showAccountPickerDialog = false
        val cleanDigits = mobileInput.filter { it.isDigit() }
        if (cleanDigits.length >= 8) {
            performGoogleLogin(account, mobileInput)
        } else {
            pendingGoogleAccount = account
            mobilePromptInput = mobileInput
            showMobilePromptDialog = true
        }
    }

    // Native Android system account picker launcher
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountEmail.isNullOrBlank()) {
                val derivedName = accountEmail.substringBefore("@")
                    .replace('.', ' ')
                    .replace('_', ' ')
                    .split(' ')
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                    .ifBlank { "Google User" }
                val item = GoogleAccountItem(
                    email = accountEmail,
                    displayName = derivedName,
                    initial = derivedName.firstOrNull()?.uppercase() ?: "G"
                )
                onAccountSelected(item)
            }
        }
    }

    val launchSystemAccountPicker: () -> Unit = {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    null,
                    null,
                    null,
                    null
                )
            } else {
                @Suppress("DEPRECATION")
                AccountManager.newChooseAccountIntent(
                    null, null, arrayOf("com.google"), false, null, null, null, null
                )
            }
            accountPickerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Showing available Google accounts list", Toast.LENGTH_SHORT).show()
            showAccountPickerDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundClean)
            .statusBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Banner with Logo
            Image(
                painter = painterResource(id = com.example.R.drawable.ic_launcher_icon),
                contentDescription = "FlowVault Logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "FlowVault",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Lend easily. Track accurately. Store safely.",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Choose your Google account from your phone. Enter your mobile number to link with your vault.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mobile Number Input Section (Asked while login)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Your Mobile Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box {
                                OutlinedTextField(
                                    value = countryCode,
                                    onValueChange = { countryCode = it },
                                    label = { Text("Code") },
                                    readOnly = false,
                                    trailingIcon = {
                                        IconButton(onClick = { showCountryDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Country Code")
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(98.dp)
                                        .testTag("google_country_code_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BluePrimary,
                                        unfocusedBorderColor = BorderCard
                                    )
                                )

                                DropdownMenu(
                                    expanded = showCountryDropdown,
                                    onDismissRequest = { showCountryDropdown = false }
                                ) {
                                    val countryList = listOf(
                                        "+91" to "India (+91)",
                                        "+1" to "US / Canada (+1)",
                                        "+44" to "UK (+44)",
                                        "+971" to "UAE (+971)",
                                        "+65" to "Singapore (+65)",
                                        "+61" to "Australia (+61)"
                                    )
                                    countryList.forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 13.sp) },
                                            onClick = {
                                                countryCode = code
                                                showCountryDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = mobileInput,
                                onValueChange = { mobileInput = it.filter { ch -> ch.isDigit() || ch == ' ' } },
                                label = { Text("Mobile Number") },
                                placeholder = { Text("98765 43210") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = FinanceGreen)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("google_mobile_input"),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderCard
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Continue with Google Button (Clicking opens phone's logged-in accounts)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(14.dp))
                            .clickable(enabled = !isLoading) {
                                showAccountPickerDialog = true
                            }
                            .testTag("google_login_button"),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = BluePrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                GoogleGLogo(modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3C4043)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tap to choose from the Google accounts logged into this phone.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Security Footnote
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End AES-256 Encrypted Ledger System",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }

    // Google Account Picker Dialog (Displays logged-in accounts on phone)
    if (showAccountPickerDialog) {
        val phoneAccounts = remember(context) { getPhoneGoogleAccounts(context) }

        AlertDialog(
            onDismissRequest = {
                showAccountPickerDialog = false
                showCustomEmailField = false
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GoogleGLogo(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Choose an account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF202124),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "to continue to FlowVault",
                        fontSize = 13.sp,
                        color = Color(0xFF5F6368),
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE8EAED), modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Accounts on this phone:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5F6368),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    phoneAccounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onAccountSelected(account)
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("account_item_${account.email}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.initial,
                                    color = Color(0xFF1967D2),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF202124),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = account.email,
                                    fontSize = 12.sp,
                                    color = Color(0xFF5F6368),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Option: "+ Use another account"
                    if (!showCustomEmailField) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCustomEmailField = true }
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                                .testTag("btn_use_another_account"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F3F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF5F6368),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Use another account",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF202124)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            OutlinedTextField(
                                value = customEmailInput,
                                onValueChange = { customEmailInput = it },
                                label = { Text("Google Email") },
                                placeholder = { Text("name@gmail.com") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_email_input"),
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    if (customEmailInput.isNotBlank() && customEmailInput.contains("@")) {
                                        val derived = customEmailInput.substringBefore("@")
                                            .replace('.', ' ')
                                            .replace('_', ' ')
                                            .split(' ')
                                            .filter { it.isNotBlank() }
                                            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                                            .ifBlank { "User" }
                                        val customItem = GoogleAccountItem(
                                            email = customEmailInput.trim(),
                                            displayName = derived,
                                            initial = derived.firstOrNull()?.uppercase() ?: "G"
                                        )
                                        onAccountSelected(customItem)
                                    } else {
                                        Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Select this account", fontSize = 13.sp)
                            }
                        }
                    }

                    // System account picker launcher button
                    TextButton(
                        onClick = {
                            showAccountPickerDialog = false
                            launchSystemAccountPicker()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_system_account_picker")
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Android System Account Chooser", fontSize = 12.sp, color = BluePrimary)
                    }

                    HorizontalDivider(color = Color(0xFFE8EAED), modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "To continue, Google will share your name and email address with FlowVault.",
                        fontSize = 11.sp,
                        color = Color(0xFF70757A),
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showAccountPickerDialog = false
                    showCustomEmailField = false
                }) {
                    Text("Cancel", color = Color(0xFF5F6368))
                }
            }
        )
    }

    // Prompt for mobile number if user picked an account without entering phone number first
    if (showMobilePromptDialog && pendingGoogleAccount != null) {
        val account = pendingGoogleAccount!!
        AlertDialog(
            onDismissRequest = { showMobilePromptDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Mobile Number", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selected: ${account.email}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )
                    Text(
                        text = "Please enter your mobile number to complete signing into your vault:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = countryCode,
                            onValueChange = { countryCode = it },
                            label = { Text("Code") },
                            modifier = Modifier.width(86.dp),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = mobilePromptInput,
                            onValueChange = { mobilePromptInput = it.filter { ch -> ch.isDigit() || ch == ' ' } },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("98765 43210") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prompt_mobile_input"),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanDigits = mobilePromptInput.filter { it.isDigit() }
                        if (cleanDigits.length < 8) {
                            Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        mobileInput = mobilePromptInput
                        showMobilePromptDialog = false
                        performGoogleLogin(account, mobilePromptInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Complete Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMobilePromptDialog = false }) {
                    Text("Back")
                }
            }
        )
    }
}
