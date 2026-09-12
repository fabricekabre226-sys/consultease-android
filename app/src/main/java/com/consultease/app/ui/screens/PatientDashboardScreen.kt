package com.consultease.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import com.consultease.app.data.Creneau
import com.consultease.app.data.FormationSanitaire
import com.consultease.app.data.Medecin
import com.consultease.app.data.SPECIALITES
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.components.BadgeStyle
import com.consultease.app.ui.components.OtpPaymentDialog
import com.consultease.app.ui.components.StatusBadge
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Border
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.Ink
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.Primary
import com.consultease.app.ui.theme.PrimaryDark
import com.consultease.app.ui.theme.PrimaryTint
import com.consultease.app.ui.theme.Sand
import com.consultease.app.ui.theme.Surface

private enum class PatTab { RESERVER, MES_RDV }

private data class Offre(val medecin: Medecin, val formation: FormationSanitaire, val dispo: List<Creneau>)

@Composable
fun PatientDashboardScreen(viewModel: AppViewModel, patientId: String) {
    val patients by viewModel.patients.collectAsState()
    val patient = patients.find { it.id == patientId } ?: return
    var tab by remember { mutableStateOf(PatTab.RESERVER) }

    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.width(150.dp).padding(vertical = 20.dp)) {
            Text(patient.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            Text(patient.telephone, color = InkSoft, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            Divider(color = Border, modifier = Modifier.padding(vertical = 12.dp))
            PatSideNavItem("Prendre rendez-vous", tab == PatTab.RESERVER) { tab = PatTab.RESERVER }
            PatSideNavItem("Mes rendez-vous", tab == PatTab.MES_RDV) { tab = PatTab.MES_RDV }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
            when (tab) {
                PatTab.RESERVER -> PatReserverSection(viewModel, patientId) { tab = PatTab.MES_RDV }
                PatTab.MES_RDV -> PatRdvSection(viewModel, patientId)
            }
        }
    }
}

@Composable
private fun PatSideNavItem(label: String, active: Boolean, onClick: () -> Unit) {
    Text(
        label,
        color = if (active) PrimaryDark else InkSoft,
        fontWeight = FontWeight.Bold,
        fontSize = 13.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (active) PrimaryTint else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatReserverSection(viewModel: AppViewModel, patientId: String, onReserve: () -> Unit) {
    val medecins by viewModel.medecins.collectAsState()
    val formations by viewModel.formations.collectAsState()
    val affectations by viewModel.affectations.collectAsState()
    val creneaux by viewModel.creneaux.collectAsState()

    var specialite by remember { mutableStateOf<String?>(null) }
    var offreChoisie by remember { mutableStateOf<String?>(null) }
    var creneauChoisi by remember { mutableStateOf<String?>(null) }
    var afficherOtp by remember { mutableStateOf(false) }

    SectionTitle("1. Choisir une spécialité")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SPECIALITES.forEach { s ->
            val active = specialite == s
            Text(
                s, fontWeight = FontWeight.Bold, fontSize = 13.5.sp,
                color = if (active) PrimaryDark else Ink,
                modifier = Modifier
                    .background(if (active) PrimaryTint else Surface, RoundedCornerShape(4.dp))
                    .clickable { specialite = s; offreChoisie = null; creneauChoisi = null }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }
    }

    if (specialite != null) {
        SectionTitle("2. Choisir un médecin et un créneau")
        val offres: List<Offre> = affectations.filter { it.statut == "valide" }.mapNotNull { a ->
            val m = medecins.find { it.id == a.medecinId } ?: return@mapNotNull null
            val f = formations.find { it.id == a.formationId } ?: return@mapNotNull null
            if (m.specialite != specialite) return@mapNotNull null
            if (!(f.statut == "actif" && f.dateExpiration > System.currentTimeMillis())) return@mapNotNull null
            val dispo = creneaux.filter { it.medecinId == m.id && it.formationId == f.id && !it.reserve }
            if (dispo.isEmpty()) return@mapNotNull null
            Offre(m, f, dispo)
        }

        if (offres.isEmpty()) {
            Text("Aucun médecin disponible pour cette spécialité actuellement.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
        }

        offres.forEach { o ->
            val key = o.medecin.id + "|" + o.formation.id
            val selected = offreChoisie == key
            Column(Modifier.fillMaxWidth().background(Surface).padding(16.dp).padding(bottom = 10.dp)) {
                Row(
                    Modifier.fillMaxWidth().clickable { offreChoisie = if (selected) null else key; creneauChoisi = null },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(o.medecin.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${o.formation.nom} · ${o.formation.ville} · ${o.dispo.size} créneau(x) libre(s)", color = InkSoft, fontSize = 12.5.sp)
                    }
                    StatusBadge(if (selected) "Masquer" else "Voir les créneaux", BadgeStyle.MUTED)
                }
                if (selected) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                        o.dispo.forEach { c ->
                            val csel = creneauChoisi == c.id
                            Text(
                                "${c.date} — ${c.heure}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp,
                                color = if (csel) androidx.compose.ui.graphics.Color.White else Ink,
                                modifier = Modifier
                                    .background(if (csel) Accent else Sand, RoundedCornerShape(4.dp))
                                    .clickable { creneauChoisi = if (csel) null else c.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                    if (creneauChoisi != null) {
                        Button(
                            onClick = { afficherOtp = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            modifier = Modifier.padding(top = 14.dp)
                        ) { Text("Payer le ticket — 100 F") }
                    }
                }
            }

            if (afficherOtp && selected && creneauChoisi != null) {
                val creneau = o.dispo.find { it.id == creneauChoisi }
                if (creneau != null) {
                    OtpPaymentDialog(
                        montant = 100,
                        onDismiss = { afficherOtp = false },
                        onConfirmed = {
                            afficherOtp = false
                            viewModel.reserverRdv(patientId, o.medecin.id, o.formation.id, creneau) {
                                specialite = null; offreChoisie = null; creneauChoisi = null
                                onReserve()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatRdvSection(viewModel: AppViewModel, patientId: String) {
    val medecins by viewModel.medecins.collectAsState()
    val formations by viewModel.formations.collectAsState()
    val creneaux by viewModel.creneaux.collectAsState()
    val rdvList by viewModel.rendezVous.collectAsState()

    val mesRdv = rdvList.filter { it.patientId == patientId }

    SectionTitle("Mes rendez-vous")
    Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 16.dp)) {
        if (mesRdv.isEmpty()) Text("Vous n'avez aucun rendez-vous.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
        mesRdv.forEach { r ->
            val m = medecins.find { it.id == r.medecinId }
            val f = formations.find { it.id == r.formationId }
            val c = creneaux.find { it.id == r.creneauId }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("${m?.nom ?: ""} — ${m?.specialite ?: ""}", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Text("${f?.nom ?: ""} · ${c?.date ?: ""} — ${c?.heure ?: ""}", color = InkSoft, fontSize = 13.sp)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    StatusBadge(
                        when (r.statut) { "terminee" -> "Consultée"; "annule" -> "Annulé"; else -> "En attente" },
                        when (r.statut) { "terminee" -> BadgeStyle.SUCCESS; "annule" -> BadgeStyle.DANGER; else -> BadgeStyle.WARN }
                    )
                    if (r.statut == "a_venir") {
                        OutlinedButton(
                            onClick = { viewModel.annulerRdv(r, c) },
                            modifier = Modifier.padding(top = 6.dp)
                        ) { Text("Annuler", color = Danger, fontSize = 12.sp) }
                    }
                }
            }
            Divider(color = Border)
        }
    }
}
