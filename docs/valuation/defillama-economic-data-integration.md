# Integración económica multifuente: DefiLlama

**Estado:** primer adaptador de proveedor implementado en PR; integración persistente y contraste on-chain pendientes.

## Alcance

Se incorpora un cliente para la API pública gratuita de DefiLlama, sin API key, que consulta por separado:

- `dailyFees`: comisiones de protocolo.
- `dailyRevenue`: ingresos de protocolo según la taxonomía del proveedor.
- `dailyHoldersRevenue`: ingresos que el proveedor clasifica como atribuibles a holders.

Cada tipo se conserva como serie distinta. Se importan los agregados de 24 h, período previo de 24 h, 7 días, histórico total y puntos diarios que entregue el endpoint.

## Límites de interpretación

- Fees, protocol revenue y holder revenue no son magnitudes intercambiables ni se suman.
- La métrica de holders del proveedor no prueba por sí sola un derecho legal, contractual o económico exigible.
- Los resultados son datos agregados del proveedor, no eventos crudos verificados directamente contra un nodo blockchain.
- No se infieren recompras ni quemas a partir de comisiones o ingresos.
- No se habilita automáticamente ningún método DCF, NAV o comparables.
- La asignación de un `protocolSlug` a un `assetId` requiere un mapeo explícito revisado; nunca se deriva solo del ticker o nombre.
- La licencia/condiciones de uso y la metodología de cada métrica deben revisarse antes de usar estos datos en una valoración de producción.

## Primera verificación on-chain: eventos de quema ERC-20

Se añade un lector de logs EVM mediante la API Etherscan-compatible de Routescan:

- Consulta logs del contrato ERC-20 confirmado en una red EVM y un rango de bloques explícito.
- Filtra el evento estándar `Transfer(address,address,uint256)` cuyo destinatario indexado es la dirección cero.
- Conserva hash de transacción, bloque, índice del log, timestamp, contrato, cantidad raw y decimales.
- Convierte la cantidad raw a unidades de token con precisión decimal, sin estimar un valor USD.
- Exige que el contrato y los decimales coincidan con un despliegue registrado para el activo canónico antes de persistir.
- Persiste los eventos de forma inmutable, con SHA-256 e IDs deterministas.

**Límite:** solo se identifican transferencias al cero canónico. No se cuentan automáticamente transferencias a direcciones dead/burn, no se deducen recompras, no se identifican distribuciones y no se trata un evento indexado como confirmación independiente del nodo. Los eventos permanecen `QUARANTINED` / `UNVERIFIED` hasta su contraste RPC y evaluación de finality/reorg.

Routescan documenta acceso keyless gratuito, endpoint de logs Etherscan-compatible, filtros por dirección/rango de bloques y el requisito de atribución para usos fuera de privado. La integración registra `ATTRIBUTION_REQUIRED` y conserva el texto de atribución. Fuente: https://docs.routescan.io/ y https://docs.routescan.io/api/etherscan/logs.

## Contraste independiente por RPC: Ethereum mainnet

Se incorpora una segunda lectura independiente para eventos de quema indexados por Routescan:

- Para chainId 1, consulta `eth_getLogs` en el endpoint público Cloudflare Ethereum RPC, filtrando contrato, bloque, evento Transfer y destinatario cero.
- Exige coincidencia única por transaction hash + log index + contrato.
- Compara topics, cantidad raw y bloque.
- Consulta `eth_getBlockByNumber` y compara el hash canónico del bloque y su timestamp con el log de Routescan.
- Conserva endpoint RPC, hash canónico y hora de comprobación en el objeto de evidencia y en el payload persistido.

El resultado se etiqueta `RPC_CROSSCHECKED_ETHEREUM_MAINNET_PENDING_FINALITY`; la observación continúa `QUARANTINED` / `UNVERIFIED` hasta establecer finality y una política de confirmaciones/reorg. No se extiende silenciosamente el endpoint Ethereum a otras redes: cualquier chainId no configurado produce abstención.

Endpoint: https://cloudflare-eth.com. Referencia JSON-RPC: https://ethereum.org/en/developers/docs/apis/json-rpc/.

## Contrato de observación

Cada observación mantiene: activo canónico, slug del protocolo, métrica del proveedor, período, valor USD, timestamp observado cuando existe, timestamp de recuperación, URL de origen, estado de evidencia y limitaciones.

## Siguiente integración necesaria

1. Persistir las observaciones de forma inmutable en Room, con IDs deterministas, hash del payload y política de idempotencia.
2. Incorporar una tabla explícita de mapeos asset–protocol–deployment y su evidencia de revisión.
3. Añadir fuentes de verificación on-chain específicas por red/protocolo para eventos de fees, distribuciones, buybacks y burns.
4. Contrastar ventanas, unidades, direcciones y transacciones; clasificar coincidencias, diferencias y cobertura.
5. Alimentar los modelos de valoración solo con métricas compatibles y derechos de captura verificados.
6. Añadir UI que muestre valor, ventana, proveedor, timestamp, cobertura y estado de verificación sin ocultar discrepancias.

## Fuente técnica

API pública documentada por DefiLlama: https://api-docs.defillama.com/ (rutas `/summary/fees/{protocol}` y parámetros `dataType`).

## Registro revisado de roles económicos y clasificación conservadora

Se añade un contrato de dominio para registrar direcciones de protocolo con rol explícito: fuente de distribución, ejecutor de recompra, tesorería o colector de ingresos. Cada mapping exige protocolo, activo, chainId, dirección, ventana temporal, URL HTTPS de evidencia y estado de revisión `REVIEWED`. Los mappings incompletos, vencidos o no revisados no participan en la clasificación.

El clasificador de transferencias ERC-20 solo genera candidatos:
- Una salida desde una dirección revisada como `DISTRIBUTION_SOURCE` se etiqueta `DISTRIBUTION_FLOW_CANDIDATE`.
- Una entrada a una dirección revisada como `BUYBACK_EXECUTOR` se etiqueta `BUYBACK_FUNDING_FLOW_CANDIDATE`; no demuestra que se haya ejecutado un swap ni que se haya comprado el token del protocolo.
- Un movimiento que toca una tesorería revisada se etiqueta `TREASURY_MOVEMENT_CANDIDATE`.

Todos los resultados conservan `CANDIDATE_UNVERIFIED`. No se declaran distribuciones pagadas a holders, recompras completadas, ingresos ni captura de valor. Para avanzar se necesita decodificación específica de transacciones/swap, activos de entrada y salida, beneficiarios finales, pruebas de liquidación y validación de eventos mediante RPC/finality.

## Evaluación de evidencia de recompra mediante swap

Se añade un evaluador de evidencia normalizada de swap. Para emitir un candidato exige simultáneamente:
- mapping de ejecutor de recompra en estado `REVIEWED`, vigente y ligado al activo, chainId y contrato del token adquirido;
- executor de la operación coincidente con la dirección revisada;
- recibo de transacción exitoso y evidencia marcada como contrastada por RPC;
- hash de transacción y bloque válidos;
- token de entrada distinto del token del protocolo y cantidades raw positivas en ambos lados;
- token de salida coincidente con el contrato canónico del activo y destinatario explícito.

El resultado conserva token y cantidad de entrada, token y cantidad adquiridos, destinatario, hash de transacción y bloque. Se etiqueta `RPC_CROSSCHECKED_SWAP_PENDING_FINALITY_AND_PROTOCOL_REVIEW`; no es todavía una recompra aprobada para valoración.

**Límite de implementación:** este evaluador consume una estructura normalizada que debe producir un decodificador específico de protocolo/DEX a partir del recibo y sus logs. No se declara que exista un decodificador universal ni se interpreta una transferencia aislada como swap. Antes de usarlo en producción se debe implementar y probar el decodificador concreto (router/ABI, eventos, rutas multihop, agregadores, fees, tokens intermedios, proxy y destinatario), volver a consultar el recibo mediante RPC y aplicar política de finality/reorg y, cuando sea posible, un segundo nodo independiente.


## Decodificador concreto: swaps single-hop tipo Uniswap V2

Se incorpora un primer decodificador real de recibos EVM para el formato de evento `Swap(address,uint256,uint256,uint256,uint256,address)` de pares compatibles con Uniswap V2. No se presenta como decodificador universal de DEX.

Requisitos antes de aceptar el resultado:
- mapping del ejecutor de recompra revisado y vigente para el activo y contrato adquirido;
- mapping revisado del par, chainId, dirección del par y orden `token0/token1`, con evidencia HTTPS y ventana temporal; el mapping del ejecutor y del par debe estar vigente en el timestamp del bloque, no solo al momento de consultar;
- recibo exitoso, contraste RPC obligatorio y coincidencia de hash/bloque en todos los logs;
- exactamente un evento Swap del par revisado;
- una sola pata de entrada y una sola pata de salida, con el token adquirido coincidente con el activo canónico;
- un único evento ERC-20 Transfer hacia el par, cuyo importe raw coincida exactamente con la entrada del evento Swap, y un único Transfer desde el par hacia el destinatario, cuyo importe coincida exactamente con la salida.

Los importes se decodifican como uint256 con precisión completa y se conservan como cadenas decimales raw. El resultado normalizado pasa después por `ProtocolBuybackEvidenceEvaluator`; por tanto, continúa pendiente de finality y revisión del protocolo.

**Abstenciones deliberadas:** swaps multihop, agregadores, múltiples pares Swap en el mismo recibo, split outputs, tokens fee-on-transfer/rebasing cuando el importe transferido no coincide con el evento, pares no revisados, cadenas sin cross-check RPC y logs con bloque/hash inconsistentes. Este primer adaptador no descubre factories ni valida por sí solo que el par pertenezca a un factory oficial; el mapping del par debe proceder de una fuente revisada. Tampoco acredita que el protocolo haya autorizado económicamente la operación: solo aporta evidencia de ejecución compatible con el mapping.

Tests añadidos para swap single-hop válido, mapping pendiente, recibo fallido, falta de cross-check, transferencias de entrada/salida discordantes, eventos Swap ambiguos, executor incorrecto y logs de bloque inconsistente.


## Decodificador concreto: swaps single-hop Uniswap V3

Se añade un decodificador de recibos para el evento estándar `Swap(address,address,int256,int256,uint160,uint128,int24)` de pools Uniswap V3. Este adaptador es deliberadamente estricto y no representa soporte genérico para routers, agregadores ni rutas multihop.

Condiciones de aceptación:
- mapping de pool revisado con chainId, dirección, `token0/token1`, evidencia HTTPS y ventana temporal;
- mapping del ejecutor `BUYBACK_EXECUTOR` revisado, vigente en el timestamp del bloque y ligado al contrato adquirido;
- recibo exitoso, cross-check RPC obligatorio y coherencia de hash, bloque y timestamp en todos los logs;
- exactamente un evento Swap del pool revisado;
- `sender` del evento Swap coincidente con el ejecutor revisado;
- interpretación de `amount0/amount1` como `int256` ABI con complemento a dos y precisión BigInteger, sin truncar a Long;
- una cantidad positiva de entrada y una negativa de salida, con el token adquirido en la pata negativa;
- Transfer ERC-20 de entrada hacia el pool y Transfer de salida desde el pool, ambos con cantidades raw exactas;
- destinatario indexado del evento Swap coincidente con el destinatario del Transfer final.

**Abstenciones deliberadas:** router que reenvía la salida desde su propia dirección, recipient intermedio, multihop, split swaps, múltiples eventos Swap, transferencias discordantes, tokens fee-on-transfer/rebasing, mapping no revisado, chain no configurada y evidencia RPC incompleta. En particular, el evento V3 identifica un recipient de pool, pero no demuestra por sí solo que ese recipient sea el holder final; por eso esta primera versión exige que el recipient del evento y el beneficiario del Transfer de salida coincidan exactamente. No se intenta decodificar calldata de routers ni inferir rutas.

El resultado pasa por `ProtocolBuybackEvidenceEvaluator` y permanece `RPC_CROSSCHECKED_SWAP_PENDING_FINALITY_AND_PROTOCOL_REVIEW`. No se convierte automáticamente en recompra aprobada para valoración. Los tests cubren cantidades signed, pago exacto, pool no revisado, receipt fallido, ausencia de cross-check, sender incorrecto, recipient discordante, transferencias discordantes, swaps ambiguos, chain incorrecta y mapping expirado.


### Decodificación conservadora de claims y pagos directos

Se añade `ReviewedDistributionClaimReceiptDecoder` como adaptador de layout ABI explícito y revisado. No presupone una firma universal de evento: cada integración debe aportar el topic exacto del evento de claim, el índice del topic del beneficiario y la posición de la palabra ABI que contiene el importe, junto con evidencia HTTPS y estado `REVIEWED`. El esquema debe corresponder al mismo chain, distribuidor y token que el mapping de rol `DISTRIBUTION_SOURCE`.

El decodificador exige en un mismo recibo: transacción exitosa, cross-check RPC previo, bloque y logs coherentes, mappings vigentes, exactamente un evento de claim del distribuidor y exactamente un `Transfer` del token desde ese distribuidor. Beneficiario e importe raw deben coincidir exactamente entre claim y pago. Rechaza destinatario cero, pagos múltiples/ambiguos, cantidades discordantes, chain incorrecta y mappings no revisados o expirados.

**Límite de interpretación:** el resultado es `RPC_CROSSCHECKED_CLAIM_PAYMENT_PENDING_FINALITY_AND_HOLDER_REVIEW`. Verifica un pago directo asociado a un evento de claim cuyo layout fue revisado; no prueba finality, que el beneficiario sea una persona/holder final (podría ser un contrato o intermediario), elegibilidad jurídica, ni acumulación de valor para todos los holders. No se incorpora aún a métricas de holder distributions ni a valoración. No hay direcciones, firmas de evento ni ABIs de protocolos concretos hardcodeados; se requieren mappings y esquemas respaldados por evidencia de cada despliegue.


## Primer adaptador de distribución real: Uniswap UNI Merkle Distributor

Se registra un perfil de protocolo específico para Ethereum mainnet, separado del decodificador genérico:

- Protocolo: Uniswap.
- Chain ID: 1.
- Distribuidor: `0x090D4613473dEE047c3f2706764f49E0821D256e`.
- Token: UNI, `0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984`.
- Contrato verificado: `MerkleDistributor`.
- Función ABI: `claim(uint256 index,address account,uint256 amount,bytes32[] merkleProof)`, selector `0x2e7ba6ef`.
- Evento ABI: `Claimed(uint256 index,address account,uint256 amount)`.
- Topic0: `0x4ec90e965519d92681267467f775ada5bd214aa92c0dc93d90a5e880ce9ed026`.

### Layout ABI exacto

La ABI verificada declara **los tres argumentos del evento como no indexados**. Por tanto:
- `topics`: exactamente un elemento, el topic0.
- `data[0]`: índice del claim (`uint256`).
- `data[1]`: beneficiario (`address`, 12 bytes superiores a cero).
- `data[2]`: cantidad (`uint256`).

El perfil específico fija 3 palabras ABI y 1 topic. El decodificador genérico ahora admite beneficiario en un topic o en una palabra de datos, valida el padding ABI de la dirección y rechaza layouts con longitud o número de topics incorrectos.

### Condiciones para aceptar un pago candidato

- Mapping revisado de la dirección distribuidora y del activo UNI en Ethereum.
- Recibo exitoso y marcado como contrastado por RPC.
- Transacción dirigida al contrato distribuidor.
- Exactamente un evento `Claimed` del distribuidor en el recibo.
- Exactamente un `Transfer` del contrato UNI desde el distribuidor.
- Beneficiario y cantidad raw coincidentes exactamente entre evento y transferencia.
- Bloque, hash de bloque, hash de transacción y log indexes coherentes.
- Vigencia temporal del mapping evaluada en el timestamp del bloque.

El perfil no inventa la fecha de inicio del mapping: la integración exige que se proporcione el timestamp de la transacción de creación del contrato obtenida de la fuente canónica. La ABI y dirección se respaldan con el [registro técnico oficial de Uniswap](https://developers.uniswap.org/docs/ecosystem/governance/technical-reference), el [contrato verificado en Etherscan](https://etherscan.io/address/0x090d4613473dee047c3f2706764f49e0821d256e#code) y la [interfaz oficial del repositorio Uniswap](https://github.com/Uniswap/merkle-distributor/blob/master/contracts/interfaces/IMerkleDistributor.sol).

**Interpretación:** el resultado prueba una relación técnica entre un evento claim y un pago directo en el mismo recibo, sujeto a cross-check RPC. No prueba finality, que el beneficiario sea un holder final, ni que exista una distribución recurrente vigente. El airdrop de UNI fue una asignación histórica; no debe presentarse como flujo actual de ingresos o recompensas.

### Cobertura de pruebas

Se agregan fixtures para el layout real no indexado, beneficiario malformado y número de palabras incorrecto. Las pruebas son sintéticas para el decodificador; no se presentan como observaciones de transacciones mainnet reales. La ejecución de Android CI e IIE CI sobre el head final sigue siendo requisito previo a fusionar.


## Segunda ampliación: vesting trimestral verificado y pagos por lotes

### Uniswap UNIVesting (Ethereum mainnet)

Se añade un perfil de detección para el contrato actual de vesting UNI:

- Contrato: `0xCa046A83EDB78F74aE338bb5A291bF6FdAc9e1D2`.
- Token: UNI, `0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984`.
- ABI: contrato `UNIVesting` verificado en Etherscan.
- Funciones de lectura ABI: `UNI()`, `recipient()`, `quarterlyVestingAmount()`, `lastUnlockTimestamp()`, `quartersPassed()`, `owner()`.
- Función de retirada: `withdraw()`.
- Evento: `Withdrawn(address indexed recipient,uint256 amount,uint48 quartersPaid)`.

El perfil fija el topic0 derivado de la firma ABI verificada `Withdrawn(address,uint256,uint48)`: `0xe3a7b52bde0e5e2ab4cd91182c9b2a0c7e49183cc039f6d5c3ba738c84e0427b`. Fija el layout del evento: dos topics (topic0 + recipient) y dos palabras de datos (amount + quartersPaid).

La lectura de calendario se acepta solo si `UNI`, contrato, recipient, importe trimestral, último timestamp de desbloqueo y quarters passed están asociados al mismo bloque canónico y cross-check RPC. Una retirada se reconoce solo cuando el evento `Withdrawn` y un `Transfer` del token UNI desde el vesting contract coinciden exactamente en recipient e importe. El número de trimestres se conserva; no se presupone que el importe sea siempre igual al número de trimestres multiplicado por la cuota, porque el contrato admite retiradas parciales cuando la allowance es insuficiente.

Fuentes: [ABI verificada en Etherscan](https://etherscan.io/address/0xca046a83edb78f74ae338bb5a291bf6fdac9e1d2#code), [despliegues oficiales Uniswap](https://github.com/Uniswap/protocol-fees) y [código fuente UNIVesting](https://github.com/Uniswap/protocol-fees/blob/main/src/UNIVesting.sol). El contrato se describe como un vesting trimestral cuyo primer desbloqueo fue el 1 de enero de 2026; el propietario debe aprobar UNI al contrato para permitir la transferencia.

El contrato histórico `TreasuryVester` `0x4750c43867ef5f89869132eccf19b9b6c4286e1a` es una integración separada: su ABI está verificada, pero el calendario de cuatro años terminó en septiembre de 2025. No se mezcla con el vesting trimestral nuevo.

### Pagos por lotes

Se añade un detector de múltiples pagos ERC-20 en un mismo recibo. Requiere:
- mapping `DISTRIBUTION_SOURCE` revisado y vigente para protocolo, activo, chainId y token;
- recibo exitoso y cross-check RPC;
- logs de bloque/hash/transacción coherentes;
- al menos dos transferencias del mismo token desde la fuente revisada;
- beneficiarios únicos, direcciones ABI válidas e importes raw positivos;
- índices de log únicos.

El resultado es `BATCH_TRANSFER_CANDIDATE_UNVERIFIED` (pendiente de finality y revisión del protocolo). Agrupar transferencias no demuestra por sí solo que se haya invocado una función batch, que cada pago sea una distribución económica o que los receptores sean holders finales. Para elevar el nivel de evidencia se necesita además el ABI específico de la función batch o evento de distribución del contrato y su decodificación de calldata/recibo.

### Límites y pruebas

Los nuevos tests usan fixtures sintéticos para:
- retirada trimestral con evento `Withdrawn` y transferencia UNI exacta;
- mismatch de beneficiario, cantidad y bloque;
- dos o más pagos en una transacción;
- rechazo de pagos únicos, beneficiarios repetidos y falta de cross-check RPC.

No se declaran retiros o pagos por lotes reales observados en mainnet ni se habilita automáticamente valoración. Las observaciones siguen sujetas a finality, reorg, elegibilidad del beneficiario y revisión de derechos económicos.


Las salidas del detector de vesting y batch se persisten mediante Room en `asset_observations`, con ID determinista, SHA-256, idempotencia, referencia al deployment UNI canónico y estado `QUARANTINED / UNVERIFIED`. El store vuelve a validar chainId, contrato/token y campos básicos de evidencia antes de insertar; no confía ciegamente en objetos candidatos construidos fuera del decodificador.
