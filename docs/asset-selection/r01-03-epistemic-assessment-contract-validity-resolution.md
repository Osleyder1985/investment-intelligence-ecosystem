# R01.3 — Semántica canónica de UNKNOWN, NOT_ASSESSED, UNDETERMINED, INVALID, CONFLICTED e INSUFFICIENT_INFORMATION

**ID:** IIE-IDENTITY-R01-03  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Hallazgos:** ICA-03, ICA-04, ICA-06  
**Documentos relacionados:** modelo canónico de estados; D-03/D-04; D-05/D-06; D-10; FSA-01; R01.1; R01.2

## 1. Objetivo y decisión propuesta

Establecer una taxonomía por capas. Los seis términos no son estados intercambiables ni pertenecen al mismo eje:

1. UNKNOWN: resultado epistemológico/lógico de una evaluación válida cuando no se puede demostrar TRUE ni FALSE.
2. NOT_ASSESSED: estado de workflow/proceso que indica que no se ejecutó una evaluación concluida.
3. UNDETERMINED: resultado de ApplicabilityAssessment cuando no puede determinarse si una obligación/regla aplica.
4. INVALID: fallo de contrato, estructura, tipo, versión, referencia o integridad; no es conclusión factual.
5. CONFLICTED: contradicción material entre afirmaciones/evidencias admisibles dentro del mismo contexto, aún no adjudicada.
6. INSUFFICIENT_INFORMATION: resultado de EligibilityAssessment cuando la información disponible no permite decidir elegibilidad.

Se propone conservar estas distinciones y prohibir un estado universal común. Cada dominio declara su vocabulario, su eje, su productor autorizado, su semántica y sus transiciones. Las traducciones entre dominios se realizan mediante contratos explícitos, preservando estado original, causa y provenance.

## 2. Taxonomía por eje

| Término | Eje propietario | Pregunta que responde | Qué no significa |
|---|---|---|---|
| UNKNOWN | Epistemic/knowledge result; ClaimResolution y lógica FSA-01 según contrato | ¿La proposición puede demostrarse o refutarse? | No evaluado, falso, vacío, inválido |
| NOT_ASSESSED | Evaluation workflow/status | ¿Se ejecutó la evaluación requerida? | Desconocido como resultado de una evaluación ya ejecutada |
| UNDETERMINED | ApplicabilityAssessment | ¿Se pudo establecer si esta obligación aplica? | NOT_APPLICABLE, evidencia falsa o evaluación ausente |
| INVALID | Contract validity | ¿El objeto y sus referencias cumplen el contrato? | FALSE, UNKNOWN, CONFLICTED o INELIGIBLE |
| CONFLICTED | Claim/identity/purpose/domain resolution, con reglas propias | ¿Hay contradicción material admisible no adjudicada? | Cualquier incertidumbre, desacuerdo trivial o datos faltantes |
| INSUFFICIENT_INFORMATION | EligibilityAssessment | ¿Falta información material para determinar elegibilidad? | INELIGIBLE demostrado, NOT_ASSESSED de origen, INVALID |

## 3. Definiciones operativas y productores autorizados

### 3.1 UNKNOWN

UNKNOWN es una conclusión epistemológica de una evaluación válida. Se usa cuando la regla, predicado, scope y tipos son válidos, pero los datos, cobertura, dependencias o adjudicación no permiten establecer TRUE ni FALSE.

En FSA-01:
- TRUE: pertenencia demostrada.
- FALSE: pertenencia refutada conforme al PredicateContract.
- UNKNOWN: no demostrada ni refutada.
- INVALID: fuera del dominio lógico, por fallo de contrato.

UNKNOWN puede surgir de:
- grafo abierto sin relación registrada;
- evidencia insuficiente o inadmisible para la proposición;
- dependencia material incompleta;
- claim material NOT_ASSESSED, CONFLICTED o RETRACTED sin sustituto;
- falta de cobertura de universo/cutoff en una inferencia que requiere cierre.

La razón concreta debe acompañar UNKNOWN. No usar UNKNOWN como contenedor genérico de todo error o falta de ejecución.

### 3.2 NOT_ASSESSED

NOT_ASSESSED expresa que el productor autorizado no ha ejecutado o completado la evaluación de dominio. Es estado de proceso, no conclusión epistemológica de un cálculo concluido.

Reglas:
- Un workflow NOT_STARTED/IN_PROGRESS puede no tener outcome de dominio.
- Si el contrato exige outcome antes de COMPLETE, la ausencia impide declarar el expediente completo.
- No convertir automáticamente NOT_ASSESSED a UNKNOWN en el registro fuente.
- Un consumidor puede producir INSUFFICIENT_INFORMATION si el assessment requerido no existe, pero debe conservar que la causa fue NOT_ASSESSED.
- No usar NOT_ASSESSED para ocultar un resultado UNKNOWN producido por una evaluación ya ejecutada.

### 3.3 UNDETERMINED

UNDETERMINED pertenece a ApplicabilityAssessment y responde si una obligación, regla, método o criterio aplica al sujeto/scope.

Reglas:
- Requiere que la evaluación de aplicabilidad haya sido ejecutada; por ello no equivale a NOT_ASSESSED.
- Indica que la evidencia/taxonomía/contexto no permite resolver aplicabilidad.
- No equivale a NOT_APPLICABLE. La no aplicabilidad requiere una regla positiva y versionada.
- No equivale automáticamente a UNKNOWN de ClaimResolution, aunque un claim UNKNOWN pueda ser input causal.
- Una obligación requerida UNDETERMINED impide cobertura completa y bloquea una conclusión favorable que dependa de demostrar el universo de obligaciones.

### 3.4 INVALID

INVALID es fallo de validez contractual. Puede aplicarse a envelope, schema, predicate contract, scope signature, referencias, versiones, unidades, serialización o invariantes.

Reglas:
- INVALID se detecta antes de la composición factual/lógica cuando el contrato puede validarse previamente.
- Si un input requerido es INVALID, la expresión/assessment dependiente no puede producir una conclusión válida.
- INVALID no es el cuarto valor de la lógica de Kleene; el dominio lógico sigue siendo TRUE/FALSE/UNKNOWN.
- INVALID no debe mapearse a UNKNOWN, FALSE, CONFLICTED, REJECTED, INELIGIBLE o INSUFFICIENT_INFORMATION.
- La respuesta operativa puede ser REJECTED_INVALID_INPUT o REVIEW_REQUIRED según contrato, pero se preserva ContractValidity=INVALID.
- La corrección produce una nueva versión/reevaluación; no se reescribe el evento original.

### 3.5 CONFLICTED

CONFLICTED requiere:
- proposiciones relevantes al mismo sujeto/predicate o cuestión de decisión;
- scopes y contextos temporales comparables;
- evidencia que supera el umbral de admisibilidad definido;
- resultados materialmente incompatibles;
- ausencia de adjudicación válida que resuelva la contradicción.

No basta:
- una fuente de baja calidad frente a una fuente admisible, si la política la descarta justificadamente;
- diferencia de scope, unidad, periodo o definición;
- un dato ausente;
- múltiples interpretaciones que no sean lógicamente incompatibles.

CONFLICTED puede existir en IdentityAssessment, ClaimResolution, PurposeAssessment o un dominio, pero el contrato de cada uno determina el alcance. No debe crearse un CONFLICTED genérico sin referencias al conflicto, claims, scope, criterio de materialidad y adjudicación pendiente.

### 3.6 INSUFFICIENT_INFORMATION

INSUFFICIENT_INFORMATION pertenece al EligibilityStatus canónico. Es la conclusión de EA de que no existe información suficiente para determinar si se satisfacen sus criterios obligatorios.

Puede derivarse como outcome consumidor de:
- PA=INSUFFICIENT_EVIDENCE en input material obligatorio;
- PA=NOT_ASSESSED cuando PA es obligatoria y no existe alternativa autorizada;
- ApplicabilityAssessment=UNDETERMINED en criterio obligatorio;
- ClaimResolution=UNKNOWN en predicado material;
- cobertura/dependencia incompleta.

No es un mapping automático de cada estado fuente. EA evalúa materialidad, obligatoriedad, alternativas autorizadas y scope. Conserva el estado original y reason code.

No utilizar INSUFFICIENT_INFORMATION cuando evidencia suficiente demuestra incumplimiento: eso corresponde a INELIGIBLE. Tampoco usarlo para un envelope INVALID: el input inválido debe rechazarse antes de la decisión de elegibilidad.

## 4. Matriz de distinción y traducción entre dominios

| Estado de origen | Dominio consumidor | Tratamiento por defecto | Condición |
|---|---|---|---|
| ClaimResolution UNKNOWN | FSA-01 | Membership UNKNOWN si el claim es material | PredicateContract válido y relación exacta |
| ClaimResolution NOT_ASSESSED | FSA-01 | No ejecutar inferencia favorable; resultado de proceso incompleto o UNKNOWN consumidor | La evaluación de claim no se ejecutó |
| Applicability UNDETERMINED | PurposeAssessment | No ADMISSIBLE; INSUFFICIENT_EVIDENCE/REVIEW_REQUIRED | Obligación requerida |
| Applicability UNDETERMINED | EligibilityAssessment | INSUFFICIENT_INFORMATION o revisión bloqueante | Criterio obligatorio y sin alternativa |
| ContractValidity INVALID | Cualquier consumidor | Rechazar/bloquear input | Nunca traducir a estado factual |
| ClaimResolution CONFLICTED | PurposeAssessment | CONFLICTED si material al propósito | Conflicto admisible, mismo scope |
| PurposeAdmissibility INSUFFICIENT_EVIDENCE | EligibilityAssessment | Puede contribuir a INSUFFICIENT_INFORMATION | Input obligatorio y material |
| PurposeAdmissibility NOT_ASSESSED | EligibilityAssessment | INSUFFICIENT_INFORMATION si obligatorio y sin alternativa | Preservar causa NOT_ASSESSED |
| EligibilityStatus INSUFFICIENT_INFORMATION | SelectionDecision | Bloqueo/insuficiencia conforme a precedencia | No convertir en INELIGIBLE |
| EvidenceStatus REJECTED | ClaimResolution | No prueba FALSE por sí solo | Evaluación de evidencia rechazada, no refutación de claim |

## 5. Reglas de precedencia y validación

1. Validar estructura, tipos, firmas, versiones y referencias antes de ejecutar evaluación factual.
2. Si el contrato es inválido, emitir ContractValidity=INVALID y detener la composición que depende de él.
3. Si el contrato es válido, ejecutar workflow. Si no se ejecutó, registrar NOT_ASSESSED/lifecycle apropiado.
4. Si se ejecutó ApplicabilityAssessment, su resultado puede ser APPLICABLE, NOT_APPLICABLE o UNDETERMINED; INVALID queda en la capa de validez contractual.
5. Si se ejecutó ClaimResolution, resolver CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, NOT_ASSESSED o RETRACTED según el contrato canónico; aclarar que NOT_ASSESSED corresponde a proceso y no a resultado lógico.
6. Para FSA-01, mapear ClaimResolution a TRUE/FALSE/UNKNOWN solo mediante PredicateContract aprobado. INVALID detiene la evaluación.
7. Si el input es válido pero no se puede resolver la proposición, UNKNOWN se conserva como resultado epistemológico.
8. Si existe contradicción admisible material, CONFLICTED prevalece sobre un simple UNKNOWN solo cuando la política de adjudicación determina que la contradicción está establecida; si la evidencia no alcanza el umbral, UNKNOWN.
9. EligibilityAssessment agrega criterios y produce INSUFFICIENT_INFORMATION solo si la insuficiencia material impide decidir; no reescribe estados fuente.
10. SelectionDecision conserva causa primaria y todas las causas secundarias según precedencia canónica.

## 6. Tabla de casos límite

| Caso | Estado correcto | Estados incorrectos |
|---|---|---|
| Claim nunca evaluado | NOT_ASSESSED | UNKNOWN como estado de workflow |
| Claim evaluado, falta cobertura para resolver | UNKNOWN | NOT_ASSESSED |
| Obligación evaluada, no se sabe si aplica | UNDETERMINED | NOT_APPLICABLE |
| Objeto malformado o reference/version inconsistente | INVALID | UNKNOWN/FALSE |
| Dos evidencias admisibles incompatibles para mismo predicate/scope | CONFLICTED | UNKNOWN genérico, salvo que no se cumpla umbral de conflicto |
| Criterio de elegibilidad no puede decidirse por falta de datos | INSUFFICIENT_INFORMATION | INELIGIBLE |
| Evidencia suficiente demuestra criterio obligatorio incumplido | INELIGIBLE | INSUFFICIENT_INFORMATION |
| Fuente rechazada, sin evidencia alternativa | UNKNOWN o insuficiencia del dominio | FALSE automático |
| Grafo abierto sin relación observada | UNKNOWN | FALSE |
| Universo cerrado con negación demostrada según contrato | FALSE | UNKNOWN por defecto |
| TTL expirado | Freshness=STALE | INVALID/CONFLICTED |
| Claim retractado sin sustitución actual | UNKNOWN para uso actual; histórico preservado | FALSE automático |

## 7. Contratos de datos mínimos

Los objetos que transportan estos resultados deben separar al menos:

- contract_validity: VALID/INVALID, reason_codes y validator/version;
- workflow_status: NOT_STARTED/IN_PROGRESS/COMPLETE/REVIEW_REQUIRED y lifecycle aplicable;
- domain_outcome: vocabulario exclusivo del dominio;
- epistemic_result: TRUE/FALSE/UNKNOWN solo donde el contrato lógico lo requiere;
- applicability_outcome: APPLICABLE/NOT_APPLICABLE/UNDETERMINED;
- conflict_record: claims/evidences, scope, temporal context, materiality rule y adjudication status;
- consumer_mapping: source state, target state, mapping policy/version, materiality, alternatives y rationale.

No todos los objetos necesitan todos los campos. El contrato define cuáles son obligatorios; no duplicar ejes en un único enum plano. La ausencia de un campo obligatorio en un objeto que declara evaluación completa es error de completitud/contrato.

## 8. Escenarios de aceptación R01.3

| ID | Escenario | Resultado esperado |
|---|---|---|
| R013-01 | Claim no ha sido evaluado | NOT_ASSESSED; no crear resultado epistemológico |
| R013-02 | Claim evaluado con contrato válido y evidencia insuficiente | UNKNOWN |
| R013-03 | Claim evaluado con evidencia suficiente para refutar predicate exacto | REFUTED; FALSE solo si PredicateContract lo establece |
| R013-04 | Predicate contract malformado | INVALID; no ejecutar lógica |
| R013-05 | Obligación evaluada pero aplicabilidad no determinable | UNDETERMINED |
| R013-06 | Regla positiva versionada demuestra que obligación no aplica | NOT_APPLICABLE |
| R013-07 | Obligación no se evaluó | NOT_ASSESSED en workflow, no UNDETERMINED |
| R013-08 | Evidencias admisibles incompatibles en scope comparable | CONFLICTED |
| R013-09 | Fuentes difieren por scope/periodo/unidad | No CONFLICTED sin normalización/comparabilidad |
| R013-10 | Fuente rechazada sin evidencia que refute el claim | No FALSE; UNKNOWN/insuficiencia según evaluación |
| R013-11 | EA no puede determinar criterio obligatorio por falta de datos | INSUFFICIENT_INFORMATION |
| R013-12 | Evidencia suficiente prueba incumplimiento obligatorio | INELIGIBLE |
| R013-13 | EA input contractual inválido | Rechazo contractual; no EligibilityStatus favorable |
| R013-14 | PA no evaluado y requerido por EA | EA puede producir INSUFFICIENT_INFORMATION conservando NOT_ASSESSED como causa |
| R013-15 | PA=INSUFFICIENT_EVIDENCE en criterio no material/opcional | EA puede continuar solo si policy y alternativa permiten |
| R013-16 | Un claim es UNKNOWN pero no material al purpose | No propagar bloqueo automáticamente |
| R013-17 | Un claim es CONFLICTED pero scope del consumidor es disjunto | No propagar conflicto fuera del scope |
| R013-18 | Conflict record carece de evidencia o criterio de materialidad | No aceptar CONFLICTED válido; revisar/invalidar contrato según defecto |
| R013-19 | Assessment COMPLETE carece de un outcome obligatorio | Error de completitud/contrato; no inferir UNKNOWN |
| R013-20 | Se recibe INVALID junto con TRUE/FALSE/UNKNOWN | INVALID estricto; no componer |
| R013-21 | Claim RETRACTED sin resolución sustituta | UNKNOWN para uso actual; preservar histórico |
| R013-22 | Datos suficientes, pero no existe política de adjudicación requerida | REVIEW_REQUIRED/UNKNOWN; no inventar CONFLICTED adjudicado |
| R013-23 | Se evalúa scope con universo abierto como si fuera cerrado | INVALID si viola contrato declarado; de otro modo UNKNOWN/no exactitud |
| R013-24 | Mapping a consumidor sin policy version | Rechazar mapping; REVIEW_REQUIRED |

## 9. Invariantes propuestas

- R013-INV-01: UNKNOWN, NOT_ASSESSED, UNDETERMINED, INVALID, CONFLICTED e INSUFFICIENT_INFORMATION no son sinónimos.
- R013-INV-02: UNKNOWN solo se emite como resultado de evaluación válida.
- R013-INV-03: NOT_ASSESSED indica workflow no ejecutado/no concluido, no conclusión epistemológica.
- R013-INV-04: UNDETERMINED solo lo emite ApplicabilityAssessment.
- R013-INV-05: NOT_APPLICABLE requiere regla positiva, scope y justificación versionados.
- R013-INV-06: INVALID pertenece a ContractValidity/validation y no al dominio factual o lógico.
- R013-INV-07: INVALID estricto bloquea la composición dependiente.
- R013-INV-08: CONFLICTED requiere evidencia admisible material, scope/context comparables y ausencia de adjudicación.
- R013-INV-09: diferencia de fuente/scope/unidad no basta por sí sola para CONFLICTED.
- R013-INV-10: INSUFFICIENT_INFORMATION pertenece a EligibilityStatus y no se retropropaga como estado de claim/evidencia.
- R013-INV-11: UNKNOWN nunca se convierte implícitamente en FALSE, EMPTY, NOT_APPLICABLE o NO_VETO_IDENTIFIED.
- R013-INV-12: NOT_ASSESSED nunca se convierte implícitamente en UNKNOWN en el registro productor.
- R013-INV-13: estados fuente, causa, scope, temporalidad y provenance sobreviven a toda traducción.
- R013-INV-14: mapping entre dominios exige consumer contract y policy version.
- R013-INV-15: un assessment COMPLETE debe satisfacer el conjunto de campos/outcomes obligatorios de su contrato.
- R013-INV-16: no existe composición favorable con un input requerido INVALID, UNKNOWN material, NOT_ASSESSED requerido o UNDETERMINED requerido.

## 10. Decisiones pendientes y secuencia

1. Aprobar taxonomía por capas y productores autorizados.
2. Revisar la aparente mezcla de NOT_ASSESSED dentro de ClaimResolutionStatus: decidir si se conserva como outcome de assessment envelope o se separa formalmente en workflow.
3. Alinear EvidenceStatus, EvidenceAdmissibility, EvidenceSufficiency, Freshness y ContractValidity (ICA-06).
4. Definir contrato para ConflictRecord y estándar de materialidad/adjudicación.
5. Definir qué consumidor transforma causas a INSUFFICIENT_INFORMATION, REVIEW_REQUIRED, QUARANTINED u otros estados.
6. Alinear FSA-01 con D-04/D-06 y demostrar las reglas del fragmento de predicados aprobado.
7. Aprobar transiciones y mappings antes de modificar documento canónico, schemas, fixtures, validadores o código.

## 11. Estado y limitaciones

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.**

Documento de resolución R01.3 con 24 escenarios y 16 invariantes. Basado en revisión estática de documentos del repositorio. No se ejecutaron pruebas automatizadas, demostraciones formales, validación empírica ni revisión independiente. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures o tests.
