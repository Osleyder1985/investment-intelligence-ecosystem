# Evaluación de evidencias en el explorador Android

**Estado:** integración local inicial; limitada al fixture sintético del catálogo demo.

## Flujo implementado

La ficha del activo obtiene la observación más reciente `IDENTITY_FIXTURE` desde Room, consulta el registro local de la fuente y construye el modelo de dominio sin exponer el JSON del payload a la interfaz. El coordinador ejecuta tres dimensiones independientes:

- **Payload:** contrato `IDENTITY_FIXTURE` v1.
- **Calidad:** política `iie-local-demo-display-quality` v1, aplicable exclusivamente a `source:iie-demo-fixture` y `LOCAL_CATALOG_DISPLAY`.
- **Frescura:** no evaluada porque no existe una política coincidente.

La política de calidad acepta `INTERNAL_DEMO_ONLY` únicamente para la visualización local y conserva `UNVERIFIED` como limitación. No autoriza análisis de mercado.

## Límites

- La integración solo evalúa el último fixture local por activo.
- No hay ingesta de proveedores ni datos reales de mercado.
- No hay política de frescura para esta observación.
- La evaluación no se persiste como histórico; se calcula al abrir la ficha.
- No se genera puntuación agregada, aprobación de inversión ni recomendación.
- La validación estructural no demuestra veracidad del contenido.

## Verificación

La prueba unitaria del evaluador local comprueba payload válido, calidad con limitaciones, ausencia de evaluación de frescura y falta de política para uso de análisis de mercado. La integración Room/UI requiere CI y pruebas instrumentadas para considerarse validada.
