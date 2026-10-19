# Note Technique : Stratégies de Fetch et de Cascade — AutoLoc

**Module :** Architecture des Systèmes d'Information (ASI 26-27) — ESPRIT  
**Atelier :** Atelier 2 — Associations JPA : Cascade et Fetch  
**Projet :** AutoLoc (`autoloc-api`)  

---

## 1. Tableau récapitulatif des associations JPA

| Association | Multiplicité | Côté Propriétaire | Fetch | Cascade | Justification Métier & Technique |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Contrat → Paiement** | `@OneToMany` / `@ManyToOne` | `Paiement` (`id_contrat`) | **LAZY** | `CascadeType.ALL` + `orphanRemoval = true` | **Relation de composition forte :** Un paiement n'a pas de sens en dehors de son contrat. Enregistrer un contrat propage ses règlements. Supprimer un contrat supprime l'ensemble de ses paiements. Le retrait d'un paiement de la collection entraîne sa suppression physique en base (`orphanRemoval`). |
| **Agence → Vehicule** | `@OneToMany` / `@ManyToOne` | `Vehicule` (`id_agence`) | **LAZY** | **Aucune** | **Survie de l'entité liée :** Un véhicule survit à son agence. Si une agence physique ferme ou est supprimée, la flotte de véhicules reste un actif de l'entreprise et doit pouvoir être réaffectée à une autre agence. |
| **Agence → Employe** | `@OneToMany` / `@ManyToOne` | `Employe` (`id_agence`) | **LAZY** | **Aucune** | **Indépendance des cycles de vie :** Un employé est rattaché administrativement à une agence mais son contrat de travail et ses données RH survivent à la fermeture de l'agence (mobilité, réaffectation). |
| **Vehicule ↔ Equipement** | `@ManyToMany` | `Vehicule` (`vehicule_equipement`) | **LAZY** | **Aucune** | **Catalogue d'équipements partagés :** Les équipements (GPS, coffre de toit, siège bébé) sont transverses et interchangeables entre véhicules. L'association passe par une table de jointure sans cascade afin de ne jamais supprimer un équipement si un véhicule est retiré. |
| **Client → Reservation** | `@OneToMany` / `@ManyToOne` | `Reservation` (`id_client`) | **LAZY** | `CascadeType.PERSIST` | **Création combinée et traçabilité :** Permet d'enregistrer commodément un nouveau client en même temps qu'une première réservation. En revanche, aucune cascade de suppression n'est appliquée (`REMOVE`), car l'historique des réservations doit être préservé pour les obligations comptables et juridiques. |
| **Reservation → Vehicule** | `@ManyToOne` (unidirectionnel) | `Reservation` (`id_vehicule`) | **LAZY** | **Aucune** | **Indépendance stricte :** La réservation référence le véhicule sélectionné. Le cycle de vie d'un véhicule (disponibilité, acquisition, vente) est totalement découplé des actes de réservation. |
| **Reservation ↔ Contrat** | `@OneToOne` | `Contrat` (`id_reservation`) | **LAZY** | `CascadeType.ALL` (côté `Reservation`) | **Cycle de vie synchronisé (1-à-1) :** Le contrat est la concrétisation juridique directe d'une réservation. La réservation pilote le contrat : persister, mettre à jour ou annuler/supprimer la réservation propage ces opérations sur le contrat associé. |
| **Vehicule → Maintenance** | `@OneToMany` / `@ManyToOne` | `Maintenance` (`id_vehicule`) | **LAZY** | `CascadeType.PERSIST` | **Carnet d'entretien :** Permet lors de la saisie d'enregistrer une maintenance planifiée avec le véhicule. Pas de cascade de suppression totale pour éviter les pertes d'historique technique et de conformité en cas de purge. |

---

## 2. Justification des choix de chargement (`FetchType`)

### Pourquoi `FetchType.LAZY` est retenu pour l'ensemble des associations :
1. **Comportement par défaut dangereux en EAGER :**  
   Par défaut en JPA/Hibernate, les relations singulières (`@ManyToOne` et `@OneToOne`) sont configurées en `FetchType.EAGER`. Cela conduit rapidement au syndrome du « gouffre de performance » (requêtes récursives chargeant tout le graphe d'objets en mémoire, problème des $N+1$ requêtes).
2. **Performance et consommation mémoire :**  
   Pour l'architecture AutoLoc, forcer `fetch = FetchType.LAZY` sur chaque `@ManyToOne` et `@OneToOne` assure qu'une entité (ex. un véhicule ou une réservation) est chargée de manière légère, sans requêtes de jointures massives non sollicitées.
3. **Maîtrise au niveau de la couche Service / Repository :**  
   Les chargements de collections ou d'entités associées nécessaires à un cas d'utilisation précis seront réalisés de manière explicite et optimisée via des requêtes `JOIN FETCH` (Atelier 7 et 10).

---

## 3. Justification des choix de propagation (`CascadeType` & `orphanRemoval`)

1. **Règle générale de sûreté (Clean Architecture) :**  
   La cascade ne doit être utilisée que dans les relations de **composition** (relation « tout / partie ») où la partie n'a aucune existence métier sans le tout.
2. **`Contrat ↔ Paiement` (`ALL` + `orphanRemoval`) :**  
   C'est l'exemple type de la composition pure : un paiement bancaire ne peut exister sans référence à un contrat de location. Supprimer une ligne de paiement de la collection doit physiquement l'effacer de la table SQL (`orphanRemoval = true`).
3. **Absence de cascade destructrice sur les relations transverses :**  
   Sur `Agence → Vehicule`, `Agence → Employe`, et `Vehicule ↔ Equipement`, aucune cascade de type `REMOVE` n'est configurée afin de garantir l'intégrité référentielle et éviter les suppressions en chaîne accidentelles de données métier critiques.
