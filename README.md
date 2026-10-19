# AutoLoc — Plateforme de Gestion de Location de Véhicules Multi-Agences

Projet développé dans le cadre du module **Architecture des Systèmes d'Information (ASI 26-27)** — ESPRIT.

---

## 1. Description du projet
**AutoLoc** est une application d'entreprise de gestion de location de véhicules multi-agences.
Elle est conçue avec une **architecture en couches (Layered Architecture)** sous **Spring Boot 3** et **Java 17**, persistant ses données via **Spring Data JPA / Hibernate**.

---

## 2. État d'avancement du Rendu

###  Atelier 1 (Séance 2)
- Configuration complète du projet Spring Boot avec Maven (`pom.xml`).
- Dépendances intégrées : Spring Web, Spring Data JPA, MySQL Connector/J, H2 Database (dev), Lombok, Bean Validation, Spring Boot DevTools, Spring Boot Test.
- Fichier `application.properties` configuré pour MySQL avec variables d'environnement (`DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`) et logs SQL détaillés.
- Profil `application-dev.properties` configuré avec base en mémoire H2 et console web (`/h2-console`).
- Première entité JPA propre `Vehicule` avec ses énumérations `CategorieVehicule` et `StatutVehicule`, annotée avec Lombok ciblé (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`) et `@Table`/`@Column`.
- Génération automatique des tables validée (`spring.jpa.hibernate.ddl-auto=update`).
- Premier commit : `Atelier 1 : init projet Spring Boot + entité Vehicule`.

###  Travail à la Maison (Préparation Atelier 2 / Séance 3)
Création des 8 entités JPA restantes du modèle AutoLoc (sans associations) et de leurs énumérations associées :
1. **`Vehicule`** : immatriculation, marque, modèle, catégorie, tarif journalier, statut
2. **`Agence`** : nom, ville, adresse, téléphone
3. **`Client`** : nom, prénom, email, téléphone, numéro de permis, date d'inscription
4. **`Employe`** : nom, prénom, rôle (`AGENT`, `MANAGER`)
5. **`Equipement`** : libellé
6. **`Reservation`** : date début, date fin, statut (`EN_ATTENTE`, `CONFIRMEE`, `ANNULEE`, `TERMINEE`)
7. **`Contrat`** : date de signature, montant total, validation
8. **`Paiement`** : montant, date de paiement, mode de paiement (`CARTE`, `ESPECES`, `VIREMENT`)
9. **`Maintenance`** : date début, date fin, description

Deuxième commit : `Prépa Atelier 2 : entités restantes sans associations`.

### 🚀 Atelier 2 (Séance 3) — Associations JPA : Cascade et Fetch
- **Implémentation pas à pas de l'association guidée `Contrat ↔ Paiement` :**
  - Côté propriétaire : `Paiement` (`@ManyToOne`, clé étrangère `id_contrat`, `FetchType.LAZY`).
  - Côté inverse : `Contrat` (`@OneToMany`, `mappedBy = "contrat"`, `CascadeType.ALL`, `orphanRemoval = true`, `FetchType.LAZY`).
- **Implémentation de l'ensemble des associations du modèle :**
  - `Agence ↔ Vehicule` : `@OneToMany` / `@ManyToOne`, `id_agence`, `FetchType.LAZY`, sans cascade.
  - `Agence ↔ Employe` : `@OneToMany` / `@ManyToOne`, `id_agence`, `FetchType.LAZY`, sans cascade.
  - `Vehicule ↔ Equipement` : `@ManyToMany`, table de jointure `vehicule_equipement`, utilisation de `Set`, `FetchType.LAZY`, sans cascade.
  - `Client ↔ Reservation` : `@OneToMany` / `@ManyToOne`, `id_client`, `FetchType.LAZY`, `cascade = PERSIST`.
  - `Reservation → Vehicule` : `@ManyToOne`, `id_vehicule`, `FetchType.LAZY`, sans cascade.
  - `Reservation ↔ Contrat` : `@OneToOne`, côté propriétaire `Contrat` (`id_reservation`), cascade `ALL` côté `Reservation`, `FetchType.LAZY`.
  - `Vehicule ↔ Maintenance` : `@OneToMany` / `@ManyToOne`, `id_vehicule`, `FetchType.LAZY`, `cascade = PERSIST`.
- **Note technique `docs/fetch-strategy.md` :** Rédaction complète justifiant les choix d'architecture, de performances (`LAZY` généralisé), d'intégrité des données (`CascadeType` ciblé et `orphanRemoval`).
- Troisième commit : `Atelier 2 : associations, cascade et fetch`.

### 🌟 Fonctionnalités "Pour aller plus loin" (Facultatif)
- Configuration du profil `dev` (H2 en mémoire) permettant le démarrage immédiat et les tests automatiques même sans serveur MySQL local actif.
- `DataInitializer` (`CommandLineRunner`) insérant 3 véhicules de démonstration au démarrage.
- Script wrapper Maven (`mvnw` et `mvnw.cmd`) inclus.

---

## 3. Structure des Packages (`tn.esprit.autoloc`)

```
tn.esprit.autoloc
├── domain                  # Entités JPA et Énumérations métiers (9 entités + 5 énumérations)
│   ├── Agence.java
│   ├── CategorieVehicule.java
│   ├── Client.java
│   ├── Contrat.java
│   ├── Employe.java
│   ├── Equipement.java
│   ├── Maintenance.java
│   ├── ModePaiement.java
│   ├── Paiement.java
│   ├── Reservation.java
│   ├── RoleEmploye.java
│   ├── StatutReservation.java
│   ├── StatutVehicule.java
│   └── Vehicule.java
├── repository              # Interfaces Spring Data JPA
│   └── VehiculeRepository.java
├── service                 # Couche métier (préparée pour les prochains ateliers)
├── web.controller          # Contrôleurs REST (préparé pour les prochains ateliers)
├── web.dto                 # DTOs et Mappers (préparé pour les prochains ateliers)
├── AutolocApiApplication.java # Classe principale Spring Boot
└── DataInitializer.java     # Insertion automatique de véhicules de démo
```

---

## 4. Démarrage et Exécution

### Prérequis
- **Java 17+** installé (`java -version`)
- Optionnel : **MySQL 8.x** démarré sur le port 3306

### Démarrage avec MySQL (Configuration par défaut)
Vérifiez que votre base MySQL est démarrée. L'application créera automatiquement la base `autoloc_db` grâce à `createDatabaseIfNotExist=true` :
```bash
./mvnw spring-boot:run
```
*(ou sous Windows : `.\mvnw.cmd spring-boot:run`)*

Pour spécifier vos identifiants MySQL personnalisés :
```bash
$env:DB_USER="root"
$env:DB_PASSWORD="votre_mot_de_passe"
.\mvnw.cmd spring-boot:run
```

### Démarrage avec le profil Dev (Base H2 en mémoire — Sans MySQL)
Idéal pour tester immédiatement le schéma et les entités sans avoir besoin d'installer ou démarrer MySQL :
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```
- Accès à la console web H2 : [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- JDBC URL : `jdbc:h2:mem:autoloc_db`
- User : `sa` / Mot de passe : *(vide)*

### Lancement des Tests
```bash
.\mvnw.cmd clean test
```

---

## 5. Historique Git des Livrables

```bash
git log --oneline
```
- `<commit-hash>` : **Atelier 2 : associations, cascade et fetch** (associations JPA complètes, CascadeType, orphanRemoval, docs/fetch-strategy.md)
- `83f3c88` : **Prépa Atelier 2 : entités restantes sans associations** (les 8 entités complémentaires, énumérations, repositories)
- `96704f0` : **Atelier 1 : init projet Spring Boot + entité Vehicule** (initialisation Maven, configurations, entité Vehicule)
