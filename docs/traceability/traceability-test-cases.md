# Casos de revisión de trazabilidad IIM

Estos son casos de revisión documental y de gobernanza. No son pruebas automatizadas del producto ni resultados de validación empírica.

| ID | Caso | Procedimiento | Resultado esperado |
|---|---|---|---|
| TR-001 | Cobertura completa | Comparar el registro con la matriz | Cada ID IIM-001 a IIM-024 aparece exactamente una vez en el registro y una vez en la matriz |
| TR-002 | Requisito sin artefacto | Retirar o invalidar temporalmente una ruta en una copia de revisión | El requisito pasa a NOT_DOCUMENTED o queda marcado como enlace roto; no conserva DOCUMENTED |
| TR-003 | PR fusionado sin aprobación | Revisar un requisito con PR integrado y documento en estado propuesta | No elevar a SPECIFIED por el merge |
| TR-004 | Casos documentales no ejecutados | Revisar referencias a archivos `*-test-cases.md` sin logs/resultados | No elevar a TESTED |
| TR-005 | Código sin prueba | Identificar implementación sin ejecución reproducible | Puede marcar IMPLEMENTED; no TESTED |
| TR-006 | Test unitario sin OOS | Revisar un modelo con pruebas unitarias pero sin validación temporal | No elevar a EMPIRICALLY_VALIDATED |
| TR-007 | Dependencia faltante | Revisar IIM-014 sin evidencia de valoración/precio comparable | Registrar bloqueo; no declarar la brecha de valoración completada |
| TR-008 | Veto compensado | Revisar una salida favorable con VETO_ACTIVE | Rechazar la clasificación favorable en el alcance del veto |
| TR-009 | Datos futuros | Revisar una evaluación histórica con publication/availability time posterior al timestamp | Detectar leakage y bloquear su uso como evidencia point-in-time |
| TR-010 | Fuente no autorizada | Revisar un proveedor sin uso/licencia confirmados | Estado pendiente/cuarentena; no habilitar captura/retención por suposición |
| TR-011 | Categoría extrema | Revisar salida EXTREME_UNDERVALUATION sin calibración OOS aprobada | Rechazar; mantener NOT_CALIBRATED |
| TR-012 | Cambio de metodología | Cambiar un modelo/política después de una evaluación | Crear nueva versión/snapshot; no sobrescribir evidencia histórica |
| TR-013 | Estado desconocido | Campo crítico ausente | Conservar UNKNOWN/NOT_EVIDENCED; no convertir a cero ni PASS |
| TR-014 | Ranking no comparable | Comparar activos con horizontes, monedas, unidades o finalidades incompatibles | Marcar no comparables o separar grupos; no forzar orden total |
| TR-015 | Duplicación de activos | Comparar wrapper y subyacente | Evitar contar doble exposición/valor; exigir identidad y relación explícitas |
| TR-016 | PR/commit incorrecto | Seguir el enlace de PR y verificar merge SHA | La matriz solo conserva evidencia si el PR/commit existe y corresponde al artefacto |
| TR-017 | Ruta rota | Abrir cada enlace relativo de la matriz en la rama | Todos los enlaces internos resuelven a un archivo existente |
| TR-018 | Estado inflado | Intentar marcar un requisito como IMPLEMENTED/TESTED sin evidencia | La revisión lo rechaza y registra la brecha |
| TR-019 | Aprobación trazable | Revisar una especificación aprobada | Debe existir versión, decisión, fecha, revisor y cambios requeridos resueltos |
| TR-020 | Alcance de validación | Revisar una afirmación empíricamente validada | Debe identificar tipología, universo, horizonte, régimen, datos, protocolo y limitaciones |

## Resultado de esta versión

La ejecución de estos casos no se declara realizada por su mera documentación. La revisión manual de esta entrega comprueba cobertura de IDs, existencia de rutas principales, correspondencia de PR/merge commits conocidos y consistencia de estados declarados. Los casos TR-001 a TR-020 permanecen como criterios para verificaciones futuras más amplias.
