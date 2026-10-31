# Perfil de identidad de activos nativos Cardano/eUTXO

**Estado:** borrador metodológico; pendiente de revisión independiente y validación con fixtures/datos de ledger.  
**Versión:** 0.1.0  
**Requisitos relacionados:** IIM-001, IIM-019

Este perfil cubre activos nativos del ledger Cardano/eUTXO, incluyendo ADA y native tokens. No es una extensión del perfil Bitcoin-like: las reglas de identificación, metadata, minting policies y UTXO son específicas de Cardano.

## 1. Identidad de red

network_id debe identificar una instancia concreta de ledger Cardano. Conservar como mínimo:

- network id de ledger y network magic observados, como atributos y no como claves globales únicas;
- hashes y versiones de los documentos génesis aplicables (Byron/Shelley y otros parámetros de arranque pertinentes);
- checkpoints de referencia: epoch/slot, block hash, block number y evidencia de la fuente;
- entorno declarado (mainnet, preprod, preview, testnet privado u otro);
- endpoints consultados, versión del nodo/indexador y evidencia de que reportan la misma historia;
- relaciones de fork/reset y divergencia, con vigencia temporal.

El network id y network magic pueden repetirse en entornos distintos. No resolver una instancia solo por el prefijo de dirección (addr, addr_test), network id o etiqueta del proveedor. Si genesis/checkpoints no distinguen dos instancias, mantener AMBIGUOUS/UNVERIFIED.

## 2. ADA y activos nativos

### 2.1 ADA

ADA es el activo nativo base del ledger. Para el modelo interno se identifica mediante (network_id, NATIVE_ASSET, ADA_SCHEME_VERSION), no mediante policy ID ni asset name ficticios.

La unidad contable on-chain es lovelace; 1 ADA equivale a 1,000,000 lovelace. Las cantidades del ledger son enteros. No modelar ADA como un token ERC-20 ni inventar una política de emisión/contrato.

### 2.2 Native token Asset ID

Un native token Cardano se identifica por la combinación:

(network_id, policy_id_28_bytes, asset_name_bytes)

Reglas:

- policy_id es el hash de 28 bytes de la minting policy script que autoriza la emisión/burn.
- asset_name es una secuencia de bytes de hasta 32 bytes; puede estar vacía y no debe asumirse que sea UTF-8.
- Preservar los bytes originales y su codificación hexadecimal canónica. Una representación textual legible es un atributo derivado, no la identidad.
- La misma asset name bajo policy IDs distintos identifica activos distintos.
- La misma policy ID puede agrupar múltiples asset names, cada uno con identidad separada.
- No usar ticker, nombre, metadata, wallet address, payment credential o stake credential como sustituto del Asset ID.
- El policy ID no identifica por sí solo al emisor, proyecto, beneficiario económico ni autoridad administrativa; es el hash de la política de minting.

El Asset ID del ledger puede representarse como concatenación de policy ID y asset name en hexadecimal. El modelo IIM añade network_id para evitar colisiones entre redes, forks y entornos de prueba.

### 2.3 Política de minting y supply

Registrar la minting policy script o evidencia verificable de su hash, language/version, parámetros relevantes, transacción de creación y reglas de ejecución. Conservar eventos de mint/burn, cantidades, txid, block hash, slot, inputs/outputs y canonicalidad.

La política controla qué operaciones de mint/burn autoriza; no debe suponerse que equivale a una autoridad administrativa mutable ni que gobierna cada transferencia posterior. Las restricciones de gasto/transferencia pueden depender de scripts/validators y del contexto UTXO.

Si la política contiene condiciones temporales (por ejemplo, límite de slot), registrar el intervalo de validez y evaluar la posibilidad de nueva emisión según la política y estado observados. No inferir supply máximo, inmovilidad o imposibilidad de mint futuro solo por el nombre del token o una etiqueta del explorer.

## 3. UTXO y ledger value

Un UTXO identifica una salida concreta de una transacción en una historia de red. Puede contener ADA y múltiples native tokens. No es la identidad del activo.

Registrar como evidencia de tenencia/transferencia:

- txid, output index, block hash, slot/height y estado de canonicalidad;
- value/bundle completo, con policy IDs, asset names en bytes y cantidades;
- datum hash/inline datum y reference inputs cuando sean relevantes;
- payment credential/script address y stake credential como condiciones de control/participación, no como identidad de activo;
- reorganizaciones, rollback y nueva inclusión en cadena canónica.

Una dirección puede recibir muchos activos; un activo puede aparecer en múltiples UTXOs y direcciones. No crear identidades nuevas por cada UTXO ni fusionar activos por compartir dirección.

## 4. Metadata: CIP-25, CIP-26, CIP-67 y CIP-68

Metadata y Asset ID deben mantenerse separados. La metadata puede ayudar a describir o clasificar, pero no sustituye policy_id + asset_name.

### 4.1 CIP-25

CIP-25 define metadata de native tokens en transaction metadata bajo el label 721. Registrar txid, block/slot, versión/forma del metadata, policy ID y asset name al que se asocia.

La presencia de un objeto bajo label 721 no demuestra por sí sola que la metadata sea auténtica, completa, única o impuesta por el ledger. Verificar que la asociación corresponde al Asset ID y conservar conflictos, duplicados y versiones.

### 4.2 CIP-26 Token Registry

CIP-26 es un registro de metadata off-chain. Si se consulta, registrar el endpoint/proveedor, versión, fecha de consulta, términos/permiso aplicables, campos y procedencia. No tratar ticker, decimals, nombre, logo o URL de registro como verdad on-chain ni como clave de identidad.

Los decimales de presentación para native tokens no son una propiedad de cantidad impuesta por el ledger. La cantidad permanece entera; si una fuente de metadata declara decimals, conservarlo como convención de presentación con procedencia, no alterar el valor bruto ni asumir que está universalmente aceptado.

### 4.3 CIP-67 asset name labels

Los labels CIP-67 se codifican como prefijos dentro del asset name. Registrar asset_name bytes completos. El label decodificado es un atributo de clasificación, no una identidad separada ni un motivo para eliminar bytes del Asset ID.

Un parser debe validar el formato/CRC y declarar la versión de la tabla de labels. Si no reconoce el prefijo o el label, conservar el asset name íntegro y clasificar el label como UNKNOWN; no truncar ni normalizar bytes heurísticamente.

### 4.4 CIP-68 reference NFT y user token

CIP-68 define una convención que relaciona un reference NFT y un user token bajo la misma policy ID, usando asset name labels y un datum asociado a un UTXO de referencia. Registrar cada Asset ID por separado y añadir una relación tipada solo después de validar:

- misma policy ID y patrón de nombres/labels esperado;
- unicidad y correspondencia del reference NFT para el user token según el estándar y clase aplicables;
- UTXO que contiene el reference NFT y datum;
- versión del datum, contenido, slot, block hash y referencia input/estado;
- script/address que controla el reference UTXO y posibilidad de actualización.

El metadata datum puede evolucionar según las reglas del script. No sobrescribir metadata histórica ni asumir que el reference NFT es el token que el usuario mantiene en su wallet. El user token y reference NFT son activos distintos relacionados por CIP-68.

CIP-25 y CIP-68 son mecanismos diferentes. No combinar sus campos como si fueran una única fuente sin registrar qué estándar y versión produjo cada dato.

## 5. Derechos, fungibilidad y riesgos

No inferir fungibilidad económica únicamente porque dos registros comparten policy ID. Verificar el Asset ID completo y, para afirmaciones de clase fungible/NFT/RFT, comprobar supply, cantidad, reglas de emisión y estándares declarados.

Registrar, cuando existan, script validators que restrinjan transferencias, freezes/lockups implementados mediante scripts, royalties/claims, referencias externas, collateral o derechos de redención. No confundir metadata declarativa con derechos exigibles.

La asociación con proyecto, creador o marketplace requiere relaciones tipadas y evidencia propia. La policy ID por sí sola no prueba quién controla el proyecto ni quién recibe el beneficio económico.

## 6. Evidencia mínima

| Dimensión | Evidencia |
|---|---|
| Red | genesis/checkpoints, network id/magic, endpoint y versión |
| Identidad nativa ADA | network_id + discriminador NATIVE_ASSET |
| Identidad native token | network_id + policy_id (28 bytes) + asset_name (0–32 bytes) |
| Mint/burn | política/script, transacción, cantidades, slot/block y canonicalidad |
| Tenencia/transferencia | UTXO/outpoint, value bundle, datum/reference inputs cuando proceda |
| Metadata | CIP/versión, txid o datum/registry, procedencia y tiempo |
| Control/restricciones | policy script, validators, scripts/direcciones y reglas verificadas |
| Relaciones | reference NFT/user token, wrapped/bridged/backed-by, proyecto/issuer con evidencia y vigencia |

## 7. Fuera de alcance

Este perfil no define indexador Cardano, parser universal Plutus datum, validación de todos los lenguajes/eras, cálculo completo de supply en presencia de forks, ni resolución de todos los estándares de metadata. Los adaptadores deben fijar era/ledger version y conservar reglas históricas.

No se afirma que cualquier explorer, token registry o endpoint sea autorizado para captura/retención. IIM-002 debe aprobar cada fuente y operación por separado.

## 8. Referencias técnicas primarias

- [Cardano Developer Portal — Native Tokens](https://developers.cardano.org/docs/developers/curriculum/native-tokens/overview/)
- [CIP-0025 — Media Token Metadata Standard](https://cips.cardano.org/cip/CIP-0025)
- [CIP-0068 — Datum Metadata Standard](https://cips.cardano.org/cip/cip-68)
- [Cardano Developer Portal — Token Metadata & Registry](https://developers.cardano.org/docs/native-tokens/cardano-token-registry/)

Los documentos anteriores son referencias técnicas del protocolo/estándares; no constituyen por sí solos autorización para almacenar o redistribuir datos de proveedores.

IIM-001 e IIM-019 permanecen en REVISION_REQUIRED hasta revisión independiente y validación con fixtures reproducibles.
