# Catálogo canónico de dominios de riesgo y aplicabilidad

**ID:** IIE-METH-RISK-TAX-004  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-02  
**Fecha:** 2026-10-31

## 1. Propósito

Proponer un catálogo versionado de dominios de riesgo y reglas de aplicabilidad por tipología de activo y scope. Complementa M-04 y el documento 07. No aprueba políticas, no modifica el registro normativo IIM y no demuestra implementación ni validación.

## 2. Estados de aplicabilidad

| Estado | Significado |
|---|---|
| APPLICABLE | El dominio es material para el activo/scope |
| NOT_APPLICABLE | No existe relación material demostrada en el scope |
| APPLICABILITY_UNRESOLVED | No hay evidencia suficiente para decidir |
| APPLICABILITY_CONFLICTED | Hay evidencia contradictoria sobre la aplicabilidad |

NOT_APPLICABLE exige regla versionada, hechos verificables, justificación, fuentes, scope, responsable y reviewer. Falta de datos, ausencia de auditoría, dificultad de análisis o ausencia de incidentes nunca justifican NOT_APPLICABLE. Los estados unresolved/conflicted no se convierten en NOT_APPLICABLE.

## 3. Catálogo propuesto

| Código | Dominio | Evidencia mínima orientativa |
|---|---|---|
| RSK-01 | Protocol & Smart Contract | Código/bytecode y versión; privilegios; upgrades; auditorías y findings; invariantes y dependencias |
| RSK-02 | Cybersecurity & Key Management | Modelo de amenazas; control de claves; signers/validadores; accesos privilegiados; incidentes |
| RSK-03 | Consensus & Network | Consenso; distribución de validadores; supuestos de seguridad; forks e interrupciones; métricas de red |
| RSK-04 | Economic Design & Tokenomics | Supply reconciliado; emisión/unlocks; distribución; vesting; incentivos; dilución y captura de valor |
| RSK-05 | Market, Liquidity & Market Integrity | Order books/trades por venue y lado; profundidad; spreads; volumen depurado; fees; slippage/impacto |
| RSK-06 | Financial & Counterparty | Emisor/contraparte/custodio; exposición; reservas/obligaciones; concentración; incidentes y términos |
| RSK-07 | Custody & Operational | Modelo de custodia; control de claves; segregación; reconciliación; continuidad y retiros |
| RSK-08 | Governance & Control | Distribución de poder; quorum; timelocks; delegación; multisig; historial de propuestas/cambios |
| RSK-09 | Legal & Regulatory | Entidades/jurisdicciones; términos; derechos exigibles; licencias/registro; fuentes oficiales y análisis competente |
| RSK-10 | Data Integrity & Market Abuse | Provenance; controles de calidad; integridad de oráculos; wash trading/manipulación y contraevidencia |
| RSK-11 | Bridge & Interoperability | Trust assumptions; contratos; guardianes/validadores; reservas; redención; pausas e incidentes |
| RSK-12 | Environmental & External Dependencies | Dependencias críticas; concentración geográfica/proveedor; energía/externalidades si son materiales |

Los códigos son identificadores estables propuestos. El catálogo no es exhaustivo; nuevos dominios requieren change control.

## 4. Tipologías no excluyentes

- CAT-01 `NATIVE_NETWORK_ASSET`
- CAT-02 `SMART_CONTRACT_TOKEN`
- CAT-03 `STABLECOIN_OR_REFERENCED_VALUE`
- CAT-04 `WRAPPED_OR_BRIDGED_ASSET`
- CAT-05 `LIQUID_STAKING_OR_RESTAKING`
- CAT-06 `DEFI_PROTOCOL_OR_POSITION`
- CAT-07 `EXCHANGE_OR_PLATFORM_TOKEN`
- CAT-08 `GOVERNANCE_OR_UTILITY_TOKEN`
- CAT-09 `PRIVACY_OR_SPECIALIZED_NETWORK_ASSET`

Un activo puede tener varias tipologías. Cada etiqueta debe sustentarse en características verificables, no solo en marketing.

## 5. Matriz baseline

Leyenda: **M** = normalmente material y debe evaluarse; **C** = condicional, sujeto a regla de activación. La matriz es una hipótesis conservadora a revisar por especialistas.

| Dominio | CAT-01 | CAT-02 | CAT-03 | CAT-04 | CAT-05 | CAT-06 | CAT-07 | CAT-08 | CAT-09 |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| RSK-01 Protocol/Contract | M | M | M | M | M | M | C | C | M |
| RSK-02 Cybersecurity/Keys | M | M | M | M | M | M | M | C | M |
| RSK-03 Consensus/Network | M | C | C | C | C | C | C | C | M |
| RSK-04 Economic/Tokenomics | M | M | M | M | M | M | M | M | M |
| RSK-05 Market/Liquidity | M | M | M | M | M | M | M | M | M |
| RSK-06 Financial/Counterparty | C | C | M | M | C | C | M | C | C |
| RSK-07 Custody/Operational | M | M | M | M | M | M | M | C | M |
| RSK-08 Governance/Control | M | C | M | M | M | M | M | M | M |
| RSK-09 Legal/Regulatory | M | M | M | M | M | M | M | M | M |
| RSK-10 Data Integrity/Market Abuse | M | M | M | M | M | M | M | M | M |
| RSK-11 Bridge/Interoperability | C | C | C | M | C | C | C | C | C |
| RSK-12 Environmental/External | C | C | C | C | C | C | C | C | C |

### Activación condicional

- RSK-03 cuando seguridad, disponibilidad o liquidación dependa de red/consenso externo.
- RSK-06 ante emisor, contraparte, custodio, exchange, market maker, reservas o redención.
- RSK-11 ante bridge, wrapper, mensajería cross-chain o activo representado.
- RSK-12 ante dependencia crítica, concentración geográfica/proveedor o externalidad material.
- RSK-01 ante código ejecutable, contratos, módulos de protocolo o mecanismos de upgrade.

## 6. Procedimiento de aplicabilidad

1. Resolver identidad, representación y controladores relevantes.
2. Asignar todas las tipologías sustentadas; no forzar categoría exclusiva.
3. Cargar versión de catálogo/matriz.
4. Resolver cada celda M/C y documentar el criterio de activación o no activación.
5. Asignar uno de los cuatro estados de aplicabilidad.
6. Exigir revisión especializada para NOT_APPLICABLE en dominios M, conflictos y materias legales/custodia/seguridad materiales.
7. Para cada dominio APPLICABLE, crear assessment con RiskDomainStatus inicial NOT_ASSESSED.
8. Mantener aplicabilidad y estado de riesgo en campos separados.
9. Reportar cobertura con numerador, denominador, desconocidos, exclusiones y justificaciones.
10. Reevaluar ante cambios materiales de categoría, representación, contratos, custodia, red, contraparte, jurisdicción o finalidad.

## 7. Reglas de cobertura

- Un dominio aplicable UNKNOWN, NOT_ASSESSED, STALE, CONFLICTED o INVALID no cuenta como evaluación favorable.
- Un dominio unresolved/conflicted en aplicabilidad bloquea conclusiones dependientes de cobertura completa.
- NOT_APPLICABLE justificado se excluye del denominador de dominios aplicables, pero no cuenta como dominio evaluado sin riesgo.
- No convertir cobertura en score de seguridad ni promediar estados.
- No ponderar dominios sin metodología aprobada y validada.
- La aplicabilidad no determina automáticamente VetoDecision; los vetos se evalúan por regla y scope.

## 8. Casos de aceptación

| Caso | Aplicabilidad | Resultado esperado |
|---|---|---|
| Activo bridged con reserva/custodio | RSK-11 y RSK-06 APPLICABLE | Ambos dominios requieren evaluación |
| Token con contrato de emisión | RSK-01 APPLICABLE | No declarar no aplicable por usar estándar conocido |
| Custodio no identificable por falta de datos | RSK-06 APPLICABILITY_UNRESOLVED | No usar NOT_APPLICABLE; resolver estructura/control |
| Fuentes contradictorias sobre reservas | RSK-06 APPLICABILITY_CONFLICTED | REVIEW_REQUIRED; conservar conflicto |
| RSK-09 declarado no aplicable sin análisis | NOT_APPLICABLE sin fundamento | Rechazar declaración y requerir revisión competente |
| Dominio no aplicable con regla y hechos trazables | NOT_APPLICABLE justificado | Excluir con justificación/versionado |
| Token DeFi bridged | CAT-04 + CAT-06; RSK-11 APPLICABLE | No omitir riesgo bridge por categoría primaria |

## 9. Gobierno y criterios X-02

Cada cambio exige motivación, análisis de impacto, revisión por dominios especialistas, versión, compatibilidad y plan de reevaluación. X-02 solo podrá aceptarse cuando: cada dominio tenga owner/definición/evidencia mínima; las reglas M/C sean reproducibles; NOT_APPLICABLE tenga trazabilidad y revisión; la cobertura conserve denominador y desconocidos; y se aprueben casos positivos, negativos y de frontera.

## 10. Estado y limitaciones

Propuesta documental; no es taxonomía aprobada, asesoría legal, evaluación real de activos, contrato de datos, implementación ni validación empírica. X-02 permanece abierto hasta revisión especializada y aprobación formal.
