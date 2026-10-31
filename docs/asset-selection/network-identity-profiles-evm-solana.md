# Perfiles de identidad por familia de red: EVM y Solana

**Estado:** borrador metodológico; pendiente de revisión independiente y validación con fixtures y datos de red.  
**Versión:** 0.1.0  
**Requisitos relacionados:** IIM-001, IIM-019

Este documento concreta reglas de identidad para dos familias de red. Define qué datos identifican una representación, qué atributos solo describen comportamiento/riesgo y cuándo la resolución debe abstenerse. No constituye un adaptador implementado ni una garantía de exhaustividad.

## 1. Principios comunes

1. La identidad de una representación es específica de una instancia de red; una dirección/mint aislada no es globalmente única.
2. Conservar el identificador observado sin normalizar, el valor normalizado, el método y la versión del normalizador.
3. Separar identidad estable de atributos que pueden cambiar: ticker, nombre, metadata, autoridades, código ejecutable, extensiones, supply y estado de negociación.
4. Una respuesta RPC o un explorador no es por sí solo evidencia suficiente de identidad si no se registra la red consultada, el endpoint, el bloque/slot y el estado de finalización.
5. Registrar el estado de resolución como CONFIRMED, PARTIALLY_CONFIRMED, DISPUTED o UNVERIFIED. No convertir ausencia de datos en prueba de inexistencia.
6. Los identificadores de red deben resolverse antes de interpretar identificadores de contrato/mint. Un chain_id o cluster label aislado no basta.

## 2. Perfil EVM

### 2.1 Identidad de red EVM

network_id es una clave interna namespaced para una instancia concreta de red EVM. Como mínimo, el expediente de red debe conservar:

- chain_id observado y su fuente/fecha/bloque; no tratarlo como identificador globalmente único;
- hash del bloque génesis y datos del bloque usados para calcularlo;
- entorno declarado (mainnet, testnet, devnet, private u otro);
- identificadores de red publicados por fuentes primarias y su versión;
- uno o más checkpoints de referencia, con altura, hash, fecha y fuente;
- endpoints consultados, método RPC y evidencia de que responden a la misma historia de cadena;
- relaciones de fork, rollup, testnet o sucesión, con vigencia y evidencia.

El hash génesis ayuda a distinguir redes, pero no garantiza por sí solo unicidad: clones pueden reutilizar configuración o historia inicial. Si dos redes candidatas comparten chain_id y ancla génesis y no existe checkpoint/registro confiable que las distinga, el resultado es AMBIGUOUS/UNVERIFIED. No resolver por popularidad, nombre comercial o proveedor único.

Para redes con historia compartida hasta un fork, conservar identidades de red separadas desde el alcance temporal relevante y una relación FORKED_FROM con bloque/estado de divergencia. No fusionar sus representaciones ni sus series posteriores al fork.

### 2.2 Identidad de representación EVM

Para un token desplegado, la clave natural conceptual es:

(network_id, address_20_bytes)

Reglas:

- Validar que la dirección tenga exactamente 20 bytes y codificación hexadecimal válida.
- La comparación de identidad usa los 20 bytes; la capitalización hexadecimal no crea otra identidad.
- Conservar la forma original y producir una representación canónica hexadecimal minúscula para comparación. El checksum EIP-55 es una validación/forma de presentación, no un espacio de identidad diferente.
- Una dirección no prueba que exista un contrato ni que sea un token. Confirmar existencia/código en un bloque concreto y validar comportamiento de token según el estándar declarado y observado.
- No inferir ERC-20 por nombre, ABI de proveedor o metadata solamente. Registrar interfaz/métodos detectados, eventos y límites de la comprobación.
- Direcciones precompiladas, cuentas sin código, contratos no disponibles en el bloque consultado y direcciones con comportamiento no estándar requieren clasificación específica; no forzar a TOKEN_CONTRACT.
- Para el activo nativo usar (network_id, NATIVE_ASSET, native_asset_scheme/version); nunca asignarle una dirección de contrato ficticia. En redes EVM donde el activo de gas o la configuración nativa sea modificable, versionar esa configuración en el tiempo.

La misma dirección en dos network_id distintos representa dos despliegues diferentes, aunque el bytecode o el ticker coincidan. Una misma dirección en redes que compartieron estado antes de un fork no autoriza a tratar sus estados posteriores como equivalentes.

### 2.3 Proxy, implementación y comportamiento

La dirección del proxy identifica la representación desplegada cuando el proxy es el punto de interacción estable. Registrar por separado:

- dirección del proxy y bloque de despliegue;
- patrón de proxy detectado y método de detección;
- slots/funciones/eventos de implementación y admin examinados;
- implementación observada por bloque, incluyendo dirección y code hash;
- autoridad de actualización, multisig/timelock y cambios observados;
- evidencia on-chain y fuente de interpretación.

La implementación no sustituye la identidad del proxy. Un upgrade crea una nueva observación/versión de comportamiento. Si el proxy cambia de lógica, conservar el intervalo de vigencia y evaluar cambios de derechos, supply, permisos, transferibilidad y riesgo. Si el mecanismo no se puede determinar, marcarlo UNKNOWN; no inferir inmutabilidad por no encontrar un evento.

Para un token no proxy, conservar code hash y observaciones de código por bloque cuando sean relevantes, sin convertir cada cambio de metadata o cada bloque en una nueva representación.

### 2.4 Atributos EVM que no son claves de identidad

symbol, name, decimals, total supply, owner, admin, implementation, code hash, ABI, URI de metadata y estado de listing no son claves de representación. Son atributos/observaciones con procedencia y tiempo. decimals debe registrarse según respuesta del contrato y no suponerse por ticker; anomalías o cambios incompatibles se marcan como conflicto.

## 3. Perfil Solana

### 3.1 Identidad de red/cluster

network_id debe identificar el cluster/instancia concreta, no solo el alias mainnet-beta, devnet o testnet. Conservar:

- cluster label declarado como atributo;
- genesis hash y método/fuente de obtención;
- identificadores/checkpoints de slots y hashes de block cuando estén disponibles;
- endpoint RPC, versión y evidencia de que el endpoint pertenece al cluster esperado;
- estado de finalización utilizado (processed, confirmed, finalized) y contexto temporal;
- relaciones de fork/cluster privado o clon, si procede.

Un alias de cluster no es una clave. Si genesis hash y evidencia disponible no distinguen dos instancias, mantener AMBIGUOUS/UNVERIFIED. Los slots pertenecen a la historia de un cluster y no deben compararse como si fueran una línea temporal universal.

### 3.2 Mint de token

Para una representación SPL Token, la clave natural conceptual es:

(network_id, mint_pubkey_32_bytes)

Reglas:

- Decodificar la dirección Base58 y validar que represente exactamente una public key de 32 bytes.
- Conservar la cadena observada y el valor binario normalizado; no normalizar por mayúsculas/minúsculas como si fuera hexadecimal.
- Verificar en el slot consultado que la cuenta mint existe, está inicializada y que su owner de cuenta es el Token Program esperado.
- Registrar el program ID propietario de la cuenta mint: Token Program clásico o Token-2022, u otro programa si el protocolo usa una implementación diferente. El program ID es evidencia esencial de semántica/ejecución, aunque la clave de la cuenta siga siendo el mint dentro de esa red.
- Registrar decimals, supply, mint authority, freeze authority y estado de inicialización como observaciones del mint, no como partes de la clave.
- Una cuenta con dirección válida pero inexistente, no inicializada o cuyo owner no sea un programa de token compatible no se clasifica como mint confirmado.
- Registrar la evidencia de lectura con slot, commitment/finalization, endpoint y método.

### 3.3 SOL nativo frente a Wrapped SOL

SOL nativo no es un mint SPL. Su identidad debe representarse mediante el discriminador tipado NATIVE_ASSET asociado al network_id y al esquema de activo nativo del cluster.

Wrapped SOL (WSOL) es una representación SPL distinta, identificada por su mint y programa propietario. No fusionar SOL nativo y WSOL como si fueran la misma entidad de representación. Puede existir una relación explícita de representación/conversión con condiciones operativas (cuenta token, sincronización de lamports y cierre), pero la equivalencia económica no elimina las diferencias de custodia, cuenta, transferencia ni disponibilidad.

No codificar una dirección concreta de WSOL como regla universal de la familia; el mint debe corroborarse en la red y periodo de evaluación.

### 3.4 Token-2022 y extensiones

Para mints Token-2022, registrar las extensiones inicializadas y su configuración efectiva en el slot consultado. Entre las propiedades que pueden alterar transferibilidad, costes, privacidad, autoridad o compatibilidad están transfer fees, transfer hooks, permanent delegate, default account state, non-transferable, pausable y otras extensiones soportadas por la versión del programa.

Las extensiones no crean automáticamente una identidad de mint distinta, pero sí pueden cambiar materialmente el comportamiento económico y el riesgo. Deben alimentar el análisis de elegibilidad, liquidez y restricciones de transferencia. Si el parser no soporta una extensión observada, el resultado debe indicar UNKNOWN/UNSUPPORTED y bloquear las conclusiones que dependan de ella.

La presencia de una extensión debe verificarse leyendo los datos de cuenta y decodificándolos con una versión declarada del programa/parser; metadata externa no sustituye esta evidencia.

### 3.5 Autoridades y metadata

Registrar por separado mint authority, freeze authority, permanent delegate, close authority cuando aplique, autoridades de extensiones y cualquier multisig/administrador relacionado. None/revocada es un estado observado en un slot concreto, no una garantía perpetua de ausencia de control en otros niveles del protocolo.

Nombre, símbolo, URI, metadata on-chain/off-chain, imagen y proyecto asociado no identifican el mint. Conservar URI, hash/contenido recuperado si está permitido, timestamp de captura, mutable/immutable flag cuando proceda y limitaciones de disponibilidad. Metadata mutable puede cambiar sin que cambie el mint.

## 4. Requisitos mínimos de evidencia

Cada perfil de red debe emitir un expediente con:

| Campo | EVM | Solana |
|---|---|---|
| Red | chain_id, genesis hash, checkpoints, endpoint y entorno | cluster label, genesis hash, checkpoints/slots, endpoint y commitment |
| Clave de representación | network_id + dirección de 20 bytes | network_id + mint public key de 32 bytes |
| Activo nativo | discriminador nativo tipado | discriminador nativo tipado |
| Existencia | estado de cuenta/código en bloque y método | cuenta mint inicializada y owner program en slot |
| Comportamiento | estándar/interfaz, code hash, proxy/implementation | Token Program/Token-2022, extensiones y configuración |
| Control | admin/roles/proxy y evidencia temporal | mint/freeze/delegate/extensión authorities y evidencia temporal |
| Procedencia | RPC, bloque/hash, método, timestamp y versión del parser | RPC, slot/commitment, método, timestamp y versión del parser |

La falta de un campo no siempre invalida la identidad; debe evaluarse por uso. Por ejemplo, una identidad de mint puede estar confirmada mientras el análisis de transferibilidad queda bloqueado por una extensión no soportada.

## 5. Casos de resolución que deben cubrirse

Los casos AS-033 a AS-042 del catálogo asociado especifican resultados esperados y fallos. Son pruebas de diseño; no se declaran ejecutadas. Antes de implementar, se deben convertir a fixtures reproducibles, con snapshots de red o datos de prueba controlados, sin publicar secretos ni depender de endpoints no autorizados.

## 6. Fuera de alcance

Este perfil no define todavía adaptadores RPC, librerías de decodificación, algoritmo de detección universal de proxies, compatibilidad completa de estándares, catálogo exhaustivo de extensiones Token-2022, resolución de todos los forks ni esquema físico. Tampoco establece que los endpoints o fuentes estén autorizados para captura/retención. Esas brechas requieren revisión técnica y de permisos separada.

IIM-001 e IIM-019 permanecen en REVISION_REQUIRED hasta que perfiles adicionales (Cosmos/UTXO/otros), casos ejecutables, revisión independiente y evidencia suficiente cierren las brechas pertinentes.
