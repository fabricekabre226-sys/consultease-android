package com.consultease.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.Role
import com.consultease.app.ui.screens.FormationDashboardScreen
import com.consultease.app.ui.screens.HomeScreen
import com.consultease.app.ui.screens.MedecinDashboardScreen
import com.consultease.app.ui.screens.PatientDashboardScreen
import com.consultease.app.ui.screens.RegisterFormationScreen
import com.consultease.app.ui.screens.RegisterMedecinScreen
import com.consultease.app.ui.screens.RegisterPatientScreen
import com.consultease.app.ui.theme.Border
import com.consultease.app.ui.theme.PrimaryDark
import com.consultease.app.ui.theme.Surface

private const val ROUTE_HOME = "home"
private const val ROUTE_REGISTER_FORMATION = "register_formation"
private const val ROUTE_REGISTER_MEDECIN = "register_medecin"
private const val ROUTE_REGISTER_PATIENT = "register_patient"
private const val ROUTE_DASHBOARD = "dashboard"

@Composable
fun ConsultEaseApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val session by viewModel.session.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Consult’", style = MaterialTheme.typography.titleLarge, color = PrimaryDark)
                },
                actions = {
                    if (session != null) {
                        IconButton(onClick = {
                            viewModel.deconnecter()
                            navController.navigate(ROUTE_HOME) { popUpTo(ROUTE_HOME) { inclusive = true } }
                        }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Se déconnecter")
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        val chargement by viewModel.chargementInitial.collectAsState()
        val erreur by viewModel.erreurConnexion.collectAsState()

        Column(modifier = Modifier.padding(padding)) {
            if (chargement) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (erreur != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF5DEDC)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = erreur ?: "",
                        color = Color(0xFFB23A34),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = { viewModel.reessayerConnexion() }) {
                        Text("Réessayer")
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                modifier = Modifier.fillMaxWidth()
            ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onConnected = { navController.navigate(ROUTE_DASHBOARD) },
                    onRegisterFormation = { navController.navigate(ROUTE_REGISTER_FORMATION) },
                    onRegisterMedecin = { navController.navigate(ROUTE_REGISTER_MEDECIN) },
                    onRegisterPatient = { navController.navigate(ROUTE_REGISTER_PATIENT) }
                )
            }
            composable(ROUTE_REGISTER_FORMATION) {
                RegisterFormationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onConnected = { navController.navigate(ROUTE_DASHBOARD) { popUpTo(ROUTE_HOME) } }
                )
            }
            composable(ROUTE_REGISTER_MEDECIN) {
                RegisterMedecinScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onConnected = { navController.navigate(ROUTE_DASHBOARD) { popUpTo(ROUTE_HOME) } }
                )
            }
            composable(ROUTE_REGISTER_PATIENT) {
                RegisterPatientScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onConnected = { navController.navigate(ROUTE_DASHBOARD) { popUpTo(ROUTE_HOME) } }
                )
            }
            composable(ROUTE_DASHBOARD) {
                val s = session
                when (s?.role) {
                    Role.FORMATION -> FormationDashboardScreen(viewModel, s.id)
                    Role.MEDECIN -> MedecinDashboardScreen(viewModel, s.id)
                    Role.PATIENT -> PatientDashboardScreen(viewModel, s.id)
                    null -> HomeScreen(
                        viewModel = viewModel,
                        onConnected = { navController.navigate(ROUTE_DASHBOARD) },
                        onRegisterFormation = { navController.navigate(ROUTE_REGISTER_FORMATION) },
                        onRegisterMedecin = { navController.navigate(ROUTE_REGISTER_MEDECIN) },
                        onRegisterPatient = { navController.navigate(ROUTE_REGISTER_PATIENT) }
                    )
                }
            }
            }
        }
    }
}
