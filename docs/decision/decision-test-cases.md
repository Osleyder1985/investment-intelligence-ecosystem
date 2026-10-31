# Casos de prueba del protocolo de decisión y ranking

**Estado:** Casos de consistencia documental; pendientes de automatización y validación empírica.

## DC-001 — Descuento extremo con veto activo
- Entrada: A presenta mayor brecha favorable frente al rango de valor; existe VETO_ACTIVE aplicable.
- Esperado: A queda bloqueado y no aparece como oportunidad favorable ni supera el veto por puntuación.
- Fallo: A ocupa primer puesto por descuento.

## DC-002 — Veto en revisión
- Entrada: alerta creíble sin resolución suficiente; VETO_REVIEW_REQUIRED.
- Esperado: REVIEW_REQUIRED, sin conclusión favorable confirmada.
- Fallo: tratar alerta como inexistente o como veto definitivamente probado sin evidencia.

## DC-003 — Riesgo desconocido
- Entrada: información insuficiente sobre custodio/llave administrativa material.
- Esperado: RISK_ASSESSMENT_INCOMPLETE; UNKNOWN no se convierte en NO_VETO_IDENTIFIED.
- Fallo: declarar bajo riesgo por falta de incidentes conocidos.

## DC-004 — Identidad conflictiva
- Entrada: ticker compartido por dos contratos y red no resuelta.
- Esperado: BLOCKED_IDENTITY; no valorar ni rankear instrumento ambiguo.
- Fallo: seleccionar el primer resultado del agregador.

## DC-005 — Datos críticos en cuarentena
- Entrada: supply o precio de referencia crítico con conflicto no resuelto.
- Esperado: BLOCKED_DATA_QUALITY o conclusión limitada que no dependa del dato; sin imputación silenciosa.
- Fallo: sustituir por cero o proveedor no declarado.

## DC-006 — Horizontes divergentes
- Entrada: catalizador de corto plazo con dilución y deterioro de derechos a largo plazo.
- Esperado: conclusiones separadas por horizonte.
- Fallo: trasladar una etiqueta a todos los horizontes.

## DC-007 — Precio nominal bajo
- Entrada: A cotiza por unidad menos que B, sin evidencia de valor razonable.
- Esperado: no inferir infravaloración ni prioridad por precio nominal.
- Fallo: colocar A primero por precio unitario.

## DC-008 — Descuento con ejecución inviable
- Entrada: A muestra descuento de modelo, pero mercado sin profundidad/retiro accesible para el alcance.
- Esperado: condición/bloqueo de ejecución; no declarar oportunidad operable.
- Fallo: ignorar costes y acceso.

## DC-009 — Rangos incompatibles
- Entrada: dos métodos aplicables producen rangos materialmente distintos para el mismo objeto.
- Esperado: mostrar ambos, explicar desacuerdo y condicionar/abstenerse si no se resuelve.
- Fallo: promediar automáticamente.

## DC-010 — Objetos económicos distintos
- Entrada: valoración de protocolo comparada con valoración del token sin puente de atribución.
- Esperado: NO_COMPARABLE_OPPORTUNITY.
- Fallo: comparar cifras como el mismo objeto.

## DC-011 — Dimensión UNKNOWN
- Entrada: concentración de holders desconocida y material.
- Esperado: mantener UNKNOWN y degradar comparabilidad/confianza cualitativa.
- Fallo: imputar concentración promedio del sector.

## DC-012 — Trade-off sin dominancia
- Entrada: A supera a B en robustez; B supera a A en liquidez.
- Esperado: no declarar dominancia sin resolver trade-off; presentar relación no ordenada.
- Fallo: inventar pesos para elegir ganador.

## DC-013 — Dominancia demostrable
- Entrada: A no es peor que B en dimensiones materiales comparables y es mejor en al menos una; incertidumbre no invierte la relación.
- Esperado: registrar A domina B con dimensiones y evidencia.
- Fallo: inferir dominancia desde métrica aislada.

## DC-014 — Universos distintos
- Entrada: activos proceden de snapshots con criterios/cobertura diferentes.
- Esperado: separar o reconciliar universos.
- Fallo: presentar orden común sin advertencia.

## DC-015 — Costes omitidos
- Entrada: brecha positiva desaparece al incluir spread, comisiones y slippage.
- Esperado: reflejar coste total y revisar clasificación.
- Fallo: comparar valor teórico con precio no ejecutable.

## DC-016 — Catalizador rumoreado
- Entrada: movimiento precede a anuncio oficial; rumor no corroborado de filtración.
- Esperado: registrar hipótesis/evidencia temporal; no afirmar filtración ni causalidad.
- Fallo: presentar rumor como causa demostrada.

## DC-017 — Agregación no validada
- Entrada: score con pesos iguales sin calibración ni validación temporal.
- Esperado: rechazar score y mantener ranking parcial/dominancia.
- Fallo: publicar puntuación total como objetiva.

## DC-018 — Resultado reproducible
- Entrada: misma versión, universo, datos, modelos y timestamp de corte.
- Esperado: mismo resultado o diferencias explicadas por no determinismo documentado.
- Fallo: no reconstruir posición, gates o razones.

## DC-019 — Cambio de veto
- Entrada: veto activo con evidencia posterior de remediación.
- Esperado: conservar evaluación histórica y crear una nueva tras verificación.
- Fallo: sobrescribir estado histórico.

## DC-020 — Evidencia insuficiente
- Entrada: valoración condicional, incertidumbre material y liquidez incompleta.
- Esperado: INSUFFICIENT_EVIDENCE o CONDITIONAL_CANDIDATE con faltantes explícitos.
- Fallo: emitir compra/venta o precio objetivo inventado.

## Criterio común
Verificar gates, alcance, horizonte, unidades, evidencia, vetos, UNKNOWN, contraevidencia, reproducibilidad y separación entre clasificación informativa y ejecución. Estos casos verifican consistencia lógica, no rentabilidad futura ni capacidad predictiva.
