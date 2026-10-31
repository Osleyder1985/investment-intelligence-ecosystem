# Política de vetos críticos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## Principio

El veto es independiente de la valoración. Ningún descuento, puntuación, momentum, ranking o rendimiento esperado compensa un veto activo. El veto se limita al alcance demostrado: contrato, red, mercado, custodio, jurisdicción, operación, tamaño o finalidad.

## Estados

| Estado | Significado | Consecuencia |
|---|---|---|
| VETO_ACTIVE | Condición crítica verificada y vigente | Bloquear candidatura en el alcance afectado |
| VETO_REVIEW_REQUIRED | Alerta creíble, evidencia contradictoria, incidente en investigación o remediación pendiente | Suspender conclusión favorable y exigir revisión |
| NO_VETO_IDENTIFIED | No se identificó veto con la evidencia examinada | No significa seguridad ni ausencia de riesgo |
| RISK_UNKNOWN | Evidencia insuficiente para evaluar dominio material | No asumir riesgo nulo; abstenerse o limitar conclusión |

El estado agregado debe reflejar el más restrictivo aplicable y conservar estados por dominio. Un dominio no evaluado permanece RISK_UNKNOWN.

## Condiciones candidatas de veto

Puede activarse VETO_ACTIVE ante evidencia verificable de:
- exploit vigente con fondos o integridad en riesgo;
- compromiso de clave con permisos críticos;
- identidad contractual incorrecta para la operación;
- redención suspendida/incumplida cuando sea esencial a la tesis;
- respaldo materialmente insuficiente confirmado;
- insolvencia, congelación o pérdida de control del custodio necesario;
- ruta de mercado/salida no operativa para la finalidad evaluada;
- restricción legal o sanción aplicable confirmada por fuente oficial;
- imposibilidad técnica de transferir, retirar o custodiar en la ruta evaluada;
- fraude o manipulación confirmados que invaliden la integridad de datos o mercado.

La lista no es exhaustiva. Cada regla debe indicar evidencia mínima, alcance, consecuencias y condición de cierre. Titulares o publicaciones sociales no prueban por sí solos el evento.

## Alertas y controles

Una noticia o denuncia no corroborada conduce normalmente a VETO_REVIEW_REQUIRED, no a presentar la acusación como hecho. Buscar fuentes primarias: transacciones, contratos, avisos oficiales, exploradores, reguladores, tribunales, custodios y auditores. Registrar contraevidencia.

Un control solo se considera efectivo si se verifica su diseño, implementación, alcance, fecha, independencia y limitaciones. Multisig requiere verificar configuración y signatarios; timelock, duración y bypasses; auditoría, versión exacta y hallazgos; proof of reserves, activos, pasivos, titularidad, gravámenes, segregación y redención. Ningún control elimina automáticamente un veto.

## Transiciones

- NO_VETO_IDENTIFIED → VETO_REVIEW_REQUIRED: aparece alerta creíble o evidencia contradictoria.
- RISK_UNKNOWN → VETO_REVIEW_REQUIRED: surge señal material que requiere investigación.
- VETO_REVIEW_REQUIRED → VETO_ACTIVE: se verifica condición crítica vigente.
- VETO_REVIEW_REQUIRED → NO_VETO_IDENTIFIED: investigación documentada descarta alerta dentro del alcance.
- VETO_ACTIVE → VETO_REVIEW_REQUIRED: hay indicios de remediación aún no validados.
- VETO_ACTIVE → NO_VETO_IDENTIFIED: remediación, restablecimiento y ausencia de condición bloqueante verificados.
- Cualquier estado → RISK_UNKNOWN: evidencia deja de ser accesible o suficiente para sostener la evaluación actual.

No retirar vetos por paso del tiempo, anuncio del proyecto o ausencia de incidentes nuevos. Una fecha de revisión no es caducidad automática.

## Alcance y decisión

Cada veto identifica entidad/red/contrato/mercado/custodio/jurisdicción, actividad bloqueada, tamaño/ruta si corresponde, fecha de inicio, última verificación y evidencia. No hay excepción automática por rentabilidad esperada.

Orden lógico: identidad y elegibilidad; suficiencia de datos; riesgos y vetos; valoración; oportunidad/ranking; conclusión. VETO_ACTIVE impide candidatura invertible en el alcance afectado. VETO_REVIEW_REQUIRED y RISK_UNKNOWN impiden conclusión favorable no condicionada. NO_VETO_IDENTIFIED permite continuar, pero no significa activo seguro.

Conservar estados anteriores y transiciones con timestamp UTC, evidencia, motivo, alcance, controles y responsable. No sobrescribir historial. Esta política no asigna probabilidades ni puntuaciones, no certifica seguridad ni sustituye análisis jurídico.