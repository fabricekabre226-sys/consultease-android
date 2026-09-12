# Consult’ — application Android de gestion des consultations médicales

Application Android native (Kotlin + Jetpack Compose) reprenant les mêmes
règles métier que le prototype web et la PWA : formations sanitaires,
médecins et patients, avec paiement mobile money simulé par OTP.

**Elle est maintenant connectée au backend central** (dossier
`consultease-backend/` livré séparément, Flask + SQLite) au lieu d'avoir sa
propre base locale : toutes les données sont partagées entre l'app Android,
la PWA, et tous les téléphones qui l'utilisent.

## ⚠️ Obtenir un fichier .apk installable

Je n'ai pas pu compiler l'APK moi-même (pas de SDK Android ni d'accès
réseau dans l'environnement où ce code a été généré). Deux façons d'en
obtenir un, au choix :

### Option A — Compilation automatique dans le cloud (aucune installation)

1. Crée un dépôt GitHub (public ou privé) et pousse-y tout le contenu de ce
   dossier `ConsultEase` (le workflow est déjà inclus dans
   `.github/workflows/build-apk.yml`).
2. Va dans l'onglet **Actions** du dépôt sur GitHub : la compilation se
   lance automatiquement (~3-5 minutes).
3. Une fois terminé, ouvre le run et télécharge l'artefact
   **ConsultEase-debug-apk** (un .zip contenant `app-debug.apk`).
4. Transfère ce .apk sur ton téléphone (câble USB, lien de téléchargement,
   WhatsApp/Telegram à toi-même…), puis ouvre-le pour l'installer — il
   faudra probablement autoriser "Installer des applications inconnues"
   pour l'application que tu utilises pour ouvrir le fichier.

### Option B — Compiler avec Android Studio

1. Installer **Android Studio**, ouvrir ce dossier (`File > Open`).
2. Laisser Gradle se synchroniser (regénère automatiquement `gradlew`).
3. `Build > Build App Bundle(s) / APK(s) > Build APK(s)`.
4. Le .apk généré se trouve dans `app/build/outputs/apk/debug/`.
5. Copie-le sur ton téléphone et installe-le.

## Se connecter au backend

Avant de lancer l'app (ou avant de la compiler pour un test réel), vérifie
l'adresse du serveur dans
`app/src/main/java/com/consultease/app/data/ApiService.kt` :

```kotlin
object ApiConfig {
    var BASE_URL: String = "http://10.0.2.2:5000/api/"
}
```

- **Émulateur Android Studio** : `10.0.2.2` fonctionne tel quel (il pointe
  vers le `localhost` de l'ordinateur qui héberge l'émulateur).
- **Téléphone physique** (donc pour l'APK que tu vas installer et tester) :
  remplace par l'adresse IP locale de l'ordinateur qui fait tourner
  `python app.py` sur le même réseau Wi-Fi, par ex.
  `http://192.168.1.20:5000/api/` (trouve cette adresse avec `ipconfig`
  sous Windows ou `ip a` / `ifconfig` sous Linux/Mac). Le téléphone et
  l'ordinateur doivent être sur le **même réseau Wi-Fi**.
- **Backend déployé sur un serveur public** : mets l'URL publique.

Après avoir changé cette adresse, il faut recompiler l'APK (recommencer
l'option A ou B ci-dessus) pour que le changement soit pris en compte.

## Architecture

- **`data/Entities.kt`** — modèles de données (`FormationSanitaire`,
  `Medecin`, `Patient`, `Affectation`, `Creneau`, `RendezVous`).
- **`data/ApiService.kt`** — interface Retrofit décrivant tous les
  endpoints du backend, et `ApiClient` qui construit le client HTTP.
- **`data/Repository.kt`** — `ConsultEaseRepository` : appelle l'API et
  garde les listes à jour dans des `StateFlow`, rechargées après chaque
  action (inscription, validation, réservation…).
- **`ui/AppViewModel.kt`** — état de session, chargement initial,
  gestion de l'erreur de connexion au serveur, et toute la logique métier.
- **`ui/screens/`** — un écran par rôle : accueil/connexion, inscriptions,
  tableau de bord formation sanitaire, médecin, patient.
- **`ui/components/OtpPaymentDialog.kt`** — dialogue de paiement mobile
  money.
- **`ui/navigation/NavGraph.kt`** — navigation Compose entre les écrans,
  avec un bandeau de chargement / erreur de connexion au backend.

## Règles métier reprises

- Formation sanitaire : inscription à 5 000 F, renouvellement mensuel
  obligatoire, sinon désactivation ; validation des médecins qui demandent
  à la rejoindre.
- Médecin : demande de rattachement à une formation sanitaire (statut « en
  attente » jusqu'à validation), planning de créneaux, gestion des
  rendez-vous (annuler, reprogrammer, valider la consultation).
- Patient : recherche par spécialité, choix du médecin/formation, paiement
  du ticket à 100 F, suivi des rendez-vous.

## Limites à documenter dans le mémoire

- **Paiement mobile money simulé** : le code OTP est généré et affiché
  directement à l'écran (aucune intégration réelle avec les API Orange
  Money, Moov Money ou Telecel Money). Une version de production
  nécessiterait un compte marchand et l'intégration de leurs API
  respectives (souvent via un agrégateur comme CinetPay ou PayDunya au
  Burkina Faso).
- **Authentification simplifiée** : pas de mot de passe ni de vérification
  d'identité — la « connexion » se fait par sélection du compte. À
  sécuriser avant toute mise en production (mot de passe, ou OTP de
  connexion en plus de l'OTP de paiement).
- **Trafic HTTP non chiffré** (`usesCleartextTraffic="true"`) : acceptable
  pour tester en local avec le backend Flask de développement, mais à
  remplacer par HTTPS avant toute mise en production.
- **APK non signé "release"** : celui produit par ce workflow/Android
  Studio est un APK de debug, suffisant pour tester sur ton téléphone mais
  pas pour publier sur le Play Store (il faudrait une clé de signature de
  release).
