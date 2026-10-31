# X-09 — Resolución de precedencia agregada A12

**ID:** IIE-METH-AGG-PRECEDENCE-012  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Ámbito:** Precedencia determinista de outcomes analíticos por unidad atómica y resumen del requested_scope.  
**Dependencias:** X-06 §4, §8–10; X-09 A12; documentos 14, 16 y 17.  
**No alcance:** No modifica requisitos IIM, schemas, código, autorización de trading ni políticas institucionales.

## 1. Dictamen y problema resuelto

Los borradores anteriores definen precedencia por unidad atómica, pero dejan ambigua la precedencia del resumen cuando distintas unidades producen simultáneamente `DECISION_INELIGIBLE`, `DECISION_REVIEW_REQUIRED` y `DECISION_ABSTAIN`. El documento 17 §3.5 remite a una aprobación futura de owners, y X-06 §10 contiene reglas que no fijan un orden total inequívoco entre esos resultados.

Esta propuesta establece una regla determinista para revisión:

**`DECISION_INELIGIBLE` > `DECISION_REVIEW_REQUIRED` > `DECISION_ABSTAIN` > estados no concluyentes > resultados favorables.**

La regla solo se aplica después de validar que la partición del scope es íntegra, exhaustiva, disjunta y semánticamente compatible. El orden no es un score ni una medida de severidad numérica. Expresa la precedencia de conclusión para el requested_scope completo: una inelegibilidad demostrada en cualquier unidad solicitada impide declarar elegible el scope completo, aunque otras unidades estén en revisión o abstención.

El vector de resultados atómicos y todas las causas se conservan. El resumen nunca sustituye ni borra el detalle.

## 2. Separación de dos capas: validación y decisión

### 2.1 ValidationEnvelope es una precondición, no un outcome

Antes de agregar outcomes se valida:

- integridad del snapshot y de cada input;
- compatibilidad de schemas, versiones, policy y decision_type;
- identidad de la unidad y relación de scopes;
- partición exacta, disjunta y exhaustiva;
- cobertura de dependencias y ausencia de pérdida de causas.

| ValidationEnvelope | Consecuencia |
|---|---|
| `REJECTED_INVALID_INPUT` | Rechazar composición; no emitir DecisionOutcome agregado |
| `REVIEW_REQUIRED` | No emitir DecisionOutcome agregado; requiere resolver contradicción, scope o compatibilidad |
| `INCOMPLETE` | No emitir DecisionOutcome agregado; cobertura o partición incompleta |
| `VALID` | Puede ejecutarse agregación determinista de outcomes atómicos |

No se permite resolver un envelope no válido eligiendo el outcome aparentemente más restrictivo o favorable. Primero se repara la integridad/completitud de la composición. Los outcomes atómicos ya válidos pueden conservarse como evidencia local, pero no justifican una conclusión sobre el requested_scope incompleto.

### 2.2 DecisionOutcome es una conclusión por unidad y por scope agregado

La composición por unidad sigue X-06 §9. Esta propuesta no cambia la precedencia interna de una unidad. Solo define cómo resumir el vector de unidades homogéneas y compatibles cuando `ValidationEnvelope=VALID`.

## 3. Regla de agregación determinista

### 3.1 Orden propuesto

Una vez que el envelope es VALID y todos los átomos requeridos tienen un DecisionOutcome compatible:

1. Si una o más unidades son `DECISION_INELIGIBLE`, el outcome agregado del requested_scope es `DECISION_INELIGIBLE`.
2. En ausencia de INELIGIBLE, si una o más unidades son `DECISION_REVIEW_REQUIRED`, el agregado es `DECISION_REVIEW_REQUIRED`.
3. En ausencia de INELIGIBLE y REVIEW_REQUIRED, si una o más unidades son `DECISION_ABSTAIN`, el agregado es `DECISION_ABSTAIN`.
4. En ausencia de los anteriores, si alguna unidad está `DECISION_IN_PROGRESS`, el agregado es `DECISION_IN_PROGRESS`.
5. En ausencia de los anteriores, si alguna unidad está `DECISION_NOT_ASSESSED`, el agregado es `DECISION_NOT_ASSESSED`.
6. Si todas las unidades son `DECISION_ELIGIBLE` o `DECISION_ELIGIBLE_WITH_LIMITATIONS` y al menos una es limitada, el agregado es `DECISION_ELIGIBLE_WITH_LIMITATIONS`.
7. Solo si todas las unidades son `DECISION_ELIGIBLE`, el agregado es `DECISION_ELIGIBLE`.

La condición “en ausencia de” hace que el orden sea inequívoco y no dependa del orden de llegada de los registros.

### 3.2 Justificación de INELIGIBLE > REVIEW > ABSTAIN

- **INELIGIBLE** requiere evidencia afirmativa suficiente de incumplimiento obligatorio o veto activo aplicable. No se deriva de ausencia de evidencia.
- **REVIEW_REQUIRED** representa conflicto o adjudicación material pendiente; no borra una inelegibilidad independiente ya demostrada en otra unidad.
- **ABSTAIN** representa incapacidad de concluir por falta de evidencia suficiente/vigente/admisible; tampoco borra un incumplimiento afirmativo demostrado en otra unidad.

Por tanto, si una unidad está INELIGIBLE y otra REVIEW_REQUIRED/ABSTAIN, el requested_scope completo no es elegible por un hecho demostrado. El resumen primario es INELIGIBLE y REVIEW/ABSTAIN permanecen como causas/estados secundarios por unidad. La revisión pendiente puede impedir operaciones posteriores o requerir adjudicación independiente, pero no convierte en elegible el scope completo.

Si no existe unidad INELIGIBLE, REVIEW_REQUIRED prevalece sobre ABSTAIN como resumen porque hay un conflicto adjudicable que debe resolverse antes de considerar una abstención por falta de datos como explicación principal. Las causas de abstención permanecen visibles.

### 3.3 No confundir resumen del scope con subset permitido

El outcome agregado siempre describe exactamente el `requested_scope` original.

Si la política permite continuar solo con un subset, ese subset requiere:
- un nuevo `derived_scope_id` explícito, contenido dentro del requested_scope original;
- nueva evaluación/composición para ese scope derivado;
- identificación del conjunto excluido y de las causas;
- enforcement verificable y probado por el consumidor;
- vínculo de provenance con la evaluación original.

No se puede reemplazar el outcome del requested_scope original por `DECISION_ELIGIBLE_WITH_LIMITATIONS` solo porque una parte sea elegible. La elegibilidad del subset es una decisión separada. Si no puede aislarse o hacerse cumplir, no se emite decisión favorable para el subset.

## 4. Tabla de combinación mínima

Supuesto: todas las unidades pertenecen al mismo decision_type, finalidad, policy version y requested_scope compatible; ValidationEnvelope=VALID.

| Outcomes atómicos presentes | Outcome agregado | Condición adicional |
|---|---|---|
| Solo ELIGIBLE | ELIGIBLE | Todas las unidades cubiertas y elegibles |
| ELIGIBLE + ELIGIBLE_WITH_LIMITATIONS | ELIGIBLE_WITH_LIMITATIONS | Restricciones exigibles |
| INELIGIBLE + ELIGIBLE | INELIGIBLE | Conservar unidad favorable local |
| INELIGIBLE + ABSTAIN | INELIGIBLE | Abstención se conserva por unidad |
| INELIGIBLE + REVIEW_REQUIRED | INELIGIBLE | Review se conserva; puede bloquear acciones posteriores |
| INELIGIBLE + REVIEW_REQUIRED + ABSTAIN | INELIGIBLE | Conservar las tres categorías y todas las causas |
| REVIEW_REQUIRED + ELIGIBLE | REVIEW_REQUIRED | Unidad favorable no resuelve revisión ajena |
| REVIEW_REQUIRED + ABSTAIN | REVIEW_REQUIRED | Abstención se conserva |
| ABSTAIN + ELIGIBLE | ABSTAIN | No declarar elegible el requested_scope |
| IN_PROGRESS + ELIGIBLE | IN_PROGRESS | No hay conclusión completa |
| NOT_ASSESSED + ELIGIBLE | NOT_ASSESSED | No hay conclusión completa |
| REVIEW_REQUIRED + IN_PROGRESS + ABSTAIN | REVIEW_REQUIRED | No se pierde ningún estado local |

Si ValidationEnvelope no es VALID, esta tabla no se ejecuta y no existe outcome agregado.

## 5. Algoritmo de referencia

Pseudocódigo normativo candidato para discusión:

```text
aggregate(requested_scope, atomic_results, validation_envelope):
    if validation_envelope == REJECTED_INVALID_INPUT:
        return (no_aggregate_outcome, REJECTED_INVALID_INPUT)
    if validation_envelope == REVIEW_REQUIRED:
        return (no_aggregate_outcome, REVIEW_REQUIRED)
    if validation_envelope == INCOMPLETE:
        return (no_aggregate_outcome, INCOMPLETE)
    require validation_envelope == VALID
    require exact_partition(requested_scope, atomic_results)
    require homogeneous_decision_context(atomic_results)

    if any(result == DECISION_INELIGIBLE):
        outcome = DECISION_INELIGIBLE
    else if any(result == DECISION_REVIEW_REQUIRED):
        outcome = DECISION_REVIEW_REQUIRED
    else if any(result == DECISION_ABSTAIN):
        outcome = DECISION_ABSTAIN
    else if any(result == DECISION_IN_PROGRESS):
        outcome = DECISION_IN_PROGRESS
    else if any(result == DECISION_NOT_ASSESSED):
        outcome = DECISION_NOT_ASSESSED
    else if all(result in {
        DECISION_ELIGIBLE,
        DECISION_ELIGIBLE_WITH_LIMITATIONS
    }) and any(result == DECISION_ELIGIBLE_WITH_LIMITATIONS):
        outcome = DECISION_ELIGIBLE_WITH_LIMITATIONS
    else:
        require all(result == DECISION_ELIGIBLE)
        outcome = DECISION_ELIGIBLE

    return (outcome, full_atomic_vector, all_causes)
```

Este pseudocódigo no define el contrato de datos ni autoriza implementación. Las condiciones `exact_partition` y `homogeneous_decision_context` deben tener especificación y pruebas propias.

## 6. Invariantes propuestas

- **AGG-INV-01 — No favorable masking:** ninguna unidad no favorable puede ocultarse tras unidades elegibles.
- **AGG-INV-02 — Proven failure dominance:** INELIGIBLE demostrado prevalece sobre REVIEW/ABSTAIN en el resumen del requested_scope.
- **AGG-INV-03 — Review over abstain:** sin INELIGIBLE, REVIEW_REQUIRED prevalece sobre ABSTAIN en el resumen.
- **AGG-INV-04 — Cause conservation:** todas las causas y outcomes atómicos se conservan en el vector.
- **AGG-INV-05 — Envelope gate:** envelope no VALID impide emitir DecisionOutcome agregado.
- **AGG-INV-06 — Exact scope:** el agregado se refiere exactamente al requested_scope declarado.
- **AGG-INV-07 — Subset non-substitution:** un resultado favorable de subset nunca sustituye el resultado del requested_scope original.
- **AGG-INV-08 — Order independence:** permutar el orden de llegada de átomos no cambia el resultado.
- **AGG-INV-09 — No severity scoring:** la precedencia es regla categórica versionada, no suma ni promedio de scores.
- **AGG-INV-10 — Context homogeneity:** no agregar decision_type, finalidad, policy version o contextos incompatibles.
- **AGG-INV-11 — Route separation:** ORE se resume por route_id; no se importa la precedencia analítica como autorización de ruta.
- **AGG-INV-12 — No implicit permission:** un resumen favorable solo se emite con cobertura completa y validación íntegra.

## 7. Escenarios de aceptación propuestos

Los escenarios siguientes amplían X-09; no son tests ejecutables ni pruebas realizadas.

| ID | Vector / condición | Expected outcome |
|---|---|---|
| AGG-S-01 | INELIGIBLE + REVIEW_REQUIRED | INELIGIBLE; review y causas preservados |
| AGG-S-02 | INELIGIBLE + ABSTAIN | INELIGIBLE; abstain y causas preservados |
| AGG-S-03 | INELIGIBLE + REVIEW_REQUIRED + ABSTAIN | INELIGIBLE; vector completo conservado |
| AGG-S-04 | REVIEW_REQUIRED + ABSTAIN, sin INELIGIBLE | REVIEW_REQUIRED |
| AGG-S-05 | ABSTAIN + ELIGIBLE | ABSTAIN |
| AGG-S-06 | IN_PROGRESS + ELIGIBLE, sin outcomes superiores | IN_PROGRESS |
| AGG-S-07 | NOT_ASSESSED + ELIGIBLE, sin outcomes superiores | NOT_ASSESSED |
| AGG-S-08 | ELIGIBLE + ELIGIBLE_WITH_LIMITATIONS | ELIGIBLE_WITH_LIMITATIONS, solo con enforcement |
| AGG-S-09 | Una unidad INELIGIBLE; se solicita subset independiente elegible | Original INELIGIBLE; subset exige nuevo scope/decision_id |
| AGG-S-10 | ValidationEnvelope=INCOMPLETE y un átomo INELIGIBLE | Sin agregado; conservar átomo local; no afirmar cobertura total |
| AGG-S-11 | ValidationEnvelope=REVIEW_REQUIRED por partición ambigua | Sin agregado; resolver partición |
| AGG-S-12 | Misma colección de outcomes en orden de llegada distinto | Resultado idéntico |
| AGG-S-13 | Unidades de policy_version incompatible | Envelope REVIEW_REQUIRED/REJECTED según causa; sin agregado |
| AGG-S-14 | Ruta A ROUTE_INELIGIBLE y ruta B ROUTE_ELIGIBLE | Matriz ORE por route_id; no permiso global |

## 8. Interfaz con X-06 y X-09

1. X-06 §9 continúa determinando cada outcome atómico.
2. Esta propuesta precisa X-06 §10 para el resumen del requested_scope.
3. El documento 17 §3.5 debe dejar de usar una nota de precedencia abierta y enlazar esta regla propuesta.
4. X-09 matriz y trazabilidad deben añadir escenarios e invariantes futuros.
5. ORE permanece por ruta y requiere política propia de agregación operativa; esta propuesta no la redefine.
6. La regla solo podrá considerarse aprobada tras revisión de Risk, Architecture, Operations, Security, Governance y Requirement Owners.

## 9. Decisiones y límites pendientes

- Confirmar institucionalmente que INELIGIBLE > REVIEW_REQUIRED > ABSTAIN es el orden aceptado para el resumen de requested_scope.
- Definir taxonomía exacta de ValidationEnvelope y condición de emisión/no emisión de outcome agregado.
- Aprobar scope algebra, subset policy y enforcement contract.
- Determinar si estados IN_PROGRESS/NOT_ASSESSED deben agregarse exactamente en este orden para todos los decision purposes.
- Definir precedencia independiente de ORE por route_id.
- Mapear escenarios a IIM por Requirement Owners; no inferir cumplimiento por trazabilidad documental.

## 10. Estado

**ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Se resuelve la ambigüedad en el nivel de propuesta documental y se ofrece un algoritmo determinista revisable. No se modifican requisitos IIM, schemas ni código. No se han creado fixtures, tests ejecutables, property-based tests ni se ha ejecutado ninguna prueba. La publicación no constituye aprobación institucional ni habilita operaciones de inversión.
