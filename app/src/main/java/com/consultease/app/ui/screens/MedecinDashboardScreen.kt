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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.components.BadgeStyle
import com.consultease.app.ui.components.StatusBadge
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Border
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.Ink
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.Primary
import com.consultease.app.ui.theme.PrimaryDark
import com.consultease.app.ui.theme.PrimaryTint
import com.consultease.app.ui.theme.Surface
private enum class MedTab { FORMATIONS, PLANNING, RDV }

@Composable
fun MedecinDashboardScreen(viewModel: AppViewModel, medecinId: String) {
    val medecins by viewModel.medecins.collectAsState()
    val medecin = medecins.find { it.id == medecinId } ?: return
    var tab by remember { mutableStateOf(MedTab.FORMATIONS) }

    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.width(150.dp).padding(vertical = 20.dp)) {
            Text(medecin.nom, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            Text(medecin.specialite, color = InkSoft, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
            Divider(color = Border, modifier = Modifier.padding(vertical = 12.dp))
            SideNavItem("Mes formations", tab == MedTab.FORMATIONS) { tab = MedTab.FORMATIONS }
            SideNavItem("Mon planning", tab == MedTab.PLANNING) { tab = MedTab.PLANNING }
            SideNavItem("Mes rendez-vous", tab == MedTab.RDV) { tab = MedTab.RDV }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
            when (tab) {
                MedTab.FORMATIONS -> MedFormationsSection(viewModel, medecinId)
                MedTab.PLANNING -> MedPlanningSection(viewModel, medecinId)
                MedTab.RDV -> MedRdvSection(viewModel, medecinId)
            }
        }
    }
}

@Composable
private fun SideNavItem(label: String, active: Boolean, onClick: () -> Unit) {
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

@Composable
private fun MedFormationsSection(viewModel: AppViewModel, medecinId: String) {
    val formations by viewModel.formations.collectAsState()
    val affectations by viewModel.affectations.collectAsState()
    val mesAffectations = affectations.filter { it.medecinId == medecinId }
    val dejaRejointes = mesAffectations.map { it.formationId }.toSet()
    val disponibles = formations.filter { it.statut == "actif" && it.dateExpiration > System.currentTimeMillis() && it.id !in dejaRejointes }
    var formationChoisie by remember(disponibles) { mutableStateOf(disponibles.firstOrNull()?.id) }

    SectionTitle("Formations sanitaires")
    Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 16.dp)) {
        if (mesAffectations.isEmpty()) Text("Vous n'avez rejoint aucune formation sanitaire.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
        mesAffectations.forEach { a ->
            val f = formations.find { it.id == a.formationId }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(f?.nom ?: "", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Text(f?.ville ?: "", color = InkSoft, fontSize = 13.sp)
                }
                StatusBadge(if (a.statut == "valide") "Validé" else "En attente", if (a.statut == "valide") BadgeStyle.SUCCESS else BadgeStyle.WARN)
            }
            Divider(color = Border)
        }
    }

    SectionTitle("Rejoindre une nouvelle formation sanitaire")
    Column(Modifier.fillMaxWidth().background(Surface).padding(16.dp)) {
        if (disponibles.isEmpty()) {
            Text("Aucune formation sanitaire disponible.", color = InkSoft, fontSize = 13.sp)
        } else {
            FormationDropdown(disponibles.map { it.id to it.nom }, formationChoisie) { formationChoisie = it }
            Button(
                onClick = { formationChoisie?.let { viewModel.demanderAffectation(medecinId, it) } },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.padding(top = 12.dp)
            ) { Text("Envoyer la demande") }
            Text(
                "Votre ajout doit être validé par la formation sanitaire avant de pouvoir planifier des consultations.",
                color = InkSoft, fontSize = 12.5.sp, modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormationDropdown(options: List<Pair<String, String>>, selected: String?, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.find { it.first == selected }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, nom) ->
                DropdownMenuItem(text = { Text(nom) }, onClick = { onSelected(id); expanded = false })
            }
        }
    }
}

@Composable
private fun MedPlanningSection(viewModel: AppViewModel, medecinId: String) {
    val formations by viewModel.formations.collectAsState()
    val affectations by viewModel.affectations.collectAsState()
    val creneaux by viewModel.creneaux.collectAsState()

    val validees = affectations.filter { it.medecinId == medecinId && it.statut == "valide" }
        .mapNotNull { a -> formations.find { it.id == a.formationId } }

    if (validees.isEmpty()) {
        SectionTitle("Mon planning")
        Column(Modifier.fillMaxWidth().background(Surface).padding(16.dp)) {
            Text("Vous devez d'abord être validé par une formation sanitaire pour créer des créneaux.", color = InkSoft, fontSize = 13.sp)
        }
        return
    }

    var formationId by remember(validees) { mutableStateOf(validees.first().id) }
    var dateStr by remember { mutableStateOf("") }
    var heureStr by remember { mutableStateOf("") }

    SectionTitle("Ajouter un créneau de consultation")
    Column(Modifier.fillMaxWidth().background(Surface).padding(16.dp)) {
        FormationDropdown(validees.map { it.id to it.nom }, formationId) { formationId = it }
        OutlinedTextField(dateStr, { dateStr = it }, label = { Text("Date (jj/mm/aaaa)") }, placeholder = { Text("Ex. 12/09/2026") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        OutlinedTextField(heureStr, { heureStr = it }, label = { Text("Heure (hh:mm)") }, placeholder = { Text("Ex. 09:30") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        Button(
            onClick = {
                if (dateStr.isNotBlank() && heureStr.isNotBlank()) {
                    viewModel.ajouterCreneau(medecinId, formationId, dateStr, heureStr)
                    dateStr = ""; heureStr = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.padding(top = 14.dp)
        ) { Text("Ajouter au planning") }
    }

    SectionTitle("Mes créneaux")
    val mesCreneaux = creneaux.filter { it.medecinId == medecinId }
    Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 16.dp)) {
        if (mesCreneaux.isEmpty()) Text("Aucun créneau créé.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
        mesCreneaux.forEach { c ->
            val f = formations.find { it.id == c.formationId }
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${c.date} — ${c.heure}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(f?.nom ?: "", color = InkSoft, fontSize = 13.sp)
                }
                if (c.reserve) StatusBadge("Réservé", BadgeStyle.MUTED)
                else Button(onClick = { viewModel.supprimerCreneau(c.id) }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Retirer") }
            }
            Divider(color = Border)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedRdvSection(viewModel: AppViewModel, medecinId: String) {
    val patients by viewModel.patients.collectAsState()
    val formations by viewModel.formations.collectAsState()
    val creneaux by viewModel.creneaux.collectAsState()
    val rdvList by viewModel.rendezVous.collectAsState()
    var reprogrammerId by remember { mutableStateOf<String?>(null) }

    val mesRdv = rdvList.filter { it.medecinId == medecinId }.sortedBy { if (it.statut == "a_venir") 0 else 1 }

    SectionTitle("Mes rendez-vous")
    Column(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 16.dp)) {
        if (mesRdv.isEmpty()) Text("Aucun rendez-vous pour l'instant.", color = InkSoft, fontSize = 13.sp, modifier = Modifier.padding(vertical = 14.dp))
        mesRdv.forEach { r ->
            val p = patients.find { it.id == r.patientId }
            val f = formations.find { it.id == r.formationId }
            val c = creneaux.find { it.id == r.creneauId }
            Column(Modifier.padding(vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(p?.nom ?: "Patient", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        Text("${f?.nom ?: ""} · ${c?.date ?: ""} — ${c?.heure ?: ""}", color = InkSoft, fontSize = 13.sp)
                    }
                    StatusBadge(
                        when (r.statut) { "terminee" -> "Consultée"; "annule" -> "Annulé"; else -> "En attente" },
                        when (r.statut) { "terminee" -> BadgeStyle.SUCCESS; "annule" -> BadgeStyle.DANGER; else -> BadgeStyle.WARN }
                    )
                }
                if (r.statut == "a_venir") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        Button(onClick = { viewModel.validerConsultation(r) }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Valider") }
                        OutlinedButton(onClick = { reprogrammerId = r.id }) { Text("Reprogrammer") }
                        Button(onClick = { viewModel.annulerRdv(r, c) }, colors = ButtonDefaults.buttonColors(containerColor = Danger)) { Text("Annuler") }
                    }
                }
                if (reprogrammerId == r.id) {
                    val libres = creneaux.filter { it.medecinId == medecinId && it.formationId == r.formationId && !it.reserve }
                    Column(Modifier.fillMaxWidth().background(PrimaryTint).padding(12.dp).padding(top = 10.dp)) {
                        Text("Choisir un nouveau créneau", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                        if (libres.isEmpty()) {
                            Text("Aucun créneau libre disponible.", color = InkSoft, fontSize = 13.sp)
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                libres.forEach { nc ->
                                    OutlinedButton(onClick = {
                                        viewModel.reprogrammerRdv(r, c, nc)
                                        reprogrammerId = null
                                    }) { Text("${nc.date} — ${nc.heure}") }
                                }
                            }
                        }
                        Text("Annuler", color = InkSoft, fontSize = 12.sp, modifier = Modifier.clickable { reprogrammerId = null }.padding(top = 8.dp))
                    }
                }
            }
            Divider(color = Border)
        }
    }
}
