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
