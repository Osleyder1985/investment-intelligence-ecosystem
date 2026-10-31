# Casos de prueba del informe y del historial de evaluación

**Estado:** Casos de consistencia documental; pendientes de automatización y validación empírica.

## ER-001 — Identidad inequívoca
- Entrada: ticker compartido por varios activos.
- Esperado: informe identifica canonical_asset_id, red y contrato/representación.
- Fallo: ticker es la única clave.

## ER-002 — Dato desconocido
- Entrada: supply no disponible.
- Esperado: UNKNOWN y explicación; no cero.
- Fallo: valor por defecto cero.

## ER-003 — Campo no aplicable
- Entrada: activo nativo sin dirección de contrato.
- Esperado: NOT_APPLICABLE con motivo.
- Fallo: campo vacío ambiguo o contrato ficticio.

## ER-004 — Trazabilidad de afirmación
- Entrada: conclusión material sobre riesgo de puente.
- Esperado: referencia a evidencia, fuente, timestamps y limitaciones.
- Fallo: afirmación sin evidencia enlazada.

## ER-005 — Veto activo
- Entrada: VETO_ACTIVE.
- Esperado: veto aparece antes de valoración/ranking y bloquea conclusión favorable.
- Fallo: ocultar veto en sección secundaria.

## ER-006 — Horizonte separado
- Entrada: corto plazo favorable y largo plazo desfavorable.
- Esperado: informes/decisiones separados y explicación.
- Fallo: una conclusión común sin matices.

## ER-007 — Serialización inválida
- Entrada: NaN, Infinity o timestamp sin zona/normalización.
- Esperado: rechazo de validación.
- Fallo: persistir JSON no interoperable.

## ER-008 — Reproducción exacta
- Entrada: mismo snapshot, código, manifiesto, parámetros y entorno declarado.
- Esperado: reconstrucción equivalente o diferencia no determinista explicada.
- Fallo: no poder identificar componentes usados.

## ER-009 — Evaluación parcial
- Entrada: captura falla para una fuente crítica.
- Esperado: PARTIAL o COMPLETED_WITH_LIMITATIONS según criterios; faltante visible y conclusión limitada.
- Fallo: presentar evaluación completa.

## ER-010 — Nueva metodología
- Entrada: política de veto cambia entre snapshots.
- Esperado: versiones diferenciadas y cambio clasificado como metodología/política.
- Fallo: atribuir la diferencia al activo.

## ER-011 — Fuente revisada
- Entrada: proveedor corrige retrospectivamente un dato.
- Esperado: preservar versión anterior, registrar corrección y disponibilidad.
- Fallo: reescribir el snapshot histórico.

## ER-012 — Comparabilidad parcial
- Entrada: mismo activo/horizonte pero cambian cobertura y proveedor de una métrica.
- Esperado: PARTIALLY_COMPARABLE y exclusión/nota del componente afectado.
- Fallo: delta global como si todo fuera homogéneo.

## ER-013 — Identidad migrada
- Entrada: token migra a nuevo contrato y ticker permanece igual.
- Esperado: nueva representación/identidad enlazada y NOT_COMPARABLE o comparación segmentada.
- Fallo: tratar contratos como instrumento idéntico sin prueba.

## ER-014 — Cambio de precio frente a valor
- Entrada: precio sube, modelo e inputs no se recalculan.
- Esperado: reportar cambio de mercado; no afirmar cambio de valor razonable.
- Fallo: mover rango de valoración automáticamente.

## ER-015 — Ranking con universo distinto
- Entrada: cambia la composición del universo entre snapshots.
- Esperado: no comparar posiciones directamente o recalcular sobre universo común con protocolo documentado.
- Fallo: interpretar variación de puesto como mejora/deterioro autónomo.

## ER-016 — Cambio de parámetros
- Entrada: nuevo parámetro de modelo modifica el rango.
- Esperado: separar efecto de parámetro del cambio de mercado.
- Fallo: atribuir el rango actualizado al desempeño del activo.

## ER-017 — Diferencia no explicada
- Entrada: dos snapshots divergen sin evidencia suficiente para asignar causa.
- Esperado: UNRESOLVED_DIFFERENCE; abstenerse de atribución.
- Fallo: inventar explicación causal.

## ER-018 — No comparabilidad total
- Entrada: cambian objeto valorado, finalidad y horizonte.
- Esperado: NOT_COMPARABLE con motivos.
- Fallo: calcular delta entre magnitudes incompatibles.

## ER-019 — Inmutabilidad
- Entrada: se corrige un error del expediente.
- Esperado: nuevo evaluation_id enlazado; snapshot anterior permanece íntegro.
- Fallo: modificar el expediente histórico.

## ER-020 — Exportación coherente
- Entrada: mismo expediente exportado a UI y JSON.
- Esperado: estados, unidades, timestamps, vetos y evidencia mantienen igual significado.
- Fallo: UI transforma UNKNOWN en cero o elimina vetos.

## Criterio común
Validar esquema, semántica de estados, integridad de referencias, timestamps, unidades, versiones, inmutabilidad y reglas de comparabilidad. Los casos verifican consistencia del contrato, no exactitud predictiva ni rentabilidad.
