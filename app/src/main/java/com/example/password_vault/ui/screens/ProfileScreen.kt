package com.example.password_vault.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.DividerGrey
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateHome: () -> Unit,
    onNavigateEditProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    val profileImageBitmap: ImageBitmap? = remember(profile.imagePath) {
        profile.imagePath?.let {
            val bytes = viewModel.loadProfileImageBytes() ?: return@let null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PassVaultBottomNav(
                currentRoute = "profile",
                onHomeClick = onNavigateHome,
                onAddClick = {},
                onProfileClick = {}
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile),
                        fontFamily = BebasFamily,
                        fontSize = 28.sp,
                        color = CoralAccent,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(3.dp, CoralAccent, RoundedCornerShape(12.dp))
                        .background(DividerGrey),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageBitmap != null) {
                        Image(
                            bitmap = profileImageBitmap,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, null, tint = SlatePrimary, modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = profile.displayName.ifBlank { "Your Name" },
                    fontFamily = BebasFamily,
                    fontSize = 22.sp,
                    color = SlatePrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = profile.username.ifBlank { "username" },
                    fontFamily = SinkinSansFamily,
                    fontSize = 14.sp,
                    color = TextGrey
                )

                Spacer(Modifier.height(24.dp))

                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.update_profile),
                    onClick = onNavigateEditProfile
                )

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    onClick = onNavigateSettings
                )

                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    label = stringResource(R.string.about_app),
                    onClick = onNavigateAbout
                )
            }

            Text(
                text = stringResource(R.string.app_version),
                fontFamily = SinkinSansFamily,
                fontSize = 12.sp,
                color = TextGrey,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = SlatePrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = CoralAccent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = SinkinSansFamily,
            fontSize = 15.sp,
            color = tintColor,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(color = DividerGrey, thickness = 0.5.dp)
}
