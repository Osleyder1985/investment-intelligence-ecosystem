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
- Entrada: activo nativo sin contrato en una red cuya identidad externa puede colisionar con la de otra red.
- Esperado: `network_id` namespaced + discriminador tipado `NATIVE_ASSET`; `chain_id` se conserva solo como atributo namespaced cuando aplique; el campo de dirección permanece ausente/no aplicable.
- Fallo: usar `chain_id` como clave global, insertar `NATIVE_ASSET` en `contract_address` o inventar una dirección.

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


## Casos adicionales derivados de la revisión fundacional

## AS-015 — Colisión de chain_id entre redes
- Entrada: dos redes distintas que comparten un identificador numérico de cadena.
- Esperado: network_id interno namespaced distinto; representaciones separadas.
- Fallo: usar chain_id como clave global única.

## AS-016 — Red no EVM y activo nativo
- Entrada: activo nativo identificado mediante reglas propias de una red no EVM.
- Esperado: identificador tipado por network_id y esquema de red; no inventar contract address.
- Fallo: forzar formato EVM o colisionar con dirección válida.

## AS-017 — Proxy con cambio de implementación
- Entrada: proxy estable cuya implementación cambia en bloque/fecha posterior.
- Esperado: conservar proxy, implementación y evento versionado; evaluar impacto en comportamiento/derechos.
- Fallo: sobrescribir la implementación histórica o asumir que nada cambió.

## AS-018 — Proyecto y activo distintos
- Entrada: un protocolo con varios tokens y una entidad que emite un token sin ser propietaria del protocolo.
- Esperado: project_id, issuer y asset/representation IDs separados.
- Fallo: inferir propiedad o identidad desde nombre del proyecto.

## AS-019 — Universo de proveedor incompleto
- Entrada: fuente discovery con cobertura parcial, paginación fallida o límite de API.
- Esperado: snapshot con cobertura incompleta, fallo de consulta y limitación explícita; no afirmar exhaustividad.
- Fallo: tratar ausencia de resultado como inexistencia del activo.

## AS-020 — Evento de membresía conocido después
- Entrada: deslistado efectivo en t1, evidencia pública o autorizada solo demostrable en t2 posterior.
- Esperado: conservar `effective_time=t1` y el timestamp de conocimiento/disponibilidad aplicable en t2, identificando la vía; no usarlo como conocido en t1 ni confundirlo con `system_ingestion_time`.
- Fallo: retrotraer el conocimiento o colapsar disponibilidad pública, autorizada e ingestión interna.

## AS-021 — Precio último frente a resultado de salida
- Entrada: activo desaparecido con última cotización antigua y sin mercado ejecutable posterior.
- Esperado: distinguir último precio, última liquidez ejecutable y recuperación/redención; incertidumbre residual explícita.
- Fallo: tratar último precio como valor realizable o asignar cero sin evidencia.

## AS-022 — Reutilización de ticker y migración
- Entrada: ticker reutilizado por nuevo proyecto después de migración o abandono.
- Esperado: identidades distintas y relaciones temporales explícitas.
- Fallo: unir series por ticker.

## AS-023 — Representación multi-colateral
- Entrada: token respaldado por cesta de activos o dependiente de varias entidades.
- Esperado: relaciones BACKED_BY múltiples con pesos/condiciones y vigencia si se conocen; UNKNOWN si no.
- Fallo: forzar un único subyacente sin evidencia.


## AS-024 — Una representación no puede pertenecer a dos redes
- Entrada: un mismo registro de representación asociado a dos network_id distintos.
- Esperado: rechazar la asignación dual; crear identidades separadas si son despliegues/representaciones diferentes y relacionarlas cuando corresponda.
- Fallo: resolver ambas por ticker o dirección sin namespace.

## AS-025 — Instrumentos con igual ticker y especificaciones distintas
- Entrada: dos mercados con el mismo símbolo visible, pero distinto venue, tipo de mercado, activo base, unidad de cotización o especificación contractual.
- Esperado: instrument_id distintos; las diferencias relevantes forman parte de la clave del instrumento.
- Fallo: fusionar series por el símbolo del par.

## AS-026 — Listing retirado y relisting
- Entrada: un instrumento retirado y posteriormente admitido de nuevo con nuevo identificador/episodio o condiciones.
- Esperado: conservar episodios de listing separados y enlazados al instrumento cuando la identidad del instrumento no haya cambiado.
- Fallo: sobrescribir la fecha de admisión o asumir continuidad de liquidez.

## AS-027 — Relación económica multi-subyacente
- Entrada: representación que declara exposición o respaldo a varios activos, sin pesos verificables.
- Esperado: varias relaciones tipadas; composición cuantitativa UNKNOWN y valoración dependiente bloqueada o limitada.
- Fallo: forzar un único subyacente o repartir pesos por igual.

## AS-028 — Conflicto temporal de relaciones
- Entrada: dos fuentes presentan relaciones incompatibles con periodos efectivos solapados.
- Esperado: conservar ambas afirmaciones, marcar DISPUTED/CONFLICTED y abstenerse del uso que dependa de resolver la contradicción.
- Fallo: preferir automáticamente la fuente más reciente o el proveedor de mayor ranking.

## AS-029 — Proxy con varias implementaciones históricas
- Entrada: proxy con varios upgrades y una implementación actual distinta de la histórica.
- Esperado: conservar cada implementación con bloque/tiempo, autoridad/mecanismo y evidencia; mantener el proxy como entidad diferenciada.
- Fallo: sustituir la implementación histórica por la actual en una reconstrucción pasada.

## AS-030 — Ciclo en relaciones del grafo
- Entrada: ciclo detectado en MIGRATES_TO o en una relación de composición/respaldo.
- Esperado: evaluar según semántica y periodo del tipo de relación; marcar sospecha para migración cíclica y permitir ciclos legítimos de otras clases si la evidencia los respalda.
- Fallo: prohibir todos los ciclos o eliminarlos automáticamente.

## AS-031 — Identificador externo reutilizado
- Entrada: venue/proveedor reutiliza un símbolo o ID externo después de retirar el objeto anterior.
- Esperado: conservar namespace, versión/periodo y entidades históricas distintas cuando corresponda.
- Fallo: aplicar unicidad global al identificador externo sin alcance temporal.

## AS-032 — Emisor, administrador y controlador distintos
- Entrada: emisor inicial, administrador operativo y controlador del proxy son entidades diferentes.
- Esperado: roles independientes, temporales y sustentados por evidencia; UNKNOWN cuando no se conozca.
- Fallo: inferir propiedad/control desde el nombre del proyecto o del emisor.
