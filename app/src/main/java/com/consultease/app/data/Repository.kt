package com.consultease.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository connecté au backend central Consult' (API REST Flask, voir
 * dossier consultease-backend/) au lieu d'une base locale : toutes les
 * données (formations sanitaires, médecins, patients, affectations,
 * créneaux, rendez-vous) sont communes à tous les téléphones qui utilisent
 * l'application, exactement comme la version PWA.
 *
 * Chaque collection est mise en cache dans un StateFlow local, rechargé
 * depuis le serveur après chaque action (création, validation, etc.) — le
 * même principe que la fonction `refresh()` du prototype web.
 */
class ConsultEaseRepository(private val api: ConsultEaseApi) {

    private val _formations = MutableStateFlow<List<FormationSanitaire>>(emptyList())
    val formations: StateFlow<List<FormationSanitaire>> = _formations.asStateFlow()

    private val _medecins = MutableStateFlow<List<Medecin>>(emptyList())
    val medecins: StateFlow<List<Medecin>> = _medecins.asStateFlow()

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _affectations = MutableStateFlow<List<Affectation>>(emptyList())
    val affectations: StateFlow<List<Affectation>> = _affectations.asStateFlow()

    private val _creneaux = MutableStateFlow<List<Creneau>>(emptyList())
    val creneaux: StateFlow<List<Creneau>> = _creneaux.asStateFlow()

    private val _rendezVous = MutableStateFlow<List<RendezVous>>(emptyList())
    val rendezVous: StateFlow<List<RendezVous>> = _rendezVous.asStateFlow()

    private val _erreur = MutableStateFlow<String?>(null)
    val erreur: StateFlow<String?> = _erreur.asStateFlow()

    suspend fun chargerTout() {
        try {
            _formations.value = api.listerFormations()
            _medecins.value = api.listerMedecins()
            _patients.value = api.listerPatients()
            _affectations.value = api.listerAffectations()
            _creneaux.value = api.listerCreneaux()
            _rendezVous.value = api.listerRdv()
            _erreur.value = null
        } catch (e: Exception) {
            _erreur.value = "Impossible de joindre le serveur (${ApiConfig.BASE_URL}). Vérifie que le backend tourne et que l'adresse est correcte."
        }
    }

    private suspend fun refreshFormations() { _formations.value = api.listerFormations() }
    private suspend fun refreshMedecins() { _medecins.value = api.listerMedecins() }
    private suspend fun refreshPatients() { _patients.value = api.listerPatients() }
    private suspend fun refreshAffectations() { _affectations.value = api.listerAffectations() }
    private suspend fun refreshCreneaux() { _creneaux.value = api.listerCreneaux() }
    private suspend fun refreshRdv() { _rendezVous.value = api.listerRdv() }

    // ---- Formation sanitaire ----
    suspend fun inscrireFormation(nom: String, ville: String, telephone: String): FormationSanitaire {
        val f = api.creerFormation(NouvelleFormationRequest(nom, ville, telephone))
        refreshFormations()
        return f
    }

    suspend fun renouvelerAbonnement(formation: FormationSanitaire) {
        api.renouvelerFormation(formation.id)
        refreshFormations()
    }

    suspend fun validerAffectation(affectation: Affectation) {
        api.validerAffectation(affectation.id)
        refreshAffectations()
    }

    suspend fun refuserAffectation(affectation: Affectation) {
        api.supprimerAffectation(affectation.id)
        refreshAffectations()
    }

    // ---- Médecin ----
    suspend fun creerMedecin(nom: String, specialite: String, telephone: String): Medecin {
        val m = api.creerMedecin(NouveauMedecinRequest(nom, specialite, telephone))
        refreshMedecins()
        return m
    }

    suspend fun demanderAffectation(medecinId: String, formationId: String) {
        api.creerAffectation(NouvelleAffectationRequest(medecinId, formationId))
        refreshAffectations()
    }

    suspend fun ajouterCreneau(medecinId: String, formationId: String, date: String, heure: String) {
        api.creerCreneau(NouveauCreneauRequest(medecinId, formationId, date, heure))
        refreshCreneaux()
    }

    suspend fun supprimerCreneau(creneauId: String) {
        api.supprimerCreneau(creneauId)
        refreshCreneaux()
    }

    suspend fun validerConsultation(rdv: RendezVous) {
        api.validerRdv(rdv.id)
        refreshRdv()
    }

    suspend fun annulerRdv(rdv: RendezVous, creneau: Creneau?) {
        api.annulerRdv(rdv.id)
        refreshRdv()
        refreshCreneaux()
    }

    suspend fun reprogrammerRdv(rdv: RendezVous, ancienCreneau: Creneau?, nouveauCreneau: Creneau) {
        api.reprogrammerRdv(rdv.id, ReprogrammerRequest(nouveauCreneau.id))
        refreshRdv()
        refreshCreneaux()
    }

    // ---- Patient ----
    suspend fun creerPatient(nom: String, telephone: String): Patient {
        val p = api.creerPatient(NouveauPatientRequest(nom, telephone))
        refreshPatients()
        return p
    }

    suspend fun reserverRdv(patientId: String, medecinId: String, formationId: String, creneau: Creneau) {
        api.creerRdv(NouveauRdvRequest(patientId, medecinId, formationId, creneau.id))
        refreshRdv()
        refreshCreneaux()
    }
}
