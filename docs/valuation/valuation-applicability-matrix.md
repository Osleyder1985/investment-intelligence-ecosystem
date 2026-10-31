# Matriz de aplicabilidad de métodos de valoración

**Estado:** Matriz cualitativa inicial; pendiente de revisión por casos reales.  
**Versión:** 0.1.0

## 1. Leyenda

- **APPLICABLE:** la familia puede estimar directamente el objeto declarado si se verifican sus datos y supuestos.
- **CONDITIONAL:** solo es aplicable cuando se demuestra una condición específica (derecho, captura de valor, comparabilidad, respaldo, etc.).
- **NOT_APPLICABLE:** no estima de forma defendible el objeto para esa tipología bajo la definición actual.
- **INSUFFICIENT_EVIDENCE:** la tipología o los derechos no están suficientemente identificados para decidir.

La matriz no es una regla automática. La evidencia del activo concreto prevalece sobre su etiqueta genérica.

## 2. Matriz inicial

| Tipología económica | NAV / respaldo | Flujos atribuibles | Comparables | Demanda / utilidad | Escenarios monetarios | Paridad / redención | Liquidación / recuperación | Coste de producción |
|---|---|---|---|---|---|---|---|---|
| Activo monetario / reserva digital | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL |
| Activo nativo de red / consenso | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL |
| Token de utilidad / acceso | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | NOT_APPLICABLE | NOT_APPLICABLE |
| Token de gobernanza | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL | NOT_APPLICABLE |
| Token con captura de comisiones / flujos | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL | NOT_APPLICABLE |
| Staking / restaking / recibo líquido | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| Stablecoin / token referenciado | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| Token respaldado / RWA | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| Token de pool / liquidez / depósito | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| Derivado / sintético / apalancado | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| NFT / coleccionable / derecho digital | CONDITIONAL | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE |
| Token especulativo / comunitario | NOT_APPLICABLE | NOT_APPLICABLE | CONDITIONAL | CONDITIONAL | CONDITIONAL | NOT_APPLICABLE | NOT_APPLICABLE | NOT_APPLICABLE |

**Nota:** CONDITIONAL no significa recomendado. Requiere demostrar las condiciones descritas en el catálogo. Si la identidad, derechos o datos no se pueden verificar, cambiar el resultado a INSUFFICIENT_EVIDENCE.

## 3. Reglas críticas de interpretación

1. Un token de gobernanza no recibe automáticamente el NAV de la tesorería del protocolo.
2. Un protocolo que genera comisiones no implica que su token capture esas comisiones.
3. El staking yield debe descomponerse en emisión, comisiones, costes, bloqueo, slashing y variación del activo subyacente.
4. Una stablecoin no se valora únicamente por su objetivo de paridad; se evalúan reservas, redención, colateral, contraparte y liquidez.
5. Un RWA requiere identificar el derecho legal y la cadena de titularidad; la tokenización por sí sola no prueba propiedad.
6. Un activo monetario sin flujos atribuibles no debe forzarse a un DCF.
7. El coste de producción no es un suelo de precio.
8. Si un activo es híbrido, evaluar cada componente por separado y evitar sumar valores superpuestos.

## 4. Registro por activo

La implementación deberá guardar, por cada par activo/método:

- clasificación y versión de taxonomía;
- estado de aplicabilidad;
- objeto valorado;
- condición necesaria y evidencia que la satisface;
- variables y cobertura disponibles;
- supuestos y limitaciones;
- motivo de descarte o abstención;
- responsable/revisor y timestamp;
- versión del modelo y dataset.

## 5. Estado de madurez

Esta matriz es un punto de partida para pruebas de consistencia. No se han asignado pesos, probabilidades, umbrales ni precisión predictiva. Antes de usarla operativamente debe revisarse con casos documentados y contraejemplos de cada clase.
