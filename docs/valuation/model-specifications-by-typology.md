# Especificaciones de modelos por tipología económica

**Estado:** Diseño inicial de modelos; no calibrado ni validado para predicción. **Versión:** 0.1.0

## 1. Regla general

Primero identificar el objeto y el derecho del titular; después seleccionar el modelo. Las fórmulas siguientes definen estructuras de cálculo, no parámetros universales ni recomendaciones. Todos los inputs deben tener unidad, timestamp, fuente y estado de evidencia.

## 2. NAV, respaldo y redención

**Objeto:** valor neto atribuible a un derecho o participación.

Estructura:
- NAV bruto = suma del valor realizable de activos identificados.
- NAV neto = NAV bruto − pasivos exigibles − costes pertinentes.
- Valor por unidad del derecho = NAV neto atribuible al conjunto de derechos / unidades con derecho equivalente, solo si el denominador y la prioridad jurídica están definidos.

Inputs: activos, precios/valoraciones, titularidad, pasivos, gravámenes, custodia, segregación, prioridad, costes, unidades elegibles y términos de redención.

Condiciones: comprobar que el token confiere derecho sobre activos o redención. Un proof of reserves no demuestra por sí solo pasivos completos, titularidad, ausencia de gravámenes ni redención.

Abstenerse si la titularidad, pasivos, derechos, prioridad o unidades elegibles no se pueden determinar. Para activos no fungibles o derechos de prioridad distinta, no dividir NAV por supply genérico.

## 3. Flujos de caja atribuibles al titular (DCF)

**Objeto:** valor presente de flujos que el titular puede recibir o reclamar.

Estructura general:
- PV de flujos = suma de CF_t atribuibles / (1 + r_t)^t, con convenciones de periodo y moneda declaradas.
- El valor terminal, si se usa, debe especificar fórmula, hipótesis de continuidad y peso relativo; no incorporarlo por defecto.
- CF_t debe ser neto de costes, impuestos/retenciones aplicables, prioridad, dilución y restricciones pertinentes.

Inputs: flujo atribuible, calendario, moneda, duración, prioridad, contraparte, costes, emisión/dilución, escenarios y tasa de descuento justificada.

Condición esencial: derecho exigible o mecanismo on-chain/contractual verificable que asigne flujo al titular. Ingresos o comisiones del protocolo sin distribución, buyback verificable u otro mecanismo atribuible no son CF del token.

Abstenerse si el derecho es discrecional/no demostrado, los flujos no se pueden separar o la tasa no puede justificarse. No asumir que staking rewards son flujo externo: separar emisión inflacionaria de comisiones/recompensas financiadas por terceros.

## 4. Comparables y valoración relativa

**Objeto:** estimación relativa para derechos, activos o redes comparables.

Estructura:
- Seleccionar métrica económica homogénea y su denominador.
- Derivar relación observada entre métrica de mercado y variable fundamental comparable.
- Aplicar rango de relaciones comparables a la variable del activo solo con ajustes transparentes por derechos, riesgo, etapa, crecimiento, oferta y liquidez.

Inputs: universo comparable point-in-time, variable económica, precio/valoración de referencia, definiciones homogéneas, fechas, unidades y ajustes.

No usar comparables seleccionados porque producen el resultado deseado. Reportar distribución, exclusiones, dispersión y sensibilidad. No aplicar múltiplo sectorial único sin evidencia. Si comparables no comparten mecanismo económico, declarar NOT_APPLICABLE o INSUFFICIENT_EVIDENCE.

## 5. Demanda económica y utilidad de red

**Objeto:** escenarios de demanda por un recurso/servicio y vínculo de esa demanda con el token.

Estructura:
- Estimar demanda de uso a partir de usuarios/actividad/servicios verificables, precios, capacidad y sustitutos.
- Modelar la fracción de demanda que requiere adquirir, mantener, bloquear o quemar el token.
- Estimar demanda neta del token considerando emisión, ventas de recompensas, velocity/rotación, sustitutos y otros usos; declarar explícitamente qué variables se observan y cuáles se suponen.

No inferir valor de token directamente de transacciones, direcciones, TVL, usuarios o fees. Debe existir mecanismo económico verificable que conecte el uso con demanda neta o derechos del token. La actividad subsidiada o automatizada debe identificarse.

Abstenerse de estimar valor monetario si elasticidad, captura de demanda o mecanismo de compra/retención no se pueden sustentar. Reportar escenarios cualitativos cuando no se pueda cuantificar con rigor.

## 6. Escenarios monetarios y reserva de valor

**Objeto:** escenarios de demanda monetaria, adopción, oferta efectiva y competencia.

Estructura:
- Escenario de demanda monetaria = hipótesis explícita sobre unidades de cuenta/ahorro/colateral/transferencia y adopción.
- Precio implícito de escenario = demanda monetaria total supuesta / oferta efectiva correspondiente, con unidad y fecha coherentes.

La demanda monetaria es una hipótesis de escenario, no un dato observado. Definir oferta efectiva y tratamiento de monedas perdidas, bloqueadas, custodiadas o no disponibles sin fingir precisión. Contrastar competencia, sustitutos, seguridad, liquidez, emisión y restricciones.

No usar stock-to-flow u otra ratio como valoración autónoma. No asignar probabilidades de escenarios sin calibración independiente. Si la demanda no puede acotarse con evidencia, reportar escenario no cuantificado/abstenerse.

## 7. Paridad y convertibilidad

**Objeto:** valor de redención o conversión accesible al titular concreto.

Estructura:
- Valor neto de redención = cantidad redimible × importe contractual por unidad − costes, penalizaciones y cargos aplicables.
- Valor realizable debe considerar elegibilidad del titular, límites, horarios, jurisdicción, solvencia, colateral, liquidez y capacidad operativa.
- Comparar con precio ejecutable del mercado, no solo último precio.

Un objetivo de paridad no es garantía. Si redención no está disponible para el titular o jurisdicción evaluados, no presentar el valor nominal como valor redimible.

## 8. Staking, restaking y recibos líquidos

Descomponer rendimiento:
- emisión de nuevos tokens;
- comisiones/recompensas pagadas por terceros;
- costes de validación/servicio;
- slashing y riesgo de operador;
- bloqueo, colas y descuento por liquidez;
- dilución y cambios de reglas.

Un APR nominal no equivale a rendimiento económico neto ni a flujo garantizado. Para recibos líquidos, modelar el derecho de rescate, exchange rate, activos subyacentes y dependencias del protocolo. No duplicar el valor del subyacente y del recibo.

## 9. Híbridos y múltiples componentes

Si el activo combina derechos, utilidad, staking, gobernanza o exposición a reserva, valorar componentes por separado y documentar solapamientos. La suma solo es admisible si los componentes son económicamente excluyentes y atribuibles; de lo contrario presentar rangos alternativos o abstenerse.

## 10. Salida mínima

Cada modelo debe reportar objeto, unidad, aplicabilidad, fórmula, inputs y fuentes, parámetros y estado, rango, escenarios, sensibilidad, incertidumbre, puente al token, riesgo de doble conteo, limitaciones y abstención.
