# Contratos técnicos Portfolio & Execution — borrador v0.1.0

**Fecha:** 2026-10-31  
**Estado:** CONTRACT_DRAFT / NOT APPROVED  
**JSON Schema:** Draft 2020-12  
**Base funcional:** `docs/architecture/portfolio-execution-integrated-requirements-draft.md` v0.2.0-draft

## 1. Alcance

Este paquete introduce esquemas JSON preliminares para seis contratos prioritarios:

1. `InvestmentMandate`
2. `ExposureSnapshot`
3. `RiskPolicy`
4. `RiskBudget`
5. `PortfolioProposal`
6. `AuditEvent`

Los archivos se encuentran en `schemas/contracts/`. Son contratos candidatos para discusión técnica, no interfaces aprobadas ni componentes ejecutables. Se ha añadido una suite preliminar de conformidad en `tests/contracts/`, pero su ejecución y sus resultados aún no están acreditados. No existe evidencia de integración en runtime ni uso en producción.

## 2. Convenciones comunes

- Codificación UTF-8 y JSON compatible con RFC 8259.
- `schema_version` identifica la versión del contrato; cada artefacto de negocio mantiene su propio ID y versión.
- Identificadores son strings opacos, estables y no reutilizables. No codificar semántica mutable en el ID.
- Fechas temporales usan RFC 3339 con offset explícito. Los campos de fecha civil usan ISO 8601 date.
- Cantidades financieras, pesos y métricas decimales se representan como strings decimales para evitar errores binarios de punto flotante. El consumidor debe convertirlas a decimal de precisión arbitraria.
- Monedas usan códigos de moneda/activo normalizados en mayúsculas; la taxonomía exacta debe fijarse en una especificación de unidades.
- Los objetos rechazan propiedades no declaradas mediante `additionalProperties: false`, salvo payloads deliberadamente extensibles y gobernados.
- Referencias externas deben resolverse a una versión inmutable y conservar evidencia/lineage.
- `UNKNOWN`, `STALE`, `INVALID` y `CONFLICT` son estados distintos y no deben convertirse en PASS ni en cero.

## 3. Catálogo de schemas

| Contrato | Archivo |
|---|---|
| InvestmentMandate | [investment-mandate.schema.json](../../schemas/contracts/investment-mandate.schema.json) |
| ExposureSnapshot | [exposure-snapshot.schema.json](../../schemas/contracts/exposure-snapshot.schema.json) |
| RiskPolicy | [risk-policy.schema.json](../../schemas/contracts/risk-policy.schema.json) |
| RiskBudget | [risk-budget.schema.json](../../schemas/contracts/risk-budget.schema.json) |
| PortfolioProposal | [portfolio-proposal.schema.json](../../schemas/contracts/portfolio-proposal.schema.json) |
| AuditEvent | [audit-event.schema.json](../../schemas/contracts/audit-event.schema.json) |

## 4. Semántica y validaciones que JSON Schema no resuelve por sí solo

### 4.1 InvestmentMandate

- Un mandato ACTIVE debe tener aprobación válida, vigencia coherente y owner/approver autorizados.
- `effective_to` debe ser posterior a `effective_from`; horizonte debe ser coherente con vigencia.
- Un mandato revocado, suspendido, vencido o no aprobado no puede respaldar nuevas propuestas.
- El cambio de restricciones crea una nueva versión; no se muta una versión aprobada.
- El benchmark puede ser nulo/omitido si no aplica; su ausencia no debe inferirse como benchmark cero.

### 4.2 ExposureSnapshot

- `as_of` expresa el tiempo económico de medición; `generated_at` expresa el tiempo de generación.
- Los valores, denominadores y ratios deben compartir unidades y base de conversión declaradas.
- Validar que `coverage_ratio` corresponda a covered/total según la convención aprobada y que esté dentro del rango permitido.
- Evitar doble conteo de wrappers, activos representados y posiciones subyacentes.
- Un look-through parcial debe reflejar explícitamente la exposición desconocida; no redistribuirla silenciosamente.
- La suma de exposiciones no tiene por qué ser 100% cuando hay apalancamiento, derivados, pasivos o denominadores distintos; la regla depende de la métrica.

### 4.3 RiskPolicy y RiskBudget

- Una política ACTIVE debe estar aprobada por una autoridad habilitada para el scope concreto.
- La precedencia entre políticas globales, cartera, estrategia y cuenta debe definirse explícitamente; no asumirla por orden de lectura.
- La evaluación debe especificar métrica, operador, unidad, ventana temporal, agregación y fuente de datos.
- Para reglas HARD_BLOCK, datos UNKNOWN/STALE/INVALID/CONFLICT deben seguir la acción fail-closed definida; nunca transformarse en PASS.
- Los budgets deben estar vinculados a una política vigente y no contradecir límites duros de mayor precedencia.
- Excepciones requieren entidad autorizante, alcance, justificación, expiración y evento de auditoría. No son un booleano que permita omitir vetos legales o de seguridad.

### 4.4 PortfolioProposal

- INITIAL_ALLOCATION y REBALANCE comparten contrato, pero conservan triggers y validaciones de origen diferentes.
- Las asignaciones deben tener activos únicos, pesos con precisión y unidad declaradas y límites definidos por el mandato.
- El consumidor debe verificar sumas, cash residual, shorting, apalancamiento, rounding, restricciones y factibilidad según el mandato; el JSON Schema no puede garantizar esas invariantes.
- Una propuesta FEASIBLE no es autorización. Requiere TradeAuthorization independiente y controles previos a la operación.
- Una propuesta vencida, superseded, abstained o infeasible no puede convertirse en orden.
- Inputs deben apuntar a snapshots/versiones inmutables; no basta un ID sin versión.
- El resultado de checks debe preservar UNKNOWN/STALE/INVALID/CONFLICT.

### 4.5 AuditEvent

- `occurred_at` y `recorded_at` representan tiempos diferentes; no sobrescribir el tiempo del evento al recibirlo tarde.
- `correlation_id`, `causation_id` y `request_id` tienen semánticas distintas y deben propagarse consistentemente.
- Definir canonicalización antes de calcular hashes; el hash no es prueba de autenticidad por sí solo.
- La cadena de hashes requiere almacenamiento append-only, protección contra truncamiento y verificación periódica.
- Las firmas necesitan identidad de clave, algoritmo, rotación, revocación y verificación. No almacenar secretos privados en eventos.
- La política de retención, privacidad, acceso y redacción debe ser definida sin destruir la integridad probatoria.

## 5. Limitaciones conocidas del borrador

1. Los schemas no usan aún un catálogo común publicado de unidades, monedas, activos, actores, entornos y taxonomías.
2. Algunos campos son intencionalmente genéricos y requieren vocabularios controlados.
3. No se han definido reglas de compatibilidad backward/forward ni política de deprecación.
4. No se han definido canonicalización, firma ni almacenamiento de eventos.
5. JSON Schema no sustituye validaciones semánticas, autorización, reglas temporales ni invariantes entre registros.
6. La validación de `format: date-time` y `format: uri` depende de la configuración del validador; debe habilitarse explícitamente.
7. Los $id usan el dominio reservado `example.invalid` como identificador documental no resoluble. Debe sustituirse por el namespace oficial del proyecto antes de publicar schemas estables.
8. Se ha incorporado `tests/contracts/test_schema_contracts.py` para meta-validación Draft 2020-12 y pruebas sintéticas positivas/negativas. La suite todavía no se ha ejecutado en un entorno verificable; por tanto, no se afirma que los schemas hayan superado la validación.

## 6. Gates antes de declarar SPECIFIED

- [ ] Aprobar vocabularios, unidades y formatos.
- [ ] Resolver campos ambiguos y nulabilidad.
- [ ] Definir compatibilidad y versionado de schemas.
- [ ] Ejecutar meta-validación Draft 2020-12 y pruebas positivas/negativas.
- [ ] Añadir validadores semánticos para invariantes cross-field y cross-record.
- [ ] Revisión independiente de arquitectura, riesgo, seguridad y operaciones.
- [ ] Registrar decisiones y aprobaciones por contrato.
- [ ] Actualizar trazabilidad requisito-schema-componente-test.
- [ ] Mantener cualquier capacidad de producción deshabilitada hasta completar los gates operativos y de seguridad.

## 7. Suite de conformidad preliminar

La suite ubicada en `tests/contracts/` utiliza `jsonschema` y `Draft202012Validator.check_schema` para comprobar la estructura de los seis schemas, y genera instancias sintéticas mínimas para probar aceptación/rechazo. Incluye comprobaciones de propiedades requeridas, propiedades raíz desconocidas, versión de contrato, formatos date-time y patrón decimal.

Ejecución desde la raíz del repositorio:

```bash
python -m pip install -r requirements-dev.txt
python -m unittest discover -s tests/contracts -p 'test_*.py' -v
```

La suite no sustituye pruebas con fixtures de negocio revisados, validaciones semánticas cross-field/cross-record, pruebas de compatibilidad, revisión de seguridad ni aprobación independiente. Hasta que se ejecute y revise su resultado, el estado permanece NOT TESTED.

## 8. Estado

**DRAFT — NOT APPROVED — NOT IMPLEMENTED — NOT TESTED.** La incorporación de pruebas no acredita que estas hayan sido ejecutadas ni que los contratos sean correctos, completos, compatibles o seguros para producción.
