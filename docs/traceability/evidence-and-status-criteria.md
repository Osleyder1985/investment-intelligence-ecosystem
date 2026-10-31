# Criterios de evidencia y estados de cumplimiento IIM

## 1. Propósito

Establecer un vocabulario uniforme para informar el avance de cada requisito IIM sin confundir documentación, especificación aprobada, implementación, pruebas ejecutadas y validación empírica.

## 2. Estados y evidencia mínima

| Estado | Definición | Evidencia mínima requerida | No es suficiente |
|---|---|---|---|
| NOT_DOCUMENTED | No existe artefacto documental identificado | Revisión de rutas/registro y constancia de búsqueda | Una suposición de que el requisito está cubierto |
| DOCUMENTED | Existe contenido que trata el requisito | Archivo versionado, ruta y revisión de contenido | Un Issue que solo describe trabajo futuro |
| SPECIFICATION_DRAFTED | Existe borrador de política, contrato, algoritmo o procedimiento | Artefacto con alcance, entradas, salidas, reglas, límites y criterios; estado de borrador visible | Documento genérico, lista de deseos o casos sin regla operativa |
| SPECIFIED | Especificación revisada y aprobada conforme a la gobernanza del proyecto | Versión identificada, revisión registrada, decisiones resueltas, criterios verificables y aprobación trazable | PR fusionado por sí solo; documento que declara «pendiente» |
| IMPLEMENTED | Existe comportamiento ejecutable que satisface el alcance especificado | Código, commit, componente identificable, configuración/dependencias y vínculo requisito-componente | Diagramas, pseudocódigo, contratos, scripts no conectados o documentación |
| TESTED | La implementación ha superado pruebas reproducibles pertinentes | Suite, entorno/versión, comando, resultado, artefactos/logs y commit probado | Casos de prueba escritos, test no ejecutado, check ausente o CI no asociado |
| EMPIRICALLY_VALIDATED | La capacidad ha demostrado el comportamiento objetivo con evidencia empírica adecuada | Protocolo predefinido, datos admisibles, separación temporal, resultados fuera de muestra, benchmarks/costes, análisis de incertidumbre, sesgos y limitaciones | Pruebas unitarias, backtest in-sample, correlación anecdótica o métrica no calibrada |

## 3. Reglas de transición

1. Los estados son acumulativos, pero cada transición requiere su propia evidencia.
2. DOCUMENTED y SPECIFICATION_DRAFTED no implican aprobación.
3. SPECIFIED requiere revisión metodológica explícita y registro de decisiones. El merge de un Pull Request demuestra integración en Git, no aceptación científica ni conformidad externa.
4. IMPLEMENTED se asigna por requisito y componente, no por repositorio completo. Si solo parte del requisito está codificada, registrar cobertura parcial y componentes pendientes.
5. TESTED requiere resultados observables de ejecución. Un workflow ausente, skipped o sin ejecución no cuenta como prueba satisfactoria.
6. EMPIRICALLY_VALIDATED solo se asigna a una afirmación concreta, tipología, horizonte, universo, régimen y versión del protocolo para los que exista evidencia adecuada. No extrapolar a otros activos u horizontes.
7. Una modificación de código, datos, política, modelo o parámetros puede invalidar evidencia previa; registrar el alcance de la invalidación y repetir las pruebas pertinentes.
8. UNKNOWN, BLOCKED, NOT_APPLICABLE y NOT_EVIDENCED deben conservarse como estados explícitos con razón. No convertirlos en PASS ni en cero.
9. Las excepciones requieren alcance, responsable, fecha, fundamento, impacto y condición de revisión.
10. El estado del requisito no se eleva por el estado de otro requisito ni por la madurez general del proyecto.

## 4. Dimensiones que deben reportarse por separado

Para cada requisito registrar, como mínimo:

- cobertura documental;
- madurez de especificación;
- estado de aprobación;
- implementación y cobertura parcial;
- pruebas ejecutadas y resultados;
- validación empírica y dominio de validez;
- brechas, dependencias y excepciones;
- versión y fecha de última revisión.

No condensar estas dimensiones en un único porcentaje o score sin una metodología aprobada y validada.

## 5. Criterios especiales para metodología de inversión

- Una valoración calculada no demuestra que el valor estimado sea correcto.
- Un backtest no demuestra capacidad predictiva si existe leakage, survivorship bias, look-ahead, selección retrospectiva o calibración sobre el conjunto de prueba.
- Una señal de infravaloración no demuestra convergencia futura ni rentabilidad neta.
- La categoría EXTREME_UNDERVALUATION requiere calibración y validación independientes y debe permanecer deshabilitada mientras no exista esa evidencia.
- Un ranking solo es defendible dentro de un universo, timestamp, horizonte, finalidad y protocolo comparables.
- Los vetos críticos y la incertidumbre no pueden ocultarse en una puntuación agregada.
- Una evaluación histórica debe utilizar exclusivamente la información disponible en su timestamp de decisión.

## 6. Conformidad interna

La revisión de conformidad IIM es un proceso interno de verificación frente a requisitos versionados. Debe identificar criterio, evidencia, resultado, desviación, excepción y aprobador. No constituye certificación ISO, certificación de tercera parte ni garantía de resultados de inversión.
