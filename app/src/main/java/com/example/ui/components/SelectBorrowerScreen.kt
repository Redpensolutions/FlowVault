package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.Borrower
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

enum class BorrowerPickerViewMode {
    SAVED_LIST,
    ADD_CONTACT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectBorrowerDialog(
    borrowers: List<Borrower>,
    direction: String = "GIVEN", // "GIVEN" or "TAKEN"
    onDismiss: () -> Unit,
    onBorrowerSelected: (Borrower) -> Unit,
    onSaveBorrower: (Borrower) -> Unit
) {
    val context = LocalContext.current
    var viewMode by remember { mutableStateOf(BorrowerPickerViewMode.SAVED_LIST) }
    var searchQuery by remember { mutableStateOf("") }

    // Pre-filled state for Add Contact form
    var formFullName by remember { mutableStateOf("") }
    var formNickName by remember { mutableStateOf("") }
    var formMobile1 by remember { mutableStateOf("") }
    var formMobile2 by remember { mutableStateOf("") }
    var formEmail by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formNote by remember { mutableStateOf("") }
    var formIdDocPath by remember { mutableStateOf<String?>(null) }
    var formIdDocBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Validation state
    var mobile1Error by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    // Document attachment picker states
    var showIdAttachmentDialog by remember { mutableStateOf(false) }

    val idGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    formIdDocBitmap = BitmapFactory.decodeStream(stream)
                }
                formIdDocPath = saveUriToInternalStorage(context, uri, "borrower_id_docs")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val idCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            formIdDocBitmap = bitmap
            try {
                formIdDocPath = saveBitmapToInternalStorage(context, bitmap, "borrower_id_docs")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val idCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            idCameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to take identity document photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Contact Picker for "Add Borrower from Contacts"
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val (cName, cPhone) = queryContactFromUri(context, uri)
            formFullName = cName
            formNickName = cName.trim().split("\\s+".toRegex()).firstOrNull() ?: cName
            formMobile1 = cPhone.replace("[^0-9+]".toRegex(), "")
            formMobile2 = ""
            formEmail = ""
            formAddress = ""
            formNote = "Imported from phone contacts"
            formIdDocPath = null
            formIdDocBitmap = null
            mobile1Error = false
            nameError = false
            viewMode = BorrowerPickerViewMode.ADD_CONTACT
            Toast.makeText(context, "Contact loaded from phone. Review details & Save.", Toast.LENGTH_SHORT).show()
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Launch contact picker regardless (PickContact intent handles selection)
        contactPickerLauncher.launch(null)
    }

    fun openContactPicker() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(22.dp)),
            color = SurfaceCard,
            tonalElevation = 6.dp
        ) {
            if (viewMode == BorrowerPickerViewMode.SAVED_LIST) {
                // SCREEN 1: Saved Contact List with Top Right +Human Button & "Add Borrower from Contacts" Popup
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = if (direction == "GIVEN") "Select Borrower" else "Select Lender",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Choose saved contact or add new",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Top right corner: +Human button to open Add Contact screen
                        Button(
                            onClick = {
                                formFullName = ""
                                formNickName = ""
                                formMobile1 = ""
                                formMobile2 = ""
                                formEmail = ""
                                formAddress = ""
                                formNote = ""
                                formIdDocPath = null
                                formIdDocBitmap = null
                                mobile1Error = false
                                nameError = false
                                viewMode = BorrowerPickerViewMode.ADD_CONTACT
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("btn_top_right_add_human")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Contact",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoShrinkButtonText(
                                text = "+ Contact",
                                maxFontSize = 12.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // POPUP / HERO BUTTON: "Add Borrower from Contacts" (Accesses device contacts)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { openContactPicker() }
                            .border(1.5.dp, BluePrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .testTag("popup_btn_add_borrower_from_contacts"),
                        colors = CardDefaults.cardColors(containerColor = BlueLight.copy(alpha = 0.4f))
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(BluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPhone,
                                        contentDescription = "Device Contacts",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Add Borrower from Contacts",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                    Text(
                                        text = "Tap to access phone contacts and import",
                                        fontSize = 11.sp,
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search saved contacts by name or phone...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_saved_contacts_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Saved Contacts (${borrowers.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Filtered List
                    val filteredBorrowers = remember(borrowers, searchQuery) {
                        if (searchQuery.isBlank()) {
                            borrowers
                        } else {
                            borrowers.filter {
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.fullName.contains(searchQuery, ignoreCase = true) ||
                                it.nickName.contains(searchQuery, ignoreCase = true) ||
                                it.phone.contains(searchQuery) ||
                                it.phone2.contains(searchQuery)
                            }
                        }
                    }

                    if (filteredBorrowers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(BorderSubtle.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonOff,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isBlank()) "No saved contacts yet" else "No matching contact found",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Contact' above or import from phone contacts",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredBorrowers, key = { it.id }) { b ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onBorrowerSelected(b)
                                            onDismiss()
                                        }
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .testTag("contact_item_${b.id}"),
                                    colors = CardDefaults.cardColors(containerColor = BackgroundClean)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar with initial
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(TealLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (b.displayName.take(1)).uppercase(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FinanceGreen
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = b.displayName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (b.fullName.isNotBlank() && b.fullName != b.displayName) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "(${b.fullName})",
                                                        fontSize = 11.sp,
                                                        color = TextMuted,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = b.phone,
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                                if (b.phone2.isNotBlank()) {
                                                    Text(
                                                        text = "• Alt: ${b.phone2}",
                                                        fontSize = 11.sp,
                                                        color = TextMuted
                                                    )
                                                }
                                            }

                                            if (b.address.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = b.address,
                                                    fontSize = 11.sp,
                                                    color = TextMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.CheckCircleOutline,
                                            contentDescription = "Select",
                                            tint = BluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // SCREEN 2: Add Contact Screen with the exact requested fields:
                // Full name (As per the Aadhaar)
                // Nick Name  (This is for display on loan screen.)
                // Mobile No 1 (Manditory)
                // Mobile No 2 (Optional)
                // Email ( Optional)
                // Address box (Optional)
                // Note: 
                // Identity Document Attachment option (Optional)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewMode = BorrowerPickerViewMode.SAVED_LIST },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to list", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Add Contact",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Borrower profile & Aadhaar identity",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable form fields
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Full name (As per the Aadhaar)
                        Column {
                            Text(
                                text = "Full name (As per the Aadhaar)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formFullName,
                                onValueChange = {
                                    formFullName = it
                                    if (it.isNotBlank()) nameError = false
                                },
                                placeholder = { Text("e.g. Rahul Ramesh Sharma", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_full_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = if (nameError) FinanceRed else BorderSubtle
                                )
                            )
                            if (nameError) {
                                Text(
                                    text = "Please enter either Full name or Nick Name",
                                    fontSize = 11.sp,
                                    color = FinanceRed,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }

                        // 2. Nick Name (This is for display on loan screen.)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nick Name",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(This is for display on loan screen)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formNickName,
                                onValueChange = {
                                    formNickName = it
                                    if (it.isNotBlank()) nameError = false
                                },
                                placeholder = { Text("e.g. Rahul Bhai / Sharmaji", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(20.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_nick_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        // 3. Mobile No 1 (Manditory)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Mobile No 1",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(Mandatory) *",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceRed
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formMobile1,
                                onValueChange = {
                                    formMobile1 = it
                                    if (it.isNotBlank()) mobile1Error = false
                                },
                                placeholder = { Text("10-digit mobile number", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_mobile1_input"),
                                isError = mobile1Error,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = if (mobile1Error) FinanceRed else BorderSubtle
                                )
                            )
                            if (mobile1Error) {
                                Text(
                                    text = "Mobile No 1 is mandatory",
                                    fontSize = 11.sp,
                                    color = FinanceRed,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }

                        // 4. Mobile No 2 (Optional)
                        Column {
                            Text(
                                text = "Mobile No 2 (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formMobile2,
                                onValueChange = { formMobile2 = it },
                                placeholder = { Text("Alternative mobile number", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_mobile2_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        // 5. Email (Optional)
                        Column {
                            Text(
                                text = "Email (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formEmail,
                                onValueChange = { formEmail = it },
                                placeholder = { Text("e.g. rahul.sharma@example.com", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_email_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        // 6. Address box (Optional)
                        Column {
                            Text(
                                text = "Address box (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formAddress,
                                onValueChange = { formAddress = it },
                                placeholder = { Text("Door / Flat No, Street, Locality, City, State, PIN...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_address_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        // 7. Note:
                        Column {
                            Text(
                                text = "Note (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = formNote,
                                onValueChange = { formNote = it },
                                placeholder = { Text("Guarantor reference, workplace, or personal remarks...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
                                minLines = 2,
                                maxLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contact_note_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BluePrimary,
                                    unfocusedBorderColor = BorderSubtle
                                )
                            )
                        }

                        // 8. Identity Document Attachment option (Optional)
                        Column {
                            Text(
                                text = "Identity Document Attachment option (Optional)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedButton(
                                onClick = { showIdAttachmentDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_attach_id_document"),
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
                                Text(
                                    text = if (formIdDocBitmap != null || formIdDocPath != null) {
                                        "Change Identity Document"
                                    } else {
                                        "Attach Identity Document (Aadhaar / PAN / ID)"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                            }

                            // Identity Document preview
                            if (formIdDocBitmap != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = BackgroundClean),
                                    border = BorderStroke(1.dp, BorderSubtle)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            bitmap = formIdDocBitmap!!.asImageBitmap(),
                                            contentDescription = "Attached Identity Document",
                                            modifier = Modifier
                                                .size(54.dp)
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
                                                    text = "ID Attached",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FinanceGreen
                                                )
                                            }
                                            Text(
                                                text = "Aadhaar / ID proof photo saved",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                formIdDocBitmap = null
                                                formIdDocPath = null
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Remove ID document",
                                                tint = FinanceRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Buttons: Save & Select Contact
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewMode = BorrowerPickerViewMode.SAVED_LIST },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            AutoShrinkButtonText(
                                text = "Cancel",
                                maxFontSize = 14.sp,
                                minFontSize = 9.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                val trimmedFull = formFullName.trim()
                                val trimmedNick = formNickName.trim()
                                val trimmedMobile1 = formMobile1.trim()

                                if (trimmedFull.isBlank() && trimmedNick.isBlank()) {
                                    nameError = true
                                    return@Button
                                }
                                if (trimmedMobile1.isBlank()) {
                                    mobile1Error = true
                                    return@Button
                                }

                                val finalDisplay = trimmedNick.ifBlank { trimmedFull }
                                val newBorrower = Borrower(
                                    name = finalDisplay,
                                    fullName = trimmedFull,
                                    nickName = trimmedNick,
                                    phone = trimmedMobile1,
                                    phone2 = formMobile2.trim(),
                                    email = formEmail.trim(),
                                    address = formAddress.trim(),
                                    notes = formNote.trim(),
                                    idDocumentPath = formIdDocPath ?: "",
                                    creditRating = "A"
                                )

                                // Save to Room database
                                onSaveBorrower(newBorrower)
                                // Select this borrower for the loan
                                onBorrowerSelected(newBorrower)
                                Toast.makeText(context, "Contact saved & selected: $finalDisplay", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                                .testTag("btn_save_and_select_contact"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            AutoShrinkButtonText(
                                text = "Save & Select",
                                maxFontSize = 14.sp,
                                minFontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Attachment Source Dialog for ID Document
    if (showIdAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showIdAttachmentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identity Document", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Attach Aadhaar / identity proof photo from device:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showIdAttachmentDialog = false
                                idGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Choose from Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Select identity proof image from phone", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showIdAttachmentDialog = false
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    idCameraLauncher.launch(null)
                                } else {
                                    idCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TealLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = FinanceGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Take Photo with Camera", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Capture Aadhaar / ID card with camera", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showIdAttachmentDialog = false }) {
                    Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

private fun queryContactFromUri(context: Context, contactUri: Uri): Pair<String, String> {
    var name = ""
    var phone = ""
    try {
        val cursor = context.contentResolver.query(contactUri, null, null, null, null)
        cursor?.use { c ->
            if (c.moveToFirst()) {
                val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = c.getString(nameIndex) ?: ""
                }
                val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) > 0 else true
                val contactId = if (idIndex != -1) c.getString(idIndex) else null

                if (hasPhone && contactId != null) {
                    val phoneCursor = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )
                    phoneCursor?.use { pc ->
                        if (pc.moveToFirst()) {
                            val pIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (pIndex != -1) {
                                phone = pc.getString(pIndex) ?: ""
                            }
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Pair(name, phone)
}

private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, folder: String): String {
    return try {
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "id_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    } catch (e: Exception) {
        ""
    }
}

private fun saveUriToInternalStorage(context: Context, uri: Uri, folder: String): String {
    return try {
        val dir = File(context.filesDir, folder)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "id_${System.currentTimeMillis()}.jpg")
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
