# Evaluación inicial de proveedor de datos de mercado para Android

**Fecha de revisión:** 2026-10-31  
**Estado:** propuesta técnica; no constituye aprobación legal ni autorización para distribución pública.

## Decisión técnica propuesta

Usar **CoinPaprika Free API** como primer proveedor para el MVP privado y de uso personal, sujeto a mantener la aplicación para investigación personal y cumplir atribución, cuotas y términos vigentes. No habilitar redistribución pública/comercial de sus datos con el plan gratuito.

La elección es pragmática, no una afirmación de superioridad de calidad: el endpoint de ticker no requiere API key, reduce el riesgo de exponer credenciales dentro del APK y devuelve un identificador estable del proveedor, precio USD y timestamp de actualización.

## Comparación acotada

| Criterio | CoinPaprika Free | CoinGecko Demo |
|---|---|---|
| Cuota publicada | 20.000 llamadas/mes por IP | 10.000 créditos/mes |
| Límite de frecuencia publicado | 10 solicitudes/segundo por IP | 100 llamadas/minuto |
| Credencial | No requiere API key | Demo API key |
| Cobertura gratis | Hasta 2.000 activos en endpoints indicados | Endpoints y cobertura sujetos al plan |
| Frescura publicada | Actualización de hasta 10 minutos | Desde 60 segundos |
| Uso | Personal; no asumir derechos comerciales | Demo no incluye derechos comerciales |
| Atribución | “Powered by CoinPaprika” | Atribución obligatoria |

Los límites, productos y licencias pueden cambiar; deben verificarse antes de cada release y antes de cualquier cambio en la distribución.

## Endpoint inicial

GET https://api.coinpaprika.com/v1/tickers/{coin_id}

Campos mínimos del contrato de integración:

- id: identificador de CoinPaprika; conservar como identidad externa, no como identidad canónica IIE.
- symbol: dato descriptivo, nunca usar como clave de asociación.
- last_updated: timestamp declarado por el proveedor.
- quotes.USD.price: precio USD.
- quotes.USD.market_cap y quotes.USD.volume_24h: opcionales, solo cuando estén presentes y válidos.

El tiempo de recuperación debe registrarse por separado en el dispositivo. No reemplaza last_updated.

## Asociación de activos

No inferir coin_id a partir del símbolo. Se requiere un registro explícito y revisable de correspondencia entre assetId IIE y coin_id CoinPaprika.

Los cuatro fixtures actuales no deben mapearse automáticamente:

- Bitcoin (asset:bitcoin) → candidato btc-bitcoin, requiere prueba de identidad.
- Ethereum (asset:ethereum) → candidato eth-ethereum, requiere prueba de identidad.
- Solana (asset:solana) → candidato sol-solana, requiere prueba de identidad.
- USDC (asset:usd-coin) → no asignar hasta resolver explícitamente el activo y su representación; no asumir equivalencia de símbolo/contrato entre redes.

La búsqueda del proveedor puede devolver activos homónimos o tokens envueltos. La selección inicial de identificadores debe quedar explícita y cubierta por pruebas; una coincidencia de nombre/símbolo no basta.

## Restricciones de uso

- El plan Free se anuncia para uso personal. No usarlo para un producto comercial.
- CoinPaprika exige atribución visible en la aplicación.
- La redistribución de datos en bloque o para crear un servicio competidor requiere permiso escrito; la página de planes también reserva la entrega de datos a clientes/productos para Enterprise.
- No publicar ni distribuir una app que exponga o redistribuya datos del plan Free sin volver a evaluar licencia y obtener autorización/plan apropiado.
- No se almacenan API keys en el APK porque el endpoint gratuito no las necesita.
- La aplicación no debe hacer polling frecuente: respetar la actualización publicada y diseñar caché/actualización manual.

## Fallos y límites de confianza

- last_updated ausente, inválido o futuro: no emitir evaluación de frescura.
- Precio ausente, no numérico, no finito o negativo: rechazar el dato como inválido; nunca convertirlo en cero.
- HTTP 429: respetar Retry-After si existe y aplicar backoff; no reintentar en bucle.
- Timeout, desconexión o error HTTP: conservar el último dato local, etiquetado con su timestamp y estado; no presentarlo como actual.
- Un ticker agregado no demuestra liquidez ejecutable, precio de una plataforma concreta ni posibilidad real de vender al precio mostrado.
- La validación de payload no demuestra exactitud, independencia ni integridad económica del proveedor.

## Fuentes oficiales consultadas

- CoinPaprika API y ejemplos de ticker: https://coinpaprika.com/api
- CoinPaprika pricing y límites: https://coinpaprika.com/api/pricing
- CoinPaprika API Terms of Use: https://coinpaprika.com/terms-api
- CoinGecko API pricing: https://www.coingecko.com/en/api/pricing
- CoinGecko API Terms: https://www.coingecko.com/en/api_terms
- CoinGecko Demo API rate limits: https://support.coingecko.com/articles/5125303456-what-is-the-rate-limit-for-coingecko-api-public-plan

## Próxima implementación

1. Aprobar explícitamente el alcance de uso personal del proveedor.
2. Añadir contrato de observación de mercado separado de IDENTITY_FIXTURE.
3. Añadir el mapeo externo explícito y validado.
4. Integrar HTTP con timeout, manejo de errores, límites de frecuencia y caché.
5. Persistir la observación en Room usando source registry y timestamps separados.
6. Mostrar precio, origen, hora del proveedor, hora de recuperación y atribución en la ficha.
7. Añadir pruebas unitarias, Room instrumented tests y CI.

Esta evaluación no autoriza análisis de mercado, ranking ni decisiones de inversión.
