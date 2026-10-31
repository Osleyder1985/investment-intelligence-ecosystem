# Resolución de identidad de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.3.0

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

## 3. Identificadores internos y atributos de identidad

No existe una clave universal única para todas las entidades. La identidad interna utiliza identificadores tipados y namespaced conforme a la sección 10. Los atributos externos no sustituyen esas claves. Conservar, cuando aplique:

- nombre y ticker observado, como atributos no únicos;
- tipo de activo y tipología económica;
- `network_id` namespaced para la instancia concreta de red; `chain_id` y otros identificadores externos se conservan como atributos con namespace/proveedor, nunca como clave global universal;
- identificador de representación tipado según la familia de red: dirección de contrato, mint, denom, asset ID u otro esquema documentado; para activos nativos, discriminador `NATIVE_ASSET` asociado a `network_id`, nunca una dirección ni marcador insertado en el campo de dirección;
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
2. Resolver `network_id` y corroborar identificadores externos (incluido `chain_id` cuando aplique) junto con la identidad de la red mediante evidencia apropiada; `chain_id` aislado no identifica universalmente una red.
3. Verificar contrato/mint/denom/asset ID según la familia de red mediante nodo o fuente independiente cuando sea posible.
4. Contrastar símbolo, nombre, emisor, decimales, estándar y estado de despliegue.
5. Contrastar IDs de proveedores; no tratarlos como autoridad única.
6. Identificar clones, forks y contratos falsos.
7. Documentar conflictos y conservar todas las candidaturas.
8. Registrar evidencia, fecha de verificación, responsable y protocolo aplicado.

La documentación oficial puede estar desactualizada; corroborar despliegue y estado actual on-chain.

## 6. Wrappers, bridges y representaciones

Cada representación tiene identidad propia y relación explícita con el subyacente. Registrar mecanismo de emisión, custodio/bridge, reservas, derecho de redención, riesgo de despegue y posibilidad de bloqueo. No sumar market caps de representaciones y subyacente como si fueran unidades económicas independientes sin justificar el tratamiento.

El análisis puede agrupar exposición económica para evitar doble conteo, pero mantener riesgos, precios y liquidez de cada representación separados.

## 7. Forks, migraciones y redenominaciones

Mantener identidades separadas para forks con reglas/derechos diferentes. Para migraciones, registrar contrato antiguo y nuevo, fechas de corte, ratio, mecanismo de canje, periodo de soporte y tratamiento de saldos no migrados. Para redenominaciones, documentar factor de conversión y continuidad de series. No concatenar series como si fueran homogéneas sin ajuste explícito y trazable.

## 8. Conflictos y cambios

No sobrescribir ticker, contrato o proveedor histórico. Cada cambio genera evento versionado con fecha de conocimiento, vigencia, evidencia y relaciones. Si el conflicto afecta la unidad económica evaluada, abstenerse hasta resolverlo.

## 9. Salida

La salida debe usar identificadores tipados y no ambiguos:

- `economic_asset_id`, si existe una unidad económica subyacente identificable;
- `network_id` para la red concreta;
- `representation_id` para el activo nativo o token desplegado;
- `instrument_id` para el instrumento negociable;
- `listing_id` para la admisión/versionado en un venue;
- `project_id` y emisor/administrador como relaciones, no como sustitutos de identidad.

El campo genérico `canonical_asset_id` no debe utilizarse como sustituto intercambiable de esos IDs. Si se conserva por compatibilidad, debe declararse qué tipo de entidad identifica en cada registro y proporcionar una migración inequívoca a la clave tipada. Cada evaluación declara su objeto: exposición económica, representación concreta o instrumento/mercado. No resolver esa elección implícitamente a partir del ticker.

La salida incluye relaciones tipadas y versionadas, estado, evidencia, fuentes, timestamps de conocimiento/vigencia, conflictos y limitaciones.


## 10. Modelo de entidades, claves y relaciones (aclaración v0.2.0)

Los identificadores internos deben ser namespaced por tipo de entidad. No reutilizar un único identificador para entidades con semántica diferente.

| Entidad | Identificador interno | Alcance |
|---|---|---|
| Economic asset | `economic_asset_id` | Exposición/derecho económico que se pretende analizar, si puede definirse |
| Network | `network_id` | Instancia concreta de una red, incluyendo entorno y génesis/fork cuando corresponda |
| Asset representation | `representation_id` | Activo nativo o token emitido en una red concreta |
| Market instrument | `instrument_id` | Instrumento negociable definido por representación, venue, mercado y moneda de cotización |
| Listing | `listing_id` | Instancia/versionado de la admisión del instrumento en un mercado |
| Project/protocol | `project_id` | Proyecto o protocolo asociado; no implica propiedad ni derecho del titular |

Las relaciones entre entidades deben ser explícitas, tipadas, con cardinalidad documentada y vigencia temporal. Ejemplos de relación: REPRESENTS, WRAPS, BRIDGES, BACKED_BY, MIGRATES_TO, FORKED_FROM, IMPLEMENTS, ISSUED_BY, LISTED_AS. No asumir que toda representación tiene un único subyacente ni que un subyacente tiene una sola representación.

### 10.1 Clave de red

`network_id` es una clave interna estable y no debe derivarse únicamente de `chain_id`. Registrar namespace, identificadores externos, entorno (mainnet/testnet/private), genesis identifier o ancla equivalente cuando exista, reglas de consenso/identidad y relaciones de fork. Los identificadores externos deben guardar proveedor/esquema/versión.

### 10.2 Identificador de representación

La clave técnica depende de la familia de red:

- EVM: network_id + dirección de contrato normalizada según reglas EVM.
- Solana: network_id + mint address.
- Cosmos y redes similares: network_id + denom/identificador nativo o de módulo, según semántica de la red.
- UTXO: network_id + definición de activo nativo o mecanismo de emisión/asset ID aplicable.
- Otras redes: adaptador de identidad documentado; no forzar el formato EVM.

Para activos nativos usar un discriminador tipado (por ejemplo, `NATIVE_ASSET`) junto con network_id. No usar una dirección ficticia ni una cadena centinela que pueda colisionar con una dirección válida.

### 10.3 Contratos actualizables

Cuando la representación use proxy, registrar por separado la dirección estable del proxy y cada implementación observada, mecanismo de actualización, autoridad/admin conocida, eventos de actualización, bloque/tiempo efectivo y evidencia. El cambio de implementación crea una nueva versión de comportamiento; no sobrescribe el historial ni implica automáticamente una nueva representación económica. Evaluar si cambian derechos, supply, control o riesgo.

### 10.4 Evidencia de resolución

Cada afirmación de identidad debe indicar qué demuestra cada evidencia: existencia/despliegue, control/administración, derechos, respaldo, redención o relación económica. Registrar independencia conocida entre fuentes; dos exploradores que dependen del mismo indexador no son corroboración plenamente independiente. Para evidencia on-chain conservar red, bloque/hash, altura, estado de finalización y referencia de consulta.

Estas aclaraciones siguen siendo borrador hasta completar casos y revisión formal. No constituyen un esquema de base de datos definitivo.


## 11. Cardinalidades, unicidad y contrato de relaciones (borrador v0.3.0)

Esta sección define restricciones estructurales para evitar que las implementaciones mezclen entidades. Las cardinalidades describen el modelo conceptual, no una obligación de que toda relación exista ni una garantía de que la evidencia disponible permita resolverla.

### 11.1 Cardinalidades estructurales

| Relación / dependencia | Cardinalidad conceptual | Restricción |
|---|---|---|
| Asset representation → Network | Cada representación pertenece a exactamente una instancia de red; una red puede contener cero o muchas representaciones. | Una representation_id no puede quedar asociada simultáneamente a dos network_id. Un cambio real de red requiere otra representación y una relación explícita. |
| Market instrument → Base representation | Cada instrumento tiene exactamente una representación base identificada. | No inferirla por ticker; el instrumento debe declarar la unidad base. |
| Market instrument → Quote unit | Cada instrumento tiene exactamente una unidad de cotización por definición de mercado. | La unidad de cotización puede ser una representación o una unidad externa tipada; no forzarla a ser criptoactivo. |
| Listing → Market instrument | Cada listing/episodio de admisión referencia exactamente un instrumento; un instrumento puede tener cero o muchos episodios de listing. | Un relisting o una nueva admisión con identidad/condiciones distintas no sobrescribe el episodio anterior. |
| Asset representation ↔ Economic asset | Relación cero a muchas en ambos sentidos cuando la evidencia lo permita. | No imponer 1:1: una representación puede expresar una cesta/derecho compuesto y un activo económico puede tener múltiples representaciones. Cada arista debe especificar el rol económico. |
| Representation ↔ Representation | Relaciones cero a muchas en ambos sentidos. | WRAPS, BRIDGES, MIGRATES_TO, FORKED_FROM y otras relaciones conservan dirección, tipo, vigencia y evidencia; no son equivalencias implícitas. |
| Project/protocol ↔ Asset representation | Relación cero a muchas en ambos sentidos. | La asociación no prueba propiedad, emisión, control ni derecho económico. Esas afirmaciones requieren relaciones diferenciadas y evidencia propia. |
| Representation → Issuer / administrator | Cero o una entidad identificada por rol y periodo; puede ser UNKNOWN o disputada. | No asumir que emisor, administrador, propietario del proyecto y controlador del proxy son la misma entidad. |
| Proxy representation → Implementation observations | Cero a muchas observaciones/versiones en el tiempo. | El proxy conserva su identidad; cada implementación y cambio se registra como observación temporal separada. |

Las cardinalidades anteriores son límites conceptuales. En particular, una relación REPRESENTS puede ser parcial, multi-subyacente o desconocida; pesos, proporciones, prioridad de redención y condiciones solo se registran cuando estén respaldados por evidencia. La ausencia de peso no autoriza distribuirlo uniformemente.

### 11.2 Claves y restricciones de unicidad

1. Todos los IDs internos son opacos, inmutables, no reutilizables y namespaced por tipo de entidad. No codificar en ellos atributos mutables como ticker, nombre, símbolo, proveedor o estado.
2. La clave natural de una representación se calcula dentro de su network_id mediante el adaptador de identidad de esa red. Para activos nativos, usar el discriminador tipado nativo; para activos emitidos, el identificador de emisión que defina la red. La normalización debe preservar la forma original y documentar reglas de mayúsculas, checksum, prefijos y encoding.
3. No imponer unicidad global a chain_id, dirección, mint, denom, símbolo, nombre ni identificador de proveedor. Los identificadores externos requieren namespace/proveedor, esquema, valor y contexto de vigencia/versión; los alias históricos no se sobrescriben.
4. La clave de instrumento debe distinguir como mínimo venue/mercado, segmento o producto, tipo de mercado, representación base, unidad de cotización y especificaciones contractuales que cambien la exposición (por ejemplo, vencimiento, multiplicador o tipo de contrato). Un ticker de par no basta.
5. Un listing_id identifica un episodio/registro de admisión. La unicidad de IDs externos de listing se limita al namespace del venue y al periodo/versión en que el venue garantiza que son únicos. No asumir que un símbolo de mercado reutilizado identifica el mismo listing históricamente.
6. Las restricciones temporales no deben prohibir por defecto relaciones históricas sucesivas. Si dos afirmaciones incompatibles se solapan en tiempo efectivo, registrar conflicto y bloquear el uso que dependa de resolverlo; no elegir silenciosamente la más reciente.
7. Las reglas de normalización y unicidad son específicas de cada adaptador de red/venue, versionadas y acompañadas por casos positivos y negativos. Si no existe regla demostrable, el resultado es UNVERIFIED, no una unicidad inventada.

### 11.3 Contrato mínimo de una relación

Cada arista de relación debe conservar como mínimo:

- relationship_id y versión;
- relationship_type de un vocabulario controlado;
- tipo e ID de entidad origen y destino;
- valid_from / valid_to o estado explícito de vigencia desconocida;
- known_from / known_to o timestamp de conocimiento equivalente, con vía/población;
- rol, dirección y qualifiers específicos del tipo;
- evidencia asociada, procedencia, independencia conocida y método de resolución;
- estado: CONFIRMED, PARTIALLY_CONFIRMED, DISPUTED, UNVERIFIED, REJECTED o RETIRED;
- versión del protocolo/adaptador que produjo la afirmación.

No convertir UNKNOWN en FALSE. Una relación ausente puede significar no investigada, no observable o refutada; el estado debe distinguir esos casos.

### 11.4 Restricciones semánticas por relación

- REPRESENTS: representación → uno o varios activos/derechos económicos; declarar rol y, si procede, proporción/condiciones. Si la composición no está demostrada, no emitir una asignación cuantitativa.
- WRAPS / BRIDGES: representación origen/destino o mecanismo de bridge, con custodio/operador, mecanismo de bloqueo/mint/burn, backing, redención y periodo. No equivalen a REPRESENTS ni prueban solvencia.
- BACKED_BY: admitir múltiples colaterales y cambios de composición; pesos y haircut solo si la fuente los demuestra. Separar colateral, garantía, reserva declarada y derecho exigible.
- MIGRATES_TO: relación dirigida entre representaciones, con ratio, ventana, elegibilidad, mecanismo y tratamiento de saldos no migrados. No implica continuidad automática de derechos ni de series de precio.
- FORKED_FROM: registrar ancestro, bloque/estado de divergencia y diferencias relevantes. No declarar equivalencia posterior.
- ISSUED_BY, ADMINISTERED_BY, CONTROLLED_BY: roles independientes y temporales; no inferir uno a partir de otro.
- IMPLEMENTS / UPGRADED_TO: vincular proxy, implementación, mecanismo/autoridad, evento, bloque y evidencia. La actualización puede cambiar comportamiento o riesgo sin cambiar automáticamente la identidad de la representación.
- LISTED_AS: enlazar instrumento y listing sin convertir listing o venue en atributo de identidad del activo económico.

### 11.5 Conflictos, ciclos y resolución

No prohibir ciclos en el grafo de forma global: pueden existir relaciones legítimas de respaldo, composición, wrappers o dependencias recíprocas. Las validaciones deben ser específicas del tipo de relación. Por ejemplo, una cadena de MIGRATES_TO con ciclo dentro del mismo periodo efectivo es sospechosa y requiere revisión; un ciclo en relaciones de colateral no es automáticamente inválido. La detección de ciclos genera un hallazgo, no una corrección automática.

La resolución debe poder terminar en RESOLVED, RESOLVED_WITH_LIMITATIONS, AMBIGUOUS, UNVERIFIED, CONFLICTED o RETIRED_OR_MIGRATED, conservando candidatos y evidencia. No usar puntuaciones de confianza sin calibración. La abstención se aplica al alcance concreto afectado: una incertidumbre sobre administración puede bloquear análisis de control, aunque no necesariamente una descripción histórica de precio.

### 11.6 Límite del contrato

Este modelo no determina todavía un esquema físico, tipos SQL, formato de serialización, algoritmo universal de matching ni cardinalidades empíricas para cada protocolo. Esos elementos requieren adaptadores y pruebas por familia de red/venue. IIM-001 e IIM-019 permanecen en revisión hasta completar esos perfiles y obtener aprobación registrada.
