# Registro de requisitos

| ID | Requisito | Prioridad | Estado | Evidencia de aceptación |
|---|---|---|---|---|
| IIM-001 | Identificar inequívocamente cada criptoactivo y su red/contrato | Crítica | Propuesto | Pruebas de resolución de identidad y colisiones de ticker |
| IIM-002 | Registrar procedencia, licencia, cobertura y calidad de los datos | Crítica | Propuesto | Auditoría de linaje reproducible |
| IIM-003 | Diferenciar precio bajo de infravaloración | Crítica | Propuesto | Casos de prueba con supply y valoraciones diferentes |
| IIM-004 | Estimar valor mediante métodos adecuados a la tipología | Alta | Propuesto | Revisión metodológica y validación |
| IIM-005 | Registrar hipótesis, alternativas y contraevidencia | Alta | Propuesto | Trazabilidad de cada conclusión |
| IIM-006 | Evaluar riesgos críticos con capacidad de veto | Crítica | Propuesto | Matriz de riesgos y pruebas de veto |
| IIM-007 | Evitar sesgos temporales y validar out-of-sample | Crítica | Propuesto | Protocolo walk-forward reproducible |
| IIM-008 | Mantener historial versionado de evaluaciones | Alta | Propuesto | Comparación de evaluaciones y auditoría |
| IIM-009 | No ejecutar análisis sin solicitud explícita del usuario | Alta | Propuesto | Prueba de control de ejecución |
| IIM-010 | Producir conclusión con nivel de evidencia e incertidumbre | Crítica | Propuesto | Casos de datos suficientes e insuficientes |
| IIM-011 | Clasificar criptoactivos por derechos, función y captura de valor, admitiendo tipologías híbridas | Crítica | Propuesto | Taxonomía versionada y casos de clasificación con evidencia |
| IIM-012 | Seleccionar métodos de valoración según el objeto y la tipología económica | Crítica | Propuesto | Catálogo y matriz de aplicabilidad con condiciones y contraejemplos |
| IIM-013 | Abstenerse de estimar valor razonable cuando falten derechos o evidencia suficientes | Crítica | Propuesto | Casos de prueba que exijan abstención y expliquen datos faltantes |
| IIM-014 | Clasificar la brecha entre precio ejecutable y rango de valor razonable con incertidumbre y costes explícitos | Crítica | Propuesto | Casos de prueba de rango, costes, liquidez y no aplicabilidad matemática |
| IIM-015 | Habilitar la categoría extrema solo tras calibración y validación temporal independiente | Crítica | Propuesto | Protocolo walk-forward/out-of-sample, control de sesgos y falsos descubrimientos |
| IIM-016 | Evaluar riesgos por tipología, evidencia, alcance, controles y riesgo residual | Crítica | Propuesto | Taxonomía y fichas trazables por activo y dominio |
| IIM-017 | Aplicar vetos críticos independientes de la valoración y con transiciones verificables | Crítica | Propuesto | Casos de prueba de activación, revisión, remediación y bloqueo de ranking |
| IIM-018 | Asegurar calidad, admisibilidad, linaje y disponibilidad point-in-time de cada dato utilizado | Crítica | Propuesto | Registro de fuentes, manifiestos reproducibles y pruebas de leakage, licencias y conflictos |
| IIM-019 | Resolver identidad canónica de activos, redes, contratos y representaciones sin depender del ticker | Crítica | Propuesto | Casos de colisión, wrapper, migración, fork y activo nativo resueltos con evidencia |
| IIM-020 | Construir universos versionados y screening por finalidad sin sesgo de supervivencia ni precio nominal | Crítica | Propuesto | Snapshots históricos, criterios trazables y pruebas de activos desaparecidos y supply desconocido |

Los requisitos no se consideran aprobados hasta su análisis, trazabilidad y aceptación formal.
