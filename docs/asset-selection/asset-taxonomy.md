# Taxonomía económica de criptoactivos

**Estado:** Propuesta metodológica; pendiente de revisión y validación.  
**Versión:** 0.1.0  
**Ámbito:** Clasificación económica para selección y valoración. No sustituye una clasificación jurídica o regulatoria.

## 1. Principios de clasificación

1. Clasificar los derechos y mecanismos económicos del activo, no solo su nombre comercial, ticker o categoría de marketing.
2. Separar el token, la red, el protocolo, la entidad emisora, los activos subyacentes y los derechos exigibles por el titular.
3. Permitir varias etiquetas simultáneas. Un activo puede ser híbrido y su función puede cambiar con el tiempo.
4. Cada clasificación debe indicar evidencia, fuente, jurisdicción relevante cuando aplique, fecha efectiva, fecha de observación y estado de incertidumbre.
5. No inferir derechos económicos a partir de declaraciones promocionales: verificar contratos, documentación vinculante, mecanismos on-chain y condiciones de redención.
6. La categoría no determina automáticamente un modelo de valoración ni implica que exista información suficiente para valorar.

## 2. Dimensiones observables

| Dimensión | Pregunta de clasificación | Evidencia preferente |
|---|---|---|
| Derechos del titular | ¿Qué puede exigir o hacer el titular? | Documentación vinculante, términos, contratos, mecanismos de redención |
| Función técnica | ¿Qué función cumple en una red o protocolo? | Especificación, código, documentación técnica |
| Captura de valor | ¿Cómo, si acaso, llega valor económico al token? | Distribuciones, quema, recompra, tarifas, derechos exigibles |
| Gobernanza | ¿Qué decisiones puede votar y con qué efecto? | Contratos de gobernanza, reglas de voto y ejecución |
| Oferta | ¿Cómo se emite, desbloquea, quema o retira? | Contratos, calendarios, datos de circulación |
| Respaldo | ¿Qué activos o garantías respaldan el token? | Informes de reservas, custodios, auditorías, pruebas y derechos de reclamación |
| Redención | ¿Quién puede redimir, bajo qué condiciones y con qué coste? | Contratos y términos de redención |
| Dependencias | ¿Qué entidades, oráculos, puentes, custodios o validadores son críticos? | Arquitectura, contratos y divulgaciones |
| Mercado | ¿Dónde se negocia y qué profundidad existe? | Mercados identificados y datos observados |

La ausencia de evidencia se registra como DESCONOCIDO; no se convierte en una afirmación negativa ni positiva.

## 3. Etiquetas económicas principales

Las siguientes clases son etiquetas no excluyentes. Un activo puede pertenecer a varias.

### 3.1 Activo monetario o de reserva digital
Su tesis económica principal se relaciona con conservar o transferir valor, escasez, neutralidad, seguridad y demanda monetaria. No presupone respaldo por activos ni derecho de redención. La emisión, la política monetaria, la seguridad y la demanda deben analizarse separadamente.

### 3.2 Activo nativo de red / recurso de consenso
Se utiliza para pagar comisiones, asegurar consenso, participar en staking o cumplir funciones esenciales de una red. Debe distinguirse la demanda funcional de la captura de valor por el titular. Las recompensas de staking pueden compensar inflación, riesgo de slashing, bloqueo y coste de oportunidad; no son automáticamente rendimiento económico neto.

### 3.3 Token de utilidad o acceso
Otorga acceso a un servicio, capacidad, recurso o producto. El análisis debe identificar si el uso exige comprar/mantener el token, si existen sustitutos, cómo se fija el precio del servicio y si la demanda se traduce en demanda neta del activo.

### 3.4 Token de gobernanza
Confiere derechos de voto u otras facultades de gobernanza. No debe atribuirse al token el valor de ingresos, tesorería o activos del protocolo salvo que existan derechos jurídicos o mecanismos ejecutables que lo sustenten. El poder de voto no equivale por sí mismo a derecho de propiedad.

### 3.5 Token con captura de comisiones, ingresos o flujos
El diseño puede transferir flujos al titular, habilitar reclamaciones o ejecutar distribuciones. Debe probarse el derecho, su continuidad, prioridad, restricciones, contraparte y exposición a cambios de gobernanza. Los ingresos del protocolo no son ingresos del token por defecto.

### 3.6 Token de staking, restaking o participación
Representa una posición bloqueada, delegada, líquida o derivada de staking/restaking. Deben modelarse activos subyacentes, tipo de cambio de redención, penalizaciones, slashing, cola de salida, riesgo de contraparte y descuentos de liquidez. No confundir rendimiento nominal con retorno real.

### 3.7 Stablecoin o token referenciado a activos
Busca mantener paridad con una unidad o cesta de referencia. Clasificar por mecanismo: reservas, sobrecolateralización, arbitraje/algorítmico, híbrido u otro. Registrar derechos de redención, composición y liquidez de reservas, custodios, segregación, auditoría/atestación, riesgo de contraparte y jurisdicción. La paridad objetivo no garantiza redención ni solvencia.

### 3.8 Token respaldado por activos / RWA
Representa exposición, derecho de crédito, participación, propiedad, usufructo o reclamación sobre activos fuera de la cadena. Identificar titular legal, vehículo, custodio, prioridad, gravámenes, valoración independiente, ejecución transfronteriza y restricciones de transferencia. La mera tokenización no demuestra titularidad efectiva.

### 3.9 Token de liquidez, participación en pool o recibo de depósito
Su valor puede derivar de una cesta de activos y pasivos, comisiones acumuladas y reglas de rescate. Separar NAV teórico, valor redimible, riesgo de pérdida impermanente, deuda, oráculos y liquidez efectiva.

### 3.10 Token derivado, sintético o apalancado
Su exposición depende de colateral, contratos, contraparte, funding, liquidaciones, oráculos y reglas de ajuste. No valorar como si fuera el activo subyacente sin modelar las diferencias y riesgos de base.

### 3.11 Token de coleccionable, NFT o derecho digital singular
Puede representar un objeto digital, acceso, licencia o derecho contractual. La valoración depende de derechos transferidos, escasez verificable, utilidad, autenticidad, liquidez y comparables; precios de ventas aisladas no bastan para establecer valor razonable.

### 3.12 Token especulativo o comunitario
La demanda puede depender principalmente de atención, narrativa, comunidad y liquidez, sin derechos ni flujos económicos identificables. No se debe fabricar un valor fundamental cuando no existe una base de valoración defendible. Puede evaluarse riesgo y comportamiento de mercado por separado.

## 4. Clasificación híbrida y cambios

- Registrar una etiqueta primaria descriptiva solo si la evidencia lo permite; conservar todas las etiquetas secundarias.
- Registrar fecha de inicio y fin de vigencia de cada característica.
- Mantener versiones históricas: un cambio de tokenomics, contrato, gobernanza, respaldo o derechos puede alterar la clasificación.
- Distinguir cambio efectivo de un anuncio o propuesta no ejecutada.
- Registrar conflictos entre documentación, código y comportamiento observado.
- La clasificación regulatoria se mantiene en un expediente separado, por jurisdicción y fecha, con fuente jurídica competente.

## 5. Resultado de clasificación

Cada expediente debe contener:

- Identificador inequívoco del activo, red y contrato/identificador nativo.
- Etiquetas económicas y dimensiones observadas.
- Derechos del titular verificados y derechos no demostrados.
- Evidencia y fuentes con fecha.
- Entidades, custodios, contratos y dependencias críticas.
- Cambios históricos y eventos pendientes.
- Datos faltantes, contradicciones y nivel de confianza cualitativo justificado.
- Métodos de valoración candidatos y razones de aplicabilidad o descarte.

La clasificación no constituye recomendación de inversión ni prueba de infravaloración.
