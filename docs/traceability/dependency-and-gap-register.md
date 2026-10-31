# Registro de dependencias y brechas IIM

## 1. Grafo de dependencias

Las relaciones siguientes representan dependencias lógicas para construir una evaluación defendible; no significan que un requisito esté implementado.

| Bloque | Requisitos | Depende principalmente de | Habilita |
|---|---|---|---|
| Identidad y universo | IIM-001, 011, 019, 020 | Resolución de identidad y clasificación | Selección de instrumentos correctos y universo sin duplicados |
| Datos y evidencia | IIM-002, 005, 018 | Identidad, fuentes/licencias y timestamps | Evidencia admisible, hipótesis trazables y reconstrucción point-in-time |
| Valoración | IIM-004, 012, 013, 021 | Taxonomía, identidad, datos y derechos económicos | Rangos defendibles o abstención |
| Infravaloración | IIM-003, 014, 015 | Valoración, precio ejecutable, datos y costes | Clasificación de brecha; categoría extrema solo tras calibración |
| Riesgo | IIM-006, 016, 017 | Evidencia, identidad, tipología y alcance | Gates/vetos independientes |
| Decisión | IIM-022 | Universo comparable, valoración, infravaloración, riesgos y horizonte | Decisión informativa y ranking parcial |
| Reporting e historial | IIM-008, 010, 023 | Evidencia, resultados y contratos | Expediente auditable y comparación longitudinal |
| Orquestación | IIM-009, 024 | Solicitud explícita, contratos, gates y persistencia | Ejecución controlada, recuperable y observable |
| Validación | IIM-007, 015 | Datos históricos point-in-time, universo y protocolo congelado | Evidencia empírica fuera de muestra |

## 2. Dependencias detalladas

- IIM-001 es prerrequisito de IIM-019 e IIM-020; identidad ambigua bloquea valoración y ranking.
- IIM-002 e IIM-018 son prerequisitos transversales para afirmaciones que consuman datos.
- IIM-011 e IIM-012 determinan qué modelos de IIM-004/IIM-021 pueden aplicarse.
- IIM-013 gobierna la abstención cuando la valoración no es defendible.
- IIM-014 depende de valoración comparable, precio de referencia ejecutable y costes; IIM-015 depende además de IIM-007.
- IIM-016 e IIM-017 deben ejecutarse sin compensación por resultados favorables de valoración.
- IIM-022 consume gates, vetos, resultados por horizonte y comparabilidad.
- IIM-010 e IIM-023 requieren evidencia y resultados enlazados; IIM-008 conserva la evolución entre snapshots.
- IIM-009 e IIM-024 gobiernan cuándo y cómo ejecutar; no sustituyen los controles analíticos.

## 3. Brechas verificadas en el repositorio

| ID de brecha | Brecha | Evidencia observada | Consecuencia | Acción recomendada |
|---|---|---|---|---|
| GAP-01 | No se evidencia implementación ejecutable en este repositorio | Árbol `main` revisado: documentación Markdown y registro de requisitos | No puede afirmarse que el sistema ya ejecute selección, valoración o clasificación | Definir repositorio/componente de implementación y contratos de integración antes de codificar |
| GAP-02 | Borradores metodológicos pendientes de aprobación | Los documentos mantienen estados «Propuesta», «pendiente de revisión» o «pendiente de implementación/validación» | Los criterios no deben tratarse como reglas metodológicas aprobadas | Ejecutar revisión por requisito mediante [Issue #19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19) y registrar decisiones/versiones aprobadas |
| GAP-03 | Casos de prueba documentales sin ejecución demostrada | Archivos `*-test-cases.md`; no se identificaron resultados reproducibles de suites ejecutadas en este repositorio | No se puede declarar TESTED | Convertir casos en tests ejecutables cuando exista implementación; conservar logs y commit |
| GAP-04 | Validación empírica no demostrada | Protocolo de calibración definido como diseño pendiente de ejecución | No hay base para declarar eficacia predictiva ni activar categoría extrema | Preparar dataset point-in-time, protocolo congelado, benchmarks/costes y walk-forward/OOS |
| GAP-05 | Disponibilidad real y permisos de fuentes no demostrados por la documentación sola | Registro de fuentes es documental y contiene estados/condiciones por verificar | Riesgo de planificar funciones sobre fuentes no accesibles o no licenciadas | Verificar cada fuente, licencia, cobertura, endpoint y uso antes de habilitarla |
| GAP-06 | Estado del registro de requisitos desactualizado | El registro anterior marcaba todos los requisitos como «Propuesto» sin diferenciar evidencia documental | No representa el avance documental ni las brechas reales | Sustituir estados genéricos por estados multidimensionales y enlaces de evidencia |
| GAP-07 | No existe aún un proceso formal de aprobación de especificaciones en el registro | PRs fusionados, pero sin evidencia de aprobación metodológica por requisito | La integración Git puede confundirse con aprobación | Aplicar [la plantilla de revisión y aprobación](specification-review-record-template.md), registrar revisor, decisión, versión, fecha y desviaciones |
| GAP-08 | Contradicciones heredadas entre contratos fundacionales | Las inconsistencias de `chain_id`, identificador nativo, IDs tipados y timestamps detectadas en la revisión XCR-01 a XCR-05 fueron armonizadas documentalmente en PR #23; no se ha demostrado aún su implementación correcta | La corrección documental reduce contradicciones, pero no acredita interoperabilidad ni validación con datos reales | Mantener el hallazgo como antecedente trazable; verificar los contratos en adaptadores y casos ejecutables antes de cerrar el riesgo |
| GAP-09 | Perfiles por familia de red incompletos y no validados | Se documentan perfiles EVM/Solana y Cosmos SDK/IBC + UTXO Bitcoin-like v0.1.0 y casos AS-033–AS-050; otras familias y protocolos especializados siguen sin perfil, y no hay adaptadores ni fixtures ejecutados | La regla genérica podría implementarse de forma inconsistente o aceptar falsos positivos de identidad | Completar perfiles de otras familias y protocolos especializados, revisión técnica independiente y suites de fixtures; mantener estados no resueltos cuando la evidencia no distinga redes o activos |

## 4. Revisión fundacional en curso

La auditoría cruzada del PR #23 y el contrato conceptual de cardinalidades del PR #24 están integrados en `main`. Las iteraciones incorporan perfiles documentales EVM/Solana y Cosmos SDK/IBC + UTXO Bitcoin-like v0.1.0, con casos AS-033 a AS-050. La ampliación reduce ambigüedades, pero no aprueba requisitos, adaptadores ni fuentes. Ningún requisito se eleva a `SPECIFIED` por la integración de cambios documentales.

Los hallazgos de IIM-001, IIM-002, IIM-018, IIM-019 e IIM-020 están registrados en [issue-19-foundational-review-findings](issue-19-foundational-review-findings.md) y [issue-19-cross-document-review](issue-19-cross-document-review.md). Las decisiones siguen siendo REVISION_REQUIRED o BLOCKED; no se ha aprobado ningún requisito ni fuente concreta. Persisten brechas de perfiles para otras redes y protocolos especializados, adaptadores y pruebas interoperables ejecutadas, cobertura histórica, controles de calidad y expedientes probatorios de fuentes.

## 5. Paquetes de trabajo futuros sugeridos

No se crean Issues de implementación en este cambio: primero debe determinarse qué repositorio albergará cada componente y aprobarse el alcance de sus contratos. Para evitar duplicados, estos paquetes se convierten en Issues separados solo tras revisar los repositorios de software y sus Issues existentes.

1. **Núcleo de identidad, universo y datos:** IIM-001, IIM-002, IIM-018, IIM-019, IIM-020. Revisión metodológica en curso: [Issue #19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19).
2. **Núcleo de tipología, valoración e infravaloración:** IIM-003, IIM-004, IIM-011, IIM-012, IIM-013, IIM-014, IIM-021.
3. **Riesgo, decisión y ranking:** IIM-005, IIM-006, IIM-016, IIM-017, IIM-022.
4. **Validación temporal y categoría extrema:** IIM-007, IIM-015.
5. **Reporting, historial y orquestación:** IIM-008, IIM-009, IIM-010, IIM-023, IIM-024.

## 6. Regla para abrir nuevas brechas

Antes de crear un Issue de implementación:

1. revisar Issues abiertos/cerrados y PRs del repositorio de implementación elegido;
2. comprobar si el contrato ya existe y su versión;
3. definir un único resultado verificable y criterios de aceptación;
4. separar implementación, pruebas de software y validación empírica;
5. declarar dependencias y datos/licencias necesarios;
6. evitar Issues paraguas que mezclen módulos no desplegables ni verificables de forma independiente.
