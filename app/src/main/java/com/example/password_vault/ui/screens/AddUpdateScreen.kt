package com.example.password_vault.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    val name by viewModel.name.collectAsState()
    val url by viewModel.url.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val passwordVisible by viewModel.passwordVisible.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 30.dp),
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

            Spacer(Modifier.height(16.dp))

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
