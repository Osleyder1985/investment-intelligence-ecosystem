# Evaluador local de calidad de observaciones — Android

**Estado:** implementación inicial del evaluador determinista; no existen políticas productivas aprobadas ni integración con una fuente real.

## Objetivo y alcance

Evaluar metadatos y condiciones de admisibilidad de una observación para un uso previsto concreto. El evaluador es independiente del evaluador de frescura y no calcula una puntuación global de calidad o confianza.

La política se delimita por:
- identificador y versión de política;
- fuente;
- tipo de observación;
- uso previsto;
- versión esperada del esquema del valor;
- estados de uso permitido de la fuente admitidos;
- valores de calidad reconocidos;
- requisito explícito de evidencia verificada.

No se incorporan TTL, pesos, umbrales estadísticos ni políticas predeterminadas.

## Controles iniciales

1. **Completitud estructural:** identificadores y metadatos de uso obligatorios no vacíos.
2. **Consistencia temporal:** timestamps no negativos, recuperación no posterior al instante de evaluación y observación no posterior a recuperación.
3. **Contrato del valor:** versión de esquema esperada y contenido normalizado no vacío.
4. **Admisibilidad declarada:** el estado de uso permitido de la fuente debe estar expresamente incluido en la política.
5. **Estado de evidencia:** valores de calidad desconocidos bloquean. UNVERIFIED produce una limitación o bloqueo según la política.
6. **Integridad declarada:** si se proporciona payloadSha256, debe tener formato hexadecimal SHA-256 de 64 caracteres.
7. **Referencia de licencia:** una referencia presente pero vacía genera advertencia. Este control no demuestra que una licencia sea válida ni que permita el uso.

Los estados de uso son comparados literalmente con la lista de la política. Por tanto, un origen INTERNAL_DEMO_ONLY no supera una política de análisis de mercado que no lo incluya expresamente.

## Resultados

- BLOCKED: al menos un hallazgo bloqueante.
- PASS_WITH_LIMITATIONS: no hay hallazgos bloqueantes, pero sí advertencias.
- PASS: todos los controles configurados pasan.
- NotEvaluated(NO_MATCHING_POLICY): no existe política para la combinación fuente/tipo/uso.

PASS solo significa que pasaron los controles implementados y configurados. No significa que el dato sea verdadero, que el proveedor sea confiable, que la licencia esté jurídicamente validada ni que el activo sea elegible para inversión.

## Limitaciones conocidas

- No se parsea valueJson; solo se comprueba que no esté vacío. La validación semántica depende de validadores específicos por tipo y versión de payload.
- No se verifica criptográficamente el hash contra los bytes originales; solo se comprueba su formato.
- No se consulta ni interpreta el texto de licencias o términos del proveedor.
- No se infiere que UNVERIFIED sea falso; significa que la evidencia no está verificada.
- No se calcula una confianza agregada ni se mezclan calidad, frescura, riesgo y elegibilidad.
- No hay políticas productivas configuradas. Las políticas de tests son fixtures sintéticos.
- El evaluador todavía no está conectado a Room, a la interfaz ni a un proceso de ingesta real.

## Verificación

Los tests unitarios cubren pase, fuente demo no admisible para análisis de mercado, evidencia no verificada, requisito de verificación, estado de calidad desconocido, esquema inesperado, payload vacío, orden temporal inválido, formato de hash, ausencia de política y duplicidad de ámbito. Consultar CI antes de declarar ejecución satisfactoria.
