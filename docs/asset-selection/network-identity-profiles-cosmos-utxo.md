# Perfiles de identidad por familia de red: Cosmos SDK/IBC y UTXO Bitcoin-like

**Estado:** borrador metodológico; pendiente de revisión independiente y validación con fixtures/datos de red.  
**Versión:** 0.1.0  
**Requisitos relacionados:** IIM-001, IIM-019

Este documento concreta perfiles acotados para redes basadas en Cosmos SDK con IBC/ICS-20 y para redes UTXO de estilo Bitcoin. No afirma que todos los chains Cosmos ni todos los protocolos UTXO compartan una única semántica. Cada implementación requiere un adaptador específico y evidencia temporal.

## 1. Principios

1. Resolver primero la instancia de red y después el identificador de activo dentro de esa red.
2. Distinguir activo nativo, denom bancario, voucher IBC, token de contrato y activo emitido por un protocolo externo.
3. Distinguir activo económico de su representación local: una transferencia IBC puede crear una representación voucher distinta en la cadena destino.
4. No tratar dirección de wallet, dirección de recepción, script, txid u outpoint como identificador universal de un activo.
5. Conservar identificador observado, normalización, protocolo, versión, bloque/altura, evidencia y estado.
6. Si falta el adaptador del protocolo de emisión, identificar el objeto técnico observado sin afirmar que se conoce su identidad económica.

## 2. Cosmos SDK y redes compatibles

### 2.1 Identidad de red

network_id identifica una instancia concreta de cadena, no solo un chain-id. Registrar como mínimo:

- chain-id exacto, incluida su revisión/sufijo si forma parte del formato de la cadena;
- genesis document/hash y, cuando proceda, app hash de referencia;
- checkpoints de altura, block ID/hash, app hash y timestamp;
- entorno declarado y fuentes primarias de configuración;
- endpoints RPC/API consultados, versiones, métodos y evidencia de que siguen la misma historia;
- relaciones de fork, chain reset, upgrade de consenso o sucesión con vigencia.

El chain-id no es una clave global inmutable: puede reutilizarse en redes de prueba, resets, forks o despliegues privados. Si chain-id y genesis no bastan para distinguir dos instancias, exigir checkpoints/registro confiable o dejar AMBIGUOUS/UNVERIFIED. No resolver por el prefijo Bech32 ni por el nombre del proyecto.

### 2.2 Denoms nativos y bank tokens

Para un activo bancario nativo o emitido mediante el módulo bank, la clave natural conceptual es:

(network_id, denom_exacto)

Reglas:

- Preservar el denom exacto, incluida capitalización y caracteres permitidos por las reglas de la cadena.
- No sustituir el denom base por el display denom o símbolo de metadata.
- Registrar metadata denom: base, display, unidades/exponentes, descripción, URI y fuente, con vigencia y timestamp de consulta.
- La metadata puede faltar o cambiar; no constituye prueba suficiente de identidad económica ni de derechos.
- Registrar supply y cuentas/módulos relevantes como observaciones de estado, no como partes de la clave.
- Validar existencia y uso del denom mediante consultas de estado/eventos de la cadena en altura concreta.
- No asumir que dos denoms de cadenas distintas son el mismo activo por compartir ticker, display denom o descripción.

La unidad base del denom y sus unidades de presentación deben mantenerse separadas. Los exponentes declarados por metadata requieren validación y no deben aplicarse retroactivamente a precios históricos sin una versión temporal.

### 2.3 IBC / ICS-20 vouchers

Un voucher IBC es una representación local del activo recibido por un canal IBC. Su identidad local se basa en (network_id destino, denom local exacto), y debe conservarse además el denom trace completo cuando esté disponible.

Registrar:

- denom local, incluido el identificador hash si la cadena lo utiliza;
- denom trace completo: secuencia ordenada de port/channel hops y base denom;
- hash del trace y algoritmo/encoding usados para derivarlo;
- canal/port de origen y destino, estado y evidencia del canal/connection/client;
- packet/acknowledgement relevantes, alturas y timestamps;
- cadena origen y representación de origen resuelta, si puede corroborarse;
- eventos de forwarding/multi-hop y transformaciones aplicadas.

No identificar el voucher solo por el hash si no se puede recuperar o verificar el trace. Si el trace está ausente, el hash no coincide o hay rutas ambiguas, mantener UNVERIFIED/CONFLICTED. Un trace diferente produce una representación local diferente aunque ambas rutas puedan terminar en el mismo activo económico.

No fusionar automáticamente vouchers de rutas diferentes. La relación con el activo de origen debe ser temporal y sustentada por evidencia de packet/acknowledgement y trace. El mecanismo de IBC no garantiza por sí mismo solvencia, redención económica ni equivalencia de riesgo.

### 2.4 Tokens de contrato y módulos de emisión

Un token CW20 u otro token desplegado por contrato no es un bank denom nativo. Para CW20, la clave conceptual es (network_id, contract_address), con validación de existencia/código y consulta de metadatos/estado del contrato en altura concreta.

Registrar por separado:

- dirección de contrato y forma canónica según la codificación de la cadena;
- code ID, checksum/hash del código y versiones de código observadas;
- admin/migrate authority y eventos de migración;
- interfaces/estándar observados y métodos consultados;
- name, symbol, decimals, total supply y otras propiedades como atributos;
- relaciones WRAPS, REPRESENTS, BACKED_BY o ISSUED_BY cuando estén justificadas.

No usar code ID como clave única del token: muchos contratos pueden compartir código y un contrato puede migrar a otra versión. Una migración de contrato no cambia automáticamente la identidad de representación si la dirección y continuidad económica se mantienen; cambios de derechos/control requieren análisis separado.

Los denoms de módulos (por ejemplo, factory denoms) deben resolverse con su gramática y autoridad propias. No parsear un prefijo como prueba suficiente de emisor o control; consultar estado del módulo y validar creator/authority según la implementación concreta.

## 3. Redes UTXO de estilo Bitcoin

### 3.1 Identidad de red

network_id identifica una instancia de red UTXO concreta. Registrar:

- genesis block hash y datos de génesis;
- message-start/network magic y parámetros de consenso relevantes;
- parámetros de dirección/encoding como atributos, no como identidad única;
- checkpoints de altura, block hash y timestamp;
- chainwork/estado de validación cuando aplique;
- fuentes/endpoints y evidencia de que reportan la misma cadena;
- relaciones de fork y divergencia con altura/hash.

Network magic y prefijos de dirección pueden reutilizarse; genesis hash aislado tampoco resuelve todos los clones. Si los anclajes disponibles no distinguen dos instancias, abstenerse. Un fork conserva historia compartida hasta la divergencia, pero las salidas y activos posteriores no deben fusionarse.

Este perfil se limita a cadenas UTXO Bitcoin-like. No se debe aplicar sin adaptación a Cardano/eUTXO, redes basadas en account model, ni protocolos de activos como Liquid, RGB, Taproot Assets, Omni o Counterparty sin un perfil específico.

### 3.2 Activo nativo

El activo nativo UTXO se identifica mediante (network_id, NATIVE_ASSET). La versión del esquema de identificación y del parser se conserva como metadato de procedencia, no como parte de la clave estable. No se identifica por ticker, símbolo de wallet, dirección ni script de recepción.

Registrar como atributos versionados:

- unidad mínima y precisión de presentación según reglas de consenso/software;
- política de emisión, subsidio, halving/reducción, límites y reglas de fee relevantes;
- cambios de parámetros por fork/upgrade;
- estado de consenso y altura de referencia.

No asumir que todos los activos nativos tienen 8 decimales ni que la unidad de presentación es inmutable. Los parámetros deben proceder de reglas de red verificables y conservar su vigencia.

### 3.3 UTXO, transacción y outpoint

Un outpoint (txid, vout) identifica una salida concreta dentro de una historia de red; no es la identidad del activo. Una dirección o scriptPubKey identifica una condición de gasto/recepción, no un activo económico.

Registrar outpoints, scripts, transacciones y bloques como evidencia de emisión, transferencia o tenencia cuando el protocolo de activo lo requiera. Conservar block hash, altura, índice, estado de confirmación/finalización y reorganizaciones. No usar txid aislado sin network_id y contexto de cadena.

### 3.4 Activos emitidos y colored assets

No existe una clave universal de activo emitido para todas las redes UTXO. Cada protocolo debe declarar un perfil de emisión con:

- namespace y versión del protocolo;
- regla determinista de asset ID (por ejemplo, outpoint/transaction de emisión, contract hash u otro identificador definido por el protocolo);
- network_id;
- datos de emisión y pruebas de inclusión/consenso;
- reglas de transferencia, asignación, destrucción/redención y supply;
- relación con scripts/outpoints y cómo se preserva el estado del activo;
- parser/decoder versionado y casos de prueba.

La clave conceptual será (network_id, protocol_namespace, protocol_asset_id) solo cuando el protocolo garantice esa semántica. La versión del estándar y del parser se conserva como metadato de interpretación; solo forma parte del namespace de identidad si el propio protocolo define explícitamente espacios de IDs incompatibles. Si no se puede verificar la regla de derivación o el estado de emisión, registrar el objeto como UNVERIFIED y no atribuirle un ticker/proyecto por heurística.

Activos en sidechains o capas adicionales (por ejemplo, Liquid, RGB o protocolos sobre Taproot) requieren perfil separado que defina su modelo de emisión, validación, custodia y relación con Bitcoin. No asumir que un asset ID de una sidechain comparte namespace o derechos con el activo de la cadena base.

### 3.5 Forks, reorganizaciones y doble conteo

Una reorganización puede retirar una transacción/outpoint de la cadena canónica; mantener la observación anterior con estado de canonicalidad y vigencia, no borrarla. Un fork de red puede replicar salidas históricas, pero las salidas posteriores pertenecen a historias de red distintas.

En activos protocolarios, la validez puede depender de reglas distintas a la mera inclusión de una transacción Bitcoin. La prueba de inclusión de una transacción no demuestra por sí sola que un activo coloreado o de segunda capa se haya emitido o transferido correctamente. Aplicar el validador del protocolo correspondiente.

## 4. Contrato mínimo de evidencia

| Dimensión | Cosmos SDK/IBC | UTXO Bitcoin-like |
|---|---|---|
| Red | chain-id, genesis, checkpoints, endpoint y versión | genesis hash, network magic, consenso, checkpoints y endpoint |
| Activo nativo | network_id + NATIVE_ASSET | network_id + NATIVE_ASSET |
| Activo local | bank denom exacto o contrato/denom de módulo | namespace/version + protocol asset ID, si aplica |
| Voucher/representación | denom trace, hash, port/channel, packet/ack y cadena origen | regla de emisión, tx/outpoint, script/state y validador del protocolo |
| Estado temporal | altura, block ID, app hash, timestamps y eventos | altura, block hash, canonicalidad, reorganización y confirmaciones |
| Comportamiento/control | módulo/contrato, admin/migrate authority, supply y metadata | reglas del protocolo, emisor, asignación y control según protocolo |

La evidencia debe ser suficiente para el uso concreto. Un denom puede estar identificado mientras su relación con el activo de origen permanece UNVERIFIED; un outpoint puede existir mientras la validez de un activo protocolario sigue sin probarse.

## 5. Fuera de alcance y abstención

No se define aquí una implementación universal de IBC trace resolution, parser CW20/factory, decoder de protocolos UTXO, indexador de cadena, esquema físico ni autorización de captura/retención. No se afirma cobertura de todas las redes Cosmos ni de todas las capas/protocolos UTXO.

Si un activo depende de un protocolo no perfilado, conservar la evidencia técnica y clasificarlo como UNVERIFIED para usos económicos que requieran validar sus derechos o supply. No aproximar su identidad por ticker, address prefix, explorer label o proveedor agregado.

IIM-001 e IIM-019 permanecen en REVISION_REQUIRED hasta revisión independiente, perfiles adicionales y validación con fixtures reproducibles.
