# Política de construcción del universo de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Propósito

Definir qué activos podían ser considerados en cada ejecución y reconstruir universos históricos sin sesgo de supervivencia. El universo es una instantánea fechada, no una lista actual aplicada retroactivamente.

## 2. Tipos de universo

- DISCOVERY_UNIVERSE: candidatos descubiertos en fuentes registradas.
- RESEARCH_UNIVERSE: activos con identidad suficiente para investigación.
- VALUATION_UNIVERSE: activos con derechos, datos y método aptos para estimar valor.
- OPERATIONAL_UNIVERSE: activos que pueden operarse por la ruta, mercado, custodio y jurisdicción evaluados.
- HISTORICAL_UNIVERSE: conjunto de activos existentes/conocidos en una fecha histórica, incluidos los que después desaparecieron.
- BENCHMARK_UNIVERSE: conjunto definido por reglas del benchmark y su versión.

La pertenencia a un universo no implica recomendación de inversión.

## 3. Construcción de snapshot

Cada snapshot debe registrar:
- universe_id, versión del protocolo y timestamp UTC;
- decision_time y finalidad;
- fuentes, licencias, cobertura y availability_time;
- canonical_asset_id y representation_id;
- estado de identidad;
- inclusión, exclusión o cuarentena y motivo;
- evidencia y transformaciones;
- activos no resueltos y fuentes ausentes;
- cambios frente al snapshot anterior.

La misma regla y datos versionados deben permitir reproducir el snapshot. No reemplazar snapshots anteriores.

## 4. Inclusión y exclusión

Incluir candidatos que cumplan identidad y alcance mínimo necesario para la finalidad. La falta de datos no equivale a exclusión silenciosa: registrar UNKNOWN o QUARANTINED. Excluir solo por regla documentada, evidencia y alcance.

Los criterios pueden depender de la finalidad:
- Investigación: puede admitir identidad parcial y datos incompletos si se declara la limitación.
- Valoración: exige identidad, derechos, método y datos esenciales suficientes.
- Operación: exige ruta real de compra/venta, custodia, mercado y acceso legal/operativo.
- Backtesting: exige disponibilidad point-in-time, universo histórico y datos de supervivencia/deslistado.

No establecer mínimos universales de capitalización, volumen, antigüedad o número de mercados sin validación y propósito específico.

## 5. Historial y survivorship bias

Reconstruir membresía del universo en cada fecha con datos conocidos entonces. Conservar:
- activos deslistados o sin mercados activos;
- proyectos cerrados, fallidos, abandonados o insolventes;
- tokens migrados, redenominados o reemplazados;
- contratos y representaciones que dejaron de operar;
- causas y fechas de salida, si son conocidas.

No asumir que un activo desaparecido tenía precio cero. Registrar último dato válido, estado, fecha de deslistado y ausencia posterior. Si la información histórica no existe, declarar sesgo residual y su efecto.

## 6. Doble conteo y agrupación económica

Mantener por separado activo económico y representaciones negociables. El snapshot puede agrupar exposiciones relacionadas para ranking agregado, pero:
- no sumar capitalizaciones duplicadas;
- no contar supply bloqueado y wrapper como dos unidades económicas;
- no fusionar mercados de distintas representaciones sin justificar;
- preservar riesgos y liquidez específicos;
- documentar reglas para elegir una representación operable sin eliminar alternativas.

## 7. Estados de membresía

- INCLUDED: cumple criterios de la finalidad.
- INCLUDED_WITH_LIMITATIONS: entra con limitaciones visibles.
- QUARANTINED: identidad, datos, licencia o conflicto pendiente.
- EXCLUDED: regla de exclusión documentada.
- HISTORICAL_ONLY: se conserva para historia/backtest, no operable actualmente.
- UNRESOLVED: no hay evidencia suficiente para resolver membresía.

Registrar fecha de inicio y fin de vigencia; no cambiar el pasado sin nueva versión.

## 8. Salida y auditoría

Publicar listado de incluidos, limitados, cuarentena, excluidos e históricos, con conteos y razones trazables. Informar cobertura, datos ausentes, licencias pendientes, representaciones agrupadas y limitaciones. No ordenar por precio nominal como proxy de oportunidad.
