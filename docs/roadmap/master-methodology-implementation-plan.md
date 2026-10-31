# Plan Maestro de Implementación de la Metodología IIE

**Identificador:** IIE-PLAN-MASTER-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / PENDING_REVIEW / NOT_APPROVED / NOT_IMPLEMENTED

> Este plan organiza el trabajo y define puertas de aceptación. No aprueba requisitos IIM, no certifica conformidad ISO, no demuestra software operativo y no autoriza recomendaciones automatizadas ni trading real.

## 1. Propósito

Convertir la metodología IIE en un sistema de inteligencia de inversión en criptoactivos capaz de descubrir, investigar, valorar, comparar y seleccionar activos; evaluar riesgos; proponer carteras; y validar sus resultados con evidencia reproducible.

El resultado no será una máquina que predice infaliblemente ganadores. Será un sistema que identifica oportunidades defendibles, cuantifica incertidumbre y puede abstenerse cuando la evidencia sea insuficiente.

## 2. Objetivo operativo prioritario

La primera capacidad de negocio que debe materializarse es un **motor reproducible de screening y selección analítica** que transforme un universo versionado en:

- activos excluidos, con razones y evidencia;
- activos pendientes de investigación o con datos insuficientes;
- candidatos elegibles para investigación o valoración;
- candidatos de inversión condicionados a mandato, horizonte y jurisdicción;
- candidatos potencialmente infravalorados, solo si existe valoración válida;
- listas de seguimiento y abstenciones explícitas.

Cada resultado debe incluir fecha de corte, finalidad, scope, versión de metodología y modelos, fuentes, frescura, cobertura, riesgos, limitaciones, razones, incertidumbre y referencias de evidencia.

No se publicará un ranking único que mezcle calidad, valoración, momentum, liquidez y riesgo sin definir su semántica y comparabilidad. La calidad de un proyecto, su valor estimado, el atractivo de su precio y su idoneidad para una cartera son dimensiones distintas.

## 3. Principios no negociables

1. Evidencia antes que opinión.
2. Identidad antes que análisis.
3. Corrección point-in-time; evitar look-ahead, leakage y survivorship bias.
4. Datos ausentes, contradictorios, obsoletos o inadmisibles no se convierten en cero.
5. Riesgos críticos y vetos no se compensan con puntuaciones.
6. Elegibilidad específica de propósito, actividad, ruta, jurisdicción y contexto.
7. Valoración solo cuando el método sea aplicable y la evidencia suficiente.
8. Separación entre selección, oportunidad de entrada, sizing y ejecución.
9. Abstención ante incertidumbre material.
10. Reproducibilidad, versionado, auditabilidad y revisión independiente.
11. No declarar IMPLEMENTED, TESTED, COMPLIANT o EMPIRICALLY_VALIDATED sin evidencia.
12. Ningún screening habilita automáticamente trading real.

## 4. Arquitectura objetivo

### 4.1 INTELLIGENCE
Identidad y universo; adquisición y normalización; calidad y linaje de datos; análisis de mercado y liquidez; fundamentos; tokenomics; tecnología y seguridad; gobernanza; valoración y escenarios.

### 4.2 DECISION
Elegibilidad por propósito; estados canónicos; evaluación de riesgos; vetos; composición determinista; tesis; clasificación de candidatos; ranking multidimensional justificado; abstención y reporting.

### 4.3 PORTFOLIO & EXECUTION
Mandatos; políticas y presupuestos de riesgo; exposiciones; propuestas de cartera; límites; simulación; controles de órdenes; reconciliación; custodia y autorización. El alcance inicial se limita a análisis y paper portfolio. La ejecución real requiere autorización, seguridad, revisión legal y gates separados.

### 4.4 VALIDATION & LEARNING
Pruebas de contratos; pruebas unitarias, integración y propiedades; backtesting point-in-time; walk-forward; out-of-sample; costes y slippage; calibración; robustez; monitorización; incidentes; revisión independiente y gestión de cambios.

Gobernanza, seguridad, compliance, provenance, observabilidad y configuración son controles transversales.

## 5. Estrategia de entrega: dos carriles coordinados

**Carril A — Cierre metodológico y normativo.** Resolver ambigüedades semánticas, decisiones de owners, trazabilidad requisito-control-evidencia, gobernanza, contratos y aplicabilidad de estándares. Ningún borrador se convierte en requisito normativo sin aprobación explícita.

**Carril B — Construcción incremental de software.** Implementar componentes pequeños con contratos estables, fixtures, pruebas automatizadas y una integración vertical temprana. Se puede desarrollar infraestructura y prototipos aislados mientras una decisión metodológica siga abierta, pero el comportamiento afectado debe permanecer deshabilitado o marcado como experimental.

Un documento no cuenta como software; código sin tests no cuenta como capacidad probada; tests sintéticos no equivalen a validación empírica.

## 6. Fases y puertas de aceptación

Las fases son secuenciales por dependencias, pero algunas actividades pueden ejecutarse en paralelo. No se asignan fechas hasta estimar capacidad, responsables y dependencias.

### Fase 0 — Baseline, gobierno del programa y alcance

**Objetivo:** fijar la línea base y controlar la evolución.

Trabajo:
- aprobar alcance, objetivos, usuarios, casos de uso, horizontes y límites de responsabilidad;
- mantener registro de decisiones, riesgos, supuestos, dependencias y cambios;
- congelar versiones de referencia de arquitectura, metodología y requisitos;
- establecer Definition of Ready, Definition of Done y estados de evidencia;
- definir propietarios, revisores y aprobadores; resolver segregación de funciones;
- distinguir requisitos normativos de propuestas.

Entregables: baseline de arquitectura y metodología; plan maestro aprobado; matriz de decisiones y responsabilidades; backlog priorizado.

**Gate 0:** alcance coherente; cada artefacto tiene owner, estado, versión y ruta de aprobación. No se alteran requisitos oficiales sin autorización.

### Fase 1 — Fundamentos, identidad, universo y datos

**Objetivo:** garantizar que sabemos qué activo analizamos y que los datos pueden utilizarse.

Trabajo:
- cerrar requisitos fundacionales y bloqueos de fuentes/licencias;
- definir taxonomía de activos y representaciones;
- ampliar resolución de identidad para redes, contratos, proxies, wrappers, bridges, migraciones, forks y activos especiales;
- crear registro de fuentes, permisos, cobertura, frescura y limitaciones;
- implementar adquisición reproducible, normalización, timestamps y linaje;
- diseñar snapshots point-in-time, detección de revisiones, conflictos, duplicados y anomalías;
- definir exclusiones por calidad y cobertura.

Entregables: registro versionado de activos e identidades; catálogo de fuentes y permisos; contratos de datos y provenance; pipeline mínimo reproducible; fixtures identificados claramente como sintéticos o reales autorizados.

**Gate 1:** identidad resuelta para el propósito requerido; fuentes autorizadas; datos con timestamps, cobertura, procedencia y controles de calidad; los desconocidos bloquean decisiones que dependan de ellos.

### Fase 2 — Screening, elegibilidad y riesgo

**Objetivo:** reducir el universo sin permitir que puntuaciones oculten riesgos críticos.

Trabajo:
- formalizar criterios de inclusión, exclusión y aplicabilidad por categoría;
- separar elegibilidad analítica de elegibilidad operacional;
- implementar estados canónicos y transiciones;
- separar RiskAssessment, RiskDomainStatus y VetoDecision;
- implementar vetos por propósito, actividad, scope, ruta y jurisdicción;
- establecer frescura, invalidación y reevaluación;
- resolver X-06 y contratos de decisión determinista;
- convertir escenarios X-09 prioritarios en pruebas ejecutables.

Entregables: motor de screening; catálogo de riesgos y reglas; motor de elegibilidad y vetos; reason codes y decision trace; pruebas de regresión y adversariales.

**Gate 2:** ningún score compensa un veto; UNKNOWN, INVALID o stale no se convierten en PASS; resultados deterministas para inputs y versiones idénticos.

### Fase 3 — Análisis fundamental, tokenomics, tecnología y mercado

**Objetivo:** construir perfiles comparables y específicos por tipología.

Trabajo:
- definir módulos por familias de activos, sin forzar una métrica universal;
- analizar oferta, emisiones, desbloqueos, concentración, derechos y captura de valor;
- medir uso, adopción, actividad económica y sostenibilidad con definiciones y límites;
- analizar arquitectura, descentralización, seguridad, upgrades, permisos y dependencias;
- medir liquidez ejecutable, profundidad, spreads, volumen fiable y costes;
- evaluar riesgos jurídicos y jurisdiccionales con revisión competente;
- conservar incertidumbre, conflictos y calidad de evidencia.

Entregables: perfiles por tipología; módulos analíticos versionados; métricas definidas y trazables; informes de cobertura y calidad.

**Gate 3:** cada métrica tiene definición, unidad, fuente, timestamp, aplicabilidad, limitaciones y test; las comparaciones se hacen entre activos razonablemente comparables.

### Fase 4 — Valoración e infravaloración

**Objetivo:** distinguir precio, valor razonable estimado y oportunidad.

Trabajo:
- definir métodos de valoración por tipología y condiciones de aplicabilidad;
- especificar inputs, supuestos, horizonte, escenarios, sensibilidad y rangos;
- evitar doble conteo y falsa precisión;
- establecer cuándo no valorar por falta de datos o inaplicabilidad;
- incorporar supply/dilution, liquidez, costes, riesgos y calidad de derechos;
- calibrar modelos y registrar errores, sesgos y cambios;
- separar estimación de valor de decisión de inversión.

Entregables: catálogo de modelos; expedientes reproducibles; escenarios; incertidumbre y límites; protocolo de calibración.

**Gate 4:** no se etiqueta UNDERVALUED si el modelo no aplica, los datos no son admisibles o el margen estimado no supera incertidumbre, costes y riesgos según criterios aprobados.

### Fase 5 — Composición de decisiones, candidatos y ranking

**Objetivo:** transformar análisis en decisiones explicables.

Trabajo:
- integrar estados, elegibilidad, riesgo, veto, valoración y freshness;
- completar Scope Algebra y sus obligaciones de prueba antes de utilizar resultados exactos;
- resolver FSA-01 a FSA-05 y dependencias X-09;
- definir categorías de salida y semántica;
- crear ranking multidimensional o por etapas, evitando agregados engañosos;
- generar tesis, catalizadores, invalidadores, riesgos y evidencia contraria;
- implementar abstención y revisión humana;
- definir interfaces de selección a portfolio.

Entregables: Decision Engine; Candidate Registry y Watchlist; informe por activo; comparación por cohortes; DecisionEnvelope y trazabilidad.

**Gate 5:** cada salida se reconstruye desde datos y versiones; ninguna salida favorable ignora veto, incertidumbre o evidencia adversa; los subconjuntos reciben decisiones independientes; la abstención funciona.

### Fase 6 — Portfolio y paper trading

**Objetivo:** comprobar que una candidatura puede ser adecuada dentro de un mandato y cartera.

Trabajo:
- aprobar contratos y requisitos Portfolio & Execution;
- separar mandato, política de riesgo, presupuesto, autorización y enforcement;
- calcular exposiciones, concentración, correlación, liquidez y drawdown;
- simular sizing, rebalanceo, costes, slippage, market impact y restricciones;
- diseñar controles de órdenes, estados unknown, cancel/reduce/close y reconciliación;
- construir paper portfolio sin credenciales ni ejecución real.

Entregables: propuestas de cartera; presupuestos y snapshots de exposición; simulador/paper portfolio; controles y audit trail; informes de atribución.

**Gate 6:** propuestas cumplen mandato y límites; estados inciertos bloquean acciones; resultados de paper trading reproducibles. Este gate no autoriza capital real.

### Fase 7 — Validación científica y rendimiento

**Objetivo:** determinar si el sistema aporta valor fuera de muestra y después de costes.

Trabajo:
- preregistrar hipótesis, targets, horizontes, benchmarks y métricas;
- construir datasets point-in-time sin leakage ni survivorship bias;
- separar train, calibration, validation y test cronológicamente;
- ejecutar walk-forward, out-of-sample y análisis por régimen;
- incluir fees, spreads, slippage, market impact, funding, borrow, impuestos cuando corresponda y restricciones de liquidez;
- evaluar turnover, drawdown, tail risk, concentración, capacidad y estabilidad;
- comparar con benchmarks simples y alternativas;
- probar robustez, sensibilidad y ablación;
- conservar resultados negativos y experimentos fallidos;
- realizar revisión cuantitativa independiente.

Entregables: protocolo aprobado; datasets reproducibles; reportes de backtest y OOS; model/strategy cards; informe independiente.

**Gate 7:** no se hacen claims de rendimiento sin resultados OOS reproducibles, costes realistas, benchmarks apropiados y revisión independiente.

### Fase 8 — Seguridad, compliance, ISO y aseguramiento independiente

**Objetivo:** demostrar controles y conformidad aplicable.

Trabajo:
- completar registro de normas y determinación de aplicabilidad;
- crear crosswalk cláusula ISO → requisito IIM → control → evidencia → owner;
- realizar gap assessment y remediación;
- implementar controles de seguridad según alcance decidido;
- revisar compliance por jurisdicción, actividad, producto, datos y proveedores;
- realizar threat modeling, revisión de arquitectura, análisis de dependencias y pruebas de seguridad;
- auditar datos, modelos, código, accesos, continuidad, incidentes y cambios;
- seleccionar organismo certificador acreditado cuando la certificación sea pertinente;
- mantener auditoría independiente metodológica, cuantitativa y de seguridad.

Referencias iniciales sujetas a verificación de edición, vigencia, aplicabilidad y alcance: ISO 31000, ISO/IEC 27001, ISO 37301, ISO 9001, ISO 19011, ISO/IEC 42001, ISO 24165, ISO 22739 e ISO 23257.

**Gate 8:** conformidad declarada solo por cláusula y alcance con evidencia suficiente; certificación solo si un organismo competente la concede. Una norma de directrices no se presenta como certificación de producto.

### Fase 9 — Operación controlada y mejora continua

**Objetivo:** operar con límites, observabilidad y capacidad de retirar decisiones o modelos degradados.

Trabajo:
- despliegue gradual por entorno y permisos;
- monitorización de datos, modelos, riesgos, fuentes y cambios de régimen;
- alertas, incidentes, rollback y kill switches;
- revisión periódica de universos, reglas, fuentes y modelos;
- auditorías y recertificación cuando aplique;
- gestión de cambios, versionado, aprobación y comunicación;
- habilitar operaciones reales solo tras decisión separada, revisión legal, seguridad, autorización y pruebas específicas.

**Gate 9:** operación dentro del alcance autorizado, observada y auditable; capacidad probada de detener, revertir o degradar el servicio.

## 7. Ruta crítica inmediata

1. Aprobar este plan como guía de ejecución sin alterar requisitos normativos.
2. Resolver bloqueos fundacionales IIM y decisiones de autoridad/gobernanza.
3. Completar FSA-02 (completitud del universo de dependencias), FSA-03 (binding de certificados), FSA-04 (obligaciones semánticas de partición) y FSA-05 (cobertura adversarial), además de someter FSA-01 a revisión formal.
4. Construir inventario ISO y matriz de aplicabilidad; no declarar conformidad anticipada.
5. Definir el MVP ejecutable y su contrato de entrada/salida.
6. Implementar identidad/universo/datos, después screening/riesgo y luego análisis/valoración/decisión.
7. Crear un primer vertical slice reproducible sobre universo acotado y tipologías explícitas.
8. Ejecutar pruebas automatizadas y revisión independiente antes de ampliar el universo.
9. Incorporar paper portfolio y validación OOS antes de cualquier claim de inversión.

No se debe bloquear ingestión, identidad, calidad de datos, harness y pruebas por cada discusión documental avanzada. Sí se debe bloquear cualquier decisión que dependa de semántica no aprobada.

## 8. MVP: definición y límites

El MVP debe responder: **¿qué activos merecen investigación adicional y cuáles, con evidencia suficiente, pueden convertirse en candidatos para un mandato definido?**

Alcance mínimo:
- universo inicial limitado y documentado;
- cobertura de redes y representaciones explícita;
- fuentes autorizadas y trazables;
- snapshots reproducibles;
- screening con reglas aprobadas;
- evaluación inicial de liquidez y riesgos críticos;
- perfiles fundamentales y tokenomics de una o dos tipologías;
- valoración solo para tipologías con método defendible;
- decisión explicable y watchlist;
- reportes y audit trail.

Fuera del MVP:
- cobertura universal de todas las cadenas y tokens;
- promesa de identificar futuros ganadores;
- rentabilidad garantizada;
- recomendación personalizada sin mandato y contexto;
- trading autónomo o custodia de claves;
- claims de conformidad ISO o validación no demostrados.

## 9. Métricas de progreso y calidad

No fijar objetivos numéricos arbitrarios antes de conocer datos, universo, horizonte y capacidad. Cada métrica tendrá baseline, umbral justificado y responsable.

**Datos:** cobertura por campo/activo/fuente; permisos; frescura; conflictos; identidad resuelta; trazabilidad.

**Decisiones:** reproducibilidad; tasa y causas de abstención; propagación de vetos; revocaciones por datos tardíos; falsos positivos/negativos revisados; consistencia entre versiones.

**Cuantitativas:** rendimiento neto y benchmark; drawdown y tail risk; turnover y costes; estabilidad por régimen; calibración y error de valoración; capacidad y liquidez.

**Ingeniería y gobernanza:** cobertura de tests por riesgo; defectos críticos; reproducibilidad; tiempos de incidentes; cambios sin aprobación; hallazgos de auditoría y remediación.

## 10. Requisitos, cambios y evidencias

Cada requisito debe mantener ID, fuente, justificación, owner, estado, versión, prioridad, dependencias, control, artefacto, prueba, evidencia, aprobación y criterio de aceptación.

Estados separados: PROPOSED, UNDER_REVIEW, APPROVED, IMPLEMENTED, TESTED, VALIDATED y DEPRECATED. No son equivalentes ni se avanzan automáticamente.

Todo cambio metodológico debe incluir impacto sobre requisitos, schemas, datos históricos, decisiones, modelos, escenarios, documentación, migración y reproducibilidad. Mantener compatibilidad o migración explícita.

## 11. Gobernanza y aprobación

La segregación de funciones debe impedir que una misma persona controle sin revisión la creación de evidencia, evaluación de riesgo, aprobación de decisiones y levantamiento de vetos. Las autoridades concretas requieren aprobación legítima.

Roles funcionales a formalizar: Methodology Owner; Data Owner/Steward; Asset Identity Owner; Risk Owner; Model/Quant Owner; Security Owner; Compliance/Legal Reviewer; Architecture Owner; Product/Portfolio Owner; Independent Reviewer; Release/Operations Owner.

La independencia y autoridad real no se presumen por nombrar roles en un documento.

## 12. Riesgos del programa

| Riesgo | Tratamiento |
|---|---|
| Documentación sin software útil | Vertical slice y gates por capacidad |
| Software basado en semántica ambigua | Contratos aprobados y feature flags |
| Datos sesgados o sin licencia | Source registry, point-in-time y legal review |
| Falsos positivos de inversión | Vetos, abstención y validación OOS |
| Sobreajuste y leakage | Protocolo preregistrado y holdout |
| Scores no comparables | Tipologías, cohortes y dimensiones separadas |
| Falsa precisión en valoración | Rangos, sensibilidad y no-valuation |
| Complejidad Scope Algebra | Límites, certificados, checker independiente y fail-closed |
| Concentración de autoridad | RACI, SoD y revisión independiente |
| Claims prematuros ISO/Top Mundial | Matriz de evidencia y auditoría externa |
| Trading prematuro | Paper-first y autorización separada |

## 13. Criterio para afirmar referencia mundial

No se utilizará “Top Mundial” como estado autodeclarado. Solo podrá sostenerse una afirmación de referencia internacional con evidencia convergente de:
1. rigor metodológico frente a comparadores definidos;
2. software fiable y reproducible;
3. validación empírica independiente y resultados transparentes;
4. controles de riesgo y seguridad auditados;
5. conformidad normativa demostrada para el alcance aplicable;
6. revisión externa por expertos competentes;
7. historial operativo y mejora continua;
8. comunicación transparente de límites y resultados negativos.

El reconocimiento externo es una consecuencia posible de la calidad demostrada, no un entregable aprobable internamente.

## 14. Definition of Done global

El IIE no se considera listo para uso de inversión hasta que:
- los requisitos críticos estén aprobados y trazados;
- datos, identidad, análisis y decisión estén implementados y probados;
- las decisiones se reproduzcan desde snapshots y versiones;
- vetos, unknown y abstención funcionen;
- las valoraciones tengan aplicabilidad, incertidumbre y calibración;
- exista validación temporal OOS con costes;
- seguridad y compliance estén revisados para el alcance;
- gobernanza y revisión independiente estén operativas;
- límites de uso y autorizaciones estén aplicados;
- exista un expediente de evidencia para cada claim relevante.

Preparación para producción y autorización de trading real son decisiones separadas.

## 15. Decisiones pendientes de los owners

1. Mandatos y perfiles de inversor soportados.
2. Horizontes y benchmarks por estrategia.
3. Universo inicial, jurisdicciones y tipologías.
4. Fuentes de datos, licencias, presupuesto y proveedores.
5. Métodos de valoración admitidos por tipología.
6. Política de liquidez y capacidad mínima.
7. Umbrales de riesgo y autoridades de veto/excepción.
8. Criterios de clasificación y ranking.
9. Métricas y protocolo de validación.
10. Alcance de certificaciones y organismo de evaluación, si aplica.

No se inventan respuestas a estas decisiones: requieren evidencia, responsables y aprobación.

## 16. Relación con artefactos existentes

Este plan coordina, pero no sustituye:
- docs/architecture/strategic-architecture.md
- docs/methodology/01-global-crypto-asset-selection-methodology.md
- docs/methodology/02-methodology-consistency-audit.md
- documentos X-01 a X-09 y Scope Algebra;
- docs/architecture/portfolio-execution-integrated-requirements-draft.md
- docs/architecture/portfolio-execution-integration-audit.md
- registro de requisitos y matriz de trazabilidad;
- registro de estándares y políticas de datos, riesgo, valoración y validación.

Ante contradicción, no se modifica silenciosamente un artefacto normativo: se registra el conflicto, se evalúa impacto y se tramita por control de cambios.

## 17. Estado y limitaciones

Este plan es una propuesta de dirección y secuenciación. No demuestra que las fases estén completadas. No implica pruebas ejecutadas, revisiones ISO, backtests, auditorías externas o validaciones matemáticas. Su aprobación no aprueba automáticamente requisitos, políticas, umbrales, modelos o contratos.

**Estado:** DRAFT / PENDING_REVIEW / NOT_APPROVED / NOT_IMPLEMENTED.
