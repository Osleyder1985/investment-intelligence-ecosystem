# Validador de payloads de observaciones — Android

**Estado:** implementación inicial; solo existe un contrato explícito para el fixture local `IDENTITY_FIXTURE` v1. No hay contratos productivos de datos de mercado.

## Propósito

Validar la sintaxis JSON y la estructura de payloads normalizados mediante contratos versionados y específicos por tipo de observación. La ausencia de contrato produce `NotEvaluated(NO_MATCHING_CONTRACT)`; nunca se interpreta como validación satisfactoria.

## Contrato inicial

`IDENTITY_FIXTURE` v1 requiere un objeto JSON con:
- `recordType`: string obligatorio;
- `marketData`: boolean obligatorio.

No admite campos adicionales. El contrato refleja exclusivamente el fixture sintético ya persistido por `DemoAssetSeeder`; no es un contrato de mercado.

## Comportamiento

- JSON malformado: inválido.
- Tipo raíz distinto del requerido: inválido.
- Campo obligatorio ausente: inválido.
- Tipo de campo incorrecto: inválido.
- Campo no declarado: inválido por defecto.
- Tipo de observación o versión sin contrato: no evaluado.
- Contratos duplicados para el mismo tipo/versión: rechazados al construir el validador.

Los errores incluyen códigos y rutas JSON para facilitar diagnóstico.

## Límites

- La primera versión valida estructura de raíz y tipos primitivos; no define restricciones anidadas, rangos numéricos, formatos de direcciones, unidades, precisión decimal, coherencia financiera ni reglas específicas de mercado.
- No valida veracidad, procedencia, autorización de uso, frescura, calidad de proveedor ni elegibilidad de inversión.
- No transforma ni corrige payloads.
- El parser usa kotlinx.serialization JSON; el contenido no se evalúa mediante código recibido desde proveedores.
- No se registra ni persiste un resultado de validación todavía; no hay integración con Room o ingesta.

## Verificación esperada

Tests unitarios para payload válido, JSON malformado, raíz incorrecta, campos ausentes, tipos incorrectos, propiedades desconocidas, contrato inexistente y ámbito duplicado. La ejecución satisfactoria debe confirmarse en CI antes de marcar la etapa como probada.
