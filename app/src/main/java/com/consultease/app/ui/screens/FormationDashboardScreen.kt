package com.consultease.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.components.BadgeStyle
import com.consultease.app.ui.components.OtpPaymentDialog
import com.consultease.app.ui.components.StatusBadge
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Border
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.Ink
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.PrimaryDark
import com.consultease.app.ui.theme.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun FormationDashboardScreen(viewModel: AppViewModel, formationId: String) {
    val formations by viewModel.formations.collectAsState()
    val medecins by viewModel.medecins.collectAsState()
    val affectations by viewModel.affectations.collectAsState()
    var afficherOtp by remember { mutableStateOf(false) }

    val formation = formations.find { it.id == formationId } ?: return
    val actif = formation.statut == "actif" && formation.dateExpiration > System.currentTimeMillis()
    val joursRestants = ((formation.dateExpiration - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    val enAttente = affectations.filter { it.formationId == formationId && it.statut == "attente" }
    val valides = affectations.filter { it.formationId == formationId && it.statut == "valide" }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text(formation.nom, style = MaterialTheme.typography.titleLarge, color = PrimaryDark)
        Text(formation.ville, color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp, bottom = 18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            StatBox("Statut du compte", Modifier.weight(1f)) { StatusBadge(if (actif) "Actif" else "Désactivé", if (actif) BadgeStyle.SUCCESS else BadgeStyle.DANGER) }
            StatBox(if (actif) "Jours restants" else "À réactiver", Modifier.weight(1f)) { Text(if (actif) "$joursRestants j" else "—", style = MaterialTheme.typography.titleLarge) }
            StatBox("Médecins validés", Modifier.weight(1f)) { Text("${valides.size}", style = MaterialTheme.typography.titleLarge) }
        }

        if (!actif || joursRestants <= 5) {
            Column(Modifier.fillMaxWidth().background(Surface).padding(18.dp).padding(bottom = 16.dp)) {
                Text(
                    if (!actif) "L'abonnement mensuel n'a pas été renouvelé : le compte est désactivé et invisible des patients."
                    else "Il reste $joursRestants jour(s) avant la désactivation automatique du compte.",
                    color = Ink, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp)
                )
                Button(onClick = { afficherOtp = true }, colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                    Text(if (!actif) "Renouveler — 5 000 F" else "Renouveler maintenant — 5 000 F")
                }
            }
        }

        SectionTitle("Demandes de médecins en attente")
        Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 18.dp)) {
            if (enAttente.isEmpty()) Text("Aucune demande en attente.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
            enAttente.forEach { a ->
                val m = medecins.find { it.id == a.medecinId }
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(m?.nom ?: "", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        Text("${m?.specialite ?: ""} · ${m?.telephone ?: ""}", color = InkSoft, fontSize = 13.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.validerAffectation(a) }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)) { Text("Accepter") }
                        Button(onClick = { viewModel.refuserAffectation(a) }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Refuser") }
                    }
                }
                Divider(color = Border)
            }
        }

        SectionTitle("Médecins validés")
        Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 18.dp)) {
            if (valides.isEmpty()) Text("Aucun médecin validé pour l'instant.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
            valides.forEach { a ->
                val m = medecins.find { it.id == a.medecinId }
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(m?.nom ?: "", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        Text(m?.specialite ?: "", color = InkSoft, fontSize = 13.sp)
                    }
                    StatusBadge("Validé", BadgeStyle.SUCCESS)
                }
                Divider(color = Border)
            }
        }
    }

    if (afficherOtp) {
        OtpPaymentDialog(
            montant = 5000,
            onDismiss = { afficherOtp = false },
            onConfirmed = {
                afficherOtp = false
                viewModel.renouvelerAbonnement(formation)
            }
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = InkSoft,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun StatBox(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .background(Surface)
            .padding(14.dp)
    ) {
        Text(label.uppercase(), color = InkSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Column(Modifier.padding(top = 6.dp)) { content() }
    }
}
