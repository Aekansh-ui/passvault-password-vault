package com.example.password_vault.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.password_vault.R
import com.example.password_vault.domain.model.GroupSummary
import com.example.password_vault.ui.theme.BebasFamily
import com.example.password_vault.ui.theme.CoralAccent
import com.example.password_vault.ui.theme.GroupCardBg
import com.example.password_vault.ui.theme.NeutralCard
import com.example.password_vault.ui.theme.SinkinSansFamily
import com.example.password_vault.ui.theme.SlatePrimary
import com.example.password_vault.ui.theme.TextGrey
import com.example.password_vault.ui.theme.White
import com.example.password_vault.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onGroupClick: (Long, String) -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    Scaffold(
        bottomBar = {
            PassVaultBottomNav(
                currentRoute = "home",
                onHomeClick = {},
                onAddClick = onAddClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Logo mark
            LogoMark(size = 40.dp)

            Spacer(Modifier.height(18.dp))

            // Search bar
            SearchBar(
                query = query,
                onQueryChange = viewModel::onSearchChange,
                onClear = viewModel::clearSearch
            )

            Spacer(Modifier.height(30.dp))

            when {
                groups.isEmpty() && query.isBlank() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_state),
                            fontFamily = SinkinSansFamily,
                            fontSize = 15.sp,
                            color = TextGrey,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                groups.isEmpty() && query.isNotBlank() -> {
                    NoResultsState()
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(groups) { group ->
                            GroupCard(group = group, onClick = { onGroupClick(group.id, group.name) })
                        }
                        if (query.isNotBlank()) {
                            item {
                                Text(
                                    text = stringResource(R.string.end_of_line),
                                    fontFamily = SinkinSansFamily,
                                    fontSize = 12.sp,
                                    color = TextGrey,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val searchHint = stringResource(R.string.search_hint)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .border(0.8.dp, CoralAccent, RoundedCornerShape(50))
            .background(NeutralCard)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = if (query.isNotEmpty()) CoralAccent else TextGrey,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(CoralAccent),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = SinkinSansFamily,
                fontSize = 14.sp,
                color = SlatePrimary
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = searchHint,
                            fontFamily = SinkinSansFamily,
                            fontSize = 14.sp,
                            color = TextGrey
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = TextGrey,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun GroupCard(group: GroupSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(11.dp),
        colors = CardDefaults.cardColors(containerColor = GroupCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(43.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(SlatePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.firstOrNull()?.uppercase() ?: "?",
                    fontFamily = BebasFamily,
                    fontSize = 22.sp,
                    color = White
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = group.name,
                fontFamily = SinkinSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlatePrimary
            )
        }
    }
}

@Composable
fun NoResultsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DetectiveIllustration()
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_results),
            fontFamily = BebasFamily,
            fontSize = 32.sp,
            color = SlatePrimary,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_results_helper),
            fontFamily = SinkinSansFamily,
            fontSize = 14.sp,
            color = TextGrey,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DetectiveIllustration() {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
        val s = size.width
        drawOval(
            color = SlatePrimary,
            topLeft = androidx.compose.ui.geometry.Offset(s * 0.25f, s * 0.35f),
            size = androidx.compose.ui.geometry.Size(s * 0.5f, s * 0.5f)
        )
        drawCircle(
            color = SlatePrimary,
            radius = s * 0.18f,
            center = androidx.compose.ui.geometry.Offset(s * 0.5f, s * 0.28f)
        )
        drawCircle(
            color = CoralAccent,
            radius = s * 0.15f,
            center = androidx.compose.ui.geometry.Offset(s * 0.72f, s * 0.55f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = s * 0.05f)
        )
        drawLine(
            color = CoralAccent,
            start = androidx.compose.ui.geometry.Offset(s * 0.82f, s * 0.65f),
            end = androidx.compose.ui.geometry.Offset(s * 0.92f, s * 0.78f),
            strokeWidth = s * 0.05f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun PassVaultBottomNav(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 10.dp)
            .height(85.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = NeutralCard,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(71.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Left third — home icon centred between left edge and FAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onHomeClick) {
                        Image(
                            painter = painterResource(R.drawable.icon_home),
                            contentDescription = "Home",
                            colorFilter = ColorFilter.tint(
                                if (currentRoute == "home") CoralAccent else TextGrey
                            ),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                // Centre third — empty, FAB floats above here
                Spacer(modifier = Modifier.weight(1f))
                // Right third — profile icon centred between FAB and right edge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onProfileClick) {
                        Image(
                            painter = painterResource(R.drawable.icon_user),
                            contentDescription = "Profile",
                            colorFilter = ColorFilter.tint(
                                if (currentRoute == "profile") CoralAccent else TextGrey
                            ),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = CoralAccent,
            shape = CircleShape,
            modifier = Modifier
                .size(70.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-9).dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
