package com.consultease.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.consultease.app.ui.theme.Accent
import com.consultease.app.ui.theme.AccentTint
import com.consultease.app.ui.theme.Danger
import com.consultease.app.ui.theme.InkSoft
import com.consultease.app.ui.theme.PrimaryTint
import com.consultease.app.ui.theme.Sand
import com.consultease.app.ui.theme.Surface
import kotlin.random.Random

private data class Operateur(val id: String, val label: String)
private val OPERATEURS = listOf(
    Operateur("orange", "Orange Money"),
    Operateur("moov", "Moov Money"),
    Operateur("telecel", "Telecel Money")
)

/**
 * Dialogue de paiement mobile money. Le code OTP est simulé et affiché
 * à l'écran (pas de véritable envoi SMS ni d'intégration opérateur réelle) :
 * il faudra brancher une vraie API d'agrégateur mobile money en production.
 */
@Composable
fun OtpPaymentDialog(
    montant: Int,
    onDismiss: () -> Unit,
    onConfirmed: () -> Unit
) {
    var operateur by remember { mutableStateOf<Operateur?>(null) }
    var telephone by remember { mutableStateOf("") }
    var codeEnvoye by remember { mutableStateOf<String?>(null) }
    var saisie by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Surface, RoundedCornerShape(4.dp))
                .padding(24.dp)
        ) {
            Text("Paiement mobile money", style = MaterialTheme.typography.titleLarge)
            Text(
                "Montant à payer : $montant F CFA",
                color = InkSoft,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (codeEnvoye == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OPERATEURS.forEach { op ->
                        val selected = operateur?.id == op.id
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) PrimaryTint else Sand, RoundedCornerShape(4.dp))
                                .clickable { operateur = op }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(op.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Numéro mobile money") },
                    placeholder = { Text("Ex. 70123456") },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )

                erreur?.let {
                    Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }

                Button(
                    onClick = {
                        if (operateur == null) { erreur = "Choisissez un opérateur."; return@Button }
                        if (telephone.trim().length < 8) { erreur = "Numéro de téléphone invalide."; return@Button }
                        erreur = null
                        codeEnvoye = (1000 + Random.nextInt(9000)).toString()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
                ) {
                    Text("Envoyer le code de confirmation")
                }
            } else {
                Text(
                    "Un code a été envoyé au $telephone (${operateur?.label}). Code de test :",
                    color = InkSoft,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(
                    codeEnvoye ?: "",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Sand, RoundedCornerShape(4.dp))
                        .padding(14.dp)
                )
                OutlinedTextField(
                    value = saisie,
                    onValueChange = { saisie = it },
                    label = { Text("Entrer le code reçu") },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
                erreur?.let {
                    Text(it, color = Danger, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                }
                Button(
                    onClick = {
                        if (saisie.trim() == codeEnvoye) {
                            onConfirmed()
                        } else {
                            erreur = "Code incorrect, réessayez."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
                ) {
                    Text("Valider le paiement")
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 8.dp)) {
                Text("Annuler", color = InkSoft)
            }
        }
    }
}

