package com.example.password_vault.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.password_vault.R
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.theme.White

@Composable
fun SplashScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        showBiometricPrompt(
            context = context,
            onSuccess = onAuthSuccess,
            onFailure = onAuthFailed
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full-screen startup background PNG
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Centred logo card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoMark(size = 72.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "PASS VAULT",
                    fontFamily = BebasFamily,
                    fontSize = 28.sp,
                    color = CoralAccent,
                    letterSpacing = 4.sp
                )
            }
        }

        // Tagline at bottom
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

private fun showBiometricPrompt(
    context: android.content.Context,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val activity = context as? FragmentActivity ?: return

    val biometricManager = BiometricManager.from(context)
    val canAuth = biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )

    if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
        onFailure()
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationFailed() { /* let user retry */ }
            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                if (code == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    code == BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    activity.finish()
                }
            }
        }
    )

    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.biometric_title))
        .setSubtitle(context.getString(R.string.biometric_subtitle))
        .setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        .build()

    prompt.authenticate(info)
}
