# Propuesta de migración del vocabulario de valoración

**ID:** IIE-VAL-MIGRATION-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Ámbito:** Armonización documental del catálogo de valoración con `ValuationStatus` propuesto en X-06.  
**No alcance:** Este documento no modifica requisitos IIM, schemas, código, datos históricos ni políticas aprobadas.

## 1. Propósito y dictamen

Resolver la incompatibilidad entre los estados legacy publicados en `docs/valuation/valuation-method-catalog.md` v0.1.0 y el catálogo canónico propuesto en `docs/methodology/03-canonical-decision-state-model.md` §4.7 y `docs/methodology/11-deterministic-decision-composition-algorithm.md` §5.1.

**Dictamen:** no existe un crosswalk total uno-a-uno. La migración debe ser semántica, contextual, versionada y conservadora. Ningún estado legacy ambiguo se convierte automáticamente en un estado favorable. Se preservan el valor fuente, su documento/versión, el contexto, la regla aplicada y la evidencia de revisión.

El vocabulario objetivo propuesto para el resultado de valoración es:

- `NOT_RUN`
- `INSUFFICIENT_EVIDENCE`
- `NOT_ESTIMABLE`
- `ESTIMABLE_WITH_LIMITATIONS`
- `ESTIMABLE`

La aplicabilidad del método es una dimensión independiente: `APPLICABLE`, `NOT_APPLICABLE`, `UNDETERMINED`. La matriz actual usa además `CONDITIONAL`; su semántica debe conservarse como condición de aplicabilidad durante la transición y no debe introducirse en `ValuationStatus`.

## 2. Fuentes auditadas

| Fuente | Versión/ubicación | Observación |
|---|---|---|
| Catálogo de métodos | `docs/valuation/valuation-method-catalog.md` v0.1.0, §6 | Publica cinco estados legacy |
| Matriz de aplicabilidad | `docs/valuation/valuation-applicability-matrix.md` v0.1.0, §§1–3 | Usa APPLICABLE, CONDITIONAL, NOT_APPLICABLE e INSUFFICIENT_EVIDENCE en la misma leyenda |
| Modelo canónico | `docs/methodology/03-canonical-decision-state-model.md` §4.7 y §9 | Separa Applicability de ValuationStatus; contiene crosswalk legacy parcial |
| Composición determinista | `docs/methodology/11-deterministic-decision-composition-algorithm.md` §5.1 | Mantiene valoración en objeto separado y prohíbe un outcome universal DECISION_NOT_ESTIMABLE |
| Resolución wrapped | `docs/methodology/17-x09-wrapped-valuation-and-atomic-aggregation.md` §§2.3–2.3.1 | Define estados canónicos y documenta GAP legacy A11 |
| Matriz X-09 | `docs/methodology/14-integrated-conformance-scenario-matrix.md` §21 | A11 permanece ADDRESSED_IN_DRAFT con GAP de armonización |

## 3. Inventario del vocabulario legacy

El catálogo v0.1.0 publica:

1. `VALUATION_SUPPORTED`
2. `VALUATION_CONDITIONAL`
3. `INSUFFICIENT_EVIDENCE`
4. `NO_APPLICABLE_METHOD`
5. `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE`

La matriz de aplicabilidad publica cuatro etiquetas:

1. `APPLICABLE`
2. `CONDITIONAL`
3. `NOT_APPLICABLE`
4. `INSUFFICIENT_EVIDENCE`

Estas etiquetas de la matriz describen la relación tipología/método y no deben tratarse como el mismo enum que el resultado global de valoración. En particular, `INSUFFICIENT_EVIDENCE` en la matriz es una señal de que no puede determinarse la aplicabilidad; en el catálogo es una salida de valoración por falta de evidencia. Se requiere separar ambos campos.

## 4. Definiciones canónicas objetivo

### 4.1 Applicability

| Estado | Definición propuesta |
|---|---|
| `APPLICABLE` | El método puede estimar el objeto declarado si sus inputs y supuestos se satisfacen |
| `NOT_APPLICABLE` | Hay justificación positiva de que el método no aplica al objeto/derecho declarado |
| `UNDETERMINED` | No hay base suficiente para decidir si el método aplica |

`CONDITIONAL` de la matriz legacy no se conserva como estado canónico de aplicabilidad. Se representa como `APPLICABLE` solo después de que se satisfagan y documenten las condiciones; antes de ello, `UNDETERMINED` o un registro de condición pendiente según el contrato futuro. No se presume aplicabilidad favorable.

### 4.2 ValuationStatus

| Estado | Definición propuesta |
|---|---|
| `NOT_RUN` | No se ha ejecutado el proceso de valoración |
| `INSUFFICIENT_EVIDENCE` | Falta evidencia material para identificar/verificar el objeto, derechos o inputs necesarios |
| `NOT_ESTIMABLE` | Objeto y derechos suficientemente identificados, pero ningún modelo/método defendible puede producir una estimación bajo las condiciones actuales |
| `ESTIMABLE_WITH_LIMITATIONS` | Existe estimación defendible para un claim/escenario delimitado, con limitaciones materiales explícitas y controlables |
| `ESTIMABLE` | Método, derechos e inputs materiales están suficientemente sustentados sin limitaciones materiales pendientes |

La distinción clave es epistemológica: no conocer suficientemente el objeto/derecho conduce a `INSUFFICIENT_EVIDENCE`; conocerlo, pero no poder modelarlo de forma defendible conduce a `NOT_ESTIMABLE`. `NOT_APPLICABLE` pertenece a Applicability, no a ValuationStatus.

## 5. Crosswalk semántico conservador

El crosswalk es una propuesta de decisión por registro, no una conversión mecánica por nombre.

| Estado legacy | Destino candidato | Condiciones obligatorias | Tratamiento si no se satisfacen |
|---|---|---|---|
| `VALUATION_SUPPORTED` | `ESTIMABLE` | Confirmar objeto/claim, derechos, método aplicable, evidencia admisible, supuestos y ausencia de limitación material | `ESTIMABLE_WITH_LIMITATIONS` si el modelo sigue siendo defendible y los límites son controlables; en otro caso `INSUFFICIENT_EVIDENCE` o `NOT_ESTIMABLE` según causa |
| `VALUATION_CONDITIONAL` | `ESTIMABLE_WITH_LIMITATIONS` | Condiciones explícitas, cuantificadas cuando proceda, verificables, con responsable y enforcement real en el consumidor | No migrar a estado favorable; `UNDETERMINED`, `INSUFFICIENT_EVIDENCE`, `NOT_ESTIMABLE` o revisión manual según el hecho |
| `INSUFFICIENT_EVIDENCE` | `INSUFFICIENT_EVIDENCE` | Confirmar que el estado pertenece a valoración y que la carencia es material para el objeto/método | Si pertenece a aplicabilidad, mover a `Applicability=UNDETERMINED`; si es de selección/riesgo, conservar en su dominio y no duplicar semántica |
| `NO_APPLICABLE_METHOD` | Sin mapping directo | Confirmar que objeto/derechos están identificados y que se evaluó el conjunto de métodos pertinentes | Si todos los métodos son no aplicables, registrar `Applicability=NOT_APPLICABLE` por método y determinar el resultado global según finalidad; `NOT_ESTIMABLE` solo si no existe método defendible para el objeto conocido |
| `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE` | Ninguno automático | Reclasificación manual usando evidencia, objeto, derechos, métodos considerados y motivo original | Mantener como legacy no interpretado y bloquear conclusión favorable hasta resolver |

### 5.1 Regla de no equivalencia

- `NO_APPLICABLE_METHOD` no equivale siempre a `NOT_ESTIMABLE`: puede describir un método individual, no el conjunto de métodos.
- `VALUATION_CONDITIONAL` no equivale siempre a `ESTIMABLE_WITH_LIMITATIONS`: una condición pendiente puede impedir que exista una estimación defendible.
- `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE` no distingue entre ausencia de datos, derechos desconocidos, inexistencia de método, valoración no ejecutada o prohibición de valorar.
- `INSUFFICIENT_EVIDENCE` no debe copiarse a todos los dominios: debe conservarse el dominio y el objeto al que se refiere.
- Ningún estado de valoración libera, reduce ni sustituye un `VetoDecision`.

## 6. Corrección propuesta para la matriz de aplicabilidad

La matriz actual mezcla aplicabilidad con suficiencia de evidencia. Se propone la siguiente separación lógica para una futura versión:

- `ApplicabilityAssessment`: estado por par activo/método y objeto de valoración.
- `EvidenceAssessment`: calidad, cobertura, admisibilidad y vigencia de inputs requeridos.
- `ValuationAssessment`: método seleccionado, modelo, supuestos, outputs y `ValuationStatus`.

Crosswalk de la leyenda de matriz:

| Etiqueta actual | Interpretación destino |
|---|---|
| `APPLICABLE` | `Applicability=APPLICABLE` |
| `NOT_APPLICABLE` | `Applicability=NOT_APPLICABLE`, con justificación |
| `CONDITIONAL` | Condiciones explícitas; `APPLICABLE` solo cuando se satisfacen; en otro caso `UNDETERMINED` o condición pendiente |
| `INSUFFICIENT_EVIDENCE` | `Applicability=UNDETERMINED` si la evidencia no permite decidir aplicabilidad; la carencia de datos de valoración se registra por separado en EvidenceAssessment/ValuationStatus |

No se deben reemplazar todas las celdas actuales en bloque. Cada celda debe revisarse contra la definición de tipología, el objeto de valoración y la condición que hace aplicable el método.

## 7. Política de compatibilidad y migración

### 7.1 Principios

1. Mantener intactos los registros históricos y el estado legacy original.
2. Nunca sobrescribir el valor fuente con el valor migrado.
3. Cada mapping registra `source_enum`, `source_document`, `source_version`, `source_record_id`, `target_enum`, `mapping_rule_id`, `mapping_version`, `reviewer`, `reviewed_at`, `evidence_refs` y `migration_confidence`.
4. Mapping automático solo podrá habilitarse para casos demostrablemente no ambiguos, tras aprobación y pruebas; esta propuesta no autoriza tal habilitación.
5. Estados desconocidos, versiones incompatibles o registros sin dominio se ponen en revisión y nunca se convierten en favorables.
6. Mantener lectores legacy durante un periodo de compatibilidad aprobado; no eliminar productores/consumidores hasta inventariarlos y probar migración/reversión.
7. No mezclar estados legacy y canónicos en el mismo campo. Usar campos versionados o envelope explícito.
8. La migración no puede convertir una falta de datos en incumplimiento demostrado ni convertir ausencia de método en veto.
9. No producir rango/precio objetivo si el estado canónico es `NOT_RUN`, `INSUFFICIENT_EVIDENCE` o `NOT_ESTIMABLE`.
10. `ESTIMABLE_WITH_LIMITATIONS` solo permite una conclusión limitada cuando el consumidor puede aplicar efectivamente las restricciones.

### 7.2 Fases propuestas

| Fase | Actividad | Gate de salida |
|---|---|---|
| M0 | Inventario de productores, consumidores, persistencia, informes y fixtures | Cobertura de dependencias documentada |
| M1 | Aprobación del vocabulario y crosswalk por owners | Decisión firmada y versionada |
| M2 | Publicación de catálogo/matriz versionados y contrato de compatibilidad | Contratos aprobados; changelog y política de deprecación |
| M3 | Migración en sombra sin alterar decisiones en producción | Diferencias explicadas y reconciliadas |
| M4 | Migración controlada con rollback y auditoría | Tests de contrato, replay y rollback aprobados |
| M5 | Retiro de legacy tras periodo aprobado | Cero consumidores legacy no autorizados |

No se establecen fechas ni duración de deprecación en este draft; dependen del inventario real de consumidores y del proceso de cambio aprobado.

### 7.3 Datos históricos

Los registros históricos deben permanecer reproducibles con su vocabulario y versión originales. Si se genera una interpretación canónica, se almacena como derivación separada y no como mutación. La imposibilidad de reconstruir el contexto obliga a marcar el mapping como no resuelto.

## 8. Casos de aceptación propuestos

Estos casos son especificaciones futuras, no fixtures ni pruebas ejecutadas.

| ID | Input | Resultado esperado |
|---|---|---|
| VM-01 | `VALUATION_SUPPORTED`, derechos y modelo defendibles, sin limitaciones materiales | Candidato `ESTIMABLE` tras revisión |
| VM-02 | `VALUATION_SUPPORTED`, existe limitación material explícita y exigible | Candidato `ESTIMABLE_WITH_LIMITATIONS`; no `ESTIMABLE` |
| VM-03 | `VALUATION_CONDITIONAL`, condición no satisfecha | No favorable; no mapping automático a `ESTIMABLE_WITH_LIMITATIONS` |
| VM-04 | `INSUFFICIENT_EVIDENCE` en valoración, objeto/derechos no verificables | `ValuationStatus=INSUFFICIENT_EVIDENCE` |
| VM-05 | `INSUFFICIENT_EVIDENCE` en matriz para determinar aplicabilidad | `Applicability=UNDETERMINED`; registrar evidencia separadamente |
| VM-06 | `NO_APPLICABLE_METHOD` para un método, otro método aplicable | No declarar valoración global `NOT_ESTIMABLE` por el descarte individual |
| VM-07 | Todos los métodos relevantes no aplican a objeto conocido | Applicability no aplicable por método; resultado global requiere regla de finalidad aprobada |
| VM-08 | `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE` sin contexto suficiente | Mapping no resuelto; preservar legacy y bloquear conclusión favorable dependiente |
| VM-09 | Legacy desconocido o versión de productor incompatible | Revisión/validación fallida; nunca favorable por defecto |
| VM-10 | `ESTIMABLE_WITH_LIMITATIONS` sin enforcement en consumidor | No permitir conclusión limitada favorable; REVIEW/ABSTAIN según causa |
| VM-11 | Valoración `ESTIMABLE` y `VETO_ACTIVE` aplicable | Veto permanece; no se habilita selección ni ruta |
| VM-12 | Histórico legacy sin evidencia suficiente para reconstruir mapping | Conservar registro original; mapping no resuelto |

## 9. Owners y aprobaciones requeridas

| Dominio | Responsabilidad de revisión | Aprobación requerida |
|---|---|---|
| Valuation | Definiciones, métodos, suficiencia de modelo y outputs | Sí |
| Risk | Materialidad, vetos, interacción con riesgo y abstención | Sí |
| Legal/Compliance | Derechos, claims, restricciones y tratamiento jurisdiccional | Sí cuando sea material |
| Architecture/Data | Contratos, versionado, persistencia, consumidores y compatibilidad | Sí |
| Requirement Owners | Impacto en IIM y trazabilidad normativa | Sí antes de cualquier cambio IIM |
| Security/Operations | Enforcement, permisos, rollout, rollback y audit trail | Sí antes de operación |

Los nombres de responsables, delegaciones y firmas no están definidos en este draft. No se presume que el autor del documento sea aprobador.

## 10. Decisiones pendientes

1. ¿El catálogo canónico de aplicabilidad será exactamente `APPLICABLE/NOT_APPLICABLE/UNDETERMINED`, o se requiere un objeto separado de condición pendiente?
2. ¿Cómo se determina que el conjunto de métodos relevantes está completo para permitir una conclusión global de `NOT_ESTIMABLE`?
3. ¿Qué finalidad permite continuar sin valoración y qué finalidad exige un ValuationStatus específico?
4. ¿Qué consumidores legacy existen y dónde persisten los enums?
5. ¿Qué periodo de compatibilidad/deprecación y estrategia de rollback se aprobarán?
6. ¿Qué mappings, si alguno, podrán automatizarse después de validación?
7. ¿Qué política gobierna históricos incompletos y resultados no reconstruibles?

## 11. Trazabilidad y relación con X-09

- Resuelve documentalmente el GAP de vocabulario identificado en X-09 A11, sin cerrar formalmente A11.
- Alimenta la revisión de `docs/methodology/17-x09-wrapped-valuation-and-atomic-aggregation.md` §2.3.1.
- Requiere reconciliar `docs/methodology/03-canonical-decision-state-model.md` §4.7 y §9, especialmente el crosswalk legacy parcial de §§9.
- Requiere revisar `docs/valuation/valuation-method-catalog.md` §6 y `docs/valuation/valuation-applicability-matrix.md` §§1–3 tras aprobación.
- X-06 §15, pregunta 8, mantiene la necesidad de una matriz de migración versionada.

## 12. Estado y límites

**Estado:** propuesta documental v0.1.0-draft. No aprobada, no implementada, no validada.

No se han modificado el catálogo fuente, la matriz de aplicabilidad, schemas, código, requisitos IIM ni datos históricos. Los casos VM-01..VM-12 no son tests ejecutables y no se han ejecutado. El cierre requiere revisión especializada, resolución de decisiones pendientes, aprobación de owners, inventario de consumidores y evidencia de compatibilidad/migración.
