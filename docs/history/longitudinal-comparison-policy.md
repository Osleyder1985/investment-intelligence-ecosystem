# Política de comparación longitudinal de evaluaciones

**Estado:** Propuesta metodológica; pendiente de validación. **Versión:** 0.1.0

## 1. Propósito

Explicar qué cambió entre dos evaluaciones sin confundir cambios del activo/mercado con cambios de medición, fuentes, cobertura, universo, modelos o políticas. La comparación es informativa y no demuestra causalidad.

## 2. Estados de comparabilidad

- **COMPARABLE:** identidad, representación, finalidad, horizonte, unidades, alcance y versiones relevantes permiten comparación directa o ajuste documentado.
- **PARTIALLY_COMPARABLE:** existe una base común, pero algunos componentes cambiaron o faltan; mostrar solo diferencias válidas y excluir explícitamente las no comparables.
- **NOT_COMPARABLE:** cambios de identidad, unidad, alcance, metodología u otros factores impiden una comparación defendible.

La decisión debe incluir razones, campos incompatibles, componentes comparables y componentes excluidos. No convertir comparabilidad parcial en una cifra agregada sin justificación.

## 3. Descomposición obligatoria del cambio

Clasificar cada diferencia material en una o varias categorías:
1. **Market/asset change:** precio, oferta, actividad, derechos, incidentes, liquidez o evento del activo observados entre fechas.
2. **Source/data change:** cambio de proveedor, definición, revisión, corrección, timestamp, licencia o método de captura.
3. **Coverage change:** aparición/desaparición de periodos, mercados, redes, contratos o datos.
4. **Methodology/policy change:** modificación de reglas de elegibilidad, riesgo, veto, valoración, infravaloración, decisión o ranking.
5. **Model/parameter change:** cambio de estructura, versión, calibración, parámetros o supuestos.
6. **Universe/composition change:** cambios en snapshot, inclusión/exclusión y criterios del universo.
7. **Execution/scope change:** mercado, par, jurisdicción, tamaño de operación, comisiones, liquidez o restricciones.
8. **Unresolved difference:** diferencia cuya causa no puede identificarse con la evidencia disponible.

No atribuir automáticamente el cambio de resultado a la evolución del activo. Si se actualizó el modelo, separar el efecto metodológico del cambio observado, mediante reejecución controlada cuando sea posible.

## 4. Comparación de métricas y rangos

Solo comparar valores con la misma definición, unidad, moneda, objeto, timestamp de corte y cobertura compatible. Mostrar por separado:
- valor anterior y actual;
- unidad/moneda;
- fuente y versión;
- timestamp;
- delta absoluto/relativo solo si la operación matemática es válida;
- incertidumbre y limitaciones;
- explicación de cambios de definición.

No calcular porcentaje de cambio si el denominador es cero, desconocido o no comparable. No equiparar una variación de precio con variación del valor razonable. El rango de valoración debe compararse junto con su modelo, inputs, escenarios y supuestos.

## 5. Cambios de estado y ranking

Explicar cada transición de decisión, veto, riesgo, infravaloración o dominancia con:
- estado anterior y nuevo;
- fecha efectiva y fecha de conocimiento;
- evidencia nueva/corregida;
- versión de política/modelo;
- causa atribuible o no atribuible;
- impacto en el alcance y horizonte;
- incertidumbre residual.

La posición del ranking solo se compara si universo, horizonte, protocolo, finalidad y dimensiones son compatibles. En caso contrario, informar que la posición no es directamente comparable y explicar por qué.

## 6. Presentación al usuario

El informe longitudinal debe mostrar:
- resumen de comparabilidad;
- cambios observados del activo/mercado;
- cambios de datos y cobertura;
- cambios metodológicos/modelo/parámetros;
- transiciones de gates/vetos/decisión;
- cambios de rango y supuestos;
- cambios de grupo/posición con límites;
- diferencias no resueltas;
- evidencias y enlaces a ambos snapshots.

## 7. Reproducibilidad

Guardar evaluation_id anterior y actual, comparación_id, política/versión, campos comparados, reglas de compatibilidad, transformaciones, hashes y resultados. Una comparación nueva no altera los snapshots de origen.
