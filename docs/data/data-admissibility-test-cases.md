# Casos de prueba de calidad, procedencia y admisibilidad

**Estado:** Casos de diseño; pendientes de automatización y validación con datos reales.

## DQ-001 — Dato publicado después del corte
- Entrada: decision_time anterior a publication_time/availability_time.
- Esperado: excluir del análisis histórico; registrar posible leakage.
- Fallo: usarlo como conocido en el instante de decisión.

## DQ-002 — Captura posterior con publicación anterior
- Entrada: publicación verificable antes de t, captura después de t.
- Esperado: admisible en RECONSTRUCTION_POINT_IN_TIME si se demuestra disponibilidad pública antes de t y se etiqueta como reconstrucción retrospectiva; no admisible en REPLAY_OPERATIONAL si el sistema aún no la había ingerido.
- Fallo: confundir capture_time con availability_time o asumir disponibilidad sin evidencia.

## DQ-003 — Timestamp desconocido
- Entrada: registro sin publication_time ni availability_time verificables.
- Esperado: UNKNOWN o exclusión cuando pueda introducir look-ahead.
- Fallo: asignar timestamp de captura como sustituto.

## DQ-004 — Revisión retrospectiva del proveedor
- Entrada: proveedor corrige un dato histórico después de decision_time.
- Esperado: conservar ambas versiones y no usar revisión posterior en reconstrucción point-in-time.
- Fallo: sobrescribir la versión conocida entonces.

## DQ-005 — Unidades incompatibles
- Entrada: supply en unidades base frente a unidades enteras, o precios en monedas distintas sin conversión temporal definida.
- Esperado: QUARANTINED hasta normalización documentada.
- Fallo: comparar directamente.

## DQ-006 — Fuentes discordantes
- Entrada: dos proveedores reportan valores distintos con cobertura/metodología diferentes.
- Esperado: investigar definiciones y timestamps, preservar conflicto y abstenerse si es material.
- Fallo: promediar sin fundamento.

## DQ-007 — Gap en serie temporal
- Entrada: periodo sin observaciones o discontinuidad no explicada.
- Esperado: marcar cobertura incompleta y evitar métricas que requieran continuidad.
- Fallo: rellenar con cero o interpolar silenciosamente.

## DQ-008 — Outlier potencialmente real
- Entrada: movimiento extremo corroborado por operaciones/eventos.
- Esperado: conservar y etiquetar; investigar, no recortar automáticamente.
- Fallo: eliminar por regla estadística sin análisis.

## DQ-009 — Duplicados
- Entrada: mismo registro/evento repetido por reintento o proveedor.
- Esperado: detectar por clave de identidad y conservar linaje de deduplicación.
- Fallo: doble conteo.

## DQ-010 — Cambio de schema
- Entrada: proveedor cambia unidad, campo o semántica sin versionado del consumidor.
- Esperado: poner registros afectados en cuarentena hasta adaptar y validar parser.
- Fallo: interpretar nuevo campo como el anterior.

## DQ-011 — Licencia restrictiva
- Entrada: fuente accesible pero términos no permiten almacenamiento o redistribución previstos.
- Esperado: TERMS_PENDING/RESTRICTED o REJECTED para ese uso; no capturar/redistribuir sin permiso.
- Fallo: inferir permiso porque API es gratuita.

## DQ-012 — Fuente oficial incompleta
- Entrada: documento oficial que no cubre todos los campos requeridos.
- Esperado: admitir solo campos cubiertos y marcar restantes UNKNOWN.
- Fallo: tratar autoridad de la fuente como completitud total.

## DQ-013 — Dato derivado sin linaje
- Entrada: indicador sin fórmula, inputs, versión o parámetros.
- Esperado: REJECTED o QUARANTINED para conclusión reproducible.
- Fallo: aceptar cifra no trazable.

## DQ-014 — On-chain indexado con retraso
- Entrada: evento de bloque con indexación posterior al timestamp del bloque.
- Esperado: separar block/event time de availability_time del indexador.
- Fallo: asumir que el evento estaba observable en tiempo real para todos.

## DQ-015 — Restricción de retención
- Entrada: términos permiten consulta y análisis pero prohíben conservar payload bruto.
- Esperado: no persistir payload; guardar solo metadatos/hash/referencias permitidas y documentar reproducibilidad limitada.
- Fallo: conservar datos prohibidos o afirmar reproducción íntegra.

## Criterio común

Verificar estado de admisibilidad, fuente, licencia, timestamps, unidades, transformaciones, evidencia, motivo, impacto en evaluación y posibilidad de reproducir el resultado. Estos casos prueban consistencia de controles, no calidad perfecta.


## Casos adicionales derivados de la revisión fundacional

## DQ-016 — Disponibilidad pública frente a ingestión interna
- Entrada: dato publicado antes de t, pero ingerido por el sistema después de t.
- Esperado: admisible solo para RECONSTRUCTION_POINT_IN_TIME si la disponibilidad pública está demostrada; no admisible para REPLAY_OPERATIONAL.
- Fallo: confundir ambos modos.

## DQ-017 — Timestamp de disponibilidad sin evidencia
- Entrada: availability_time inferido únicamente desde capture_time.
- Esperado: UNKNOWN; bloquear afirmaciones históricas dependientes.
- Fallo: convertir la hora de captura en disponibilidad pública.

## DQ-018 — Hash sin procedencia
- Entrada: payload con hash pero sin fuente, fecha, endpoint ni evidencia de origen.
- Esperado: hash solo acredita comparación de integridad del objeto disponible; procedencia y disponibilidad UNKNOWN.
- Fallo: tratar hash como prueba de autenticidad.

## DQ-019 — Términos por operación
- Entrada: términos permiten consulta y análisis, pero no retención o redistribución.
- Esperado: decisión separada por operación; bloquear solo usos no autorizados y no persistir payload restringido.
- Fallo: aprobación global de proveedor.

## DQ-020 — Indexador con retraso
- Entrada: evento on-chain en bloque t1, indexado en t2 e ingerido en t3.
- Esperado: conservar los tres tiempos y la finalidad de cada uno; replay exige ingestión antes del corte.
- Fallo: asignar disponibilidad del indexador al timestamp de bloque.

## DQ-021 — Dato derivado con insumo tardío
- Entrada: indicador calculado con un insumo que estuvo disponible después de los demás.
- Esperado: availability del derivado no anterior al insumo más tardío ni al cálculo terminado.
- Fallo: fechar el derivado con el primer insumo.

## DQ-022 — Fuente con cobertura parcial
- Entrada: endpoint no devuelve todas las páginas o redes esperadas.
- Esperado: cobertura incompleta y consulta fallida/parcial explícita; no afirmar exhaustividad.
- Fallo: interpretar ausencia de filas como ausencia de activos.
