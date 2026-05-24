package com.example.password_vault.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.password_vault.R
import com.example.password_vault.security.showBiometricPrompt
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.theme.White

@Composable
fun SplashScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    val context = LocalContext.current

    fun triggerBiometric() {
        showBiometricPrompt(
            context = context,
            title = context.getString(R.string.biometric_title),
            subtitle = context.getString(R.string.biometric_subtitle),
            onSuccess = onAuthSuccess,
            onError = { code ->
                if (code == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    code == BiometricPrompt.ERROR_USER_CANCELED ||
                    code == BiometricPrompt.ERROR_HW_UNAVAILABLE
                ) {
                    (context as? FragmentActivity)?.finish()
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.startup_page1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.455f)
                .aspectRatio(1f)
                .clickable { triggerBiometric() },
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = White.copy(alpha = 0.85f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                LogoMark(size = 72.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = CoralAccent, fontWeight = FontWeight.Bold)) {
                            append("PASS ")
                        }
                        withStyle(SpanStyle(color = SlatePrimary, fontWeight = FontWeight.Bold)) {
                            append("VAULT")
                        }
                    },
                    fontFamily = BebasFamily,
                    fontSize = 30.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Text(
            text = stringResource(R.string.tagline),
            fontFamily = SinkinSansFamily,
            fontSize = 13.sp,
            color = TextGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 40.dp)
        )
    }
}

@Composable
fun LogoMark(size: Dp = 32.dp) {
    Image(
        painter = painterResource(R.drawable.ic_logo),
        contentDescription = "Pass Vault logo",
        modifier = Modifier.size(size)
    )
}
