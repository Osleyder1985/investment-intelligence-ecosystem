# Esquema estructurado de evaluación

**Estado:** Contrato inicial propuesto; pendiente de implementación y validación. **Versión:** 0.1.0

## 1. Objetivo

Este documento define el contrato lógico para JSON/API. La implementación debe publicar un JSON Schema versionado y validar cada documento antes de persistirlo o exportarlo. El fragmento siguiente es una base normativa de estructura, no un motor ni una implementación ejecutable.

## 2. Reglas de serialización

- Codificación UTF-8 y timestamps ISO 8601 en UTC.
- Identificadores estables; no usar ticker como clave primaria.
- Importes y cantidades incluyen unidad y moneda cuando corresponda.
- No serializar NaN, Infinity ni valores numéricos como cadenas.
- Valores ausentes usan estado explícito; no usar cero por defecto.
- Objetos referenciados usan IDs estables, no copias divergentes.
- Cada documento incluye schema_version y methodology_version.
- Las versiones publicadas son inmutables; cambios incompatibles incrementan versión mayor del esquema.

## 3. Tipos comunes

| Tipo | Campos mínimos |
|---|---|
| EvidenceRef | evidence_id, source_id, claim_ids, event/publication/availability/capture timestamps cuando se conozcan, admissibility_status, limitations |
| DataValue | value o ausencia explícita, unit, currency si aplica, evidence_status, observed_at, available_at, source/evidence refs |
| GateResult | gate_id, status, scope, rationale, evidence_refs, evaluated_at, policy_version |
| RiskFinding | risk_id, domain, inherent/residual status, scope, controls, evidence_refs, unknowns, veto_status |
| ValuationResult | model_id/version, valuation_object, applicability, range/units, scenarios, sensitivity, assumptions, uncertainty, abstention |
| DecisionResult | horizon, state, gates, vetoes, rationale, counterevidence, conditions_to_reassess |
| ComparisonResult | universe_snapshot_id, horizon, comparability_state, dimensions, dominance_relations, exclusions and rationale |

## 4. Objeto raíz requerido

Cada documento raíz debe incluir:
- schema_version;
- evaluation_id;
- created_at_utc y evaluation_timestamp_utc;
- canonical_asset_id y representation;
- scope (purpose, market, pair, jurisdiction, operation_size si aplica);
- horizon;
- universe_snapshot_id;
- methodology/policy/model versions;
- data_manifest_id;
- identity, evidence_quality, gates, risks, valuations, undervaluation, decision;
- assumptions, unknowns, counterevidence;
- report references y reproducibility metadata.

## 5. Esquema JSON ilustrativo

El siguiente ejemplo define la forma de un objeto raíz. Los enums son controlados y deben mantenerse alineados con las políticas versionadas. Las propiedades concretas de cada subobjeto se validan mediante definiciones reutilizables publicadas junto al esquema completo.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://example.invalid/schemas/evaluation-0.1.0.json",
  "title": "Cryptoasset Evaluation Record",
  "type": "object",
  "additionalProperties": false,
  "required": [
    "schema_version",
    "evaluation_id",
    "created_at_utc",
    "evaluation_timestamp_utc",
    "canonical_asset_id",
    "representation",
    "scope",
    "horizon",
    "universe_snapshot_id",
    "methodology_version",
    "data_manifest_id",
    "gates",
    "risks",
    "valuations",
    "decision",
    "unknowns",
    "reproducibility"
  ],
  "properties": {
    "schema_version": { "const": "0.1.0" },
    "evaluation_id": { "type": "string", "minLength": 1 },
    "created_at_utc": { "type": "string", "format": "date-time" },
    "evaluation_timestamp_utc": { "type": "string", "format": "date-time" },
    "canonical_asset_id": { "type": "string", "minLength": 1 },
    "representation": {
      "type": "object",
      "required": ["representation_id", "kind", "network"],
      "properties": {
        "representation_id": { "type": "string" },
        "kind": { "enum": ["NATIVE", "TOKEN", "WRAPPED", "BRIDGED", "RECEIPT", "OTHER", "UNKNOWN"] },
        "network": { "type": ["string", "null"] },
        "chain_id": { "type": ["string", "integer", "null"] },
        "contract_address": { "type": ["string", "null"] }
      }
    },
    "scope": {
      "type": "object",
      "required": ["purpose", "market_id"],
      "properties": {
        "purpose": { "enum": ["RESEARCH", "VALUATION", "SCREENING", "COMPARISON", "OPERATIONAL"] },
        "market_id": { "type": ["string", "null"] },
        "pair": { "type": ["string", "null"] },
        "jurisdiction": { "type": ["string", "null"] },
        "operation_size": { "type": ["object", "null"] }
      }
    },
    "horizon": { "enum": ["SHORT", "MEDIUM", "LONG"] },
    "universe_snapshot_id": { "type": "string" },
    "methodology_version": { "type": "string" },
    "policy_versions": { "type": "object", "additionalProperties": { "type": "string" } },
    "model_versions": { "type": "object", "additionalProperties": { "type": "string" } },
    "data_manifest_id": { "type": "string" },
    "gates": { "type": "array", "items": { "type": "object" } },
    "risks": { "type": "array", "items": { "type": "object" } },
    "valuations": { "type": "array", "items": { "type": "object" } },
    "undervaluation": { "type": ["object", "null"] },
    "decision": { "type": "object" },
    "assumptions": { "type": "array", "items": { "type": "object" } },
    "unknowns": { "type": "array", "items": { "type": "object" } },
    "counterevidence": { "type": "array", "items": { "type": "object" } },
    "evidence_refs": { "type": "array", "items": { "type": "string" } },
    "comparison": { "type": ["object", "null"] },
    "reproducibility": {
      "type": "object",
      "required": ["code_version", "data_manifest_hash", "created_by"],
      "properties": {
        "code_version": { "type": "string" },
        "code_commit": { "type": ["string", "null"] },
        "data_manifest_hash": { "type": "string" },
        "parameter_hash": { "type": ["string", "null"] },
        "created_by": { "type": "string" }
      }
    },
    "supersedes_evaluation_id": { "type": ["string", "null"] }
  }
}
```

El valor example.invalid es un identificador reservado ilustrativo, no una URL publicada. Antes de implementar, sustituirlo por el identificador estable del esquema del proyecto.

## 6. Estados de ausencia

Para valores que no se puedan proporcionar, usar un objeto de estado o una unión discriminada que diferencie al menos UNKNOWN, NOT_APPLICABLE, NOT_COLLECTED, REJECTED, QUARANTINED y REDACTED. La implementación no debe aceptar que un campo numérico opcional ausente sea interpretado como cero.

## 7. Validación del contrato

Validar estructura, tipos, enums, timestamps, unidades, referencias existentes, coherencia entre gates y decisión, coherencia de horizonte y hashes/manifiestos. Las validaciones semánticas deben ser adicionales al JSON Schema; un JSON válido no prueba que los datos o conclusiones sean correctos.

## 8. Compatibilidad

Cambios aditivos compatibles pueden incrementar versión menor; cambios que alteren semántica, tipos, estados o campos obligatorios requieren versión mayor. No reinterpretar silenciosamente expedientes antiguos bajo el esquema nuevo.
