# Paquete para revisión técnica independiente — Issue #19

**Estado:** preparado para revisión externa; no constituye una revisión independiente ni una aprobación.  
**Fecha de preparación:** 2026-10-31  
**Base documental:** `main` en `c765b687f68fbde5e9b87c61f013057b5ade7796`  
**Issue:** [#19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19)

## 1. Objetivo

Proporcionar a una persona revisora con experiencia técnica en redes distribuidas, tokenización y sistemas de datos un procedimiento acotado para evaluar los contratos de identidad de activos y el contrato de fixtures.

Este paquete reduce ambigüedades sobre qué debe revisarse y qué evidencia permitiría aceptar una especificación. No sustituye el juicio de la persona revisora, no demuestra que los perfiles sean correctos y no eleva ningún requisito a `SPECIFIED`.

## 2. Alcance de la revisión

La revisión debe cubrir, como mínimo:

1. Modelo conceptual de entidades, claves tipadas, cardinalidades y relaciones temporales.
2. Resolución de instancia de red y límites de identificadores externos.
3. Perfiles EVM, Solana, Cosmos SDK/IBC, UTXO Bitcoin-like y Cardano/eUTXO.
4. Semántica de estados de resolución, conflictos, evidencia insuficiente y abstención.
5. Contrato de fixtures: estructura, provenance, temporalidad, permisos, determinismo y resultados.
6. Coherencia transversal entre perfiles, casos AS, matriz de trazabilidad y registro de brechas.

No se solicita aprobar redes o proveedores no documentados, ni extrapolar las reglas a protocolos sin perfil específico.

## 3. Documentos que debe inspeccionar la persona revisora

| Artefacto | Pregunta central |
|---|---|
| `docs/asset-selection/asset-identity-resolution.md` | ¿El modelo de entidades, claves, cardinalidades y relaciones es internamente coherente y no mezcla activo económico, representación e instrumento? |
| `docs/asset-selection/network-identity-profiles-evm-solana.md` | ¿Las claves y pruebas de red EVM/Solana son suficientes, correctas y explícitas en sus límites? |
| `docs/asset-selection/network-identity-profiles-cosmos-utxo.md` | ¿Se distinguen correctamente denoms, vouchers IBC, CW20, activos UTXO y protocolos overlay? |
| `docs/asset-selection/network-identity-profile-cardano-eutxo.md` | ¿Se conserva la identidad binaria policy ID + asset name y se separan ADA, metadata y relaciones CIP? |
| `docs/asset-selection/identity-profile-fixture-contract.md` | ¿El contrato permite fixtures reproducibles, verificables, minimizados y con permisos trazables? |
| `docs/asset-selection/universe-test-cases.md` | ¿Los casos AS detectan errores reales de identidad y abstención sin presuponer resultados no demostrados? |
| `docs/traceability/requirements-traceability-matrix.md` | ¿Cada decisión y brecha está vinculada al requisito correcto? |
| `docs/traceability/dependency-and-gap-register.md` | ¿Las brechas y dependencias reflejan fielmente el estado actual? |
| `docs/traceability/issue-19-cross-document-review.md` | ¿Los hallazgos previos y sus límites siguen vigentes tras PR #24–#29? |

La persona revisora debe registrar la versión o commit exacto inspeccionado. Si un artefacto cambia durante la revisión, debe indicar qué versión evaluó.

## 4. Preguntas técnicas obligatorias

### 4.1 Modelo de identidad

- ¿Cada identificador está asociado a un tipo de entidad explícito?
- ¿Se evita usar ticker, nombre, símbolo, dirección aislada o `chain_id` como clave global?
- ¿La clave de representación está namespaced por instancia de red y por el identificador protocolario exacto?
- ¿Los activos nativos se representan sin direcciones ficticias y sin perder identificadores locales cuando el protocolo sí los define?
- ¿La versión de parser/adaptador permanece fuera de la identidad estable, salvo namespace incompatible definido por el propio protocolo?
- ¿Las cardinalidades y restricciones temporales permiten forks, migraciones, wrappers, bridges, proxies y representaciones multi-subyacente sin fusiones arbitrarias?

### 4.2 Perfiles de red

- EVM: ¿se distingue `chain_id` de la instancia de red y se exige evidencia suficiente para resolver clones/forks?
- Solana: ¿se verifica la cuenta mint, su inicialización y el programa propietario, y se tratan explícitamente extensiones no soportadas?
- Cosmos: ¿se conserva el denom base exacto y se distingue un voucher IBC de su posible activo económico de origen?
- UTXO: ¿se separan red, activo nativo, outpoint, script y namespaces de protocolos overlay?
- Cardano: ¿se preservan los bytes de policy ID y asset name, y se separan CIP metadata, reference NFT y user token?
- ¿Cada perfil define condiciones verificables para RESOLVED, RESOLVED_WITH_LIMITATIONS, AMBIGUOUS, UNVERIFIED, CONFLICTED y RETIRED_OR_MIGRATED?

### 4.3 Evidencia y temporalidad

- ¿Se distingue cuándo un hecho era válido de cuándo fue observado, publicado, accesible mediante autorización, capturado, ingerido y procesado?
- ¿Puede reconstruirse una decisión sin introducir información que no estaba disponible bajo la modalidad temporal declarada?
- ¿Se conserva evidencia contradictoria en lugar de resolverla por popularidad, ranking de proveedor o recencia automática?
- ¿La ausencia de evidencia produce UNKNOWN, INCONCLUSIVE o BLOCKED cuando corresponda, y nunca un PASS implícito?

### 4.4 Fixtures

- ¿La clase SYNTHETIC está inequívocamente separada de CAPTURED y REDACTED_CAPTURE?
- ¿Los fixtures capturados exigen expediente de permisos por operación y finalidad antes de almacenar o retener payloads?
- ¿El input conserva identificadores raw, encoding, método de normalización y contexto suficiente para reproducir el resultado?
- ¿El resultado esperado especifica entidades candidatas, relaciones, assertions negativas y alcance de abstención?
- ¿La ejecución offline puede ser determinista y está separada de pruebas live?
- ¿Los estados PASS, FAIL, INCONCLUSIVE, BLOCKED y NOT_RUN se usan sin convertir ausencia de ejecución en validación?
- ¿El ejemplo sintético del contrato satisface todas las reglas normativas del propio contrato?

## 5. Evidencia mínima que debe acompañar la revisión

La revisión no debe limitarse a una aprobación general. Debe entregar:

1. Identidad o perfil profesional pertinente de la persona revisora, suficiente para justificar competencia técnica sin publicar datos personales innecesarios.
2. Commit exacto y lista de documentos/versiones examinados.
3. Decisión por artefacto: `ACCEPT`, `ACCEPT_WITH_FINDINGS` o `REJECT`.
4. Hallazgos individualizados con severidad, ubicación, explicación, contraejemplo o evidencia y corrección propuesta.
5. Preguntas no resueltas y evidencia adicional necesaria.
6. Declaración de alcance: redes/protocolos revisados y explícitamente no revisados.
7. Fecha de revisión y posibles conflictos de interés relevantes para interpretar la independencia.

No se exige que la persona revisora acepte los documentos. Un rechazo fundamentado o una abstención por falta de competencia/evidencia son resultados válidos.

## 6. Severidad de hallazgos

| Severidad | Criterio |
|---|---|
| CRITICAL | Puede provocar identificación errónea sistemática, fusión de activos distintos, pérdida de evidencia o uso de datos no autorizados. |
| HIGH | Puede invalidar un perfil, una reconstrucción temporal o una decisión dependiente de identidad. |
| MEDIUM | Produce ambigüedad, cobertura insuficiente o diagnósticos incompletos con impacto acotado. |
| LOW | Mejora claridad, mantenibilidad o precisión documental sin alterar la resolución esperada. |

La severidad debe justificarse por el efecto observable, no por preferencia estilística.

## 7. Formato del registro de revisión

Cada hallazgo debe registrarse con esta estructura:

- **ID:** identificador estable, por ejemplo `IR-001`.
- **Artefacto y ubicación:** ruta, sección y fragmento relevante.
- **Severidad:** CRITICAL / HIGH / MEDIUM / LOW.
- **Tipo:** contradicción / omisión / regla no verificable / caso límite / terminología / trazabilidad.
- **Descripción:** qué afirma el contrato y por qué resulta problemático.
- **Contraejemplo o evidencia:** escenario mínimo reproducible o referencia técnica.
- **Impacto:** entidades, relaciones, estados o decisiones afectados.
- **Recomendación:** cambio concreto o evidencia requerida.
- **Estado:** OPEN / RESOLVED / ACCEPTED_RISK / NOT_APPLICABLE.
- **Respuesta del responsable:** justificación y enlace al cambio, si existe.

## 8. Criterios de salida

La revisión se considera completada únicamente cuando:

- Todos los artefactos del alcance tienen una decisión explícita.
- No quedan hallazgos CRITICAL abiertos para los perfiles que se pretendan aprobar.
- Los hallazgos HIGH están resueltos o existe una decisión documentada que impide aprobar el requisito afectado.
- Las observaciones MEDIUM/LOW tienen resolución o aceptación razonada.
- Se registran límites de cobertura por familia de red y protocolo.
- La matriz de trazabilidad y el registro de brechas reflejan los resultados.
- Se identifica qué pruebas/fixtures siguen pendientes y no se declara TESTED sin resultados reproducibles.

Una revisión favorable no sustituye fixtures ejecutados, validación interoperable, evidencia point-in-time ni expedientes IIM-002 de permisos de fuentes.

## 9. Independencia y límites

La preparación de este paquete y las revisiones documentales previas del repositorio no son una revisión independiente. La persona revisora debe ser distinta de quien redactó los contratos sometidos a evaluación y debe emitir sus propios hallazgos.

Si no se consigue una persona revisora independiente, el estado debe permanecer explícitamente pendiente. No se debe simular independencia mediante una segunda pasada automatizada o una nueva revisión del mismo autor.

## 10. Estado de los requisitos tras este paquete

Este documento es una herramienta de gobernanza. No cambia el estado de ningún requisito:

- IIM-001: `REVISION_REQUIRED`.
- IIM-019: `REVISION_REQUIRED`.
- IIM-020: `REVISION_REQUIRED`.
- IIM-018: `REVISION_REQUIRED`.
- IIM-002: `BLOCKED` para aprobar fuentes concretas.

La revisión técnica independiente y la validación con fixtures/adaptadores continúan pendientes.
