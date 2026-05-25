package com.example.password_vault.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.password_vault.R
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.White
import com.example.password_vault.ui.theme.DividerGrey
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val editName by viewModel.editName.collectAsState()
    val editImageUri by viewModel.editImageUri.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(profile) {
        if (editName.isEmpty()) viewModel.prepareEdit(profile)
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) viewModel.editImageUri.value = uri
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_profile),
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Profile photo preview
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(3.dp, CoralAccent, RoundedCornerShape(12.dp))
                    .background(DividerGrey),
                contentAlignment = Alignment.Center
            ) {
                when {
                    editImageUri != null -> {
                        AsyncImage(
                            model = editImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    profile.imagePath != null -> {
                        val bytes = viewModel.loadProfileImageBytes()
                        val bmp = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                        if (bmp != null) {
                            Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(Icons.Default.Person, null, tint = SlatePrimary, modifier = Modifier.size(48.dp))
                        }
                    }
                    else -> {
                        Icon(Icons.Default.Person, null, tint = SlatePrimary, modifier = Modifier.size(48.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.change_picture),
                fontFamily = SinkinSansFamily,
                fontSize = 14.sp,
                color = CoralAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            FormField(
                label = stringResource(R.string.field_name),
                value = editName,
                onValueChange = { viewModel.editName.value = it },
                placeholder = "John Doe"
            )

            Spacer(Modifier.height(32.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent),
                border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent)
            ) {
                Text(stringResource(R.string.cancel), fontFamily = SinkinSansFamily, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    viewModel.saveProfile()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.save), fontFamily = SinkinSansFamily, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
