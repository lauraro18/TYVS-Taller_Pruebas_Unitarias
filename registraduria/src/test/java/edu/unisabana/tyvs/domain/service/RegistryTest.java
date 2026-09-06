package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Gender;
import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas por EJEMPLO del dominio: cada prueba fija una entrada concreta y su
 * resultado esperado, siguiendo el patrón AAA (Arrange-Act-Assert).
 *
 * <p>La tabla de clases de equivalencia y valores límite, junto con los
 * escenarios BDD (Given-When-Then) equivalentes a cada prueba, está
 * documentada en la Wiki del repositorio (página "Clases de Equivalencia y
 * BDD").</p>
 *
 * <p>Complemento: {@link RegistryPropertiesTest} expresa las mismas reglas
 * como PROPIEDADES sobre rangos completos de entradas, en vez de ejemplos
 * sueltos.</p>
 */
class RegistryTest {

    private Registry registry;

    /**
     * Un Registry NUEVO antes de cada prueba.
     *
     * Importante: Registry guarda estado (los ids ya registrados, ver R6).
     * Si dos pruebas compartieran la misma instancia, la primera podria
     * "ensuciar" a la siguiente y los resultados dependerian del orden de
     * ejecucion. Cada prueba debe ser independiente.
     */
    @BeforeEach
    void setUp() {
        registry = new Registry();
    }

    // ---------------------------------------------------------------
    // R7 - camino feliz
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given una persona viva, mayor de edad y con id único, When se registra, Then el resultado es VALID")
    void shouldRegisterValidPerson() {
        // Arrange
        Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

    // ---------------------------------------------------------------
    // R1 - persona nula
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given que la persona es null, When se registra, Then el resultado es INVALID")
    void shouldReturnInvalidWhenPersonIsNull() {
        // Act
        RegisterResult result = registry.registerVoter(null);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

    // ---------------------------------------------------------------
    // R2 - id no positivo (clase inválida: id <= 0; bordes 0 y -5)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given una persona con id = 0, When se registra, Then el resultado es INVALID")
    void shouldRejectWhenIdIsZero() {
        // Arrange
        Person person = new Person("Luis", 0, 25, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

    @Test
    @DisplayName("Given una persona con id negativo, When se registra, Then el resultado es INVALID")
    void shouldRejectWhenIdIsNegative() {
        // Arrange
        Person person = new Person("Luis", -5, 25, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

    // ---------------------------------------------------------------
    // R3 - persona no viva
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given una persona no viva, When se registra, Then el resultado es DEAD")
    void shouldRejectDeadPerson() {
        // Arrange
        Person dead = new Person("Carlos", 2, 40, Gender.MALE, false);

        // Act
        RegisterResult result = registry.registerVoter(dead);

        // Assert
        assertEquals(RegisterResult.DEAD, result);
    }

    @Test
    @DisplayName("Given una persona muerta Y menor de edad, When se registra, Then el resultado es DEAD (R3 se evalúa antes que R5)")
    void deadRuleTakesPriorityOverUnderageRule() {
        // Arrange: 15 años, no viva -> según el orden de evaluación R1..R7, DEAD gana sobre UNDERAGE
        Person deadMinor = new Person("Pedro", 3, 15, Gender.MALE, false);

        // Act
        RegisterResult result = registry.registerVoter(deadMinor);

        // Assert
        assertEquals(RegisterResult.DEAD, result);
    }

    @Test
    @DisplayName("Given un id inválido Y una persona muerta, When se registra, Then el resultado es INVALID (R2 se evalúa antes que R3)")
    void invalidIdRuleTakesPriorityOverDeadRule() {
        // Arrange: id <= 0 y además no viva -> gana INVALID porque R2 se evalúa antes que R3
        Person invalidAndDead = new Person("Marta", -1, 40, Gender.FEMALE, false);

        // Act
        RegisterResult result = registry.registerVoter(invalidAndDead);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

    // ---------------------------------------------------------------
    // R4 - edad biológicamente imposible (clase inválida: edad < 0 o > 120)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given una persona con edad -1, When se registra, Then el resultado es INVALID_AGE")
    void shouldRejectInvalidAgeUnderZero() {
        // Arrange
        Person person = new Person("Juan", 10, -1, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.INVALID_AGE, result);
    }

    @Test
    @DisplayName("Given una persona con edad 121, When se registra, Then el resultado es INVALID_AGE")
    void shouldRejectInvalidAgeOver120() {
        // Arrange
        Person person = new Person("Juan", 11, 121, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.INVALID_AGE, result);
    }

    @Test
    @DisplayName("Given una persona con edad límite superior 120, When se registra, Then el resultado es VALID")
    void shouldAcceptMaxAge120() {
        // Arrange
        Person person = new Person("Rosa", 12, 120, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

    // ---------------------------------------------------------------
    // R5 - mayoría de edad (clase "menor": 0 <= edad < 18; bordes 17 y 18)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given una persona de 17 años viva y con id válido, When se registra, Then el resultado es UNDERAGE")
    void shouldRejectUnderageAt17() {
        // Arrange
        Person person = new Person("Sofía", 13, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
    }

    @Test
    @DisplayName("Given una persona de 18 años viva y con id válido, When se registra, Then el resultado es VALID")
    void shouldAcceptAdultAt18() {
        // Arrange
        Person person = new Person("Diego", 14, 18, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

    // ---------------------------------------------------------------
    // R6 - duplicados (clase "duplicado": mismo id ya registrado)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Given un id ya registrado previamente, When se registra otra persona con el mismo id, Then el resultado es DUPLICATED")
    void shouldRejectDuplicatedId() {
        // Arrange
        Person first = new Person("Carlos", 200, 30, Gender.MALE, true);
        Person second = new Person("Carla", 200, 25, Gender.FEMALE, true);

        // Act
        RegisterResult firstResult = registry.registerVoter(first);
        RegisterResult secondResult = registry.registerVoter(second);

        // Assert
        assertEquals(RegisterResult.VALID, firstResult);
        assertEquals(RegisterResult.DUPLICATED, secondResult);
    }

    @Test
    @DisplayName("Given un id nunca registrado, When se registra, Then el resultado es VALID (no se confunde con DUPLICATED)")
    void shouldAcceptUniqueId() {
        // Arrange
        Person person = new Person("Elena", 300, 40, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }
}
