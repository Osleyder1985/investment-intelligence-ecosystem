# Casos de prueba de identidad, universo y screening

**Estado:** Casos de diseño; pendientes de automatización y validación histórica.

## AS-001 — Ticker duplicado
- Entrada: dos contratos/redes con mismo ticker y nombres similares.
- Esperado: identidades separadas; AMBIGUOUS hasta corroborar cada una.
- Fallo: seleccionar por ranking del agregador.

## AS-002 — Token falso
- Entrada: contrato no reconocido que imita nombre y símbolo oficial.
- Esperado: UNVERIFIED/CONFLICTED; bloquear valoración y operación hasta resolver.
- Fallo: heredar identidad por ticker.

## AS-003 — Activo nativo
- Entrada: moneda nativa sin contrato.
- Esperado: identidad basada en red/chain_id y atributos propios; contract_address=NATIVE_ASSET.
- Fallo: inventar dirección de contrato.

## AS-004 — Wrapper y subyacente
- Entrada: token envuelto respaldado por activo en otra red.
- Esperado: IDs distintos, relación explícita, riesgo bridge/custodio y control de doble conteo.
- Fallo: sumar capitalizaciones como activos independientes sin fundamento.

## AS-005 — Migración de contrato
- Entrada: token antiguo migrado a nuevo contrato con ratio y fecha.
- Esperado: dos identidades relacionadas, evento de migración, continuidad solo con ajuste documentado.
- Fallo: sobrescribir dirección histórica.

## AS-006 — Fork
- Entrada: bifurcación con reglas o gobernanza distintas.
- Esperado: activos separados y universos independientes.
- Fallo: tratar ambos como la misma unidad.

## AS-007 — Activo deslistado y fallido
- Entrada: activo presente en universo histórico que desaparece después.
- Esperado: conservarlo en snapshot histórico y registrar salida/estado; no asignar precio cero por defecto.
- Fallo: eliminarlo de backtest por no existir hoy.

## AS-008 — Supply desconocido
- Entrada: circulating supply no verificable.
- Esperado: UNKNOWN; bloquear market cap derivada que dependa de ese dato.
- Fallo: sustituir supply por cero o estimación no etiquetada.

## AS-009 — Precio unitario bajo
- Entrada: token con precio nominal pequeño, supply elevado y precio dentro del rango de valor estimado.
- Esperado: no clasificar como infravalorado por precio unitario.
- Fallo: rankearlo como oportunidad por valer fracciones de unidad monetaria.

## AS-010 — FDV frente a market cap
- Entrada: supply total muy superior al circulante.
- Esperado: mostrar market cap y FDV por separado, con fuentes y supuestos.
- Fallo: sustituir market cap por FDV sin advertencia.

## AS-011 — Rebase
- Entrada: supply cambia por mecanismo rebase.
- Esperado: supply y balance timestamped; métricas normalizadas y comparabilidad explicada.
- Fallo: comparar precio unitario sin considerar cambios de unidades.

## AS-012 — Universo actual usado en el pasado
- Entrada: snapshot de activos actuales aplicado a periodo histórico.
- Esperado: invalidar backtest hasta reconstruir universo point-in-time o declarar sesgo residual.
- Fallo: excluir proyectos desaparecidos.

## AS-013 — Elegible para investigar, no para operar
- Entrada: identidad y datos suficientes para investigación, pero no hay mercado/custodia/ruta accesible.
- Esperado: ELIGIBLE para investigación y INELIGIBLE_FOR_SCOPE para operación.
- Fallo: confundir analizable con comprable.

## AS-014 — Umbral de volumen arbitrario
- Entrada: filtro mínimo sin fundamento ni validación.
- Esperado: rechazar filtro o exigir justificación específica de finalidad.
- Fallo: excluir silenciosamente activos.

## Criterio común

Comprobar identidad, snapshot, finalidad, estado, evidencia, variables, licencias, tratamiento histórico, motivo y ausencia de selección retrospectiva. Estos casos verifican consistencia metodológica, no rentabilidad.
