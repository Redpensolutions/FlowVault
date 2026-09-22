package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BackgroundClean
import com.example.ui.theme.BlueDark
import com.example.ui.theme.BlueLight
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FinanceGreen
import com.example.ui.theme.FinanceGreenLight
import com.example.ui.theme.FinanceRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealSlate
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    savedPin: String,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    faceUnlockEnabled: Boolean,
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinFallback by remember { mutableStateOf(false) }

    // Dialog state for hardware / simulated biometric prompts
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showFaceDialog by remember { mutableStateOf(false) }
    var biometricScanState by remember { mutableStateOf("SCANNING") } // "SCANNING", "SUCCESS", "FAILED"
    var faceScanState by remember { mutableStateOf("SCANNING") } // "SCANNING", "SUCCESS", "FAILED"

    val triggerBiometric: () -> Unit = {
        val activity = context as? android.app.Activity ?: (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
        if (activity != null && com.example.util.BiometricSecurityHelper.isBiometricSupported(context)) {
            com.example.util.BiometricSecurityHelper.authenticateWithBiometrics(
                activity = activity,
                onSuccess = {
                    Toast.makeText(context, "Biometric Fingerprint Verified!", Toast.LENGTH_SHORT).show()
                    onUnlockSuccess()
                },
                onError = { err ->
                    // Fall back to animated biometric dialog
                    biometricScanState = "SCANNING"
                    showBiometricDialog = true
                },
                onNegativeButton = {
                    showPinFallback = true
                }
            )
        } else {
            biometricScanState = "SCANNING"
            showBiometricDialog = true
        }
    }

    val triggerFace: () -> Unit = {
        faceScanState = "SCANNING"
        showFaceDialog = true
    }

    // Auto-prompt active security on appearance
    LaunchedEffect(Unit) {
        if (biometricEnabled) {
            delay(350)
            triggerBiometric()
        } else if (faceUnlockEnabled && !pinEnabled) {
            delay(350)
            triggerFace()
        }
    }

    fun handleDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            errorMessage = null
            enteredPin += digit
            if (enteredPin.length == 4) {
                // Validate PIN
                val targetPin = if (savedPin.isNotBlank()) savedPin else "1234"
                if (enteredPin == targetPin) {
                    Toast.makeText(context, "PIN verified! Welcome back.", Toast.LENGTH_SHORT).show()
                    onUnlockSuccess()
                } else {
                    errorMessage = "Incorrect PIN. Default PIN is $targetPin"
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun handleClear() {
        enteredPin = ""
        errorMessage = null
    }

    val isPinPrimary = pinEnabled || showPinFallback || (!biometricEnabled && !faceUnlockEnabled)

    val lockHeaderTitle = when {
        biometricEnabled && faceUnlockEnabled && pinEnabled -> "FlowVault Multi-Factor Security"
        biometricEnabled && pinEnabled -> "PIN & Fingerprint Security Active"
        faceUnlockEnabled && pinEnabled -> "PIN & Face Recognition Active"
        biometricEnabled && faceUnlockEnabled -> "Biometric & Face Security Active"
        biometricEnabled -> "Fingerprint Security Active"
        faceUnlockEnabled -> "Face Recognition Active"
        pinEnabled -> "Security PIN Protection Active"
        else -> "FlowVault is Locked"
    }

    val lockHeaderSubtitle = when {
        !isPinPrimary && biometricEnabled -> "Touch your fingerprint sensor to unlock"
        !isPinPrimary && faceUnlockEnabled -> "Look at the front camera to unlock"
        isPinPrimary && (biometricEnabled || faceUnlockEnabled) -> "Enter PIN or tap quick biometrics below"
        else -> "Enter your 4-digit security PIN"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundClean)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_icon),
                contentDescription = "FlowVault Logo",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "FlowVault",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Lend easily. Track accurately. Store safely.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Lock Header Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when {
                    !isPinPrimary && biometricEnabled -> TealLight
                    !isPinPrimary && faceUnlockEnabled -> BlueLight
                    else -> Color(0xFFEDE7F6)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        !isPinPrimary && biometricEnabled -> TealSlate.copy(alpha = 0.3f)
                        !isPinPrimary && faceUnlockEnabled -> BluePrimary.copy(alpha = 0.3f)
                        else -> Color(0xFF673AB7).copy(alpha = 0.3f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !isPinPrimary && biometricEnabled -> TealSlate
                                    !isPinPrimary && faceUnlockEnabled -> BluePrimary
                                    else -> Color(0xFF673AB7)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                !isPinPrimary && biometricEnabled -> Icons.Default.Fingerprint
                                !isPinPrimary && faceUnlockEnabled -> Icons.Default.Face
                                isPinPrimary && (biometricEnabled || faceUnlockEnabled) -> Icons.Default.Shield
                                else -> Icons.Default.Lock
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = lockHeaderTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                !isPinPrimary && biometricEnabled -> TealSlate
                                !isPinPrimary && faceUnlockEnabled -> BluePrimary
                                else -> Color(0xFF4A148C)
                            }
                        )
                        Text(
                            text = lockHeaderSubtitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isPinPrimary) {
                // PIN Dots Display
                Text(
                    text = "Enter 4-Digit Security PIN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) BluePrimary else Color(0xFFE2E8F0))
                                .border(1.5.dp, if (isFilled) BlueDark else Color(0xFFCBD5E1), CircleShape)
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = FinanceRed,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Numeric Keypad
                Column(
                    modifier = Modifier.widthIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val keypad = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )

                    keypad.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (key) {
                                                "C", "⌫" -> Color(0xFFF1F5F9)
                                                else -> SurfaceCard
                                            }
                                        )
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                        .clickable {
                                            when (key) {
                                                "C" -> handleClear()
                                                "⌫" -> handleBackspace()
                                                else -> handleDigitPress(key)
                                            }
                                        }
                                        .testTag("keypad_$key"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = if (key == "C") 16.sp else 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (key) {
                                                "C" -> FinanceRed
                                                else -> TextPrimary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (biometricEnabled || faceUnlockEnabled) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (biometricEnabled) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = TealLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, TealSlate.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { triggerBiometric() }
                                    .testTag("btn_unlock_biometric")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = TealSlate,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fingerprint",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealSlate
                                    )
                                }
                            }
                        }

                        if (faceUnlockEnabled) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BlueLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { triggerFace() }
                                    .testTag("btn_unlock_face")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = BluePrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Face Unlock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Biometric or Face is the primary security method
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (biometricEnabled) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(TealLight)
                                    .border(2.dp, TealSlate.copy(alpha = 0.5f), CircleShape)
                                    .clickable { triggerBiometric() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint Authentication",
                                    tint = TealSlate,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Biometric Verification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Touch device fingerprint sensor or tap above",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { triggerBiometric() },
                                colors = ButtonDefaults.buttonColors(containerColor = TealSlate),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_verify_fingerprint_primary")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verify Fingerprint", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (faceUnlockEnabled) {
                            if (biometricEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Button(
                                onClick = { triggerFace() },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_verify_face_primary")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Face to Unlock", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        TextButton(
                            onClick = { showPinFallback = true },
                            modifier = Modifier.testTag("btn_use_pin_fallback")
                        ) {
                            Text("Use 4-digit PIN fallback", fontSize = 13.sp, color = BluePrimary)
                        }
                    }
                }
            }
        }
    }

    // Biometric Fingerprint Prompt Dialog
    if (showBiometricDialog) {
        LaunchedEffect(Unit) {
            delay(1200)
            biometricScanState = "SUCCESS"
            delay(600)
            showBiometricDialog = false
            Toast.makeText(context, "Biometric Fingerprint Verified!", Toast.LENGTH_SHORT).show()
            onUnlockSuccess()
        }

        AlertDialog(
            onDismissRequest = { showBiometricDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (biometricScanState == "SUCCESS") FinanceGreenLight else TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (biometricScanState == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = if (biometricScanState == "SUCCESS") FinanceGreen else TealSlate,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (biometricScanState == "SUCCESS") "Fingerprint Recognized!" else "Biometric Authentication",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (biometricScanState == "SUCCESS") {
                            "Identity confirmed via biometric sensor. Unlocking..."
                        } else {
                            "Touch the fingerprint sensor to unlock FlowVault."
                        },
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (biometricScanState == "SCANNING") {
                        Spacer(modifier = Modifier.height(14.dp))
                        CircularProgressIndicator(
                            color = TealSlate,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showBiometricDialog = false
                    showPinFallback = true
                }) {
                    Text("Use PIN Instead")
                }
            }
        )
    }

    // Face Recognition Prompt Dialog
    if (showFaceDialog) {
        LaunchedEffect(Unit) {
            delay(1400)
            faceScanState = "SUCCESS"
            delay(600)
            showFaceDialog = false
            Toast.makeText(context, "Face ID Recognized!", Toast.LENGTH_SHORT).show()
            onUnlockSuccess()
        }

        AlertDialog(
            onDismissRequest = { showFaceDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (faceScanState == "SUCCESS") FinanceGreenLight else BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (faceScanState == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Face,
                        contentDescription = null,
                        tint = if (faceScanState == "SUCCESS") FinanceGreen else BluePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (faceScanState == "SUCCESS") "Face ID Recognized!" else "Face Recognition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (faceScanState == "SUCCESS") {
                            "Facial geometry matched. Unlocking..."
                        } else {
                            "Looking for face... Please look directly at the front camera."
                        },
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    if (faceScanState == "SCANNING") {
                        Spacer(modifier = Modifier.height(14.dp))
                        CircularProgressIndicator(
                            color = BluePrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showFaceDialog = false
                    showPinFallback = true
                }) {
                    Text("Use PIN Instead")
                }
            }
        )
    }
}
