package com.syednoufal.rideflow.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.syednoufal.rideflow.core.designsystem.component.RideFlowCard
import com.syednoufal.rideflow.core.designsystem.component.RideFlowLoadingState
import com.syednoufal.rideflow.core.designsystem.component.RideFlowSecondaryButton
import com.syednoufal.rideflow.core.designsystem.theme.RideFlowTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The rider's profile: identity, rating, and the sign-out action. */
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToLogin -> onLoggedOut()
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onIntent(ProfileIntent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (state.isLoading) {
            RideFlowLoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            val user = state.user
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(RideFlowTheme.spacing.large),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    if (user?.profilePhotoUrl != null) {
                        AsyncImage(
                            model = user.profilePhotoUrl,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(48.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
                Text(
                    text = user?.fullName ?: "Rider",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = user?.phoneNumber.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.large))
                RideFlowCard(modifier = Modifier.fillMaxWidth()) {
                    ProfileStatRow(
                        label = "Rating",
                        value = user?.rating?.let { String.format(Locale.US, "★ %.2f", it) } ?: "--",
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = RideFlowTheme.spacing.small))
                    ProfileStatRow(
                        label = "Member since",
                        value = user?.memberSinceEpochMillis?.let(::formatDate) ?: "--",
                    )
                }

                Spacer(modifier = Modifier.height(RideFlowTheme.spacing.extraLarge))
                RideFlowSecondaryButton(
                    label = if (state.isLoggingOut) "Signing out..." else "Sign out",
                    onClick = { viewModel.onIntent(ProfileIntent.LogoutClicked) },
                    enabled = !state.isLoggingOut,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("MMMM yyyy", Locale.US).format(Date(epochMillis))
