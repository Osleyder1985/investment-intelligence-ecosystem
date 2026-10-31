# R01.2 — Separación entre PurposeAssessment y EligibilityAssessment

**ID:** IIE-IDENTITY-R01-02  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED  
**Hallazgos:** ICA-02, ICA-09  
**Documentos relacionados:** modelo canónico de estados; D-03/D-04; D-09/D-10; R01.1; Scope Algebra; X-06

## 1. Decisión arquitectónica propuesta

Mantener dos evaluaciones distintas, con autoridad y semántica diferentes:

1. **PurposeAssessment (PA):** determina si el conjunto de claims, evidencias, identidades y obligaciones requeridas es admisible para un propósito informacional/analítico exacto, scope, activity, tiempo y consumidor.
2. **EligibilityAssessment (EA):** determina si un sujeto/objeto satisface los criterios sustantivos de una finalidad de elegibilidad declarada, bajo una política, mandato y scope concretos.

PA es una evaluación de suficiencia/admisibilidad de inputs y obligaciones. EA es una decisión de elegibilidad de dominio. PA puede ser un input obligatorio de EA, pero nunca sustituye EA ni emite ELIGIBLE, INELIGIBLE o QUARANTINED.

**No existe mapping 1:1 entre PurposeAdmissibilityStatus y EligibilityStatus.** La relación es una interfaz de consumo con reglas de bloqueo, traducción condicionada y conservación de procedencia.

## 2. Contratos y autoridad

### 2.1 PurposeAssessment

Responde: «¿Podemos utilizar estos claims/evidencias para este propósito y scope, conforme a su contrato y política?»

Debe identificar:
- purpose_id/version y owner;
- subject/entity refs y sus versiones;
- scope_kind, decision_type, purpose, activity y ScopeDefinition/version/digest;
- catálogo de obligaciones esperado/versionado y su cobertura completa;
- ApplicabilityAssessment por obligación;
- ClaimEnvelope/ClaimResolution y PredicateContract;
- EvidenceRecord, SourceAssessment, ClaimEvidenceAssessment y provenance;
- freshness, temporal cutoff y lifecycle;
- dependencias, membership proofs y validation envelope;
- outcome, limitaciones, blockers, reason codes;
- evaluator, engine/adapter version, policy version, approvals y replay refs.

PA no puede afirmar por sí sola calidad de inversión, riesgo aceptable, valoración atractiva, elegibilidad de custodia/venue ni autorización de ejecución.

### 2.2 EligibilityAssessment

Responde: «¿Cumple este sujeto los criterios de elegibilidad para esta finalidad concreta?»

Debe identificar:
- eligibility_subject y tipo de entidad evaluada;
- eligibility_purpose y scope exactos;
- eligibility_policy_id/version y owner;
- criterios obligatorios, opcionales, prohibiciones y umbrales;
- mandato/jurisdicción/activity cuando sean pertinentes;
- PurposeAssessment refs requeridos y reglas de compatibilidad;
- RiskAssessment/VetoDecision u otros inputs requeridos, sin absorber sus autoridades;
- resultado canónico EligibilityStatus;
- condiciones/limitaciones, causas, vigencia, lifecycle y trazabilidad.

Una evaluación de elegibilidad solo puede ser emitida por el componente/autoridad definido en la gobernanza aplicable. Un proveedor de datos o PA no puede autoaprobar elegibilidad.

### 2.3 Selección final

SelectionDecision es un tercer nivel: agrega identidad, eligibility, evidencia, riesgo, vetos, valoración, comparabilidad y protocolo de oportunidad. No es alias de PA ni EA. Una EA favorable no prueba oportunidad de inversión.

## 3. Vocabularios

### 3.1 PurposeAdmissibilityStatus — revisión requerida

D-10 propone:
- ADMISSIBLE
- ADMISSIBLE_WITH_LIMITATIONS
- INSUFFICIENT_EVIDENCE
- CONFLICTED
- NOT_ASSESSED

**Hallazgo adicional R01.2-A:** ese catálogo no representa explícitamente el caso en que el envelope está completo, la evidencia es admisible y suficiente, pero una obligación obligatoria del propósito ha sido demostrada como no satisfecha. «No ADMISSIBLE» no identifica por sí solo si el resultado es insuficiente, conflictivo o una conclusión adversa probada.

Se propone añadir NOT_ADMISSIBLE con esta semántica:
- El contrato/envelope y catálogo de obligaciones son válidos y completos.
- Las obligaciones aplicables fueron evaluadas con evidencia admisible y suficiente.
- Al menos una obligación obligatoria del propósito no se cumple de forma demostrada.
- Se conservan la obligación, claim, evidence, policy y reason code que fundamentan el resultado.

NOT_ADMISSIBLE no significa que el activo sea globalmente inelegible. Solo significa que los inputs/claims no satisfacen ese propósito exacto.

No introducir REJECTED como alias: ese estado corresponde a la evaluación de evidencia según el modelo canónico. INVALID es error contractual, no resultado de propósito.

### 3.2 EligibilityStatus — conservar catálogo canónico

| Estado | Semántica |
|---|---|
| ELIGIBLE | Cumple todos los criterios obligatorios del purpose/scope de elegibilidad |
| ELIGIBLE_WITH_LIMITATIONS | Cumple bajo limitaciones explícitas y autorizadas |
| INELIGIBLE | Incumple al menos un criterio obligatorio de elegibilidad, demostrado y aplicable |
| INSUFFICIENT_INFORMATION | No hay información suficiente para determinar elegibilidad |
| QUARANTINED | Conflicto/anomalía material exige aislamiento o adjudicación antes de decidir |

No crear nuevos estados de EligibilityStatus para reflejar estados de PA. Preservar las causas y los estados fuente.

## 4. Relación y reglas de consumo

La siguiente tabla define el tratamiento por defecto de un PA consumido por EA. Las políticas de elegibilidad pueden endurecerlo, nunca relajar controles críticos sin excepción aprobada.

| PurposeAdmissibility | Tratamiento en EligibilityAssessment | ¿Mapping automático? |
|---|---|---|
| ADMISSIBLE | Input admisible para evaluar los criterios que dependen de ese propósito | No produce ELIGIBLE |
| ADMISSIBLE_WITH_LIMITATIONS | Input utilizable solo dentro de las limitaciones y usos permitidos | No; EA verifica compatibilidad de cada limitación |
| INSUFFICIENT_EVIDENCE | Falta input requerido; EA no puede concluir favorablemente si es material | Puede contribuir a INSUFFICIENT_INFORMATION, sujeto a materialidad y otros inputs |
| CONFLICTED | Conflicto no adjudicado; aislar la conclusión dependiente | QUARANTINED si el conflicto es material para la elegibilidad; de otro modo INSUFFICIENT_INFORMATION/REVIEW_REQUIRED según policy |
| NOT_ASSESSED | No existe conclusión de PA | INSUFFICIENT_INFORMATION si PA es obligatoria; de otro modo EA puede continuar solo con fuente alternativa autorizada |
| NOT_ADMISSIBLE | El propósito no satisface una obligación probada | INELIGIBLE solo si la política de EA convierte explícitamente esa obligación en criterio obligatorio; de lo contrario bloquea el uso de ese PA y exige otra base admisible |

Reglas adicionales:
1. ADMISSIBLE nunca se traduce directamente en ELIGIBLE.
2. ADMISSIBLE_WITH_LIMITATIONS nunca se traduce automáticamente en ELIGIBLE_WITH_LIMITATIONS; EA debe comprobar que las limitaciones son compatibles con sus propios criterios y alcance.
3. CONFLICTED no siempre equivale a QUARANTINED: EA decide si el conflicto es material para su scope. No puede ignorarlo si afecta un criterio obligatorio.
4. NOT_ADMISSIBLE no implica INELIGIBLE global: el propósito puede no ser obligatorio o puede existir otra fuente/purpose autorizado.
5. INSUFFICIENT_EVIDENCE no es NOT_ADMISSIBLE: desconocimiento no es incumplimiento demostrado.
6. NOT_ASSESSED no es INSUFFICIENT_EVIDENCE en origen. La primera indica ausencia de evaluación; la segunda indica evaluación que concluyó insuficiencia. EA puede mapear ambas a INSUFFICIENT_INFORMATION como resultado consumidor, preservando el motivo.
7. INVALID en PA, Scope, contract o envelope invalida el expediente de entrada y bloquea su consumo; no se transforma en EligibilityStatus.
8. STALE, SUPERSEDED o INVALIDATED se resuelven según R01.1/Lifecycle. No se traducen a un estado favorable ni a INELIGIBLE sin evaluación sustantiva.
9. La ausencia de un PA no es equivalente a NOT_APPLICABLE. La no aplicabilidad se declara por ApplicabilityAssessment para una obligación concreta, con justificación positiva y política versionada.
10. Si el PA es opcional, EA debe registrar qué fuente alternativa autorizada satisface el criterio. No puede omitir silenciosamente un input declarado obligatorio.

## 5. Composición de EligibilityAssessment

EA debe enumerar el conjunto completo y versionado de criterios de elegibilidad. Cada criterio tiene:
- criterion_id/version;
- REQUIRED, OPTIONAL o NOT_APPLICABLE_ALLOWED según policy;
- scope y applicability;
- evidence/PA/input refs;
- criterion outcome (SATISFIED, NOT_SATISFIED, INSUFFICIENT, CONFLICTED, NOT_ASSESSED) como resultado interno por criterio, no como EligibilityStatus global;
- rationale y provenance.

Reglas de agregación propuestas:
- Cualquier criterio REQUIRED con NOT_SATISFIED probado y aplicable → INELIGIBLE, salvo una excepción autorizada y explícita.
- Cualquier criterio REQUIRED material CONFLICTED → QUARANTINED o INSUFFICIENT_INFORMATION conforme a la política de adjudicación; nunca ELIGIBLE.
- Cualquier criterio REQUIRED INSUFFICIENT/NOT_ASSESSED → INSUFFICIENT_INFORMATION.
- Todos los criterios REQUIRED satisfechos, limitaciones compatibles y aprobaciones completas → ELIGIBLE o ELIGIBLE_WITH_LIMITATIONS.
- Criterios NOT_APPLICABLE solo mediante ApplicabilityAssessment válida; no se permite elegibilidad por vacuidad si el catálogo está vacío/incompleto.
- Cualquier bloqueo por riesgo/veto se conserva en su dominio de origen y se propaga a SelectionDecision conforme a precedencia; EA no sustituye VetoDecision.
- El outcome incluye todas las causas y restricciones; el estado agregado no elimina los resultados por criterio.

## 6. Contrato de salida y consumo

| Productor | Produce | Consumidor autorizado | Prohibición |
|---|---|---|---|
| IdentityAssessment | Identidad por dimensión | PA, EA y otros dominios según contrato | No declarar elegibilidad |
| PurposeAssessment | Admisibilidad por propósito/scope | EA, RiskAssessment, VetoAssessment, Selection según interfaces aprobadas | No declarar ELIGIBLE ni decisión final |
| EligibilityAssessment | Elegibilidad por purpose/scope | SelectionDecision y consumidores explícitos | No declarar oportunidad ni autorizar ejecución |
| RiskAssessment | Riesgo por dominio/scope | Veto/Selection/Portfolio conforme a policy | No convertir riesgo no evaluado en riesgo aceptable |
| VetoDecision | Resultado de política de veto | Selection y controles downstream | No puede ser levantado por PA/EA |
| SelectionDecision | Decisión analítica agregada | Candidate Registry/Portfolio proposal según mandato | No autoriza órdenes ni custodia |

## 7. Escenarios de aceptación R01.2

| ID | Escenario | Resultado esperado |
|---|---|---|
| R012-01 | PA=ADMISSIBLE para ANALYTICAL_SELECTION_SUPPORT | EA evalúa criterios; no inferir ELIGIBLE |
| R012-02 | PA=ADMISSIBLE_WITH_LIMITATIONS y limitación compatible | EA puede continuar y solo emitir ELIGIBLE_WITH_LIMITATIONS si cumple su policy |
| R012-03 | PA=ADMISSIBLE_WITH_LIMITATIONS y limitación incompatible con criterio obligatorio | EA no puede emitir ELIGIBLE |
| R012-04 | PA=INSUFFICIENT_EVIDENCE en input obligatorio | EA=INSUFFICIENT_INFORMATION si el input es material |
| R012-05 | PA=CONFLICTED en claim obligatorio | EA=QUARANTINED o INSUFFICIENT_INFORMATION según policy; nunca ELIGIBLE |
| R012-06 | PA=NOT_ASSESSED y PA requerido | EA=INSUFFICIENT_INFORMATION |
| R012-07 | PA=NOT_ASSESSED pero input alternativo está autorizado y completo | EA puede evaluar usando alternativa y registra sustitución |
| R012-08 | PA=NOT_ADMISSIBLE en obligación que es criterio hard | EA=INELIGIBLE con referencia a criterio y evidencia |
| R012-09 | PA=NOT_ADMISSIBLE en propósito opcional | No implica INELIGIBLE; no consumir ese PA |
| R012-10 | PA inválido contractualmente | Rechazar envelope; no producir EligibilityStatus favorable |
| R012-11 | PA=STALE para cutoff operativo | No consumir para ese use; EA no debe convertirlo en INELIGIBLE por sí solo |
| R012-12 | PA=SUPERSEDED/INVALIDATED | Rechazar para evaluación actual; permitir replay histórico explícito |
| R012-13 | Criterio obligatorio no satisfecho con evidencia suficiente | EA=INELIGIBLE |
| R012-14 | Criterio obligatorio no evaluado | EA=INSUFFICIENT_INFORMATION |
| R012-15 | Conflicto material sobre un criterio de elegibilidad | EA=QUARANTINED o estado de revisión definido; nunca ELIGIBLE |
| R012-16 | Todos los criterios marcados NOT_APPLICABLE por falta de datos | Invalidar aplicabilidad; no permitir elegibilidad vacua |
| R012-17 | EA=ELIGIBLE pero existe VETO_ACTIVE aplicable | SelectionDecision refleja VETOED según precedencia; EA permanece como resultado fuente |
| R012-18 | EA=ELIGIBLE para RESEARCH, consumer solicita EXECUTION | Rechazar reutilización; se requiere EA específica y ORE/controles operativos |
| R012-19 | EA favorable con valoración no estimable | SelectionDecision no puede ser OPPORTUNITY_SUPPORTED |
| R012-20 | Dos PA con scopes incompatibles se presentan como equivalentes | Rechazar combinación o exigir transformación/proof de comparabilidad |
| R012-21 | PA/EA pertenecen a diferentes policy versions sin compatibilidad declarada | REVIEW_REQUIRED; no combinar silenciosamente |
| R012-22 | Se actualiza una evidencia upstream | Invalidar/recalcular PA y EA dependientes conforme a grafo y lifecycle |

## 8. Invariantes propuestas

- R012-INV-01: PurposeAssessment y EligibilityAssessment son objetos/decisiones distintos.
- R012-INV-02: PA no emite EligibilityStatus.
- R012-INV-03: ADMISSIBLE no implica ELIGIBLE.
- R012-INV-04: ADMISSIBLE_WITH_LIMITATIONS no implica ELIGIBLE_WITH_LIMITATIONS sin evaluación de compatibilidad.
- R012-INV-05: INSUFFICIENT_EVIDENCE, NOT_ASSESSED, CONFLICTED y NOT_ADMISSIBLE son semánticamente distintos.
- R012-INV-06: NOT_ADMISSIBLE requiere evidencia suficiente y obligación incumplida demostrada.
- R012-INV-07: NOT_ADMISSIBLE no implica inelegibilidad global.
- R012-INV-08: QUARANTINED solo se emite por política de contención material, no como alias genérico de CONFLICTED.
- R012-INV-09: EA referencia policy, purpose, scope y catálogo completo de criterios versionados.
- R012-INV-10: ninguna obligación/criterio requerido desaparece de la cobertura.
- R012-INV-11: estados fuente, causas, provenance y limitaciones sobreviven al mapping.
- R012-INV-12: EA no sustituye RiskAssessment, VetoDecision, RouteEligibility ni SelectionDecision.
- R012-INV-13: un veto activo aplicable impide decisión agregada favorable, aunque EA sea ELIGIBLE.
- R012-INV-14: elegibilidad para una finalidad no se hereda a otra finalidad/actividad/scope.
- R012-INV-15: cambios upstream propagan invalidación/re-evaluación por dependencias y lifecycle.

## 9. Decisiones de arquitectura pendientes

1. Aprobar la frontera PA → EA → SelectionDecision.
2. Aprobar la incorporación de NOT_ADMISSIBLE a PurposeAdmissibilityStatus o decidir una alternativa semánticamente equivalente y explícita.
3. Aprobar el catálogo de criterios y regla de agregación de EA.
4. Determinar si QUARANTINED se reserva a conflictos materiales bajo política de contención.
5. Definir los owners y autoridades para PA, EA y SelectionDecision conforme a RACI.
6. Aprobar el mapping por consumidor, incluyendo Risk, Veto, ORE y Portfolio.
7. Alinear purpose/decision_type/scope_kind y ScopeSignatureRegistry.
8. Tras aprobación: actualizar documentos canónicos, schemas, fixtures, validadores y pruebas de contrato; no antes.

## 10. Estado y limitaciones

**PROPOSED / PENDING_ARCHITECTURE_APPROVAL / NOT_APPROVED.**

Esta propuesta resuelve conceptualmente la separación y documenta 22 escenarios y 15 invariantes. La auditoría fue estática sobre los documentos citados. No se ejecutaron pruebas automatizadas, validación formal, validación empírica ni revisión independiente. No se modificaron el modelo canónico, D-10, requisitos IIM, schemas, código, fixtures o tests.
