# Registro de revisión y aprobación de especificaciones IIM

**Estado del artefacto:** plantilla de gobernanza; no constituye una aprobación.  
**Issue relacionado:** [#19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19)

## 1. Propósito

Registrar de forma auditable la revisión metodológica de cada especificación. La integración de un documento en Git, la existencia de casos de prueba o la revisión editorial no equivalen por sí solas a aprobación metodológica.

Debe completarse un registro independiente por requisito o por conjunto de requisitos cuando compartan exactamente el mismo alcance y decisión.

## 2. Ficha de revisión

| Campo | Valor |
|---|---|
| requirement_id | Identificador IIM afectado |
| specification_title | Nombre del documento/contrato revisado |
| specification_path | Ruta versionada |
| specification_version | Versión exacta revisada |
| reviewed_commit | Commit cuyo contenido fue revisado |
| review_scope | Alcance y usos cubiertos |
| reviewer | Identificador/rol del revisor |
| review_date_utc | Fecha y hora de revisión en UTC |
| decision | APPROVED / APPROVED_WITH_LIMITATIONS / REVISION_REQUIRED / BLOCKED / NOT_REVIEWED |
| rationale | Fundamento de la decisión |
| evidence_refs | Fuentes, pruebas, decisiones y documentos examinados |
| unresolved_items | Cuestiones abiertas, UNKNOWN y conflictos |
| limitations | Límites de aplicabilidad y exclusiones |
| required_actions | Acciones correctivas con responsable/Issue |
| supersedes | Registro o versión anterior, si aplica |

## 3. Criterios de revisión

- [ ] El objetivo, alcance y finalidad están definidos.
- [ ] Los términos y entidades están definidos sin ambigüedades materiales.
- [ ] Entradas, salidas, unidades, timestamps y estados están especificados.
- [ ] Las reglas positivas, exclusiones, excepciones y transiciones están descritas.
- [ ] UNKNOWN, NOT_EVIDENCED, BLOCKED y conflictos no se convierten en PASS, cero ni ausencia de riesgo.
- [ ] Dependencias y requisitos previos están identificados.
- [ ] Fuentes, licencias, derechos de uso y límites están documentados para el alcance.
- [ ] Los riesgos de look-ahead, survivorship, leakage y doble conteo están tratados cuando aplican.
- [ ] Las alternativas y contraevidencias relevantes están registradas.
- [ ] Los criterios de aceptación son verificables y no contienen umbrales sin fundamento.
- [ ] Los casos de revisión cubren escenarios normales, límites, fallos y abstención.
- [ ] Las limitaciones y cuestiones no resueltas son explícitas.

## 4. Interpretación de decisiones

- **APPROVED:** los criterios de revisión aplicables están satisfechos para el alcance declarado; permite asignar SPECIFIED al requisito cubierto.
- **APPROVED_WITH_LIMITATIONS:** aprobación restringida a un alcance explícito; las exclusiones y condiciones deben propagarse a consumidores.
- **REVISION_REQUIRED:** se identificaron cambios necesarios; no asignar SPECIFIED.
- **BLOCKED:** falta evidencia, permiso, decisión o dependencia crítica; no asignar SPECIFIED.
- **NOT_REVIEWED:** no se ha completado la revisión.

No usar APPROVED_WITH_LIMITATIONS para ocultar defectos críticos ni para autorizar un uso fuera del alcance aprobado.

## 5. Registro de decisiones y desviaciones

| ID | Cuestión / decisión | Alternativas consideradas | Evidencia y contraevidencia | Decisión | Justificación | Impacto / requisitos afectados |
|---|---|---|---|---|---|---|
| DEC-XXX | Pendiente de completar | — | — | NOT_REVIEWED | — | — |

## 6. Control de cambios

Una modificación material de reglas, fuentes, permisos, contratos, datos, unidades, timestamps o alcance requiere nueva revisión. Conservar el registro anterior, identificar la versión que lo sustituye y evaluar qué pruebas/evidencias quedan invalidadas.

## 7. Límite de aprobación

La aprobación registrada es interna al proyecto y al alcance especificado. No constituye certificación ISO, auditoría independiente, opinión jurídica ni validación empírica de capacidad predictiva. La aprobación de una especificación no eleva el requisito a IMPLEMENTED, TESTED o EMPIRICALLY_VALIDATED.
