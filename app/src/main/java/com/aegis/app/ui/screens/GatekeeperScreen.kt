package com.aegis.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegis.app.ui.viewmodel.PreferencesViewModel

@Composable
fun GatekeeperScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: PreferencesViewModel = hiltViewModel()
) {
    val loading by viewModel.loading.collectAsState()
    val prefs by viewModel.prefs.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(loading) {
        if (!loading) {
            // A brand new user starts with the default ALL_MODULES, but we can tell if they are new
            // if their preferences ID is null (meaning no row in DB).
            // Actually, the easiest way is checking if the DB row exists.
            // If it returned null, we set a default in ViewModel but ID remains null.
            if (prefs.id == null) {
                onNavigateToOnboarding()
            } else {
                onNavigateToMain()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF3B82F6))
    }
}
