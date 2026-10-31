# Workflow integral de evaluación bajo demanda

**Estado:** Propuesta metodológica; pendiente de implementación. **Versión:** 0.1.0

## 1. Principio de ejecución

Una evaluación solo comienza por solicitud explícita del usuario o acción explícita dentro de la aplicación. No existen ejecuciones periódicas implícitas. La solicitud define activo/representación, finalidad, horizonte, alcance, mercado/jurisdicción cuando aplique y cualquier parámetro material.

El workflow coordina tareas; no altera las reglas de decisión. El estado técnico de una ejecución es independiente del estado de inversión que produzca.

## 2. Etapas y dependencias

| Orden lógico | Etapa | Precondiciones | Salida y condición de avance |
|---|---|---|---|
| 1 | Validar solicitud | Solicitud explícita e identificable | Request validado o rechazo accionable |
| 2 | Resolver identidad | Datos mínimos del activo/representación | Identidad RESOLVED o bloqueo/abstención |
| 3 | Congelar contexto | Identidad y alcance; universo si la finalidad lo requiere | Request snapshot, universe snapshot y versiones fijadas |
| 4 | Planificar fuentes | Contexto congelado, licencias/usos revisados | Plan de captura y fuentes admisibles |
| 5 | Recopilar datos | Plan de fuentes | Resultados por fuente, cobertura y errores; paralelizar solo tareas independientes |
| 6 | Validar y admitir datos | Capturas con linaje y timestamps | Dataset/manifiesto admitido, cuarentenas y faltantes |
| 7 | Screening y elegibilidad | Identidad y datos críticos | Elegibilidad por finalidad y motivos |
| 8 | Evaluar riesgos/vetos | Evidencia admisible de riesgos | Riesgos, controles, riesgo residual y estados de veto |
| 9 | Valorar | Método aplicable y inputs requeridos | Rangos/escenarios o abstención por método |
| 10 | Clasificar infravaloración | Precio de referencia comparable y valoración defendible | Estado de brecha, incertidumbre y costes |
| 11 | Decidir/comparar | Gates, vetos, riesgos y resultados anteriores | Estado por horizonte y ranking parcial cuando proceda |
| 12 | Generar informe | Expediente y referencias consistentes | Informe humano y estructura validada |
| 13 | Persistir snapshot | Validaciones de integridad superadas | Expediente inmutable y referencias de reproducción |

Las etapas 5 pueden ejecutarse en paralelo por fuente si no existe dependencia, limitación contractual o conflicto de recursos. Las etapas posteriores solo consumen outputs explícitos y versionados; nunca leen datos cambiantes sin snapshot.

## 3. Barreras de avance

- Identidad UNRESOLVED/CONFLICTED: no ejecutar valoración ni ranking del instrumento ambiguo.
- Fuente o dato crítico REJECTED/QUARANTINED/UNKNOWN: bloquear conclusiones dependientes; permitir solo salida parcial que identifique el faltante.
- Restricción de licencia: no capturar, almacenar, transformar ni redistribuir fuera del uso permitido.
- VETO_ACTIVE: bloquear estado favorable dentro del alcance; las demás etapas solo continúan si aportan información permitida y necesaria para el propósito solicitado.
- VETO_REVIEW_REQUIRED o RISK_UNKNOWN material: impedir conclusión favorable confirmada y reflejar revisión/incompletitud.
- Valoración NOT_APPLICABLE/INSUFFICIENT_EVIDENCE: no generar brecha cuantificada.
- Fallo de validación del expediente: no publicar como informe completo.

No confundir una barrera analítica con fallo técnico. El workflow puede terminar correctamente con una conclusión ABSTAINED o bloqueada.

## 4. Paralelismo

Se permite paralelismo entre adaptadores de fuentes independientes, verificaciones de mercado sin dependencias entre sí y cálculos que consuman el mismo snapshot inmutable. No se permite:
- mezclar datos de snapshots diferentes;
- iniciar valoración antes de confirmar inputs y unidades;
- calcular ranking antes de aplicar gates y vetos;
- generar informe antes de validar referencias y estados;
- persistir un snapshot COMPLETED antes de cerrar las etapas requeridas.

## 5. Fallos y resultado final

Cada etapa declara COMPLETED, COMPLETED_WITH_LIMITATIONS, SKIPPED_NOT_APPLICABLE, BLOCKED, FAILED o CANCELLED. La ejecución final se deriva de las etapas:
- COMPLETED: todas las etapas requeridas completadas sin limitación material pendiente;
- COMPLETED_WITH_LIMITATIONS: evaluación válida dentro de un alcance reducido, con faltantes/limitaciones visibles;
- PARTIAL: etapas relevantes incompletas o fuentes críticas sin resolver; no presentar como evaluación completa;
- FAILED: fallo técnico que impide producir expediente íntegro;
- CANCELLED: cancelación confirmada;
- ABSTAINED: no se emite conclusión por gates/evidencia/método insuficientes; puede existir informe de abstención.

La decisión analítica (por ejemplo, INSUFFICIENT_EVIDENCE) no reemplaza el estado de ejecución.

## 6. Reanudación

Una reanudación utiliza el execution_id original, verifica integridad de snapshots y etapas ya completadas, y solo repite etapas idempotentes con inputs idénticos. Si cambió cualquier dato, fuente, política, modelo o parámetro, se crea una nueva evaluación vinculada; no se modifica el snapshot previo.

## 7. Registro por etapa

Registrar stage_id, execution_id, nombre/versión, estado, precondiciones, inputs y hashes, outputs y hashes, timestamps, fuente/adaptador, código/commit, errores normalizados, reintentos y motivo de skip/bloqueo. No registrar secretos, claves privadas ni credenciales.
