# Política de escenarios, sensibilidad e incertidumbre de valoración

**Estado:** Propuesta metodológica; pendiente de validación. **Versión:** 0.1.0

## 1. Propósito

Evitar que una estimación dependa de un único supuesto oculto o de un punto central no defendible. La incertidumbre debe exponerse y no reducirse a una probabilidad inventada.

## 2. Tipos de escenario

- BASELINE: conjunto de inputs observados y supuestos centrales documentados; no implica escenario más probable.
- ADVERSE: condiciones desfavorables plausibles sustentadas por riesgos o evidencia.
- FAVOURABLE: condiciones favorables plausibles con fundamento observable.
- STRESS: evento o combinación severa relevante para la estructura del activo.
- ALTERNATIVE_MODEL: resultado de otro método aplicable con objeto comparable.

No asignar probabilidades a estos escenarios sin calibración histórica. No forzar los tres escenarios si la evidencia no permite construirlos.

## 3. Sensibilidad

Identificar variables que más afectan el resultado y variar solo dentro de rangos defendibles. Para cada variable documentar:
- valor/base y unidad;
- origen y clasificación (observado, derivado, estimado, calibrado, supuesto, desconocido);
- intervalo y fundamento;
- relación con otras variables;
- efecto sobre el rango de valor;
- umbrales de discontinuidad o cambio de aplicabilidad, si existen.

No variar inputs independientemente cuando exista dependencia económica. Evitar escenarios imposibles o combinaciones incoherentes. No elegir amplitudes por conveniencia.

## 4. Incertidumbre y forma de salida

Distinguir:
- incertidumbre de datos;
- incertidumbre de parámetros;
- incertidumbre estructural del modelo;
- incertidumbre de escenarios;
- incertidumbre jurídica/de derechos;
- incertidumbre de liquidez y ejecución.

Si no existe distribución calibrada, reportar rangos de escenarios o incertidumbre cualitativa, no intervalos probabilísticos. Si existe distribución, documentar muestra, método, supuestos, cobertura y validación.

## 5. Métodos alternativos y desacuerdo

Aplicar métodos alternativos solo cuando sean realmente aplicables al mismo objeto y unidad. Mostrar cada rango y por qué difieren. No promediar rangos heterogéneos sin una metodología que justifique ponderación y dependencia. El desacuerdo material puede exigir abstención o conclusión condicionada.

## 6. Doble conteo

Mantener mapa de componentes y flujos. Identificar qué ingresos, reservas, fees, colateral o demanda se incluyen en cada método. No sumar dos métodos que valoran el mismo componente. Para token y protocolo, mostrar resultados separados hasta demostrar atribución.

## 7. Cambio de régimen y extrapolación

Señalar cuando los datos históricos no representan condiciones actuales, cuando la red cambió tokenomics/consenso, cuando el marco regulatorio cambió o cuando el modelo extrapola fuera de su muestra. No extrapolar parámetros calibrados a otra tipología sin validación.

## 8. Criterios de abstención

Abstenerse o limitar la salida si:
- los derechos o la atribución al token no se verifican;
- inputs críticos son UNKNOWN o no admisibles;
- el rango cambia materialmente bajo supuestos igualmente plausibles y no hay base para elegir;
- métodos aplicables discrepan sin explicación;
- los escenarios dependen de datos futuros o leakage;
- existe doble conteo no resuelto;
- el modelo está fuera de dominio.

## 9. Registro

Guardar escenario, inputs, versiones, rangos, sensibilidad, dependencias, supuestos, limitaciones, responsable, timestamp UTC y motivo de abstención. Las actualizaciones crean una nueva evaluación versionada.
