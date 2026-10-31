# X-09 — Fundamentos formales y estrategia de verificación del Scope Algebra

**ID:** IIE-METH-SCOPE-FORMAL-014  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Dependencias:** documentos 10–12, 14, 16–19; M-03; X-05/X-06/X-07/X-09.  
**Propósito:** definir obligaciones formales, estrategia de demostración, límites de complejidad y criterios de prueba para la futura implementación del Scope Algebra.  
**No alcance:** no selecciona una tecnología definitiva, no crea schemas/código, no modifica requisitos IIM ni aprueba políticas institucionales.

## 1. Dictamen

El documento 19 define el modelo semántico y las operaciones parciales. Este documento especifica qué significa demostrar que una operación es correcta y cómo verificar una futura implementación.

No existe una única técnica que demuestre exactamente cualquier expresión arbitraria de conjuntos. La estrategia depende de la clase de predicado: enumeración finita, intervalos, jerarquías versionadas o expresiones simbólicas restringidas. Si el sistema no puede producir una prueba verificable dentro de límites aprobados, debe devolver UNRESOLVED/INCOMPLETE o REVIEW_REQUIRED, nunca aproximar una prueba y tratarla como exacta.

**Principio:** la implementación no puede declarar una propiedad más fuerte que la evidencia de verificación que realmente produjo.

## 2. Semántica formal mínima

### 2.1 Universo tipado

Para una firma de scope fija y una versión de universo U, sea:
- D_i el dominio tipado de la dimensión i;
- U = D_1 × ... × D_n el universo de combinaciones válidas;
- S ⊆ U el conjunto denotado por un scope resuelto.

Una dimensión UNCONSTRAINED_BY_SCOPE representa el dominio completo D_i de la versión de universo declarada; no un wildcard global. NOT_APPLICABLE solo es admisible cuando la firma del scope_kind declara esa dimensión no aplicable. UNRESOLVED no denota un subconjunto conocido de U.

### 2.2 Scope denotation

Una ScopeDefinition resuelta denota un conjunto S. La representación debe preservar:
- scope_kind y dimensión/firma;
- universe_id/version;
- normalization_version;
- temporal and quantitative conventions;
- predicate language/version;
- dependency snapshot/version cuando sea material;
- provenance y digest semántico.

Dos representaciones textuales distintas pueden denotar el mismo conjunto; dos textos idénticos pueden no ser equivalentes si cambia universo, versión o semántica.

### 2.3 Resultados de operación

Cada operador retorna un resultado tipado (status, denotation, proof_ref, reason_codes, operand_refs, algorithm_version). RESOLVED requiere denotation exacta; EMPTY requiere prueba de vacío; UNRESOLVED significa que no se pudo decidir; INVALID significa que el contrato o dominio no es admisible. Ningún consumidor puede descartar el status y usar solo el payload.

## 3. Leyes algebraicas y obligaciones

Las leyes siguientes aplican únicamente a operandos resueltos, comparables, con la misma firma y universo, y dentro de un modelo de conjuntos exacto.

### 3.1 Igualdad de conjuntos

- Reflexividad: A = A.
- Simetría: si A = B, entonces B = A.
- Transitividad: si A = B y B = C, entonces A = C.
- Igualdad extensional: A = B si y solo si A ⊆ B y B ⊆ A.

La igualdad de scope IDs no prueba igualdad extensional; el digest de una representación no sustituye la comprobación de equivalencia semántica.

### 3.2 Contención

- A ⊆ A.
- Si A ⊆ B y B ⊆ C, entonces A ⊆ C.
- A = B si A ⊆ B y B ⊆ A.
- A ⊆ B implica A ∩ B = A.

No inferir contención desde nombres, prefijos, similitud, muestras o relaciones no versionadas.

### 3.3 Unión e intersección

- Conmutatividad: A ∪ B = B ∪ A; A ∩ B = B ∩ A.
- Asociatividad: (A ∪ B) ∪ C = A ∪ (B ∪ C); equivalente para intersección.
- Idempotencia: A ∪ A = A; A ∩ A = A.
- Distributividad: A ∩ (B ∪ C) = (A ∩ B) ∪ (A ∩ C); dual para unión.
- Absorción: A ∪ (A ∩ B) = A; A ∩ (A ∪ B) = A.
- Identidades: A ∪ ∅ = A; A ∩ U = A.

Una operación no puede reclamar estas propiedades entre universos, firmas o versiones incompatibles.

### 3.4 Diferencia y complemento relativo

- A \ ∅ = A.
- A \ A = ∅.
- (A \ B) ∩ B = ∅.
- (A \ B) ∪ (A ∩ B) = A.
- A \ (B ∪ C) = (A \ B) \ C.
- A \ B = A ∩ (U \ B), solo cuando U y complemento están definidos y cerrados.

La diferencia requiere universo/operando suficientemente resuelto; un B incompleto no permite declarar residual exacto.

### 3.5 Partición exacta

Para P={P_1,...,P_n} y requested scope R:
- n ≥ 1;
- cada P_i es no vacío y P_i ⊆ R;
- P_i ∩ P_j = ∅ para todo i ≠ j;
- ⋃P_i = R.

La prueba de cobertura y la prueba de disjunción son obligaciones independientes. Comprobar únicamente cardinalidades, muestras o conteos no satisface ninguna de las dos.

## 4. Matriz de prueba según tipo de dominio

| Dominio | Representación inicial candidata | Verificación exacta posible si… | Riesgo/limitación |
|---|---|---|---|
| Enumeración finita | Set de IDs canónicos | Universo cerrado, IDs únicos y snapshot inmutable | Cardinalidad y memoria |
| Intervalos numéricos | Intervalos tipados | Unidad, escala, límites e inclusividad normalizados | Conversión/overflow/precisión |
| Tiempo | Intervalos half-open UTC con precisión | TZ, calendario, precisión y orden definidos | Leap seconds, DST, fuentes temporales |
| Jerarquía | Árbol/DAG versionado | Ancestros, ciclos y closure controlados | Cambios de jerarquía y DAG ambiguo |
| Producto cartesiano | Predicados por dimensión | Independencia de dimensiones demostrada | Explosión combinatoria |
| Predicate DSL | AST restringido | Fragmento decidible y solver/checker confiable | Coste y límites del solver |
| Dependency graph | Grafo tipado versionado | Edges, activación, dirección y transitividad definidos | Ciclos, graph poisoning, incompletitud |

La selección de técnicas queda pendiente de Architecture/Security. No se permite aceptar un resultado aproximado de solver como prueba exacta.

## 5. Certificados de prueba y verificación independiente

Cada operación crítica debe producir un artefacto de verificación o una razón explícita por la que no pudo producirlo.

### 5.1 Certificados mínimos por propiedad

| Propiedad | Evidencia mínima propuesta |
|---|---|
| EMPTY | Contradicción demostrada, conjunto enumerado vacío o certificado de solver verificable |
| DISJOINT | Prueba de que A ∩ B = ∅ |
| CONTAINS(A,B) | Prueba de que B \ A = ∅ |
| EQUAL(A,B) | Pruebas de contención en ambas direcciones |
| COVERAGE | Prueba de R \ ⋃P_i = ∅ y ⋃P_i ⊆ R |
| PAIRWISE_DISJOINT | Prueba de intersección vacía para cada par o método equivalente certificado |
| DEPENDENCY_CLOSURE | Snapshot de grafo, reglas de propagación y conjunto de edges activados |
| NORMALIZATION | Entrada original, salida canónica, versión y evidencia de ausencia de colisión |

El proof_ref debe apuntar a un objeto inmutable con hash, versión de formato y método. Un hash acredita integridad del artefacto, no la verdad matemática del contenido. Debe existir verificador independiente para los certificados admitidos.

### 5.2 TCB (trusted computing base)

Architecture y Security deben identificar y minimizar:
- canonicalizer/resolver;
- parser y type checker;
- normalizer de unidades/tiempo;
- motor de operaciones;
- solver cuando aplique;
- checker de certificados;
- generador de manifest/digest;
- almacén de snapshots y provenance.

El verificador de certificados debe ser más pequeño y simple que el generador cuando sea viable. No se afirma que el uso de solver constituya por sí mismo prueba independiente.

### 5.3 Fallo de verificación

Timeout, límite de memoria, solver UNKNOWN/UNKNOWN-like, certificado ausente, checker no disponible, versión incompatible o snapshot incompleto => UNRESOLVED/INCOMPLETE o REVIEW_REQUIRED según política aprobada. Nunca transformar un timeout en EMPTY, DISJOINT o NOT_AFFECTED.

## 6. Algoritmo de partición: obligaciones

El algoritmo futuro debe ser determinista respecto de:
- requested_scope y universo congelados;
- canonicalization/normalization version;
- orden estable de reglas, boundaries y dependency edges;
- predicate language y versión;
- política de partición/fusión;
- límite de recursos y comportamiento al excederlo.

Procedimiento de referencia:
1. Validar y congelar scope, universe y snapshots.
2. Resolver IDs, alias y dimensiones; rechazar ambigüedad material.
3. Validar compatibilidad de firmas y unidades.
4. Construir boundaries exactos inducidos por predicados y dependencias aplicables.
5. Calcular regiones por operadores exactos.
6. Mantener residual explícito dentro de R.
7. Verificar homogeneidad por inputs, reglas, dependencias y consecuencias.
8. Verificar no vacío, contención, disjunción par a par y cobertura exacta.
9. Verificar edges que cruzan fronteras y propagación aplicable.
10. Fusionar solo bajo equivalencia completa aprobada.
11. Emitir manifest y certificados; si cualquier obligación falla, no emitir VALID.

Un particionador puede devolver más átomos de los mínimos posibles. La minimalidad no es requisito de corrección; la exactitud, exhaustividad, disjunción y homogeneidad sí. La reducción/merge se optimiza después de preservar las fronteras semánticas.

## 7. Complejidad y control de explosión combinatoria

Con k dimensiones y b_i regiones por dimensión, una expansión cartesiana ingenua puede producir hasta ∏ b_i regiones. Con muchas dimensiones y reglas solapadas el crecimiento puede ser exponencial.

Controles propuestos:
- representar predicados simbólicamente en vez de enumerar todo el producto cartesiano;
- particionar solo por boundaries relevantes para la decisión y requested_scope;
- evaluar dependencias bajo demanda, con closure explícito y acotado;
- usar índices por dimensión y pruning solo cuando la exclusión sea demostrable;
- cachear resultados con clave que incluya operands, universo, versiones y algoritmo;
- imponer límites deterministas de tiempo, memoria, número de regiones, edges y profundidad;
- conservar residual no resuelto cuando el límite se exceda;
- no fusionar regiones solo para caber en el límite si se pierde homogeneidad.

Los límites numéricos deben fijarse mediante benchmark reproducible y revisión de seguridad; no se inventan en esta propuesta.

## 8. Determinismo y canonicalización

Mismas entradas semánticas, mismo snapshot, mismo universo, mismas versiones y misma configuración deben producir:
- mismo conjunto denotado;
- misma clasificación de resolución;
- mismo conjunto de fronteras canónicas;
- mismo manifest semántico y digest;
- mismas razones y resultados.

Orden de llegada, concurrencia, paginación, locale, reloj implícito o orden de iteración de estructuras no pueden alterar resultado. Si el output incluye metadatos de ejecución variables, separarlos del digest semántico.

Canonicalización debe definir orden de campos, codificación, normalización Unicode, formato de IDs, decimales, intervalos, timezone, nullability y arrays. Los detalles criptográficos/digest se aprobarán en una especificación de seguridad separada.

## 9. Estrategia de pruebas

### 9.1 Niveles

1. Unit tests por operador y tipo de dimensión.
2. Property-based tests para leyes algebraicas.
3. Metamorphic tests para invariancia bajo permutación/representación equivalente.
4. Differential tests entre implementación y checker/oráculo independiente.
5. Boundary tests para intervalos, precisión, overflow y límites.
6. Negative/adversarial tests para alias collision, scope injection, graph poisoning, malformed predicates y resource exhaustion.
7. Integration tests X-05/X-06/X-07/X-09 y ValidationEnvelope.
8. Replay tests para reproducibilidad histórica.
9. Performance tests para medir explosión y comportamiento de límites.
10. Independent review de los modelos, generadores de casos y expected outcomes.

### 9.2 Propiedades generativas

Para scopes resueltos/comparables:
- commutativity/associativity/idempotence de unión/intersección;
- distributivity y absorption;
- reflexivity/transitivity de igualdad/contención;
- A∩B y A\B disjuntos;
- (A∩B)∪(A\B)=A;
- partición aleatoria válida reconstruye R y no solapa;
- permutar operandos no cambia denotation/digest semántico;
- canonicalizar dos veces es idempotente;
- serializar/deserializar conserva denotation;
- derived scope siempre está contenido en parent;
- dependency propagation respeta edge scope y policy;
- límites de recursos nunca producen falso proof.

Generadores deben producir también casos degenerados, vacíos, extremos, duplicados, aliases, intervalos contiguos, inclusividad distinta, universos vacíos/cambiantes y relaciones cíclicas.

### 9.3 Pruebas negativas obligatorias

- dimensión omitida tratada como ALL;
- UNRESOLVED tratado como EMPTY/DISJOINT;
- conversión USD/token sin rate snapshot;
- intervalos solapados en frontera;
- alias ambiguo/reutilizado;
- chain/address sin namespace;
- parent/child no registrado;
- scope con versión de universe diferente;
- dependencia ausente tratada como independencia;
- transitividad implícita;
- timeout del solver convertido en resultado exacto;
- límite computacional excedido y resultado favorable;
- reordenamiento de inputs altera digest;
- merge de átomos con inputs distintos;
- derived scope amplía parent;
- residual sin baseline evaluada.

## 10. Trazabilidad de propiedades

Cada property/test debe mapear a:
- law_id/invariant_id;
- operador y tipo de dimensión;
- scope_kind y universe fixture;
- preconditions;
- generator/seed;
- expected property, no solo expected example;
- versión de implementation y checker;
- evidencia de ejecución, resultado, logs y digest;
- owner y reviewer.

Los seeds deben guardarse para reproducir fallos. Un test que solo pasa en una muestra no prueba exhaustividad matemática; la combinación de pruebas generativas y certificados debe corresponder al modelo de dominio declarado.

## 11. Criterios de aceptación previos a implementación

- [ ] Architecture aprueba firmas y universo por scope_kind.
- [ ] Owners aprueban semántica por dimensión y valores UNCONSTRAINED/NOT_APPLICABLE.
- [ ] Se fija un fragmento de predicados decidible y versionado.
- [ ] Se documentan leyes válidas y precondiciones de cada operador.
- [ ] Se aprueba proof strategy para EMPTY, DISJOINT, CONTAINS, EQUAL y COVERAGE.
- [ ] Se define checker independiente y TCB.
- [ ] Se definen límites de recursos y comportamiento fail-closed.
- [ ] Se define determinismo/canonicalización/digest.
- [ ] Se aprueba dependency closure y reglas de transitividad.
- [ ] Se completa threat model y pruebas adversariales.
- [ ] Se aprueba mapping de ScopeResolutionResult a ValidationEnvelope.
- [ ] Cada escenario/invariante tiene expected result, owner y futuro test ID.
- [ ] CI ejecuta suites deterministas y conserva artefactos.
- [ ] Revisión independiente de Architecture, Risk, Security, Operations y Requirement Owners.

## 12. Cuestiones abiertas

1. ¿Se limitará el predicate language a un fragmento decidible, y cuál?
2. ¿Qué solver/checker o algoritmos exactos se aceptarán por tipo de dimensión?
3. ¿Qué formato de proof certificate será verificable y portable?
4. ¿Qué universo cerrado se exige para cada operación?
5. ¿Qué nivel de prueba es suficiente para jerarquías y dependencias?
6. ¿Qué límites de recursos y SLO son aceptables por caso de uso?
7. ¿Qué mecanismo de canonicalización/digest resiste colisiones semánticas?
8. ¿Qué independencia se requiere para el checker y para el equipo que aprueba reglas?
9. ¿Qué casos pueden resolverse simbólicamente sin materializar átomos?
10. ¿Cómo se mide y gobierna el coste computacional sin degradar cobertura?

## 13. Estado y límites

**ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Este documento define obligaciones y una estrategia candidata de verificación. No demuestra que el álgebra actual esté formalmente probada, no selecciona solver, no contiene código, schemas, fixtures ni tests ejecutables. No se han ejecutado property-based, differential, performance ni pruebas adversariales. No hay benchmark ni límites de recursos aprobados. No modifica requisitos IIM, políticas aprobadas ni contratos.

El siguiente gate es revisión de owners y selección del fragmento de predicados/proof strategy. Solo después debe diseñarse el contrato ejecutable y una implementación verificable.
