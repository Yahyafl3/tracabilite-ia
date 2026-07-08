# 🤖 ML Integration - TODO pour Badderdine

## ✅ Ce qui est FAIT (par Yahya):

### 1. Service ML Python (ml-service/)
- ✅ `app.py` - Flask API complète avec endpoints:
  - `GET /health` - Health check
  - `POST /predict` - Prédiction ML (crédit)
  - `POST /train` - Réentraînement du modèle
- ✅ `requirements.txt` - Dépendances Python
- ✅ `Dockerfile` - Image Docker ML service
- ✅ Modèle RandomForest pré-entraîné avec données simulées

### 2. Backend Java - DTOs et Interface
- ✅ `CreditAnalysisRequest.java` - Request DTO pour analyse crédit
- ✅ `MLPredictionResponse.java` - Response DTO depuis Python ML
- ✅ `MLDecisionService.java` - Interface du service
- ✅ `docker-compose.yml` - Service ml-service ajouté (port 5000)

---

## ❌ Ce qui MANQUE (pour Badderdine):

### 1️⃣ **COMPLÉTER LES ENTITIES** (URGENT)

Les entities sont VIDES actuellement. Il faut les compléter avec JPA annotations:

#### `Decision.java`
```java
@Entity
@Table(name = "decisions")
public class Decision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String contenu;
    private String contexte;
    private LocalDateTime dateDecision;
    
    @Enumerated(EnumType.STRING)
    private StatutDecisionEnum statut;
    
    // Hash Chain pour traçabilité
    @Column(length = 64, nullable = false)
    private String currentHash;
    
    @Column(length = 64)
    private String previousHash;
    
    // Relations
    @ManyToOne
    @JoinColumn(name = "systeme_ia_id")
    private SystemeIA systemeIA;
    
    @OneToMany(mappedBy = "decision", cascade = CascadeType.ALL)
    private List<ValidationAction> validations = new ArrayList<>();
    
    // Score de confiance ML
    private Double scoreConfiance;
    
    // Getters, setters, constructors
}
```

#### `SystemeIA.java`
```java
@Entity
@Table(name = "systemes_ia")
@DiscriminatorValue("SYSTEME_IA")
public class SystemeIA extends Utilisateur {
    private String typeModele; // "RandomForest", "Neural Network", etc.
    private String version;
    
    @OneToMany(mappedBy = "systemeIA")
    private List<Decision> decisions = new ArrayList<>();
    
    // Getters, setters, constructors
}
```

#### `Utilisateur.java` (base class)
```java
@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_utilisateur")
public abstract class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
    
    private LocalDateTime dateCreation;
    private boolean actif = true;
    
    // Getters, setters, constructors
}
```

---

### 2️⃣ **CRÉER LES REPOSITORIES**

Créer dans `repository/`:

```java
// DecisionRepository.java
public interface DecisionRepository extends JpaRepository<Decision, Long> {
    List<Decision> findBySystemeIAId(Long systemeIAId);
    List<Decision> findByStatut(StatutDecisionEnum statut);
}

// SystemeIARepository.java
public interface SystemeIARepository extends JpaRepository<SystemeIA, Long> {
    Optional<SystemeIA> findByUsername(String username);
}
```

---

### 3️⃣ **IMPLÉMENTER MLDecisionServiceImpl**

Créer `service/impl/MLDecisionServiceImpl.java`:

```java
@Service
@RequiredArgsConstructor
public class MLDecisionServiceImpl implements MLDecisionService {
    
    private final RestTemplate restTemplate;
    private final DecisionRepository decisionRepository;
    private final SystemeIARepository systemeIARepository;
    
    @Value("${ml.service.url:http://ml-service:5000}")
    private String mlServiceUrl;
    
    @Override
    public MLPredictionResponse genererDecisionML(CreditAnalysisRequest request) {
        try {
            String url = mlServiceUrl + "/predict";
            ResponseEntity<MLPredictionResponse> response = restTemplate.postForEntity(
                url, 
                request, 
                MLPredictionResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erreur ML Service: " + e.getMessage());
        }
    }
    
    @Override
    public Decision creerDecisionDepuisML(CreditAnalysisRequest request, Long systemeIaId) {
        // 1. Appeler ML service
        MLPredictionResponse mlResponse = genererDecisionML(request);
        
        // 2. Récupérer SystemeIA
        SystemeIA systemeIA = systemeIARepository.findById(systemeIaId)
            .orElseThrow(() -> new ResourceNotFoundException("SystemeIA not found"));
        
        // 3. Calculer hash (Hash Chain)
        Decision lastDecision = decisionRepository.findTopByOrderByIdDesc();
        String previousHash = (lastDecision != null) ? lastDecision.getCurrentHash() : "0";
        String currentHash = calculateHash(mlResponse.getContenu(), previousHash);
        
        // 4. Créer Decision
        Decision decision = new Decision();
        decision.setContenu(mlResponse.getContenu());
        decision.setContexte(mlResponse.getContexte());
        decision.setDateDecision(LocalDateTime.now());
        decision.setStatut(StatutDecisionEnum.EN_ATTENTE);
        decision.setSystemeIA(systemeIA);
        decision.setScoreConfiance(mlResponse.getScoreConfiance());
        decision.setPreviousHash(previousHash);
        decision.setCurrentHash(currentHash);
        
        return decisionRepository.save(decision);
    }
    
    @Override
    public boolean isMLServiceAvailable() {
        try {
            restTemplate.getForEntity(mlServiceUrl + "/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String calculateHash(String content, String previousHash) {
        // SHA-256 hash implementation
        String data = content + previousHash + LocalDateTime.now().toString();
        return DigestUtils.sha256Hex(data);
    }
}
```

---

### 4️⃣ **CRÉER MLController**

Créer `controller/MLController.java`:

```java
@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MLController {
    
    private final MLDecisionService mlDecisionService;
    
    @PostMapping("/analyze")
    public ResponseEntity<MLPredictionResponse> analyzerCredit(
            @RequestBody CreditAnalysisRequest request) {
        MLPredictionResponse response = mlDecisionService.genererDecisionML(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/generate-decision")
    public ResponseEntity<Decision> genererDecision(
            @RequestBody CreditAnalysisRequest request,
            @RequestParam Long systemeIaId) {
        Decision decision = mlDecisionService.creerDecisionDepuisML(request, systemeIaId);
        return ResponseEntity.ok(decision);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        boolean available = mlDecisionService.isMLServiceAvailable();
        Map<String, Object> health = new HashMap<>();
        health.put("mlServiceAvailable", available);
        health.put("status", available ? "UP" : "DOWN");
        return ResponseEntity.ok(health);
    }
}
```

---

### 5️⃣ **AJOUTER CONFIGURATION**

Dans `application.properties`:
```properties
# ML Service Configuration
ml.service.url=http://ml-service:5000
ml.service.timeout=5000

# RestTemplate Configuration
spring.http.client.connection-timeout=5000
spring.http.client.read-timeout=5000
```

Créer `config/RestTemplateConfig.java`:
```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

## 🧪 COMMENT TESTER:

### 1. Démarrer tous les services:
```bash
rebuild.bat
```

### 2. Tester ML service directement:
```bash
curl -X POST http://localhost:5000/predict ^
  -H "Content-Type: application/json" ^
  -d "{\"revenuMensuel\":15000,\"dettesActuelles\":2000,\"age\":35,\"ancienneteEmploi\":5,\"montantDemande\":50000}"
```

### 3. Tester via Backend API:
```bash
curl -X POST http://localhost:8080/api/ml/analyze ^
  -H "Content-Type: application/json" ^
  -d "{\"revenuMensuel\":15000,\"dettesActuelles\":2000,\"age\":35,\"ancienneteEmploi\":5,\"montantDemande\":50000}"
```

---

## 📊 ARCHITECTURE ML:

```
┌─────────────┐      ┌──────────────┐      ┌──────────────┐
│   Frontend  │─────▶│   Backend    │─────▶│  ML Service  │
│  (Angular)  │      │  (Spring)    │      │   (Flask)    │
└─────────────┘      └──────────────┘      └──────────────┘
                            │
                            ▼
                     ┌──────────────┐
                     │  PostgreSQL  │
                     │  (Decisions) │
                     └──────────────┘
```

---

## ⚠️ NOTES IMPORTANTES:

1. **Hash Chain**: Chaque décision doit calculer son hash basé sur la décision précédente
2. **Validation**: Les décisions ML sont créées avec statut `EN_ATTENTE`
3. **Sécurité**: Ajouter `@PreAuthorize` sur les endpoints sensibles
4. **Error Handling**: Gérer les cas où ML service est down
5. **Logging**: Ajouter logs pour debug

---

## 📞 CONTACT:
- Yahya: Frontend (Angular)
- Badderdine: Backend (Spring Boot + ML Integration)

Bon courage! 💪
