# Distinción de identidad entre USDC y USDT

**Estado:** aclaración de dominio para el Android MVP.

## Regla obligatoria

USDC y USDT son criptoactivos distintos. Nunca deben compartir `assetId`, `canonicalKey`, identificador externo, observación de mercado ni ficha de activo.

- **USDC — USD Coin:** emitido por Circle.
- **USDT — Tether:** emitido por Tether.

Que ambos busquen mantener una paridad aproximada con USD no los convierte en el mismo activo ni demuestra que sus riesgos, reservas, mecanismos de redención o liquidez sean equivalentes.

## Identidad y despliegues

El activo canónico se mantiene separado de sus despliegues en redes. Un activo puede tener múltiples contratos/representaciones; cada despliegue debe registrar su red y dirección/identificador específico.

No asociar activos por símbolo, nombre, paridad nominal o semejanza de contrato. Para cada proveedor externo se requiere un mapeo explícito y revisable:

- `assetId` IIE de USDC → identificador externo de USDC.
- `assetId` IIE de USDT → identificador externo de USDT.

Nunca reutilizar el identificador externo de uno para el otro.

## Estado del catálogo demo actual

El seeder actual contiene una ficha sintética para USD Coin (`asset:usd-coin`) con identidad y despliegue sin resolver. No contiene aún una ficha separada para Tether (USDT). Este fixture no constituye una identificación verificada ni un mapeo de proveedor.

La incorporación de USDT al catálogo debe crear un nuevo `assetId` y `canonicalKey` independiente, con sus propios identificadores externos y despliegues verificados. No se debe renombrar ni reutilizar el registro de USDC. El seeder demo incorpora ahora una ficha sintética USDT separada y un identificador dentro del namespace local `iie-demo-fixture`; ese valor `tether` es solo un fixture, no una correspondencia verificada con CoinPaprika ni otro proveedor. Ambas fichas permanecen sin despliegues de red hasta que se verifiquen contratos y redes concretos.

## Consecuencias para la integración de mercado

- El mapeo USDC ↔ proveedor se valida de forma independiente.
- El mapeo USDT ↔ proveedor se valida de forma independiente.
- Si el identificador externo o el despliegue no está resuelto, se omite la consulta de mercado para ese activo.
- Los tickers, precios y métricas nunca se copian de un activo al otro.
- Una cotización cercana a 1 USD no se usa para inferir identidad.

Esta regla es de integridad de identidad y evita contaminación de datos y errores de selección.
