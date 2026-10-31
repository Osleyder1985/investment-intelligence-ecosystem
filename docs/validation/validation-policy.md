# Política de validación

## Objetivo

Comprobar si los métodos y señales aportan información útil fuera de la muestra usada para diseñarlos.

## Controles mínimos

- Separación temporal estricta entre desarrollo, validación y prueba.
- Walk-forward y out-of-sample.
- Prevención de look-ahead bias, survivorship bias, data leakage y selección retrospectiva.
- Inclusión de comisiones, spread, slippage, liquidez, impacto y restricciones de ejecución cuando corresponda.
- Benchmark apropiado y comparación con estrategias base.
- Métricas con definiciones, intervalos de incertidumbre y tamaños de muestra.
- Análisis por régimen de mercado y por tipología de activo.
- Tratamiento explícito de activos desaparecidos, suspendidos o sin datos.
- Registro de experimentos, parámetros, datasets, código y resultados.

## Criterio

Una prueba fallida o no concluyente debe publicarse como tal. No ajustar parámetros sobre el conjunto de prueba. No declarar eficacia por resultados retrospectivos aislados.

Los protocolos, benchmarks y criterios de aceptación específicos se definirán en Issues antes de ejecutar experimentos.
