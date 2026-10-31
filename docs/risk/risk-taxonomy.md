# Taxonomía de riesgos de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## Propósito

Lenguaje común para identificar y revisar riesgos según activo, derechos, arquitectura, mercado, custodios, jurisdicción, finalidad y horizonte. No es una lista exhaustiva, puntuación, certificación de seguridad ni sustituto de diligencia debida.

Cada riesgo registra objeto afectado, evento, causa, consecuencia, exposición, horizonte, evidencia, fecha de conocimiento, controles, riesgo residual, incertidumbre, alcance y responsable. No identificar un riesgo no demuestra que no exista.

## Dominios

| Dominio | Riesgos a examinar | Evidencia posible, no concluyente por sí sola |
|---|---|---|
| Identidad e integridad | Ticker ambiguo, contrato falso, suplantación, migración no reconocida, wrapper confundido con activo nativo | Dirección oficial contrastada, exploradores, documentación de migración |
| Fraude y gobernanza | Rug pull, información engañosa, conflictos de interés, control opaco, manipulación de gobernanza | Código, permisos, propuestas/votos, fuentes verificables |
| Contratos inteligentes | Exploit, bug, fallo de lógica, dependencia vulnerable, pérdida o bloqueo de fondos | Código desplegado, auditorías, avisos, transacciones e informes técnicos |
| Administración y upgrades | Claves privilegiadas, multisig débil, upgrade unilateral, pausas, blacklist, mint/burn discrecional | Roles on-chain, umbrales, signatarios, timelocks e historial |
| Consenso e infraestructura | Ataques, reorganizaciones, censura, interrupciones, centralización de validadores/mineros | Métricas verificables, nodos, incidentes y distribución |
| Puentes e interoperabilidad | Fallo de bridge, validadores concentrados, mensajes falsificados, wrapped asset sin respaldo | Contratos, modelo de confianza, custodios, reservas e incidentes |
| Oráculos y dependencias | Dato manipulado, fuente única, retraso, indisponibilidad | Configuración, diversidad de fuentes, latencia e incidentes |
| Custodia y contraparte | Insolvencia, rehypothecation, congelación, pérdida de claves, segregación insuficiente | Contratos, estados auditados, términos y pruebas de control |
| Reservas y redención | Reservas insuficientes, activos gravados, desajuste de vencimientos, redención suspendida | Atestaciones, auditorías, composición, titularidad y términos |
| Tokenomics y derechos | Dilución, desbloqueos, emisiones discrecionales, concentración, derechos económicos inexistentes | Contrato, calendarios, asignaciones, vesting y documentos jurídicos |
| Mercado y liquidez | Spread, profundidad insuficiente, mercado inactivo, wash trading, manipulación | Libro de órdenes, operaciones, profundidad y concentración por venue |
| Operación y continuidad | Fallo de proveedor, pérdida de mantenedores, interrupción, cadena de suministro | Releases, repositorios, dependencias, planes e incidentes |
| Datos y modelo | Datos incompletos, licencia incompatible, revisión retrospectiva, timestamp incorrecto, leakage | Linaje, licencias, hashes, cobertura y validaciones |
| Legal y jurisdiccional | Restricciones de oferta/negociación/custodia, sanciones, licencias, litigios y cambios normativos | Fuentes oficiales, jurisdicción, fecha y asesoría cualificada |
| Fiscal y acceso operativo | Imposibilidad de abrir cuenta, depositar, retirar, convertir o cumplir obligaciones | Reglas oficiales de proveedor/autoridad y términos vigentes |
| Concentración y contagio | Dependencia de entidad, stablecoin, bridge, exchange, cloud o protocolo | Exposiciones, relaciones técnicas/económicas y escenarios |

## Dimensiones transversales

- **Riesgo inherente:** exposición antes de controles.
- **Controles:** medidas preventivas, detectivas y correctivas verificadas.
- **Riesgo residual:** exposición que permanece tras controles comprobados.
- **Incertidumbre:** límites de conocimiento, calidad de evidencia y supuestos.
- **Alcance:** activo, contrato, red, mercado, custodio, jurisdicción, operación y periodo.
- **Temporalidad:** fecha del evento, publicación, captura y evaluación.
- **Interdependencia:** riesgos que comparten una causa o proveedor.

No reducir estas dimensiones a una puntuación única sin metodología y validación.

## Evidencia

Clasificar afirmaciones como HECHO_VERIFICADO, FUENTE_REPORTADA, HIPOTESIS, ALERTA_NO_CONFIRMADA, OPINION o DESCONOCIDO. Registrar fuente primaria, corroborantes, fecha y limitaciones. Una auditoría, atestación o declaración del equipo no demuestra por sí sola ausencia de riesgo; verificar alcance, independencia, fecha, exclusiones y evidencia subyacente.

## Dependencia de tipología

- Gobernanza: poderes efectivos, concentración y cambios de parámetros.
- Utilidad: demanda verificable, sustitución y vínculo entre uso y token.
- Stablecoin: reservas, redención, custodios, contraparte y estabilización.
- Staking/restaking: slashing, validadores, liquidez de salida y dependencias.
- DeFi: contratos, oráculos, liquidaciones, gobernanza y garantías.
- Wrapped/bridged: emisor, bridge, respaldo y derecho de redención.
- RWA: titularidad, vehículo, derechos exigibles, custodio y jurisdicción.
- Moneda nativa: consenso, seguridad económica, emisión y continuidad.

Un activo puede tener varias tipologías. Registrar la combinación y su fundamento. Riesgos desconocidos permanecen explícitos.