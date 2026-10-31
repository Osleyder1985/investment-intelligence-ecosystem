# Resolución de identidad de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Principio

El ticker y el nombre comercial no identifican un activo de forma inequívoca. La identidad debe resolverse con evidencia y conservar diferencias entre activo económico, representación tokenizada, red, contrato y mercado.

## 2. Entidades que no deben confundirse

| Entidad | Definición |
|---|---|
| Economic asset | Derecho/activo económico subyacente que se pretende analizar |
| Native asset | Activo nativo de una red, identificado por red y reglas de consenso/emisión |
| Token representation | Token desplegado en una red/contrato que representa un derecho o exposición |
| Wrapped/bridged representation | Representación emitida por custodio, bridge o mecanismo de bloqueo/mint |
| Market instrument | Par negociado en un venue, con moneda de cotización y reglas propias |
| Listing | Registro de negociación del instrumento en un exchange/mercado |
| Project/protocol | Organización, software o protocolo asociado; no necesariamente propietario del token |

Un mismo proyecto puede tener varios activos. Un mismo activo económico puede tener representaciones distintas, con riesgos y derechos diferentes.

## 3. Clave canónica

La identidad interna debe usar un identificador estable propio y conservar, cuando aplique:

- nombre y ticker observado, como atributos no únicos;
- tipo de activo y tipología económica;
- red, chain_id y estándar;
- dirección de contrato normalizada o marcador explícito NATIVE_ASSET;
- emisor/administrador y fuente oficial de identificación;
- identificadores externos de proveedores, con proveedor y versión;
- fecha de creación, despliegue, activación y vigencia;
- relación con activo subyacente, wrapper, bridge, migración o sucesor;
- derechos económicos/jurídicos que representa;
- estado de identidad y evidencia.

No inventar direcciones ni completar identidad por similitud de nombre. Para activos nativos no fabricar una dirección de contrato.

## 4. Estados de resolución

- RESOLVED: identidad corroborada para el uso evaluado.
- RESOLVED_WITH_LIMITATIONS: identidad suficiente con incertidumbres delimitadas.
- AMBIGUOUS: existen varias coincidencias plausibles.
- UNVERIFIED: faltan fuentes fiables para confirmar.
- CONFLICTED: fuentes fiables se contradicen.
- RETIRED_OR_MIGRATED: activo retirado/migrado; mantener identidad histórica y relación al sucesor.

AMBIGUOUS, UNVERIFIED y CONFLICTED no deben pasar a una valoración que dependa de identidad inequívoca. No escoger automáticamente el activo con mayor capitalización o más listings.

## 5. Resolución y corroboración

1. Obtener identificadores desde documentación oficial y fuentes primarias de red.
2. Verificar chain_id y contrato mediante explorador/nodo independiente.
3. Contrastar símbolo, nombre, emisor, decimales, estándar y estado de despliegue.
4. Contrastar IDs de proveedores; no tratarlos como autoridad única.
5. Identificar clones, forks y contratos falsos.
6. Documentar conflictos y conservar todas las candidaturas.
7. Registrar evidencia, fecha de verificación, responsable y protocolo aplicado.

La documentación oficial puede estar desactualizada; corroborar despliegue y estado actual on-chain.

## 6. Wrappers, bridges y representaciones

Cada representación tiene identidad propia y relación explícita con el subyacente. Registrar mecanismo de emisión, custodio/bridge, reservas, derecho de redención, riesgo de despegue y posibilidad de bloqueo. No sumar market caps de representaciones y subyacente como si fueran unidades económicas independientes sin justificar el tratamiento.

El análisis puede agrupar exposición económica para evitar doble conteo, pero mantener riesgos, precios y liquidez de cada representación separados.

## 7. Forks, migraciones y redenominaciones

Mantener identidades separadas para forks con reglas/derechos diferentes. Para migraciones, registrar contrato antiguo y nuevo, fechas de corte, ratio, mecanismo de canje, periodo de soporte y tratamiento de saldos no migrados. Para redenominaciones, documentar factor de conversión y continuidad de series. No concatenar series como si fueran homogéneas sin ajuste explícito y trazable.

## 8. Conflictos y cambios

No sobrescribir ticker, contrato o proveedor histórico. Cada cambio genera evento versionado con fecha de conocimiento, vigencia, evidencia y relaciones. Si el conflicto afecta la unidad económica evaluada, abstenerse hasta resolverlo.

## 9. Salida

La resolución produce canonical_asset_id, representation_id cuando corresponda, identidad de red/contrato, relación económica, estado, evidencia, fuentes consultadas, timestamp UTC, conflictos y limitaciones. Cada evaluación referencia esos IDs, nunca solo el ticker.


## 10. Modelo de entidades, claves y relaciones (aclaración v0.2.0)

Los identificadores internos deben ser namespaced por tipo de entidad. No reutilizar un único identificador para entidades con semántica diferente.

| Entidad | Identificador interno | Alcance |
|---|---|---|
| Economic asset | `economic_asset_id` | Exposición/derecho económico que se pretende analizar, si puede definirse |
| Network | `network_id` | Instancia concreta de una red, incluyendo entorno y génesis/fork cuando corresponda |
| Asset representation | `representation_id` | Activo nativo o token emitido en una red concreta |
| Market instrument | `instrument_id` | Instrumento negociable definido por representación, venue, mercado y moneda de cotización |
| Listing | `listing_id` | Instancia/versionado de la admisión del instrumento en un mercado |
| Project/protocol | `project_id` | Proyecto o protocolo asociado; no implica propiedad ni derecho del titular |

Las relaciones entre entidades deben ser explícitas, tipadas, con cardinalidad documentada y vigencia temporal. Ejemplos de relación: REPRESENTS, WRAPS, BRIDGES, BACKED_BY, MIGRATES_TO, FORKED_FROM, IMPLEMENTS, ISSUED_BY, LISTED_AS. No asumir que toda representación tiene un único subyacente ni que un subyacente tiene una sola representación.

### 10.1 Clave de red

`network_id` es una clave interna estable y no debe derivarse únicamente de `chain_id`. Registrar namespace, identificadores externos, entorno (mainnet/testnet/private), genesis identifier o ancla equivalente cuando exista, reglas de consenso/identidad y relaciones de fork. Los identificadores externos deben guardar proveedor/esquema/versión.

### 10.2 Identificador de representación

La clave técnica depende de la familia de red:

- EVM: network_id + dirección de contrato normalizada según reglas EVM.
- Solana: network_id + mint address.
- Cosmos y redes similares: network_id + denom/identificador nativo o de módulo, según semántica de la red.
- UTXO: network_id + definición de activo nativo o mecanismo de emisión/asset ID aplicable.
- Otras redes: adaptador de identidad documentado; no forzar el formato EVM.

Para activos nativos usar un discriminador tipado (por ejemplo, `NATIVE_ASSET`) junto con network_id. No usar una dirección ficticia ni una cadena centinela que pueda colisionar con una dirección válida.

### 10.3 Contratos actualizables

Cuando la representación use proxy, registrar por separado la dirección estable del proxy y cada implementación observada, mecanismo de actualización, autoridad/admin conocida, eventos de actualización, bloque/tiempo efectivo y evidencia. El cambio de implementación crea una nueva versión de comportamiento; no sobrescribe el historial ni implica automáticamente una nueva representación económica. Evaluar si cambian derechos, supply, control o riesgo.

### 10.4 Evidencia de resolución

Cada afirmación de identidad debe indicar qué demuestra cada evidencia: existencia/despliegue, control/administración, derechos, respaldo, redención o relación económica. Registrar independencia conocida entre fuentes; dos exploradores que dependen del mismo indexador no son corroboración plenamente independiente. Para evidencia on-chain conservar red, bloque/hash, altura, estado de finalización y referencia de consulta.

Estas aclaraciones siguen siendo borrador hasta completar casos y revisión formal. No constituyen un esquema de base de datos definitivo.
