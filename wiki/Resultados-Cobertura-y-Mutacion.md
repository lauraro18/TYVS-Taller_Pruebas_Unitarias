# Resultados: Cobertura (JaCoCo) y Mutación (PIT)

> ⚠️ **Nota de honestidad metodológica**: los números de esta página son una **predicción justificada por inspección estática del código** (contando líneas y ramas a mano), no una medición real — el entorno donde se preparó este proyecto no tenía salida a Maven Central para ejecutar `mvn verify` ni PIT. **Antes de entregar, debes correr los comandos de abajo tú mismo, reemplazar los números y pegar las capturas reales.** Esto es, además, exactamente el tipo de verificación que pide el punto 4 de "Para entregar" del README.

## Cómo generar los reportes reales

Desde `registraduria/`:

```sh
mvn clean verify
# abre registraduria/target/site/jacoco/index.html

mvn test-compile org.pitest:pitest-maven:mutationCoverage
# abre registraduria/target/pit-reports/index.html
```

## Predicción (a confirmar) — Cobertura JaCoCo

| Clase | Líneas cubiertas (predicción) | Justificación |
|---|---|---|
| `Registry` | ~100% | Las 7 reglas (R1–R7) y las 2 pruebas de orden de evaluación ejercitan cada `if`/`return` del método. No queda ninguna rama sin al menos una prueba que la fuerce. |
| `Person` | ~parcial (faltan `getName()` y `getGender()`) | `Registry.registerVoter` solo llama a `getId()`, `getAge()` e `isAlive()`. Ningún test llama nunca a `getName()` ni a `getGender()` porque ninguna regla de negocio los necesita — ver más abajo. |
| `RegisterResult`, `Gender` | 100% | Enums simples; todos sus valores aparecen referenciados en las pruebas. |

**Global esperado: por encima del 80%** exigido, con el único hueco identificable en `Person.getName()` / `Person.getGender()`.

**Captura real (reemplaza esta línea con tu captura de `target/site/jacoco/index.html`):**

`[ TODO: pega aquí tu captura de pantalla de JaCoCo ]`

- **Cobertura global real:** `___%`
- **Cobertura del paquete `domain`:** `___%`
- **Líneas sin cubrir y por qué:** _(completa aquí si aparece algo distinto a lo predicho arriba)_

## Predicción (a confirmar) — Mutation score PIT

Igual que en el ejemplo del README (donde `Person.getAge()`, `getId()`, `getName()` y `getGender()` sobrevivían contra el `Registry` de la iteración 2), aquí el análisis cambia porque `Registry` ya consulta `getAge()`, `getId()` e `isAlive()` para tomar decisiones — así que los mutantes sobre esos tres getters **deberían morir** ahora (si PIT cambia el retorno de `getAge()` por `0`, por ejemplo, rompe `shouldRejectUnderageAt17` o `shouldAcceptAdultAt18`).

Lo que **no** cambia es `getName()` y `getGender()`: como ninguna regla de negocio los usa, cualquier mutante sobre ellos (reemplazar el retorno por `""` o por `null`) sigue sin tener ninguna prueba que lo note. Es la predicción más específica y verificable de esta página.

**Captura real (reemplaza esta línea con tu captura de `target/pit-reports/index.html`):**

`[ TODO: pega aquí tu captura de pantalla de PIT ]`

- **Mutation score real:** `___%` (umbral configurado en el `pom.xml`: 60%; si baja de eso, el build falla)
- **Mutantes sobrevivientes:** `___` de `___`

### Análisis de un mutante sobreviviente

Con base en la predicción de arriba, el mutante candidato a analizar es:

```text
Person.getGender() -> reemplazar el retorno por null
```

- **¿Por qué sobrevive?** Ninguna prueba de `RegistryTest` ni de `RegistryPropertiesTest` verifica el género de la persona registrada; `Registry.registerVoter` nunca lee `p.getGender()`. El mutante cambia el comportamiento de un método real del dominio sin que ninguna prueba se entere.
- **¿Qué prueba lo mataría?** Una prueba directa sobre `Person`, por ejemplo:
  ```java
  @Test
  void shouldExposeTheGenderItWasConstructedWith() {
      // Arrange
      Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);

      // Act
      Gender result = person.getGender();

      // Assert
      assertEquals(Gender.FEMALE, result);
  }
  ```
  Esta prueba pertenecería a una clase `PersonTest` (no existe todavía en el proyecto) porque verifica el modelo `Person`, no el servicio `Registry`.
- **¿Vale la pena matarlo?** Aquí hay una decisión de diseño legítima para discutir en la [Reflexión final](Reflexion-Final): `getName()` y `getGender()` existen porque el modelo de dominio los necesita conceptualmente (una persona tiene nombre y género), aunque la regla de negocio actual (`registerVoter`) no los use. Escribir un `PersonTest` que verifique los getters básicos de un objeto inmutable **sí vale la pena** — es barato y documenta el contrato del modelo — pero es un tipo de prueba distinto (prueba del modelo, no de la regla de negocio) y por eso no aparecía en `RegistryTest`.

## Comparación cobertura vs. mutación (para completar con datos reales)

| Métrica | Predicción | Valor real medido |
|---|---|---|
| Cobertura JaCoCo (global) | > 80% | `___%` |
| Mutation score PIT | probablemente > 80% también (a diferencia del ejemplo del README con solo 2 iteraciones) | `___%` |

Si al medir la diferencia entre ambos números resulta pequeña (a diferencia del 89% vs. 64% del ejemplo del README con solo dos iteraciones implementadas), eso es evidencia de que las pruebas de este proyecto no solo *ejecutan* el código sino que *verifican* su comportamiento — que es exactamente lo que el ejercicio de mutación buscaba demostrar. Si la diferencia es grande, es señal de que hay pruebas que "tocan" código sin realmente comprobar su resultado (coverage theater), y toca revisarlas.
