# Casos de prueba del marco de infravaloración

**Estado:** Casos de diseño; pendientes de implementación automatizada y validación empírica.

## UC-001 — Precio nominal muy bajo sin descuento
- Entrada: precio unitario pequeño, oferta muy elevada y precio dentro del rango de valor estimado.
- Esperado: WITHIN_FAIR_VALUE_RANGE; no etiquetar como barato por precio nominal.
- Fallo: clasificar por céntimos por token.

## UC-002 — Caída pronunciada sin infravaloración
- Entrada: drawdown elevado; método aplicable estima rango por debajo del precio de mercado actual.
- Esperado: OVERVALUED_BY_MODEL o WITHIN_FAIR_VALUE_RANGE según comparación; drawdown es descriptivo.
- Fallo: interpretar caída como descuento fundamental.

## UC-003 — Precio por debajo del rango con evidencia robusta
- Entrada: precio ejecutable comparable inferior a V_L; datos suficientes, método aplicable, sensibilidad y riesgos revisados.
- Esperado: MODEL_SUPPORTED_UNDERVALUATION, sujeto a revisión y ausencia de veto crítico.
- Fallo: omitir costes, riesgos o comparabilidad temporal.

## UC-004 — Precio por debajo del rango, pero alta incertidumbre
- Entrada: estimación central sugiere descuento, pero rango amplio y resultados sensibles a supuestos no verificables.
- Esperado: POTENTIAL_UNDERVALUATION o INSUFFICIENT_EVIDENCE; nunca EXTREME_UNDERVALUATION sin calibración.
- Fallo: usar solo la estimación central.

## UC-005 — Mercado ilíquido
- Entrada: último precio muy por debajo de V_L, pero profundidad insuficiente para la cantidad objetivo y spread elevado/no fiable.
- Esperado: reducir conclusión o INSUFFICIENT_EVIDENCE; no afirmar oportunidad ejecutable.
- Fallo: tratar último precio como precio de compra realizable.

## UC-006 — Denominador no positivo
- Entrada: V_L ≤ 0.
- Esperado: no calcular G_L; registrar no aplicabilidad matemática. Puede conservarse una brecha absoluta si es válida.
- Fallo: dividir por cero, usar valor absoluto o introducir constante artificial.

## UC-007 — Ausencia de método de valoración
- Entrada: token sin derechos, flujos, respaldo ni vínculo de demanda demostrable.
- Esperado: NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE; no generar valor razonable.
- Fallo: inventar un precio objetivo.

## UC-008 — Categoría extrema no calibrada
- Entrada: brecha grande, pero sin protocolo out-of-sample aprobado.
- Esperado: EXTREME_UNDERVALUATION_NOT_CALIBRATED; no asignar etiqueta extrema activa.
- Fallo: aplicar un porcentaje o percentil arbitrario.

## UC-009 — Sesgo de supervivencia
- Entrada: universo histórico excluye tokens deslistados o fallidos.
- Esperado: invalidar el experimento hasta reconstruir o documentar el sesgo y su impacto.
- Fallo: presentar resultados solo con activos supervivientes.

## UC-010 — Datos futuros filtrados
- Entrada: variable histórica cuyo dato fue publicado después del timestamp de decisión.
- Esperado: excluir variable o desplazar su disponibilidad al timestamp real; registrar leakage.
- Fallo: usar el valor retrospectivo como si se conociera en tiempo real.

## Criterio común

Cada prueba debe comprobar estado, motivo, trazabilidad, datos usados, timestamps, abstención y ausencia de etiquetas no calibradas. Los casos no constituyen evidencia de rentabilidad.
