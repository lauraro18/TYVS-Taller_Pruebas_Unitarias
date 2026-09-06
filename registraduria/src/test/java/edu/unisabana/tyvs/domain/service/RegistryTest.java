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
 * resultado esperado. Estado al terminar la ITERACION 2 del README.
 *
 * Complemento: RegistryPropertiesTest expresa las mismas reglas como
 * PROPIEDADES sobre rangos completos de entradas, en vez de ejemplos sueltos.
 */
class RegistryTest {

    private Registry registry;

    /**
     * Un Registry NUEVO antes de cada prueba.
     *
     * Importante: cuando implemente DUPLICATED, el Registry guardara estado
     * (los ids ya registrados). Si compartiera la misma instancia entre
     * pruebas, una prueba podria "ensuciar" a la siguiente y los resultados
     * dependerian del orden de ejecucion. Cada prueba debe ser independiente.
     */
    @BeforeEach
    void setUp() {
        registry = new Registry();
    }

    @Test
    @DisplayName("Given una persona viva y mayor de edad, When se registra, Then el resultado es VALID")
    void shouldRegisterValidPerson() {
        // Arrange: preparar los datos
        Person person = new Person("Ana", 1, 30, Gender.FEMALE, true);

        // Act: ejecutar la accion que queremos probar
        RegisterResult result = registry.registerVoter(person);

        // Assert: verificar el resultado esperado
        assertEquals(RegisterResult.VALID, result);
    }

    @Test
    @DisplayName("Given una persona no viva, When se registra, Then el resultado es DEAD")
    void shouldRejectDeadPerson() {
        // Arrange: preparar los datos
        Person dead = new Person("Carlos", 2, 40, Gender.MALE, false);

        // Act: ejecutar la accion que queremos probar
        RegisterResult result = registry.registerVoter(dead);

        // Assert: verificar el resultado esperado
        assertEquals(RegisterResult.DEAD, result);
    }

    @Test
    @DisplayName("Given que la persona es null, When se registra, Then el resultado es INVALID")
    void shouldReturnInvalidWhenPersonIsNull() {
        // Act
        RegisterResult result = registry.registerVoter(null);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }

    // --- Iteracion 3: R2 - id no positivo -------------------------------

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

    // --- Iteracion 4: R4 - edad biologicamente imposible ----------------

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
    @DisplayName("Given una persona con edad limite superior 120, When se registra, Then el resultado es VALID")
    void shouldAcceptMaxAge120() {
        // Arrange
        Person person = new Person("Rosa", 12, 120, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }

    // --- Iteracion 5: R5 - mayoria de edad -------------------------------

    @Test
    @DisplayName("Given una persona de 17 anios viva y con id valido, When se registra, Then el resultado es UNDERAGE")
    void shouldRejectUnderageAt17() {
        // Arrange
        Person person = new Person("Sofía", 13, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
    }

    @Test
    @DisplayName("Given una persona de 18 anios viva y con id valido, When se registra, Then el resultado es VALID")
    void shouldAcceptAdultAt18() {
        // Arrange
        Person person = new Person("Diego", 14, 18, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(person);

        // Assert
        assertEquals(RegisterResult.VALID, result);
    }
}
