# Clases de Equivalencia, Valores Límite y Escenarios BDD

## Matriz de clases de equivalencia y valores límite

El espacio de entradas de `registerVoter(Person)` se particiona por atributo. Para cada clase se eligió al menos un representante y, donde aplica, los dos valores del borde.

| Atributo | Clase de equivalencia | Valor(es) límite | Resultado esperado |
|---|---|---|---|
| Nulidad | `person == null` | — | `INVALID` |
| Identificador | `id ≤ 0` (inválido) | `0`, `-1`, `-5` | `INVALID` |
| Identificador | `id > 0` y no registrado (único) | `1` | continúa evaluación |
| Identificador | `id > 0` y ya registrado (duplicado) | mismo id dos veces | `DUPLICATED` (en la 2ª inscripción) |
| Estado de vida | `alive = false` | — (independiente de la edad) | `DEAD` |
| Estado de vida | `alive = true` | — | continúa evaluación |
| Edad | `edad < 0` (inválida, borde inferior) | `-1` | `INVALID_AGE` |
| Edad | `0 ≤ edad < 18` (menor) | `0`, `17` | `UNDERAGE` |
| Edad | `18 ≤ edad ≤ 120` (válida) | `18`, `120` | contribuye a `VALID` |
| Edad | `edad > 120` (inválida, borde superior) | `121` | `INVALID_AGE` |

Son **10 clases de equivalencia** (más del mínimo de 5 exigido), cada una con su prueba dedicada en `RegistryTest` y, para las cuatro reglas centrales (id, vida, edad, mayoría de edad), también con una propiedad en `RegistryPropertiesTest` que cubre el rango completo, no solo el representante.

## Trazabilidad: regla de negocio → prueba JUnit → propiedad jqwik → escenario BDD

| Regla | Prueba JUnit (`RegistryTest`) | Propiedad jqwik (`RegistryPropertiesTest`) | Escenario BDD (Given–When–Then) |
|---|---|---|---|
| R1 | `shouldReturnInvalidWhenPersonIsNull` | *(cubierta por inspección: null no es representable como `@ForAll Person`)* | **Given** la persona es `null`; **When** intento registrarla; **Then** el resultado debe ser `INVALID` |
| R2 | `shouldRejectWhenIdIsZero`, `shouldRejectWhenIdIsNegative` | `idNoPositivoSiempreEsInvalido` | **Given** la persona tiene `id = 0` (o `id = -5`), edad 25 y está viva; **When** intento registrarla; **Then** el resultado debe ser `INVALID` |
| R3 | `shouldRejectDeadPerson` | `unaPersonaNoVivaSiempreEsRechazada` | **Given** la persona no está viva; **When** intento registrarla; **Then** el resultado debe ser `DEAD`, sin importar su edad, id o género |
| R4 (inferior) | `shouldRejectInvalidAgeUnderZero` | *(cubierta por `nuncaDevuelveNullNiLanzaExcepcion`, que usa el rango completo de `int`)* | **Given** la persona tiene edad -1, está viva y su id es válido; **When** intento registrarla; **Then** el resultado debe ser `INVALID_AGE` |
| R4 (superior) | `shouldRejectInvalidAgeOver120`, `shouldAcceptMaxAge120` | *(idem)* | **Given** la persona tiene edad 121 (o 120), está viva y su id es válido; **When** intento registrarla; **Then** el resultado debe ser `INVALID_AGE` (o `VALID` en 120) |
| R5 | `shouldRejectUnderageAt17`, `shouldAcceptAdultAt18` | `todoMenorDeEdadEsRechazado`, `todoAdultoValidoSeRegistra` | **Given** la persona tiene 17 años, está viva y su id es válido; **When** intento registrarla; **Then** el resultado debe ser `UNDERAGE` |
| R6 | `shouldRejectDuplicatedId`, `shouldAcceptUniqueId` | `elMismoIdRegistradoDosVecesSiempreEsDuplicadoLaSegundaVez` | **Given** el id 200 ya fue registrado antes; **When** intento registrar otra persona con id 200; **Then** el resultado debe ser `DUPLICATED` |
| R7 | `shouldRegisterValidPerson` | `todoAdultoValidoSeRegistra` (implícita) | **Given** la persona está viva, tiene 30 años y un id único; **When** intento registrarla; **Then** el resultado debe ser `VALID` |
| Orden R2 > R3 | `invalidIdRuleTakesPriorityOverDeadRule` | — | **Given** la persona tiene id inválido y además no está viva; **When** intento registrarla; **Then** el resultado debe ser `INVALID`, no `DEAD` |
| Orden R3 > R5 | `deadRuleTakesPriorityOverUnderageRule` | — | **Given** la persona no está viva y además es menor de edad; **When** intento registrarla; **Then** el resultado debe ser `DEAD`, no `UNDERAGE` |
| Estructural: determinismo | — | `elResultadoNoDependeDeLaInstancia` | **Given** una persona cualquiera; **When** la registro en dos `Registry` recién creados; **Then** ambos resultados deben ser iguales |
| Estructural: totalidad | — | `nuncaDevuelveNullNiLanzaExcepcion` | **Given** cualquier combinación de datos, incluidos extremos de `int`; **When** intento registrar; **Then** nunca se obtiene `null` ni una excepción |
| Estructural: partición | — | `elResultadoSiempreCaeEnUnaParticionConocida` | **Given** cualquier combinación de datos; **When** intento registrar; **Then** el resultado pertenece siempre al conjunto conocido de valores de `RegisterResult` |

## Por qué estos bordes y no otros

- **17 y 18** son los únicos valores donde el resultado cambia de `UNDERAGE` a `VALID`; cualquier otro par de edades adyacentes se comporta igual dentro de su clase.
- **0 y 120** son los límites biológicos declarados en el enunciado (R4); **-1 y 121** son los primeros valores fuera de ese rango en cada dirección.
- **0 y -5** para el id cubren tanto el borde exacto (`0`) como un valor claramente negativo, para asegurarse de que la condición es `< MIN_ID` y no, por ejemplo, un error de signo que solo se manifieste con negativos "grandes".
- El **orden de evaluación** (R1→R7) no es una clase de equivalencia sobre un solo atributo, sino sobre la *interacción* entre atributos; por eso se documenta con pruebas dedicadas (`deadRuleTakesPriorityOverUnderageRule`, `invalidIdRuleTakesPriorityOverDeadRule`) en vez de dejarlo implícito.
