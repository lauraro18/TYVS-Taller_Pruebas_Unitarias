*Nota mía: ya corrí tanto `mvn clean verify` como el comando de PIT, así que todo lo de esta página son números reales de mi máquina, no estimaciones.*

Para generar los reportes, desde `registraduria/`:

```sh
mvn clean verify
# el reporte queda en registraduria/target/site/jacoco/index.html

mvn test-compile org.pitest:pitest-maven:mutationCoverage
# el reporte queda en registraduria/target/pit-reports/index.html
```

## Cobertura (JaCoCo)

Corrí `mvn clean verify` y las 22 pruebas pasan (14 de `RegistryTest`, 8 de `RegistryPropertiesTest`), sin ningún fallo. El reporte de `target/site/jacoco/index.html` da esto por clase:

| Clase | Instrucciones | Ramas | Líneas | Métodos |
|---|---|---|---|---|
| `Registry` | 100% (56/56) | 100% (14/14) | 100% (16/16) | 100% (2/2) |
| `Gender` | 100% | — | 100% | 100% |
| `RegisterResult` | 100% | — | 100% | 100% |
| `Person` | 81.8% (27/33) | — | 83.3% (10/12) | 66.7% (4/6) |

`Registry` quedó totalmente cubierto: las 7 reglas más las 2 pruebas de orden de evaluación pasan por cada `if`/`return` del método, incluidas las 14 ramas. El único hueco real está en `Person`, y es justo el que esperaba: `registerVoter` solo llama a `getId()`, `getAge()` e `isAlive()`, así que `getName()` y `getGender()` (2 de sus 6 métodos) nunca se ejecutan en ninguna prueba, porque ninguna regla de negocio los necesita.

- Cobertura global (instrucciones): **96%** (143/149)
- Cobertura global (líneas): **95%** (35/37)
- Muy por encima del 80% que pide el taller.

## Mutación (PIT)

El ejemplo del enunciado (con el código de la iteración 2, mucho más incompleto) mostraba mutantes sobrevivientes en `getAge()`, `getId()`, `getName()` y `getGender()`. Corriendo PIT sobre mi versión completa, el resultado fue **92% de mutation score global (22 de 24 mutantes eliminados)** — muy por encima del umbral de 60% que exige el `pom.xml`. Por paquete:

| Paquete | Mutation score |
|---|---|
| `domain.service` (`Registry`) | 100% (18/18) |
| `domain.model` (`Person`, `Gender`, `RegisterResult`) | 67% (4/6) |

Tal como esperaba, todos los mutantes sobre `Registry` murieron: al cambiar cualquier `<` por `<=`, invertir un `if` o reemplazar un `return` por `null`, alguna de mis pruebas se dio cuenta y falló. También murieron los mutantes sobre `getAge()`, `getId()` e `isAlive()` de `Person`, porque `Registry` sí usa esos tres valores para decidir el resultado.

Los dos mutantes que sobrevivieron son exactamente los que había anticipado:

```text
Person.getGender() -> reemplazar el retorno por null   (línea 31, NO_COVERAGE)
Person.getName()   -> reemplazar el retorno por ""     (línea 19, NO_COVERAGE)
```

Ambos figuran como `NO_COVERAGE` en el reporte — ni siquiera llegan a ejecutarse en ninguna prueba, porque `registerVoter` nunca llama a esos dos getters. La prueba que mataría al de `getGender()`, por ejemplo, sería algo así en una clase `PersonTest` aparte (prueba el modelo, no el servicio, así que no la mezclé con `RegistryTest`):

```java
@Test
void shouldExposeTheGenderItWasConstructedWith() {
    Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);
    Gender result = person.getGender();
    assertEquals(Gender.FEMALE, result);
}
```

No la agregué al proyecto final porque no verifica ninguna regla de negocio, solo el getter en sí — pero queda anotada como algo que valdría la pena si el proyecto creciera.

## Cobertura vs. mutación

En el ejemplo del enunciado, con solo dos iteraciones implementadas, la cobertura daba 89% pero la mutación solo 64% — una diferencia grande, señal de que había pruebas que ejecutaban código sin verificar nada. En mi versión esa brecha prácticamente desaparece: 96% de cobertura por instrucciones contra 92% de mutation score, una diferencia de apenas 4 puntos. Eso me dice que casi todo lo que mis pruebas ejecutan también lo están comparando contra un resultado esperado — el hueco que queda (los dos mutantes de `getName()`/`getGender()`) es exactamente el mismo hueco que ya había identificado en la cobertura, no algo nuevo que apareciera solo con la mutación.
