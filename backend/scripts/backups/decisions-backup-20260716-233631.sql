--
-- PostgreSQL database dump
--

\restrict 9u0WW9ciwNQ7Ddsmryl3tfaTRdiVin5SZk9cpTsuWRWDJ2tCqseiYsb1uum9sdL

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
8e96de4e-3696-4c30-937b-e611155d911e	f39819d8-579e-48c4-be06-4c9a38e5172b	29417	\N	qwen3:0.6b	qwen3:0.6b	OLLAMA	{"suggestedDecision":"APPROVE","confidence":0.0,"riskLevel":"LOW","summary":"La demande de credit pour un montant de 25 000 avec revenu mensuel de 15 000 en secteur SERVICES, ratio d'endettement de 0.22 et incidents de paiement minimis├®s.","explanation":"La demande met en avant une situation solide en termes de revenu, de secteur et de risque, ce qui justifie une approbation.","recommendations":["Valider la demande","Valider les documents"]}	SUCCESS	Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.\nReponds uniquement en JSON valide, sans markdown, sans texte autour.\nLe JSON doit respecter exactement ce schema :\n{\n  "suggestedDecision": "APPROVE|REJECT|REVIEW",\n  "confidence": 0.0,\n  "riskLevel": "LOW|MEDIUM|HIGH",\n  "summary": "resume court du contexte",\n  "explanation": "explication de la recommandation",\n  "recommendations": ["action 1", "action 2"]\n}\n	2026-07-16 13:43:21.497326	Analyse la decision suivante et produis une recommandation structuree.\n\nPrompt metier:\nAnalyse cette demande de credit et produis un resume textuel clair pour un validateur humain.\nDecision ML: APPROUVER\nConfiance: 93.38%\nRisque: LOW\nMontant: 25000.0\nRevenu mensuel: 15000.0\nSecteur: SERVICES\nRatio d'endettement: 0.22\nIncidents de paiement: 0\n\n\nContexte:\nResume complementaire uniquement. Ne pas inventer de poids SHAP.\n	e0c6fe7e-e2bf-4b07-87ae-0088780655db
4aea496a-3a84-4465-a13a-c55c0cdc58c6	713f5fd0-b80b-4d80-ad03-b121e22c3be1	25762	\N	qwen3:0.6b	qwen3:0.6b	OLLAMA	{"suggestedDecision":"APPROVE","confidence":0.0,"riskLevel":"LOW","summary":"Demande de cr├®dit pour un client avec revenu mensuel de 15000, ratio d'endettement de 0,22 et incidents de paiement, sans risque ├®lev├®.","explanation":"La demande s'inscrit dans un contexte de bonnes pratiques, le revenu est suffisant, les risques sont faibles, et il n'y a pas de d├®pendance croissante.","recommendations":["Appeler le validateur pour valider","Valider le dossier en ligne"]}	SUCCESS	Tu es un assistant d'analyse de decisions pour un systeme de tracabilite IA.\nReponds uniquement en JSON valide, sans markdown, sans texte autour.\nLe JSON doit respecter exactement ce schema :\n{\n  "suggestedDecision": "APPROVE|REJECT|REVIEW",\n  "confidence": 0.0,\n  "riskLevel": "LOW|MEDIUM|HIGH",\n  "summary": "resume court du contexte",\n  "explanation": "explication de la recommandation",\n  "recommendations": ["action 1", "action 2"]\n}\n	2026-07-16 14:01:51.000121	Analyse la decision suivante et produis une recommandation structuree.\n\nPrompt metier:\nAnalyse cette demande de credit et produis un resume textuel clair pour un validateur humain.\nDecision ML: APPROUVER\nConfiance: 93.38%\nRisque: LOW\nMontant: 25000.0\nRevenu mensuel: 15000.0\nSecteur: SERVICES\nRatio d'endettement: 0.22\nIncidents de paiement: 0\n\n\nContexte:\nResume complementaire uniquement. Ne pas inventer de poids SHAP.\n	e0c6fe7e-e2bf-4b07-87ae-0088780655db
\.


--
-- Data for Name: decision; Type: TABLE DATA; Schema: public; Owner: tracabilite
--

COPY public.decision (decision_id, contexte, current_hash, model_name, model_version, previous_hash, prompt, reponse, statut_validation, "timestamp", decision_precedente_id, raison, score_confiance, systeme_ia_id, correlation_id, sources, confidence_score, explanation_source, features_json, probabilities_json, resume_ollama, risk_level, suggested_decision, consensus_json, agent_responses_hash, business_data_hash, human_decision, sources_hash, validator_email) FROM stdin;
ebcbeaa9-8692-4790-b069-dc812e332cf1	Credit | montant=25000.0 | revenu=15000.0 | secteur=SERVICES | debtRatio=0.22	10596069151a8239675e05d378d0feda7c47cb4a678d6c9bef1aba52a9c9741b	LogisticRegression	2.0.0	\N	Demande de cr├®dit professionnelle	Decision ML: APPROUVER | Confiance: 93.38% | Risque: LOW | Source explicabilite: SHAP\n\n[Resume Ollama]\nDemande de cr├®dit pour un client avec revenu mensuel de 15000, ratio d'endettement de 0,22 et incidents de paiement, sans risque ├®lev├®.	MODIFIEE	2026-07-16 14:01:50.995012	\N	\N	\N	\N	\N	\N	93.38	SHAP	{"amount":25000.0,"companyAgeYears":5.0,"debtRatio":0.22,"monthlyIncome":15000.0,"paymentIncidents":0.0,"sector":"SERVICES"}	{"approuver":93.38,"refuser":6.62}	Demande de cr├®dit pour un client avec revenu mensuel de 15000, ratio d'endettement de 0,22 et incidents de paiement, sans risque ├®lev├®.	LOW	APPROUVER	\N	\N	\N	\N	\N	\N
595b3466-6ab6-4303-85d8-b14f70b0cdd1	Credit | montant=25000.0 | revenu=15000.0 | secteur=SERVICES | debtRatio=0.22	53bf70f96b33ea0c89f0b4d94a0552a030bfeedf84e03154219627edd73edfda	LogisticRegression	2.0.0	\N	Test OpenRouter 3 agents	Decision ML: APPROUVER | Confiance: 93.38% | Risque: LOW | Source explicabilite: SHAP\n\n[Consensus OpenRouter] REVIEW | agents reussis=0/3	EN_ATTENTE	2026-07-16 20:37:28.768317	\N	\N	\N	\N	\N	\N	93.38	SHAP	{"amount":25000.0,"companyAgeYears":5.0,"debtRatio":0.22,"monthlyIncome":15000.0,"paymentIncidents":0.0,"sector":"SERVICES"}	{"approuver":93.38,"refuser":6.62}	\N	LOW	APPROUVER	{"decisionConsensus":"REVIEW","confianceMoyenne":null,"agentsConsultes":3,"agentsReussis":0,"votes":{},"resume":null,"note":"Consensus informatif uniquement. La decision ML LogisticRegression et les SHAP ne sont pas modifies."}	\N	\N	\N	\N	\N
b1617bcc-f64e-4a09-b77a-990d81350351	Credit | montant=25000.0 | revenu=15000.0 | secteur=SERVICES | debtRatio=0.22	47115fceb0e6b13cab7448cf5835910283350c6eedb8d4b4cfe65d69463a537c	LogisticRegression	2.0.0	\N	Test propre - credit services	Decision ML: APPROUVER | Confiance: 93.38% | Risque: LOW | Source explicabilite: SHAP\n\n[Resume Ollama]\nLa demande de credit pour un montant de 25 000 avec revenu mensuel de 15 000 en secteur SERVICES, ratio d'endettement de 0.22 et incidents de paiement minimis├®s.	APPROUVEE	2026-07-16 13:43:21.485972	\N	\N	\N	\N	\N	\N	93.38	SHAP	{"amount":25000.0,"companyAgeYears":5.0,"debtRatio":0.22,"monthlyIncome":15000.0,"paymentIncidents":0.0,"sector":"SERVICES"}	{"approuver":93.38,"refuser":6.62}	La demande de credit pour un montant de 25 000 avec revenu mensuel de 15 000 en secteur SERVICES, ratio d'endettement de 0.22 et incidents de paiement minimis├®s.	LOW	APPROUVER	\N	\N	\N	\N	\N	\N
7b09710b-cdda-4eff-8a6c-5299697fae36	Credit | montant=30000.0 | revenu=18000.0 | secteur=TECH | debtRatio=0.18	c034eaf0c8fa8d007e72cc0261c507012ac5790840d6b30102ab6b6557df43ee	LogisticRegression	2.0.0	\N	Retry OpenRouter test	Decision ML: APPROUVER | Confiance: 98.12% | Risque: LOW | Source explicabilite: SHAP\n\n[Consensus OpenRouter] REVIEW | agents reussis=0/3	EN_ATTENTE	2026-07-16 20:38:35.46833	\N	\N	\N	\N	\N	\N	98.12	SHAP	{"amount":30000.0,"companyAgeYears":6.0,"debtRatio":0.18,"monthlyIncome":18000.0,"paymentIncidents":0.0,"sector":"TECH"}	{"approuver":98.12,"refuser":1.88}	\N	LOW	APPROUVER	{"decisionConsensus":"REVIEW","confianceMoyenne":null,"agentsConsultes":3,"agentsReussis":0,"votes":{},"resume":null,"note":"Consensus informatif uniquement. La decision ML LogisticRegression et les SHAP ne sont pas modifies."}	\N	\N	\N	\N	\N
\.


--
-- Data for Name: explanation_factor; Type: TABLE DATA; Schema: public; Owner: tracabilite
--

COPY public.explanation_factor (factor_id, contribution_percent, impact, name, rank, shap_value, source, value, decision_id) FROM stdin;
8657b9c7-0a65-4325-ae91-fe5d7e77aead	44.45	POSITIVE	paymentIncidents	1	3.858058	SHAP	0.0	b1617bcc-f64e-4a09-b77a-990d81350351
86b5e8d2-4c7a-449c-8be5-760b410d44ff	22.65	POSITIVE	amount	2	1.966296	SHAP	25000.0	b1617bcc-f64e-4a09-b77a-990d81350351
91618b01-8443-419f-b668-61adbf5e2eaf	14.58	NEGATIVE	companyAgeYears	3	-1.265742	SHAP	5.0	b1617bcc-f64e-4a09-b77a-990d81350351
9058fc1d-4c11-43b3-b73e-38a9e0ef346c	13.16	NEGATIVE	monthlyIncome	4	-1.142459	SHAP	15000.0	b1617bcc-f64e-4a09-b77a-990d81350351
a0c010e4-b429-4952-a278-d1a4cb905572	4.48	POSITIVE	debtRatio	5	0.389125	SHAP	0.22	b1617bcc-f64e-4a09-b77a-990d81350351
35b6d287-0b4e-4485-82b1-1253283a3fd7	0.67	NEGATIVE	sector	6	-0.058109	SHAP	SERVICES	b1617bcc-f64e-4a09-b77a-990d81350351
05139d6d-9279-4490-a1f7-357014021b2e	44.45	POSITIVE	paymentIncidents	1	3.858058	SHAP	0.0	ebcbeaa9-8692-4790-b069-dc812e332cf1
b2da85fa-73f2-4d79-b470-9c1cd800d211	22.65	POSITIVE	amount	2	1.966296	SHAP	25000.0	ebcbeaa9-8692-4790-b069-dc812e332cf1
a96d98e5-0d03-4a40-9a38-d5a2c02e4e2a	14.58	NEGATIVE	companyAgeYears	3	-1.265742	SHAP	5.0	ebcbeaa9-8692-4790-b069-dc812e332cf1
e0abba8c-db12-459d-ba20-c6bff3e648d1	13.16	NEGATIVE	monthlyIncome	4	-1.142459	SHAP	15000.0	ebcbeaa9-8692-4790-b069-dc812e332cf1
93dd643e-31b8-44a4-a964-8716a9af6b27	4.48	POSITIVE	debtRatio	5	0.389125	SHAP	0.22	ebcbeaa9-8692-4790-b069-dc812e332cf1
0608496d-b345-4b2f-8336-ecbcd8fefec5	0.67	NEGATIVE	sector	6	-0.058109	SHAP	SERVICES	ebcbeaa9-8692-4790-b069-dc812e332cf1
85b26ea0-e575-4bd6-8e35-612aa0d3ab36	44.45	POSITIVE	paymentIncidents	1	3.858058	SHAP	0.0	595b3466-6ab6-4303-85d8-b14f70b0cdd1
af89b486-3ea7-4579-9f17-7ad993ba7384	22.65	POSITIVE	amount	2	1.966296	SHAP	25000.0	595b3466-6ab6-4303-85d8-b14f70b0cdd1
4baa7167-0f7d-4637-a3a9-5eaf7d295cdf	14.58	NEGATIVE	companyAgeYears	3	-1.265742	SHAP	5.0	595b3466-6ab6-4303-85d8-b14f70b0cdd1
d886a5ce-3d09-45b8-b99f-9286b1b38e56	13.16	NEGATIVE	monthlyIncome	4	-1.142459	SHAP	15000.0	595b3466-6ab6-4303-85d8-b14f70b0cdd1
4471042c-c316-40e9-8372-e94919e03f4d	4.48	POSITIVE	debtRatio	5	0.389125	SHAP	0.22	595b3466-6ab6-4303-85d8-b14f70b0cdd1
ebf87ff5-07a4-4b6d-8630-f314db545d0a	0.67	NEGATIVE	sector	6	-0.058109	SHAP	SERVICES	595b3466-6ab6-4303-85d8-b14f70b0cdd1
c15048aa-5f97-4bfb-89c2-469b12ec64a8	47.58	POSITIVE	paymentIncidents	1	3.858058	SHAP	0.0	7b09710b-cdda-4eff-8a6c-5299697fae36
31fc01b4-c617-4dab-bbfb-452998bc0b90	17.06	POSITIVE	amount	2	1.383586	SHAP	30000.0	7b09710b-cdda-4eff-8a6c-5299697fae36
1fc026c4-8ead-4b9c-a976-d68efe91b716	13.36	NEGATIVE	companyAgeYears	3	-1.083358	SHAP	6.0	7b09710b-cdda-4eff-8a6c-5299697fae36
a5fabfc5-25d1-44ef-a91e-ce016d51d864	9.13	POSITIVE	debtRatio	4	0.740554	SHAP	0.18	7b09710b-cdda-4eff-8a6c-5299697fae36
6f13ba1f-b7a2-4d4a-bdf3-d1ec9fa95b6b	7.4	POSITIVE	sector	5	0.59992	SHAP	TECH	7b09710b-cdda-4eff-8a6c-5299697fae36
c6880135-5277-4e66-abab-285d06c5db26	5.47	NEGATIVE	monthlyIncome	6	-0.443223	SHAP	18000.0	7b09710b-cdda-4eff-8a6c-5299697fae36
\.


--
-- PostgreSQL database dump complete
--

\unrestrict 9u0WW9ciwNQ7Ddsmryl3tfaTRdiVin5SZk9cpTsuWRWDJ2tCqseiYsb1uum9sdL

