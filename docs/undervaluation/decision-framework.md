# Marco de decisión para clasificar infravaloración

**Estado:** Propuesta metodológica; pendiente de revisión y validación.  
**Versión:** 0.1.0

## 1. Propósito

Convertir una estimación de valor razonable y un precio de mercado comparable en una clasificación reproducible. La clasificación mide evidencia de descuento frente a un modelo; no demuestra por sí sola rentabilidad futura ni constituye una orden de compra.

## 2. Condiciones previas obligatorias

No iniciar la clasificación si falta cualquiera de los siguientes elementos esenciales:

- Identidad inequívoca del activo, red, contrato y unidad negociada.
- Objeto de valoración y tipología económica documentados.
- Método aplicable y justificación según el catálogo de valoración.
- Rango de valor razonable, fecha de referencia, horizonte, supuestos y limitaciones.
- Precio de mercado con mercado, par, moneda, timestamp y fuente identificados.
- Oferta circulante/total pertinente y su definición cuando afecte al método.
- Evaluación de calidad, cobertura, liquidez, profundidad y costes relevantes.
- Registro de incertidumbre, escenarios y contraevidencia.

Si una condición esencial falla, emitir SIN_EVIDENCIA_SUFICIENTE o VALOR_NO_ESTIMABLE; no sustituirla por un valor supuesto.

## 3. Comparabilidad temporal y de mercado

El precio y el rango de valor deben referirse al mismo instante o a una ventana temporal definida y justificada, la misma unidad económica y una moneda comparable. Registrar zona horaria UTC, mercado, par, red, wrapper/bridged status y fuente.

Para una posible entrada, preferir el coste de adquisición ejecutable (ask y profundidad disponible) frente al último precio negociado. Incluir comisiones, spread, slippage e impacto de mercado cuando sean materiales. Para una salida o liquidación, usar referencias de venta ejecutables (bid) y costes correspondientes. Si solo hay último precio, marcarlo como referencia no necesariamente ejecutable.

No mezclar cotizaciones de mercados distintos sin normalización y justificación. Si la liquidez no permite estimar una ejecución representativa, reducir el nivel de conclusión o abstenerse.

## 4. Estados de clasificación propuestos

| Estado | Interpretación | Condición general |
|---|---|---|
| INSUFFICIENT_EVIDENCE | Evidencia insuficiente | Faltan datos, identidad, comparabilidad o derechos esenciales |
| NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE | Valor no estimable | No existe método defendible o no se puede estimar un rango |
| OVERVALUED_BY_MODEL | Sobrevalorado según el modelo | Precio comparable supera el límite superior del rango aplicable |
| WITHIN_FAIR_VALUE_RANGE | Dentro del rango estimado | Precio comparable está dentro del rango |
| POTENTIAL_UNDERVALUATION | Posible infravaloración | Precio está por debajo del rango, pero persiste incertidumbre o condiciones materiales |
| MODEL_SUPPORTED_UNDERVALUATION | Infravaloración respaldada por el modelo | Precio ejecutable está por debajo del límite conservador del rango y se satisfacen controles de evidencia, robustez, liquidez y riesgo |
| EXTREME_UNDERVALUATION_NOT_CALIBRATED | Categoría extrema no calibrada | La metodología no tiene calibración independiente aprobada; no es una etiqueta activa de oportunidad |
| EXTREME_UNDERVALUATION | Infravaloración extrema | Solo tras definición y calibración empírica aprobadas, con validación independiente fuera de muestra |

Los estados son mutuamente excluyentes para una evaluación concreta. El sistema puede conservar resultados secundarios y motivos, pero no asignar simultáneamente estados contradictorios.

## 5. Uso del rango de valor razonable

Sea [V_L, V_U] el rango estimado y P_ref el precio de referencia comparable. Antes de comparar, comprobar:

- V_L y V_U corresponden al mismo objeto, unidad, moneda y horizonte.
- V_L ≤ V_U y las unidades están normalizadas.
- Se documenta si el rango es un intervalo estadístico, un intervalo de incertidumbre del modelo o una envolvente de escenarios. No llamarlo intervalo de confianza si no tiene cobertura estadística calibrada.
- La estimación incluye supuestos y sensibilidad; los riesgos no modelados se muestran por separado.

Regla conceptual inicial:

- P_ref < V_L: el precio está por debajo del rango estimado; puede existir descuento según el modelo.
- V_L ≤ P_ref ≤ V_U: no hay evidencia de precio por debajo del rango.
- P_ref > V_U: precio por encima del rango estimado; posible sobrevaloración según el modelo.

La comparación no basta por sí sola para emitir MODEL_SUPPORTED_UNDERVALUATION: también deben pasar los controles de datos, robustez, liquidez, costes y riesgos. Los casos de límites coincidentes, valores no positivos, rango abierto o incomparable deben tratarse explícitamente.

## 6. Robustez y contraevidencia

Una conclusión fuerte requiere evaluar:

- Sensibilidad a supuestos materiales.
- Resultados de métodos alternativos aplicables.
- Dispersión entre modelos y calidad relativa de cada uno.
- Escenarios adversos y condiciones que invalidan el método.
- Riesgos de emisión, desbloqueos, concentración, custodia, tecnología, contraparte, regulación y liquidez.
- Eventos y noticias que expliquen racionalmente el descuento.
- Hipótesis alternativas y evidencia contraria.

Si el descuento desaparece bajo cambios razonables de supuestos, mantener una clasificación condicionada o abstenerse. No seleccionar únicamente el modelo que produce el mayor descuento.

## 7. Separación de decisiones

La clasificación de infravaloración no es una recomendación personalizada. La decisión final debe considerar horizonte, tolerancia al riesgo, concentración, exposición existente, restricciones jurisdiccionales, custodia y posibilidad real de ejecutar la operación. Un veto de riesgo crítico puede impedir una candidatura aunque el modelo estime descuento.

## 8. Categoría extrema

EXTREME_UNDERVALUATION permanecerá DESHABILITADA / NOT_CALIBRATED hasta que exista:

- definición cuantitativa documentada;
- dataset histórico con universo y activos desaparecidos;
- protocolo de calibración separado del conjunto de prueba;
- walk-forward y evaluación out-of-sample;
- controles de falsos descubrimientos y selección múltiple;
- análisis de estabilidad por régimen y tipología;
- costes y restricciones de ejecución;
- criterios de aceptación previamente registrados.

No usar percentiles o porcentajes elegidos por conveniencia. Si la calibración no es defendible, mostrar «categoría extrema no calibrada».

## 9. Registro de salida

Guardar: activo e identidad, timestamp UTC, mercado/par, precio y tipo de referencia, rango y tipo de intervalo, método/versión, gap si aplica, coste de ejecución, liquidez, estado, motivos, riesgos, incertidumbre, alternativas, contraevidencia, datos faltantes, horizonte y versión de protocolo.
