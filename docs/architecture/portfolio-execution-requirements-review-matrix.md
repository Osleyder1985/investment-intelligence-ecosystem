# Matriz de revisión individual — requisitos Portfolio & Execution

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado general:** PENDING_REVIEW  
**Baseline:** propuesta `portfolio-execution-requirements-proposal.md`  
**Autoridad:** Issue #34  
**Naturaleza:** artefacto de revisión; no es registro normativo aprobado.

> Los IDs IIM-025 a IIM-037 son provisionales. Cada fila requiere decisión explícita. La presencia de criterios de aceptación no significa que el requisito esté aprobado, especificado o implementado.

## 1. Estados permitidos para la revisión

- **PENDING_REVIEW:** aún no evaluado por revisor autorizado.
- **ACCEPTED_FOR_REGISTER:** aceptado para incorporarse al registro; no implica diseño técnico ni implementación.
- **ACCEPTED_WITH_ACTIONS:** aceptado condicionado a acciones identificadas y con responsable/fecha.
- **REVISE:** requiere reformulación, división o aclaración antes de decidir.
- **MERGE:** debe fusionarse con otro requisito; registrar destino y preservar trazabilidad.
- **REJECTED:** no se incorpora; registrar justificación.
- **DEFERRED:** válido, pero fuera del alcance de la siguiente fase; registrar condición de reentrada.

No usar APPROVED como sinónimo de IMPLEMENTED, TESTED o EMPIRICALLY_VALIDATED.

## 2. Matriz de decisiones y criterios individuales

| ID | Dominio | Criterios mínimos para aceptar el requisito | Dependencias principales | Estado |
|---|---|---|---|---|
| IIM-025 | Mandato de cartera | Define propietario/autoridad, objetivo y horizonte, moneda base, universo elegible, benchmark si aplica, restricciones, liquidez requerida, límites, versión y vigencia. Cada cartera activa debe referenciar un mandato vigente; cambios quedan versionados y auditables. | IIM-001, 002, 019, 020, 023 | PENDING_REVIEW |
| IIM-026 | Construcción y asignación | Define entradas permitidas, política/método, restricciones, tratamiento de activos no elegibles y datos faltantes, factibilidad, costes, explicación y reproducibilidad. Produce propuesta, no autorización ni orden. Debe soportar abstención si los datos o restricciones no permiten una solución válida. | IIM-005, 006, 013, 016, 017, 022, IIM-025, 027–030 | PENDING_REVIEW |
| IIM-027 | Exposición y concentración | Define taxonomía, jerarquías y reglas de agregación; evita doble conteo de wrappers/representaciones; documenta look-through, exposición indirecta, cobertura y desconocidos. Debe poder reproducir exposición a partir de posiciones y mappings versionados. | IIM-001, 002, 018, 019, 020, IIM-025 | PENDING_REVIEW |
| IIM-028 | Presupuestos y límites de riesgo | Distingue límites duros de métricas informativas; define ámbito, unidad, fuente, vigencia y política ante dato ausente/desactualizado. Evalúa pre y post asignación y bloquea violaciones duras sin compensación por score. Umbrales concretos requieren aprobación de gobernanza. | IIM-016, 017, 022, IIM-025, 026, 027, 030 | PENDING_REVIEW |
| IIM-029 | Liquidez y coste de ejecución | Especifica fuentes y timestamp, instrumento/venue, tamaño, horizonte, profundidad, spread, comisiones, slippage, impacto y supuestos. Expone incertidumbre, cobertura y capacidad; no extrapola silenciosamente fuera del rango soportado. | IIM-002, 018, 019, 020, IIM-027, 032 | PENDING_REVIEW |
| IIM-030 | Escenarios y estrés | Define catálogo versionado de escenarios, shocks y supuestos; documenta dependencias/correlaciones y limitaciones. Resultados reproducibles por cartera y escenario, con pérdidas/impactos y factores explicativos. No presenta escenarios como predicciones probabilísticas si no están calibrados. | IIM-007, 015, 016, 018, IIM-027–029 | PENDING_REVIEW |
| IIM-031 | Rebalanceo | Define triggers, bandas o calendario parametrizados, restricciones, costes, turnover, prioridad y excepciones. Genera plan propuesto con comparación antes/después y motivo; requiere autorización separada. | IIM-025–030, IIM-032 | PENDING_REVIEW |
| IIM-032 | Venue y contraparte | Define evaluación por entidad, jurisdicción, producto, mercado y operación; considera custodia, liquidez, solvencia/operación, incidentes, restricciones y vigencia de evidencia. Un estado desconocido o vencido no equivale a aprobado. | IIM-002, 018, 019, 020, IIM-029, IIM-035 | PENDING_REVIEW |
| IIM-033 | Ciclo de vida de órdenes | Define máquina de estados, identificadores idempotentes, eventos fuera de orden, parciales, cancelación/rechazo/expiración, reintentos y reconciliación. Cada orden se vincula a propuesta y autorización; ningún estado implica fill sin confirmación de fuente. | IIM-008, 009, 023, 024, IIM-025–029, 031, 032, 034–037 | PENDING_REVIEW |
| IIM-034 | Controles pre/post-trade | Define validaciones, límites, secuencia de controles, bloqueos, alertas, kill switch, recuperación y pruebas de fallo. La parada debe impedir nuevas órdenes y definir tratamiento seguro de órdenes abiertas; su autoridad y procedimiento de reanudación deben quedar explícitos. | IIM-016, 017, 022, IIM-028, 029, 032, 033, 035–037 | PENDING_REVIEW |
| IIM-035 | Seguridad, secretos y custodia | Define identidad, mínimo privilegio, segregación de funciones, ciclo de vida de secretos, rotación/revocación, auditoría, respuesta a incidentes y prohibición de secretos en código/logs. La firma/custodia de activos requiere revisión de seguridad especializada y diseño separado. | IIM-009, 018, 023, IIM-032–034, 037 | PENDING_REVIEW |
| IIM-036 | Conciliación | Define fuentes de verdad, frecuencia/trigger, tolerancias aprobadas, tratamiento de parciales, comisiones, movimientos, reorgs y discrepancias. Toda diferencia se conserva, clasifica y resuelve con evidencia; no se corrige silenciosamente el estado contable. | IIM-008, 018, 023, 024, IIM-032, 033, 035 | PENDING_REVIEW |
| IIM-037 | Aislamiento de entornos y promoción | Define separación técnica de endpoints, credenciales, datos, permisos y configuración para backtest, simulación, paper y real. Real permanece deshabilitado por defecto; promoción requiere gates independientes, aprobación explícita, pruebas, seguridad, cumplimiento y rollback. | IIM-007, 009, 015, 018, 023, 024, IIM-032–036 | PENDING_REVIEW |

## 2.1. Revisión técnica del lote A

La revisión preliminar de IIM-025, IIM-027 e IIM-028 está documentada en [`portfolio-execution-lot-a-review.md`](portfolio-execution-lot-a-review.md) y se sigue en el [Issue #35](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/35). La recomendación técnica es REVISE para los tres requisitos. Sus estados formales permanecen PENDING_REVIEW hasta que se registre una decisión autorizada en el Issue #34.

## 2.2. Revisión técnica del lote B

La revisión preliminar de IIM-026, IIM-029, IIM-030 e IIM-031 está documentada en [`portfolio-execution-lot-b-review.md`](portfolio-execution-lot-b-review.md) y se sigue en el [Issue #36](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/36). La recomendación técnica es REVISE para los cuatro requisitos. Sus estados formales permanecen PENDING_REVIEW hasta que se registre una decisión autorizada en el Issue #34.

## 2.3. Revisión técnica del lote C

La revisión preliminar de IIM-032, IIM-035 e IIM-037 está documentada en [`portfolio-execution-lot-c-review.md`](portfolio-execution-lot-c-review.md) y se sigue en el [Issue #37](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/37). La recomendación técnica es REVISE para los tres requisitos. Sus estados formales permanecen PENDING_REVIEW hasta decisión autorizada en el Issue #34.


## 2.4. Revisión técnica del lote D

La revisión preliminar de IIM-033, IIM-034 e IIM-036 está documentada en [`portfolio-execution-lot-d-review.md`](portfolio-execution-lot-d-review.md) y se sigue en el [Issue #38](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/38). La recomendación técnica es REVISE para los tres requisitos. Los estados formales permanecen PENDING_REVIEW hasta decisión autorizada en el Issue #34.

## 3. Riesgos de solapamiento que deben resolverse

1. **IIM-016 vs IIM-028:** IIM-016 produce evaluación de riesgo del activo/oportunidad; IIM-028 gobierna límites y presupuestos agregados de cartera. Evitar duplicar métricas o confundir perfil con política de riesgo.
2. **IIM-029 vs IIM-033:** IIM-029 estima coste/capacidad; IIM-033 gestiona eventos y estados reales de órdenes. La estimación no prueba ejecución.
3. **IIM-032 vs IIM-035:** IIM-032 aprueba elegibilidad de venue/contraparte; IIM-035 gobierna seguridad, permisos, secretos y custodia técnica.
4. **IIM-033 vs IIM-034:** IIM-033 modela ciclo de vida; IIM-034 decide si una acción puede ejecutarse y puede detener nuevas operaciones.
5. **IIM-036 vs IIM-008/IIM-023:** IIM-036 concilia estados económicos/operativos; IIM-008/IIM-023 conservan historial y auditoría. No son intercambiables.
6. **IIM-037 vs IIM-007/IIM-015:** IIM-037 separa entornos y promoción; IIM-007/IIM-015 validan modelos/procesos y controlan su ciclo temporal.

## 4. Reglas para la decisión individual

Para cada requisito, el revisor debe registrar:
- ID y versión exacta revisada;
- decisión de la lista de estados;
- revisor y competencia/rol;
- fecha;
- razonamiento y contraargumentos;
- solapamientos detectados y resolución;
- cambios requeridos y responsable;
- dependencias bloqueantes;
- evidencia que permita auditar la decisión.

No se considerará aceptación suficiente un comentario genérico como "LGTM". La decisión debe referirse a los criterios específicos del requisito.

## 5. Orden recomendado de revisión

**Lote A — semántica de cartera:** IIM-025, IIM-027, IIM-028.  
**Lote B — propuesta y factibilidad:** IIM-026, IIM-029, IIM-030, IIM-031.  
**Lote C — perímetro de operación:** IIM-032, IIM-035, IIM-037.  
**Lote D — órdenes y control:** IIM-033, IIM-034, IIM-036.

No iniciar contratos de ejecución real mientras los lotes C y D no tengan decisiones formales y revisión de seguridad/legal aplicable.

## 6. Decisión global

**PENDING_REVIEW.** La matriz sirve como instrumento de revisión. No incorpora los requisitos al registro oficial, no modifica los estados de IIM-001 a IIM-024, no declara contratos SPECIFIED y no habilita operación real.
