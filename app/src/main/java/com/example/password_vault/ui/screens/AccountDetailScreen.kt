package com.example.password_vault.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.domain.model.PasswordVersion
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.security.showBiometricPrompt
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.DividerGrey
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.viewmodel.AccountEvent
import com.example.password_vault.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToUpdate: (Long) -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val detail by viewModel.detail.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val displayVersion by viewModel.displayVersion.collectAsState()
    val isLatest by viewModel.isLatest.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showVersionMenu by remember { mutableStateOf(false) }

    // FLAG_SECURE for this screen
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                AccountEvent.NavigateBack -> onBack()
                AccountEvent.GroupDeleted -> onNavigateToHome()
                AccountEvent.VersionRestored -> {
                    scope.launch { snackbarHostState.showSnackbar("Previous version restored.") }
                }
                is AccountEvent.NavigateToUpdate -> onNavigateToUpdate(event.accountId)
            }
        }
    }

    val d = detail ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = SlatePrimary, fontFamily = SinkinSansFamily)
        }
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 30.dp),
                title = {
                    Text(
                        text = d.groupName.uppercase(),
                        fontFamily = BebasFamily,
                        fontSize = 28.sp,
                        color = CoralAccent,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CoralAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Version chip — below header, right-aligned
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, DividerGrey, RoundedCornerShape(20.dp))
                            .clickable { showVersionMenu = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        val vLabel = displayVersion?.let { "v${it.versionNo}" } ?: "v?"
                        Text(
                            text = "$vLabel ▼",
                            fontFamily = SinkinSansFamily,
                            fontSize = 13.sp,
                            color = SlatePrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showVersionMenu,
                        onDismissRequest = { showVersionMenu = false }
                    ) {
                        d.versions.forEach { version ->
                            DropdownMenuItem(
                                text = {
                                    Text("v${version.versionNo}", fontFamily = SinkinSansFamily, fontSize = 14.sp, color = SlatePrimary)
                                },
                                onClick = {
                                    if (version.id == d.currentVersionId) viewModel.selectLatestVersion()
                                    else viewModel.selectVersion(version)
                                    showVersionMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Date field
            DetailField(
                icon = Icons.Default.CalendarToday,
                label = "Date",
                value = displayVersion?.let { formatDate(it.createdAt) } ?: "—"
            )

            // URL field
            DetailField(
                icon = Icons.Default.Link,
                label = "Website",
                value = d.websiteUrl.ifBlank { "—" }
            )

            // Email/Username field
            DetailField(
                icon = Icons.Default.Person,
                label = "Email / Username",
                value = d.username
            )

            // Password field
            val pwDisplay = if (passwordVisible) {
                displayVersion?.password ?: "—"
            } else {
                "•".repeat((displayVersion?.password?.length ?: 8).coerceIn(6, 16))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, null, tint = SlatePrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Password", fontFamily = SinkinSansFamily, fontSize = 12.sp, color = TextGrey)
                    Text(
                        text = pwDisplay,
                        fontFamily = if (passwordVisible) FontFamily.Monospace else SinkinSansFamily,
                        fontSize = 16.sp,
                        color = SlatePrimary
                    )
                }
                // Eye toggle — reveal requires biometric, hide is instant
                IconButton(
                    onClick = {
                        if (passwordVisible) {
                            viewModel.setPasswordVisible(false)
                        } else {
                            showBiometricPrompt(
                                context = context,
                                title = context.getString(R.string.biometric_reveal_title),
                                subtitle = context.getString(R.string.biometric_reveal_subtitle),
                                onSuccess = { viewModel.setPasswordVisible(true) }
                            )
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide" else "Show",
                        tint = CoralAccent
                    )
                }
                // Copy
                IconButton(
                    onClick = {
                        displayVersion?.password?.let { pw ->
                            copyToClipboard(context, pw)
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.password_copied)) }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, "Copy", tint = CoralAccent)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerGrey)
            )

            Spacer(Modifier.height(24.dp))

            // Action buttons — only visible on latest version
            if (isLatest) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.delete), fontFamily = SinkinSansFamily)
                    }

                    Button(
                        onClick = viewModel::navigateToUpdate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.update), fontFamily = SinkinSansFamily)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        val hasMultiple = (d.versions.size > 1)
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title), fontFamily = SinkinSansFamily) },
            text = {
                Text(
                    if (hasMultiple) stringResource(R.string.delete_confirm_msg_has_prev)
                    else stringResource(R.string.delete_confirm_msg_only),
                    fontFamily = SinkinSansFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCurrentVersion()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CoralAccent)
                ) {
                    Text(stringResource(R.string.confirm), fontFamily = SinkinSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), fontFamily = SinkinSansFamily, color = TextGrey)
                }
            }
        )
    }
}

@Composable
private fun DetailField(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = SlatePrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontFamily = SinkinSansFamily, fontSize = 12.sp, color = TextGrey)
            Text(value, fontFamily = SinkinSansFamily, fontSize = 16.sp, color = SlatePrimary)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerGrey))
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("password", text)
    // Mark as sensitive on Android 13+
    clip.description.extras = android.os.PersistableBundle().apply {
        putBoolean("android.content.extra.IS_SENSITIVE", true)
    }
    clipboard.setPrimaryClip(clip)
    // Auto-clear after 30 seconds
    Handler(Looper.getMainLooper()).postDelayed({
        if (clipboard.primaryClip?.getItemAt(0)?.text == text) {
            clipboard.clearPrimaryClip()
        }
    }, 30_000L)
}
