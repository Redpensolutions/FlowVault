package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.LendingCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.security.FlowVaultSecurityManager
import com.example.data.security.VaultDataSnapshot

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `test main activity launch`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertTrue(activity != null)
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FlowVault", appName)
  }

  @Test
  fun `test flowvault security encryption and local secured storage`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val snapshot = VaultDataSnapshot(
      version = 1,
      appName = "FlowVault",
      timestamp = System.currentTimeMillis(),
      borrowers = emptyList(),
      virtualAccounts = emptyList(),
      loans = emptyList(),
      ledgerEntries = emptyList()
    )

    // 1. Encrypt with AES-256-GCM and SHA-256
    val encryptedPkg = FlowVaultSecurityManager.encryptSnapshot(snapshot)
    assertTrue(encryptedPkg.ciphertextBase64.isNotEmpty())
    assertTrue(encryptedPkg.ivBase64.isNotEmpty())
    assertTrue(encryptedPkg.sha256Checksum.isNotEmpty())

    // 2. Save to local secured space
    val file = FlowVaultSecurityManager.saveToLocalSecuredSpace(context, encryptedPkg)
    assertTrue(file.exists())
    assertTrue(file.length() > 0)

    // 3. Load and decrypt
    val loadedPkg = FlowVaultSecurityManager.loadFromLocalSecuredSpace(context)
    assertTrue(loadedPkg != null)
    val decrypted = FlowVaultSecurityManager.decryptSnapshot(loadedPkg!!)
    assertEquals("FlowVault", decrypted.appName)
    assertEquals(0, decrypted.totalRecordsCount)

    // 4. Verify local secured info
    val info = FlowVaultSecurityManager.getLocalSecuredInfo(context)
    assertTrue(info.isSecured)
    assertTrue(info.backupFileExists)
  }

  @Test
  fun `test simple interest calculation`() {
    val result = LendingCalculations.calculateLoan(
      principal = 10000.0,
      annualRatePercent = 12.0,
      interestType = "SIMPLE",
      termMonths = 12
    )
    assertEquals(10000.0, result.principal, 0.01)
    assertEquals(1200.0, result.totalInterest, 0.01)
    assertEquals(11200.0, result.totalAmountDue, 0.01)
    assertEquals(933.33, result.monthlyPayment, 0.01)
  }

  @Test
  fun `test compound interest calculation`() {
    val result = LendingCalculations.calculateLoan(
      principal = 10000.0,
      annualRatePercent = 12.0,
      interestType = "COMPOUND",
      termMonths = 12
    )
    assertTrue(result.totalInterest > 1200.0)
    assertTrue(result.totalAmountDue > 11200.0)
  }
}
