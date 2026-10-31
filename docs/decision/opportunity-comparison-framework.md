# Marco de comparación de oportunidades

**Estado:** Propuesta metodológica; pendiente de calibración y validación. **Versión:** 0.1.0

## 1. Unidad de comparación

Una observación comparable se identifica por activo canónico, representación, mercado/par, moneda de cotización, timestamp UTC, finalidad, jurisdicción/alcance, horizonte y snapshot del universo. No comparar evaluaciones con diferencias materiales en estos campos.

Distinguir último precio, precio medio, bid/ask y precio ejecutable estimado para el tamaño de operación considerado. El precio ejecutable debe incorporar, cuando sea posible, spread, comisiones, slippage, impacto, profundidad, límites de retiro/depósito y restricciones del usuario. Si no puede estimarse, declarar la limitación; no sustituirlo silenciosamente por el último precio.

## 2. Dimensiones de comparación

No se suman automáticamente. Cada dimensión conserva unidad, dirección, ventana, cobertura, timestamp, fuente y limitación.

| Dimensión | Qué representa | Dirección interpretativa | Precauciones |
|---|---|---|---|
| Brecha frente al valor | Relación entre referencia de mercado y rango de valor defendible | Una brecha favorable puede apoyar una tesis | No es retorno esperado; depende de modelo, derechos, unidad y rango |
| Robustez de valoración | Estabilidad ante escenarios, sensibilidad y métodos alternativos | Mayor robustez reduce fragilidad | No equivale a exactitud predictiva |
| Calidad/cobertura de evidencia | Admisibilidad, linaje, completitud, oportunidad y conflictos | Mejor evidencia reduce incertidumbre epistemológica | No convertir en score sin calibración |
| Riesgo residual | Riesgos que permanecen tras controles y mitigaciones | Menor riesgo residual puede ser preferible | Riesgos no son intercambiables ni siempre cuantificables |
| Liquidez y ejecución | Capacidad de entrar/salir y coste de hacerlo | Mayor liquidez y menor coste suelen facilitar ejecución | Depende de tamaño, mercado, jurisdicción y momento |
| Captura de valor/derechos | Mecanismo verificable que conecta actividad/activos con el titular | Derechos más claros pueden sustentar valoración | No asumir que valor del protocolo pertenece al token |
| Oferta y dilución | Emisión, unlocks, supply elegible y concentración | Presión dilutiva puede deteriorar tesis | Supply desconocido permanece UNKNOWN |
| Catalizadores y eventos | Eventos potencialmente relevantes, fecha, estado y evidencia | Pueden modificar escenarios | Separar anunciado, confirmado, rumoreado e hipótesis; correlación no prueba causalidad |
| Horizonte y encaje temporal | Coherencia entre tesis, calendario y horizonte | Debe corresponder al horizonte evaluado | No mezclar corto, medio y largo |
| Incertidumbre | Datos, parámetros, estructura, escenarios, derechos y ejecución | Menor incertidumbre material puede mejorar comparabilidad | No comprimir fuentes en cifra arbitraria |

## 3. Estado de evidencia por dimensión

Cada dimensión debe etiquetarse OBSERVED, DERIVED, ESTIMATED, CALIBRATED, ASSUMPTION o UNKNOWN. Registrar además unidad/escala, fecha de observación y disponibilidad, periodo, fuente/admisibilidad, transformación/código, dirección interpretativa, aplicabilidad, incertidumbre y limitaciones.

UNKNOWN no es neutro, medio, cero ni favorable. No imputar valores para facilitar ranking sin política validada y señalización visible.

## 4. Desacuerdo entre métodos y escenarios

- Mostrar resultados separados cuando objeto, derechos o supuestos difieran.
- Agregar solo si los métodos valoran el mismo objeto y unidad, son aplicables y existe procedimiento validado que trate dependencia y sesgo.
- No promediar rangos por defecto.
- Mantener escenarios BASELINE, ADVERSE, FAVOURABLE, STRESS y ALTERNATIVE_MODEL separados, sin probabilidades inventadas.
- Si métodos igualmente aplicables producen conclusiones materialmente contradictorias, degradar a CONDITIONAL_CANDIDATE, NO_COMPARABLE_OPPORTUNITY o INSUFFICIENT_EVIDENCE según la causa.
- Registrar contraevidencia, hipótesis alternativas y observaciones futuras que podrían discriminarlas.

## 5. Compatibilidad y segmentación

Verificar misma finalidad y criterio de elegibilidad; horizonte compatible; unidad económica y moneda; referencia de mercado y timestamp; aplicabilidad de modelos al objeto; alcance de riesgos/jurisdicción; y calidad/cobertura suficientes.

Si no se cumple, separar grupos o declarar no comparabilidad. No normalizar diferencias estructurales solo para obtener una tabla ordenada.

## 6. Salida comparativa

Por activo y horizonte presentar: estado de gates y vetos; estado de valoración e infravaloración; rango y moneda/unidad, si son defendibles; precio de referencia y mercado/timestamp; coste/liquidez y limitaciones; evidencia y cobertura; riesgos residuales y desconocidos; catalizadores y nivel de evidencia; escenarios y contraevidencia; estado comparativo y razón de posición/grupo; condiciones que cambiarían la conclusión.

La tabla no debe ocultar restricciones. Vetos y bloqueos aparecen antes de cualquier clasificación.
