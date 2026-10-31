# Distinción de identidad entre USDC y USDT

**Estado:** aclaración de dominio y mapeo de IDs de proveedor para el Android MVP.

## Regla obligatoria

USDC y USDT son criptoactivos distintos. Nunca deben compartir `assetId`, `canonicalKey`, identificador externo, observación de mercado ni ficha de activo.

- **USDC — USD Coin:** emitido por Circle. Identificador CoinPaprika: `usdc-usd-coin`.
- **USDT — Tether:** emitido por Tether. Identificador CoinPaprika: `usdt-tether`.

Que ambos busquen mantener una paridad aproximada con USD no los convierte en el mismo activo ni demuestra que sus riesgos, reservas, mecanismos de redención o liquidez sean equivalentes.

## Estado en el catálogo Android

El catálogo demo contiene dos fichas sintéticas independientes: `asset:usd-coin` (USDC) y `asset:tether` (USDT). El seeder registra los IDs de CoinPaprika en el namespace `coinpaprika`, vinculados a sus respectivos `assetId`, y registra la fuente con uso `PERSONAL_USE_ONLY`.

Estos IDs identifican los activos en el catálogo del proveedor. No demuestran identidad de un contrato específico ni validan despliegues multi-chain. Ambas fichas siguen sin despliegues de red asignados hasta verificar contratos y redes concretos.

## Consecuencias para la integración de mercado

- El mapeo USDC ↔ `usdc-usd-coin` se valida independientemente.
- El mapeo USDT ↔ `usdt-tether` se valida independientemente.
- No asociar activos por símbolo, nombre, paridad nominal o semejanza de contrato.
- Si el ID externo no está resuelto o no coincide con la respuesta, se rechaza la observación.
- Los tickers, precios y métricas nunca se copian de un activo al otro.
- Una cotización cercana a 1 USD no se usa para inferir identidad.

## Alcance y licencia

El plan gratuito de CoinPaprika se describe como destinado a uso personal. La aplicación MVP se está construyendo como herramienta local personal. No asumir que ese plan permite redistribuir cotizaciones a terceros ni publicar una aplicación comercial con esos datos. La atribución exigida por el proveedor debe mostrarse cuando se presenten sus datos.

Esta regla es de integridad de identidad y evita contaminación de datos y errores de selección.
