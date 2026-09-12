package com.consultease.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Adresse du backend Consult' (Flask, voir dossier consultease-backend).
 *
 * - Émulateur Android Studio : 10.0.2.2 pointe vers le "localhost" de
 *   l'ordinateur qui héberge l'émulateur -> valeur par défaut ci-dessous.
 * - Téléphone physique sur le même réseau Wi-Fi que l'ordinateur qui fait
 *   tourner "python app.py" : remplace par l'adresse IP locale de cet
 *   ordinateur, par ex. "http://192.168.1.20:5000/api/"
 *   (sur l'ordinateur : `ipconfig` sous Windows, `ifconfig`/`ip a` sous
 *   Linux/Mac pour trouver cette adresse).
 * - Backend déployé sur un serveur : mets l'URL publique du serveur.
 */
object ApiConfig {
    var BASE_URL: String = "http://10.0.2.2:5000/api/"
}

data class NouvelleFormationRequest(val nom: String, val ville: String, val telephone: String)
data class NouveauMedecinRequest(val nom: String, val specialite: String, val telephone: String)
data class NouveauPatientRequest(val nom: String, val telephone: String)
data class NouvelleAffectationRequest(val medecinId: String, val formationId: String)
data class NouveauCreneauRequest(val medecinId: String, val formationId: String, val date: String, val heure: String)
data class NouveauRdvRequest(val patientId: String, val medecinId: String, val formationId: String, val creneauId: String)
data class ReprogrammerRequest(val nouveauCreneauId: String)

interface ConsultEaseApi {
    @GET("formations")
    suspend fun listerFormations(): List<FormationSanitaire>

    @POST("formations")
    suspend fun creerFormation(@Body body: NouvelleFormationRequest): FormationSanitaire

    @POST("formations/{id}/renouveler")
    suspend fun renouvelerFormation(@Path("id") id: String): FormationSanitaire

    @GET("medecins")
    suspend fun listerMedecins(): List<Medecin>

    @POST("medecins")
    suspend fun creerMedecin(@Body body: NouveauMedecinRequest): Medecin

    @GET("patients")
    suspend fun listerPatients(): List<Patient>

    @POST("patients")
    suspend fun creerPatient(@Body body: NouveauPatientRequest): Patient

    @GET("affectations")
    suspend fun listerAffectations(): List<Affectation>

    @POST("affectations")
    suspend fun creerAffectation(@Body body: NouvelleAffectationRequest): Affectation

    @POST("affectations/{id}/valider")
    suspend fun validerAffectation(@Path("id") id: String): Affectation

    @DELETE("affectations/{id}")
    suspend fun supprimerAffectation(@Path("id") id: String)

    @GET("creneaux")
    suspend fun listerCreneaux(): List<Creneau>

    @POST("creneaux")
    suspend fun creerCreneau(@Body body: NouveauCreneauRequest): Creneau

    @DELETE("creneaux/{id}")
    suspend fun supprimerCreneau(@Path("id") id: String)

    @GET("rdv")
    suspend fun listerRdv(): List<RendezVous>

    @POST("rdv")
    suspend fun creerRdv(@Body body: NouveauRdvRequest): RendezVous

    @POST("rdv/{id}/valider")
    suspend fun validerRdv(@Path("id") id: String): RendezVous

    @POST("rdv/{id}/annuler")
    suspend fun annulerRdv(@Path("id") id: String): RendezVous

    @POST("rdv/{id}/reprogrammer")
    suspend fun reprogrammerRdv(@Path("id") id: String, @Body body: ReprogrammerRequest): RendezVous
}

object ApiClient {
    fun create(): ConsultEaseApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConsultEaseApi::class.java)
    }
}
