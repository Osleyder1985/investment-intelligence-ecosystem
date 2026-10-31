# Revisión de consistencia de A13 — Verificación formal del Scope Algebra

**ID:** IIE-METH-SCOPE-FORMAL-REVIEW-015  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Objeto:** revisión documental de la estrategia formal del Scope Algebra (documento 20), su incorporación en X-09 (documento 14) y su trazabilidad (documento 16).  
**No alcance:** no aprueba semántica, solver, lenguaje de predicados, certificados, políticas ni requisitos IIM.

## 1. Dictamen

La estrategia A13 está incorporada documentalmente y representa un avance sustancial: define leyes algebraicas con precondiciones, clases de dominios, certificados candidatos, TCB, tratamiento fail-closed, límites de complejidad, determinismo y niveles de prueba.

**Dictamen de revisión: REVIEW_REQUIRED.** La propuesta no debe considerarse lista para traducirse directamente a contratos o código. Persisten brechas en la semántica de incertidumbre, la demostración de cierre de dependencias, el binding de certificados y la verificación de homogeneidad semántica de la partición.

La revisión es documental y no constituye revisión matemática independiente, auditoría de seguridad ni validación de implementación.

## 2. Hallazgos

### FSA-01 — Semántica formal de pertenencia no resuelta
**Severidad:** HIGH  
**Evidencia:** documento 20 §§2.1–2.3 y 3.4; documento 19 §§3, 5 y 11.

El documento 20 establece que UNRESOLVED no denota un subconjunto conocido del universo, pero no define completamente cómo se evalúa la pertenencia de un punto a un predicado cuando alguna dimensión, alias, relación o dependencia es desconocida. Tampoco formaliza cómo se propaga esa incertidumbre a negación, diferencia, complemento relativo y equivalencia.

**Riesgo:** dos implementaciones podrían tratar de forma distinta una condición desconocida (por ejemplo, NOT P, A \\ B o A = B) y producir resultados divergentes; una implementación incorrecta podría convertir desconocimiento en exclusión demostrada.

**Resolución requerida:**
- definir un resultado tipado de pertenencia, al menos TRUE / FALSE / UNKNOWN / INVALID, o una semántica alternativa formalmente justificada;
- definir tablas de verdad y reglas para AND, OR, NOT, cuantificación y operaciones de conjuntos;
- separar FALSE demostrado de UNKNOWN;
- prohibir que UNKNOWN pruebe EMPTY, DISJOINT, NOT_AFFECTED, cobertura o contención;
- añadir escenarios positivos y negativos para negación, diferencia y complementos con predicados parcialmente conocidos.

### FSA-02 — Certificado de dependency closure insuficientemente especificado
**Severidad:** HIGH  
**Evidencia:** documento 20 §5.1 y §6; documento 19 §8.

El artefacto mínimo propuesto para DEPENDENCY_CLOSURE contiene snapshot de grafo, reglas y edges activados, pero no establece cómo se demuestra que el snapshot representa un universo de dependencias completo para el scope y la consulta, ni cómo se acredita la exhaustividad de la exploración de edges.

**Riesgo:** un conjunto de edges activados puede ser internamente consistente y, aun así, omitir una dependencia relevante. Un hash del snapshot acredita integridad del artefacto, no completitud del grafo.

**Resolución requerida:**
- definir el boundary de consulta, el universo/registro de edges y su versión;
- distinguir grafo completo, parcial, desconocido y conflictivo;
- exigir evidencia de cobertura del registro y de la exploración para el scope consultado;
- emitir UNKNOWN/INCOMPLETE si no se demuestra completitud;
- especificar ciclos, límites de profundidad, paginación, eventos tardíos y cierre transitivo permitido por tipo de edge.

### FSA-03 — Binding y validación de certificados no especificados a nivel suficiente
**Severidad:** HIGH  
**Evidencia:** documento 20 §§5.1–5.3 y 8.

Se requiere un proof_ref inmutable con hash y versión, pero aún no se define el binding obligatorio del certificado a los operandos exactos, sus denotaciones, universe/version, normalization version, predicate-language version, dependency snapshot, algoritmo y configuración de verificación.

**Riesgo:** reutilización de certificados válidos para otros scopes/versiones, sustitución de operandos, replay fuera de contexto o aceptación de pruebas producidas bajo semántica incompatible.

**Resolución requerida:**
- definir un envelope de certificado con referencias/digests de todos los inputs semánticamente relevantes;
- verificar que el checker rechaza certificados con operandos, scope, universo, versión o snapshot distintos;
- definir integridad, autenticidad, protección contra replay y revocación de claves cuando corresponda;
- separar hash/integridad, autenticidad del emisor y validez matemática;
- añadir pruebas de certificado alterado, reusado, truncado, malformado y fuera de versión.

### FSA-04 — La partición formal requiere obligaciones semánticas además de las set-theoretic
**Severidad:** MEDIUM  
**Evidencia:** documento 20 §3.5 y §6; documento 19 §§7–8.

La definición set-theoretic verifica no vacío, contención, disjunción y cobertura. El algoritmo añade homogeneidad y dependencias, pero la matriz de certificados no define pruebas explícitas para homogeneidad del decision context, equivalencia de inputs materiales ni cierre de dependencias que cruzan fronteras.

**Riesgo:** una partición puede ser exacta como conjunto y aun así ser inválida para agregación si mezcla policy versions, finalidad, actividad, snapshots o consecuencias no homogéneas.

**Resolución requerida:**
- convertir homogeneidad contextual, applicability, inputs materiales y dependencias en proof obligations separadas;
- declarar qué dimensiones exigen igualdad exacta y cuáles permiten equivalencia aprobada;
- impedir ValidationEnvelope=VALID si cualquier obligación semántica no está probada;
- conservar los certificados por obligación en el manifest.

### FSA-05 — Cobertura adversarial del verificador incompleta
**Severidad:** MEDIUM  
**Evidencia:** documento 20 §§5, 9.3 y 10; X-09 S-133 y S-140.

La estrategia contempla differential testing y discrepancias, pero no explicita suficientes pruebas contra certificados correctamente formados pero ligados a inputs incorrectos, certificados manipulados, checker con versión incompatible o disagreement en el binding del proof.

**Resolución requerida:**
- ampliar escenarios para certificados falsificados/manipulados, operand mismatch, universe mismatch, replay, versión incompatible y checker failure;
- incluir pruebas de independencia del checker y revisión de su TCB;
- registrar prueba rechazada, reason code, artefactos y replay determinista.

## 3. Hallazgo de trazabilidad corregido

El encabezado del documento 16 reservaba futuros test IDs únicamente hasta S-084 e INV-29, pese a que X-09 v0.7.0-draft contiene escenarios hasta S-140 e invariantes hasta INV-63.

Se actualizó el documento 16 a v0.4.0-draft para reflejar:
- escenarios individuales: TC-X09-S-001 … TC-X09-S-140;
- escenarios combinados: TC-X09-C-01 … TC-X09-C-16;
- invariantes: TC-X09-INV-01 … TC-X09-INV-63.

La corrección armoniza los rangos reservados; no implica que esos tests existan ni que estén implementados o ejecutados.

## 4. Orden de resolución propuesto

1. Resolver FSA-01: semántica formal de incertidumbre y operadores.
2. Resolver FSA-02: completitud del universo de dependencias y certificados de closure.
3. Resolver FSA-03: envelope, binding y checker de certificados.
4. Resolver FSA-04: obligaciones semánticas de partición y gate ValidationEnvelope.
5. Resolver FSA-05: pruebas adversariales del verificador.
6. Actualizar X-09 escenarios/invariantes y crosswalks únicamente después de acordar expected outcomes.
7. Someter los cambios a revisión de Architecture, Security, Risk, Operations y Requirement Owners.

## 5. Estado y límites

**ADDRESSED_IN_DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

La corrección de rangos de trazabilidad fue documental. Los hallazgos FSA-01..FSA-05 permanecen abiertos. No se modificaron requisitos IIM, schemas, código, fixtures ni políticas aprobadas. No se ejecutaron pruebas matemáticas, property-based, differential, adversariales, benchmarks ni validación empírica.