package com.consultease.app.data

// Statuts utilisés dans l'application
object StatutFormation {
    const val ACTIF = "actif"
    const val DESACTIVE = "desactive"
}

object StatutAffectation {
    const val ATTENTE = "attente"
    const val VALIDE = "valide"
}

object StatutRdv {
    const val A_VENIR = "a_venir"
    const val TERMINEE = "terminee"
    const val ANNULE = "annule"
}

val SPECIALITES = listOf(
    "Médecine générale", "Pédiatrie", "Gynécologie", "Cardiologie",
    "Dermatologie", "Ophtalmologie", "ORL", "Dentisterie"
)

data class FormationSanitaire(
    val id: String,
    val nom: String,
    val ville: String,
    val telephone: String,
    val statut: String,
    val dateExpiration: Long
)

data class Medecin(
    val id: String,
    val nom: String,
    val specialite: String,
    val telephone: String
)

data class Patient(
    val id: String,
    val nom: String,
    val telephone: String
)

data class Affectation(
    val id: String,
    val medecinId: String,
    val formationId: String,
    val statut: String
)

data class Creneau(
    val id: String,
    val medecinId: String,
    val formationId: String,
    val date: String,
    val heure: String,
    val reserve: Boolean
)

data class RendezVous(
    val id: String,
    val patientId: String,
    val medecinId: String,
    val formationId: String,
    val creneauId: String,
    val statut: String
)
