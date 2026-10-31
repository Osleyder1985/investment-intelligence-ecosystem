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
