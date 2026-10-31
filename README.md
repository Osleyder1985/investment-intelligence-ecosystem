# Investment Intelligence Ecosystem

**Versión de metodología:** 0.1.0  
**Estado:** En desarrollo; no validada ni certificada por ISO.  
**Idioma documental:** Español. Los nombres de archivos y directorios se mantienen en inglés.

## Propósito

Desarrollar una metodología reproducible, basada en evidencia y sometida a validación empírica para investigar criptoactivos y evaluar si presentan una oportunidad de inversión. El sistema debe distinguir explícitamente entre precio nominal bajo, valor razonable estimado e infravaloración.

La metodología apoya la toma de decisiones; no garantiza resultados ni constituye asesoramiento financiero personalizado.

## Principios obligatorios

- No inventar datos, métricas, umbrales, probabilidades, causalidad ni resultados.
- Distinguir hechos, estimaciones, hipótesis y opiniones.
- Registrar procedencia, fecha, cobertura, calidad y limitaciones de cada dato.
- Tratar los datos ausentes como desconocidos; nunca convertirlos silenciosamente en cero.
- Separar correlación temporal de causalidad demostrada.
- Incluir riesgos críticos y criterios de exclusión independientes de cualquier puntuación.
- Evitar look-ahead bias, survivorship bias y leakage en validación.
- Validar con pruebas walk-forward y out-of-sample, incluyendo costes y restricciones operativas.
- Mantener historial, versiones, decisiones y evidencia auditables.
- El usuario decide cuándo ejecutar una evaluación.

## Estructura documental

- `docs/methodology/`: carta y capítulos de la metodología.
- `docs/standards/`: registro de normas y estado de verificación.
- `docs/governance/`: políticas de evidencia y control.
- `docs/asset-selection/`: elegibilidad y selección del universo.
- `docs/valuation/`: valoración.
- `docs/undervaluation/`: detección de infravaloración.
- `docs/validation/`: validación empírica.
- `requirements/`: requisitos trazables.
- `tests/identity_fixtures/`: primera suite offline de fixtures sintéticos EVM/Solana; alcance limitado y pendiente de revisión independiente.
- `models/`, `evidence/`, `templates/`, `validation/`: artefactos futuros.

## Referencias normativas

Las normas ISO/IEC relacionadas pueden aportar marcos para gestión de riesgos, seguridad, arquitectura y terminología DLT. **Ninguna de ellas prescribe por sí misma una fórmula universal para seleccionar criptomonedas ni certifica esta metodología.** La vigencia, edición, alcance y aplicabilidad de cada referencia deben verificarse en fuentes oficiales antes de aprobarla.

## Gobernanza

Los cambios se proponen mediante Issues y Pull Requests. Cada Issue debe describir problema, causa, solución propuesta, evidencia y criterios de aceptación. Ningún cambio metodológico se considera aprobado solo por haber sido redactado: requiere revisión, trazabilidad y pruebas apropiadas.

## Estado

Esta versión establece la estructura inicial. Los criterios, modelos, pesos, umbrales y reglas de decisión permanecen pendientes de especificación y validación; no deben tratarse como aprobados.
