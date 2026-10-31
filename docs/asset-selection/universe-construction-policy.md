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


## 9. Cobertura, conocimiento y tiempo de membresía (aclaración v0.2.0)

Un snapshot debe distinguir al menos:

- `decision_time`: instante al que se refiere la decisión;
- `snapshot_created_at`: momento real de generación del snapshot;
- `knowledge_cutoff`: límite temporal de información admisible;
- `valid_from` / `valid_to`: vigencia efectiva de una característica o membresía;
- `availability_time`: primer momento demostrable en que la evidencia pudo conocerse por la vía definida;
- `capture_time`: momento de captura por el sistema.

No intercambiar estos campos. Un snapshot reconstruido posteriormente debe identificarse como tal y conservar su fecha real de creación.

### 9.1 Cobertura y completitud

Cada snapshot debe declarar el alcance de descubrimiento: redes, tipos de activos, venues, fuentes, periodos, endpoints, filtros, páginas/rangos consultados y fallos de consulta. Registrar cobertura conocida, fuentes ausentes, límites de paginación/cuotas y activos no resueltos. La ausencia de un activo en un proveedor no demuestra inexistencia, deslistado ni inelegibilidad.

No etiquetar el universo como exhaustivo salvo que exista método de enumeración y evidencia de cobertura suficientes para ese alcance. Cuando no pueda demostrarse, usar `COVERAGE_INCOMPLETE` o una limitación equivalente y describir el sesgo residual.

### 9.2 Eventos de membresía

Cada transición debe ser un evento inmutable con event_id, universe_id, entity IDs, estado anterior/nuevo, motivo, effective_time, knowledge_time, source/evidence refs, protocolo y versión. Eventos posibles incluyen DISCOVERED, INCLUDED, INCLUDED_WITH_LIMITATIONS, QUARANTINED, EXCLUDED, DELISTED, MIGRATED, RETIRED y UNRESOLVED. No inferir DELISTED solo por ausencia en un proveedor.

### 9.3 Reconstrucción histórica

La membresía en fecha t solo puede utilizar evidencia admisible en t según el modo point-in-time definido. La fecha efectiva de un hecho puede ser anterior a su fecha de conocimiento. Conservar ambas. Si la fecha de conocimiento no puede demostrarse, marcarla UNKNOWN y no usarla para afirmar que el sistema o el mercado la conocían entonces.

Los activos desaparecidos deben conservarse cuando exista evidencia histórica. Distinguir último precio observado, última cotización ejecutable, redención/recuperación y resultado final; ninguno equivale automáticamente a cero. Si la historia de deslistados/fallos es incompleta, declarar el límite en resultados y validaciones.

### 9.4 Versionado y comparabilidad

Cambios en fuentes, filtros, cobertura, reglas de identidad o criterios de elegibilidad crean una nueva versión de protocolo/snapshot. No reescribir la membresía pasada. Comparaciones longitudinales deben separar cambios reales del universo de cambios en cobertura o metodología.
