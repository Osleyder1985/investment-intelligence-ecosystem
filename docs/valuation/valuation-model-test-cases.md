# Casos de prueba de modelos de valoración

**Estado:** Casos de diseño; pendientes de automatización y validación empírica.

## VM-001 — DCF sin derecho del token
- Entrada: protocolo genera ingresos, pero token no tiene derecho ni mecanismo verificable de distribución.
- Esperado: DCF no aplicable al token; no atribuir ingresos del protocolo.
- Fallo: capitalizar ingresos brutos como flujo del titular.

## VM-002 — NAV sin titularidad ni redención
- Entrada: informe de reservas sin evidencia de titularidad, pasivos o derecho exigible.
- Esperado: INSUFFICIENT_EVIDENCE; no asignar NAV por token.
- Fallo: dividir reservas reportadas por supply.

## VM-003 — Doble conteo de tesorería y flujos
- Entrada: mismo activo económico se incluye como NAV y genera flujos ya capitalizados.
- Esperado: identificar solapamiento; no sumar componentes duplicados.
- Fallo: sumar ambos sin ajuste.

## VM-004 — Dilución material
- Entrada: unlocks/emisión futura documentada cambia unidades con derecho económico.
- Esperado: incorporar trayectoria de oferta por timestamp y escenarios; no usar supply estático sin justificación.
- Fallo: mantener denominador actual durante todo horizonte.

## VM-005 — Comparables no homogéneos
- Entrada: múltiplos de tokens con derechos, etapas o modelos económicos distintos.
- Esperado: excluir comparables no defendibles o ajustar con fundamento; informar dispersión.
- Fallo: aplicar media de sector arbitraria.

## VM-006 — Actividad sin captura de valor
- Entrada: aumento de TVL/transacciones, sin vínculo probado con demanda o derechos del token.
- Esperado: no elevar valor del token automáticamente.
- Fallo: convertir actividad directamente en precio objetivo.

## VM-007 — Escenarios monetarios no calibrados
- Entrada: hipótesis de adopción sin base suficiente para cuantificar demanda.
- Esperado: escenario cualitativo o abstención; sin probabilidades inventadas.
- Fallo: asignar porcentajes de adopción como hechos.

## VM-008 — Redención no accesible
- Entrada: paridad nominal, pero usuario/jurisdicción no puede redimir o existe suspensión.
- Esperado: no usar nominal como valor realizable; aplicar riesgo/veto correspondiente.
- Fallo: asumir convertibilidad universal.

## VM-009 — Staking yield inflacionario
- Entrada: recompensa financiada principalmente por emisión de nuevos tokens.
- Esperado: separar emisión, fees, costes, slashing y dilución.
- Fallo: presentar APR nominal como retorno neto garantizado.

## VM-010 — Wrapper y subyacente
- Entrada: subyacente y recibo/wrapper representan misma exposición económica.
- Esperado: mantener riesgos separados y evitar sumar ambos valores.
- Fallo: doble conteo de supply o NAV.

## VM-011 — Parámetro desconocido
- Entrada: tasa de descuento/elasticidad/supply esencial no disponible.
- Esperado: UNKNOWN; sensibilidad o abstención según materialidad.
- Fallo: insertar valor por defecto no documentado.

## VM-012 — Sensibilidad inestable
- Entrada: cambios plausibles y justificados de supuestos producen rangos incompatibles.
- Esperado: mostrar incertidumbre y condicionar/abstenerse.
- Fallo: escoger solo el escenario favorable.

## VM-013 — Modelo fuera de dominio
- Entrada: método calibrado para una tipología se aplica a otra sin evidencia.
- Esperado: NOT_APPLICABLE o nueva validación.
- Fallo: extrapolar sin justificar.

## VM-014 — Revisión histórica
- Entrada: modelo o parámetro actualizado después del timestamp histórico.
- Esperado: conservar versión anterior y separar nueva evaluación.
- Fallo: reescribir resultados históricos.

## Criterio común

Comprobar objeto, derechos, fórmula, unidades, datos point-in-time, estado de parámetros, rango, sensibilidad, doble conteo, abstención y versión. Los casos verifican consistencia lógica, no exactitud predictiva.
