# Arquitectura estratégica del Investment Intelligence Ecosystem

**Estado:** BASELINE ESTRATÉGICO APROBADO; especificación técnica pendiente  
**Versión:** 1.0.0  
**Fecha:** 2026-10-31  
**Alcance:** Arquitectura funcional de alto nivel; no constituye evidencia de implementación, validación empírica, certificación ni autorización de operación financiera.

## 1. Propósito

El Investment Intelligence Ecosystem (IIE) es un ecosistema integral para investigar criptoactivos, evaluar sus fundamentos y riesgos, estimar su valor, formular decisiones de inversión, gestionar exposiciones y validar sistemáticamente los resultados.

El IIE debe producir conclusiones reproducibles y auditables, o abstenerse cuando la evidencia sea insuficiente. No debe optimizar la apariencia de precisión ni convertir datos incompletos en recomendaciones.

## 2. Objetivo general

Construir un sistema independiente, reproducible, auditable y empíricamente validado que transforme datos admisibles sobre criptoactivos y mercados en análisis, valoraciones y decisiones de inversión justificables, con control explícito de riesgos, incertidumbre, costes y limitaciones.

## 3. Arquitectura estratégica: cuatro capas

### Capa 1 — INTELLIGENCE

**Misión:** establecer una base de conocimiento fiable y temporalmente correcta sobre activos, proyectos, redes, instrumentos y mercados.

Capacidades:
- Resolución de identidad y relaciones entre activos, representaciones, redes, contratos, wrappers, bridges, migraciones, forks, instrumentos y listings.
- Adquisición y normalización de datos desde fuentes admisibles.
- Control de licencias, permisos, procedencia, cobertura, calidad, frescura y linaje point-in-time.
- Análisis de mercado: precio, volumen, liquidez, microestructura, volatilidad, ciclos y regímenes.
- Análisis fundamental: economía del protocolo, tokenomics, oferta, demanda, uso, adopción, seguridad, gobernanza y sostenibilidad.
- Valoración mediante modelos diferenciados por tipología, con supuestos, incertidumbre y límites explícitos.

Salidas contractuales:
- Identidad resuelta o estado explícito de incertidumbre.
- Observaciones y conjuntos de datos versionados, con provenance y timestamps.
- Perfil de mercado, perfil fundamental, evaluación de calidad y expediente de valoración.
- Alertas de cobertura insuficiente, conflicto, retraso o inadmisibilidad.

### Capa 2 — DECISION

**Misión:** convertir la inteligencia validada en tesis y decisiones justificadas, sin ocultar incertidumbre ni riesgos no compensatorios.

Capacidades:
- Elegibilidad del activo para una finalidad y fecha determinadas.
- Filtros de exclusión y vetos de riesgo no compensatorios.
- Evaluación de oportunidad frente a valor razonable, precio, horizonte y escenarios.
- Síntesis de evidencia favorable, evidencia contraria, hipótesis y catalizadores.
- Clasificación/ranking solo cuando sus criterios estén especificados y validados.
- Estados de decisión explícitos: elegible, no elegible, oportunidad condicionada, esperar, rechazar o abstenerse.
- Registro de razones, sensibilidad, incertidumbre, condiciones de invalidación y caducidad de la tesis.

Salidas contractuales:
- Expediente de decisión versionado y reproducible.
- Tesis de inversión y contra-tesis.
- Riesgos bloqueantes, condiciones, horizonte y evidencia pendiente.
- Recomendación metodológica o abstención; nunca una señal opaca.

### Capa 3 — PORTFOLIO & EXECUTION

**Misión:** traducir decisiones aprobadas en exposiciones de cartera y operaciones controladas, respetando restricciones y costes.

Capacidades:
- Construcción y seguimiento de carteras.
- Asignación de capital, diversificación y límites de concentración.
- Presupuestos de riesgo, exposición por activo, red, protocolo, contraparte y factor.
- Rebalanceo y análisis de escenarios.
- Modelado de liquidez, slippage, comisiones, impacto de mercado, latencia y restricciones de venue.
- Preparación, autorización, registro y conciliación de órdenes.
- Controles pre-trade, post-trade y mecanismos de parada.
- Separación entre análisis, decisión, autorización humana y ejecución.

Salidas contractuales:
- Propuesta de cartera con restricciones, supuestos y riesgo estimado.
- Plan de órdenes o simulación, costes y límites.
- Registro de autorizaciones, operaciones, fills, conciliación y excepciones.
- Estado de exposición y cumplimiento de límites.

**Restricción de despliegue:** esta capa no implica habilitar trading autónomo. La ejecución real requiere controles de seguridad, permisos, pruebas, aprobación explícita y cumplimiento legal aplicable. La simulación debe preceder a cualquier operación real.

### Capa 4 — VALIDATION & LEARNING

**Misión:** comprobar si los datos, modelos, decisiones y procesos funcionan, detectar fallos y gobernar su evolución.

Capacidades:
- Pruebas unitarias, integración, contrato, conformidad y regresión.
- Backtesting point-in-time, walk-forward y out-of-sample.
- Control de look-ahead bias, survivorship bias, leakage y selección retrospectiva.
- Comparación con benchmarks pertinentes y estrategias simples de referencia.
- Inclusión de comisiones, slippage, liquidez, restricciones y capacidad.
- Evaluación de rendimiento y riesgo: retornos netos, drawdown, volatilidad, exposición, colas y estabilidad por régimen.
- Calibración, sensibilidad, incertidumbre, robustez y análisis de fallos.
- Monitorización de drift, degradación, incidentes y cambios de régimen.
- Registro de predicciones, decisiones, resultados y revisiones.
- Revisión independiente y proceso formal de aprobación, retirada o reversión de modelos.

Salidas contractuales:
- Informes de validación con dataset, periodo, versión, metodología y resultados reproducibles.
- Evidencia de pruebas y controles.
- Dictamen de aprobación, aprobación condicionada o rechazo.
- Registro de incidentes, desviaciones, cambios y lecciones aprendidas.

## 4. Capacidades transversales obligatorias

Las cuatro capas comparten estos controles:

1. **Gobernanza:** roles, separación de funciones, revisiones, aprobaciones, gestión de conflictos y control de cambios.
2. **Datos y evidencia:** provenance, point-in-time, licencias, calidad, retención, linaje y reproducibilidad.
3. **Seguridad:** gestión de secretos, mínimo privilegio, protección de credenciales, seguridad de cadena de suministro y respuesta a incidentes.
4. **Arquitectura y software:** contratos versionados, APIs explícitas, modularidad, CI, pruebas, observabilidad y despliegues reproducibles.
5. **Auditoría:** logs inmutables o protegidos contra alteración, registro de decisiones y capacidad de reconstruir resultados.
6. **Incertidumbre:** distinción entre hecho, estimación, hipótesis y opinión; UNKNOWN/BLOCKED/INCONCLUSIVE no se convierten en PASS.
7. **Cumplimiento:** revisión de licencias, privacidad, jurisdicción y obligaciones aplicables antes de habilitar datos u operaciones.

## 5. Flujo lógico e interfaces

Flujo de referencia:

1. INTELLIGENCE publica datos, perfiles y valoraciones con contratos, timestamps, procedencia y estado de calidad.
2. DECISION consume únicamente entradas admisibles y versionadas; registra las versiones exactas utilizadas.
3. PORTFOLIO & EXECUTION consume decisiones vigentes y límites de riesgo; separa propuesta, autorización y ejecución.
4. VALIDATION & LEARNING observa las cuatro capas, reconstruye resultados y emite dictámenes que pueden bloquear, condicionar o retirar componentes.
5. Los resultados de validación alimentan cambios controlados; nunca modifican silenciosamente decisiones o modelos históricos.

Ninguna capa debe depender de campos ambiguos o semánticas implícitas. Los contratos técnicos, esquemas, APIs, eventos, claves y estados se definirán en especificaciones posteriores.

## 6. Principios no negociables

- Evidencia antes que opiniones.
- Identidad correcta antes que análisis.
- Admisibilidad y calidad de datos antes que modelado.
- Riesgo antes que rentabilidad esperada.
- Vetos críticos no compensables por puntuaciones.
- Abstención antes que una conclusión injustificada.
- Validación fuera de muestra antes de producción.
- Costes y restricciones reales antes de atribuir rendimiento.
- Reproducibilidad antes que resultados extraordinarios.
- Revisión independiente antes de declarar conformidad de alto impacto.
- Ningún estado IMPLEMENTED, TESTED o EMPIRICALLY_VALIDATED sin evidencia correspondiente.

## 7. Indicadores de éxito

Los indicadores y umbrales numéricos se aprobarán en especificaciones separadas. Como mínimo, el sistema deberá medir:
- Cobertura, calidad, frescura y porcentaje de datos admisibles.
- Tasa de identidad resuelta, ambigüedad y falsos emparejamientos.
- Error, sesgo, estabilidad e incertidumbre de valoración por tipología.
- Calidad de calibración y capacidad predictiva fuera de muestra.
- Rendimiento neto de costes frente a benchmarks, drawdown y riesgo de cola.
- Rotación, capacidad, slippage e impacto de mercado.
- Estabilidad por régimen y sensibilidad a supuestos.
- Reproducibilidad de análisis y decisiones.
- Incidentes, fallos de control, drift y tiempo de detección/resolución.
- Porcentaje de requisitos con evidencia aprobada y trazabilidad completa.

No se fijan objetivos de rentabilidad garantizada ni umbrales sin datos, justificación y validación.

## 8. Fases de implementación

- **Fase 0 — Fundamentos:** cerrar especificaciones de identidad, universo, datos, admisibilidad, gobernanza y contratos.
- **Fase 1 — Base verificable:** CI, calidad de software, almacenamiento versionado, linaje, observabilidad y harness de pruebas.
- **Fase 2 — Intelligence:** construir y validar adaptadores de datos, análisis de mercado/fundamental y modelos de valoración.
- **Fase 3 — Decision:** implementar elegibilidad, vetos, tesis, estados de decisión y trazabilidad.
- **Fase 4 — Portfolio & Execution:** simulación de cartera, límites, costes y controles; operación real solo tras aprobación específica.
- **Fase 5 — Validation & Learning:** validación histórica y prospectiva, monitorización, revisión independiente y mejora gobernada.

Las fases son dependencias de entrega, no una afirmación de que las capacidades estén implementadas. Validation & Learning es transversal desde el inicio, aunque su capacidad empírica madura a medida que existan componentes y datos.

## 9. Exclusiones y límites

Esta arquitectura no:
- Prescribe una fórmula universal de valoración ni un ranking único.
- Define pesos, umbrales, probabilidades o señales de compra/venta.
- Garantiza rentabilidad ni elimina el riesgo.
- Autoriza trading autónomo ni custodia de activos.
- Declara cumplimiento regulatorio, certificación ISO o validación independiente.
- Convierte documentos, prototipos o fixtures sintéticos en evidencia de producción.

## 10. Relación con la línea base existente

Este documento establece el mapa estratégico del producto. No sustituye la carta metodológica, los requisitos IIM, los contratos de identidad/universo/datos, las políticas de riesgo, las especificaciones de valoración ni el protocolo de validación. Las correspondencias requisito-capa y los criterios técnicos de aceptación deberán incorporarse a la matriz de trazabilidad en una revisión posterior.

## 11. Criterios para considerar aprobada esta línea base

- Las cuatro capas y sus responsabilidades están identificadas sin solapamientos críticos.
- Cada capa define misión, capacidades, entradas/salidas y límites.
- Los controles transversales aplican al ciclo completo.
- Se distingue arquitectura estratégica de implementación y validación.
- Las fases respetan dependencias y no autorizan producción prematura.
- Toda especificación técnica posterior conserva trazabilidad con esta arquitectura.

**Decisión:** la arquitectura de cuatro capas queda adoptada como línea base estratégica del IIE. Cualquier cambio material de alcance requiere una decisión documentada y revisión de impacto.
