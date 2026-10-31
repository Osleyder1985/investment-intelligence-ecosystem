# Casos de prueba del workflow de ejecución

**Estado:** Casos de diseño; pendientes de automatización y validación técnica.

## EX-001 — Sin solicitud explícita
- Entrada: no existe acción/autorización del usuario.
- Esperado: ninguna ejecución ni captura inicia.
- Fallo: iniciar evaluación periódica o implícita.

## EX-002 — Solicitud inválida
- Entrada: activo/alcance insuficiente para resolver instrumento.
- Esperado: REJECTED o solicitud de aclaración; sin etapas analíticas.
- Fallo: adivinar activo por ticker.

## EX-003 — Identidad no resuelta
- Entrada: contrato/red en conflicto.
- Esperado: bloqueo de valoración y ranking dependientes; expediente de abstención si procede.
- Fallo: continuar con activo supuesto.

## EX-004 — Fuente transitoria recuperable
- Entrada: timeout temporal y contrato de fuente permite reintento.
- Esperado: reintento sujeto a política/deadline; todos los intentos trazados.
- Fallo: reintento infinito o ignorar Retry-After.

## EX-005 — Error permanente
- Entrada: endpoint retirado o autenticación inválida.
- Esperado: no reintentar como transitorio; fallo o limitación visible.
- Fallo: repetir indiscriminadamente.

## EX-006 — Restricción de licencia
- Entrada: fuente no autoriza captura/almacenamiento para ese uso.
- Esperado: detener operación afectada y registrar restricción.
- Fallo: eludir ToS o guardar datos prohibidos.

## EX-007 — Fallo de fuente crítica
- Entrada: fuente crítica no responde y no hay sustituto admisible.
- Esperado: PARTIAL/ABSTAINED o FAILED según alcance; nunca COMPLETED silencioso.
- Fallo: ocultar ausencia en el informe.

## EX-008 — Fuente secundaria no crítica
- Entrada: fuente secundaria falla, otras fuentes permiten conclusión limitada.
- Esperado: COMPLETED_WITH_LIMITATIONS con cobertura/faltante explícitos si política lo permite.
- Fallo: reportar completitud total.

## EX-009 — Veto activo
- Entrada: VETO_ACTIVE aplicable.
- Esperado: no emitir conclusión favorable; detener etapas opcionales no necesarias o continuar diagnóstico permitido.
- Fallo: ranking favorable por descuento.

## EX-010 — Solicitudes repetidas técnicamente
- Entrada: reintento de la misma operación interna con igual idempotency_key.
- Esperado: resultado idempotente sin duplicar captura/escritura.
- Fallo: dos capturas idénticas no trazadas.

## EX-011 — Nueva solicitud del usuario
- Entrada: nuevo request_id con mismos parámetros que una evaluación anterior.
- Esperado: nuevo expediente; cualquier dato reutilizado debe justificarse y referenciarse.
- Fallo: reemplazar o reescribir evaluación previa.

## EX-012 — Cancelación en cola
- Entrada: usuario cancela antes de ejecución.
- Esperado: CANCELLED, sin captura iniciada.
- Fallo: ejecución posterior no autorizada.

## EX-013 — Cancelación durante captura
- Entrada: usuario cancela con adaptadores activos.
- Esperado: propagación, aislamiento de respuestas tardías y persistencia del estado.
- Fallo: incluir resultados tardíos no verificados.

## EX-014 — Recuperación tras reinicio
- Entrada: worker se interrumpe después de varias etapas completadas.
- Esperado: verificar hashes y reanudar solo etapas seguras; preservar outputs previos.
- Fallo: mezclar datos de una ejecución nueva.

## EX-015 — Concurrencia de fuentes
- Entrada: varias fuentes independientes autorizadas.
- Esperado: paralelismo sin mezclar contextos y respetando políticas de proveedor.
- Fallo: carrera que sobrescribe manifiestos.

## EX-016 — Fallo de persistencia
- Entrada: escritura de artefacto o manifiesto falla.
- Esperado: no marcar COMPLETED; reconciliar por IDs/hashes o FAILED.
- Fallo: informe sin snapshot recuperable.

## EX-017 — Etapa no aplicable
- Entrada: método no aplicable por tipología.
- Esperado: SKIPPED_NOT_APPLICABLE con motivo; no fallo técnico.
- Fallo: inventar resultado del modelo.

## EX-018 — Timeout de fuente
- Entrada: deadline derivado de política/SLA expirado.
- Esperado: fuente marcada timeout, efecto sobre cobertura y estado final visible.
- Fallo: timeout interpretado como cero o respuesta vacía válida.

## EX-019 — Mezcla de versiones
- Entrada: parámetros/modelo cambian mientras una ejecución está activa.
- Esperado: ejecución mantiene versiones congeladas; nueva versión solo en otra evaluación.
- Fallo: outputs con versiones mezcladas.

## EX-020 — Resultado parcial
- Entrada: etapas principales completadas y una etapa requerida falla.
- Esperado: PARTIAL o COMPLETED_WITH_LIMITATIONS según criterios documentados.
- Fallo: convertir estado parcial en COMPLETED.

## Criterio común
Verificar consentimiento explícito, precondiciones, transiciones, idempotencia, aislamiento, licencias, timestamps, hashes, estado final y reproducibilidad. Estos casos son especificaciones, no pruebas automatizadas ejecutadas.
