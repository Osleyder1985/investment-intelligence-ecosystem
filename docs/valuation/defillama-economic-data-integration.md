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
