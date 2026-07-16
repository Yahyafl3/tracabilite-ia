package com.pfa.tracabilite_ia.config;

import com.pfa.tracabilite_ia.enumeration.CreditSectorEnum;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schéma ML centralisé — aligné sur ml-service/sector_schema.py.
 * Le pipeline utilise le même schéma pour tous les secteurs ;
 * le secteur est une feature catégorielle OneHot.
 */
public final class CreditSchemaConfig {

    public static final String SCHEMA_TYPE = "unified";
    public static final String SCHEMA_DESCRIPTION =
            "Le modèle LogisticRegression utilise un pipeline commun pour tous les secteurs. "
                    + "Le secteur est une feature catégorielle encodée en OneHot (cat__sector_*).";
    public static final String SECTOR_HINT =
            "Les données demandées sont adaptées au secteur sélectionné.";

    public static final List<String> NUMERIC_FEATURES = List.of(
            "amount", "monthlyIncome", "companyAgeYears", "paymentIncidents", "debtRatio"
    );
    public static final List<String> CATEGORICAL_FEATURES = List.of("sector");
    public static final List<String> ALL_ML_FEATURES = List.of(
            "amount", "monthlyIncome", "companyAgeYears", "paymentIncidents", "debtRatio", "sector"
    );

    private static final Map<CreditSectorEnum, List<CreditFieldDefinition>> SECTOR_FIELDS = buildSectorFields();

    private CreditSchemaConfig() {
    }

    public static List<CreditFieldDefinition> fieldsFor(CreditSectorEnum sector) {
        return SECTOR_FIELDS.get(sector);
    }

    public static Set<String> allowedMlKeys(CreditSectorEnum sector) {
        return Set.copyOf(ALL_ML_FEATURES);
    }

    public static Map<String, Object> toSchemaResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("schemaType", SCHEMA_TYPE);
        response.put("description", SCHEMA_DESCRIPTION);
        response.put("sectorHint", SECTOR_HINT);
        response.put("sectors", CreditSectorEnum.values());
        response.put("numericFeatures", NUMERIC_FEATURES);
        response.put("categoricalFeatures", CATEGORICAL_FEATURES);
        response.put("allInputFeatures", ALL_ML_FEATURES);
        response.put("sectorFields", toSectorFieldsMap());
        return response;
    }

    private static Map<String, List<Map<String, Object>>> toSectorFieldsMap() {
        Map<String, List<Map<String, Object>>> map = new LinkedHashMap<>();
        for (CreditSectorEnum sector : CreditSectorEnum.values()) {
            map.put(sector.name(), SECTOR_FIELDS.get(sector).stream().map(CreditFieldDefinition::toMap).toList());
        }
        return map;
    }

    private static Map<CreditSectorEnum, List<CreditFieldDefinition>> buildSectorFields() {
        Map<CreditSectorEnum, List<CreditFieldDefinition>> map = new LinkedHashMap<>();
        map.put(CreditSectorEnum.SERVICES, List.of(
                field("amount", "Montant demandé", "Montant du crédit professionnel demandé.", 25000, 1, null),
                field("monthlyIncome", "Revenu mensuel", "Revenu mensuel net de l'entreprise de services.", 15000, 1, null),
                field("companyAgeYears", "Ancienneté de l'entreprise (années)", "Nombre d'années d'activité.", 5, 0, null),
                field("paymentIncidents", "Incidents de paiement", "Incidents de paiement sur les 24 derniers mois.", 0, 0, null),
                field("debtRatio", "Ratio d'endettement (0-1)", "Endettement actuel / revenus (0 = aucun, 1 = maximum).", 0.22, 0, 1.0)
        ));
        map.put(CreditSectorEnum.INDUSTRIE, List.of(
                field("amount", "Montant d'investissement industriel", "Investissement ou crédit d'équipement industriel.", 50000, 1, null),
                field("monthlyIncome", "Chiffre d'affaires mensuel moyen", "Revenus mensuels récurrents de l'activité industrielle.", 20000, 1, null),
                field("companyAgeYears", "Ancienneté du site industriel (années)", "Ancienneté de l'exploitation industrielle.", 8, 0, null),
                field("paymentIncidents", "Incidents de paiement fournisseurs", "Retards ou incidents de paiement récents.", 1, 0, null),
                field("debtRatio", "Ratio d'endettement industriel (0-1)", "Part de la dette dans les revenus mensuels.", 0.35, 0, 1.0)
        ));
        map.put(CreditSectorEnum.COMMERCE, List.of(
                field("amount", "Montant de financement commercial", "Besoin de trésorerie ou d'investissement commercial.", 30000, 1, null),
                field("monthlyIncome", "Revenu mensuel du point de vente", "Revenus mensuels moyens du commerce.", 12000, 1, null),
                field("companyAgeYears", "Ancienneté du commerce (années)", "Durée d'exploitation du commerce.", 4, 0, null),
                field("paymentIncidents", "Incidents de paiement", "Incidents bancaires ou fournisseurs récents.", 0, 0, null),
                field("debtRatio", "Ratio d'endettement (0-1)", "Niveau d'endettement actuel.", 0.28, 0, 1.0)
        ));
        map.put(CreditSectorEnum.TECH, List.of(
                field("amount", "Montant de financement tech", "Montant demandé pour développement ou croissance tech.", 40000, 1, null),
                field("monthlyIncome", "Revenu mensuel récurrent (MRR)", "Revenus mensuels (abonnements, licences, etc.).", 25000, 1, null),
                field("companyAgeYears", "Ancienneté de la startup (années)", "Ancienneté de la structure technologique.", 3, 0, null),
                field("paymentIncidents", "Incidents de paiement", "Incidents de paiement sur la période récente.", 0, 0, null),
                field("debtRatio", "Ratio d'endettement (0-1)", "Endettement rapporté aux revenus mensuels.", 0.18, 0, 1.0)
        ));
        map.put(CreditSectorEnum.AGRICULTURE, List.of(
                field("amount", "Montant de financement agricole", "Crédit pour matériel, intrants ou trésorerie agricole.", 20000, 1, null),
                field("monthlyIncome", "Revenu mensuel de l'exploitation", "Revenus mensuels moyens de l'exploitation.", 8000, 1, null),
                field("companyAgeYears", "Ancienneté de l'exploitation (années)", "Ancienneté de l'activité agricole.", 10, 0, null),
                field("paymentIncidents", "Incidents de paiement", "Retards ou incidents de paiement.", 2, 0, null),
                field("debtRatio", "Ratio d'endettement (0-1)", "Part de la dette dans les revenus mensuels.", 0.40, 0, 1.0)
        ));
        return map;
    }

    private static CreditFieldDefinition field(
            String key,
            String label,
            String help,
            double defaultValue,
            double min,
            Double max
    ) {
        return new CreditFieldDefinition(key, label, help, defaultValue, min, max);
    }

    public record CreditFieldDefinition(
            String key,
            String label,
            String help,
            double defaultValue,
            double min,
            Double max
    ) {
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("key", key);
            map.put("label", label);
            map.put("help", help);
            map.put("defaultValue", defaultValue);
            map.put("type", "number");
            map.put("min", min);
            if (max != null) {
                map.put("max", max);
            }
            map.put("required", true);
            return map;
        }
    }
}
