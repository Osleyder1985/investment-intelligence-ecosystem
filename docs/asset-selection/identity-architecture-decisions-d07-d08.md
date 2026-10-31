# Resolución arquitectónica propuesta — D-07 y D-08

**ID:** IIE-IDENTITY-ARCH-DECISIONS-008  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Dependencias:** D-01..D-06; Lifecycle/Freshness/Invalidation Framework; Scope Algebra; FSA-01  
**Issue:** #61

## 1. Propósito y límites

D-07 establece un contrato temporal auditable para hechos, observaciones, disponibilidad de información y reconstrucción point-in-time. D-08 establece cómo los cambios y eventos recorren dependencias, distinguiendo señales provisionales, revisión e invalidación confirmada.

La propuesta reutiliza el Lifecycle Framework existente; no crea una máquina de estados paralela. No modifica requisitos IIM, documentos canónicos, schemas, código, fixtures ni tests. No implica aprobación institucional ni validación empírica.

## 2. D-07 — Temporalidad y reconstrucción point-in-time

### 2.1 Decisión propuesta: temporalidad multidimensional

No existe un único timestamp capaz de representar simultáneamente cuándo ocurrió un hecho, cuándo fue válido, cuándo se observó, cuándo se publicó, cuándo estuvo disponible y cuándo lo conoció el sistema.

Cada registro temporal debe distinguir, según aplique:

| Campo | Significado | No debe confundirse con |
|---|---|---|
| valid_time | Intervalo durante el cual el hecho era válido en el dominio | Fecha de captura/publicación |
| occurred_at | Momento/intervalo en que ocurrió el evento real | Momento de detección |
| source_published_at | Momento en que la fuente publicó el contenido | Disponibilidad pública efectiva |
| public_availability_at | Momento desde el cual un miembro del público podía acceder al contenido | Fecha que declara el documento |
| authorized_availability_at | Momento desde el cual el sistema/actor tenía acceso legal y técnicamente autorizado | Publicación pública |
| observed_at | Momento en que una fuente/sistema observó el hecho | Ingesta |
| captured_at | Momento de captura del artefacto | Tiempo de validez del hecho |
| ingested_at | Momento en que el dato entró al pipeline | Registro durable |
| recorded_at | Momento en que quedó registrado en el sistema | Momento real del evento |
| evaluated_at | Momento de evaluación de una regla/assessment | Fecha del hecho evaluado |
| effective_at | Momento desde el que una transición/cambio produce efecto | Momento de recepción del evento |
| knowledge_cutoff | Límite temporal de información permitida para una reconstrucción | Fecha de evaluación actual |

Los campos no aplicables se omiten con razón explícita; los desconocidos permanecen UNKNOWN. No se sustituyen entre sí ni se rellenan con timestamps cercanos por conveniencia.

### 2.2 Contrato de timestamp

Cada timestamp o límite temporal debe preservar:
- valor original exacto y texto/representación de origen;
- valor normalizado (UTC cuando sea posible);
- timezone/offset original y método de conversión;
- precisión/resolución conocida;
- intervalo de incertidumbre o incertidumbre explícita;
- reloj/origen y método de sincronización cuando aplique;
- tipo semántico del tiempo;
- evidencia y regla de derivación;
- estado: KNOWN, UNKNOWN, NOT_APPLICABLE o INVALID según contrato aprobado.

No inferir zona, precisión ni orden temporal. Si la fuente solo declara una fecha sin hora, el dato representa esa precisión, no medianoche UTC. Si se conoce un intervalo, preservar el intervalo; no reducirlo a un instante arbitrario.

Para comparaciones, definir una semántica uniforme de intervalos, límites y zona. La propuesta preferida es UTC normalizado y ventanas semiabiertas [start,end), preservando la expresión original. La convención debe aprobarse antes de implementación.

### 2.3 Point-in-time: dos preguntas distintas

Toda reconstrucción histórica debe declarar al menos:

1. **Fact-time question:** ¿qué hechos eran válidos en el dominio en el instante/intervalo objetivo?
2. **Knowledge-time question:** ¿qué información podía conocer legal y técnicamente el actor/sistema en el cutoff declarado?

Una evaluación point-in-time requiere ambas dimensiones cuando la decisión histórica depende del conocimiento disponible. El motor no puede usar un documento publicado posteriormente para justificar que una decisión anterior estaba informada, aunque el documento describa un hecho que ya existía.

### 2.4 Knowledge profile y cutoff

Cada reconstrucción debe fijar:
- target_time / valid-time query;
- knowledge_cutoff;
- actor/system knowledge profile (qué fuentes, credenciales y canales estaban disponibles);
- reglas de public/authorized availability;
- timezone, precisión y regla de frontera;
- universo y snapshots;
- versions de datos, source assessments, claim assessments, policies y código;
- tratamiento de retrasos, revisiones, correcciones y retractaciones;
- objetivo de reconstrucción: as-known-then, corrected-history o current-best-understanding.

No combinar perfiles de conocimiento diferentes en una misma evaluación sin declararlo. Un sistema que recibió un feed privado no debe reconstruir el conocimiento de un inversor público usando ese feed.

### 2.5 Tres vistas históricas no intercambiables

| Vista | Pregunta | Uso |
|---|---|---|
| AS_KNOWN_THEN | ¿Qué podía conocer el actor en ese momento? | Backtesting sin look-ahead y auditoría de decisiones |
| AS_VALID_THEN | ¿Qué hechos eran válidos entonces, según evidencia actualmente disponible? | Reconstrucción factual retrospectiva |
| CURRENT_CORRECTED_HISTORY | ¿Cuál es hoy la mejor reconstrucción histórica corregida? | Investigación, restatement y análisis de errores |

AS_VALID_THEN y CURRENT_CORRECTED_HISTORY pueden incorporar evidencia publicada después del target_time; AS_KNOWN_THEN no puede hacerlo si excede el knowledge_cutoff. Los resultados deben etiquetarse y nunca mezclarse silenciosamente.

### 2.6 Admisibilidad temporal de evidencia

Una evidencia puede ser:
- válida para un hecho histórico, pero no conocida en el cutoff;
- conocida en el cutoff, pero referirse a un hecho futuro;
- publicada en una fecha, pero accesible más tarde;
- capturada ahora desde un endpoint que expone datos históricos modificables;
- corregida/retractada después de la decisión.

La evaluación debe comprobar por separado:
- compatibilidad de valid_time con el periodo consultado;
- disponibilidad pública/authorized availability <= cutoff según el perfil;
- ingest/recording time para reconstrucción del conocimiento interno;
- point-in-time snapshot del contenido realmente disponible;
- correcciones/retractaciones conocidas a la fecha;
- frescura apropiada para purpose y activity.

La fecha de publicación declarada no prueba por sí sola la disponibilidad histórica. Si no hay evidencia suficiente de disponibilidad, el resultado point-in-time es UNKNOWN o REVIEW_REQUIRED, no una suposición optimista.

### 2.7 Eventos tardíos, revisiones y orden parcial

Un evento recibido en t2 puede describir un hecho ocurrido en t1<t2. Se preservan ambos tiempos y el momento en que el sistema lo conoció. Un evento tardío crea una nueva evaluación/replay; no reescribe registros ni decisiones previas.

Cuando los intervalos temporales se solapan y no demuestran precedencia, el orden es UNRESOLVED. No inventar secuencia causal a partir del orden de llegada. Los eventos deben incluir causation_id/correlation_id cuando existan, sin equiparar correlación a causalidad.

### 2.8 Integridad de series temporales y datos de mercado

Para market data, cada observación debe identificar instrumento/venue, tipo de dato, timestamp de evento/mercado, timestamp de recepción, ventana, zona, secuencia/ID si existe, condiciones de agregación, correcciones y fuente. No usar timestamp del candle como prueba de cuándo fue visible el candle completo. Para OHLCV, funding, open interest, order books y métricas on-chain, la semántica de disponibilidad y finalización es específica del proveedor/endpoint.

Un snapshot actual de una API que devuelve histórico no demuestra que ese dato estuviera disponible históricamente ni que no haya sido revisado. Deben conservarse snapshots point-in-time o evidencia verificable de versiones históricas.

### 2.9 Escenarios D-07

| ID | Escenario | Resultado exigido |
|---|---|---|
| D07-01 | Incidente ocurrió ayer, alerta recibida hoy | occurred_at y recorded_at distintos; evento tardío |
| D07-02 | Informe describe hecho pasado pero se publica después del cutoff | Excluirlo de AS_KNOWN_THEN |
| D07-03 | Informe publicado, pero acceso autorizado comienza después | No admitir antes de authorized_availability_at |
| D07-04 | Timestamp sin zona | Preservar original; no asumir UTC |
| D07-05 | Fuente solo da fecha | Mantener precisión de día; no inventar hora |
| D07-06 | Dos eventos con intervalos solapados | Orden UNRESOLVED salvo prueba adicional |
| D07-07 | API reescribe histórico | No asumir disponibilidad histórica; exigir snapshot/versionado |
| D07-08 | Dato válido entonces pero descubierto después | Puede entrar en AS_VALID_THEN; no AS_KNOWN_THEN |
| D07-09 | Corrección posterior de un informe | Nueva versión y replay; preservar versión conocida en cutoff |
| D07-10 | Feed privado no disponible a público | Excluir de perfil público; admitir solo perfil autorizado que lo pruebe |
| D07-11 | Candle tiene close_time pero se consulta antes de cierre | No tratar candle final como conocido |
| D07-12 | Mismo cutoff, distinto perfil de acceso | Evaluaciones distintas, explícitamente etiquetadas |
| D07-13 | Zona/offset y normalización inconsistentes | INVALID si contrato no puede resolver la representación |
| D07-14 | Backtest usa métricas históricas actuales sin snapshots | No declarar point-in-time validado |

## 3. D-08 — Lifecycle y propagación de dependencias

### 3.1 Decisión propuesta: reutilizar el Lifecycle Framework

No crear estados ni reglas de invalidación paralelos dentro del subsistema de identidad. Reutilizar los conceptos del framework de Lifecycle:
- objetos y versiones upstream/downstream;
- DependencyEdge tipado;
- scope transform;
- intended use;
- propagation rule/version;
- lifecycle events;
- invalidation records;
- reassessment requests;
- causation/correlation;
- idempotency;
- ownership, audit trail y replay.

Identity, Evidence, Claim, SourceAssessment, PurposeAssessment, Scope, Decision y Operational Route deben integrarse mediante referencias y contratos explícitos.

### 3.2 DependencyEdge tipado

Cada dependencia debe declarar:
- edge_id/version;
- upstream_object_id/version/type;
- downstream_object_id/version/type;
- dependency_class: REQUIRED, MATERIAL, OPTIONAL o CONTEXTUAL;
- intended_use y decision_type;
- source_scope y target_scope;
- scope_transform_id/version;
- propagation_rule_id/version;
- temporal applicability;
- owner y creation/effective/retired timestamps;
- causation/correlation references;
- evidence/justification for the edge;
- status and lifecycle.

La dependencia no se infiere por proximidad, mismo ticker, mismo proyecto, mismo proveedor o mera relación de grafo. La transitividad no se presume: A→B y B→C no demuestran automáticamente A→C para todo propósito.

### 3.3 Clasificación de propagación

Para cada downstream object, la evaluación de impacto debe producir una de estas clasificaciones:
- AFFECTED: el cambio altera una premisa material dentro del scope dependiente;
- PARTIALLY_AFFECTED: afecta solo un subconjunto demostrable del scope;
- NOT_AFFECTED: existe evidencia positiva de independencia/no materialidad para el uso y scope;
- UNRESOLVED: dependencia, scope o materialidad no pueden determinarse;
- NOT_EVALUATED: el análisis de impacto aún no se ejecutó.

NOT_AFFECTED nunca se deriva de ausencia de dependencia registrada. UNRESOLVED no equivale a NOT_AFFECTED. NOT_EVALUATED no es un resultado favorable.

### 3.4 Evento, señal provisional e invalidación confirmada

Separar tres etapas:

1. **Trigger/evento recibido:** noticia, alerta, cambio de código, corrección, retractación, incidente o modificación de política. Puede ser no verificado.
2. **Impact assessment:** verifica autenticidad, alcance, materialidad, dependencias y reglas de propagación.
3. **Lifecycle transition:** emite REVIEW_REQUIRED, QUARANTINED, INVALIDATED, SUPERSEDED o reassessment según autoridad y evidencia.

Una señal creíble no verificada puede justificar revisión urgente o cuarentena provisional en un scope plausible. No debe marcar automáticamente todos los downstream como INVALIDATED. Una invalidación confirmada requiere evidencia, regla aplicable, scope, autoridad y registro de decisión. Las medidas preventivas de seguridad pueden ser más conservadoras, pero deben registrarse como controles provisionales distintos de la conclusión factual.

### 3.5 Algoritmo de impacto propuesto

1. Registrar evento inmutable con origen, evidencia, timestamps, integridad, estado de verificación y causa.
2. Resolver objeto/version upstream; si no se identifica, abrir revisión de universo/consumidores según política, sin inventar vínculo.
3. Consultar snapshot versionado del grafo de dependencias válido para el tiempo y purpose.
4. Validar cada DependencyEdge, scope transform, lifecycle y propagation rule.
5. Calcular intersección del scope afectado con el scope downstream usando Scope Algebra; no ampliar scope por aproximación.
6. Evaluar materialidad y condiciones del PredicateContract/consumer.
7. Clasificar impacto AFFECTED, PARTIALLY_AFFECTED, NOT_AFFECTED, UNRESOLVED o NOT_EVALUATED con proof/reasons.
8. Emitir invalidation/reassessment/review events según clasificación y autoridad.
9. Recalcular solo objetos afectados o parcialmente afectados; para UNRESOLVED aplicar revisión/abstención según policy.
10. Preservar el resultado previo, la ruta de propagación, versiones, proof bundle e idempotency key.

### 3.6 Reglas de propagación por tipo de cambio

| Cambio upstream | Propagación esperada (condicionada por dependencia) | No inferir |
|---|---|---|
| Entity identity / network binding | Claims, mappings, universe membership, analytics y routes que referencien esa identidad/representación | Que todas las representaciones o activos relacionados son inválidos |
| Evidence correction/retraction | ClaimEvidenceAssessment y ClaimResolution dependientes; downstream claims/decisions por edge | Que toda la fuente o todos sus datos son falsos |
| Source compromise | Assessments que dependan de esa fuente y claims soportados exclusivamente o materialmente por ella | Que fuentes independientes alternativas también quedan invalidadas |
| Contract migration/upgrade | Identidad, security, tokenomics, control y route scopes dependientes | Que el activo económico subyacente desaparece |
| Bridge exploit | Representación bridged/wrapped, backing/redemption y rutas relacionadas | Propagación al activo nativo sin edge probado |
| Market data correction | Métricas/valoración/liquidez/decisiones dependientes del intervalo y mercado | Invalidación automática de identidad o hechos históricos no dependientes |
| Policy/method change | Nuevas evaluaciones o replay bajo nueva versión | Reescritura de decisiones históricas bajo policy antigua |
| Venue/custody incident | Operational Route Eligibility y portfolio controls dependientes | Invalidación automática de Analytical Eligibility |
| Jurisdiction change | Evaluaciones legales y actividades/usuarios/venues cubiertos por jurisdicción | Extensión global sin scope jurídico |
| Dependency edge correction | Impact assessment y replay de consumidores potencialmente conectados | Propagación transitiva no demostrada |

### 3.7 Scope parcial, fan-out y aislamiento

- Cada propagación conserva source_scope, target_scope y transform explícitos.
- Si solo una representación, venue, jurisdicción, periodo o actividad queda afectada, el resto no se invalida automáticamente.
- Si la partición exacta no puede probarse, el resultado es UNRESOLVED y no se declara el resto NOT_AFFECTED.
- Aislar Analytical Eligibility (AE) de Operational Route Eligibility (ORE), conforme a los contratos existentes.
- El fan-out debe limitarse por dependencias versionadas y políticas; límites computacionales alcanzados producen estado incompleto/revisión, no una propagación truncada presentada como completa.
- El cierre de un incidente no restaura automáticamente estados anteriores; exige remediación verificada, nueva evaluación y autoridad competente.

### 3.8 Idempotencia, eventos duplicados y concurrencia

Cada evento y acción de propagación debe tener identificador estable, idempotency key, event version, causation/correlation IDs y estado de procesamiento. Duplicados no deben duplicar transiciones. Eventos fuera de orden requieren reconciliación/replay. Concurrencia sobre el mismo objeto debe usar control de versión/optimistic concurrency o mecanismo equivalente; conflictos producen retry/review, no sobrescritura silenciosa.

### 3.9 Reversión, recuperación y decisiones históricas

INVALIDATED no se revierte borrando el evento. La recuperación requiere:
- evidencia de remediación;
- verificación independiente cuando el riesgo lo exija;
- nuevo assessment con scope/temporalidad;
- revisión de todas las dependencias afectadas;
- autorización según RACI/SoD;
- nuevo evento de recuperación y enlace a la invalidación original.

Las decisiones históricas permanecen inmutables. Una evaluación corregida se almacena como nuevo resultado, indicando si representa AS_KNOWN_THEN, AS_VALID_THEN o CURRENT_CORRECTED_HISTORY. Una política nueva puede requerir replay, pero no cambia retroactivamente la política que se aplicó.

### 3.10 Escenarios D-08

| ID | Escenario | Resultado exigido |
|---|---|---|
| D08-01 | Fuente reporta posible exploit no verificado | Trigger provisional y revisión; no invalidación factual automática |
| D08-02 | Exploit confirmado en bridge específico | Propagar a representaciones/routes dependientes, scope probado |
| D08-03 | Cambio de identidad de token | Reevaluar claims y decisiones que referencien la identidad/version |
| D08-04 | Incidente exclusivo de venue | ORE/portfolio dependiente; AE aislada salvo dependencia material |
| D08-05 | Edge downstream no registrado | No concluir NOT_AFFECTED; UNRESOLVED |
| D08-06 | Cambio afecta solo una jurisdicción | Propagación parcial y aislamiento geográfico/jurídico |
| D08-07 | Fuente retracta un informe | Reevaluar claims dependientes; no declarar todos los claims falsos |
| D08-08 | Dos eventos duplicados | Idempotencia; una transición efectiva |
| D08-09 | Evento llega fuera de orden | Reconciliación/replay con historial conservado |
| D08-10 | Límite de fan-out alcanzado | Propagación incompleta/revisión; no declarar cierre completo |
| D08-11 | Bridge remediado | Nueva verificación y autorización; no levantar veto automáticamente |
| D08-12 | Policy cambia | Replay versionado; preservar decisiones anteriores |
| D08-13 | Scope afectado solo parcialmente | Partición exacta y decisiones independientes por sub-scope |
| D08-14 | DependencyEdge cambia | Versionar edge y reevaluar consumidores potencialmente afectados |
| D08-15 | No se puede probar no afectación | UNRESOLVED; no NOT_AFFECTED |
| D08-16 | Reassessment termina favorable | Nueva evaluación enlazada; no borrar invalidación histórica |

## 4. Interfaces D-07/D-08 con D-05/D-06

- SourceRecord/EvidenceRecord conservan tiempos de origen, publicación, disponibilidad, captura, observación e ingesta.
- ClaimEvidenceAssessment y ClaimResolutionRecord declaran knowledge_cutoff y perfil de conocimiento.
- PredicateContract especifica qué tiempos son obligatorios y qué freshness aplica a cada purpose/activity.
- El adaptador FSA-01 no acepta evidencia fuera del cutoff de la vista AS_KNOWN_THEN.
- LifecycleEvent usa timestamps tipados; una corrección tardía desencadena replay y no mutación histórica.
- DependencyEdge conserva la vigencia temporal de la relación y scope transform.
- InvalidationRecord identifica evento, objeto/version, scope afectado, regla, prueba, autoridad y downstream reevaluados.
- DecisionRecord conserva snapshot temporal, versión de política, evidence lineage y modo de reconstrucción.

## 5. Riesgos y decisiones humanas pendientes

1. Definir perfiles de conocimiento: público, institucional, proveedor licenciado, interno y otros.
2. Aprobar prueba de public_availability_at y authorized_availability_at por fuente/tipo de dato.
3. Definir precisión, incertidumbre, intervalos y convención de límites.
4. Determinar estándares point-in-time específicos de exchanges, proveedores on-chain, protocolos y fuentes legales.
5. Aprobar criterios de materialidad y autoridad para invalidación.
6. Definir responsables de incident triage, impact assessment, quarantine, invalidation y recovery.
7. Definir límites operativos de fan-out y colas de revisión sin pérdida de cobertura.
8. Resolver dependencias ausentes/incompletas y cobertura del grafo.
9. Alinear el contrato con Scope Algebra, RACI y estados canónicos.
10. Validar legalmente conservación, retención y licencias de snapshots históricos.

## 6. Criterios de aprobación

- [ ] Catálogo temporal y timestamp contract aprobados.
- [ ] Distinción valid-time / knowledge-time y vistas históricas aprobada.
- [ ] Perfiles de conocimiento y reglas de cutoff aprobados.
- [ ] Evidencia de disponibilidad pública/autorizada definida por fuente.
- [ ] Tratamiento de precisión e incertidumbre aprobado.
- [ ] DependencyEdge y propagation rules alineados con Lifecycle.
- [ ] Trigger provisional separado de invalidación confirmada.
- [ ] Scope propagation y aislamiento AE/ORE aprobados.
- [ ] Idempotencia, concurrencia, fan-out y replay aprobados.
- [ ] Autoridades RACI/SoD y recuperación aprobadas.
- [ ] Escenarios D07-01..14 y D08-01..16 convertidos en fixtures/tests ejecutables.
- [ ] Tests ejecutados en CI y resultados revisados independientemente.

## 7. Estado final

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

D-07 y D-08 quedan desarrolladas documentalmente con 30 escenarios. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas ni validación matemática/empírica.
