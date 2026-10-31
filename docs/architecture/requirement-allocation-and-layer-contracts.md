# Asignación de requisitos y contratos entre capas — IIE

**Versión:** 1.0.0  
**Fecha:** 2026-10-31  
**Estado:** PROPUESTA DE ARQUITECTURA / PENDIENTE DE REVISIÓN  
**Base estratégica:** `docs/architecture/strategic-architecture.md` v1.0.0  
**Registro de requisitos:** `requirements/requirements-register.md`  
**Matriz de evidencia:** `docs/traceability/requirements-traceability-matrix.md`

> Este documento asigna los requisitos existentes a las capas estratégicas y define interfaces conceptuales. No modifica el estado metodológico de ningún requisito, no aprueba contratos técnicos, no declara implementación y no autoriza producción.

## 1. Propósito

Convertir la arquitectura de cuatro capas en una estructura trazable para el diseño técnico. Cada requisito IIM debe tener:
- una capa responsable principal;
- capas consumidoras o colaboradoras cuando corresponda;
- una salida verificable;
- dependencias explícitas;
- evidencia necesaria para avanzar de DOCUMENTED/SPECIFICATION_DRAFTED a SPECIFIED, IMPLEMENTED, TESTED y EMPIRICALLY_VALIDATED, según sea aplicable.

La asignación indica responsabilidad arquitectónica, no propiedad exclusiva del requisito. Los controles de gobernanza, datos, seguridad, observabilidad y auditoría atraviesan todas las capas.

## 2. Definiciones de capas

| Código | Capa | Responsabilidad |
|---|---|---|
| L1 | INTELLIGENCE | Identidad, universo, datos, análisis de mercado/fundamental, riesgo analítico y valoración. |
| L2 | DECISION | Elegibilidad decisional, vetos, tesis, comparación, ranking parcial, decisión y abstención. |
| L3 | PORTFOLIO & EXECUTION | Cartera, exposición, asignación, órdenes, autorización, ejecución y conciliación. |
| L4 | VALIDATION & LEARNING | Pruebas, backtesting, evaluación fuera de muestra, calibración, monitorización, auditoría y aprendizaje. |
| X | TRANSVERSAL | Gobernanza, seguridad, provenance, versionado, control de cambios, observabilidad y auditoría aplicables a todas las capas. |

## 3. Matriz de asignación de los 24 requisitos

| ID | Capa principal | Colaboración | Resultado arquitectónico esperado |
|---|---|---|---|
| IIM-001 | L1 | X, L4 | Identidad inequívoca y tipada del activo/red/representación, con estado de resolución y evidencia. |
| IIM-002 | L1 | X, L4 | Registro de fuentes, derechos de uso, cobertura, calidad, procedencia y restricciones por finalidad. |
| IIM-003 | L2 | L1, L4 | Conclusión que separa precio nominal, valor estimado y oportunidad, con abstención si no hay base suficiente. |
| IIM-004 | L1 | L4 | Valoración con método adecuado a tipología y objeto económico, supuestos y límites explícitos. |
| IIM-005 | L2 | L1, L4, X | Tesis con hipótesis, alternativas, evidencia favorable/contraria y registro de cambios. |
| IIM-006 | L2 | L1, L3, L4 | Evaluación de riesgos críticos y vetos que no puedan compensarse mediante una puntuación. |
| IIM-007 | L4 | L1, L2, L3, X | Evidencia de controles temporales y validación fuera de muestra sin leakage ni sesgos indebidos. |
| IIM-008 | L4 | L2, L3, X | Historial versionado, reconstruible y comparable de evaluaciones y decisiones. |
| IIM-009 | L3 (orquestación) | L1, L2, L4, X | Ejecución de evaluaciones solo bajo solicitud explícita y autorización correspondiente. |
| IIM-010 | L2 | L1, L4, X | Conclusión estructurada con evidencia, incertidumbre, limitaciones y estado de decisión. |
| IIM-011 | L1 | L2, L4 | Taxonomía de activos por derechos, función y captura de valor, incluidas tipologías híbridas. |
| IIM-012 | L1 | L4 | Selección justificada del método de valoración según objeto y tipología. |
| IIM-013 | L1 | L2, L4 | Abstención de valoración cuando derechos, identidad o evidencia sean insuficientes. |
| IIM-014 | L2 | L1, L3, L4 | Evaluación de brecha entre precio ejecutable y rango de valor razonable, incluyendo costes e incertidumbre. |
| IIM-015 | L4 | L2, X | Calibración y validación independiente requeridas antes de habilitar cualquier categoría extrema. |
| IIM-016 | L2 | L1, L3, L4 | Perfil de riesgo por tipología, evidencia, controles y riesgo residual. |
| IIM-017 | L2 | L3, L4, X | Gates de veto con transiciones verificables y precedencia sobre el resultado de valoración/ranking. |
| IIM-018 | L1 | L4, X | Datos admisibles, con calidad, linaje y semántica temporal point-in-time demostrables. |
| IIM-019 | L1 | L4, X | Resolución canónica de activos, redes, contratos y representaciones sin depender del ticker. |
| IIM-020 | L1 | L2, L4 | Universos versionados y elegibilidad por finalidad/fecha, con controles de survivorship bias. |
| IIM-021 | L1 | L2, L4 | Modelos reproducibles con atribución, escenarios, sensibilidad e incertidumbre. |
| IIM-022 | L2 | L1, L3, L4 | Decisiones y rankings parciales reproducibles por horizonte, con gates, dominancia y abstención. |
| IIM-023 | L2 | L4, X | Informes trazables y snapshots inmutables, comparables longitudinalmente. |
| IIM-024 | L3 (orquestación) | L1, L2, L4, X | Workflow bajo demanda con estados, gates, idempotencia, recuperación y observabilidad. |

### 3.1 Interpretación

- **L1 es la fuente de hechos y estimaciones admisibles.** L2 no debe reconstruir identidades, corregir silenciosamente datos ni recalcular valoraciones fuera de contratos controlados.
- **L2 es la autoridad de decisión metodológica.** No debe convertir una señal de valoración en una orden ni omitir vetos.
- **L3 controla la materialización de decisiones en cartera/operaciones.** Una decisión elegible no equivale a autorización para operar.
- **L4 tiene autoridad de control y evaluación.** Puede bloquear, condicionar, retirar o exigir revisión de componentes cuando la evidencia no supere los criterios aprobados.
- **X aplica a todo el ciclo.** No debe existir una ruta de datos, decisión u operación que evada provenance, seguridad, versionado y auditoría.

## 4. Interfaces conceptuales entre capas

Las interfaces siguientes son límites lógicos. Sus esquemas, APIs, eventos, serialización, claves, protocolos y reglas de compatibilidad quedan pendientes de especificación técnica.

### IF-01 — Fuentes externas → INTELLIGENCE

**Propósito:** admitir observaciones de proveedores, redes, mercados y fuentes documentales.

Entradas conceptuales:
- identificador de fuente y versión de términos;
- referencia de recurso o instrumento;
- contenido/observación y unidad;
- timestamps de evento, disponibilidad pública/autorizada, captura e ingestión cuando correspondan;
- licencia, permiso, finalidad y periodo de uso;
- evidencia de cobertura, calidad y procedencia.

Salidas:
- observación normalizada o rechazo explícito;
- metadatos de provenance y temporalidad;
- estado de admisibilidad/calidad;
- motivo y evidencia de conflicto, ausencia, retraso o restricción.

Invariantes:
- ninguna fuente se considera habilitada por el mero hecho de ser accesible;
- ningún dato ausente se convierte silenciosamente en cero;
- una captura posterior no se presenta como conocimiento disponible en el pasado;
- los usos no demostrados permanecen bloqueados o restringidos.

### IF-02 — INTELLIGENCE → DECISION

**Propósito:** transferir una base de análisis que pueda sustentar una decisión reproducible.

Paquete lógico de salida:
- identidad y versión del universo;
- observaciones/datasets con referencias de provenance;
- perfil de mercado y fundamental;
- valoración o estado de abstención;
- riesgos analíticos y calidad de evidencia;
- fecha de corte, timestamps y versiones de métodos/modelos;
- incertidumbre, cobertura, limitaciones y conflictos conocidos.

Invariantes:
- L2 consume solo datos admisibles para la finalidad y el instante de decisión;
- cada estimación conserva método, inputs, supuestos y versión;
- UNKNOWN, BLOCKED, QUARANTINED, REJECTED o INCONCLUSIVE no equivalen a PASS;
- no se emite una conclusión más fuerte que la evidencia de entrada.

### IF-03 — DECISION → PORTFOLIO & EXECUTION

**Propósito:** entregar decisiones vigentes como insumo para la construcción de cartera y, si se autoriza separadamente, para la preparación de operaciones.

Paquete lógico de salida:
- decisión y estado;
- activo/instrumento y listing inequívocos;
- horizonte y fecha de caducidad/revisión;
- tesis, contra-tesis y condiciones de invalidación;
- riesgos bloqueantes, gates y restricciones;
- evidencia y versiones utilizadas;
- incertidumbre y condiciones de abstención.

Invariantes:
- elegibilidad no significa orden ni autorización;
- estados REJECTED, BLOCKED, EXPIRED o ABSTAIN no pueden convertirse en órdenes;
- la cartera debe aplicar límites independientes de la puntuación;
- las operaciones requieren autorización explícita, controles pre-trade y registro posterior;
- el modo de simulación y el modo real deben estar separados de forma inequívoca.

### IF-04 — PORTFOLIO & EXECUTION → VALIDATION & LEARNING

**Propósito:** aportar el registro necesario para evaluar decisiones, exposiciones y ejecución.

Salidas:
- snapshots de cartera y exposiciones;
- órdenes, autorizaciones, fills, cancelaciones y conciliaciones;
- timestamps, venue, instrumento y costes observados;
- restricciones, excepciones, fallos y decisiones de parada;
- vínculo con la decisión, modelo y datos que originaron la operación.

Invariantes:
- separar resultados simulados de reales;
- no atribuir fills no confirmados;
- registrar costes y eventos fallidos;
- conservar suficiente detalle para reconstrucción y auditoría.

### IF-05 — Todas las capas → VALIDATION & LEARNING

**Propósito:** permitir evaluación transversal de datos, software, modelos, decisiones y operación.

Entradas:
- versiones de código, configuración, especificación y modelo;
- fixtures, datasets y snapshots con provenance;
- resultados de pruebas y métricas;
- predicciones/decisiones emitidas y resultados observados;
- incidencias, excepciones, cambios y aprobaciones.

Salidas:
- dictamen de prueba/validación con alcance y limitaciones;
- métricas y resultados reproducibles;
- hallazgos, severidad y acciones correctivas;
- aprobación condicionada, rechazo, bloqueo o retirada;
- evidencia de regresión y monitorización.

Invariantes:
- no declarar TESTED sin logs/resultados reproducibles vinculados a versión;
- no declarar EMPIRICALLY_VALIDATED sin protocolo y evidencia empírica apropiados;
- congelar previamente el protocolo de evaluación cuando sea necesario para evitar selección retrospectiva;
- los cambios de modelo/configuración requieren nueva evaluación según análisis de impacto.

## 5. Sobre común de trazabilidad

Como requisito de diseño, todo artefacto intercambiado entre capas deberá poder vincularse, cuando sea aplicable, a:

- identificador y versión del contrato;
- identificadores tipados de entidad, instrumento y listing;
- instante de referencia y fecha de generación;
- referencias a datos y evidencia de origen;
- versiones de código, configuración, método y modelo;
- estado de calidad/admisibilidad y resultado;
- incertidumbre, limitaciones y motivos de abstención;
- identificador de ejecución/correlación y registro de auditoría.

Esta lista es un **sobre conceptual mínimo**, no un JSON Schema aprobado. La obligatoriedad, cardinalidad, tipos, normalización, reglas temporales, firma/hash y compatibilidad deben definirse en contratos técnicos específicos.

## 6. Dependencias y gates de implementación

| Gate | Dependencia que debe cerrarse | Requisitos principales | Evidencia mínima para avanzar |
|---|---|---|---|
| G0 — Fundamentos | Identidad, universo, fuentes, calidad, point-in-time y gobernanza | IIM-001, 002, 018, 019, 020 | Revisión metodológica registrada; decisiones abiertas explícitas; expedientes de fuente cuando proceda; fixtures y pruebas ejecutadas para el alcance aprobado. |
| G1 — Intelligence | Contratos de taxonomía, datos, riesgo analítico y valoración | IIM-004, 011, 012, 013, 016, 021 | Especificaciones aprobadas, datos admisibles, casos de prueba, resultados y control de abstención. |
| G2 — Decision | Contratos de gates, tesis, incertidumbre, comparación y reporting | IIM-003, 005, 006, 010, 014, 017, 022, 023 | Reglas formales, precedencia de vetos, escenarios límite, trazabilidad y pruebas de regresión. |
| G3 — Orquestación y control | Estados, autorización, historial, idempotencia y recuperación | IIM-008, 009, 024 | Máquina de estados probada, idempotencia, recuperación, audit trail y autorización bajo demanda. |
| G4 — Portfolio & Execution | Riesgo de cartera, liquidez, venue, costes, seguridad y permisos | IIM-006, 016, 017, 024 más especificaciones nuevas derivadas de cartera/ejecución | Simulación reproducible, límites pre-trade, pruebas operativas y de seguridad; autorización explícita antes de cualquier uso real. |
| G5 — Validación empírica | Datos históricos admisibles, protocolo congelado, benchmarks y costes | IIM-007, 015 y validación transversal | Walk-forward/out-of-sample, control de sesgos, costes, sensibilidad, robustez y revisión independiente cuando aplique. |

Los gates son dependencias de entrega. L4 (VALIDATION & LEARNING) debe proporcionar pruebas desde G0; G5 representa la madurez de validación empírica, no el inicio tardío de las pruebas.

## 7. Brecha entre requisitos actuales y alcance estratégico

Los 24 requisitos actuales no cubren por sí solos todo el alcance de PORTFOLIO & EXECUTION. Antes de implementar esa capa deben derivarse y aprobarse requisitos específicos, al menos para:

- construcción de cartera y política de asignación;
- límites de concentración y presupuestos de riesgo;
- exposición agregada por activo, red, protocolo, contraparte y factor;
- liquidez, capacidad, slippage, comisiones e impacto de mercado;
- selección/admisibilidad de venue y riesgo de contraparte;
- gestión de órdenes, autorización, cancelación, fills y conciliación;
- custodia, gestión de secretos, permisos y segregación de funciones;
- kill switches, límites operativos y respuesta a incidentes;
- simulación/paper trading frente a operación real;
- obligaciones legales y jurisdiccionales aplicables.

Esta brecha es intencionalmente visible. No se debe forzar la cobertura de estas capacidades dentro de requisitos existentes que no las especifican.

## 8. Reglas de conformidad arquitectónica

Una implementación futura solo será conforme con esta arquitectura si:
1. cada módulo declara la capa y los requisitos que satisface;
2. sus entradas/salidas respetan contratos versionados;
3. no evita gates ni consume estados bloqueados como aprobados;
4. conserva provenance, temporalidad, versiones y auditoría;
5. separa decisión, autorización y ejecución;
6. expone errores, incertidumbre y abstención;
7. cuenta con pruebas reproducibles adecuadas a su nivel de riesgo;
8. registra dependencias y efectos de cambios sobre otras capas.

## 9. Decisiones pendientes

No se consideran resueltas por este documento:
- nombres definitivos de servicios/módulos y límites de despliegue;
- APIs, eventos, esquemas y serialización;
- almacenamiento, base de datos, colas y orquestador;
- semántica exacta de estados y transiciones;
- políticas cuantitativas, umbrales, pesos y criterios de ranking;
- requisitos nuevos de cartera/ejecución;
- requisitos de seguridad y cumplimiento específicos de jurisdicción;
- selección de proveedores y autorización de sus datos.

Cada decisión deberá contar con ADR o especificación controlada, alternativas, consecuencias, evidencia y aprobación.

## 10. Criterios de aceptación de este documento

- Los 24 requisitos IIM-001 a IIM-024 están asignados a una capa principal.
- Se declaran colaboraciones cuando la responsabilidad cruza capas.
- Las cinco interfaces conceptuales tienen propósito, entradas/salidas e invariantes.
- El sobre de trazabilidad está marcado como conceptual y no como esquema aprobado.
- Los gates reflejan dependencias sin declarar tareas completadas.
- Se identifica explícitamente la brecha de requisitos de cartera/ejecución.
- Se preservan los estados actuales de aprobación, implementación, pruebas y validación.

**Decisión propuesta:** utilizar esta asignación como base para revisión técnica y derivación de especificaciones; no iniciar implementaciones de producción basándose únicamente en este documento.
