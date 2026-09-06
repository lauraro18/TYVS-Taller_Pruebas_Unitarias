# Patrón AAA (Arrange – Act – Assert)

Todas las pruebas de `RegistryTest` y `RegistryPropertiesTest` siguen el patrón **AAA**, separando explícitamente qué se prepara, qué se ejecuta y qué se verifica.

## Ejemplo completo

```java
@Test
@DisplayName("Given una persona de 17 años viva y con id válido, When se registra, Then el resultado es UNDERAGE")
void shouldRejectUnderageAt17() {
    // Arrange: preparar los datos y el objeto a probar
    Person person = new Person("Sofía", 13, 17, Gender.FEMALE, true);

    // Act: ejecutar la acción que queremos probar
    RegisterResult result = registry.registerVoter(person);

    // Assert: verificar el resultado esperado
    assertEquals(RegisterResult.UNDERAGE, result);
}
```

- **Arrange**: se construye la `Person` con los datos exactos que necesita el caso (17 años, viva, id válido) y se usa el `registry` ya inicializado en `@BeforeEach`.
- **Act**: una sola línea, la invocación del método bajo prueba. Nunca se mezcla con la preparación ni con la verificación.
- **Assert**: una única aserción por prueba (salvo el caso de duplicados, donde se necesitan dos aserciones porque el escenario tiene dos pasos: registrar la primera persona y luego la segunda).

## Pautas aplicadas en todo el proyecto

1. **Un `@BeforeEach` para eliminar repetición.** `registry = new Registry();` se ejecuta antes de cada prueba, así el bloque *Arrange* de cada test solo contiene lo que es específico de ese caso (la `Person`), no la construcción repetida del objeto bajo prueba.
2. **Nombres de prueba autoexplicativos**, con el patrón `should<Resultado>When<Condición>` (`shouldRejectUnderageAt17`, `shouldAcceptAdultAt18`, `shouldRejectDuplicatedId`). El nombre por sí solo comunica la regla de negocio sin necesidad de leer el cuerpo.
3. **`@DisplayName` en formato Given–When–Then** sobre cada prueba, para que el reporte de ejecución (`mvn test`) se lea como la especificación de negocio, no como jerga de programación. Esto conecta directamente el patrón AAA con la sección de [BDD](Clases-de-Equivalencia-y-BDD).
4. **Una sola razón para fallar por prueba.** Cada prueba ejercita una sola regla de negocio (excepto las dos pruebas de "orden de evaluación", que existen precisamente para documentar cómo interactúan dos reglas).
5. **Sin lógica condicional dentro de la prueba.** Ninguna prueba tiene `if`, bucles ni ramas: eso mantiene la fase *Assert* trivial de leer y verificar a simple vista.

## Por qué importa

Un test AAA se lee de arriba hacia abajo como una mini historia: "dado esto, cuando pasa esto, entonces se espera esto". Cuando una prueba falla, el bloque *Arrange* dice inmediatamente qué entrada se usó, y el *Assert* dice qué se esperaba — no hay que rastrear variables mutadas en medio del método para entender el fallo.
