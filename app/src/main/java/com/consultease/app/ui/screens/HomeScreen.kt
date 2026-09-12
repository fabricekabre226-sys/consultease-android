package com.consultease.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.consultease.app.data.FormationSanitaire
import com.consultease.app.data.Medecin
import com.consultease.app.data.Patient
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.Role
import com.consultease.app.ui.components.BadgeStyle
import com.consultease.app.ui.components.StatusBadge
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Border
import com.consultease.app.ui.theme.Ink
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.Primary
import com.consultease.app.ui.theme.PrimaryDark
import com.consultease.app.ui.theme.Surface

private enum class HomeTab { FORMATION, MEDECIN, PATIENT }

@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onConnected: () -> Unit,
    onRegisterFormation: () -> Unit,
    onRegisterMedecin: () -> Unit,
    onRegisterPatient: () -> Unit
) {
    val formations by viewModel.formations.collectAsState()
    val medecins by viewModel.medecins.collectAsState()
    val patients by viewModel.patients.collectAsState()

    var tab by remember { mutableStateOf(HomeTab.FORMATION) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text(
            "La consultation, du rendez-vous au dossier, en un seul endroit.",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryDark
        )
        Text(
            "Les formations sanitaires gèrent leurs médecins, les médecins organisent leur planning, les patients réservent et paient leur ticket en ligne.",
            color = InkSoft,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            TabLabel("Formation sanitaire", tab == HomeTab.FORMATION) { tab = HomeTab.FORMATION }
            TabLabel("Médecin", tab == HomeTab.MEDECIN) { tab = HomeTab.MEDECIN }
            TabLabel("Patient", tab == HomeTab.PATIENT) { tab = HomeTab.PATIENT }
        }
        Divider(color = Border, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))

        when (tab) {
            HomeTab.FORMATION -> {
                CardContainer {
                    if (formations.isEmpty()) EmptyText("Aucune formation sanitaire enregistrée.")
                    formations.forEach { f ->
                        AccountRow(
                            titre = f.nom,
                            sousTitre = f.ville,
                            badge = {
                                val actif = f.statut == "actif" && f.dateExpiration > System.currentTimeMillis()
                                StatusBadge(if (actif) "Active" else "Désactivée", if (actif) BadgeStyle.SUCCESS else BadgeStyle.DANGER)
                            },
                            onClick = { viewModel.connecter(Role.FORMATION, f.id); onConnected() }
                        )
                    }
                }
                Button(
                    onClick = onRegisterFormation,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) { Text("Inscrire une formation sanitaire") }
            }
            HomeTab.MEDECIN -> {
                CardContainer {
                    if (medecins.isEmpty()) EmptyText("Aucun médecin enregistré.")
                    medecins.forEach { m ->
                        AccountRow(
                            titre = m.nom,
                            sousTitre = m.specialite,
                            badge = null,
                            onClick = { viewModel.connecter(Role.MEDECIN, m.id); onConnected() }
                        )
                    }
                }
                Button(
                    onClick = onRegisterMedecin,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) { Text("Créer un compte médecin") }
            }
            HomeTab.PATIENT -> {
                CardContainer {
                    if (patients.isEmpty()) EmptyText("Aucun patient enregistré.")
                    patients.forEach { p ->
                        AccountRow(
                            titre = p.nom,
                            sousTitre = p.telephone,
                            badge = null,
                            onClick = { viewModel.connecter(Role.PATIENT, p.id); onConnected() }
                        )
                    }
                }
                Button(
                    onClick = onRegisterPatient,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) { Text("Créer un compte patient") }
            }
        }
    }
}

@Composable
private fun TabLabel(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        label,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = if (active) PrimaryDark else InkSoft,
        modifier = Modifier.clickable(onClick = onClick).padding(bottom = 6.dp)
    )
}

@Composable
private fun CardContainer(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(0.dp))
            .padding(4.dp)
    ) { content() }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
private fun AccountRow(titre: String, sousTitre: String, badge: (@Composable () -> Unit)?, onClick: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(titre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ink)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(sousTitre, color = InkSoft, fontSize = 13.sp)
                    badge?.let {
                        Text(" · ", color = InkSoft, fontSize = 13.sp)
                        it()
                    }
                }
            }
            OutlinedButton(onClick = onClick) { Text("Se connecter") }
        }
        Divider(color = Border)
    }
}
