# Registro de Defectos — Registraduría

Este documento recopila los defectos encontrados durante el desarrollo guiado por pruebas (TDD) del proyecto **Registraduría**, al continuar el código a partir del estado en que lo deja el README al terminar la **iteración 2** (solo validaba `person == null` y `alive`).

Cada defecto se detectó exactamente de la forma en que TDD lo espera: se escribió primero la prueba (o la propiedad) que describía la regla de negocio faltante, se ejecutó `mvn test`, y falló contra el `Registry` de la iteración 2. Esa falla es el defecto documentado abajo. Después se implementó la regla correspondiente y el defecto quedó resuelto.

---

## Formato: Lista detallada (narrativa)

### Defecto 01 — id no positivo aceptado

- **Caso de prueba**: `shouldRejectWhenIdIsZero` / `shouldRejectWhenIdIsNegative` (regla de negocio R2).
- **Entrada**: `Person("Luis", id=0, age=25, gender=MALE, alive=true)` (y equivalente con `id=-5`).
- **Resultado esperado**: `INVALID`.
- **Resultado obtenido** (código de la iteración 2): `VALID`.
- **Causa probable**: `Registry.registerVoter` no validaba el número de documento; solo comprobaba `null` y `alive`, así que cualquier `id`, incluido `0` o negativo, llegaba directo a `VALID`.
- **Estado**: **Resuelto** — se agregó la guarda `if (p.getId() < MIN_ID) return RegisterResult.INVALID;` (iteración 3) y quedó cubierto por `shouldRejectWhenIdIsZero`, `shouldRejectWhenIdIsNegative` y la propiedad `idNoPositivoSiempreEsInvalido`.

---

### Defecto 02 — edades biológicamente imposibles aceptadas

- **Caso de prueba**: `shouldRejectInvalidAgeUnderZero` / `shouldRejectInvalidAgeOver120` (regla de negocio R4).
- **Entrada**: `Person("Juan", id=10, age=-1, gender=MALE, alive=true)` y `Person("Juan", id=11, age=121, gender=MALE, alive=true)`.
- **Resultado esperado**: `INVALID_AGE` en ambos casos.
- **Resultado obtenido** (código de la iteración 2): `VALID` en ambos casos.
- **Causa probable**: no existía ninguna validación sobre el rango de edad; el enum `RegisterResult` ni siquiera tenía el valor `INVALID_AGE` todavía (error de compilación al escribir la prueba, evidencia clásica de "rojo" en TDD).
- **Estado**: **Resuelto** — se agregó `INVALID_AGE` al enum y la guarda `if (p.getAge() < MIN_AGE || p.getAge() > MAX_AGE) return RegisterResult.INVALID_AGE;` (iteración 4).

---

### Defecto 03 — menores de edad registrados como votantes válidos

- **Caso de prueba**: `shouldRejectUnderageAt17` (regla de negocio R5).
- **Entrada**: `Person("Sofía", id=13, age=17, gender=FEMALE, alive=true)`.
- **Resultado esperado**: `UNDERAGE`.
- **Resultado obtenido** (código de la iteración 2): `VALID`.
- **Causa probable**: `Registry` no comprobaba la mayoría de edad; una persona de 17 años, viva y con id "válido" en ese momento, pasaba a `VALID` sin restricción. Este es el defecto más delicado porque es un **error de negocio real**: permitiría inscribir votantes menores de edad.
- **Estado**: **Resuelto** — se agregó la guarda `if (p.getAge() < MIN_VOTING_AGE) return RegisterResult.UNDERAGE;` (iteración 5), verificada además con la propiedad `todoMenorDeEdadEsRechazado` sobre todo el rango 0–17.

---

### Defecto 04 — registros duplicados por documento

- **Caso de prueba**: `shouldRejectDuplicatedId` (regla de negocio R6).
- **Entradas**:
  - Persona 1: `Person("Carlos", id=200, age=30, gender=MALE, alive=true)`.
  - Persona 2: `Person("Carla", id=200, age=25, gender=FEMALE, alive=true)`.
- **Resultado esperado**: Persona 1 → `VALID`; Persona 2 → `DUPLICATED`.
- **Resultado obtenido** (código de la iteración 2): Persona 1 → `VALID`; Persona 2 → `VALID`.
- **Causa probable**: `Registry` no tenía memoria de los `id` ya registrados (no mantenía ningún estado entre llamadas), así que dos personas con el mismo documento quedaban ambas como `VALID`.
- **Estado**: **Resuelto** — se agregó el campo de instancia `Set<Integer> registeredIds` y la guarda de duplicados (iteración 6), verificada también con la propiedad `elMismoIdRegistradoDosVecesSiempreEsDuplicadoLaSegundaVez`.
  - ⚠️ Nota de diseño registrada durante la corrección: al volver `Registry` un objeto **con estado**, sus pruebas dejan de ser independientes si comparten instancia. El `@BeforeEach` que crea un `Registry` nuevo en cada prueba (heredado de la iteración 2) es lo que evita que este defecto se vuelva a introducir de forma silenciosa por contaminación entre pruebas.

---

## Formato: Tabla de defectos (bug tracking)

| ID | Caso de Prueba | Entrada | Resultado Esperado | Resultado Obtenido (iteración 2) | Causa Probable | Estado |
|----|-----------------|---------|---------------------|-----------------------------------|-----------------|--------|
| 01 | id no positivo | `Person(id=0, age=25, alive=true)` | `INVALID` | `VALID` | No se validaba `id` | Resuelto (iteración 3) |
| 02 | Edad fuera de rango | `Person(id=10, age=-1)` / `Person(id=11, age=121)` | `INVALID_AGE` | `VALID` | No existía validación de rango de edad ni el valor `INVALID_AGE` | Resuelto (iteración 4) |
| 03 | Menor de edad | `Person(id=13, age=17, alive=true)` | `UNDERAGE` | `VALID` | No se validaba mayoría de edad | Resuelto (iteración 5) |
| 04 | Registro duplicado | `Person(id=200)` registrado dos veces | 1º `VALID`, 2º `DUPLICATED` | 1º `VALID`, 2º `VALID` | `Registry` no tenía memoria de ids ya usados | Resuelto (iteración 6) |

---

## Convenciones de Estado

| Estado | Significado |
|---------|-------------|
| **Abierto** | El defecto fue detectado pero no corregido. |
| **En progreso** | El defecto se encuentra en análisis o corrección. |
| **Resuelto** | El defecto fue corregido y validado mediante pruebas. |

## Observaciones

- Los cuatro defectos de este archivo corresponden exactamente a los "mutantes sobrevivientes" que PIT reportaría contra el `Registry` de la iteración 2: código que se ejecuta (y que JaCoCo cuenta como cubierto por las pruebas de la iteración 1-2) pero cuyo comportamiento real no estaba verificado por ninguna prueba.
- Si al ejecutar `mvn test-compile org.pitest:pitest-maven:mutationCoverage` sobre el proyecto final aparece algún mutante sobreviviente adicional, se documenta aquí como un nuevo defecto (ver sección "Reflexión" de la Wiki para el análisis de mutantes).
