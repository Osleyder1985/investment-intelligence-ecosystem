# Plantilla de evaluación de riesgos

**Estado:** Plantilla propuesta. **Versión del esquema:** 0.1.0

Usar una ficha por riesgo y una vista agregada por activo. Para campos materiales usar DESCONOCIDO, NO_APLICA con justificación o PENDIENTE; no dejar vacíos ambiguos.

## A. Identificación y alcance

| Campo | Contenido |
|---|---|
| assessment_id | Identificador único |
| asset_id | Identidad canónica del activo |
| network / chain_id | Red y chain ID, si aplica |
| contract_address | Dirección verificada o NO_APLICA |
| risk_id / domain | Identificador y dominio de la taxonomía |
| event_or_condition | Evento o condición evaluada |
| scope | Activo, contrato, mercado, custodio, jurisdicción, operación y cantidad |
| assessment_timestamp_utc | Momento de evaluación |
| event_timestamp_utc | Momento del evento, si se conoce |
| publication_timestamp_utc | Momento de publicación |
| horizon | Horizonte y fundamento |

## B. Análisis

| Campo | Contenido |
|---|---|
| cause / impact | Causa y consecuencias plausibles |
| inherent_risk | Exposición antes de controles, descrita cualitativamente |
| evidence_status | HECHO_VERIFICADO / FUENTE_REPORTADA / HIPOTESIS / ALERTA_NO_CONFIRMADA / OPINION / DESCONOCIDO |
| evidence_refs | Fuentes, hashes, URLs, bloques/transacciones y fragmentos |
| evidence_limitations | Alcance, independencia, fecha y cobertura |
| controls | Controles preventivos, detectivos y correctivos |
| control_verification | Cómo y cuándo se comprobó cada control |
| residual_risk | Riesgo restante tras controles verificados |
| uncertainty | Supuestos, límites y evidencia contradictoria |
| dependencies | Proveedores, custodios y protocolos relacionados |

## C. Estado y decisión

| Campo | Contenido |
|---|---|
| status | VETO_ACTIVE / VETO_REVIEW_REQUIRED / NO_VETO_IDENTIFIED / RISK_UNKNOWN |
| status_reason | Motivo trazable |
| veto_scope | Alcance exacto del bloqueo o NO_APLICA |
| blocked_actions | Actividades impedidas |
| remediation_required | Evidencia/acción necesaria |
| reactivation_evidence | Evidencia requerida para retirar/reducir veto |
| reviewer | Rol responsable |
| reviewed_at_utc | Última revisión |
| next_review_trigger | Evento que obliga a revisar |
| supersedes | Registro anterior, si aplica |
| protocol_version | Versión de taxonomía/política |

## D. Integridad

- No usar cero o cadena vacía para ocultar desconocimiento.
- Toda activación o retirada de veto enlaza evidencia y alcance.
- Separar fecha del evento, publicación, captura y evaluación.
- Las correcciones crean una nueva revisión; conservar historial.
- No agregar riesgos heterogéneos en puntuación global sin metodología aprobada.
- La vista agregada conserva vetos por dominio y alcance.
- NO_VETO_IDENTIFIED no significa seguro.
- Si un riesgo material no puede evaluarse, usar RISK_UNKNOWN y explicar qué falta.

## E. Ejemplo esquemático, no referido a un activo real

- domain: SMART_CONTRACT
- event_or_condition: alerta de exploit en contrato dependiente
- evidence_status: ALERTA_NO_CONFIRMADA
- status: VETO_REVIEW_REQUIRED
- veto_scope: contrato y operaciones dependientes
- status_reason: falta corroborar transacciones y correspondencia de la versión desplegada.

El ejemplo no afirma que haya ocurrido un exploit.