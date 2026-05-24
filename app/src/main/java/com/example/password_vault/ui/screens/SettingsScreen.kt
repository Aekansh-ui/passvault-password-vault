package com.example.password_vault.ui.screens

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
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.DividerGrey
import com.example.password_vault.ui.theme.GroupCardBg
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.viewmodel.SettingsViewModel

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 30.dp),
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

            SettingsRow(
                label = stringResource(R.string.session_timeout_label),
                description = stringResource(R.string.session_timeout_desc)
            ) {
                SessionTimeoutDropdown(
                    currentLabel = currentLabel,
                    onSelect = { ms -> viewModel.setSessionTimeout(ms) }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsRow(
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
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = CoralAccent,
                modifier = Modifier.padding(end = 8.dp)
            )
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
