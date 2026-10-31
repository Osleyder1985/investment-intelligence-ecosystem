# Casos de consistencia para taxonomía y valoración

**Estado:** Casos documentales de diseño; no son resultados de mercado ni pruebas empíricas de rentabilidad.

## Objetivo

Comprobar que la clasificación y selección de métodos no atribuyen valor económico al token sin evidencia de derechos, captura de valor o activos subyacentes.

## Casos

### VC-001 — Activo monetario sin flujos contractuales
- Entrada: activo nativo con política de oferta y demanda monetaria; no hay derecho del titular a flujos.
- Esperado: DCF de flujos del titular = NOT_APPLICABLE; escenarios monetarios = CONDITIONAL; comparables = CONDITIONAL.
- Fallo que debe detectarse: fabricar ingresos o aplicar DCF por analogía.

### VC-002 — Token de gobernanza con tesorería de protocolo
- Entrada: el protocolo posee tesorería, pero el token solo confiere voto y no derecho de distribución o reclamación.
- Esperado: NAV directo del token = NOT_APPLICABLE; DCF = NOT_APPLICABLE salvo que se demuestre un derecho ejecutable.
- Fallo que debe detectarse: sumar la tesorería al valor del token sin puente jurídico/económico.

### VC-003 — Token con distribución contractual de comisiones
- Entrada: documentación y contratos verifican distribución al titular; hay histórico de flujos, reglas de prioridad y datos de oferta.
- Esperado: DCF = CONDITIONAL, sujeto a continuidad, riesgo, costes, escenarios y tasa justificada.
- Fallo que debe detectarse: usar ingresos brutos del protocolo o ignorar cambios de gobernanza y dilución.

### VC-004 — Stablecoin con redención limitada
- Entrada: paridad objetivo 1:1, reservas reportadas, pero redención limitada a ciertas entidades o jurisdicciones.
- Esperado: paridad/redención = CONDITIONAL; valorar liquidez de mercado y acceso real del titular.
- Fallo que debe detectarse: declarar solvencia o valor garantizado por cotizar cerca de 1.

### VC-005 — Token RWA con titularidad no demostrada
- Entrada: token representa una referencia a un activo físico, pero no hay evidencia de derecho exigible ni cadena de titularidad.
- Esperado: NAV del activo subyacente = INSUFFICIENT_EVIDENCE para el token.
- Fallo que debe detectarse: atribuir al titular del token la propiedad del activo anunciado.

### VC-006 — Token de staking líquido
- Entrada: token redimible por activo delegado, sujeto a cola de salida, slashing y descuento de mercado.
- Esperado: NAV y redención = CONDITIONAL; modelar tipo de cambio, penalizaciones, cola, contraparte y liquidez.
- Fallo que debe detectarse: tratar recompensa nominal como rendimiento neto garantizado.

### VC-007 — Token de utilidad con sustitutos
- Entrada: token se usa para pagar un servicio, pero hay alternativas de pago y el protocolo puede cambiar sus reglas.
- Esperado: demanda/utilidad = CONDITIONAL; evaluar necesidad efectiva de mantener el token y sustitución.
- Fallo que debe detectarse: asumir que el volumen del servicio se convierte íntegramente en demanda del token.

### VC-008 — Token especulativo sin mecanismo de captura
- Entrada: narrativa y volumen observados; no se identifican derechos, flujos, respaldo ni necesidad funcional.
- Esperado: no estimar valor fundamental con evidencia disponible; métricas de mercado se presentan como descriptivas.
- Fallo que debe detectarse: asignar infravaloración extrema a partir de caída de precio o bajo precio unitario.

## Criterio común de aceptación

Cada caso debe producir una explicación reproducible del objeto valorado, clasificación, método elegido o descartado, evidencia necesaria, datos faltantes y límites de conclusión. Un resultado de abstención es correcto cuando la evidencia es insuficiente.
