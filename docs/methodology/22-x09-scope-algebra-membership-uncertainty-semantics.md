# FSA-01 — Semántica de pertenencia e incertidumbre del Scope Algebra

**ID:** IIE-METH-SCOPE-SEMANTICS-016  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** ADDRESSED_IN_DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Documento padre:** `20-x09-scope-algebra-formal-verification-strategy.md`  
**Hallazgo:** FSA-01 de `21-x09-scope-algebra-formal-verification-review.md`.

## 1. Propósito y dictamen

Este documento propone una semántica explícita para evaluar pertenencia de un elemento a un scope cuando hay información completa, falsa, desconocida o inválida. También define cómo se propaga ese resultado por operadores booleanos, operaciones de conjuntos y consultas de prueba.

**Dictamen:** FSA-01 queda ADDRESSED_IN_DRAFT, no cerrado. La semántica propuesta usa tres valores de conocimiento (TRUE, FALSE, UNKNOWN) y un estado de error de contrato (INVALID). INVALID no es un cuarto valor lógico ordinario: es un resultado de evaluación que impide continuar con el cálculo como si fuera una proposición válida.

El modelo es una propuesta formal para revisión de Architecture, Security, Risk, Operations y Requirement Owners. No constituye aprobación matemática independiente ni habilita implementación.

## 2. Separación entre validez, conocimiento y pertenencia

Se distinguen tres capas que no deben colapsarse:

1. **Validity:** determina si la expresión, su firma, tipos, versiones y referencias cumplen el contrato. Puede producir INVALID.
2. **Knowledge/evaluation:** para una expresión válida, indica si la pertenencia está demostrada (TRUE), refutada (FALSE) o no decidida (UNKNOWN).
3. **Set-operation result:** agrega los resultados de pertenencia sobre un universo declarado y devuelve RESOLVED, EMPTY, UNRESOLVED o INVALID con las pruebas exigidas.

Por tanto, INVALID no equivale a FALSE; UNKNOWN no equivale a FALSE; y una evaluación UNKNOWN no denota el conjunto vacío.

## 3. Dominio semántico

Para una firma y snapshot de universo compatibles, sea U el universo tipado. Para una expresión válida P y un elemento x ∈ U:

`eval(P, x) ∈ {T, F, U}`

donde:
- **T / TRUE:** la evidencia y las reglas aprobadas demuestran que x satisface P.
- **F / FALSE:** la evidencia y las reglas aprobadas demuestran que x no satisface P.
- **U / UNKNOWN:** no hay base suficiente para demostrar T ni F, existe una dimensión relevante no resuelta, conflicto no adjudicado o dependencia incompleta.

Si la validación estructural/semántica de P falla, el evaluador devuelve `INVALID(reason_codes)` fuera del dominio lógico anterior. Un INVALID en cualquier subexpresión necesaria invalida la expresión compuesta; no se degrada a UNKNOWN ni FALSE.

Una evaluación T o F debe estar respaldada por evidencia admisible y contexto versionado. La falta de evidencia solo produce F cuando la regla del dominio define explícitamente una condición negativa cerrada y la completitud de la fuente/universo está demostrada. En caso contrario produce U.

## 4. Conectivos: lógica de Kleene fuerte con INVALID estricto

Para operandos válidos, AND, OR y NOT siguen la lógica de Kleene fuerte de tres valores. INVALID es estricto: si cualquier operando requerido es inválido, la expresión compuesta es INVALID. La validación de tipos/contratos precede a la evaluación lógica.

### 4.1 NOT

| a | NOT a |
|---|---|
| T | F |
| F | T |
| U | U |

### 4.2 AND

| AND | T | F | U |
|---|---:|---:|---:|
| **T** | T | F | U |
| **F** | F | F | F |
| **U** | U | F | U |

### 4.3 OR

| OR | T | F | U |
|---|---:|---:|---:|
| **T** | T | T | T |
| **F** | T | F | U |
| **U** | T | U | U |

### 4.4 Reglas operativas

- `T AND U = U`; `F AND U = F`.
- `T OR U = T`; `F OR U = U`.
- `NOT U = U`.
- INVALID en cualquier operando requerido => INVALID, incluso si otro operando podría absorber el valor lógico. Esta regla evita ocultar entradas malformadas; una optimización short-circuit solo puede omitirse si preserva el mismo resultado y los mismos diagnostics.
- No se permiten conversiones implícitas de UNKNOWN o INVALID a boolean false/true.

## 5. Pertenencia y operaciones de conjuntos

Para scopes A y B con misma firma/universo, la pertenencia se compone punto a punto:

| Operación | Pertenencia en x |
|---|---|
| Unión A ∪ B | eval(A,x) OR eval(B,x) |
| Intersección A ∩ B | eval(A,x) AND eval(B,x) |
| Complemento relativo U \\ A | NOT eval(A,x), solo con U cerrado y compatible |
| Diferencia A \\ B | eval(A,x) AND NOT eval(B,x) |

Si el universo, la firma, las unidades o las versiones no son compatibles, el resultado es INVALID o UNRESOLVED según la naturaleza del problema y la política de validación; nunca se fuerza una comparación.

### 5.1 Aproximaciones inferior y superior

Para una expresión válida P sobre universo cerrado U:
- `L(P) = {x ∈ U | eval(P,x)=T}` es la extensión demostrada (lower bound).
- `H(P) = {x ∈ U | eval(P,x) ∈ {T,U}}` es la extensión posible (upper bound).
- Se cumple `L(P) ⊆ H(P) ⊆ U`.
- P tiene denotación exacta solo si `L(P)=H(P)`, es decir, no quedan elementos UNKNOWN dentro del universo consultado.

Estas aproximaciones son artefactos internos de razonamiento; no autorizan a tratar H(P) como scope elegible ni L(P) como cobertura exhaustiva. Si U no es cerrado/completo, tampoco puede afirmarse que H(P) cubra el universo real.

Para operandos válidos y compatibles, las cotas se propagan de forma conservadora:
- `L(A ∪ B) = L(A) ∪ L(B)` y `H(A ∪ B) = H(A) ∪ H(B)`;
- `L(A ∩ B) = L(A) ∩ L(B)` y `H(A ∩ B) = H(A) ∩ H(B)`;
- `L(U \\ A) = U \\ H(A)` y `H(U \\ A) = U \\ L(A)`;
- `L(A \\ B) = L(A) \\ H(B)` y `H(A \\ B) = H(A) \\ L(B)`.

Estas ecuaciones son parte de la obligación de soundness del modelo propuesto y deben demostrarse para el fragmento de predicados aprobado. En consecuencia, `DISJOINT(A,B)` requiere demostrar `H(A) ∩ H(B) = ∅`; `A ⊆ B` requiere demostrar `H(A) \\ L(B) = ∅`; y `A = B` requiere ambas contenciones. No basta con que `L(A) ∩ L(B)` sea vacío, porque regiones UNKNOWN pueden contener una intersección posible.

### 5.2 Consultas de vacío y no vacío

- **EMPTY probado:** requiere universo cerrado/completo y prueba de que `L(P)=∅` y `H(P)=∅` (equivalentemente, toda pertenencia relevante está demostrada F). Si hay cualquier región U, no se demuestra EMPTY.
- **NON_EMPTY probado:** basta un witness x con evaluación T, con identidad y evidencia válidas.
- **UNKNOWN emptiness:** si no existe witness T pero queda alguna región U, no se puede concluir EMPTY ni NON_EMPTY.
- La ausencia de filas, resultados o muestras no demuestra EMPTY salvo que el universo de consulta sea cerrado y la exhaustividad de enumeración esté probada.

### 5.3 DISJOINT

A y B son DISJOINT únicamente si se demuestra que no existe x ∈ U con `eval(A,x)=T` y `eval(B,x)=T`, y la consulta cubre exhaustivamente el universo relevante. Cualquier región donde ambas pertenencias puedan ser T y no se pueda refutar esa posibilidad mantiene la relación UNRESOLVED. Un witness T/T demuestra OVERLAPS.

### 5.4 Contención e igualdad

- `A ⊆ B` se prueba cuando no existe contraejemplo demostrado ni posible en el universo completo: ningún x puede tener A=T y B=F, y ninguna región UNKNOWN puede ocultar ese contraejemplo.
- `A = B` requiere contención demostrada en ambas direcciones, o prueba equivalente de diferencia simétrica vacía.
- Una sola muestra favorable, igualdad de IDs o igualdad de digest textual no demuestra contención ni igualdad extensional.

### 5.5 Cuantificación

- `FORALL x∈U: P(x)` es TRUE solo si la cobertura de U es completa y cada región está demostrada T; es FALSE si existe witness F; en otro caso UNKNOWN.
- `EXISTS x∈U: P(x)` es TRUE si existe witness T; FALSE solo si U está cerrado/completo y todas las regiones son F; en otro caso UNKNOWN.
- Un dominio infinito/simbólico requiere una prueba exacta del método aprobado; enumeración parcial no prueba universalidad ni inexistencia.

## 6. INVALID frente a UNKNOWN

| Situación | Resultado propuesto |
|---|---|
| Predicado bien formado, fuente temporalmente incompleta | UNKNOWN |
| Alias no resuelto o relación necesaria ausente | UNKNOWN, salvo conflicto de contrato |
| Alias colisionado con identidad contradictoria | UNKNOWN o INVALID según catálogo de identidad aprobado; nunca FALSE |
| Campo obligatorio omitido, tipo incorrecto, unidad prohibida | INVALID |
| Universo/versiones incompatibles para el operador | INVALID si el contrato prohíbe la combinación; UNRESOLVED solo si el resolver admite comparación pendiente |
| Solver timeout, UNKNOWN o certificado ausente | UNKNOWN/UNRESOLVED |
| Certificado malformado o prueba que no verifica | INVALID para el artefacto; resultado de la propiedad no probado |
| Registro declarado completo pero con contradicción interna | INVALID o CONFLICTED según contrato de fuente |

Los catálogos definitivos de reason codes y el mapping a ScopeOperationResult/ValidationEnvelope requieren aprobación. No se debe sobrecargar INVALID para representar una falta de conocimiento.

## 7. Reglas fail-closed para decisiones

1. UNKNOWN nunca se convierte en FALSE, EMPTY, DISJOINT, NOT_AFFECTED, NOT_APPLICABLE, NO_VETO_IDENTIFIED ni ausencia de dependencia.
2. INVALID nunca se convierte en un resultado lógico favorable; bloquea la operación y conserva diagnostics.
3. Un resultado RESOLVED exige que la denotación sea exacta para el universo/version declarados; una denotación parcial debe conservar estado UNRESOLVED.
4. Un `ValidationEnvelope=VALID` exige que todas las obligaciones de cobertura, disjunción, contención, no vacío cuando aplique, homogeneidad y dependency closure estén demostradas.
5. El consumidor debe inspeccionar status, proof references, universe/version, completeness y reason codes; nunca usar solo un payload booleano.
6. Una policy puede decidir bloquear ante UNKNOWN, solicitar revisión o limitar una actividad; esa consecuencia pertenece a policy/enforcement, no altera la semántica matemática de UNKNOWN.

## 8. Escenarios de conformidad propuestos

Estos escenarios amplían X-09. Son especificaciones conceptuales, no fixtures ni tests ejecutables.

| ID | Condición | Resultado esperado |
|---|---|---|
| S-141 | P(x)=U y se evalúa NOT P(x) | U; nunca T ni F |
| S-142 | A(x)=F, B(x)=U; evaluar A∩B | F, con incertidumbre de B preservada en diagnostics si aplica |
| S-143 | A(x)=T, B(x)=U; evaluar A∪B | T para ese punto; no afirmar que la denotación global esté completa |
| S-144 | A(x)=T, B(x)=U; evaluar A\\B | U; no clasificar el punto como incluido ni excluido |
| S-145 | Universo cerrado, todos los puntos de P son F | EMPTY solo con certificado exhaustivo |
| S-146 | No hay witness T y queda una región U | UNRESOLVED; prohibido EMPTY |
| S-147 | Intersección A∩B contiene región U sin witness T/T | No DISJOINT; relación UNRESOLVED |
| S-148 | Existe witness x con A(x)=T y B(x)=T | OVERLAPS demostrado |
| S-149 | Predicado malformado dentro de expresión AND/OR | INVALID estricto; no ocultar por absorción booleana |
| S-150 | EXISTS sobre universo incompleto sin witness T | UNKNOWN, no FALSE |
| S-151 | FORALL sobre universo completo con al menos un witness F | FALSE |

## 9. Invariantes nuevas

- **INV-64 — Unknown preservation:** operadores lógicos y de conjuntos no convierten UNKNOWN en TRUE/FALSE sin prueba adicional.
- **INV-65 — Invalid strictness:** INVALID de una expresión requerida invalida la composición y no se absorbe por valores booleanos.
- **INV-66 — Exact denotation:** ScopeOperationResult=RESOLVED requiere `L(P)=H(P)` sobre universo cerrado/versionado.
- **INV-67 — Empty proof completeness:** EMPTY requiere exhaustividad del universo y ausencia demostrada de cualquier miembro.
- **INV-68 — Disjointness proof completeness:** DISJOINT requiere prueba de ausencia de intersección en todo el universo relevante; UNKNOWN residual bloquea DISJOINT.
- **INV-69 — Quantifier completeness:** FORALL/EXISTS negativos solo se resuelven con cobertura completa o witness suficiente según el cuantificador.

## 10. Obligaciones de prueba

Antes de implementación se deben demostrar o verificar, para cada fragmento de predicados aprobado:

1. Corrección de las tablas de verdad y del comportamiento estricto de INVALID.
2. Conservación de `L(P) ⊆ H(P) ⊆ U`.
3. Corrección de las operaciones punto a punto y su soundness respecto de las extensiones inferior/superior.
4. Condiciones necesarias y suficientes para EMPTY, NON_EMPTY, DISJOINT, OVERLAPS, CONTAINS y EQUAL.
5. Cuantificación correcta sobre universos finitos cerrados y fragmentos simbólicos admitidos.
6. Ningún timeout, incompletitud, conflicto o ausencia de certificado se convierte en prueba negativa/favorable.
7. Correspondencia entre estos resultados y los estados canónicos de ScopeOperationResult y ValidationEnvelope, sin colapsar dominios.

## 11. Límites y cuestiones abiertas

- Debe decidirse si se adopta exactamente la lógica de Kleene fuerte o un formalismo alternativo para ciertos dominios; la regla INVALID estricta es una política de validación propuesta, no un axioma de la lógica.
- Debe aprobarse cuándo un conflicto de fuente es UNKNOWN, CONFLICTED o INVALID y cómo se resuelve por dominio.
- La prueba sobre universos infinitos depende del fragmento decidible y del checker seleccionados.
- Las aproximaciones L/H no sustituyen certificados ni definen por sí solas una estrategia de partición.
- Deben aprobarse reason codes, serialization, proof envelope y mapping de estados.
- Los owners deben revisar si un operador puede devolver una conclusión local T/F mientras el resultado global sigue UNRESOLVED; esta distinción se propone explícitamente.

## 12. Trazabilidad

| Elemento | Relación |
|---|---|
| FSA-01 | Propuesta de resolución; pendiente de revisión independiente |
| X-09 | S-141..S-151; INV-64..INV-69 |
| Doc. 20 | Complementa §§2–5, 9 y 11; no reemplaza sus criterios de aceptación |
| Doc. 19 | Precisa UNKNOWN/UNRESOLVED y resultados de operaciones |
| FSA-02..FSA-05 | Permanecen abiertos y no se consideran resueltos por este documento |

## 13. Estado

**ADDRESSED_IN_DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

FSA-01 queda propuesto para revisión; no cerrado institucionalmente. No se modificaron requisitos IIM, schemas, código ni políticas aprobadas. No se crearon fixtures o tests ejecutables. No se ejecutaron pruebas ni se realizó demostración formal asistida por un proof assistant. La lógica, las reglas de conjunto y los escenarios requieren revisión de especialistas y owners antes de aprobarse.