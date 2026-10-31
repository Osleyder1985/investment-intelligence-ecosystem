# Gates de decisión y estados de salida

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Propósito y alcance

Este protocolo transforma resultados de identidad, elegibilidad, calidad de datos, valoración, infravaloración y riesgos en una conclusión informativa reproducible. No decide por el usuario, no ejecuta órdenes y no garantiza resultados.

La decisión se emite por combinación de activo, representación/mercado, finalidad, jurisdicción cuando corresponda, timestamp UTC y horizonte. Un mismo activo puede tener resultados distintos por red, contrato, mercado, tamaño de operación, jurisdicción u horizonte.

## 2. Orden obligatorio de evaluación

1. **Identidad:** resolver activo canónico, red, contrato/representación y relación con wrappers, bridges o migraciones.
2. **Elegibilidad por finalidad:** determinar si el activo puede investigarse, valorarse, compararse u operarse en el contexto declarado.
3. **Admisibilidad de evidencia:** verificar que los datos críticos son trazables, point-in-time y admisibles para el uso.
4. **Vetos y riesgos críticos:** evaluar vetos aplicables al activo, mercado, jurisdicción, custodio y operación.
5. **Aplicabilidad de valoración:** comprobar que existe método defendible para el objeto económico y que el valor puede atribuirse al token/derecho.
6. **Clasificación de infravaloración:** comparar precio de referencia ejecutable con rango de valor defendible, incorporando incertidumbre y costes.
7. **Comparación de oportunidad:** comparar dimensiones compatibles y solo dentro del mismo universo, finalidad y horizonte.
8. **Salida y registro:** producir estado, razones, limitaciones, evidencia, versión y condiciones de revisión.

Un gate posterior nunca anula un bloqueo anterior.

## 3. Estados de gates

| Gate | Estados mínimos | Efecto |
|---|---|---|
| Identidad | RESOLVED / UNRESOLVED / CONFLICTED | UNRESOLVED o CONFLICTED bloquea valoración y ranking dependientes |
| Elegibilidad | ELIGIBLE / INELIGIBLE / CONDITIONAL / UNKNOWN | INELIGIBLE bloquea la finalidad; UNKNOWN no presume elegibilidad |
| Datos críticos | ADMISSIBLE / ADMISSIBLE_WITH_LIMITATIONS / QUARANTINED / REJECTED / UNKNOWN | Datos críticos no admisibles impiden conclusiones que dependan de ellos |
| Riesgo por dominio (RiskDomainStatus) | ASSESSED_WITH_FINDINGS / ASSESSED_NO_MATERIAL_FINDING / UNKNOWN / NOT_ASSESSED / STALE / CONFLICTED / INVALID | UNKNOWN y estados no favorables bloquean conclusiones dependientes |
| Decisión de veto (VetoDecision) | VETO_ACTIVE / VETO_REVIEW_REQUIRED / VETO_NOT_ASSESSED / NO_VETO_IDENTIFIED | Aplicar reglas de la sección 4; separado de RiskDomainStatus |
| Valoración | ESTIMABLE / CONDITIONAL / NOT_APPLICABLE / INSUFFICIENT_EVIDENCE | Sin rango defendible no se afirma descuento cuantificado |
| Infravaloración | SUPPORTED_BY_MODEL / CONDITIONAL / NO_SUFFICIENT_EVIDENCE / NOT_ESTIMABLE / WITHIN_RANGE / OVERVALUED_BY_MODEL | Salida del modelo, no recomendación autónoma |

Los estados no se colapsan en un booleano ni en una puntuación única. Un dato ausente es UNKNOWN, no cero ni favorable.

## 4. Tratamiento obligatorio de vetos

- **VETO_ACTIVE:** bloquea toda clasificación favorable dentro del alcance del veto. No puede compensarse con descuento, momentum, upside modelado, score ni posición en ranking. La salida será NOT_ELIGIBLE_DUE_TO_ACTIVE_VETO.
- **VETO_REVIEW_REQUIRED:** no es veto confirmado ni ausencia de riesgo. La salida será REVIEW_REQUIRED; se bloquea la clasificación favorable hasta resolver la revisión o limitar justificadamente su alcance.
- **RISK_UNKNOWN:** ausencia de evidencia no demuestra seguridad. La salida será RISK_ASSESSMENT_INCOMPLETE; nunca se convierte en NO_VETO_IDENTIFIED.
- **NO_VETO_IDENTIFIED:** significa únicamente que no se identificó veto con la evidencia y alcance revisados. No equivale a activo seguro ni libre de riesgo.

Un veto debe acotarse a su objeto: activo, representación, red, mercado, jurisdicción, custodio, contraparte, finalidad y/o tamaño de operación. No generalizarlo más allá de la evidencia.

Retirar, caducar o considerar remediado un veto exige evidencia verificable, revisión registrada y una nueva versión de evaluación. Un anuncio del proyecto por sí solo no prueba remediación.

## 5. Abstención y bloqueo

Abstenerse de emitir conclusión favorable o cuantificada cuando exista:
- identidad no resuelta o conflictiva;
- elegibilidad desconocida o incumplida para la finalidad;
- dato crítico rechazado, en cuarentena o sin procedencia admisible;
- VETO_ACTIVE o VETO_REVIEW_REQUIRED aplicable;
- RISK_UNKNOWN material para la tesis;
- derechos/captura de valor no demostrados para el modelo;
- valoración no estimable o modelos incompatibles sin reconciliación;
- precio de referencia, unidad, moneda, mercado o timestamp no comparables;
- costes, liquidez o restricciones de ejecución desconocidos cuando alteren materialmente la conclusión;
- falta de evidencia para discriminar escenarios relevantes.

La abstención indica qué conclusión queda bloqueada, qué sí puede afirmarse, qué falta y qué evidencia permitiría reanudar.

## 6. Estados de decisión informativa

| Estado | Significado |
|---|---|
| BLOCKED_IDENTITY | Identidad no resuelta o conflictiva |
| INELIGIBLE_FOR_SCOPE | No cumple elegibilidad para la finalidad concreta |
| BLOCKED_DATA_QUALITY | Evidencia crítica no admisible o insuficiente |
| NOT_ELIGIBLE_DUE_TO_ACTIVE_VETO | Veto activo dentro del alcance |
| REVIEW_REQUIRED | Revisión pendiente antes de conclusión favorable |
| RISK_ASSESSMENT_INCOMPLETE | Riesgo material desconocido |
| NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE | No existe valoración defendible con evidencia actual |
| NO_COMPARABLE_OPPORTUNITY | No se puede comparar válidamente con el universo elegido |
| CONDITIONAL_CANDIDATE | Puede continuar como candidato con condiciones explícitas |
| OPPORTUNITY_SUPPORTED_BY_EVIDENCE | Evidencia respalda la tesis en el modelo, horizonte y alcance declarados; no es orden de compra |
| NO_OPPORTUNITY_SUPPORTED | Evidencia disponible no respalda una oportunidad bajo el protocolo |
| INSUFFICIENT_EVIDENCE | Información insuficiente para clasificar responsablemente |

Los estados deben ser mutuamente excluyentes por combinación de activo/mercado/finalidad/horizonte y versión. Si coexisten causas, registrar causa primaria y todas las restricciones adicionales.

OPPORTUNITY_SUPPORTED_BY_EVIDENCE no significa rentabilidad futura demostrada y no puede emitirse con gates bloqueantes pendientes.

## 7. Decisiones por horizonte

Emitir evaluaciones independientes para corto, medio y largo plazo. La definición temporal de cada horizonte debe declararse y justificarse por tesis y frecuencia de datos; esta política no fija duraciones universales.

Reevaluar en cada horizonte catalizadores, calendario, estructura de mercado, liquidez, oferta, emisiones, unlocks, derechos, captura de valor, riesgos, escenarios, costes y capacidad de ejecución. No trasladar automáticamente una conclusión entre horizontes.

## 8. Registro mínimo

Guardar decision_id, canonical_asset_id, representation_id, market_id, purpose, jurisdiction/scope, horizon, evaluation_timestamp_utc, universe_snapshot_id, manifiesto de datos, estados de gates, referencias de veto, modelo/versión, estado de infravaloración, protocolo comparativo/versión, evidencias, supuestos, desconocidos, contraevidencia, estado final, justificación y condiciones de reevaluación.

Cada ejecución crea un registro inmutable. Una reevaluación crea otro registro enlazado; nunca sobrescribe la conclusión histórica.
