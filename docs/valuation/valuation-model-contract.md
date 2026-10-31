# Contrato de modelo de valoración

**Estado:** Especificación propuesta; pendiente de revisión y validación empírica. **Versión:** 0.1.0

## 1. Objetivo

Todo modelo debe declarar qué estima, para quién, en qué fecha, bajo qué derechos, unidades, datos y supuestos. Un nombre de método (DCF, NAV, comparables) no basta para hacer reproducible una valoración.

## 2. Identificación

| Campo | Descripción |
|---|---|
| model_id / model_version | Identificador y versión inmutable |
| canonical_asset_id | Activo inequívoco |
| representation_id | Representación negociable, si aplica |
| valuation_object | Token, derecho, claim, reserva, recurso, protocolo u otro objeto |
| valuation_unit | Unidad económica valorada |
| valuation_currency | Moneda de expresión |
| valuation_timestamp_utc | Fecha de corte |
| horizon | Horizonte y fundamento |
| taxonomy_version | Versión de tipología |
| applicability_status | APPLICABLE / CONDITIONAL / NOT_APPLICABLE / INSUFFICIENT_EVIDENCE |
| method_family | Familia del catálogo |
| intended_use | Investigación, comparación, screening o decisión informativa |

## 3. Puente de captura de valor

El modelo debe demostrar la cadena económica desde actividad/activos del protocolo hasta el derecho que posee el titular. Registrar:
- quién genera el flujo o posee el activo;
- quién tiene derecho legal/contractual/on-chain a recibirlo;
- condiciones, prioridades, discrecionalidad y restricciones;
- mecanismo de distribución/redención/compra/quema, si existe;
- costes, contraparte, dilución y riesgos;
- evidencia primaria y fecha de vigencia.

Si no existe vínculo demostrable entre protocolo y token, no atribuir al token tesorería, ingresos, TVL ni comisiones.

## 4. Especificación matemática

Cada modelo documenta:
- ecuaciones y orden de operaciones;
- definición, unidad y dominio de cada variable;
- inputs observados, derivados, estimados, calibrados, supuestos y desconocidos;
- fuentes, timestamps y disponibilidad point-in-time;
- parámetros y justificación;
- tratamiento de faltantes, redondeo, precisión y datos negativos/cero;
- escenarios, dependencias y límites de extrapolación;
- pruebas de consistencia y casos donde no aplica.

No introducir constantes ocultas ni parámetros por defecto sin trazabilidad.

## 5. Salidas

Como mínimo:
- estado de aplicabilidad y estado de evidencia;
- objeto y unidad valorados;
- rango de estimación, si es defendible;
- tipo de rango (estadístico, incertidumbre del modelo, escenarios o consenso);
- estimación central solo si tiene justificación;
- escenarios y sensibilidad;
- contribución atribuible al token y mecanismo demostrado;
- variables desconocidas y cobertura;
- riesgos y contraevidencia;
- motivos de abstención;
- versión del modelo, dataset, código y parámetros.

No producir valor puntual si solo existe un rango defendible. No presentar el rango como intervalo de confianza si no tiene cobertura estadística calibrada.

## 6. Parámetros y evidencia

Etiquetar cada parámetro:
- OBSERVED: medido directamente con fuente y timestamp.
- DERIVED: calculado a partir de inputs identificados.
- ESTIMATED: estimado con método documentado.
- CALIBRATED: calibrado en datos de entrenamiento identificados.
- ASSUMPTION: supuesto explícito no verificado.
- UNKNOWN: desconocido o no disponible.

Un parámetro desconocido no puede convertirse silenciosamente en cero. Un supuesto material requiere sensibilidad y contraescenario.

## 7. Prevención de doble conteo

Dibujar un mapa de flujos/activos y marcar qué componentes se incluyen en cada método. No sumar NAV, flujos, tesorería, valor de red o comparables si representan la misma fuente económica. Para token y protocolo, reportar valores separados salvo que exista puente demostrable.

## 8. Validación y versionado

Conservar protocolo, fórmula, código/commit, parámetros, dataset y hashes, universo, timestamp, particiones temporales, benchmarks, costes y resultados. La calibración y prueba deben ser temporalmente independientes. Cada cambio de definición, input o parámetro crea nueva versión; no sobrescribir evaluaciones anteriores.

## 9. Abstención

Usar NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE o NO_APPLICABLE_METHOD cuando falten derechos, datos esenciales, comparabilidad o método defendible. Explicar qué falta, qué parte de la conclusión se bloquea y qué evidencia permitiría reanudar.
