# Contrato de fixtures de conformidad para identidad de activos

**Estado:** borrador metodológico; pendiente de revisión independiente.  
**Versión:** 0.1.0  
**Requisitos relacionados:** IIM-001, IIM-018, IIM-019, IIM-002

## 1. Objetivo y límites

Este contrato define cómo representar entradas reproducibles para comprobar los perfiles de identidad de red y activo. Permite convertir casos AS documentales en pruebas deterministas sin depender de consultas RPC durante cada ejecución.

Un fixture es una entrada controlada y versionada con resultado esperado. No es, por sí mismo, evidencia de que un activo exista actualmente, de que una fuente sea autorizada, ni de que el resultado tenga validez económica.

No se afirma que los fixtures estén implementados ni ejecutados. La aprobación de este contrato tampoco aprueba proveedores, endpoints, permisos o retención de datos.

## 2. Clases de fixture

| Clase | Definición | Requisito |
|---|---|---|
| SYNTHETIC | Datos creados para probar reglas, incluidos casos límite/malformados. | Marcar explícitamente como sintético; no presentarlo como dato real. |
| CAPTURED | Snapshot obtenido de una fuente/red real. | Registrar procedencia, red, bloque/slot, captura, permisos y condiciones de retención. |
| REDACTED_CAPTURE | Snapshot real minimizado o redactado. | Documentar campos eliminados, efecto de la redacción y límites de reproducibilidad. |

Los fixtures CAPTURED/REDACTED_CAPTURE solo pueden incluir datos cuyo uso y almacenamiento estén respaldados por el expediente IIM-002 correspondiente. Un endpoint público o una API gratuita no demuestra autorización de almacenamiento, retención o redistribución.

## 3. Sobre mínimo obligatorio

Cada fixture debe incluir:

| Campo | Obligatorio | Descripción |
|---|---|---|
| fixture_id | Sí | Identificador interno estable del fixture; no es ID de activo productivo. |
| fixture_version | Sí | Versión del contenido del fixture. |
| fixture_kind | Sí | SYNTHETIC, CAPTURED o REDACTED_CAPTURE. |
| purpose | Sí | Regla, riesgo o comportamiento que se comprueba. |
| requirement_ids | Sí | Requisitos IIM cubiertos. |
| case_ids | Sí | Casos AS cubiertos. |
| profile_id / profile_version | Sí | Perfil de red aplicado y versión. |
| adapter_version | Si aplica | Versión del parser/adaptador bajo prueba; nunca parte de la identidad esperada. |
| input | Sí | Observaciones mínimas necesarias para la resolución. |
| expected_result | Sí | Entidades, relaciones, estado y assertions esperadas. |
| provenance | Sí | Origen y condiciones de uso; para SYNTHETIC declarar que no aplica fuente real. |
| created_at / updated_at | Sí | Fechas de creación y modificación del fixture. |

No incluir secretos, credenciales, claves privadas, datos personales ni payloads completos cuando un subconjunto mínimo sea suficiente.

## 4. Estructura de input

El input debe separar:

### 4.1 Network context

- family y network_id_fixture;
- entorno declarado;
- identificadores externos con namespace (chain_id, genesis hash, cluster label, network magic u otros);
- checkpoints disponibles (altura/slot, block hash, app hash, finality/commitment);
- endpoint/source reference y versión, si el fixture es capturado;
- candidatos de red alternativos cuando se pruebe AMBIGUOUS.

Un fixture puede incluir más de un candidato. No forzar network_id resuelto si la evidencia no lo permite.

### 4.2 Observed identity

- entity_type esperado (network, representation, instrument, listing u otro);
- identifier_namespace;
- raw_identifier y encoding;
- normalized_identifier y método/version de normalización;
- atributos de contexto que no son claves (ticker, name, symbol, decimals, code hash, owner, metadata, program ID, denom trace, policy ID, asset name, etc.);
- block/slot/height y canonicality;
- perfil/estándar relevante.

Conservar el raw_identifier siempre que esté permitido. Si la normalización no es reversible, guardar la transformación y la razón.

### 4.3 Temporal context

No colapsar los tiempos siguientes:

- valid_time: cuándo era cierto el hecho en la red;
- observation_time: cuándo se observó en la fuente;
- public_availability_time: cuándo se puede demostrar que era públicamente accesible;
- authorized_access_time: cuándo estuvo disponible mediante acceso autorizado;
- capture_time: cuándo el sistema capturó el dato;
- ingestion_time: cuándo el sistema lo incorporó;
- processing_time: cuándo terminó la transformación.

Los campos no demostrables se declaran UNKNOWN/NOT_AVAILABLE; no se inventan timestamps. Los fixtures históricos deben indicar si prueban REPLAY_OPERATIONAL o RECONSTRUCTION_POINT_IN_TIME.

## 5. Estructura de expected_result

El resultado esperado debe contener:

- resolution_status del vocabulario canónico: RESOLVED, RESOLVED_WITH_LIMITATIONS, AMBIGUOUS, UNVERIFIED, CONFLICTED o RETIRED_OR_MIGRATED;
- entity_candidates con IDs de fixture, tipo, clave compuesta esperada y razón;
- selected_entity_fixture_id, solo si la selección es defendible;
- relationships con tipo, endpoints tipados, dirección, vigencia, qualifiers y estado;
- assertions positivas y negativas;
- abstention_scope: usos que deben bloquearse y usos que pueden continuar;
- expected_diagnostics: errores, warnings, missing evidence o conflicts.

Los IDs de fixture son identificadores locales de prueba; no deben reutilizarse como IDs productivos ni confundirse con claves externas de red.

## 6. Provenance y permiso

Para CAPTURED/REDACTED_CAPTURE, provenance debe incluir:

- proveedor/producto, endpoint o documento y versión;
- fecha de consulta/captura y referencia al snapshot;
- red, bloque/slot/altura y hash de referencia;
- source_terms_version o identificador del expediente de términos;
- operaciones/finalidades autorizadas: consulta, captura, almacenamiento, retención, transformación, modelado, visualización, exportación y redistribución, cada una por separado;
- retención/expiración y restricciones de redistribución;
- hash del artefacto almacenado cuando sea permitido;
- responsable y método de redacción, si aplica.

Si el permiso no está demostrado, no incluir el payload capturado. Usar SYNTHETIC o una referencia externa autorizada sin copiar datos restringidos.

## 7. Determinismo y ejecución

1. La prueba normal debe ejecutarse offline contra el fixture versionado; no depender de que un RPC/explorer siga disponible.
2. Una actualización del fixture genera nueva versión y conserva la anterior cuando la licencia lo permita.
3. El resultado debe ser determinista para la misma versión de fixture, perfil, configuración y adapter version.
4. La versión del parser puede cambiar el diagnóstico o detectar un defecto, pero no debe cambiar la clave de identidad si la regla protocolaria permanece igual.
5. Si el fixture carece de evidencia obligatoria, el resultado correcto puede ser INCONCLUSIVE/BLOCKED, no PASS.
6. Las pruebas de red en vivo son una capa separada; registran endpoint, latencia/estado, bloque/slot y limitaciones. No sustituyen las pruebas offline.
7. Ningún resultado de fixture demuestra rendimiento predictivo, valoración, liquidez o validez jurídica del activo.

## 8. Clases de prueba obligatorias

Cada perfil debe cubrir, cuando aplique:

- identidad positiva y canonicalización;
- identificadores iguales en namespaces/redes distintas;
- colisiones de ticker/nombre/metadata;
- claves malformadas y datos ausentes;
- red ambigua, fork, reset o endpoint inconsistente;
- activo nativo y representaciones wrapped/bridged;
- proxies, upgrades, autoridades y metadata mutable;
- relaciones multi-subyacente, migraciones y vouchers;
- conflicto entre fuentes y temporalidad;
- parser/profile version change sin cambio de identidad;
- estados de abstención y alcance de bloqueo;
- datos capturados sin permisos suficientes (deben bloquear almacenamiento/uso, no saltarse el control).

## 9. Ejemplo sintético mínimo

El siguiente ejemplo es ilustrativo y no corresponde a una red real. Las direcciones/hash son valores sintéticos.

~~~json
{
  "fixture_id": "FX-EVM-001",
  "fixture_version": "1.0.0",
  "fixture_kind": "SYNTHETIC",
  "purpose": "La capitalización hexadecimal no crea una identidad EVM distinta",
  "requirement_ids": ["IIM-001", "IIM-019"],
  "case_ids": ["AS-034", "AS-058"],
  "profile_id": "EVM",
  "profile_version": "0.1.0",
  "adapter_version": "test-adapter-0.1",
  "input": {
    "network_context": {
      "family": "EVM",
      "network_id_fixture": "NET-EVM-SYNTH-01",
      "chain_id": "999001",
      "genesis_hash": "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
      "checkpoint": {
        "height": 100,
        "block_hash": "0xbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
      }
    },
    "observations": [
      {
        "entity_type": "asset_representation",
        "raw_identifier": "0x1111111111111111111111111111111111111111",
        "encoding": "hex-20-byte",
        "observation_ref": "synthetic-observation-a"
      },
      {
        "entity_type": "asset_representation",
        "raw_identifier": "0x1111111111111111111111111111111111111111",
        "encoding": "hex-20-byte-checksum-display",
        "observation_ref": "synthetic-observation-b"
      }
    ]
  },
  "expected_result": {
    "resolution_status": "RESOLVED",
    "entity_candidates": [
      {
        "entity_fixture_id": "REP-EVM-SYNTH-01",
        "entity_type": "asset_representation",
        "key": ["NET-EVM-SYNTH-01", "0x1111111111111111111111111111111111111111"]
      }
    ],
    "assertions": [
      "both_observations_resolve_to_same_entity",
      "adapter_version_is_not_part_of_identity_key"
    ],
    "abstention_scope": []
  },
  "provenance": {
    "synthetic": true,
    "source": null,
    "permission_record": null
  },
  "created_at": "2026-10-31T00:00:00Z",
  "updated_at": "2026-10-31T00:00:00Z"
}
~~~

Los valores del ejemplo son marcadores sintéticos; no prueban existencia, despliegue ni propiedad de un contrato.

## 10. Resultado de ejecución y auditoría

Cada ejecución debe registrar por separado:

- fixture_id y fixture_version;
- profile_id/profile_version y adapter_version;
- commit/build del código bajo prueba;
- fecha/hora y entorno de ejecución;
- estado: PASS, FAIL, INCONCLUSIVE, BLOCKED o NOT_RUN;
- assertions evaluadas, resultados observados y diff frente a expected_result;
- logs mínimos, hash de artefactos y causa de cualquier excepción.

No etiquetar NOT_RUN, INCONCLUSIVE o BLOCKED como TESTED/PASS. La aprobación metodológica del perfil requiere revisión de criterios, fixtures representativos y evidencia; no se deriva de que una suite pase.

## 11. Fuera de alcance

Este documento no prescribe JSON Schema definitivo, lenguaje de tests, framework, base de datos, CI ni arquitectura del adaptador. Esos contratos pertenecen a la implementación y deben conservar compatibilidad con esta especificación tras su aprobación.

IIM-001 e IIM-019 permanecen en REVISION_REQUIRED. IIM-002 permanece BLOCKED para cualquier fuente concreta cuyo expediente de permisos no exista.
