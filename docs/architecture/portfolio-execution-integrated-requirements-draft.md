# Borrador integrado de requisitos Portfolio & Execution

**Versión:** 0.2.0-draft  
**Estado:** NOT APPROVED / PENDING_REVIEW  
**Fecha:** 2026-10-31

Este documento consolida las propuestas IIM-025 a IIM-037 y los hallazgos INT-01 a INT-11. Es un borrador para revisión humana. No modifica el registro normativo, no demuestra implementación ni pruebas y no habilita uso operativo.

## Principios de integración

1. Mandato, política de riesgo, autorización y enforcement son conceptos distintos.
2. Medición de exposición, medición de riesgo, análisis de estrés y aplicación de límites tienen responsabilidades separadas.
3. Asignación inicial y rebalanceo comparten un contrato de propuesta, pero conservan sus disparadores específicos.
4. Estimaciones no se confunden con resultados observados.
5. Elegibilidad de terceros y seguridad técnica requieren decisiones independientes.
6. Ciclo de órdenes, controles y conciliación son dominios separados.
7. UNKNOWN, STALE, INVALID y CONFLICT no equivalen a PASS ni a cero.
8. Cada decisión y transición conserva identidad, versiones, tiempos y evidencia.

## 1. IIM-025 — Mandato de inversión

Definir mandatos versionados por cartera y subcartera con autoridad aprobadora, objetivos, horizonte, moneda de referencia, universo elegible, benchmark cuando aplique, necesidades de liquidez, restricciones, facultades, jurisdicción y vigencia. Cada propuesta debe referenciar una versión vigente e inmutable. Mandatos ausentes, ambiguos, vencidos o revocados bloquean nuevas propuestas. El mandato no constituye autorización para una acción operativa concreta.

## 2. IIM-026 — Construcción y asignación

Generar propuestas reproducibles a partir del mandato, posiciones, universo, exposición, riesgo, liquidez, costes y restricciones aprobadas. Declarar método, entradas, supuestos, factibilidad, sensibilidad, alternativas y limitaciones. Abstenerse cuando los datos o restricciones no permitan una solución defendible. La salida no autoriza ni ejecuta acciones.

## 3. IIM-027 — Exposición y concentración

Calcular snapshots reproducibles de exposiciones directas e indirectas mediante taxonomía versionada. Cada métrica declara unidad, denominador, moneda, timestamp, fuente, método de agregación, cobertura, look-through e incertidumbre. Exposiciones desconocidas permanecen explícitas; nunca se convierten en cero.

## 4. IIM-028 — Presupuestos y límites de riesgo

Aplicar políticas versionadas de apetito, presupuestos y límites, distinguiendo métricas, alertas, límites duros y excepciones autorizadas. Evaluar antes y después de cambios de cartera. Incumplimientos, datos inválidos o cobertura insuficiente bloquean la decisión afectada. Las excepciones requieren autoridad, alcance, vigencia, justificación y evidencia, y no anulan vetos no dispensables.

## 5. IIM-029 — Liquidez y coste

Estimar liquidez y coste esperado por instrumento, venue, lado, tamaño y horizonte. Declarar spread, profundidad, comisiones, slippage, impacto, gas o price impact cuando apliquen, además de fuente, timestamp, supuestos, cobertura e incertidumbre. El volumen reportado no demuestra capacidad ejecutable; una estimación no es un coste realizado.
