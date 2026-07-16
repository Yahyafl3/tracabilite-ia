--
-- PostgreSQL database dump
--

\restrict KhDLLFyR7j96TZvrN8I5XCqk2M1OEe8fLRnXXfkWywMu1qEOFfOCk1KVDdCiVPA

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.14

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: appel_ia; Type: TABLE DATA; Schema: public; Owner: tracabilite
--

COPY public.appel_ia (appel_ia_id, correlation_id, duration_ms, error_message, model, model_version, provider, response, statut, system_prompt, "timestamp", user_prompt, utilisateur_id) FROM stdin;
3eeffb3a-b402-45c6-8f83-d070f1029afd	4d2f00f2-ff4d-4cc6-bab7-de3f9c1d0c02	51163	\N	qwen3:0.6b	qwen3:0.6b	OLLAMA	{"suggestedDecision":"APPROVE","confidence":0.0,"riskLevel":"LOW","summary":"La demande de cr├®dit avec une confiance de 93,38%, un risque bas, un montant de 25 000, un revenu mensuel de 15 000, un secteur de SERVICES, un ratio d'endettement de 0.22 et des incidents de paiement.","explanation":"La demande repr├®sente un bon comportement ├®conomique et financier, avec des facteurs cl├®s pour un cr├®dit s├®curis├®. Les donn├®es indiquent un risque faible, ce qui permet une approbation.","recommendations":["action 1","action 2"]}	SUCCESS	Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.\nReponds uniquement en JSON valide, sans markdown, sans texte autour.\nLe JSON doit respecter exactement ce schema :\n{\n  "suggestedDecision": "APPROVE|REJECT|REVIEW",\n  "confidence": 0.0,\n  "riskLevel": "LOW|MEDIUM|HIGH",\n  "summary": "resume court du contexte",\n  "explanation": "explication de la recommandation",\n  "recommendations": ["action 1", "action 2"]\n}\n	2026-07-16 00:24:30.892009	Analyse la decision suivante et produis une recommandation structuree.\n\nPrompt metier:\nAnalyse cette demande de credit et produis un resume textuel clair pour un validateur humain.\nDecision ML: APPROUVER\nConfiance: 93.38%\nRisque: LOW\nMontant: 25000.0\nRevenu mensuel: 15000.0\nSecteur: SERVICES\nRatio d'endettement: 0.22\nIncidents de paiement: 0\n\n\nContexte:\nResume complementaire uniquement. Ne pas inventer de poids SHAP.\n	e0c6fe7e-e2bf-4b07-87ae-0088780655db
74a16bce-7249-46f5-9295-5f15fe91a4fa	f49cba57-114b-4059-a856-909c0419a6cb	53267	\N	qwen3:0.6b	qwen3:0.6b	OLLAMA	{"suggestedDecision":"APPROVE","confidence":0.0,"riskLevel":"LOW","summary":"La demande de cr├®dit avec un montant significatif (22000) et un ratio d'endettement r├®duit ├á 0.3, ce qui r├®duit le risque. Le secteur commerce et un revenu mensuel modest indiquent une bonne chances de succ├¿s.","explanation":"Le score de confiance (50,05%) indique une bonne probabilit├®, mais le risque (risque ├®lev├®) montre qu'il y a de la d├®pendance au montant et ├á la qualit├® des donn├®es. Les facteurs cl├®s comme le revenu et le secteur sont positifs.","recommendations":["Revisiter les ├®tapes de validation des donn├®es pour garantir l'int├®grit├®.","Analyser les incidents de paiement pour d├®tecter des anomalies."]}	SUCCESS	Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.\nReponds uniquement en JSON valide, sans markdown, sans texte autour.\nLe JSON doit respecter exactement ce schema :\n{\n  "suggestedDecision": "APPROVE|REJECT|REVIEW",\n  "confidence": 0.0,\n  "riskLevel": "LOW|MEDIUM|HIGH",\n  "summary": "resume court du contexte",\n  "explanation": "explication de la recommandation",\n  "recommendations": ["action 1", "action 2"]\n}\n	2026-07-16 00:27:30.192842	Analyse la decision suivante et produis une recommandation structuree.\n\nPrompt metier:\nAnalyse cette demande de credit et produis un resume textuel clair pour un validateur humain.\nDecision ML: APPROUVER\nConfiance: 50.05%\nRisque: MEDIUM\nMontant: 22000.0\nRevenu mensuel: 12580.0\nSecteur: COMMERCE\nRatio d'endettement: 0.3\nIncidents de paiement: 1\n\n\nContexte:\nResume complementaire uniquement. Ne pas inventer de poids SHAP.\n	e0c6fe7e-e2bf-4b07-87ae-0088780655db
80221d1b-b9cb-48fc-87de-67894c333a35	4541576c-fbfa-4037-91c0-dec886f1a5c0	25508	\N	qwen3:0.6b	qwen3:0.6b	OLLAMA	{"suggestedDecision":"REJECT","confidence":0.0,"riskLevel":"HIGH","summary":"La demande de cr├®dit a ├®t├® rejet├®e pour son risque ├®lev├®, confirm├® par une confiance de 99.83% et une analyse de paiements de 5 incidents.","explanation":"La demande de cr├®dit est rejet├®e en raison de son risque ├®lev├®, confirm├® par une confiance ├®lev├®e et une analyse d├®taill├®e des risques.","recommendations":[]}	SUCCESS	Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.\nReponds uniquement en JSON valide, sans markdown, sans texte autour.\nLe JSON doit respecter exactement ce schema :\n{\n  "suggestedDecision": "APPROVE|REJECT|REVIEW",\n  "confidence": 0.0,\n  "riskLevel": "LOW|MEDIUM|HIGH",\n  "summary": "resume court du contexte",\n  "explanation": "explication de la recommandation",\n  "recommendations": ["action 1", "action 2"]\n}\n	2026-07-16 13:27:20.860675	Analyse la decision suivante et produis une recommandation structuree.\n\nPrompt metier:\nAnalyse cette demande de credit et produis un resume textuel clair pour un validateur humain.\nDecision ML: REJETER\nConfiance: 99.83%\nRisque: HIGH\nMontant: 25000.0\nRevenu mensuel: 15000.0\nSecteur: SERVICES\nRatio d'endettement: 0.35\nIncidents de paiement: 5\n\n\nContexte:\nResume complementaire uniquement. Ne pas inventer de poids SHAP.\n	e0c6fe7e-e2bf-4b07-87ae-0088780655db
\.


--
-- Data for Name: decision; Type: TABLE DATA; Schema: public; Owner: tracabilite
--

COPY public.decision (decision_id, contexte, current_hash, model_name, model_version, previous_hash, prompt, reponse, statut_validation, "timestamp", decision_precedente_id, raison, score_confiance, systeme_ia_id, correlation_id, sources, confidence_score, explanation_source, features_json, probabilities_json, resume_ollama, risk_level, suggested_decision) FROM stdin;
109e684c-1c08-480d-8108-c636aa041169	Credit | montant=25000.0 | revenu=15000.0 | secteur=SERVICES | debtRatio=0.22	7a6c2dc672ce957589573fcef789759c348a56079b5b33926f74275f0e03f86f	LogisticRegression	2.0.0	\N	Test propre apres nettoyage - demande credit services	Decision ML: APPROUVER | Confiance: 93.38% | Risque: LOW | Source explicabilite: SHAP\n\n[Resume Ollama]\nLa demande de cr├®dit avec une confiance de 93,38%, un risque bas, un montant de 25 000, un revenu mensuel de 15 000, un secteur de SERVICES, un ratio d'endettement de 0.22 et des incidents de paiement.	EN_ATTENTE	2026-07-16 00:24:30.846481	\N	\N	\N	\N	\N	\N	93.38	SHAP	{"amount":25000.0,"companyAgeYears":5.0,"debtRatio":0.22,"monthlyIncome":15000.0,"paymentIncidents":0.0,"sector":"SERVICES"}	{"approuver":93.38,"refuser":6.62}	La demande de cr├®dit avec une confiance de 93,38%, un risque bas, un montant de 25 000, un revenu mensuel de 15 000, un secteur de SERVICES, un ratio d'endettement de 0.22 et des incidents de paiement.	LOW	APPROUVER
f6251141-8170-4912-9626-06d5029a6d10	Credit | montant=22000.0 | revenu=12580.0 | secteur=COMMERCE | debtRatio=0.3	c8707464783eb15324a2b102fce5b4db30577add468f4cc3a1115df36e52dad6	LogisticRegression	2.0.0	\N	Demande de cr├®dit professionnelle 	Decision ML: APPROUVER | Confiance: 50.05% | Risque: MEDIUM | Source explicabilite: SHAP\n\n[Resume Ollama]\nLa demande de cr├®dit avec un montant significatif (22000) et un ratio d'endettement r├®duit ├á 0.3, ce qui r├®duit le risque. Le secteur commerce et un revenu mensuel modest indiquent une bonne chances de succ├¿s.	EN_ATTENTE	2026-07-16 00:27:30.186707	\N	\N	\N	\N	\N	\N	50.05	SHAP	{"amount":22000.0,"companyAgeYears":4.0,"debtRatio":0.3,"monthlyIncome":12580.0,"paymentIncidents":1.0,"sector":"COMMERCE"}	{"approuver":50.05,"refuser":49.95}	La demande de cr├®dit avec un montant significatif (22000) et un ratio d'endettement r├®duit ├á 0.3, ce qui r├®duit le risque. Le secteur commerce et un revenu mensuel modest indiquent une bonne chances de succ├¿s.	MEDIUM	APPROUVER
ea4497fe-99cf-43a9-9efa-912393e25873	Credit | montant=25000.0 | revenu=15000.0 | secteur=SERVICES | debtRatio=0.35	b7013d22e59a30754ca70f319bb6af9e870687b3a455729049acedbcda860f2a	LogisticRegression	2.0.0	\N	Demande de cr├®dit professionnelle	Decision ML: REJETER | Confiance: 99.83% | Risque: HIGH | Source explicabilite: SHAP\n\n[Resume Ollama]\nLa demande de cr├®dit a ├®t├® rejet├®e pour son risque ├®lev├®, confirm├® par une confiance de 99.83% et une analyse de paiements de 5 incidents.	EN_ATTENTE	2026-07-16 13:27:20.831456	\N	\N	\N	\N	\N	\N	99.83	SHAP	{"amount":25000.0,"companyAgeYears":5.0,"debtRatio":0.35,"monthlyIncome":15000.0,"paymentIncidents":5.0,"sector":"SERVICES"}	{"approuver":0.17,"refuser":99.83}	La demande de cr├®dit a ├®t├® rejet├®e pour son risque ├®lev├®, confirm├® par une confiance de 99.83% et une analyse de paiements de 5 incidents.	HIGH	REJETER
\.


--
-- Data for Name: explanation_factor; Type: TABLE DATA; Schema: public; Owner: tracabilite
--

COPY public.explanation_factor (factor_id, contribution_percent, impact, name, rank, shap_value, source, value, decision_id) FROM stdin;
8bdb21fd-dde8-4a6b-bc1f-42326d04ae95	44.45	POSITIVE	paymentIncidents	1	3.858058	SHAP	0.0	109e684c-1c08-480d-8108-c636aa041169
b4641366-f767-4f42-9ad6-18a112302ea8	22.65	POSITIVE	amount	2	1.966296	SHAP	25000.0	109e684c-1c08-480d-8108-c636aa041169
ef564341-de87-47b2-96bd-db70794e5da1	14.58	NEGATIVE	companyAgeYears	3	-1.265742	SHAP	5.0	109e684c-1c08-480d-8108-c636aa041169
8a3bb118-20fd-4804-b9aa-4b0838d96517	13.16	NEGATIVE	monthlyIncome	4	-1.142459	SHAP	15000.0	109e684c-1c08-480d-8108-c636aa041169
54c6cfde-c03e-4036-a180-ec74440f044c	4.48	POSITIVE	debtRatio	5	0.389125	SHAP	0.22	109e684c-1c08-480d-8108-c636aa041169
43b5afe8-1630-44fc-a55c-59ce101a496a	0.67	NEGATIVE	sector	6	-0.058109	SHAP	SERVICES	109e684c-1c08-480d-8108-c636aa041169
ab844aab-9fbb-4bc4-9f7f-fc08d06a1f59	28.61	POSITIVE	amount	1	2.315922	SHAP	22000.0	f6251141-8170-4912-9626-06d5029a6d10
9d6480bc-5ac5-44d8-836a-0d9467f0cf48	28.2	POSITIVE	paymentIncidents	2	2.28334	SHAP	1.0	f6251141-8170-4912-9626-06d5029a6d10
eb2fa2f0-6c9a-4260-9b1e-67ce0d95d8d0	21.08	NEGATIVE	monthlyIncome	3	-1.70651	SHAP	12580.0	f6251141-8170-4912-9626-06d5029a6d10
0344c3d4-8c77-4035-9ea6-711c679a8ffb	17.89	NEGATIVE	companyAgeYears	4	-1.448125	SHAP	4.0	f6251141-8170-4912-9626-06d5029a6d10
e355f29f-e624-41be-baff-28b0475eb512	3.88	NEGATIVE	debtRatio	5	-0.313733	SHAP	0.3	f6251141-8170-4912-9626-06d5029a6d10
21a0a778-c12c-4279-b876-f56c84707667	0.35	NEGATIVE	sector	6	-0.028495	SHAP	COMMERCE	f6251141-8170-4912-9626-06d5029a6d10
b051776d-bd78-4003-9533-6647459adf41	43.64	NEGATIVE	paymentIncidents	1	-4.01553	SHAP	5.0	ea4497fe-99cf-43a9-9efa-912393e25873
445b769f-8089-4d50-aa23-28470458fd2c	21.37	POSITIVE	amount	2	1.966296	SHAP	25000.0	ea4497fe-99cf-43a9-9efa-912393e25873
a2ee707c-5a14-41da-a534-82ca7d6abbcc	13.76	NEGATIVE	companyAgeYears	3	-1.265742	SHAP	5.0	ea4497fe-99cf-43a9-9efa-912393e25873
99cfc726-4bad-4022-82fe-bcb6133ea4eb	12.42	NEGATIVE	monthlyIncome	4	-1.142459	SHAP	15000.0	ea4497fe-99cf-43a9-9efa-912393e25873
90218dd1-0ec8-40ee-a187-52d182d8339d	8.18	NEGATIVE	debtRatio	5	-0.753019	SHAP	0.35	ea4497fe-99cf-43a9-9efa-912393e25873
72123f33-00c5-4d2f-92d4-272e4f1f2084	0.63	NEGATIVE	sector	6	-0.058109	SHAP	SERVICES	ea4497fe-99cf-43a9-9efa-912393e25873
\.


--
-- PostgreSQL database dump complete
--

\unrestrict KhDLLFyR7j96TZvrN8I5XCqk2M1OEe8fLRnXXfkWywMu1qEOFfOCk1KVDdCiVPA

