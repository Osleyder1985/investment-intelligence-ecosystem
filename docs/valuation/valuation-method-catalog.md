# Catálogo de métodos de valoración de criptoactivos

**Estado:** Catálogo metodológico inicial; no valida modelos ni parámetros.  
**Versión:** 0.1.0

## 1. Objeto de valoración

Antes de calcular cualquier cifra, declarar qué se estima:

- Precio de mercado observable de una unidad en un mercado y momento concretos.
- Valor neto de activos o reservas atribuibles a un derecho específico.
- Valor económico de una reclamación o flujo al que el titular tenga derecho.
- Valor económico de uso/demanda del recurso o servicio.
- Valor de red o protocolo bajo una hipótesis explícita.
- Valor de una posición derivada, cesta o recibo de depósito.

Estas magnitudes no son intercambiables. Una estimación del valor de un protocolo no demuestra que ese valor pertenezca al token.

## 2. Familias de métodos

### 2.1 Valor neto de activos (NAV) y respaldo/redención
**Objeto:** valor de activos atribuibles a una participación o reclamación, menos pasivos y costes pertinentes.

**Aplicabilidad:** tokens respaldados por activos, recibos de depósito, participaciones en pools, determinados productos tokenizados y stablecoins con reservas verificables.

**Datos:** inventario de activos, titularidad, valoración, pasivos, gravámenes, custodia, segregación, liquidez, derechos de redención, costes y fecha de corte.

**Limitaciones:** el NAV contable o reportado puede no ser realizable; reservas no verificadas, activos ilíquidos, pasivos omitidos, riesgo de custodio y ausencia de derecho exigible pueden invalidar la inferencia.

### 2.2 Flujos de caja descontados atribuibles al titular
**Objeto:** valor presente de flujos que el titular del token puede recibir o reclamar de forma jurídicamente y operativamente sustentada.

**Aplicabilidad:** solo cuando existe un mecanismo verificable de distribución, reclamación o derecho económico atribuible al token.

**Datos:** flujo atribuible, prioridad, duración, frecuencia, moneda, retenciones, dilución, costes, riesgo de contraparte, escenarios y tasa de descuento justificada.

**Limitaciones:** no usar ingresos brutos del protocolo si no pertenecen al titular; cambios de gobernanza, discrecionalidad, riesgos jurídicos o ausencia de obligación pueden hacer inaplicable el método. No inventar tasas de descuento.

### 2.3 Valoración relativa / comparables
**Objeto:** estimación relativa frente a activos, redes, protocolos o derechos comparables.

**Aplicabilidad:** cuando los comparables comparten función económica, derechos, etapa, riesgo, estructura de oferta y condiciones de mercado suficientemente semejantes.

**Datos:** universo comparable, fecha, definición homogénea de métricas, calidad, ajustes por riesgo y diferencias estructurales.

**Limitaciones:** múltiplos observados reflejan precios, no necesariamente valor razonable; selección retrospectiva, comparables no equivalentes y denominadores inconsistentes distorsionan resultados. Informar dispersión y sensibilidad; no presentar un múltiplo sectorial arbitrario como verdad.

### 2.4 Enfoque de demanda económica y utilidad de red
**Objeto:** escenarios de demanda por uso de un recurso o servicio y su posible relación con la demanda del token.

**Aplicabilidad:** activos cuyo uso o tenencia sea necesario o económicamente relevante para acceder, liquidar o asegurar un servicio.

**Datos:** usuarios y uso verificables, tarifas, sustitutos, elasticidad, capacidad, competencia, mecanismo de compra/retención/quema, emisión y relación entre uso y demanda neta del token.

**Limitaciones:** actividad de red no equivale a valor capturado; transacciones automatizadas, incentivos, wash activity, subsidios y cambios de diseño pueden distorsionar. No convertir usuarios, TVL, transacciones o comisiones en valor del token sin puente económico demostrado.

### 2.5 Enfoque monetario / escenarios de reserva de valor
**Objeto:** escenarios explícitos de demanda monetaria, adopción, liquidez, oferta disponible y competencia.

**Aplicabilidad:** activos cuya tesis dominante sea monetaria o de reserva digital, sin flujos contractuales atribuibles.

**Datos:** oferta efectiva y futura, distribución, emisión, demanda y usos observables, sustitutos, seguridad, liquidez, restricciones y horizonte.

**Limitaciones:** depende de hipótesis de adopción y primas monetarias difíciles de observar; no produce un valor intrínseco preciso por sí solo. Modelar escenarios, no probabilidades inventadas. Los ratios stock-to-flow o similares no bastan como valoración independiente.

### 2.6 Paridad y convertibilidad
**Objeto:** desviación del precio respecto a una unidad de referencia y capacidad efectiva de conversión/redención.

**Aplicabilidad:** stablecoins y activos con mecanismo de paridad o redención.

**Datos:** precio por mercado, liquidez, derecho de redención, reservas/colateral, mecanismos de estabilización, costes, límites, horarios, jurisdicción y solvencia de contrapartes.

**Limitaciones:** la paridad objetivo no equivale a valor garantizado. Un precio cercano a paridad en un exchange no prueba solvencia ni acceso universal a redención.

### 2.7 Valor de liquidación / recuperación
**Objeto:** recuperación estimada para un derecho en escenarios de estrés o liquidación.

**Aplicabilidad:** activos respaldados, claims, estructuras con colateral y posiciones con prioridad de reclamación identificable.

**Datos:** activos realizables, pasivos, prioridad, costes, tiempo, jurisdicción, descuentos por iliquidez y escenarios de estrés.

**Limitaciones:** la prioridad jurídica puede ser incierta y el proceso puede ser lento, costoso o transfronterizo. No confundir valor de recuperación con precio normal de mercado.

### 2.8 Coste de producción o reposición
**Objeto:** coste de producir, asegurar o reproducir un recurso o servicio.

**Aplicabilidad:** análisis complementario de determinados sistemas de consenso o infraestructura.

**Datos:** energía, hardware, capital, operación, dificultad, competencia, eficiencia y restricciones.

**Limitaciones:** el coste del productor no establece un suelo de precio de mercado ni valor para el titular. No utilizar como método autónomo de valor razonable del token.

## 3. Métricas que no son métodos de valoración autónomos

Estas métricas pueden describir actividad, ciclo, valoración relativa o estructura, pero aisladas no estiman valor razonable:

- Market capitalization y fully diluted valuation (FDV).
- TVL, volumen, direcciones activas, usuarios, transacciones y comisiones.
- NVT, MVRV, realized capitalization y ratios derivados.
- Precio histórico, drawdown, volatilidad, RSI y otras señales técnicas.
- Rendimiento de staking sin ajustar por emisión, riesgo, bloqueo y costes.
- Coste de minería o producción.

Para usarlas en una estimación se debe especificar el mecanismo económico que conecta la métrica con el objeto valorado, sus supuestos y validación.

## 4. Magnitudes de mercado y suministro

- **Precio:** precio observado por unidad, mercado, par, timestamp y metodología.
- **Circulating market capitalization:** precio multiplicado por una estimación documentada de oferta circulante; la definición de circulante varía entre proveedores.
- **FDV:** precio multiplicado por una oferta totalmente diluida definida explícitamente; no equivale a valor de empresa ni predice capitalización futura.
- **Valor de red/protocolo:** concepto que exige definición; no asumir que equivale a capitalización del token.
- **NAV/reservas:** valor de activos y pasivos a una fecha; no prueba derecho del token.
- **Valor redimible:** importe al que un titular concreto puede redimir según términos y restricciones; puede diferir del NAV y del precio negociado.

Toda cifra debe acompañarse de fórmula, unidad, proveedor, timestamp, definición de oferta y limitaciones.

## 5. Protocolo de selección de método

1. Definir el objeto de valoración y unidad.
2. Confirmar identidad y clasificación económica del activo.
3. Verificar derechos del titular y mecanismo de captura de valor.
4. Evaluar disponibilidad, calidad, cobertura, licencia y fecha de los datos.
5. Consultar la matriz de aplicabilidad.
6. Documentar método principal, métodos alternativos y razones de descarte.
7. Especificar supuestos, horizonte, escenarios y sensibilidad.
8. Evaluar riesgos y contraevidencia.
9. Emitir rango solo si los datos y el método lo permiten.
10. Abstenerse de estimar si falta un vínculo económico o evidencia esencial.

## 6. Abstención y estados de salida

Estados iniciales propuestos:

- VALUATION_SUPPORTED: existe método aplicable y evidencia suficiente para estimar un rango.
- VALUATION_CONDITIONAL: estimación posible bajo condiciones o supuestos materiales explícitos.
- INSUFFICIENT_EVIDENCE: faltan datos o derechos verificables.
- NO_APPLICABLE_METHOD: no se identifica método defendible para el objeto solicitado.
- NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE: abstención explícita por falta de base suficiente.

Estos estados son propuestas de esquema, no señales de inversión ni umbrales aprobados. No generar precio objetivo puntual cuando el rango no sea defendible.
