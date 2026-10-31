# Elegibilidad y screening inicial de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Separación de decisiones

La metodología debe emitir decisiones separadas para:
- elegible para descubrir/investigar;
- elegible para clasificar tipología;
- elegible para valorar;
- elegible para evaluar riesgos;
- elegible para operar en ruta concreta;
- elegible para comparación/backtest.

Un activo puede investigarse aunque no pueda valorarse u operarse. La causa de cada restricción debe permanecer visible.

## 2. Estados de elegibilidad

- ELIGIBLE: cumple requisitos de la finalidad indicada.
- ELIGIBLE_WITH_LIMITATIONS: puede continuar con limitaciones explícitas.
- QUARANTINED: conflicto material pendiente de resolución.
- INELIGIBLE_FOR_SCOPE: no cumple requisito de esa finalidad.
- INSUFFICIENT_INFORMATION: falta evidencia para decidir.

No crear un estado global que oculte diferencias entre investigación, valoración y operación.

## 3. Screening: variables separadas

| Variable | Definición/precaución |
|---|---|
| Unit price | Precio por unidad de la representación y mercado concretos; no mide por sí solo valoración |
| Circulating supply | Unidades consideradas circulantes según definición y fuente documentadas |
| Total supply | Unidades emitidas menos las destruidas según semántica de fuente/contrato |
| Max supply | Límite máximo declarado o codificado; puede ser ilimitado, modificable o desconocido |
| Market capitalization | Precio de referencia multiplicado por supply circulante compatible; explicar metodología y limitaciones |
| Fully diluted valuation (FDV) | Precio por supply total/diluido definido por la fuente; no equivale a capitalización circulante ni valor razonable |
| Trading volume | Volumen reportado; requiere análisis de calidad, venues y posible wash trading |
| Executable liquidity | Profundidad y coste de ejecución para tamaño/ruta/mercado especificados |
| Concentration | Distribución de tenencia/control, con tratamiento de exchanges, bridges, contratos y direcciones no atribuibles |
| Emissions/unlocks | Flujos futuros conocidos en el instante de evaluación; calendario, fuente y cambios |
| Rights/redemption | Derechos efectivos del token, restricciones y mecanismos de redención |

No sumar variables de unidades distintas ni comparar sin normalización y definición.

## 4. Por qué precio nominal bajo no significa infravaloración

El precio por unidad depende de la denominación y cantidad de unidades. Un token con precio pequeño puede tener una oferta enorme y valoración agregada elevada; otro con precio unitario alto puede tener menor valoración agregada. Splits, redenominaciones, rebase y cambios de decimales alteran el precio unitario sin crear valor económico.

Por tanto:
- ranking de tokens por precio unitario solo describe denominación;
- drawdown, sobreventa técnica o RSI no demuestran infravaloración fundamental;
- market cap y FDV son métricas de tamaño/valoración implícita, no valor razonable;
- una métrica de supply desconocida no se reemplaza por cero;
- «barato» requiere comparación con valor razonable aplicable y evidencia suficiente, según el marco de infravaloración.

## 5. Casos especiales

- Rebase: registrar supply y balance en cada timestamp; el precio por unidad no es directamente comparable sin ajuste.
- Wrapped/bridged: analizar representación, respaldo y redención; no duplicar capitalización.
- Stablecoin: evaluar peg, reservas y redención; no usar el precio cercano a par como prueba de bajo riesgo.
- RWA: distinguir token, derecho jurídico, activo subyacente y vehículo.
- NFT/no fungible: no aplicar métricas de supply fungible sin adaptación.
- Supply desconocido o disputado: marcar UNKNOWN y bloquear cálculos que dependan de él.
- Max supply ausente o modificable: registrar como ilimitado/desconocido/modificable según evidencia, no inventar límite.

## 6. Filtros y límites

Cada filtro debe tener finalidad, fundamento, fuente, vigencia, efecto y prueba de sesgo. No establecer límites mínimos universales de precio, market cap, volumen, antigüedad o listings sin validación empírica. Si la operación exige liquidez, evaluarla contra el tamaño y ruta concretos; no usar un umbral arbitrario desconectado de la ejecución.

## 7. Salida de screening

Por activo y snapshot: identidad, representación, universo, elegibilidad por finalidad, variables de precio/supply/market cap/FDV/liquidez/concentración, calidad de datos, riesgos conocidos, campos UNKNOWN, motivo de cuarentena/exclusión y referencia a evaluación posterior.

El screening no asigna recomendación, no calcula valor razonable y no sustituye el análisis de riesgos.
