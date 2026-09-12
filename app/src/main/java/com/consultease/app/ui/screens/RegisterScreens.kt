package com.consultease.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.consultease.app.data.SPECIALITES
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.Role
import com.consultease.app.ui.components.OtpPaymentDialog
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.Primary

@Composable
fun RegisterFormationScreen(viewModel: AppViewModel, onBack: () -> Unit, onConnected: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var ville by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }
    var afficherOtp by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Inscrire une formation sanitaire", style = MaterialTheme.typography.titleLarge)
        Text(
            "Frais d'inscription : 5 000 F CFA, puis renouvellement mensuel obligatoire.",
            color = InkSoft, fontSize = 13.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )
        OutlinedTextField(nom, { nom = it }, label = { Text("Nom de la formation sanitaire") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        OutlinedTextField(ville, { ville = it }, label = { Text("Ville") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        OutlinedTextField(telephone, { telephone = it }, label = { Text("Téléphone de contact") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        erreur?.let { Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        Button(
            onClick = {
                if (nom.isBlank() || ville.isBlank() || telephone.isBlank()) {
                    erreur = "Merci de remplir tous les champs."
                } else {
                    erreur = null
                    afficherOtp = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Continuer vers le paiement — 5 000 F") }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Retour") }
    }

    if (afficherOtp) {
        OtpPaymentDialog(
            montant = 5000,
            onDismiss = { afficherOtp = false },
            onConfirmed = {
                afficherOtp = false
                viewModel.inscrireFormation(nom, ville, telephone) { f ->
                    viewModel.connecter(Role.FORMATION, f.id)
                    onConnected()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMedecinScreen(viewModel: AppViewModel, onBack: () -> Unit, onConnected: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var specialite by remember { mutableStateOf(SPECIALITES.first()) }
    var telephone by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }
    var menuOuvert by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Créer un compte médecin", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 18.dp))
        OutlinedTextField(nom, { nom = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))

        ExposedDropdownMenuBox(expanded = menuOuvert, onExpandedChange = { menuOuvert = it }) {
            OutlinedTextField(
                value = specialite,
                onValueChange = {},
                readOnly = true,
                label = { Text("Spécialité") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOuvert) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )
            ExposedDropdownMenu(expanded = menuOuvert, onDismissRequest = { menuOuvert = false }) {
                SPECIALITES.forEach { s ->
                    DropdownMenuItem(text = { Text(s) }, onClick = { specialite = s; menuOuvert = false })
                }
            }
        }

        OutlinedTextField(telephone, { telephone = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        erreur?.let { Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        Button(
            onClick = {
                if (nom.isBlank() || telephone.isBlank()) {
                    erreur = "Merci de remplir tous les champs."
                } else {
                    viewModel.creerMedecin(nom, specialite, telephone) { m ->
                        viewModel.connecter(Role.MEDECIN, m.id)
                        onConnected()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Créer mon compte") }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Retour") }
    }
}

@Composable
fun RegisterPatientScreen(viewModel: AppViewModel, onBack: () -> Unit, onConnected: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Créer un compte patient", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 18.dp))
        OutlinedTextField(nom, { nom = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
        OutlinedTextField(telephone, { telephone = it }, label = { Text("Téléphone") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        erreur?.let { Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp)) }

        Button(
            onClick = {
                if (nom.isBlank() || telephone.isBlank()) {
                    erreur = "Merci de remplir tous les champs."
                } else {
                    viewModel.creerPatient(nom, telephone) { p ->
                        viewModel.connecter(Role.PATIENT, p.id)
                        onConnected()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Créer mon compte") }

        TextButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) { Text("← Retour") }
    }
}
