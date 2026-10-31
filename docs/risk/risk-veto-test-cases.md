# Casos de prueba de riesgos y vetos

**Estado:** Casos de diseño; pendientes de automatización y validación con escenarios históricos.

## RV-001 — Exploit confirmado y vigente
- Entrada: transacciones verificables muestran drenaje activo en contrato que soporta el activo.
- Esperado: VETO_ACTIVE para contrato/ruta afectados; preservar evidencia y alcance.
- Fallo: permitir que descuento o ranking compense el veto.

## RV-002 — Rumor no corroborado
- Entrada: publicación social sin evidencia técnica verificable.
- Esperado: VETO_REVIEW_REQUIRED si la señal es creíble y material; marcar alerta no confirmada.
- Fallo: presentarla como hecho o ignorarla sin revisión.

## RV-003 — Clave administrativa comprometida
- Entrada: evidencia fiable de compromiso de clave con permisos críticos vigentes.
- Esperado: VETO_ACTIVE en el alcance de permisos; reevaluar tras rotación y verificación on-chain.
- Fallo: aceptar solo declaración del equipo.

## RV-004 — Auditoría antigua/código cambiado
- Entrada: auditoría de versión distinta a la desplegada.
- Esperado: auditoría no prueba seguridad actual; RISK_UNKNOWN o VETO_REVIEW_REQUIRED según señales.
- Fallo: extrapolar sin verificar correspondencia.

## RV-005 — Bridge comprometido
- Entrada: bridge detenido, explotado o con respaldo no conciliado.
- Esperado: veto a activos/rutas dependientes; no generalizar al token nativo si no depende de él.
- Fallo: ignorar dependencia o vetar redes no afectadas.

## RV-006 — Proof of reserves incompleta
- Entrada: atestación de activos sin pasivos, titularidad, gravámenes o redención.
- Esperado: no concluir respaldo íntegro; RISK_UNKNOWN o VETO_REVIEW_REQUIRED según exposición.
- Fallo: presentarla como garantía de solvencia.

## RV-007 — Redención suspendida
- Entrada: activo cuya tesis depende de redención con esta suspendida y confirmada.
- Esperado: VETO_ACTIVE para esa tesis/operación.
- Fallo: valorar como si redención siguiera disponible.

## RV-008 — Unlock y dilución
- Entrada: calendario verificable que altera materialmente oferta y derechos.
- Esperado: registrar riesgo tokenómico y actualizar escenarios; veto solo si condición crítica justificada se cumple.
- Fallo: vetar automáticamente cualquier unlock o ignorar dilución.

## RV-009 — Liquidez no ejecutable
- Entrada: último precio visible, pero profundidad insuficiente para cantidad/ruta evaluada.
- Esperado: VETO_ACTIVE para esa operación si no hay ruta ejecutable; RISK_UNKNOWN si profundidad no se conoce.
- Fallo: usar último precio como salida realizable.

## RV-010 — Restricción jurisdiccional oficial
- Entrada: acto oficial vigente que prohíbe operación en jurisdicción aplicable.
- Esperado: VETO_ACTIVE para esa jurisdicción/actividad; registrar fuente, vigencia y alcance.
- Fallo: extrapolar a todos los países o usuarios.

## RV-011 — Riesgo no evaluable
- Entrada: información insuficiente sobre custodio, reservas o permisos.
- Esperado: RISK_UNKNOWN; no convertir ausencia de datos en NO_VETO_IDENTIFIED.
- Fallo: asignar riesgo cero.

## RV-012 — Remediación anunciada, no verificada
- Entrada: equipo anuncia parche/rotación, sin verificación de despliegue y funcionamiento.
- Esperado: VETO_ACTIVE → VETO_REVIEW_REQUIRED; no retirar veto.
- Fallo: retirarlo por comunicado.

## RV-013 — Remediación comprobada
- Entrada: cambio desplegado, permisos verificados, pruebas independientes y funcionamiento recuperado.
- Esperado: transición a NO_VETO_IDENTIFIED solo con revisión documentada; conservar veto histórico.
- Fallo: borrar incidente o evidencia.

## RV-014 — Descuento frente a veto
- Entrada: modelo estima infravaloración, pero existe VETO_ACTIVE.
- Esperado: activo bloqueado para candidatura afectada; descuento permanece como dato no accionable.
- Fallo: ranking favorable o recomendación de inversión.

## RV-015 — No se detectan vetos
- Entrada: dominios relevantes revisados con evidencia suficiente y sin condición bloqueante.
- Esperado: NO_VETO_IDENTIFIED con cobertura y limitaciones; no declarar seguro.
- Fallo: inferir ausencia absoluta de riesgo.

## Criterio común

Comprobar estado, alcance, evidencia, timestamp, transición, motivo, contraevidencia e impacto en decisión. Los casos prueban consistencia lógica, no ausencia real de riesgo ni capacidad predictiva.
