# Evaluador local de frescura — alcance inicial Android

**Estado:** Implementación de motor determinista; políticas productivas no configuradas ni aprobadas.

## Objetivo

Evaluar la vigencia de una observación únicamente cuando existe una política explícita que coincide con fuente, tipo de observación y finalidad. El motor no asigna TTL globales ni deduce la vigencia por ausencia de errores.

## Contrato de política

Cada política identifica:
- `policyId` y versión;
- `sourceId`;
- `observationType`;
- `intendedUse`;
- timestamp de referencia (`OBSERVED_AT` o `RETRIEVED_AT`);
- edad máxima;
- ventana de aviso de expiración.

Los umbrales son parámetros obligatorios de una política específica. Los valores utilizados en pruebas unitarias son datos sintéticos de prueba, no límites recomendados ni políticas aprobadas para fuentes reales.

## Resultados

- `FRESH`: dentro del límite y fuera de la ventana preventiva.
- `APPROACHING_EXPIRY`: todavía vigente, dentro de la ventana preventiva.
- `STALE`: supera la edad máxima.
- `NOT_APPLICABLE`: reservado para una futura regla positiva y explícita; el motor inicial no lo emite automáticamente.

La ausencia de política, timestamp requerido o una secuencia temporal imposible produce `NotEvaluated` con razón explícita. No se convierte en `FRESH`, `STALE` ni `NOT_APPLICABLE`.

## Límites deliberados

- No hay políticas productivas configuradas para precios, volumen, identidad, supply, métricas on-chain ni fundamentos.
- El fixture local `IDENTITY_FIXTURE` no recibe TTL: es una evidencia sintética de demostración, no una observación de mercado.
- La evaluación de frescura no equivale a calidad, exactitud, integridad, licencia o confiabilidad de proveedor.
- No se altera el modelo canónico global ni requisitos IIM.
- La integración con el flujo de ingesta y la visualización de evaluaciones persistidas quedan fuera de este incremento.

## Verificación

Los tests unitarios cubren ausencia de política, timestamp faltante, FRESH, APPROACHING_EXPIRY, STALE, orden temporal inválido y duplicidad de políticas. Los resultados de CI deben consultarse antes de afirmar que se ejecutaron correctamente.
