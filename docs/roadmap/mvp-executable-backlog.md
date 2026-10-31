# Backlog ejecutable del MVP de selección de criptoactivos IIE

**Identificador:** IIE-BACKLOG-MVP-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / PENDING_REVIEW / NOT_APPROVED  
**Plan padre:** [Plan Maestro de Implementación](master-methodology-implementation-plan.md)

> Este backlog descompone el Plan Maestro en paquetes de trabajo. No aprueba requisitos IIM ni afirma que el software, pruebas o validaciones estén completados.

## 1. Resultado del MVP

Construir una primera integración vertical, limitada y reproducible, que reciba un universo versionado de activos y produzca:
- identidad y representación del activo con estado de resolución;
- snapshot de datos con provenance, timestamps, calidad y cobertura;
- resultado de screening y elegibilidad por propósito;
- evaluación inicial de riesgos y vetos no compensatorios;
- clasificación explicable: EXCLUDED, INSUFFICIENT_EVIDENCE, RESEARCH_CANDIDATE, VALUATION_ELIGIBLE, INVESTMENT_CANDIDATE o ABSTAIN;
- expediente reproducible con razones, contraevidencia, incertidumbre, versiones y referencias.

INVESTMENT_CANDIDATE es una candidatura analítica condicionada a un mandato y no una orden, recomendación personalizada ni autorización de ejecución. UNDERVALUED_CANDIDATE queda fuera del primer corte hasta que un método de valoración por tipología haya sido aprobado, implementado y validado.

## 2. Lectura del estado actual

La inspección documental de main confirma:
- README y registro de requisitos declaran que la metodología está en desarrollo y no validada/certificada.
- IIM-001..024 permanecen documentados en borrador; no deben tratarse como requisitos formalmente aprobados.
- Issue #19 mantiene IIM-001, IIM-018, IIM-019 e IIM-020 en revisión y IIM-002 bloqueado para aprobación de fuentes concretas.
- Existe un harness offline de fixtures sintéticos EVM/Solana en tests/identity_fixtures/.
- Existe una suite preliminar de contratos JSON para Portfolio & Execution en tests/contracts/.
- requirements-dev.txt declara jsonschema>=4.18,<5.
- Estos harnesses no constituyen un motor integral de selección. Los tests no se declaran ejecutados en esta actualización.
- La matriz de trazabilidad existente afirma que ningún requisito se encuentra IMPLEMENTED, TESTED o EMPIRICALLY_VALIDATED.

La inspección es estática de los artefactos citados; no equivale a ejecución local ni a auditoría completa de cada commit.

## 3. Reglas de priorización

- P0: bloquea la integridad de los resultados del MVP.
- P1: necesario para la utilidad analítica y la decisión explicable.
- P2: expansión, endurecimiento o capacidad posterior.
- No iniciar tareas dependientes de fuentes/licencias no verificadas.
- No convertir criterios o umbrales provisionales en normativos sin aprobación.
- Cada paquete requiere owner, revisor, evidencia y aceptación antes de cerrarse.
- El código debe enlazar requisito, contrato, test y evidencia; documentación sola no cierra una tarea de implementación.

## 4. Épicas y paquetes de trabajo

### EPIC-00 — Gobierno del backlog y baseline
**Prioridad:** P0  
**Dependencias:** ninguna.

- MVP-001: revisar y aprobar el Plan Maestro y este backlog; registrar decisiones, owners, revisores y límites.
- MVP-002: derivar una matriz de trazabilidad Plan → requisito IIM → paquete → artefacto → prueba → evidencia.
- MVP-003: definir Definition of Ready/Done, control de cambios, branch protection y política de estados.
- MVP-004: determinar alcance inicial: familias de activos, redes, finalidad, horizonte de análisis y exclusiones. No inventar una cartera objetivo.

**Aceptación:** plan y backlog revisados; decisiones abiertas asignadas; alcance inicial explícito; ninguna modificación no autorizada al registro IIM.

### EPIC-01 — Cierre fundacional: identidad, universo y fuentes
**Prioridad:** P0  
**Dependencia:** EPIC-00.  
**Issue de coordinación:** #19.

- MVP-010: cerrar revisión semántica de IIM-001/IIM-019: identidad económica, representación, red, contrato, nativo, wrapper, bridge, proxy, migración, fork e instrumento/listing.
- MVP-011: cerrar criterios IIM-020: construcción y snapshot del universo, inclusión/exclusión, activos desaparecidos y prevención de survivorship bias.
- MVP-012: resolver IIM-018: calidad, admisibilidad, timestamps, freshness, revisiones, conflictos y linaje point-in-time.
- MVP-013: obtener expediente de permisos por fuente, producto y operación para IIM-002. Mientras no exista, fuente BLOQUEADA/UNKNOWN para uso afectado.
- MVP-014: completar perfiles de identidad priorizados y documentar los límites de redes no soportadas.
- MVP-015: ampliar fixtures por perfiles y relaciones; separar SYNTHETIC, CAPTURED y REDACTED_CAPTURE.
- MVP-016: establecer harness interoperable y reproducible; pruebas negativas para identidades ambiguas, conflictos, aliases, relaciones temporales y checkpoints ausentes.
- MVP-017: definir contrato versionado de Asset Registry y Universe Snapshot, sin fijar aún schema normativo antes de la aprobación semántica.

**Aceptación:** decisiones registradas por requisito; perfiles acotados y revisados; fuentes con permisos documentados o explícitamente bloqueadas; fixtures trazables; UNKNOWN no se convierte en PASS; snapshot de universo reproducible.

### EPIC-02 — Ingeniería base y calidad continua
**Prioridad:** P0  
**Dependencia:** EPIC-00; puede avanzar en paralelo con EPIC-01 sin codificar semántica pendiente.

- MVP-020: establecer estructura de paquete de aplicación y límites entre domain, application, adapters e infrastructure.
- MVP-021: fijar Python/runtime soportado, dependencias bloqueadas, gestión de configuración y estrategia de entornos.
- MVP-022: incorporar CI para instalación limpia, JSON Schema, unit tests y suites offline.
- MVP-023: añadir lint, type checking y controles de dependencias/secret scanning adecuados al stack aprobado.
- MVP-024: implementar logging estructurado, correlation/evaluation IDs y manejo explícito de errores.
- MVP-025: generar artefactos de prueba y reportes CI reproducibles; proteger main con checks obligatorios.
- MVP-026: definir política de secretos, permisos mínimos y separación dev/test/prod. No almacenar credenciales de trading en el MVP.

**Aceptación:** build limpio reproducible; CI ejecuta suites y falla ante regresiones; dependencias y configuración documentadas; ningún secreto en repositorio.

### EPIC-03 — Data contracts, provenance y snapshots
**Prioridad:** P0  
**Dependencias:** EPIC-01 y EPIC-02.

- MVP-030: especificar contratos de datos de entrada con fuente, licencia/permiso, campo, unidad, timestamp de evento, timestamp de observación, timestamp de registro y transformaciones.
- MVP-031: implementar estados de calidad/admisibilidad: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN y STALE.
- MVP-032: implementar validaciones de esquema, dominio, rango, integridad referencial, duplicados y consistencia temporal.
- MVP-033: crear snapshots inmutables del universo y datos con hash, versiones y manifest.
- MVP-034: gestionar conflictos entre fuentes, revisiones retrospectivas y late-arriving data sin sobrescritura silenciosa.
- MVP-035: implementar un adaptador inicial de datos autorizado, seleccionado solo después de revisión de permisos; incluir fixture/provider fake para tests.
- MVP-036: definir cobertura mínima por evaluación y reglas de abstención ante campos críticos faltantes.

**Aceptación:** cada valor usado tiene provenance y tiempo; snapshots reconstruibles; licencias/uso permitido verificables; datos desconocidos, stale o conflictivos bloquean o limitan decisiones dependientes.

### EPIC-04 — Screening, elegibilidad y riesgo
**Prioridad:** P0  
**Dependencias:** EPIC-01, EPIC-02 y EPIC-03; semántica aplicable aprobada.

- MVP-040: convertir criterios de screening aprobados en reglas declarativas versionadas.
- MVP-041: implementar elegibilidad por finalidad y separar elegibilidad analítica de operacional.
- MVP-042: implementar RiskAssessment, RiskDomainStatus y VetoDecision como entidades separadas.
- MVP-043: implementar catálogo de riesgos inicial y matriz de aplicabilidad por tipología.
- MVP-044: implementar veto no compensatorio, scope-aware, con reason codes y evidencia.
- MVP-045: integrar estados canónicos, precedencia y composición determinista (X-06), una vez revisados.
- MVP-046: aplicar freshness, invalidación y reevaluación (X-07).
- MVP-047: convertir escenarios prioritarios X-09 en pruebas ejecutables de unidad, propiedad, integración y adversariales.
- MVP-048: implementar ABSTAIN/INSUFFICIENT_EVIDENCE y prevenir la conversión de UNKNOWN/INVALID a PASS.

**Aceptación:** decisiones deterministas; vetos no compensables; resultados limitados por scope; evidencia y causas visibles; unknown/invalid/stale fail closed donde corresponda; tests automatizados pasan en CI.

### EPIC-05 — Inteligencia de activo: fundamentos, tokenomics, tecnología y mercado
**Prioridad:** P1  
**Dependencias:** EPIC-03 y EPIC-04.

- MVP-050: seleccionar una o dos tipologías iniciales justificadas por cobertura de datos y capacidad metodológica.
- MVP-051: implementar perfil de fundamentos con métricas definidas, fuente, unidad, periodo, metodología y limitaciones.
- MVP-052: implementar tokenomics: supply, emisiones, desbloqueos, dilución, concentración y derechos/captura de valor, según aplicabilidad.
- MVP-053: implementar perfil tecnológico/seguridad/gobernanza con evidencias y cobertura explícita.
- MVP-054: implementar liquidez y calidad de mercado: profundidad, spread, volumen fiable, concentración de venue y costes; contextualizar por tamaño, lado y horizonte.
- MVP-055: mantener separación entre métricas de protocolo y valor económico atribuible al token.
- MVP-056: generar expediente analítico por activo y conservar contraevidencia.

**Aceptación:** cada métrica tiene contrato, linaje, aplicabilidad y tests; no hay métricas sin unidad/definición; las comparaciones se limitan a cohortes homogéneas.

### EPIC-06 — Valoración y detección de infravaloración
**Prioridad:** P1  
**Dependencias:** EPIC-05 y revisión/aprobación de contratos de valoración.

- MVP-060: aprobar métodos de valoración por tipología y condiciones de no aplicabilidad.
- MVP-061: implementar primer método de valoración únicamente para una tipología defendible.
- MVP-062: modelar escenarios, sensibilidad, intervalos y supuestos explícitos.
- MVP-063: comparar valor estimado con precio ejecutable, costes, liquidez y horizonte.
- MVP-064: implementar NOT_ESTIMABLE/ABSTAIN cuando los inputs o derechos sean insuficientes.
- MVP-065: definir calibración, error histórico y protocolo de validación antes de habilitar etiquetas fuertes de infravaloración.
- MVP-066: mantener deshabilitada la categoría extrema hasta calibración y validación independiente.

**Aceptación:** modelo reproducible y aplicable; incertidumbre y costes explícitos; pruebas de sensibilidad; no se etiqueta infravaloración por precio nominal ni por score aislado.

### EPIC-07 — Decision Engine y Candidate Registry
**Prioridad:** P1  
**Dependencias:** EPIC-04, EPIC-05 y, para categorías de infravaloración, EPIC-06.

- MVP-070: implementar composición de decisiones con envelopes validados y scopes consistentes.
- MVP-071: resolver formalmente FSA-01..FSA-05 antes de usar Scope Algebra para conclusiones exactas dependientes de sus garantías.
- MVP-072: implementar categorías de salida aprobadas y su semántica.
- MVP-073: implementar ranking parcial/multidimensional por horizonte y cohorte, sin forzar orden total.
- MVP-074: generar tesis, catalizadores, invalidadores, riesgos, contraevidencia e incertidumbre.
- MVP-075: implementar Candidate Registry, Watchlist y snapshots longitudinales.
- MVP-076: asegurar replay determinista y auditoría de cada decisión.

**Aceptación:** cada salida reconstruible; ninguna decisión favorable elude gates; abstención válida; ranking explicable y comparable; cambio de datos/modelo produce nuevo snapshot.

### EPIC-08 — Validación y paper portfolio
**Prioridad:** P1/P2  
**Dependencias:** EPIC-06 y EPIC-07.

- MVP-080: preregistrar hipótesis, horizonte, target, benchmark y métricas.
- MVP-081: construir dataset point-in-time con activos desaparecidos y universo histórico.
- MVP-082: implementar backtest walk-forward/OOS sin leakage.
- MVP-083: modelar fees, spreads, slippage, market impact, funding/borrow y restricciones relevantes.
- MVP-084: comparar contra benchmarks y estrategias simples.
- MVP-085: evaluar drawdown, tail risk, turnover, estabilidad, calibración y capacidad.
- MVP-086: implementar paper portfolio y registro de decisiones simuladas.
- MVP-087: preparar revisión cuantitativa independiente y registrar resultados negativos.

**Aceptación:** protocolo reproducible, separación temporal correcta, costes netos, benchmarks adecuados y holdout intacto; no se declaran resultados validados antes de revisión independiente.

### EPIC-09 — Seguridad, compliance y conformidad ISO
**Prioridad:** P1 transversal.

- MVP-090: verificar vigencia, edición, alcance y aplicabilidad de cada estándar en fuentes oficiales.
- MVP-091: crear crosswalk cláusula → requisito IIM → control → evidencia → responsable.
- MVP-092: ejecutar gap assessment con estados y evidencias por cláusula.
- MVP-093: completar threat model, análisis de dependencias, gestión de vulnerabilidades, accesos y respuesta a incidentes.
- MVP-094: revisar compliance legal por jurisdicción, actividad, proveedores y tratamiento de datos.
- MVP-095: definir auditoría independiente y, si aplica, esquema de certificación acreditado.

**Aceptación:** ninguna declaración de conformidad sin evidencia por alcance; certificación solo si organismo competente la otorga. ISO no se presenta como fórmula de selección de tokens.

## 5. Orden de ejecución recomendado

| Orden | Paquetes | Resultado |
|---|---|---|
| 1 | MVP-001..004 | Gobierno, alcance y backlog aprobados |
| 2 | MVP-010..017 | Identidad/universo/datos fundacionales revisados |
| 3 | MVP-020..026 | CI y base de ingeniería |
| 4 | MVP-030..036 | Snapshots y provenance |
| 5 | MVP-040..048 | Primer screening y vetos ejecutables |
| 6 | MVP-050..056 | Perfil analítico inicial |
| 7 | MVP-060..066 | Primera valoración tipológica |
| 8 | MVP-070..076 | Selección y Candidate Registry |
| 9 | MVP-080..087 | Validación y paper portfolio |
| 10 | MVP-090..095 | Aseguramiento y conformidad aplicable |

EPIC-02 puede avanzar en paralelo con EPIC-01 siempre que no codifique semántica pendiente. EPIC-09 también puede empezar con inventario y aplicabilidad, pero no declarar conformidad.

## 6. Primer vertical slice que debemos construir

El primer incremento ejecutable no será un modelo de predicción ni un ranking de cientos de tokens. Será un flujo pequeño y auditable:

1. cargar un pequeño universo de fixtures versionados;
2. resolver identidad con resultado explícito;
3. validar el snapshot de datos y su provenance;
4. ejecutar reglas de elegibilidad/screening;
5. evaluar un conjunto inicial de riesgos y vetos;
6. producir una salida estructurada con razones y abstención;
7. persistir/reproducir el expediente de evaluación;
8. ejecutar todo en CI.

Los fixtures sintéticos sirven para probar comportamiento técnico; no se presentarán como análisis de mercado real ni evidencia de inversión.

## 7. Definition of Done por paquete

Un paquete solo se cierra cuando:
- el alcance y owner están definidos;
- dependencias y decisiones previas están resueltas;
- artefactos y contratos están versionados;
- implementación está en el repositorio;
- pruebas pertinentes están automatizadas y ejecutadas;
- evidencia de ejecución está enlazada;
- revisión independiente requerida está registrada;
- trazabilidad se actualiza;
- limitaciones y riesgos residuales se documentan.

Estados permitidos deben reflejar la evidencia: PROPOSED, UNDER_REVIEW, APPROVED, IMPLEMENTED, TESTED, VALIDATED, BLOCKED o DEPRECATED. No avanzar un estado por inferencia.

## 8. Bloqueos que requieren decisiones humanas

1. aprobación del alcance inicial y tipologías;
2. owners y autoridades de aprobación/veto/excepción;
3. permisos/licencias y presupuesto de fuentes;
4. mandato, horizonte y jurisdicciones objetivo;
5. métodos de valoración permitidos;
6. criterios de liquidez y riesgo;
7. benchmarks y protocolo de validación.

Si falta una decisión, registrar BLOCKED con impacto y alternativa segura; no inventar parámetros.

## 9. Fuera del primer MVP

- universalidad de redes y activos;
- recomendación financiera personalizada;
- promesas de rentabilidad;
- clasificación extrema de infravaloración;
- ejecución real, custodia o gestión de claves;
- certificación ISO asumida;
- afirmaciones de superioridad mundial no comparadas y auditadas.

## 10. Estado

Este backlog es una descomposición propuesta del Plan Maestro. No implica que los paquetes estén aprobados, implementados, probados o validados. El avance debe registrarse con evidencia en Issues/PRs y en la matriz de trazabilidad.
