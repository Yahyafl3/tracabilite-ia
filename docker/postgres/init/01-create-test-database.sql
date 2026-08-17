-- Base dédiée à la suite de tests backend (profil "test", Hibernate create-drop).
-- La séparer de tracabilite_ia évite qu'un `mvnw test` local ne détruise les données de démonstration.
CREATE DATABASE tracabilite_ia_test OWNER tracabilite;
