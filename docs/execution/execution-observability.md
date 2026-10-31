# Observabilidad y auditoría del workflow

**Estado:** Propuesta metodológica; pendiente de implementación. **Versión:** 0.1.0

## 1. Objetivo

Permitir reconstruir qué solicitó el usuario, qué etapas se ejecutaron, qué fuentes participaron, qué falló y cómo se obtuvo el resultado, sin exponer secretos, credenciales ni contenido restringido.

## 2. Identificadores y correlación

Cada evento debe incluir cuando corresponda:
- request_id;
- execution_id;
- stage_id;
- source_id/adapter_id;
- evaluation_id;
- snapshot_id/data_manifest_id;
- trace_id/span_id;
- code_commit, schema_version y policy/model versions.

Los identificadores deben permitir navegar entre solicitud, etapas, capturas, evaluación, informe y snapshot.

## 3. Eventos mínimos

Registrar:
- solicitud creada, validada, rechazada o cancelada;
- ejecución encolada, iniciada y finalizada;
- etapa preparada, iniciada, completada, bloqueada, omitida o fallida;
- fuente consultada, respuesta clasificada, paginación y cobertura;
- retry y causa;
- dato admitido, en cuarentena o rechazado;
- gate/veto activado, revisado o resuelto;
- modelo aplicado o abstención;
- informe validado y snapshot persistido;
- comparación longitudinal y diferencias no resueltas.

## 4. Campos de evento

Como mínimo: event_id, event_type, event_timestamp_utc, request_id, execution_id, stage_id, status, source/adapter cuando aplique, correlation IDs, code/config/policy versions, input/output hashes, error_class, retry_reason, duration y rationale. Los logs no deben contener claves API, tokens de acceso, secretos, claves privadas ni datos personales no necesarios.

## 5. Métricas operativas

Medir, sin convertirlas automáticamente en umbrales de calidad analítica:
- duración por etapa/fuente;
- tasa de éxito/fallo por clase;
- cobertura de fuentes y periodos;
- reintentos y rate limits;
- tamaño de artefactos realmente observados;
- cola/concurrencia y consumo de recursos;
- ejecuciones parciales/canceladas;
- discrepancias de persistencia e integridad.

Los límites de alerta se fijan a partir de SLO, contratos, capacidad medida y análisis de riesgo; no se inventan en esta política.

## 6. Errores y mensajes

El mensaje al usuario debe explicar etapa, fuente si puede revelarse, clase de fallo, impacto en la evaluación, si se reintentó, estado final y acción necesaria. No revelar trazas internas sensibles ni atribuir culpa al proveedor sin evidencia.

## 7. Retención y acceso

Aplicar controles de acceso por función y retención según obligaciones legales, seguridad y licencias. Registrar acceso a expedientes sensibles. No conservar secretos en logs. Las políticas de retención deben documentarse por clase de artefacto; esta política no inventa periodos universales.

## 8. Auditoría

Una revisión debe poder reconstruir el estado a partir de eventos y snapshots. Las correcciones se registran como eventos nuevos, nunca editando silenciosamente la historia.
