# IIE — Global Crypto Asset Selection Methodology

**Identificador:** IIM-MASTER-001  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT — integración metodológica; pendiente de revisión, aprobación, implementación y validación empírica.  
**Fecha:** 2026-10-31  
**Ámbito:** selección y evaluación comparativa de criptoactivos para inversión.  
**Autoridad:** subordinada a la arquitectura estratégica y a los requisitos normativos aprobados del IIE. Este documento no modifica por sí mismo el registro oficial de requisitos.

## 1. Propósito y resultado esperado

Esta especificación integra en un flujo coherente las políticas existentes de identidad, universo, elegibilidad, datos, análisis, valoración, riesgo, decisión, ranking, cartera y validación. Su finalidad es determinar, para un activo y un instante de decisión concretos, si existe evidencia suficiente para considerarlo una candidatura de inversión dentro de un mandato, horizonte, jurisdicción, tamaño y ruta de mercado definidos.

La metodología debe producir una de estas salidas defendibles: continuar investigación, elegible para una finalidad específica, candidato condicionado, oportunidad respaldada por evidencia, no elegible, bloqueado por riesgo o abstención. No promete rentabilidad, no identifica infaliblemente los ganadores futuros y no sustituye la decisión del inversor.

**Distinción esencial:** selección de activos, valoración, oportunidad de entrada y asignación de cartera son decisiones relacionadas, pero distintas. Un activo de alta calidad puede estar sobrevalorado; un activo aparentemente infravalorado puede ser invendible; y una oportunidad individual puede no ser adecuada para una cartera concreta.

## 2. Principios no negociables

1. **Evidence-first:** toda conclusión material debe enlazar evidencia, fuente, timestamp, alcance y limitaciones.
2. **Identity-before-analysis:** no analizar ni operar una representación cuya identidad no esté resuelta para la finalidad correspondiente.
3. **Point-in-time correctness:** solo utilizar información disponible en el instante de decisión; conservar tiempo del evento, publicación, observación y procesamiento.
4. **Purpose-specific eligibility:** la elegibilidad para investigación no implica elegibilidad para valoración, comparación, custodia u operación.
5. **Risk non-compensation:** los vetos críticos no pueden compensarse mediante score, descuento, momentum o rentabilidad esperada.
6. **Unknown is not zero:** datos ausentes, contradictorios, obsoletos o no admisibles permanecen explícitamente desconocidos.
7. **No forced valuation:** no estimar valor cuando el objeto, los derechos o la evidencia no soportan el modelo.
8. **No universal score:** no imponer una fórmula común a tipologías económicas, horizontes o finalidades no comparables.
9. **Uncertainty disclosure:** distinguir hechos, estimaciones, hipótesis, escenarios y opiniones; reportar intervalos y sensibilidad cuando proceda.
10. **Abstention is a valid result:** la metodología puede detenerse sin producir ranking ni recomendación.
11. **Net-of-costs discipline:** evaluar precios ejecutables, liquidez, spreads, comisiones, slippage, impacto y restricciones.
12. **Reproducibility:** cada resultado debe poder reconstruirse con el universo, datos, código, parámetros y versiones originales.
13. **No hindsight:** cambios metodológicos posteriores no pueden reescribir decisiones históricas.
14. **No performance claims without validation:** ningún modelo se declara eficaz por resultados in-sample o ejemplos seleccionados.

## 3. Objeto de evaluación y contexto obligatorio

La unidad de evaluación es:

`Evaluation = (asset_identity, representation, purpose, decision_time, horizon, mandate, market_scope, methodology_version)`

Cada evaluación debe fijar como mínimo:

- Identidad económica del activo, red, contrato y representación.
- Finalidad: DISCOVERY, RESEARCH, VALUATION, COMPARISON, PORTFOLIO o EXECUTION_ROUTE.
- Timestamp de decisión UTC y snapshot del universo.
- Horizonte de inversión y fecha de vencimiento/revisión de la tesis.
- Mandato, restricciones, moneda base y jurisdicción cuando sean relevantes.
- Tamaño o rango de tamaño para cualquier conclusión de liquidez/ejecución.
- Mercados, pares, venues y rutas incluidos.
- Versión metodológica, modelos, políticas, código y datasets utilizados.

Si un parámetro contextual es necesario y no está disponible, la evaluación debe limitarse o abstenerse; no debe inventarlo.

## 4. Universo y tipología

### 4.1 Construcción del universo

El universo debe ser versionado y reconstruible en cada fecha histórica. Incluir activos desaparecidos, fallidos, migrados, deslistados y redenominados en los estudios históricos cuando existan datos admisibles. Registrar criterios de inclusión, exclusión, cuarentena, cobertura y cambios.

No filtrar por precio nominal unitario como proxy de baratura. Market capitalization, FDV, volumen reportado y número de listings tampoco prueban por sí solos valor o invertibilidad.

### 4.2 Taxonomía económica

Clasificar por derechos, función, captura de valor, gobernanza, oferta, respaldo, redención, dependencias y comportamiento de mercado. Admitir etiquetas múltiples e híbridas. La clasificación jurídica/regulatoria es un análisis distinto y debe remitirse a revisión especializada.

Las familias de análisis pueden incluir, sin ser exhaustivas:
- activos monetarios/de reserva;
- activos nativos de red y consenso;
- tokens de utilidad/acceso;
- tokens de gobernanza;
- tokens con flujos o captura de comisiones;
- staking/restaking y recibos líquidos;
- stablecoins y activos referenciados;
- RWA y derechos sobre activos;
- tokens de pool, liquidez o depósito;
- derivados y sintéticos;
- NFT y derechos digitales singulares;
- activos especulativos/comunitarios.

La categoría no demuestra que exista un modelo de valoración aplicable. Un protocolo exitoso no implica que su token capture el valor económico generado.

## 5. Flujo metodológico y gates

El flujo es secuencial. Los gates bloquean pasos dependientes cuando la evidencia previa no es admisible.

### G0 — Contexto y mandato

Verificar finalidad, horizonte, tamaño, restricciones, moneda base, jurisdicción y universo. Si el mandato no está definido, se puede realizar investigación general, pero no declarar idoneidad para una cartera concreta.

### G1 — Identidad y representación

Resolver activo, red, contrato, estándar, emisor, wrappers, bridges, proxies, migraciones, forks y relaciones de representación. Registrar conflictos y confianza. Identidad ambigua bloquea valoración dependiente de supply, precio, derechos u operación.

### G2 — Calidad y admisibilidad de datos

Por cada variable: fuente, licencia/permiso, cobertura, metodología, timestamp, frescura, integridad, revisiones, transformaciones, calidad y linaje. Contrastar fuentes independientes para variables críticas. Detectar wash trading, mercados inactivos, outliers, datos sintéticos, revisiones retrospectivas y conflictos.

Estados mínimos: ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN y STALE. Ningún estado se convierte implícitamente en PASS.

### G3 — Elegibilidad por finalidad e invertibilidad

Evaluar separadamente elegibilidad para investigación, valoración, comparación histórica, custodia y ruta concreta de ejecución. Considerar profundidad ejecutable, spread, slippage esperado, impacto, límites de venue, restricciones de transferencia, concentración, disponibilidad de custodia, contraparte y jurisdicción.

La liquidez se evalúa para un tamaño, mercado, lado, horizonte y momento definidos. El volumen reportado no sustituye profundidad ejecutable. Un activo puede ser investigable y no invertible.

### G4 — Análisis de inteligencia

Ejecutar los módulos aplicables de las secciones 6 a 10. No todos los módulos son aplicables a todas las tipologías. La inaplicabilidad debe justificarse; la falta de datos no equivale a inaplicabilidad.

### G5 — Riesgos y vetos

Evaluar riesgos materiales antes de emitir una conclusión favorable. Aplicar la precedencia de estados de veto y conservar alcance, evidencia, controles y transiciones. VETO_ACTIVE bloquea la candidatura en el alcance afectado. VETO_REVIEW_REQUIRED y RISK_UNKNOWN impiden una conclusión favorable no condicionada.

### G6 — Valoración y oportunidad

Estimar valor solo si el objeto y el método son aplicables. Comparar precio ejecutable con rango de valor razonable, incorporando costes, incertidumbre y horizonte. No confundir calidad del activo con atractivo del precio.

### G7 — Decisión y comparación

Emitir estado, razones, contraevidencia, incertidumbre, condiciones e invalidación. Comparar solo activos suficientemente comparables. El ranking puede ser parcial; no forzar orden total ni score compuesto sin validación.

### G8 — Cartera y autorización

Solo después de una decisión vigente y de comprobar el mandato, evaluar contribución marginal, concentración, correlaciones, exposición común, presupuesto de riesgo y costes. Una propuesta de cartera no es autorización de trading. Ejecución real requiere controles, permisos y aprobación explícita independientes.

### G9 — Seguimiento y reevaluación

Vigilar cambios de identidad, datos, fundamentos, tokenomics, seguridad, gobernanza, liquidez, valoración, riesgos y condiciones de tesis. La reevaluación debe crear un nuevo snapshot, no sobrescribir el anterior.

## 6. Análisis fundamental y económico

El expediente fundamental debe cubrir, según aplicabilidad:

- Problema atendido, producto/servicio, propuesta de valor y alternativas.
- Usuarios, actividad, retención, crecimiento, concentración y calidad de adopción.
- Uso económico de la red/protocolo y distinción entre actividad orgánica, incentivos y actividad posiblemente artificial.
- Comisiones, ingresos, costes, tesorería, pasivos, subvenciones y sostenibilidad.
- Dependencias de entidades, proveedores, oráculos, custodios, puentes y validadores.
- Ventaja competitiva, efectos de red, sustitutos, barreras y riesgo de obsolescencia.
- Calidad de divulgación, gobernanza y capacidad de ejecución del equipo cuando sea verificable.

Separar métricas de protocolo de beneficios atribuibles al titular del token. TVL, usuarios, transacciones, comisiones e ingresos no deben interpretarse aisladamente ni sumarse sin controlar doble conteo, incentivos y composición.

## 7. Tokenomics, derechos y captura de valor

Documentar:
- Supply circulante, total, máximo o ilimitado, con definiciones y fuentes.
- Emisión, quema, rebase, vesting, desbloqueos, tesorería y asignaciones.
- Concentración de tenedores/control, con límites de atribución de direcciones.
- Derechos exigibles, redención, gobernanza y restricciones.
- Mecanismos de demanda del token y necesidad de mantenerlo.
- Captura de comisiones/ingresos, buybacks, burns, staking y distribución.
- Dilución, inflación neta, incentivos, lockups, slashing y colas de salida.
- Cambios de contrato o gobernanza que puedan modificar las reglas.

No inferir derechos a partir de marketing. No atribuir ingresos del protocolo al token sin mecanismo verificable y análisis de su continuidad, prioridad y exigibilidad. Rendimiento nominal de staking no equivale a retorno económico neto.

## 8. Análisis tecnológico, seguridad y gobernanza

Evaluar arquitectura, consenso, descentralización efectiva, validadores, concentración de poder, upgrades, permisos administrativos, multisig, timelocks, dependencias críticas, historial de incidentes, auditorías y alcance exacto de estas. Incluir smart contracts, bridges, oráculos, custodios, MEV, disponibilidad, finality/reorg, seguridad operacional y procesos de respuesta.

La existencia de auditoría no garantiza seguridad. Deben verificarse versión auditada, alcance, fecha, hallazgos, remediaciones y diferencias respecto del código desplegado.

## 9. Análisis de mercado y cuantitativo

Cuando existan datos admisibles, evaluar:
- Estructura de tendencia, momentum y reversión.
- Volatilidad realizada e implícita cuando sea fiable.
- Profundidad, spreads, impacto, liquidez por venue y concentración.
- Correlación, beta, exposición a factores y dependencia del régimen.
- Flujos, derivados, funding, open interest, liquidaciones y basis.
- Actividad on-chain y flujos entre entidades/venues con cautela de atribución.
- Catalizadores, eventos, desbloqueos, cambios de protocolo y riesgo de evento.

Toda señal debe especificar definición, ventana, timestamp, universo, transformación, hipótesis, costes, limitaciones y prueba de robustez. Evitar indicadores redundantes y data mining. Una señal técnica no prueba valor fundamental ni garantiza persistencia.

## 10. Valoración por tipología

Seleccionar métodos según el objeto económico y los derechos demostrados. Métodos posibles, sujetos a aplicabilidad: NAV/redención, flujos atribuibles, comparables, utilidad/demanda, escenarios monetarios, paridad, liquidación/recuperación y coste de producción como dato contextual.

Para cada modelo registrar:
- objeto valorado (token, derecho, posición, protocolo o colateral);
- justificación de aplicabilidad;
- variables, fuentes y timestamp;
- supuestos, ecuaciones, parámetros y versión;
- escenarios, sensibilidad, incertidumbre e intervalos;
- riesgos de doble conteo y limitaciones;
- criterios de rechazo y abstención.

No forzar DCF a activos sin flujos atribuibles; no atribuir NAV de tesorería a tokens de gobernanza sin derechos; no tratar coste de producción como suelo de precio; no usar paridad objetivo como prueba de solvencia. En activos híbridos, separar componentes y evitar sumar valores superpuestos.

## 11. Evaluación integral de riesgos

Como mínimo considerar:
- mercado, volatilidad, colas, gaps y correlación;
- liquidez, concentración de venues y capacidad;
- tecnología, smart contracts, bridges, oráculos y upgrades;
- consenso, validadores, gobernanza y captura de control;
- contraparte, custodia, reservas, redención y segregación;
- tokenomics, dilución, unlocks y concentración;
- legal, jurisdicción, sanciones y restricciones;
- operacional, datos, proveedor, ciberseguridad y continuidad;
- modelo, incertidumbre, error de valoración y dependencia de supuestos;
- reputación, fraude, manipulación y conflictos de interés.

Cada riesgo debe declarar escenario, probabilidad solo si estimable, impacto, horizonte, evidencia, controles, riesgo residual, alcance y responsable/revisor. No convertir escalas ordinales en probabilidades o puntuaciones cardinales sin fundamento.

## 12. Decisión, estados y abstención

Los estados de salida deben distinguir al menos:
- RESEARCH_ONLY: admisible para investigación, no implica invertibilidad.
- ELIGIBLE: elegible para la finalidad declarada.
- ELIGIBLE_WITH_LIMITATIONS: continúa con restricciones explícitas.
- WATCHLIST: requiere evento, precio o evidencia adicional.
- CANDIDATE: cumple gates para evaluación de oportunidad.
- OPPORTUNITY_SUPPORTED: evidencia suficiente respalda oportunidad bajo versión, horizonte y contexto definidos; no es orden.
- OVERVALUED_UNDER_MODEL: precio supera rango estimado bajo el modelo aplicable.
- INELIGIBLE: falla criterio específico de elegibilidad.
- VETOED: existe veto activo dentro del alcance.
- REVIEW_REQUIRED: conflicto o alerta material sin resolver.
- ABSTAIN: evidencia insuficiente, no comparable o modelo no aplicable.

La máquina de estados, precedencias y transiciones deberán formalizarse en contrato. No usar estos nombres como estados operativos hasta su revisión y aprobación.

Toda decisión debe contener tesis, contra-tesis, hechos, estimaciones, hipótesis, evidencia favorable/contraria, gates, vetos, incertidumbre, escenarios, sensibilidad, condiciones de entrada/salida si aplican, invalidación, caducidad y preguntas abiertas.

## 13. Ranking y scoring

La línea base es ranking parcial por tipología, horizonte, snapshot y finalidad. Primero se agrupan bloqueados, revisión requerida, información insuficiente, no comparables, candidatos condicionales y oportunidades respaldadas. La dominancia requiere dimensiones comparables y evidencia robusta.

Un score compuesto solo podrá habilitarse mediante una propuesta metodológica separada que:
1. defina constructo y resultado objetivo;
2. justifique variables, dirección, normalización, pesos y tratamiento de faltantes;
3. mida correlación, redundancia, dependencia y estabilidad;
4. controle outliers, datos manipulados y selección múltiple;
5. congele el modelo antes del test independiente;
6. valide temporalmente por tipología y régimen;
7. compare con benchmarks y reglas simples;
8. incluya costes, liquidez y capacidad;
9. publique sensibilidad, fallos, incertidumbre y límites;
10. tenga revisión independiente y monitorización de drift.

No se habilitan pesos, umbrales, scores universales ni probabilidades en esta versión.

## 14. Oportunidad, horizonte y condiciones de tesis

La oportunidad requiere separar:
- calidad intrínseca del activo;
- precio frente al rango de valor estimado;
- margen de seguridad e incertidumbre;
- catalizadores y riesgos de evento;
- liquidez y coste para el tamaño previsto;
- horizonte y condiciones de invalidación.

Las condiciones de entrada, mantenimiento, reducción o salida deben ser explícitas y comprobables. No emitir un precio objetivo puntual sin intervalo, supuestos y fecha de validez. La categoría de infravaloración extrema permanece deshabilitada hasta calibración y validación independiente conforme al protocolo específico.

## 15. Cartera y ejecución

La selección individual no determina automáticamente el peso de cartera. La construcción debe considerar mandato, presupuesto de riesgo, exposición por activo/red/protocolo/venue/factor, concentración, correlación, liquidez, costes, escenarios de estrés y restricciones. El rebalanceo debe comparar beneficio esperado con costes, impuestos aplicables y riesgos operativos.

Separar inequívocamente propuesta de cartera, autorización humana, decisión de control de trading, ciclo de vida de orden, fills y conciliación. Esta metodología no autoriza ejecución real ni custodia; cualquier entorno live requiere aprobación específica, seguridad y cumplimiento aplicable.

## 16. Validación científica y protocolo de evidencia

La validación debe ser temporal, point-in-time y survivorship-aware. Como mínimo:
- definir hipótesis, objetivo, población, unidad experimental y horizonte antes de analizar resultados;
- reconstruir universos históricos e incluir activos fallidos/deslistados;
- separar desarrollo, calibración, validación y holdout final;
- ejecutar walk-forward sin leakage;
- controlar look-ahead, survivorship, selection bias, multiple testing y data snooping;
- incorporar comisiones, spread, slippage, impacto, liquidez, capacidad y no ejecución;
- comparar con benchmarks adecuados y reglas base simples;
- reportar retorno neto, drawdown, volatilidad, riesgo de cola, turnover, exposición, estabilidad y métricas de clasificación/calibración cuando correspondan;
- reportar tamaño de muestra, intervalos de incertidumbre, resultados negativos, exclusiones y análisis por tipología/régimen;
- preregistrar protocolo y conservar código, datos, parámetros, logs y hashes;
- reservar el holdout final y evitar su reutilización tras cambios.

La validación de una métrica no valida automáticamente la metodología completa. La validación in-sample no habilita producción. La revisión independiente debe examinar datos, diseño experimental, resultados y reproducibilidad.

## 17. Monitorización y control de cambios

Vigilar calidad/frescura de datos, drift de variables, calibración, rendimiento, riesgos, cambios de tokenomics, contratos, gobernanza, liquidez, venues y régimen de mercado. Definir alertas, responsables, severidad, respuesta, rollback y suspensión.

Cualquier cambio de universo, taxonomía, fuente, variable, modelo, peso, umbral, gate, horizonte o regla de decisión requiere:
- motivo y evidencia;
- análisis de impacto y compatibilidad;
- nueva versión;
- pruebas de regresión;
- evaluación de sesgo;
- aprobación según gobernanza;
- fecha efectiva y plan de reversión.

No reescribir evaluaciones ni resultados históricos con la metodología nueva.

## 18. Expediente mínimo de evaluación

Cada evaluación debe persistir:
- evaluation_id, asset_id y representación;
- purpose, mandate_id, horizon, decision_time y snapshot del universo;
- versiones de metodología, taxonomía, políticas, modelos y código;
- dataset manifest, provenance, timestamps y hashes;
- estados de calidad, cobertura y campos desconocidos;
- resultados por módulo, aplicabilidad y razones de abstención;
- riesgos, vetos y transiciones;
- valoración, escenarios, sensibilidad e incertidumbre;
- decisión, contraevidencia, ranking/dominancia y no comparabilidad;
- costes, liquidez y restricciones;
- aprobaciones/revisiones, usuario/rol y audit events;
- fecha de caducidad y condiciones de reevaluación.

## 19. Métricas de calidad metodológica

No se fijan umbrales numéricos en esta versión. Definir y medir, por tipología y horizonte:
- cobertura y admisibilidad de datos;
- errores de identidad y clasificación;
- calidad y estabilidad de métricas;
- error de valoración y cobertura de intervalos;
- calibración y capacidad predictiva;
- falsos positivos/negativos y cobertura de abstención;
- rendimiento neto y relativo a benchmarks;
- drawdown, riesgo de cola y estabilidad por régimen;
- turnover, costes, capacidad y slippage;
- reproducibilidad y completitud de trazabilidad;
- incidentes, drift y tiempos de detección/resolución.

La tasa de abstención no debe optimizarse aisladamente: una baja abstención puede ocultar exceso de confianza y una alta abstención puede reflejar falta de cobertura. Evaluar utilidad y error conjuntamente.

## 20. Trazabilidad con documentos existentes

Este documento integra y no reemplaza las especificaciones detalladas. La relación inicial es:

| Dominio | Documento de referencia |
|---|---|
| Arquitectura de cuatro capas | [Strategic architecture](../architecture/strategic-architecture.md) |
| Carta metodológica | [Methodology charter](00-methodology-charter.md) |
| Identidad y taxonomía | [Asset identity resolution](../asset-selection/asset-identity-resolution.md); [asset taxonomy](../asset-selection/asset-taxonomy.md) |
| Universo y screening | [Universe construction](../asset-selection/universe-construction-policy.md); [eligibility and screening](../asset-selection/eligibility-and-screening.md) |
| Calidad y linaje | [Data quality](../data/data-quality-policy.md); [point-in-time lineage](../data/data-lineage-and-point-in-time.md) |
| Evidencia | [Evidence policy](../governance/evidence-policy.md) |
| Valoración | [Valuation catalog](../valuation/valuation-method-catalog.md); [applicability matrix](../valuation/valuation-applicability-matrix.md); [model specifications](../valuation/model-specifications-by-typology.md) |
| Infravaloración | [Decision framework](../undervaluation/decision-framework.md); [calibration protocol](../validation/undervaluation-calibration-protocol.md) |
| Riesgo | [Risk taxonomy](../risk/risk-taxonomy.md); [veto policy](../risk/veto-policy.md) |
| Decisión y ranking | [Decision gates](../decision/decision-gates-and-outcomes.md); [ranking and dominance](../decision/ranking-and-dominance-policy.md) |
| Reporting e historial | [Evaluation report contract](../reporting/evaluation-report-contract.md); [snapshot/versioning](../history/evaluation-snapshot-and-versioning.md) |
| Workflow | [Evaluation workflow](../execution/evaluation-workflow.md) |
| Portfolio & Execution | [Integrated requirements draft](../architecture/portfolio-execution-integrated-requirements-draft.md) |

La trazabilidad normativa debe completarse contra IIM-001 a IIM-024 y los requisitos de Portfolio & Execution propuestos IIM-025 a IIM-037. Esta tabla no cambia el estado de aprobación de esos requisitos.

## 21. Brechas que bloquean la aprobación

Antes de promover esta especificación:
1. Resolver contradicciones y solapamientos con los documentos de dominio.
2. Formalizar estados, precedencias, transiciones y contratos de decisión.
3. Definir matriz de aplicabilidad de módulos por tipología y finalidad.
4. Establecer metodología de selección de fuentes y expedientes de licencia/permisos.
5. Definir protocolos concretos para liquidez ejecutable, manipulación y concentración.
6. Completar revisión de riesgos por expertos de dominios y jurisdicciones pertinentes.
7. Definir diseño de experimentos, benchmarks y criterios estadísticos antes de observar resultados.
8. Completar trazabilidad requisito → sección → contrato → componente → test → evidencia.
9. Ejecutar revisión independiente y registrar decisiones/disensos.
10. Implementar y probar el flujo end-to-end antes de cualquier declaración de capacidad operativa.

## 22. Criterios de aceptación de esta versión

La versión 0.1.0-draft solo podrá pasar a revisión formal cuando:
- todas las etapas y gates tengan entradas, salidas, responsables y reglas de parada;
- cada módulo indique aplicabilidad, evidencia mínima y abstención;
- los estados de elegibilidad, riesgo y decisión no sean contradictorios;
- el ranking parcial y las condiciones para habilitar scores estén definidos;
- los modelos de valoración se seleccionen por objeto/derechos y no por conveniencia;
- los costes y restricciones estén incorporados a oportunidad y validación;
- el expediente sea reconstruible point-in-time;
- la matriz de trazabilidad esté completa y sin requisitos huérfanos;
- la revisión independiente no tenga hallazgos críticos abiertos.

La aprobación documental no implica implementación ni eficacia empírica. Para declarar EMPIRICALLY_VALIDATED se requerirá evidencia de experimentos independientes y reproducibles conforme a protocolo aprobado.

## 23. Estado y limitaciones

**Estado actual:** DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_EMPIRICALLY_VALIDATED.

No se asignan pesos, umbrales, probabilidades, objetivos de rentabilidad ni rankings de activos. No se autoriza trading autónomo. No se afirma certificación ISO, conformidad regulatoria ni reconocimiento externo. Esta especificación es una línea de integración para revisión y evolución controlada.
