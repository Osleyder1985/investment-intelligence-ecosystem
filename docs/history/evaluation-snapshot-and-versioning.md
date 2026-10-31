# Snapshots de evaluación y política de versionado

**Estado:** Propuesta metodológica; pendiente de implementación. **Versión:** 0.1.0

## 1. Principio de inmutabilidad

Cada evaluación completada es un snapshot inmutable identificado por evaluation_id. Corregir un dato, recalcular un modelo o modificar una política crea una nueva evaluación enlazada mediante supersedes_evaluation_id o relación equivalente. No se actualiza el contenido histórico en sitio.

Los estados de ejecución fallida o parcial también deben conservarse cuando sean relevantes para auditoría; distinguirlos de una evaluación completa.

## 2. Contenido mínimo del snapshot

Conservar referencias/versiones y hashes de:
- identidad canónica y representación;
- alcance, finalidad, mercado, par, jurisdicción, tamaño de operación y horizonte;
- snapshot del universo y reglas de elegibilidad;
- datos originales/admisibles y manifiesto de fuentes;
- event/publication/availability/capture/processing timestamps disponibles;
- transformaciones, consultas, código, commit y dependencias;
- políticas de evidencia, riesgo, veto, valoración, infravaloración y decisión;
- modelos, parámetros, escenarios y supuestos;
- resultados de gates, riesgos, valoración, clasificación y ranking;
- evidencias, contraevidencias, faltantes y abstenciones;
- versión del esquema y formato de informe;
- autor/proceso y motivo de ejecución.

Guardar hashes no sustituye la conservación legalmente permitida de los datos ni demuestra por sí solo autenticidad de una fuente. Registrar también licencias, retención permitida y restricciones de redistribución.

## 3. Versiones separadas

Distinguir:
- schema_version;
- methodology_version;
- policy_versions;
- taxonomy_version;
- valuation_model_versions;
- code_version/commit;
- source_adapter_versions;
- dataset/data_manifest versions;
- universe_snapshot_id;
- parameter_set_version.

Un cambio en cualquiera de estas capas debe poder identificarse. No usar un único número de versión para ocultar qué componente cambió.

## 4. Estados del expediente

Como mínimo:
- REQUESTED: usuario solicitó ejecución;
- IN_PROGRESS: proceso iniciado;
- COMPLETED: expediente completo según criterios aplicables;
- COMPLETED_WITH_LIMITATIONS: completado con limitaciones explícitas;
- PARTIAL: ejecución parcial;
- FAILED: ejecución fallida;
- ABSTAINED: no se emite conclusión por falta de base suficiente.

El estado de ejecución es independiente del estado de decisión. Una ejecución COMPLETED puede concluir INSUFFICIENT_EVIDENCE.

## 5. Reproducibilidad y retención

La reproducción debe identificar versión de código, datos disponibles, parámetros, dependencias, entorno relevante y manifiestos. Si una fuente ya no puede redistribuirse o conservarse, registrar referencia/hash y limitación de reproducción, respetando sus condiciones.

No inventar límites de retención. La política concreta debe derivarse de licencias, requisitos legales, seguridad y gobernanza del proyecto, documentando cualquier excepción.

## 6. Integridad y acceso

Los snapshots deben ser append-only desde la perspectiva de la aplicación, con controles de integridad, permisos y registro de cambios. La corrección se realiza mediante nuevo snapshot y relación explícita. El borrado o anonimización legalmente exigible debe dejar constancia del alcance permitido sin conservar datos cuya retención esté prohibida.

## 7. Relación entre evaluaciones

Una nueva evaluación puede enlazar con la anterior como sucesora, comparación o reejecución. Registrar motivo: nuevo timestamp de mercado, nueva fuente, corrección, cambio de cobertura, cambio de modelo, cambio de política, cambio de identidad, nuevo horizonte o solicitud del usuario.

No inferir que una nueva evaluación es comparable solo porque corresponde al mismo ticker.
