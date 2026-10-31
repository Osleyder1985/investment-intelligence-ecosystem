# Revisión técnica del lote C — IIM-032, IIM-035 e IIM-037

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_RECOMMENDATIONS_ONLY

> Revisión documental asistida. No es auditoría de seguridad, revisión legal, pentest, certificación de custodia, aprobación formal ni validación operativa. Los IDs siguen siendo provisionales; sus estados formales permanecen PENDING_REVIEW.

## 1. Conclusión

| ID | Recomendación | Brecha principal |
|---|---|---|
| IIM-032 | REVISE | Elegibilidad contextual de venues y contrapartes. |
| IIM-035 | REVISE | Seguridad integral, separación de funciones y custodia/firma. |
| IIM-037 | REVISE | Aislamiento técnico y promoción controlada entre entornos. |

Live trading debe permanecer deshabilitado por defecto. Ninguno de estos requisitos, por sí solo, autoriza operación real.

## 2. IIM-032 — Evaluación de venues, mercados, custodios y contrapartes

### Hallazgos

La formulación inicial no separa suficientemente:
- entidad legal, grupo, filial, operador, dominio/API, custodio y subcustodio;
- venue, listing, broker, custodio, emisor, pool/contrato y contraparte;
- permisos de consulta de datos, trading, depósitos, retiros, custodia, staking/lending y derivados;
- jurisdicción, cuenta, instrumento, red, operación, tamaño y exposición;
- riesgo inherente, controles declarados y controles verificados;
- solvencia, segregación de activos, rehypothecation, derechos de retiro e insolvencia;
- incidentes, cambios de términos, vigencia y re-evaluación;
- evidencia desconocida, contradictoria o vencida.

Una entidad conocida no equivale a una ruta aprobada. Aprobar trading no aprueba custodia, depósitos, retiros ni todos sus productos.

### Redacción propuesta

**IIM-032 — Evaluar y mantener decisiones versionadas de elegibilidad y riesgo para cada venue, mercado, listing, intermediario, custodio, emisor o contraparte, con alcance explícito por entidad legal/técnica, jurisdicción, cuenta/usuario, instrumento, red, producto, operación y exposición. Conservar identidad, términos, permisos, evidencia, incidentes, controles, incertidumbre, vigencia y autoridad aprobadora; distinguir aprobación para consulta de datos, trading, depósitos/retiros y custodia; y bloquear o abstenerse cuando identidad, elegibilidad, evidencia o autorización sean desconocidas, contradictorias, restringidas o vencidas.**

### Criterios de aceptación

1. Identifica entidad legal y entidades operativas; no confunde marca, dominio, filial, broker, venue y custodio.
2. Identifica endpoints, dominios, API, contratos, redes y direcciones oficiales con evidencia y control de cambios.
3. Separa decisiones para datos, trading, depósitos, retiros, staking/lending, derivados y custodia.
4. Define alcance por jurisdicción, instrumento/listing, red, cuenta, operación y tamaño.
5. Evalúa continuidad, custodia, segregación, derechos contractuales, rehypothecation y tratamiento en insolvencia según evidencia.
6. Considera liquidez, incidentes, suspensiones, restricciones, litigios y cambios de términos.
7. Distingue evidencia declarada, de terceros e independientemente verificada.
8. Mantiene estados APPROVED_FOR_SCOPE, RESTRICTED, SUSPENDED, REJECTED, REVIEW_REQUIRED y UNKNOWN, o equivalentes.
9. UNKNOWN, TERMS_PENDING, evidencia vencida o conflicto material nunca equivalen a aprobado.
10. Cada aprobación registra autoridad, revisor competente, fecha, versión, fundamento, vigencia y triggers de revisión.
11. Un incidente material suspende o fuerza revisión de la elegibilidad afectada.
12. Ningún score compensa restricción legal, veto crítico o falta de autorización.

**Recomendación: REVISE.** Debe ser una decisión contextual versionada, no una lista estática de exchanges aprobados. La revisión legal corresponde a profesionales competentes.

## 3. IIM-035 — Seguridad, secretos, permisos y custodia

### Hallazgos

La formulación inicial se centra en secretos y mínimo privilegio. Falta precisar:
- autenticación de usuarios, servicios y workloads;
- autorización por acción, recurso, cartera y entorno;
- segregación entre proponer, aprobar, operar, administrar claves y auditar;
- ciclo de vida de credenciales y revocación;
- separación de API keys de lectura, trading y withdrawal;
- protección de claves privadas, signers, recuperación y backups;
- seguridad de red, infraestructura, dependencias, CI/CD y artefactos;
- cifrado, hardening, gestión de vulnerabilidades y logs sin secretos;
- respuesta ante compromiso de claves y revisión independiente.

Un secret manager no resuelve por sí solo la custodia. El motor analítico no debe acceder a claves privadas; el firmante debe estar aislado.

### Redacción propuesta

**IIM-035 — Proteger identidades, credenciales, secretos, material criptográfico, infraestructura y activos mediante controles verificables de autenticación fuerte, mínimo privilegio, segregación de funciones, aislamiento, ciclo de vida de secretos, protección de claves y firma, hardening, seguridad de cadena de suministro, auditoría y respuesta a incidentes. Separar técnicamente análisis, autorización, ejecución y custodia/firma; prohibir secretos y material privado en código, repositorios, logs y artefactos no autorizados; y exigir threat modeling, revisión especializada y pruebas de seguridad antes de cualquier uso con activos reales.**

### Criterios de aceptación

1. Threat model con activos, actores, límites de confianza, amenazas, mitigaciones y riesgos residuales.
2. Autenticación fuerte humana y workload identity para servicios; evitar cuentas compartidas.
3. Autorización granular y deny-by-default.
4. Segregación de funciones con excepciones formalmente aprobadas.
5. Secretos en sistema especializado; nunca en código, Git, fixtures, logs o artefactos públicos.
6. Permisos API separados para lectura, trading y withdrawal; withdrawal deshabilitado salvo aprobación específica.
7. Rotación, expiración, revocación y respuesta a credenciales comprometidas.
8. Claves privadas fuera del motor analítico; firma aislada con autorización verificable.
9. Si existe custodia propia, diseño especializado de HSM/MPC/multisig, quorum, signatarios, recuperación y pruebas independientes. La tecnología elegida no demuestra seguridad.
10. Cifrado, segmentación, hardening, parcheo y seguridad de cadena de suministro.
11. Logs con redacción de secretos, acceso restringido e integridad.
12. Runbooks para filtración de credenciales, compromiso de signer y dependencia comprometida.
13. Revisión de arquitectura y pruebas independientes antes de conectar credenciales con trading/custodia.
14. Un control no probado no se considera efectivo.

**Recomendación: REVISE.** Custodia/firma requiere subdiseño especializado y revisión independiente. No recomiendo desarrollar custodia propia en la fase inicial.

## 4. IIM-037 — Aislamiento de entornos y promoción

### Hallazgos

Separar nombres de entorno o archivos de configuración no demuestra aislamiento. Faltan:
- barreras de red, identidad, permisos, secretos, endpoints, cuentas, wallets y datos;
- imposibilidad técnica de que backtest/simulación/paper alcancen endpoints de trading real;
- artefactos inmutables y promoción de versiones identificadas;
- gates, pruebas negativas, regresión y segregación de datos;
- protección de CI/CD, rollback, revocación e incident response;
- autorización live limitada por cuenta/estrategia;
- barreras adicionales a feature flags y variables de entorno.

### Redacción propuesta

**IIM-037 — Aislar técnica y operativamente los entornos de investigación/backtest, simulación, paper trading y producción mediante separación verificable de identidades, permisos, secretos, red, endpoints, cuentas, wallets, datos y configuración. La promoción debe utilizar artefactos versionados e inmutables y gates independientes con evidencia de pruebas, riesgo, seguridad, cumplimiento y autorización humana explícita; producción debe permanecer deshabilitada por defecto, con rollback, revocación y prevención demostrable de rutas accidentales desde entornos no productivos.**

### Criterios de aceptación

1. Define research/backtest, simulation, paper y live/production.
2. Separa identidades, roles, secretos, cuentas, wallets, endpoints, redes y permisos.
3. Entornos no productivos carecen de credenciales/endpoints con capacidad real de trading o withdrawal.
4. Políticas de red bloquean rutas no autorizadas hacia producción; incluye pruebas negativas.
5. Datos sintéticos, históricos, paper y live se etiquetan y segregan.
6. Artefactos promovidos se identifican por commit, hash, configuración, dependencias, modelo y políticas.
7. Promoción auditable; cambios manuales no registrados quedan prohibidos.
8. Gates separados: especificación, pruebas técnicas, validación, riesgo, seguridad, legal/compliance y autorización humana.
9. Autorización live limitada a cuenta, estrategia, instrumentos, permisos, alcance, vigencia y responsable.
10. Live deshabilitado por defecto; falta de aprobación produce DENY.
11. Feature flags no son la única barrera; se requieren controles de identidad, red, credenciales y servicio.
12. Kill switch, revocación, rollback e incident response definidos y probados.
13. Pruebas negativas demuestran que simulation/paper no pueden emitir órdenes reales ni acceder a secretos productivos.
14. Cambios materiales invalidan gates afectados y exigen reevaluación.

**Recomendación: REVISE.** Una bandera de configuración no constituye una frontera de seguridad.

## 5. Modelo de confianza y gates

Separación conceptual mínima:
- **Research/Analytics:** sin secretos de trading ni material de firma.
- **Decision/Authorization:** valida mandato, límites, vetos y aprobación humana.
- **Order Gateway:** acepta solo órdenes autorizadas y verificables.
- **Signer/Custody:** componente aislado, política de firma y claves protegidas.
- **Reconciliation/Audit:** consume eventos y fuentes de verdad; no altera órdenes ni saldos silenciosamente.

| Gate | Evidencia mínima | Si falta |
|---|---|---|
| G0 Scope | Mandato, cuenta, jurisdicción, producto y operaciones permitidas | DENY |
| G1 Specification | Contratos, políticas y responsables aprobados | DENY |
| G2 Functional | Pruebas reproducibles, idempotencia, recuperación y estados | DENY |
| G3 Risk | Límites, vetos, liquidez y escenarios aprobados | DENY |
| G4 Security | Threat model, revisión independiente, secretos y aislamiento probados | DENY |
| G5 Legal/Compliance | Revisión aplicable, términos y permisos vigentes | DENY |
| G6 Authorization | Aprobación humana explícita, limitada y vigente | DENY |
| G7 Monitoring/Recovery | Alertas, kill switch, revocación, rollback y runbooks | DENY |

Los gates no se compensan entre sí. Un backtest favorable no compensa una brecha de seguridad o una restricción legal.

## 6. Dependencias y evidencia previa a live

IIM-032 consume identidad, licencias, jurisdicción, riesgo, liquidez y contraparte; no asume que popularidad o volumen sustituyen diligencia debida. IIM-035 consume controles de identidad, infraestructura, secretos, ejecución y auditoría; no asume que un secret manager resuelve custodia. IIM-037 consume CI/CD, identidades, secretos, redes, gates y aprobación; no asume que un nombre de entorno proporciona aislamiento.

Antes de live se requiere revisión legal aplicable, threat model, revisión independiente de seguridad, pruebas negativas de aislamiento, evidencia de ausencia de secretos en repositorio/logs/CI, pruebas de revocación y kill switch, pruebas de idempotencia y conciliación, simulación/paper adecuada y runbooks. Documentar estas pruebas no demuestra que se hayan ejecutado.

No seleccionar en esta fase exchanges, brokers, custodios, jurisdicciones, wallets, signers, proveedores KMS/HSM/MPC ni permisos de withdrawal.

## 7. Estado

- IIM-032: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-035: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-037: **PENDING_REVIEW** — recomendación técnica: REVISE.

Esta revisión no sustituye auditoría de seguridad, revisión legal ni aprobación del Issue #34. No modifica el registro oficial ni habilita operación real.
