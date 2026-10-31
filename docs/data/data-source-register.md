# Registro de fuentes de datos

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## Propósito

Mantener un inventario controlado de proveedores, fuentes primarias, endpoints, documentos, conjuntos de datos y restricciones de uso. Una fuente puede ser útil para una variable y no apta para otra. La evaluación se realiza por fuente, producto de datos, campo, periodo y finalidad.

## Ficha mínima de fuente

| Campo | Descripción |
|---|---|
| source_id | Identificador estable e interno |
| provider / owner | Proveedor y propietario/origen del dato |
| source_name | Nombre del producto, dataset o documento |
| source_type | Primaria, oficial, exchange, nodo, indexador, agregador, analista u otra |
| endpoint_or_location | Endpoint, archivo, contrato, página o ubicación |
| documentation | Documentación técnica y especificación |
| terms_url / license_id | Términos, licencia y versión aplicable |
| permitted_uses | Captura, almacenamiento, análisis, transformación, redistribución y uso comercial/no comercial |
| restrictions | Límites contractuales, atribución, retención, privacidad y prohibiciones |
| coverage | Activos, mercados, redes, campos y periodo cubiertos |
| methodology | Definición del dato y método de agregación |
| update_frequency | Frecuencia declarada y observada |
| timestamps | Semántica de timestamps y zona horaria |
| known_limitations | Faltantes, retrasos, revisiones, sesgos y cobertura |
| capture_method | API, descarga, nodo, archivo, scraping permitido u otro |
| validation_method | Reconciliación y controles aplicados |
| status | CANDIDATE / APPROVED_FOR_SCOPE / RESTRICTED / SUSPENDED / REJECTED / TERMS_PENDING |
| assessed_at_utc | Fecha de revisión |
| valid_from / valid_until | Vigencia conocida de la aprobación/condiciones |
| evidence_refs | Copias, hashes y referencias de evidencia |

## Estados de admisibilidad de fuente

- CANDIDATE: identificada, aún no evaluada.
- APPROVED_FOR_SCOPE: aprobada solo para los campos, periodos y usos documentados.
- RESTRICTED: admisible con restricciones explícitas.
- SUSPENDED: suspendida por cambio de términos, calidad, seguridad o disponibilidad.
- REJECTED: no admisible para el uso evaluado, con motivo.
- TERMS_PENDING: condiciones o permisos no resueltos; no habilitar captura/retención que requiera autorización.

La aprobación no es global ni permanente. Revaluar cuando cambien términos, endpoints, metodología, propietario, cobertura o finalidad.

## Evaluación de licencia y términos

Separar expresamente permisos para:
1. consultar/acceder;
2. capturar/descargar;
3. almacenar y conservar;
4. transformar y derivar;
5. usar en investigación/modelos;
6. mostrar al usuario;
7. redistribuir/exportar;
8. uso comercial/no comercial.

No inferir permiso de almacenamiento o redistribución a partir de acceso gratuito. Conservar versión de términos, fecha, texto/hash, alcance, responsable y justificación. Si hay ambigüedad material, mantener TERMS_PENDING y solicitar aclaración.

## Selección de fuentes

Priorizar la fuente más cercana al hecho económico y técnicamente verificable, no simplemente la más popular. Una fuente oficial puede ser incompleta o no independiente; un agregador puede aportar cobertura, pero requiere conservar linaje y metodología. No sustituir una fuente sin registrar el cambio y su impacto.

## Historial

No sobrescribir aprobaciones anteriores. Cada revisión genera una nueva versión con timestamp UTC, autor/rol, evidencia, diferencias y campos afectados. Una fuente puede estar aprobada para un uso y restringida para otro.
