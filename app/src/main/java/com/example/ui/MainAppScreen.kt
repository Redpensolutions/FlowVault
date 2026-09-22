package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Loan
import com.example.ui.components.AddLoanDialog
import com.example.ui.components.AddVirtualAccountDialog
import com.example.ui.components.AppLockScreen
import com.example.ui.components.LoanDetailsDialog
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.TransferFundsDialog
import java.util.Locale
import com.example.ui.screens.dashboard.AdminDashboardScreen
import com.example.ui.screens.BorrowerDetailScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoanParticularDetailScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VirtualAccountsScreen
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LoanViewModel
import kotlinx.coroutines.launch

enum class AppSection(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    VIRTUAL_ACCOUNTS("Accounts", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, "nav_virtual_accounts"),
    LOANS("Loans", Icons.Filled.Payments, Icons.Outlined.Payments, "nav_loans"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@Composable
fun MainAppScreen(viewModel: LoanViewModel) {
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    var showNamePromptDialog by remember { mutableStateOf(false) }
    var enteredProfileName by remember { mutableStateOf("") }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppMinimized()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!userSession.isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                val currentName = viewModel.userName.value
                if (currentName.startsWith("User") || currentName.isBlank()) {
                    enteredProfileName = ""
                    showNamePromptDialog = true
                }
            }
        )
        return
    }

    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val pinEnabled by viewModel.pinSecurityEnabled.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricSecurityEnabled.collectAsStateWithLifecycle()
    val faceUnlockEnabled by viewModel.faceUnlockEnabled.collectAsStateWithLifecycle()
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()

    if (isAppLocked) {
        AppLockScreen(
            savedPin = securityPin,
            pinEnabled = pinEnabled,
            biometricEnabled = biometricEnabled,
            faceUnlockEnabled = faceUnlockEnabled,
            onUnlockSuccess = {
                viewModel.unlockApp()
            }
        )
        return
    }

    var currentSection by remember { mutableStateOf(AppSection.DASHBOARD) }
    var showAdminDashboard by remember { mutableStateOf(false) }

    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val virtualAccounts by viewModel.allVirtualAccounts.collectAsStateWithLifecycle()
    val currency by viewModel.defaultCurrency.collectAsStateWithLifecycle()
    val defaultInterestType by viewModel.defaultInterestType.collectAsStateWithLifecycle()

    val overdueCount = allLoans.count { it.isOverdue }

    // Dialog visibility states
    var showAddLoanDialog by remember { mutableStateOf(false) }
    var addLoanDirection by remember { mutableStateOf("GIVEN") }
    var preselectedBorrowerName by remember { mutableStateOf<String?>(null) }
    var loanForPayment by remember { mutableStateOf<Loan?>(null) }
    var isPaymentFromQuickOperations by remember { mutableStateOf(false) }
    var loanForDetails by remember { mutableStateOf<Loan?>(null) }
    var showAddVirtualAccountDialog by remember { mutableStateOf(false) }
    var transferSourceId by remember { mutableStateOf<Long?>(null) }
    var showTransferFundsDialog by remember { mutableStateOf(false) }
    var selectedBorrowerForDetail by remember { mutableStateOf<String?>(null) }
    var showNotificationsScreen by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundClean)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Responsive Tablet / Foldable Layout: Side Navigation Rail + Main View
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = SurfaceCard,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    header = {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LOAN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BluePrimary
                        )
                        Text(
                            text = "TRACKER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                ) {
                    AppSection.entries.forEach { section ->
                        val isSelected = currentSection == section
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { currentSection = section },
                            label = { Text(section.title, fontSize = 11.sp) },
                            icon = {
                                if (section == AppSection.LOANS && overdueCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = FinanceRed) {
                                                Text("$overdueCount")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                                            contentDescription = section.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                                        contentDescription = section.title
                                    )
                                }
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = BluePrimary,
                                selectedTextColor = BluePrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = BlueLight
                            ),
                            modifier = Modifier.testTag(section.testTag)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    AppContentSection(
                        section = currentSection,
                        viewModel = viewModel,
                        onNavigateToLoans = {
                            viewModel.setLoanStatusFilter("ALL")
                            currentSection = AppSection.LOANS
                        },
                        onNavigateToOverdueLoans = {
                            viewModel.setLoanStatusFilter("OVERDUE")
                            currentSection = AppSection.LOANS
                        },
                        onOpenAddLoan = { dir ->
                            addLoanDirection = dir
                            preselectedBorrowerName = null
                            showAddLoanDialog = true
                        },
                        onOpenRecordPayment = {
                            val activeLoan = allLoans.firstOrNull { it.balanceRemaining > 0 } ?: allLoans.firstOrNull()
                            if (activeLoan != null) {
                                isPaymentFromQuickOperations = true
                                loanForPayment = activeLoan
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No active loans with outstanding debt to pay.")
                                }
                            }
                        },
                        onOpenAddVirtualAccount = { showAddVirtualAccountDialog = true },
                        onSelectLoanForDetails = { loanForDetails = it },
                        onSelectLoanForPayment = {
                            isPaymentFromQuickOperations = false
                            loanForPayment = it
                        },
                        onOpenTransferFunds = { sourceId ->
                            transferSourceId = sourceId
                            showTransferFundsDialog = true
                        },
                        onSelectBorrower = { borrowerName ->
                            selectedBorrowerForDetail = borrowerName
                        },
                        onNavigateToNotifications = {
                            showNotificationsScreen = true
                        },
                        onOpenAdminDashboard = {
                            showAdminDashboard = true
                        }
                    )
                }
            }
        } else {
            // Standard Mobile Layout: Bottom NavigationBar
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                containerColor = BackgroundClean,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceCard,
                        contentColor = TextPrimary,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("main_bottom_nav")
                    ) {
                        AppSection.entries.forEach { section ->
                            val isSelected = currentSection == section
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentSection = section },
                                label = {
                                    Text(
                                        text = section.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                icon = {
                                    if (section == AppSection.LOANS && overdueCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = FinanceRed) {
                                                    Text("$overdueCount")
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                                                contentDescription = section.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                                            contentDescription = section.title
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = BluePrimary,
                                    selectedTextColor = BluePrimary,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = BlueLight
                                ),
                                modifier = Modifier.testTag(section.testTag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AppContentSection(
                        section = currentSection,
                        viewModel = viewModel,
                        onNavigateToLoans = {
                            viewModel.setLoanStatusFilter("ALL")
                            currentSection = AppSection.LOANS
                        },
                        onNavigateToOverdueLoans = {
                            viewModel.setLoanStatusFilter("OVERDUE")
                            currentSection = AppSection.LOANS
                        },
                        onOpenAddLoan = { dir ->
                            addLoanDirection = dir
                            preselectedBorrowerName = null
                            showAddLoanDialog = true
                        },
                        onOpenRecordPayment = {
                            val activeLoan = allLoans.firstOrNull { it.balanceRemaining > 0 } ?: allLoans.firstOrNull()
                            if (activeLoan != null) {
                                isPaymentFromQuickOperations = true
                                loanForPayment = activeLoan
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No active loans with outstanding debt.")
                                }
                            }
                        },
                        onOpenAddVirtualAccount = { showAddVirtualAccountDialog = true },
                        onSelectLoanForDetails = { loanForDetails = it },
                        onSelectLoanForPayment = {
                            isPaymentFromQuickOperations = false
                            loanForPayment = it
                        },
                        onOpenTransferFunds = { sourceId ->
                            transferSourceId = sourceId
                            showTransferFundsDialog = true
                        },
                        onSelectBorrower = { borrowerName ->
                            selectedBorrowerForDetail = borrowerName
                        },
                        onNavigateToNotifications = {
                            showNotificationsScreen = true
                        },
                        onOpenAdminDashboard = {
                            showAdminDashboard = true
                        }
                    )
                }
            }
        }
    }

    // Borrower Detail Screen overlay
    selectedBorrowerForDetail?.let { borrowerName ->
        BorrowerDetailScreen(
            borrowerName = borrowerName,
            viewModel = viewModel,
            onBack = { selectedBorrowerForDetail = null },
            onOpenAddLoanForBorrower = { bName ->
                val bLoan = allLoans.firstOrNull { it.borrowerName.equals(bName, ignoreCase = true) }
                addLoanDirection = bLoan?.direction ?: "GIVEN"
                preselectedBorrowerName = bName
                showAddLoanDialog = true
            },
            onOpenPaymentForLoan = { loan ->
                isPaymentFromQuickOperations = false
                loanForPayment = loan
            }
        )
    }

    // Modal Dialogs
    if (showAddLoanDialog) {
        val borrowersList by viewModel.allBorrowers.collectAsStateWithLifecycle()
        AddLoanDialog(
            virtualAccounts = virtualAccounts,
            borrowers = borrowersList,
            currencySymbol = currency,
            defaultInterestType = defaultInterestType,
            initialDirection = addLoanDirection,
            initialBorrowerName = preselectedBorrowerName ?: "",
            onDismiss = {
                showAddLoanDialog = false
                preselectedBorrowerName = null
            },
            onSaveBorrower = { b -> viewModel.createBorrower(b) },
            onConfirm = { name, phone, email, principal, rate, type, months, accountId, notes, direction, interestPeriod, interestBasis, hasInterest, tags, startDate, dueDate, interestStartDate, attachments ->
                viewModel.createLoan(
                    borrowerName = name,
                    borrowerPhone = phone,
                    borrowerEmail = email,
                    principal = principal,
                    rate = rate,
                    type = type,
                    termMonths = months,
                    disbursementAccountId = accountId,
                    notes = notes,
                    direction = direction,
                    interestPeriod = interestPeriod,
                    interestBasis = interestBasis,
                    hasInterest = hasInterest,
                    tags = tags,
                    startDate = startDate,
                    dueDate = dueDate,
                    interestStartDate = interestStartDate,
                    attachments = attachments,
                    onComplete = {
                        showAddLoanDialog = false
                        preselectedBorrowerName = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (direction == "GIVEN") "Given loan successfully recorded." else "Taken loan successfully recorded."
                            )
                        }
                    }
                )
            }
        )
    }

    loanForPayment?.let { loan ->
        RecordPaymentDialog(
            loan = loan,
            virtualAccounts = virtualAccounts,
            currencySymbol = currency,
            allLoans = allLoans,
            allowBorrowerSelection = isPaymentFromQuickOperations,
            onDismiss = {
                loanForPayment = null
                isPaymentFromQuickOperations = false
            },
            onConfirmPayment = { targetLoan, amount, vaId, notes, towardsPrincipal, towardsInterest, penalty, mode, date ->
                viewModel.recordPayment(
                    loanId = targetLoan.id,
                    amount = amount,
                    virtualAccountId = vaId,
                    notes = notes,
                    towardsPrincipal = towardsPrincipal,
                    towardsInterest = towardsInterest,
                    penaltyAmount = penalty,
                    paymentMode = mode,
                    paidDate = date,
                    onComplete = {
                        loanForPayment = null
                        isPaymentFromQuickOperations = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Repayment of $currency${String.format(Locale.US, "%,.2f", amount)} posted for ${targetLoan.borrowerName}.")
                        }
                    }
                )
            }
        )
    }

    loanForDetails?.let { loan ->
        BackHandler { loanForDetails = null }
        LoanParticularDetailScreen(
            loan = loan,
            currencySymbol = currency,
            viewModel = viewModel,
            onBack = { loanForDetails = null },
            onRecordPayment = {
                val current = loan
                loanForDetails = null
                isPaymentFromQuickOperations = false
                loanForPayment = current
            },
            onTopUp = {
                val current = loan
                loanForDetails = null
                // Open payment or top up
                isPaymentFromQuickOperations = false
                loanForPayment = current
            }
        )
    }

    if (showAddVirtualAccountDialog) {
        AddVirtualAccountDialog(
            currencySymbol = currency,
            onDismiss = { showAddVirtualAccountDialog = false },
            onConfirm = { name, code, poolType, balance, description ->
                viewModel.createVirtualAccount(
                    name = name,
                    code = code,
                    poolType = poolType,
                    balance = balance,
                    description = description,
                    onComplete = {
                        showAddVirtualAccountDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Virtual Account pool $code opened.")
                        }
                    }
                )
            }
        )
    }

    if (showTransferFundsDialog) {
        TransferFundsDialog(
            accounts = virtualAccounts,
            currencySymbol = currency,
            preselectedSourceId = transferSourceId,
            onDismiss = {
                showTransferFundsDialog = false
                transferSourceId = null
            },
            onConfirmTransfer = { sourceId, destId, amount, reason ->
                viewModel.transferBetweenAccounts(
                    sourceId = sourceId,
                    destId = destId,
                    amount = amount,
                    reason = reason,
                    onComplete = { success ->
                        showTransferFundsDialog = false
                        transferSourceId = null
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Transferred $currency$amount successfully.")
                            } else {
                                snackbarHostState.showSnackbar("Transfer failed: check account balance.")
                            }
                        }
                    }
                )
            }
        )
    }

    // Notifications Screen Overlay
    if (showNotificationsScreen) {
        BackHandler { showNotificationsScreen = false }
        NotificationsScreen(
            viewModel = viewModel,
            onBack = { showNotificationsScreen = false }
        )
    }

    // Admin & User Analytics Dashboard Overlay
    if (showAdminDashboard) {
        BackHandler { showAdminDashboard = false }
        AdminDashboardScreen(
            viewModel = viewModel,
            onBack = { showAdminDashboard = false }
        )
    }

    // Post-Login Set Name Prompt Dialog (Asked after login, not before)
    if (showNamePromptDialog) {
        AlertDialog(
            onDismissRequest = { showNamePromptDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Welcome to FlowVault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "What is your name? (You can also change or update this anytime later in Settings).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = enteredProfileName,
                        onValueChange = { enteredProfileName = it },
                        label = { Text("Your Full Name") },
                        placeholder = { Text("e.g. Rahul Sharma") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_login_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredProfileName.isNotBlank()) {
                            viewModel.updateUserProfile(
                                name = enteredProfileName.trim(),
                                email = userSession.userEmail ?: "",
                                phone = userSession.userPhone ?: ""
                            )
                        }
                        showNamePromptDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Save Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNamePromptDialog = false }) {
                    Text("Skip for Now")
                }
            }
        )
    }
}

@Composable
fun AppContentSection(
    section: AppSection,
    viewModel: LoanViewModel,
    onNavigateToLoans: () -> Unit,
    onNavigateToOverdueLoans: () -> Unit,
    onOpenAddLoan: (direction: String) -> Unit,
    onOpenRecordPayment: () -> Unit,
    onOpenAddVirtualAccount: () -> Unit,
    onSelectLoanForDetails: (Loan) -> Unit,
    onSelectLoanForPayment: (Loan) -> Unit,
    onOpenTransferFunds: (sourceId: Long?) -> Unit,
    onSelectBorrower: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {}
) {
    AnimatedContent(
        targetState = section,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SectionTransition"
    ) { target ->
        when (target) {
            AppSection.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToLoans = onNavigateToLoans,
                onNavigateToOverdueLoans = onNavigateToOverdueLoans,
                onOpenAddLoan = { onOpenAddLoan("GIVEN") },
                onOpenRecordPayment = onOpenRecordPayment,
                onOpenAddVirtualAccount = onOpenAddVirtualAccount,
                onNavigateToNotifications = onNavigateToNotifications
            )
            AppSection.VIRTUAL_ACCOUNTS -> VirtualAccountsScreen(
                viewModel = viewModel,
                onOpenAddVirtualAccount = onOpenAddVirtualAccount,
                onOpenTransferFunds = onOpenTransferFunds
            )
            AppSection.LOANS -> LoansScreen(
                viewModel = viewModel,
                onOpenAddLoan = onOpenAddLoan,
                onSelectLoanForDetails = onSelectLoanForDetails,
                onSelectLoanForPayment = onSelectLoanForPayment,
                onSelectBorrower = onSelectBorrower
            )
            AppSection.SETTINGS -> SettingsScreen(
                viewModel = viewModel,
                onOpenAdminDashboard = onOpenAdminDashboard
            )
        }
    }
}
