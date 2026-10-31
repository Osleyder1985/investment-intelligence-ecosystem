# Métricas de brecha entre precio y valor estimado

**Estado:** Definiciones candidatas; no calibradas como señales de inversión.  
**Versión:** 0.1.0

## 1. Notación y convenciones

- P: precio de mercado observado por unidad.
- P_buy: coste de adquisición por unidad estimado para la cantidad objetivo, incluyendo costes aplicables.
- P_sell: ingreso neto de venta por unidad estimado para la cantidad objetivo, descontando costes aplicables.
- V_L: límite inferior del rango de valor razonable.
- V_U: límite superior del rango de valor razonable.
- V_M: estimación central, solo si el método justifica su existencia.\n- P_ref: referencia de ejecución elegida para la finalidad de la comparación; para entrada será P_buy y para salida será P_sell. No mezclar ambos en una misma brecha.

Registrar moneda, unidad, mercado, timestamp y horizonte de todas las variables. No combinar datos incompatibles.

## 2. Brecha absoluta frente al límite inferior

Definición candidata:

Δ_L = V_L − P_ref

La unidad es la misma que la cotización (por ejemplo, USD por token). Δ_L > 0 indica que el precio de referencia está por debajo del límite inferior del rango. Δ_L = 0 indica coincidencia; Δ_L < 0 indica que el precio está en o por encima de ese límite.

Es una diferencia absoluta, no una rentabilidad esperada. No expresa por sí sola incertidumbre, probabilidad ni facilidad de ejecución.

## 3. Brecha relativa

Cuando V_L > 0, puede calcularse:

G_L = (V_L − P_ref) / V_L

La métrica es adimensional y puede expresarse como porcentaje. Un valor positivo indica precio inferior al límite inferior estimado. No calcularla cuando V_L sea cero, negativo, no finito o no comparable. No sustituir el denominador por una constante arbitraria.

La fórmula es una convención candidata para medir distancia relativa respecto al límite inferior; no representa retorno esperado, margen de seguridad validado ni probabilidad de convergencia.

## 4. Costes y precio ejecutable

Para una entrada, comparar el valor estimado con P_buy, no asumir que P equivale al coste real. P_buy debe derivarse de libro de órdenes/profundidad o de un método de ejecución documentado, para una cantidad de referencia explícita, e incluir comisiones, spread, slippage e impacto cuando corresponda.

Para salida, usar P_sell. No mezclar brecha de entrada con brecha de salida. Si el tamaño objetivo no puede ejecutarse con datos disponibles, marcar coste no estimable y reducir la conclusión.

No sumar dos veces el spread o las comisiones si ya están incorporados en el precio ejecutable.

## 5. Tipos de rango

El registro debe distinguir al menos:

- Intervalo estadístico con método y cobertura declarados.
- Intervalo de incertidumbre del modelo, sin cobertura probabilística necesariamente.
- Envolvente de escenarios, cuyos extremos dependen de supuestos definidos.
- Rango de consenso entre métodos, indicando composición y dispersión.

No convertir una envolvente de escenarios en intervalo de confianza. No interpretar V_L como suelo garantizado ni como valor de liquidación.

## 6. Casos donde la métrica no aplica

Abstenerse de calcular brecha relativa cuando:

- V_L ≤ 0;
- el rango no es finito, no está ordenado o carece de unidad clara;
- el precio y el valor estimado no corresponden al mismo activo económico;
- monedas, unidades, wrappers, derechos o timestamps no son comparables;
- el rango procede de un método declarado no aplicable;
- los datos esenciales están incompletos o su licencia impide el uso previsto.

En esos casos, conservar la razón de abstención. No generar un cero, porcentaje aproximado ni sustituto implícito.

## 7. Interpretación prohibida

- G_L positivo no significa que el mercado vaya a converger a V_L.
- G_L no es retorno esperado ni probabilidad de ganancia.
- Una brecha elevada no elimina riesgos de insolvencia, fraude, iliquidez, pérdida total o dilución.
- Una caída del precio no crea por sí misma una brecha de valoración.
- No comparar directamente G_L entre métodos, clases o horizontes sin analizar comparabilidad y calibración.
- No ordenar activos por G_L sin controles de calidad, elegibilidad, riesgos y sesgos.

## 8. Calibración pendiente

No hay umbrales de «barato», «muy barato» o «extremadamente infravalorado» aprobados en esta versión. Cualquier límite de clasificación debe justificarse mediante el protocolo histórico, validarse fuera de muestra y evaluarse por tipología y régimen. Hasta entonces, informar las magnitudes descriptivas y sus limitaciones sin forzar etiquetas de intensidad.
