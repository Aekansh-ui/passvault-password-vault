package com.example.password_vault.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.security.showBiometricPrompt
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.DividerGrey
import com.example.password_vault.ui.theme.GroupCardBg
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.viewmodel.SettingsEvent
import com.example.password_vault.ui.viewmodel.SettingsViewModel
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TIMEOUT_OPTIONS = listOf(
    60_000L     to "1 minute",
    120_000L    to "2 minutes",
    300_000L    to "5 minutes (default)",
    600_000L    to "10 minutes",
    900_000L    to "15 minutes",
    1_800_000L  to "30 minutes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTimeoutMs by viewModel.sessionTimeoutMs.collectAsState()
    val currentLabel = TIMEOUT_OPTIONS.firstOrNull { it.first == currentTimeoutMs }?.second
        ?: "5 minutes (default)"
    val passwordWords by viewModel.passwordWords.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showWordsDialog by remember { mutableStateOf(false) }
    var wordsInput by remember { mutableStateOf("") }

    // Holds generated JSON until the file-creation dialog resolves
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingBackupJson ?: return@rememberLauncherForActivityResult
        pendingBackupJson = null
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                withContext(Dispatchers.Main) {
                    snackbarHostState.showSnackbar("Backup saved successfully.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackbarHostState.showSnackbar("Failed to write backup file.")
                }
            }
        }
    }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                }
                if (json != null) {
                    withContext(Dispatchers.Main) { viewModel.restore(json) }
                } else {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Could not read the selected file.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    snackbarHostState.showSnackbar("Failed to read backup file.")
                }
            }
        }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.BackupReady -> {
                    pendingBackupJson = event.json
                    val filename = "passvault_backup_${
                        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    }.json"
                    createFileLauncher.launch(filename)
                }
                is SettingsEvent.RestoreDone -> {
                    val r = event.result
                    snackbarHostState.showSnackbar(
                        "Restored ${r.imported} account${if (r.imported != 1) "s" else ""}." +
                        if (r.skipped > 0) " Skipped ${r.skipped} duplicate${if (r.skipped != 1) "s" else ""}." else ""
                    )
                }
                is SettingsEvent.Error -> snackbarHostState.showSnackbar(event.msg)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontFamily = BebasFamily,
                        fontSize = 28.sp,
                        color = CoralAccent,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CoralAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // Session timeout
            SettingsRow(
                icon = { Icon(Icons.Default.Timer, null, tint = CoralAccent, modifier = Modifier.padding(end = 8.dp)) },
                label = stringResource(R.string.session_timeout_label),
                description = stringResource(R.string.session_timeout_desc)
            ) {
                SessionTimeoutDropdown(
                    currentLabel = currentLabel,
                    onSelect = { ms -> viewModel.setSessionTimeout(ms) }
                )
            }

            Spacer(Modifier.height(12.dp))

            // Backup
            SettingsRow(
                icon = { Icon(Icons.Default.FileDownload, null, tint = CoralAccent, modifier = Modifier.padding(end = 8.dp)) },
                label = stringResource(R.string.backup_label),
                description = stringResource(R.string.backup_desc)
            ) {
                ActionButton(label = stringResource(R.string.backup_action)) {
                    showBiometricPrompt(
                        context = context,
                        title = context.getString(R.string.biometric_backup_title),
                        subtitle = context.getString(R.string.biometric_backup_subtitle),
                        onSuccess = { viewModel.prepareBackup() }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Restore
            SettingsRow(
                icon = { Icon(Icons.Default.FileUpload, null, tint = CoralAccent, modifier = Modifier.padding(end = 8.dp)) },
                label = stringResource(R.string.restore_label),
                description = stringResource(R.string.restore_desc)
            ) {
                ActionButton(label = stringResource(R.string.restore_action)) {
                    openFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Password words for generator
            SettingsRow(
                icon = { Icon(Icons.Default.Password, null, tint = CoralAccent, modifier = Modifier.padding(end = 8.dp)) },
                label = stringResource(R.string.password_words_label),
                description = if (passwordWords.isNotBlank()) passwordWords
                              else stringResource(R.string.password_words_desc)
            ) {
                ActionButton(label = stringResource(R.string.password_words_action)) {
                    wordsInput = passwordWords
                    showWordsDialog = true
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showWordsDialog) {
        Dialog(onDismissRequest = { showWordsDialog = false }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.password_words_dialog_title),
                        fontFamily = SinkinSansFamily,
                        fontSize = 18.sp,
                        color = SlatePrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.password_words_dialog_desc),
                        fontFamily = SinkinSansFamily,
                        fontSize = 12.sp,
                        color = TextGrey
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = wordsInput,
                        onValueChange = { wordsInput = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.password_words_hint),
                                fontFamily = SinkinSansFamily,
                                color = TextGrey
                            )
                        },
                        singleLine = false,
                        minLines = 8,
                        maxLines = Int.MAX_VALUE,
                        textStyle = TextStyle(fontFamily = SinkinSansFamily, fontSize = 14.sp, color = SlatePrimary),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralAccent,
                            unfocusedBorderColor = DividerGrey,
                            focusedTextColor = SlatePrimary,
                            unfocusedTextColor = SlatePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = { showWordsDialog = false }) {
                            Text(stringResource(R.string.cancel), fontFamily = SinkinSansFamily, color = TextGrey)
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                viewModel.setPasswordWords(wordsInput.trim())
                                showWordsDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = CoralAccent)
                        ) {
                            Text(stringResource(R.string.save), fontFamily = SinkinSansFamily)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: @Composable () -> Unit,
    label: String,
    description: String,
    control: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(GroupCardBg)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(
                text = label,
                fontFamily = BebasFamily,
                fontSize = 13.sp,
                color = SlatePrimary,
                letterSpacing = 1.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            fontFamily = SinkinSansFamily,
            fontSize = 12.sp,
            color = TextGrey
        )
        Spacer(Modifier.height(12.dp))
        control()
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent),
        border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, fontFamily = SinkinSansFamily, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionTimeoutDropdown(
    currentLabel: String,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SinkinSansFamily,
                fontSize = 14.sp,
                color = SlatePrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralAccent,
                unfocusedBorderColor = DividerGrey,
                focusedTrailingIconColor = CoralAccent,
                unfocusedTrailingIconColor = TextGrey
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TIMEOUT_OPTIONS.forEach { (ms, label) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontFamily = SinkinSansFamily,
                            fontSize = 14.sp,
                            color = if (label == currentLabel) CoralAccent else SlatePrimary
                        )
                    },
                    onClick = {
                        onSelect(ms)
                        expanded = false
                    }
                )
            }
        }
    }
}
