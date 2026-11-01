# Coordinación de evaluación de evidencias — Android

**Estado:** coordinador de dominio inicial; no integrado en Room, UI ni ingesta.

## Dimensiones independientes

`ObservationEvidenceEvaluator` ejecuta tres evaluaciones para una observación:
1. **Payload:** sintaxis y contrato JSON exacto por tipo/versión.
2. **Calidad:** metadatos, cronología, versión esperada, uso permitido y estado de verificación según política.
3. **Frescura:** antigüedad respecto de una política específica de fuente, tipo e intención de uso.

El resultado conserva las tres dimensiones separadas. No hay puntuación agregada ni decisión automática de inversión.

## Semántica importante

- Payload inválido no se convierte en válido porque calidad/frescura pasen.
- Ausencia de contrato o política se expresa como `NotEvaluated`.
- `INTERNAL_DEMO_ONLY` solo puede pasar si la política de calidad lo autoriza para el uso explícito de demostración.
- Ninguna política de frescura productiva se crea por defecto.
- La validación estructural no demuestra veracidad ni exactitud económica.

## Alcance pendiente

- Integración con flujo de ingesta y persistencia de evaluaciones.
- Políticas productivas aprobadas para proveedores reales.
- Contratos de mercado versionados y pruebas con payloads reales autorizados.
- Pruebas instrumentadas Room en dispositivo/emulador.
- Exposición de resultados en UI con explicación de cada dimensión.
