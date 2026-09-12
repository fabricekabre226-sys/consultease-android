package com.consultease.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.consultease.app.data.Affectation
import com.consultease.app.data.ApiClient
import com.consultease.app.data.Creneau
import com.consultease.app.data.ConsultEaseRepository
import com.consultease.app.data.FormationSanitaire
import com.consultease.app.data.Medecin
import com.consultease.app.data.Patient
import com.consultease.app.data.RendezVous
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Role { FORMATION, MEDECIN, PATIENT }
data class Session(val role: Role, val id: String)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ConsultEaseRepository(ApiClient.create())

    val formations: StateFlow<List<FormationSanitaire>> =
        repository.formations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val medecins: StateFlow<List<Medecin>> =
        repository.medecins.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val patients: StateFlow<List<Patient>> =
        repository.patients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val affectations: StateFlow<List<Affectation>> =
        repository.affectations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val creneaux: StateFlow<List<Creneau>> =
        repository.creneaux.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rendezVous: StateFlow<List<RendezVous>> =
        repository.rendezVous.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val erreurConnexion: StateFlow<String?> =
        repository.erreur.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session

    private val _chargementInitial = MutableStateFlow(true)
    val chargementInitial: StateFlow<Boolean> = _chargementInitial

    init {
        viewModelScope.launch {
            repository.chargerTout()
            _chargementInitial.value = false
        }
    }

    fun reessayerConnexion() {
        viewModelScope.launch { repository.chargerTout() }
    }

    fun connecter(role: Role, id: String) {
        _session.value = Session(role, id)
    }

    fun deconnecter() {
        _session.value = null
    }

    // ---- Actions Formation sanitaire ----
    fun inscrireFormation(nom: String, ville: String, telephone: String, onDone: (FormationSanitaire) -> Unit) {
        viewModelScope.launch {
            val f = repository.inscrireFormation(nom, ville, telephone)
            onDone(f)
        }
    }

    fun renouvelerAbonnement(formation: FormationSanitaire) {
        viewModelScope.launch { repository.renouvelerAbonnement(formation) }
    }

    fun validerAffectation(affectation: Affectation) {
        viewModelScope.launch { repository.validerAffectation(affectation) }
    }

    fun refuserAffectation(affectation: Affectation) {
        viewModelScope.launch { repository.refuserAffectation(affectation) }
    }

    // ---- Actions Médecin ----
    fun creerMedecin(nom: String, specialite: String, telephone: String, onDone: (Medecin) -> Unit) {
        viewModelScope.launch {
            val m = repository.creerMedecin(nom, specialite, telephone)
            onDone(m)
        }
    }

    fun demanderAffectation(medecinId: String, formationId: String) {
        viewModelScope.launch { repository.demanderAffectation(medecinId, formationId) }
    }

    fun ajouterCreneau(medecinId: String, formationId: String, date: String, heure: String) {
        viewModelScope.launch { repository.ajouterCreneau(medecinId, formationId, date, heure) }
    }

    fun supprimerCreneau(creneauId: String) {
        viewModelScope.launch { repository.supprimerCreneau(creneauId) }
    }

    fun validerConsultation(rdv: RendezVous) {
        viewModelScope.launch { repository.validerConsultation(rdv) }
    }

    fun annulerRdv(rdv: RendezVous, creneau: Creneau?) {
        viewModelScope.launch { repository.annulerRdv(rdv, creneau) }
    }

    fun reprogrammerRdv(rdv: RendezVous, ancienCreneau: Creneau?, nouveauCreneau: Creneau) {
        viewModelScope.launch { repository.reprogrammerRdv(rdv, ancienCreneau, nouveauCreneau) }
    }

    // ---- Actions Patient ----
    fun creerPatient(nom: String, telephone: String, onDone: (Patient) -> Unit) {
        viewModelScope.launch {
            val p = repository.creerPatient(nom, telephone)
            onDone(p)
        }
    }

    fun reserverRdv(patientId: String, medecinId: String, formationId: String, creneau: Creneau, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.reserverRdv(patientId, medecinId, formationId, creneau)
            onDone()
        }
    }
}
