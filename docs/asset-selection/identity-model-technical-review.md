# Auditoría técnica del modelo de identidad de criptoactivos

**ID:** IIE-IDENTITY-REVIEW-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_FINDINGS_OPEN / NOT_APPROVED  
**Requisitos:** IIM-001, IIM-019 (impacto indirecto IIM-018, IIM-002)  
**Paquete:** EPIC-01 / WP-02  
**Issue:** #61  
**Artefactos revisados:** asset-identity-resolution.md; identity-profile-fixture-contract.md; perfiles EVM/Solana, Cosmos/IBC, UTXO Bitcoin-like y Cardano/eUTXO; universe-test-cases.md; tests/identity_fixtures/*.

## 1. Alcance y método

Revisión estática de contratos, perfiles, catálogo de casos y harness/fixtures disponibles en main. No se ejecutaron tests en esta revisión, no se consultaron redes RPC, no se inspeccionaron datos reales de proveedores y no se realizó validación legal o de seguridad.

La auditoría evalúa coherencia conceptual, límites de identidad, capacidad de convertir reglas en contratos verificables y cobertura real del harness. Los hallazgos son recomendaciones técnicas pendientes de revisión; no modifican requisitos normativos.

## 2. Dictamen ejecutivo

El modelo documental tiene una base conceptual sólida: separa economic asset, network, representation, instrument, listing y project; utiliza claves namespaced; conserva relaciones tipadas y temporales; evita identificar por ticker; distingue wrappers/bridges, forks, migraciones, proxies y metadatos; y define estados de abstención.

Sin embargo, **no está listo para ser tratado como un contrato de identidad aprobado ni como un servicio de resolución universal**. La mayor parte de la semántica todavía no está codificada en esquemas ni validada por adaptadores. El harness es deliberadamente un primer slice sintético EVM/Solana y no implementa el grafo de entidades/relaciones descrito en la metodología.

Conclusión WP-02: **REVIEW_REQUIRED**. No se recomienda cerrar IIM-001/IIM-019 ni habilitar selección automática sobre perfiles no implementados.

## 3. Hallazgos

### IDR-01 — Falta un contrato ejecutable para el grafo tipado de entidades y relaciones
**Severidad:** HIGH  
**Evidencia:** asset-identity-resolution §§10–11 define economic_asset, network, representation, instrument, listing, project, relaciones, temporalidad y estados. El schema actual de fixtures valida principalmente observaciones de representación EVM/Solana y permite propiedades adicionales en objetos anidados. No hay contrato ejecutable para Relationship, endpoint typing, qualifiers, validity/knowledge intervals o claim evidence.

**Riesgo:** implementaciones distintas pueden interpretar de forma diferente WRAPS, BRIDGES, REPRESENTS, BACKED_BY, MIGRATES_TO, FORKED_FROM, ISSUED_BY, CONTROLLED_BY, IMPLEMENTS y LISTED_AS. La documentación puede parecer completa sin que se puedan detectar relaciones inválidas.

**Recomendación:** diseñar contratos versionados separados para Network, EconomicAsset, AssetRepresentation, MarketInstrument, Listing, Project/Actor, Relationship y EvidenceClaim. Incorporar validaciones de tipos de endpoint, vocabulario, dirección, temporalidad, cardinalidad contextual y procedencia. Mantener el schema como propuesta hasta revisión.

**Criterio de cierre:** fixtures positivos/negativos para cada tipo de relación y validador que rechace endpoints mal tipados, intervalos inválidos y relaciones sin evidencia mínima.

### IDR-02 — La identidad global y la aptitud para un uso no están suficientemente separadas en el contrato de salida
**Severidad:** HIGH  
**Evidencia:** el modelo declara estados globales RESOLVED, RESOLVED_WITH_LIMITATIONS, AMBIGUOUS, UNVERIFIED, CONFLICTED y RETIRED_OR_MIGRATED, y también afirma que la abstención debe ser específica al uso afectado. El harness, sin embargo, devuelve un único resolution_status esperado y no calcula aptitud por propósito.

**Riesgo:** una incertidumbre en control, redención o respaldo puede bloquear indebidamente una descripción histórica, o una identidad técnica resuelta puede interpretarse como aprobación de derechos, solvencia, transferibilidad o elegibilidad de inversión.

**Recomendación:** separar IdentityResolution del resultado de evaluación por claim/purpose. Representar por separado resolución de entidad, estado de cada relación/afirmación y admissibility/eligibility para un uso declarado. La identidad resuelta nunca debe equivaler a seguridad, legitimidad, liquidez o autorización operativa.

**Criterio de cierre:** escenarios donde la misma representación es RESOLVED como entidad, pero BACKING_UNKNOWN, CONTROL_DISPUTED o REDEMPTION_UNVERIFIED; demostrar que solo se bloquean los usos dependientes.

### IDR-03 — Identidad de instrumento de mercado incompleta para derivados y productos estructurados
**Severidad:** MEDIUM / SCOPE_DECISION_REQUIRED  
**Evidencia:** el documento define MarketInstrument a partir de representación base, venue, mercado y quote unit, pero no formaliza especificaciones de contrato, vencimiento, multiplicador, tipo de liquidación, margen, índice/oráculo, funding ni reglas de mark/index price.

**Riesgo:** un futuro perpetuo, futuro con vencimiento, opción o instrumento sintético puede colisionar con un spot market instrument o con otro contrato que comparte ticker/base/quote.

**Recomendación:** decidir explícitamente si el MVP cubre solo instrumentos spot. Si sí, declarar derivados fuera de alcance y rechazar su clasificación como spot. Si no, crear un modelo separado de DerivativeInstrument con identificadores de contrato y atributos contractuales obligatorios. No ampliar el scope implícitamente.

**Criterio de cierre:** decisión de alcance aprobada y casos de colisión spot/perpetual/future/option.

### IDR-04 — Evidencia de identidad de red no tiene política mínima de corroboración ejecutable
**Severidad:** HIGH  
**Evidencia:** los perfiles requieren genesis, chain_id, checkpoints, endpoints, estado de finalización y relaciones de fork, pero el contrato no define un procedimiento común para evaluar discrepancias entre endpoints, independencia de fuentes, quorum, canonicality o checkpoint conflict.

**Riesgo:** una red clonada, endpoint equivocado, fork o proveedor indexado puede producir una clave de red formalmente válida pero asociada a una historia incorrecta.

**Recomendación:** especificar un NetworkIdentityEvidenceRecord con anclas por familia, origen, independencia, método, timestamp, checkpoint, canonicality/finality y estado de conflicto. Definir cuándo una única fuente primaria basta y cuándo se exige corroboración; no fijar quorum arbitrario sin justificación.

**Criterio de cierre:** fixtures de endpoints divergentes, genesis/checkpoint reutilizado, fork y endpoint mal etiquetado; el resolver debe abstenerse en casos no discriminables.

### IDR-05 — Harness inicial valida canonicalización, no resolución completa de identidad
**Severidad:** HIGH  
**Evidencia:** tests/identity_fixtures/harness.py implementa validación sintética de formato/canonicalización EVM, code-presence/code-hash declarados y Base58/mint-account Solana. No consulta RPC, verifica estado real, estándares completos, consenso/finality, proxies, bridges ni relaciones económicas. Solo hay tres fixtures iniciales y nueve tests unitarios.

**Riesgo:** el término PASS puede interpretarse como identidad de activo validada, aunque solo se ha verificado una regla sintética limitada.

**Recomendación:** mantener explícito el alcance de PASS como conformidad del fixture/adaptador para las assertions declaradas. Añadir result metadata con profile/adapter/test scope, y no permitir que PASS sea consumido como decisión de inversión. Ampliar pruebas adversariales antes de cualquier uso productivo.

**Criterio de cierre:** nomenclatura/contrato de resultados aprobado, tests negativos de alcance y trazabilidad test → assertion → requisito.

### IDR-06 — Cobertura de perfiles está documentada, pero solo EVM/Solana están en el harness
**Severidad:** HIGH  
**Evidencia:** existen perfiles documentales Cosmos SDK/IBC, UTXO Bitcoin-like y Cardano/eUTXO; el JSON Schema restringe profile_id a EVM/SOLANA y el harness solo despacha esos dos perfiles. Los casos AS-043..AS-057 son especificaciones, no tests ejecutables.

**Riesgo:** un consumidor podría inferir soporte multi-chain a partir de la existencia de perfiles documentales.

**Recomendación:** publicar matriz de soporte por familia, subprotocolo, regla implementada, evidencia requerida, fixture y test. Mantener perfiles no implementados como UNSUPPORTED/OUT_OF_SCOPE, sin asignar PASS.

**Criterio de cierre:** cada perfil habilitado para MVP tiene adaptador, fixtures, tests ejecutados y límites declarados; el resto permanece excluido.

### IDR-07 — Relaciones de bridge/wrapper requieren modelar riesgo y equivalencia en dimensiones separadas
**Severidad:** HIGH  
**Evidencia:** la documentación distingue representación, activo económico, backing, redención, custodio/bridge y riesgo de depeg; no existe aún contrato ejecutable que separe relación técnica, equivalencia de exposición, derecho de redención, backing observado y solvencia del operador.

**Riesgo:** una relación BRIDGES/WRAPS puede interpretarse como equivalencia económica o respaldo garantizado, provocando doble conteo o falsa seguridad.

**Recomendación:** modelar por separado (a) relación de representación/origen, (b) mecanismo de emisión/burn/lock, (c) reservas/backing observados, (d) derecho y condiciones de redención, (e) control/custodia y (f) estado de riesgo. No inferir una dimensión a partir de otra.

**Criterio de cierre:** casos de bridge custodial, canonical bridge, wrapped asset, multi-collateral, backing parcial, depeg y redemption paused.

### IDR-08 — Las cardinalidades conceptuales necesitan restricciones dependientes del tipo de relación
**Severidad:** MEDIUM  
**Evidencia:** la sección 11 define cardinalidades generales, pero varias relaciones admiten composición o múltiples endpoints y no hay un catálogo completo de cardinalidades por tipo de arista.

**Riesgo:** un validador puede aceptar un MIGRATES_TO con varios sucesores incompatibles o rechazar una composición legítima; las restricciones genéricas no bastan.

**Recomendación:** crear tabla por relationship_type con source/target type, aridad, dirección, exclusividad temporal, qualifiers obligatorios, ciclos esperados/prohibidos y evidencia mínima. No aplicar una prohibición global de ciclos.

**Criterio de cierre:** pruebas por tipo de relación, incluyendo ciclos legítimos y ciclos de migración sospechosos.

### IDR-09 — La temporalidad de claims y relaciones no tiene contrato interoperable completo
**Severidad:** HIGH  
**Evidencia:** el modelo exige valid_from/to y known_from/to, pero no fija en el contrato de relación la semántica de límites, precisión, zona horaria, open-ended intervals, backdating, correcciones ni orden de precedencia entre fuentes.

**Riesgo:** se pueden reconstruir relaciones históricas usando información conocida posteriormente o generar intervalos contradictorios.

**Recomendación:** alinear explícitamente el contrato de identidad con el framework lifecycle/point-in-time: valid time, public/authorized availability, observed/recorded/ingested time, effective time; semántica de intervalos half-open si se aprueba; correcciones y eventos tardíos. No introducir timestamps ficticios.

**Criterio de cierre:** fixtures de relación retroactiva, upgrade tardío, fork, migración y dato corregido, con replay histórico sin look-ahead.

### IDR-10 — Perfil Solana/harness clasifica programas por etiquetas, no por identidad verificable del programa
**Severidad:** MEDIUM  
**Evidencia:** el harness exige token_program en {TOKEN_PROGRAM, TOKEN_2022} como etiqueta declarada, mientras el perfil exige registrar/validar el program ID propietario y reconoce otros programas compatibles como posible caso.

**Riesgo:** un fixture puede etiquetar un programa arbitrario como TOKEN_PROGRAM sin que el harness compruebe que su program ID corresponde a un programa conocido y versionado.

**Recomendación:** mapear program ID exacto a catálogo versionado de programas, conservar la evidencia de cuenta/owner y mantener UNKNOWN/UNSUPPORTED si no se reconoce. El catálogo debe tener fuente, versión y vigencia.

**Criterio de cierre:** casos con program ID correcto, incorrecto, desconocido y actualización del catálogo; ninguna etiqueta textual por sí sola produce PASS.

### IDR-11 — Los contratos de fixtures permiten demasiada estructura libre en objetos anidados
**Severidad:** MEDIUM  
**Evidencia:** el schema raíz usa additionalProperties: false, pero network_context, observations, expected_result, relationship-like payloads y provenance permiten additionalProperties: true. La estructura de relaciones tampoco está formalizada en el schema actual.

**Riesgo:** campos mal escritos o semánticamente desconocidos pueden pasar validación de schema y ser ignorados por adaptadores, causando pérdida silenciosa de evidencia.

**Recomendación:** para el contrato estable, usar $defs tipados, unevaluatedProperties: false o política explícita de extensiones namespaced/versionadas. No cerrar extensibilidad de forma incompatible sin mecanismo de evolución.

**Criterio de cierre:** tests para typo, campo no soportado, extensión registrada, versión incompatible y preservación de extensiones.

### IDR-12 — Falta separación explícita entre identidad del activo, identidad del actor y atribución de derechos
**Severidad:** MEDIUM  
**Evidencia:** Project/issuer/administrator/controller están diferenciados conceptualmente, pero no hay modelo completo de Actor/Organization/ContractAuthority ni relación entre entidad jurídica, multisig, DAO, deployer y beneficiario.

**Riesgo:** atribuir emisión, control, propiedad o beneficio económico a un proyecto por metadata, repositorio o asociación comercial.

**Recomendación:** modelar Actor/Authority como entidades independientes, roles temporales y evidencia específica. Diferenciar identidad técnica de cuenta/contrato y atribución a persona/organización; no inferir identidad personal.

**Criterio de cierre:** escenarios deployer vs issuer vs admin vs multisig vs project owner vs beneficiary, con atribución UNKNOWN cuando no esté probada.

## 4. Matriz de cobertura de perfiles

| Familia | Perfil documental | Harness/adapter observado | Casos | Estado permitido |
|---|---|---|---|---|
| EVM | Sí | Harness sintético parcial | AS-033..AS-038 y casos comunes | IMPLEMENTED_LIMITED en harness; no validación de red |
| Solana SPL/Token-2022 | Sí | Harness sintético parcial | AS-039..AS-042 y casos comunes | IMPLEMENTED_LIMITED en harness; no validación de red |
| Cosmos SDK/bank/CW20/IBC-ICS20 | Sí, alcance acotado | No observado | AS-043..AS-047 | DOCUMENTED_ONLY |
| UTXO Bitcoin-like | Sí, alcance acotado | No observado | AS-048..AS-050 | DOCUMENTED_ONLY |
| Cardano/eUTXO | Sí | No observado | AS-051..AS-057 | DOCUMENTED_ONLY |
| Otros protocolos/bridges/rollups/derivatives | Parcial/por decidir | No observado | Casos transversales | OUT_OF_SCOPE o UNKNOWN hasta perfil explícito |

IMPLEMENTED_LIMITED describe solo código presente para reglas sintéticas del harness, no soporte de producción ni verificación de red.

## 5. Recomendaciones de resolución

Orden recomendado:
1. Resolver IDR-01 e IDR-02: contrato de grafo y separación claim/purpose.
2. Resolver IDR-04 e IDR-09: evidencia de red y temporalidad.
3. Resolver IDR-07 e IDR-08: semántica relacional de wrappers/bridges y cardinalidad.
4. Resolver IDR-05, IDR-06, IDR-10 e IDR-11: resultados, cobertura y contrato ejecutable.
5. Decidir scope spot/derivatives y atribución de actores (IDR-03/12).

No se recomienda empezar una reescritura del harness antes de acordar los contratos mínimos y el alcance del MVP. El código existente debe preservarse como slice de canonicalización y fixture con límites explícitos.

## 6. Decisiones que requieren aprobación

| Decisión | Propuesta para revisión | Autoridad requerida |
|---|---|---|
| Alcance de instrumentos MVP | Spot-only inicialmente; derivados fuera de alcance hasta modelo separado | Product/methodology owner |
| Estados de identidad y aptitud | Identidad global + claim-level status + purpose-scoped admissibility | Methodology owner / risk owner |
| Perfiles habilitados | EVM/Solana solo para primer slice; otros perfiles documentales no habilitados hasta tests | Engineering + methodology owner |
| Evidencia de red | Contrato común con reglas específicas por familia y abstención ante conflicto | Technical owner |
| Grafo de relaciones | Vocabulario, cardinalidad y qualifiers por tipo | Data architecture + methodology owner |
| Identidad de bridges/wrappers | No inferir solvencia/redención desde relación técnica | Risk owner |
| Extensibilidad de schema | Extensiones namespaced/versionadas y rechazo de campos desconocidos no autorizados | Engineering owner |

## 7. Criterios de cierre WP-02

- [ ] Todos los hallazgos HIGH tienen resolución documental aprobada o riesgo residual aceptado por autoridad competente.
- [ ] Decisión spot-only vs derivatives registrada.
- [ ] Contratos de entidad, relación, claim/evidence y propósito diseñados y revisados.
- [ ] Temporalidad alineada con el framework point-in-time.
- [ ] Cobertura por perfil declara qué está implementado, probado y excluido.
- [ ] Casos de prueba derivados con fixtures y tests ejecutables.
- [ ] Revisión independiente registrada.
- [ ] No se han elevado requisitos a APPROVED/SPECIFIED sin autorización.

## 8. Limitaciones y conclusión

Esta revisión es estática y preliminar. No es una auditoría de seguridad, revisión formal matemática, auditoría legal ni validación empírica de activos/redes. Los hallazgos requieren revisión y decisión de owners autorizados.

**Conclusión:** el modelo es una base conceptual útil, pero IIM-001 e IIM-019 permanecen REVISION_REQUIRED. El harness EVM/Solana es un primer slice de pruebas sintéticas, no un resolvedor integral. WP-02 no está cerrado.
