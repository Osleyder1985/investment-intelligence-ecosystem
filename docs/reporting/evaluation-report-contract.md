# Contrato del informe integral de evaluación

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Propósito

Definir una salida única, legible y auditable para cada evaluación de un criptoactivo. El informe es una representación de un expediente estructurado e inmutable; no debe contener conclusiones que no existan en ese expediente.

El mismo contrato debe poder alimentar interfaz web, desktop, Android, iOS y exportaciones sin alterar el significado de los datos.

## 2. Identidad y alcance obligatorios

Cada informe identifica:
- evaluation_id único e inmutable;
- canonical_asset_id y nombre/ticker únicamente como etiquetas;
- representación concreta: activo nativo, token, wrapper, bridge u otra;
- red, chain_id y contrato cuando sean aplicables;
- mercado, exchange/venue, par y moneda de cotización;
- finalidad: research, valuation, screening, comparison u operational assessment;
- jurisdicción y restricciones relevantes, si aplican;
- tamaño de operación evaluado cuando la conclusión incluya ejecución;
- timestamp de corte UTC y timestamps de generación;
- horizonte y definición temporal utilizada;
- snapshot del universo y versiones de metodología/políticas.

Si un campo no aplica, declararlo NOT_APPLICABLE con motivo. Si se desconoce, usar UNKNOWN; no dejar que un consumidor lo interprete como cero o valor por defecto.

## 3. Estructura mínima del informe

### 3.1 Resumen ejecutivo
- conclusión informativa y estado de decisión;
- horizonte al que se refiere;
- principales fundamentos a favor y en contra;
- gates/vetos que bloquean o condicionan;
- grado y tipo de incertidumbre, sin inventar puntuaciones;
- datos críticos faltantes;
- advertencia: no es orden de compra/venta ni garantía de rentabilidad.

### 3.2 Identidad y alcance
Identidad canónica, representación, red/contrato, mercado, par, finalidad, jurisdicción, tamaño de operación, timestamp, horizonte y snapshot del universo.

### 3.3 Gates, elegibilidad y vetos
Mostrar todos los estados, alcance, evidencia, vigencia y razón. VETO_ACTIVE, VETO_REVIEW_REQUIRED y RISK_UNKNOWN deben ser visibles antes de cualquier ranking o etiqueta favorable.

### 3.4 Calidad y cobertura de evidencia
Resumen de fuentes, licencias/usos permitidos, cobertura temporal, disponibilidad point-in-time, conflictos, faltantes, cuarentenas, transformaciones y limitaciones.

### 3.5 Valoración e infravaloración
Para cada método: objeto valorado, aplicabilidad, derechos/captura de valor, rango, unidad/moneda, timestamp, inputs, supuestos, sensibilidad, escenarios, incertidumbre, doble conteo y abstención. Separar valor del protocolo y valor atribuible al token.

La clasificación de infravaloración debe referirse al precio/mercado/fecha comparados y al rango de valor. «Extremadamente infravalorado» solo puede mostrarse si el protocolo correspondiente está calibrado y validado; de lo contrario, estado no habilitado.

### 3.6 Riesgos y restricciones
Riesgos inherentes, controles, riesgo residual, desconocidos, vetos, riesgos de contrato/consenso/puente/custodia/contraparte, tokenomics, concentración, unlocks, liquidez, manipulación, regulación, jurisdicción y operación, según aplicabilidad.

### 3.7 Mercado, liquidez y ejecución
Precio de referencia y tipo de precio, spread, profundidad, volumen admisible, slippage, impacto, comisiones, límites y restricciones de acceso. Distinguir valoración teórica de precio ejecutable.

### 3.8 Catalizadores, eventos e hipótesis
Separar hechos confirmados, anuncios, rumores, hipótesis de filtración y causalidad no establecida. Para cada evento registrar event time, publication time, availability/knowledge time, capture time y fuentes. No inferir causalidad solo por precedencia temporal.

### 3.9 Escenarios, hipótesis y contraevidencia
Incluir alternativas, evidencia favorable, evidencia contraria, variables desconocidas, dependencias y condiciones que cambiarían la conclusión. No asignar probabilidades no calibradas.

### 3.10 Comparación y ranking
Mostrar universo, horizonte, compatibilidad, dimensiones comparadas, relaciones de dominancia, grupos y activos no comparables. No ocultar gates/vetos en el ranking.

### 3.11 Historial y cambios
Enlazar evaluaciones previas, versiones, cambios de datos/fuentes/cobertura, metodología, modelos/parámetros, mercado y estado del activo. Distinguir cambios observados del activo de cambios metodológicos o de medición.

## 4. Semántica de valores y campos ausentes

Toda variable cuantitativa debe incluir valor, unidad, moneda si aplica, timestamp, estado de evidencia y fuente/linaje. No usar cero como sustituto de UNKNOWN, NOT_APPLICABLE, NOT_COLLECTED, REDACTED o REJECTED.

Los campos opcionales no deben desaparecer de forma ambigua cuando su ausencia sea material. Usar un estado explícito y una explicación. Los números deben ser finitos; prohibidos NaN e Infinity en el contrato exportable.

## 5. Trazabilidad de afirmaciones

Cada afirmación material del resumen o conclusión debe enlazar con uno o más evidence_id o calculation_id. Cada evidencia debe conservar fuente, referencia, fecha, disponibilidad, captura, admisibilidad, licencia/uso, hash cuando proceda y limitaciones.

Las afirmaciones interpretativas deben distinguirse de hechos observados. La ausencia de evidencia se presenta como limitación, no como evidencia negativa.

## 6. Comparación entre horizontes

Corto, medio y largo plazo son evaluaciones separadas. El informe puede presentarlas juntas, pero no compartir automáticamente valoración, riesgo, catalizadores, decisión o ranking. Declarar diferencias y sus causas documentadas.

## 7. Exportación y compatibilidad

La representación humana y la estructurada deben derivarse del mismo objeto de evaluación versionado. La exportación debe conservar IDs, estados, unidades, timestamps, enlaces de evidencia y versiones. La interfaz no puede transformar UNKNOWN en cero, ocultar vetos ni reordenar campos de forma que cambie el significado.

## 8. Control de generación

El informe se genera solo después de una solicitud explícita del usuario o de una acción explícita dentro del producto. La generación del informe no autoriza ejecución de operaciones ni activa evaluaciones futuras automáticas.

## 9. Advertencia de uso

La conclusión es una evaluación informativa condicionada por evidencia, supuestos, alcance y timestamp. No es asesoramiento financiero personalizado, promesa de retorno ni instrucción de compra/venta.
