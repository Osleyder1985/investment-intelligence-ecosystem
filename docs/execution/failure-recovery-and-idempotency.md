# Política de fallos, recuperación e idempotencia

**Estado:** Propuesta metodológica; pendiente de implementación. **Versión:** 0.1.0

## 1. Clasificación de fallos

| Clase | Ejemplos | Tratamiento |
|---|---|---|
| TRANSIENT | timeout temporal, rate limit recuperable, DNS temporal, error de transporte transitorio | Reintento acotado según contrato de fuente, Retry-After, deadline y política específica |
| PERMANENT | endpoint inexistente, esquema incompatible, autenticación inválida, recurso retirado | No reintentar automáticamente; registrar causa y bloquear/degradar según criticidad |
| POLICY | uso no permitido, licencia vencida, restricción contractual | Detener operación afectada; no eludir restricciones |
| DATA_QUALITY | conflicto, discontinuidad, dato inválido, cobertura insuficiente | Cuarentena/rechazo o salida limitada; nunca normalizar silenciosamente |
| IDENTITY | colisión de ticker, contrato/red no resueltos | Bloquear dependencias analíticas |
| INTERNAL | excepción, corrupción, fallo de persistencia, versión incompatible | Aislar, preservar logs seguros y marcar FAILED si integridad no puede garantizarse |
| CANCELLED | cancelación solicitada por usuario | Propagar cancelación y persistir resultado confirmado |

## 2. Reintentos

Reintentar solo errores clasificados como transitorios y cuando el contrato de la fuente lo permita. Respetar Retry-After, límites del proveedor, deadlines y presupuestos de ejecución. Usar backoff y jitter únicamente si son compatibles con el proveedor y no contradicen sus instrucciones.

No fijar un número universal de reintentos, intervalo o tamaño de respuesta. La política debe derivarse de la documentación, mediciones y condiciones de cada fuente. Los errores permanentes, de licencia, autenticación o esquema no se reintentan como si fueran transitorios.

Cada intento registra error normalizado, fecha, demora, respuesta del proveedor sin secretos, decisión de reintento y resultado.

## 3. Idempotencia y deduplicación

Cada solicitud del usuario recibe request_id único. Las operaciones internas derivan idempotency_key de request_id, stage_id, source_id, scope y versión de inputs. Repetir la misma llamada técnica con la misma clave no debe duplicar capturas ni escrituras.

Una nueva solicitud explícita del usuario recibe un nuevo request_id y crea un nuevo expediente, incluso si los parámetros coinciden con una evaluación anterior. La reutilización de datos previos solo es válida si se demuestra que son admisibles, vigentes para el timestamp y licencia, y quedan referenciados en el nuevo manifiesto.

La deduplicación de solicitudes activas equivalentes debe preservar el consentimiento y devolver el execution_id correspondiente; no convertir silenciosamente una nueva solicitud en ejecución automática.

## 4. Aislamiento entre ejecuciones

Cada ejecución utiliza su propio contexto, snapshot de solicitud, manifiesto, parámetros y outputs. No compartir archivos temporales mutables, variables de estado ni cachés sin clave/versionado. Toda referencia entre etapas debe apuntar a artefactos inmutables identificados.

## 5. Recuperación tras interrupción

Al reiniciar:
1. localizar ejecuciones no terminales;
2. verificar estado y lease del worker;
3. validar integridad de inputs/outputs persistidos;
4. identificar etapas completadas y sus hashes;
5. reanudar solo etapas idempotentes con inputs idénticos;
6. marcar PARTIAL/FAILED si la recuperación segura no es posible;
7. crear nueva ejecución si cambió el contexto o se necesita repetir una etapa no idempotente.

Nunca asumir que un timeout significa que la fuente no procesó la solicitud. Antes de repetir una operación externa, verificar su estado o usar una clave de idempotencia del proveedor cuando exista.

## 6. Cancelación

La cancelación se trata como solicitud, no como resultado instantáneo. Detener nuevas etapas, propagar señal a tareas activas, cerrar recursos y evitar escrituras parciales no atómicas. Persistir qué se completó y qué se canceló. Si una tarea externa no puede cancelarse, aislar su resultado y no incorporarlo al snapshot sin verificación.

## 7. Timeouts y límites

Definir deadlines por fuente/etapa con base en SLA/documentación, observación de latencia, requisitos de usuario y coste operativo. Conservar el valor aplicado y su justificación. No imponer límites arbitrarios de bytes, filas, histórico o tamaño de respuesta; cualquier límite debe responder a una restricción demostrable y permitir registrar cobertura truncada o continuar por paginación/segmentación cuando proceda.

## 8. Integridad de persistencia

Usar escrituras atómicas o patrón equivalente. Un fallo entre escritura de artefacto y registro debe poder reconciliarse mediante IDs y hashes. No marcar COMPLETED hasta verificar que el manifiesto y los artefactos requeridos son recuperables.
