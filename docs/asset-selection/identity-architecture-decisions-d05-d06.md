# Resolución arquitectónica propuesta — D-05 y D-06

**ID:** IIE-IDENTITY-ARCH-DECISIONS-007  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Dependencias:** D-01..D-04; docs. 03, 12, 19 y 22; FSA-01  
**Issue:** #61

## 1. Propósito y límites

D-05 define el modelo de evidencia sin confundir fuente, artefacto, afirmación y evaluación de soporte. D-06 define el adaptador controlado que transforma evaluaciones admisibles de claims en pertenencia lógica FSA-01.

Este documento es una propuesta semántica. No modifica requisitos IIM, modelos canónicos, schemas, código, fixtures ni tests. No constituye aprobación institucional, validación matemática ni validación empírica.

## 2. D-05 — Modelo de evidencia

### 2.1 Decisión propuesta: cuatro objetos independientes

| Objeto | Pregunta que responde | Contenido |
|---|---|---|
| SourceRecord | ¿Qué origen produjo o publicó el material? | Identidad de fuente, operador/editor, canal, ubicación, lineage y condiciones de acceso/licencia |
| EvidenceRecord | ¿Qué artefacto u observación concreta se preserva? | Payload o referencia inmutable, digest, formato, captura, timestamps, integridad y método |
| SourceAssessment | ¿Qué propiedades y limitaciones tiene esta fuente para un uso? | Autoridad, competencia, metodología, independencia, incentivos, acceso, licencia, historial, limitaciones |
| ClaimEvidenceAssessment | ¿Qué aporta esta evidencia al claim exacto para este purpose/scope/cutoff? | Dirección probatoria, cobertura, admisibilidad, relevancia, suficiencia, contradicciones y rationale |

SourceRecord no es EvidenceRecord: una misma fuente puede producir múltiples artefactos; un artefacto puede replicarse o ser preservado por distintos sistemas sin convertirse en fuentes independientes. SourceAssessment no confirma claims. EvidenceRecord no tiene un estado global de “verdad”. ClaimEvidenceAssessment es contextual y versionado.

### 2.2 SourceRecord y lineage

SourceRecord deberá identificar, cuando sea posible:
- source_id y versión/identidad del publicador u operador;
- tipo de fuente y canal (registro oficial, auditor, emisor, explorer, exchange, investigación, dataset, API, observación on-chain, etc.);
- autoridad/competencia declarada y jurisdicción/alcance;
- URL/locator o identificador estable, fecha de publicación y condiciones de acceso;
- relaciones de dependencia/copia/sindicación, propietarios comunes y metodología compartida;
- licencia, permisos, restricciones y evidencia de autorización de uso;
- historial de correcciones, retractaciones y cambios de operador;
- provenance y método de verificación de la identidad de la fuente.

La identidad de una fuente no presupone su independencia, autoridad, licencia ni veracidad.

### 2.3 EvidenceRecord: preservación e integridad

EvidenceRecord preserva el artefacto u observación original, sin reinterpretarlo como conclusión. Campos conceptuales:
- evidence_id, evidence_version y tipo;
- source_ref(s), locator, captura/payload o referencia de almacenamiento;
- digest criptográfico, algoritmo y tamaño cuando corresponda;
- formato, parser/extractor y versión;
- captured_at, observed_at, published_at, ingested_at y recorded_at, conservando desconocidos y precisión;
- valid_time del hecho observado cuando pueda establecerse, separado del tiempo de captura;
- contexto de observación: chain/network, block height/hash, transaction/event, endpoint, query, parámetros, método y entorno;
- transformación derivada: parent evidence refs, código/método/versión, parámetros y digest de salida;
- integridad/verificación, custodia y lifecycle;
- restricciones de acceso, clasificación y retención.

Una evidencia derivada no sustituye ni altera el original. Su cadena de transformación debe ser reproducible o declarar límites. Un digest prueba igualdad de bytes bajo un algoritmo, no autenticidad, exactitud, completitud ni verdad.

### 2.4 SourceAssessment contextual

SourceAssessment se emite para una SourceRecord, un scope y un intended use; no debe existir un rating universal que se aplique sin contexto.

Dimensiones separadas:
- identity_verification;
- authority/competence;
- methodological_transparency;
- directness/proximity to observed fact;
- independence/dependency lineage;
- conflict_of_interest/incentives;
- integrity/security;
- access/licensing/permission;
- coverage and known limitations;
- freshness for intended use;
- correction/retraction history.

Los resultados de cada dimensión y sus evidencias se conservan. No se promedian para producir una cifra global que oculte un veto legal, un fallo de integridad o una dependencia común. La política de uso determina si una fuente puede ser aceptada, limitada, puesta en cuarentena o rechazada.

### 2.5 ClaimEvidenceAssessment contextual y direccional

Se emite por tupla:
`(claim_id/version, evidence_id/version, purpose, scope_id/version, knowledge_cutoff, policy_version)`.

Debe declarar:
- relación probatoria: SUPPORTS, REFUTES, NEUTRAL, INCONCLUSIVE;
- relevancia/directness y cobertura del claim;
- compatibilidad temporal, jurisdiccional, tipológica y de sujeto;
- SourceAssessment utilizado y versión;
- admissibility outcome y reason codes;
- limitaciones, dependencia con otras evidencias y contradicciones;
- evaluator/method/version, evaluated_at y lifecycle.

La dirección probatoria no es la resolución final del claim. Una evidencia puede apoyar una parte, ser neutral para otra y refutar una tercera. Si el claim no es atómico, se rechaza la evaluación o se exige descomposición.

Vocabulario de admisibilidad propuesto para ClaimEvidenceAssessment:
- ADMISSIBLE;
- ADMISSIBLE_WITH_LIMITATIONS;
- INSUFFICIENT;
- QUARANTINED;
- REJECTED;
- NOT_ASSESSED;
- INVALID (error de contrato, fuera de conclusión factual).

La relación probatoria y la admisibilidad son ejes distintos: evidencia REJECTED puede parecer SUPPORTS en contenido, pero no aporta soporte admisible. Evidencia ADMISSIBLE puede ser NEUTRAL o INCONCLUSIVE y no demuestra el claim.

### 2.6 Independencia y corroboración

1. Dos URLs, reportes o APIs no son dos fuentes independientes si replican la misma fuente primaria, comparten propietario, proveedor, método o linaje material.
2. El sistema debe modelar source_family/dependency edges y la base de independencia declarada, con evidencia y nivel de certeza.
3. Independencia UNKNOWN no se interpreta como independencia confirmada.
4. El conteo de referencias no equivale a corroboración.
5. Una única fuente primaria autoritativa puede satisfacer un estándar si la policy del claim lo permite; múltiples fuentes secundarias no la sustituyen automáticamente.
6. La agregación de evidencia se realiza por claim type y PredicateContract, con reglas explícitas de independencia, suficiencia, cobertura, contradicción y autoridad.
7. No se permite sumar o promediar puntuaciones de evidencia para compensar una condición obligatoria no satisfecha.
8. La falta de una fuente alternativa no refuta la fuente existente; la ausencia de corroboración puede limitar el nivel de conclusión según policy.
9. Los conflictos materiales no resueltos producen CONFLICTED/UNKNOWN según la regla de adjudicación; no se resuelven por mayoría simple ni por reputación genérica.
10. La dependencia entre fuentes no probada no autoriza tratarlas como independientes.

### 2.7 Resolución de claim y agregación

ClaimResolutionRecord se calcula desde ClaimEvidenceAssessments admisibles, PredicateContract y política de adjudicación. Estados propuestos conservan D-04: CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, NOT_ASSESSED, RETRACTED.

Reglas:
- CONFIRMED requiere cobertura completa de condiciones necesarias y estándar probatorio satisfecho.
- REFUTED requiere evidencia admisible que refute la proposición exacta con cobertura suficiente.
- UNKNOWN si falta evidencia, cobertura, independencia, temporalidad o condición necesaria; también si solo hay soporte inadmisible.
- CONFLICTED si hay soporte y refutación material admisibles incompatibles sin adjudicación.
- NOT_ASSESSED si no se ha ejecutado la evaluación.
- RETRACTED conserva el registro y requiere razón/autoridad; el uso actual queda sujeto a una resolución sustituta.
- INVALID es fallo de contrato, no un resultado del claim.

La política no puede convertir falta de evidencia en falsedad ni evidencia rechazada en refutación.

### 2.8 Licencias, seguridad y retención

La admisibilidad no elimina restricciones legales/contractuales. Una evidencia puede ser probatoriamente pertinente pero no estar autorizada para almacenamiento, redistribución o un propósito concreto. Deben separarse access authorization, lawful basis/licence, retention, confidentiality y evidentiary use. El sistema no debe copiar datos restringidos por defecto; puede conservar metadatos, hashes y locator cuando la política lo permita. Requiere revisión legal y de seguridad.

### 2.9 Escenarios D-05

| ID | Escenario | Resultado exigido |
|---|---|---|
| D05-01 | Tres sitios reproducen un mismo comunicado | Un linaje primario y dependencias; no contar tres corroboraciones |
| D05-02 | Auditor independiente inspecciona reservas | Evaluar competencia, alcance, fecha, metodología y limitaciones; no inferir redención |
| D05-03 | Digest coincide | Integridad de bytes corroborada; no verdad/autenticidad por sí sola |
| D05-04 | Evidencia admisible pero neutral | No aporta TRUE ni FALSE |
| D05-05 | Evidencia rechazada cuyo contenido contradice claim | No aporta refutación lógica |
| D05-06 | Fuente oficial con dato desactualizado | Autoridad no elimina freshness; evaluar por purpose/cutoff |
| D05-07 | Fuente primaria y copias dependientes | Política cuenta independencia, no número de URLs |
| D05-08 | Dos fuentes admisibles se contradicen | CONFLICTED o UNKNOWN según adjudicación; no mayoría simple |
| D05-09 | EvidenceRecord derivado de otra evidencia | Conservar parents, método, versión y digest; no tratar como origen independiente |
| D05-10 | Fuente con licencia incompatible con uso | No admisible para ese uso aunque sea relevante |
| D05-11 | Falta evidencia alternativa | No refutar la existente; aplicar estándar de suficiencia |
| D05-12 | Fuente cambia/retracta contenido | Nuevo EvidenceRecord/versión y lifecycle; preservar el anterior |

## 3. D-06 — Adaptador de evidencia y claims a FSA-01

### 3.1 Decisión propuesta

El adaptador no acepta status strings aislados. Recibe un paquete de evaluación inmutable y tipado, y emite un resultado lógico para una expresión de pertenencia específica.

Firma conceptual:
`MembershipEvaluation = f(PredicateContract, ScopeDefinition, Subject, ClaimResolutionRecords, ClaimEvidenceAssessments, SourceAssessments, UniverseSnapshot, KnowledgeCutoff, PolicyVersions)`.

El resultado se compone de:
- validity: VALID o INVALID;
- knowledge_value: TRUE, FALSE o UNKNOWN solo si validity=VALID;
- reason_codes;
- support/refutation proof references;
- coverage/completeness declaration;
- adapter_version y evaluation digest.

INVALID no es cuarto valor lógico de Kleene. Si validity=INVALID, no existe knowledge_value válido y el motor debe bloquear la composición.

### 3.2 Requisitos del PredicateContract

Cada predicado que participe en pertenencia deberá declarar:
1. predicate_id/version y semántica normativa;
2. subject/object domain y range;
3. qualifiers y dimensiones obligatorias;
4. claims necesarios y suficientes, con referencias a versiones;
5. reglas de composición (AND/OR/NOT/quantifiers) y operador de tres valores;
6. evidencia admisible y estándar de prueba por claim;
7. reglas de autoridad, independencia, cobertura y adjudicación;
8. scope transform y compatibilidad con Scope Algebra;
9. open-world por defecto; closed-world solo con universo, población, cobertura, cutoff y completitud demostrables;
10. condiciones exactas para TRUE, FALSE, UNKNOWN e INVALID;
11. freshness/temporal policy y point-in-time;
12. reason codes y tipos de proof artifact;
13. límites de complejidad y fail-closed behavior;
14. versión del adapter/engine y estrategia de replay.

### 3.3 Tabla de mapping controlado

| Entrada probatoria válida | Condición | Salida lógica |
|---|---|---|
| Claim CONFIRMED | Predicate contract exacto; condiciones necesarias cubiertas por evidencia admisible; scope/cutoff coinciden | TRUE |
| Claim REFUTED | Refutación admisible, exacta y suficiente de la condición de pertenencia | FALSE |
| Claim REFUTED | Refuta claim relacionado, no la condición exacta | UNKNOWN si la condición depende del dato; no propagar FALSE |
| Claim UNKNOWN/CONFLICTED/NOT_ASSESSED | Material para el predicado | UNKNOWN |
| Claim RETRACTED | Sin resolución sustituta vigente | UNKNOWN para uso actual |
| Evidencia INSUFFICIENT/QUARANTINED/REJECTED | Es la única evidencia relevante | No aporta soporte; UNKNOWN salvo que otra evidencia admisible baste |
| Evidencia con freshness STALE | Intended use requiere actualidad y cutoff no permite aceptarla | UNKNOWN para ese uso/cutoff |
| Claim y evidence assessments compatibles | Solo parte de una condición compuesta probada | Componer por regla explícita; no elevar condición completa a TRUE |
| Scope/universe incompleto o incomparables | Impide demostrar pertenencia | UNKNOWN o INVALID si el contrato estructural está malformado |
| Tipo, versión, firma o referencia obligatoria inválida | Contrato incumplido | INVALID (sin knowledge_value) |

### 3.4 Reglas para TRUE

TRUE solo puede emitirse si:
- envelope y todas las referencias son estructural/semánticamente válidas;
- el sujeto, predicate version, qualifiers, scope, universe y cutoff coinciden con el contrato;
- cada condición necesaria está cubierta por evidence assessments admisibles;
- se satisfacen autoridad, independencia, freshness, temporalidad y estándar probatorio;
- no queda contradicción material no adjudicada;
- existe proof bundle que identifica los claims/evidencias y la regla exacta que sustenta cada condición.

No basta un ClaimResolutionStatus=CONFIRMED sin comprobar la compatibilidad contextual con el predicado de pertenencia.

### 3.5 Reglas para FALSE

FALSE solo puede emitirse si:
- el contrato define la condición como decidible bajo la evidencia/cobertura disponible;
- una proposición exacta, admisible y suficiente demuestra no pertenencia;
- o el contrato permite closed-world y existe evidencia verificable de universo/cobertura/completitud/cutoff;
- la negación se refiere al mismo sujeto, scope, periodo, qualifiers y versión;
- no existen huecos materiales que impidan concluir ausencia.

La ausencia de registro en grafo abierto, la falta de corroboración, la evidencia rechazada, un error de consulta o la ausencia de una fuente no producen FALSE.

### 3.6 Reglas para UNKNOWN e INVALID

UNKNOWN es resultado epistemológico válido: información incompleta, conflicto, evidencia insuficiente, dependencia no resuelta, freshness inadecuada, universo abierto o cobertura parcial.

INVALID es error de contrato: firma incompatible, referencia/version no resoluble, tipos incorrectos, unidad obligatoria ausente, scope malformado, predicate contract inválido o proof bundle inconsistente. INVALID bloquea el cálculo afectado; no se degrada silenciosamente a UNKNOWN ni FALSE. Un error externo transitorio se clasifica por el contrato de ejecución (por ejemplo, evaluación no disponible), no se disfraza como hecho.

### 3.7 Composición y múltiples evidencias

1. Aplicar lógica fuerte de Kleene solo después de validar cada entrada y operador.
2. Una entrada INVALID requerida invalida la expresión compuesta; no permitir que FALSE AND INVALID o TRUE OR INVALID oculten un error estructural de contrato requerido.
3. Evidencia inadmisible se elimina del conjunto de soporte/refutación antes de adjudicar; su rechazo se conserva en auditoría.
4. Evidencia independiente se evalúa conforme a source lineage; no usar conteo simple.
5. Contradicciones entre evidencias admisibles se mantienen visibles. El contrato define si una fuente autoritativa resuelve, si se necesita adjudicación o si la salida es UNKNOWN/CONFLICTED.
6. No se permite usar una puntuación probabilística/confidence como sustituto de TRUE/FALSE/UNKNOWN. Si se calcula confianza, es atributo separado, calibrado y con metodología validada.
7. Subscopes producen evaluaciones separadas con IDs y proofs propios; no heredan TRUE de un parent ni alteran el parent.
8. La evaluación es determinista respecto de snapshots, versiones, policies y cutoff idénticos.

### 3.8 Proof bundle y replay

Cada resultado debe poder reconstruirse con:
- predicate contract y digest;
- ScopeDefinition y digest;
- subject canonical ref/version;
- universe snapshot/cutoff;
- claim/resolution/evidence/source assessment IDs y versiones;
- source lineage/dependency snapshot;
- reglas de admisibilidad y adjudicación;
- proof obligations, support/refutation refs y coverage manifest;
- adapter/engine/version, parameters y digest de salida;
- evaluated_at y provenance.

Un digest no reemplaza los datos o referencias necesarios para replay. Si una referencia se pierde, el resultado no se promociona a TRUE/FALSE; se marca como no reproducible/review según política.

### 3.9 Escenarios D-06

| ID | Escenario | Resultado exigido |
|---|---|---|
| D06-01 | Claim confirmado pero evidence inadmisible | UNKNOWN; no TRUE |
| D06-02 | Claim refutado por evidencia inadmisible | UNKNOWN; no FALSE |
| D06-03 | Claim confirmado con scope distinto | UNKNOWN/no match |
| D06-04 | Claim refutado exacto con cobertura completa | FALSE si PredicateContract lo declara suficiente |
| D06-05 | Ausencia de edge en grafo abierto | UNKNOWN |
| D06-06 | Ausencia en universo cerrado con cobertura probada | FALSE solo bajo regla closed-world |
| D06-07 | Dos fuentes dependientes coinciden | No contar como corroboración independiente |
| D06-08 | Evidencia admisible conflictiva sin adjudicación | UNKNOWN |
| D06-09 | Un conjunct necesario UNKNOWN | Conjunción no TRUE según Kleene |
| D06-10 | Referencia obligatoria rota o tipo incompatible | INVALID; bloquear |
| D06-11 | Un sub-scope confirmado | No elevar parent scope a TRUE |
| D06-12 | Mismos snapshots y versiones | Resultado y proof digest deterministas |
| D06-13 | Evento tardío corrige evidencia histórica | Nuevo replay/evaluation; no reescribir registro anterior |
| D06-14 | Se pierde un artefacto requerido para replay | No declarar resultado reproducible; revisión según política |

## 4. Riesgos residuales y decisiones humanas

- Definir estándares de prueba por familias de claims (on-chain, legal, reservas, tokenomics, control, gobernanza, seguridad, mercado).
- Aprobar taxonomía de fuentes y criterio de autoridad por jurisdicción.
- Aprobar tratamiento de source lineage incompleto.
- Determinar cuándo se permite closed-world y quién certifica cobertura.
- Definir licencias, retención, secretos comerciales y protección de datos.
- Revisar la lógica estricta de INVALID ante operadores compuestos y armonizarla con FSA-01.
- Demostrar soundness de las reglas de lower/upper approximations y mapping con revisión matemática independiente.
- Diseñar fixtures adversariales y pruebas diferenciales; todavía no existen aquí.

## 5. Criterios de aprobación

- [ ] Se aprueban SourceRecord, EvidenceRecord, SourceAssessment y ClaimEvidenceAssessment como objetos separados.
- [ ] Se aprueba source lineage y la política de independencia/corroboración.
- [ ] Se aprueba el estándar probatorio por tipo de claim y purpose/scope.
- [ ] Se aprueban licencias, acceso, retención y security handling.
- [ ] Se aprueba el contrato PredicateContract y su relación con Scope Algebra.
- [ ] Se aprueba mapping TRUE/FALSE/UNKNOWN/INVALID con revisión matemática.
- [ ] Se aprueba comportamiento ante INVALID en composiciones y errores transitorios.
- [ ] Se aprueba proof bundle, digest y replay.
- [ ] Se revisan D05-01..12 y D06-01..14.
- [ ] Se deriva suite automatizada y se ejecuta en CI tras aprobación.

## 6. Estado final

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

D-05 y D-06 quedan desarrolladas documentalmente con 26 escenarios. No se modificaron documentos metodológicos canónicos, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas, demostraciones formales ni validación empírica.
