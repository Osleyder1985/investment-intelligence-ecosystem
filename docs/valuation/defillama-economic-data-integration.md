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
