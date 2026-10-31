# Política de calidad y admisibilidad de datos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Principio

La calidad se evalúa para un uso concreto: variable, activo, mercado, periodo, frecuencia, horizonte y decisión. No existe una fuente universalmente fiable ni una puntuación agregada aprobada en esta versión.

## 2. Dimensiones de calidad

| Dimensión | Pregunta de control |
|---|---|
| Exactitud | ¿El valor concuerda con evidencia primaria o reconciliación independiente? |
| Completitud | ¿Están presentes los campos y periodos necesarios? |
| Consistencia | ¿Unidades, definiciones, relaciones y series son coherentes? |
| Oportunidad | ¿El dato estaba disponible a tiempo para la decisión? |
| Unicidad | ¿Se detectan duplicados o eventos repetidos? |
| Validez | ¿Cumple esquema, dominio, unidad y reglas semánticas? |
| Cobertura | ¿Representa los activos, mercados, redes y periodos relevantes? |
| Continuidad | ¿Existen gaps, discontinuidades o cambios de metodología? |
| Trazabilidad | ¿Se puede reconstruir origen y transformación? |
| Licencia | ¿El uso concreto está permitido y documentado? |

No asignar pesos ni sumar dimensiones en una puntuación sin calibración y validación. Mostrar cada dimensión, evidencia y limitación por separado.

## 3. Estados de admisibilidad del dato

- ACCEPTED: satisface los controles requeridos para el uso declarado.
- ACCEPTED_WITH_LIMITATIONS: puede utilizarse con limitaciones visibles y efecto documentado.
- QUARANTINED: conflicto, anomalía o cambio pendiente de investigación; no usar en conclusión principal.
- REJECTED: inválido, incompatible, no verificable o prohibido para ese uso.
- UNKNOWN: no existe evidencia suficiente para decidir admisibilidad.

La admisibilidad de una fuente no convierte automáticamente todos sus registros en ACCEPTED.

## 4. Reglas de validación

- Validar schema, tipos, unidades, rangos semánticos y timestamps.
- Detectar duplicados, orden temporal incorrecto, gaps, discontinuidades y revisiones.
- Comparar con fuentes independientes cuando la variable y el riesgo lo justifiquen.
- No eliminar outliers automáticamente: investigar si son errores, eventos reales o manipulación.
- No interpolar, rellenar ni extrapolar faltantes silenciosamente.
- Preservar dato original y dato corregido como versiones separadas.
- Registrar motivo y evidencia de aceptación, cuarentena o rechazo.
- Documentar metodología de agregadores, venue coverage, exclusiones y filtros.
- En datos de mercado, distinguir último precio, bid/ask, volumen reportado y volumen ejecutable.
- En datos on-chain, distinguir estado de cadena, indexación, reorganización, finalización y semántica del proveedor.
- En noticias, distinguir event_time, publication_time y availability_time.
- En datos jurídicos, registrar jurisdicción, autoridad, vigencia, fecha y alcance; no convertir resumen de terceros en texto normativo.

## 5. Reconciliación de fuentes

Cuando fuentes discrepen:
1. verificar que miden el mismo objeto, unidad, mercado, periodo y definición;
2. comprobar timestamps, revisiones y cobertura;
3. inspeccionar fuente primaria y método del agregador;
4. conservar valores discrepantes y explicar diferencias;
5. seleccionar una fuente para el uso solo con justificación documentada;
6. si no se resuelve una discrepancia material, QUARANTINED o UNKNOWN y abstenerse de conclusiones dependientes.

No promediar precios, supply o métricas heterogéneas para esconder conflicto.

## 6. Faltantes y anomalías

Los faltantes son null/desconocidos, nunca cero. Un gap no se rellena sin método, etiqueta y justificación. Una discontinuidad puede invalidar indicadores o comparaciones temporales. Los outliers se investigan con datos originales, eventos y fuentes independientes; no recortarlos para mejorar resultados.

Si un campo crítico falta, reducir alcance, usar ACCEPTED_WITH_LIMITATIONS si es defendible o abstenerse. Documentar qué conclusión se ve afectada.

## 7. Cambios de proveedor y revisiones

Versionar definiciones, esquemas, endpoints, metodologías, backfills y políticas de corrección. Comparar series antes/después y señalar quiebres. No recalcular una evaluación histórica usando datos revisados sin marcar una nueva versión y preservar la anterior.

## 8. Licencias

La admisibilidad técnica y la legal son controles separados. Un dato técnicamente válido se REJECTED o queda bloqueado para un uso si la licencia no lo permite. Registrar permisos para captura, almacenamiento, transformación, análisis, visualización, exportación y redistribución. TERMS_PENDING no habilita usos que requieren autorización.

## 9. Efecto sobre conclusiones

Un dato crítico UNKNOWN, QUARANTINED o REJECTED impide conclusiones que dependan de él. La salida debe identificar campos faltantes, fuentes en conflicto, cobertura y efecto sobre incertidumbre. Nunca convertir falta de evidencia en evidencia favorable.


## 10. Contrato de control por variable y finalidad (aclaración v0.2.0)

La decisión de admisibilidad debe emitirse para una unidad de evaluación definida: source_id/producto, campo/variable, activo o instrumento, red/venue, periodo, frecuencia, finalidad, decision_time y modalidad point-in-time.

Para cada control registrar:

- control_id y dimensión de calidad;
- aplicabilidad y razón;
- método/consulta/versión utilizada;
- resultado observado y unidad;
- evidencia y timestamp;
- estado (PASS, FAIL, UNKNOWN, NOT_APPLICABLE) con justificación;
- efecto sobre el dato y las conclusiones dependientes;
- revisor y versión del protocolo.

La decisión agregada ACCEPTED, ACCEPTED_WITH_LIMITATIONS, QUARANTINED, REJECTED o UNKNOWN debe derivarse de controles requeridos para ese uso y conservar el detalle. No convertir UNKNOWN en PASS. No crear límites universales de latencia, discrepancia, completitud o desviación sin fundamento y validación por finalidad.

### 10.1 Disponibilidad y tiempo

Para cada dato histórico identificar si la disponibilidad se refiere a acceso público demostrable, acceso bajo cuenta/permiso concreto o ingestión efectiva por el sistema. Registrar por separado availability_time y capture_time. Si no puede probarse la disponibilidad relevante para la afirmación, marcar UNKNOWN y bloquear la conclusión temporal que dependa de ella.

### 10.2 Dependencias y datos derivados

La admisibilidad de un derivado depende de sus insumos, transformaciones y tiempos. Registrar el insumo con disponibilidad más tardía, processing_time, cierre de ventana y reglas de revisión/backfill. Un insumo UNKNOWN o inadmisible propaga la limitación a los derivados que dependan materialmente de él.

### 10.3 Hashes y procedencia

Un hash puede ayudar a detectar cambios del objeto comparado, pero no prueba por sí solo autoría, veracidad, timestamp de publicación, disponibilidad ni permiso de uso. Esos atributos requieren evidencia independiente.
