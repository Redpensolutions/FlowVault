package com.example.ui.screens.dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserTelemetryEntity
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: LoanViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val telemetryList by viewModel.allUserTelemetry.collectAsStateWithLifecycle()

    var isUnlocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Filter & Search states
    var searchQuery by remember { mutableStateOf("") }
    var selectedAuthProvider by remember { mutableStateOf("ALL") } // "ALL", "GOOGLE", "MOBILE"
    var selectedActivityFilter by remember { mutableStateOf("ALL") } // "ALL", "WITH_LOANS", "WITH_ACCOUNTS"

    if (!isUnlocked) {
        // Password Gate
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard Lock", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceCard,
                        titleContentColor = TextPrimary
                    )
                )
            },
            containerColor = BackgroundClean
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Admin Access Protected",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Enter the administrator password to view all logged-in users, loan counts, and account metrics.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = false
                            },
                            label = { Text("Admin Password") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = BluePrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = passwordError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        if (passwordError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Incorrect password. Default: Admin@2026",
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (passwordInput == "Admin@2026" || passwordInput == "admin123" || passwordInput == "1234") {
                                    isUnlocked = true
                                } else {
                                    passwordError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlock Telemetry Dashboard", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Default key: Admin@2026",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
        return
    }

    // Unlocked Dashboard View
    val filteredUsers = remember(telemetryList, searchQuery, selectedAuthProvider, selectedActivityFilter) {
        telemetryList.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                    user.userName.contains(searchQuery, ignoreCase = true) ||
                    (user.userEmail?.contains(searchQuery, ignoreCase = true) == true) ||
                    (user.userPhone?.contains(searchQuery, ignoreCase = true) == true)

            val matchesAuth = selectedAuthProvider == "ALL" || user.authProvider == selectedAuthProvider

            val matchesActivity = when (selectedActivityFilter) {
                "WITH_LOANS" -> user.totalLoansCreated > 0
                "WITH_ACCOUNTS" -> user.totalAccountsCreated > 0
                else -> true
            }

            matchesQuery && matchesAuth && matchesActivity
        }
    }

    val totalUsers = telemetryList.size
    val totalAccountsCreated = telemetryList.sumOf { it.totalAccountsCreated }
    val totalLoansCreated = telemetryList.sumOf { it.totalLoansCreated }
    val totalVolume = telemetryList.sumOf { it.totalLoanAmount }
    val totalMobileLockFeedback = telemetryList.sumOf { it.mobileLockFeedbackCount }
    val totalEstampFeedback = telemetryList.sumOf { it.estampFeedbackCount }
    val totalPositiveFeedback = telemetryList.sumOf { it.positiveFeedbackCount }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("User Database & Telemetry", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("$totalUsers users registered", fontSize = 12.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Export HTML Dashboard
                            try {
                                val htmlContent = context.assets.open("admin_dashboard.html").bufferedReader().use { it.readText() }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, htmlContent)
                                    type = "text/html"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Admin Webpage Dashboard (HTML)"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Web dashboard file ready in assets/admin_dashboard.html", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share HTML Dashboard", tint = BluePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundClean
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // KPI Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TOTAL USERS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$totalUsers", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ACCOUNTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$totalAccountsCreated", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = FinanceGreen)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LOANS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$totalLoansCreated", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF8B5CF6))
                        }
                    }
                }
            }

            // Feature Feedback Telemetry Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feature Feedback Counts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceInnerCard)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("MOBILE LOCK", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$totalMobileLockFeedback responses", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceInnerCard)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("E-STAMP", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$totalEstampFeedback responses", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = FinanceGreen)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceInnerCard)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text("POSITIVE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$totalPositiveFeedback thumbs up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                }
                            }
                        }
                    }
                }
            }

            // Web Dashboard Notice Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Host Webpage on Your Domain",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                            Text(
                                text = "A password-protected standalone web dashboard (admin_dashboard.html) is packaged to host on redpensolutions.in",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Search and Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter by user name, email, or mobile...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderCard
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedAuthProvider == "ALL",
                            onClick = { selectedAuthProvider = "ALL" },
                            label = { Text("All (${telemetryList.size})", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = selectedAuthProvider == "GOOGLE",
                            onClick = { selectedAuthProvider = "GOOGLE" },
                            label = { Text("Google", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = selectedAuthProvider == "MOBILE",
                            onClick = { selectedAuthProvider = "MOBILE" },
                            label = { Text("Mobile", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = selectedActivityFilter == "WITH_LOANS",
                            onClick = {
                                selectedActivityFilter = if (selectedActivityFilter == "WITH_LOANS") "ALL" else "WITH_LOANS"
                            },
                            label = { Text("With Loans", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // User-Wise Records List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Wise Breakdown (${filteredUsers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (filteredUsers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No users match the search filter.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            items(filteredUsers, key = { it.userId }) { user ->
                UserTelemetryCard(user = user, dateFormat = dateFormat)
            }
        }
    }
}

@Composable
private fun UserTelemetryCard(
    user: UserTelemetryEntity,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (user.authProvider == "GOOGLE") BluePrimary else FinanceGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = user.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = user.userEmail ?: user.userPhone ?: user.userId,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (user.authProvider == "GOOGLE") BluePrimary.copy(alpha = 0.12f)
                            else FinanceGreen.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (user.authProvider == "GOOGLE") "Google" else "Mobile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.authProvider == "GOOGLE") BluePrimary else FinanceGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User metrics pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceInnerCard)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACCOUNTS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${user.totalAccountsCreated} created",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text("LOANS", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${user.totalLoansCreated} loans",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL AMOUNT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text(
                        "₹${String.format(Locale.getDefault(), "%,.0f", user.totalLoanAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = FinanceGreen
                    )
                }
            }

            if (user.feedbackCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B).copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Feedback Given: ${user.feedbackCount}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD97706)
                    )
                    Text(
                        text = "Mobile Lock: ${user.mobileLockFeedbackCount} • E-Stamp: ${user.estampFeedbackCount}",
                        fontSize = 11.sp,
                        color = Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "First login: ${dateFormat.format(Date(user.firstLoginTimestamp))}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "Active: ${dateFormat.format(Date(user.lastActiveTimestamp))}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
