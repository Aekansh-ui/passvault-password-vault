package com.example.password_vault.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.White
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.util.UNIT_DAYS
import com.example.password_vault.util.UNIT_MONTHS
import com.example.password_vault.util.UNIT_WEEKS
import com.example.password_vault.ui.viewmodel.AddUpdateViewModel
import com.example.password_vault.ui.viewmodel.FormEvent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpdateScreen(
    isUpdate: Boolean,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onDuplicateFound: (Long) -> Unit,
    viewModel: AddUpdateViewModel = hiltViewModel()
) {
    val detail by viewModel.existingDetail.collectAsState()
    val url by viewModel.url.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val isLoading                by viewModel.isLoading.collectAsState()
    val reminderEnabled          by viewModel.reminderEnabled.collectAsState()
    val reminderUnit             by viewModel.reminderUnit.collectAsState()
    val reminderValue            by viewModel.reminderValue.collectAsState()
    val showUsernameWarningDialog by viewModel.showUsernameWarningDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Prefill in update mode
    LaunchedEffect(detail) {
        detail?.let { viewModel.prefillFromDetail(it) }
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                FormEvent.Success -> onSuccess()
                is FormEvent.DuplicateFound -> onDuplicateFound(event.accountId)
                is FormEvent.Error -> scope.launch { snackbarHostState.showSnackbar(event.msg) }
            }
        }
    }

    if (isUpdate && showUsernameWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelUsernameChange() },
            title = {
                Text(
                    stringResource(R.string.username_change_title),
                    fontFamily = SinkinSansFamily
                )
            },
            text = {
                Text(
                    stringResource(R.string.username_change_msg),
                    fontFamily = SinkinSansFamily,
                    color = TextGrey
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.proceedWithUsernameChange() },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = CoralAccent)
                ) {
                    Text(stringResource(R.string.proceed), fontFamily = SinkinSansFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelUsernameChange() }) {
                    Text(stringResource(R.string.cancel), fontFamily = SinkinSansFamily, color = TextGrey)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isUpdate) stringResource(R.string.update) else stringResource(R.string.add_new),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // URL field
            FormField(
                label = stringResource(R.string.field_url),
                value = url,
                onValueChange = { viewModel.url.value = it },
                placeholder = stringResource(R.string.hint_url),
                keyboardType = KeyboardType.Uri
            )

            // EMAIL / USERNAME field
            FormField(
                label = stringResource(R.string.field_email),
                value = email,
                onValueChange = { viewModel.email.value = it },
                placeholder = stringResource(R.string.hint_email),
                keyboardType = KeyboardType.Email
            )

            // PASSWORD field
            Column {
                Text(
                    stringResource(R.string.field_password),
                    fontFamily = SinkinSansFamily,
                    fontSize = 12.sp,
                    color = TextGrey
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    placeholder = {
                        Text(stringResource(R.string.hint_password), fontFamily = SinkinSansFamily, color = TextGrey)
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisible) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null,
                                tint = CoralAccent
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoralAccent,
                        unfocusedBorderColor = TextGrey,
                        focusedTextColor = SlatePrimary,
                        unfocusedTextColor = SlatePrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // GENERATE NEW button
                Spacer(Modifier.height(28.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = viewModel::generatePassword,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.generate_new), fontFamily = SinkinSansFamily, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // PERIODIC REMINDER section
            ReminderSection(
                enabled = reminderEnabled,
                onEnabledChange = { viewModel.reminderEnabled.value = it },
                unit = reminderUnit,
                onUnitChange = { viewModel.reminderUnit.value = it },
                value = reminderValue,
                onValueChange = { viewModel.reminderValue.value = it }
            )

            Spacer(Modifier.height(24.dp))

            // Primary CTA
            Button(
                onClick = viewModel::submit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isUpdate) stringResource(R.string.save_changes) else stringResource(R.string.add_password),
                        fontFamily = SinkinSansFamily,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReminderSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "PERIODIC REMINDER",
            fontFamily = SinkinSansFamily,
            fontSize = 12.sp,
            color = TextGrey
        )

        // Yes / No bubbles
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BubbleChip(label = "NO",  selected = !enabled, onClick = { onEnabledChange(false) })
            BubbleChip(label = "YES", selected = enabled,  onClick = { onEnabledChange(true) })
        }

        if (enabled) {
            // Unit bubbles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BubbleChip(label = "DAYS",   selected = unit == UNIT_DAYS,   onClick = { onUnitChange(UNIT_DAYS) })
                BubbleChip(label = "WEEKS",  selected = unit == UNIT_WEEKS,  onClick = { onUnitChange(UNIT_WEEKS) })
                BubbleChip(label = "MONTHS", selected = unit == UNIT_MONTHS, onClick = { onUnitChange(UNIT_MONTHS) })
            }

            // Number dropdown (1-180)
            ReminderValueDropdown(value = value, onValueChange = onValueChange)
        }
    }
}

@Composable
fun BubbleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor     = if (selected) CoralAccent else Color.Transparent
    val borderColor = if (selected) CoralAccent else TextGrey
    val textColor   = if (selected) White else SlatePrimary

    Box(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .background(bgColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontFamily = SinkinSansFamily, fontSize = 13.sp, color = textColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderValueDropdown(value: Int, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "EVERY",
            fontFamily = SinkinSansFamily,
            fontSize = 12.sp,
            color = TextGrey
        )
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = SinkinSansFamily,
                    fontSize = 14.sp,
                    color = SlatePrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoralAccent,
                    unfocusedBorderColor = TextGrey,
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
                (1..180).forEach { n ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = n.toString(),
                                fontFamily = SinkinSansFamily,
                                fontSize = 14.sp,
                                color = if (n == value) CoralAccent else SlatePrimary
                            )
                        },
                        onClick = { onValueChange(n); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontFamily = SinkinSansFamily, fontSize = 12.sp, color = TextGrey)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontFamily = SinkinSansFamily, color = TextGrey) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = readOnly,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoralAccent,
                unfocusedBorderColor = TextGrey,
                focusedTextColor = SlatePrimary,
                unfocusedTextColor = SlatePrimary,
                disabledTextColor = SlatePrimary.copy(alpha = 0.6f),
                disabledBorderColor = TextGrey
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
