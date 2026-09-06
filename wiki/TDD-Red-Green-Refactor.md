# Historia TDD: Red → Green → Refactor

El README documenta paso a paso las **iteraciones 1 y 2** (camino feliz y persona muerta). A partir de ahí, este proyecto continúa con las iteraciones 3 a 6, una por cada regla de negocio que faltaba (`R2`, `R4`, `R5`, `R6`). Cada iteración siguió el mismo ciclo estricto: escribir la prueba que falla, implementar lo mínimo para que pase, y solo entonces refactorizar.

---

## Iteración 3 — id no positivo (R2)

### 🔴 Red

Se agregaron dos pruebas a `RegistryTest`:

```java
@Test
void shouldRejectWhenIdIsZero() {
    Person person = new Person("Luis", 0, 25, Gender.MALE, true);
    RegisterResult result = registry.registerVoter(person);
    assertEquals(RegisterResult.INVALID, result);
}

@Test
void shouldRejectWhenIdIsNegative() {
    Person person = new Person("Luis", -5, 25, Gender.MALE, true);
    RegisterResult result = registry.registerVoter(person);
    assertEquals(RegisterResult.INVALID, result);
}
```

Al ejecutar `mvn test`, ambas fallan: el `Registry` de la iteración 2 devuelve `VALID` para cualquier id, porque nunca lo valida. Documentado como **Defecto 01** en [`defectos.md`](../blob/main/defectos.md).

### 🟢 Green

Implementación mínima: una guarda adicional, evaluada **antes** que la de `alive` (así lo exige el orden R1→R7 del README):

```java
if (p.getId() < 1) {
    return RegisterResult.INVALID;
}
```

### 🔵 Refactor

Se extrajo el literal `1` a una constante con nombre, para que la regla se lea como especificación:

```java
static final int MIN_ID = 1;
...
if (p.getId() < MIN_ID) {
    return RegisterResult.INVALID;
}
```

Las pruebas siguen en verde.

---

## Iteración 4 — edad biológicamente imposible (R4)

### 🔴 Red

```java
@Test
void shouldRejectInvalidAgeUnderZero() {
    Person person = new Person("Juan", 10, -1, Gender.MALE, true);
    RegisterResult result = registry.registerVoter(person);
    assertEquals(RegisterResult.INVALID_AGE, result);
}
```

Esta prueba **no compila**: `RegisterResult.INVALID_AGE` todavía no existe. Un error de compilación también cuenta como rojo — es la señal de que el dominio necesita crecer (mismo patrón que la iteración 2 del README con `DEAD`).

### 🟢 Green

Primero se agrega la constante que la prueba exige:

```java
public enum RegisterResult { VALID, DUPLICATED, INVALID, DEAD, INVALID_AGE }
```

Y luego la regla, evaluada después de `alive` pero antes de la mayoría de edad:

```java
if (p.getAge() < 0 || p.getAge() > 120) {
    return RegisterResult.INVALID_AGE;
}
```

Se agrega también la prueba del borde superior (`shouldRejectInvalidAgeOver120`, edad 121) y la del límite válido (`shouldAcceptMaxAge120`, edad 120) para fijar exactamente dónde está la frontera.

### 🔵 Refactor

Se extraen los límites a constantes:

```java
static final int MIN_AGE = 0;
static final int MAX_AGE = 120;
...
if (p.getAge() < MIN_AGE || p.getAge() > MAX_AGE) {
    return RegisterResult.INVALID_AGE;
}
```

Documentado como **Defecto 02** en `defectos.md`.

---

## Iteración 5 — mayoría de edad (R5)

### 🔴 Red

```java
@Test
void shouldRejectUnderageAt17() {
    Person person = new Person("Sofía", 13, 17, Gender.FEMALE, true);
    RegisterResult result = registry.registerVoter(person);
    assertEquals(RegisterResult.UNDERAGE, result);
}
```

Falla: la iteración anterior solo descarta edades *imposibles* (`<0` o `>120`); 17 años es una edad válida biológicamente, así que cae directo en `VALID`. Documentado como **Defecto 03**.

### 🟢 Green

```java
public enum RegisterResult { VALID, DUPLICATED, INVALID, DEAD, UNDERAGE, INVALID_AGE }
```

```java
if (p.getAge() < 18) {
    return RegisterResult.UNDERAGE;
}
```

Se agrega `shouldAcceptAdultAt18` para fijar el otro lado del borde (18 años → `VALID`).

### 🔵 Refactor

```java
static final int MIN_VOTING_AGE = 18;
...
if (p.getAge() < MIN_VOTING_AGE) {
    return RegisterResult.UNDERAGE;
}
```

Refactor adicional: se reordenan visualmente los `if` para que coincidan exactamente con la tabla R1→R7 del README, de arriba hacia abajo, de modo que el código se lea como la especificación.

---

## Iteración 6 — duplicados (R6)

### 🔴 Red

```java
@Test
void shouldRejectDuplicatedId() {
    Person first = new Person("Carlos", 200, 30, Gender.MALE, true);
    Person second = new Person("Carla", 200, 25, Gender.FEMALE, true);

    RegisterResult firstResult = registry.registerVoter(first);
    RegisterResult secondResult = registry.registerVoter(second);

    assertEquals(RegisterResult.VALID, firstResult);
    assertEquals(RegisterResult.DUPLICATED, secondResult);
}
```

Falla: `Registry` no tiene memoria de qué ids ya se registraron, así que la segunda persona también obtiene `VALID`. Documentado como **Defecto 04**.

### 🟢 Green

```java
private final Set<Integer> registeredIds = new HashSet<>();
...
if (registeredIds.contains(p.getId())) {
    return RegisterResult.DUPLICATED;
}
registeredIds.add(p.getId());
return RegisterResult.VALID;
```

### 🔵 Refactor

Aquí el refactor más importante **no fue sobre el código de producción sino sobre el diseño de las pruebas**: al volver `Registry` un objeto con estado, dos pruebas que compartieran la misma instancia contaminarían sus resultados según el orden de ejecución. El `@BeforeEach` heredado de la iteración 2 (`registry = new Registry();`) ya resuelve esto — cada prueba recibe una instancia limpia — pero solo al llegar a esta iteración se vuelve evidente *por qué* ese refactor de la iteración 2 no era opcional.

Se agregó además `shouldAcceptUniqueId` como contraparte explícita: un id nunca antes visto debe dar `VALID`, no confundirse con `DUPLICATED`.

---

## Resumen de las 4 iteraciones propias

| Iteración | Regla | Prueba(s) clave | Constante extraída |
|---|---|---|---|
| 3 | R2 — id no positivo | `shouldRejectWhenIdIsZero`, `shouldRejectWhenIdIsNegative` | `MIN_ID` |
| 4 | R4 — edad imposible | `shouldRejectInvalidAgeUnderZero`, `shouldRejectInvalidAgeOver120`, `shouldAcceptMaxAge120` | `MIN_AGE`, `MAX_AGE` |
| 5 | R5 — mayoría de edad | `shouldRejectUnderageAt17`, `shouldAcceptAdultAt18` | `MIN_VOTING_AGE` |
| 6 | R6 — duplicados | `shouldRejectDuplicatedId`, `shouldAcceptUniqueId` | (estado: `registeredIds`) |

Además se agregaron dos pruebas de **orden de evaluación** (`deadRuleTakesPriorityOverUnderageRule`, `invalidIdRuleTakesPriorityOverDeadRule`) que documentan explícitamente, con un caso ejecutable, la decisión de diseño "la primera regla que falla en el orden R1→R7 gana" — que de otra forma solo quedaría escrita en un comentario.
