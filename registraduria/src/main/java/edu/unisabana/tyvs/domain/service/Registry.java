package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio de dominio de la Registraduría: decide si una {@link Person} queda
 * registrada como votante, aplicando las reglas de negocio R1-R7 (ver README).
 *
 * <p>Orden de evaluación (decisión de diseño, ver README): R1 -> R2 -> R3 ->
 * R4 -> R5 -> R6 -> R7. La primera regla que falla determina el resultado.
 * Por ejemplo, una persona muerta de 15 años devuelve {@code DEAD}, no
 * {@code UNDERAGE}, porque R3 se evalúa antes que R5.</p>
 *
 * <p>Esta clase mantiene estado (los ids ya registrados). Por eso cada prueba
 * debe trabajar con una instancia nueva de {@code Registry} (ver
 * {@code @BeforeEach} en {@code RegistryTest}); de lo contrario el orden de
 * ejecución de las pruebas contaminaría los resultados.</p>
 */
public class Registry {

    /** R2: el id del documento debe ser mayor o igual a este valor. */
    static final int MIN_ID = 1;

    /** R4: edad mínima biológicamente posible. */
    static final int MIN_AGE = 0;

    /** R4: edad máxima biológicamente posible. */
    static final int MAX_AGE = 120;

    /** R5: edad mínima para poder votar. */
    static final int MIN_VOTING_AGE = 18;

    private final Set<Integer> registeredIds = new HashSet<>();

    public RegisterResult registerVoter(Person p) {
        if (p == null) {
            return RegisterResult.INVALID; // R1: persona nula
        }
        if (p.getId() < MIN_ID) {
            return RegisterResult.INVALID; // R2: id no positivo
        }
        if (!p.isAlive()) {
            return RegisterResult.DEAD; // R3: persona no viva
        }
        if (p.getAge() < MIN_AGE || p.getAge() > MAX_AGE) {
            return RegisterResult.INVALID_AGE; // R4: edad biológicamente imposible
        }
        if (p.getAge() < MIN_VOTING_AGE) {
            return RegisterResult.UNDERAGE; // R5: menor de edad
        }
        if (registeredIds.contains(p.getId())) {
            return RegisterResult.DUPLICATED; // R6: id ya registrado
        }
        registeredIds.add(p.getId());
        return RegisterResult.VALID; // R7: cumple todas las reglas
    }
}
