package com.example.password_vault.ui.screens

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
fun LogoMark(size: Dp = 32.dp, modifier: Modifier = Modifier) {
    // Eye blink animation — translated 1:1 from the SVG SMIL animateTransform keyframes.
    // Pivot is the center of the eye clip-rect (12,10) in the 24×24 viewport.
    val blinkEasing = CubicBezierEasing(0.333f, 0f, 0.667f, 1f)
    val transition = rememberInfiniteTransition(label = "logo_blink")

    val eyeScaleY by transition.animateFloat(
        initialValue = 1f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1f     at 0    with blinkEasing
                0.011f at 500  with blinkEasing   // snap vertical close
                0.542f at 667  with blinkEasing   // rising
                1f     at 833  with blinkEasing   // fully open
                1f     at 1000 with blinkEasing
                1f     at 1167 with blinkEasing   // X blinks here; Y holds 1
                0.542f at 1333 with blinkEasing   // Y dips while X recovers
                1f     at 1500 with blinkEasing
                1f     at 1833 with blinkEasing
                0.71f  at 2000 with blinkEasing   // partial blink
                1f     at 2167 with blinkEasing   // X blinks here; Y recovers
                1f     at 2500 with blinkEasing
                0.795f at 2667 with blinkEasing
                1f     at 3000 with blinkEasing
                0.518f at 3333 with blinkEasing
                1f     at 3667 with blinkEasing
                0.968f at 4000 with blinkEasing
                1f     at 4167 with blinkEasing
                1f     at 4333 with blinkEasing
                0.976f at 4667 with blinkEasing
                1f     at 5000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eyeScaleY"
    )

    val eyeScaleX by transition.animateFloat(
        initialValue = 1f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000
                1f     at 0    with blinkEasing
                1f     at 1000 with blinkEasing   // hold until first horizontal blink
                0.007f at 1167 with blinkEasing   // snap horizontal close
                1f     at 1333 with blinkEasing   // recover
                1f     at 2000 with blinkEasing   // hold until second blink
                0.007f at 2167 with blinkEasing   // second horizontal blink
                1f     at 2333 with blinkEasing   // recover
                1f     at 5000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eyeScaleX"
    )

    Box(modifier = modifier.size(size)) {
        Image(
            painter = painterResource(R.drawable.ic_logo_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.ic_logo_eye),
            contentDescription = "Pass Vault logo",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = eyeScaleX
                    scaleY = eyeScaleY
                    // pivot at center of eye clip-rect: (12/24, 10/24)
                    transformOrigin = TransformOrigin(0.5f, 10f / 24f)
                }
        )
    }
}
