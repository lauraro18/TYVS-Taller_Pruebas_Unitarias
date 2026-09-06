package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

/**
 * PUNTO DE PARTIDA DEL TALLER - no es la solucion final.
 *
 * Esta clase es el estado del codigo al terminar la ITERACION 5 (regla
 * "mayoria de edad"). La regla que falta es la que usted debe construir
 * con TDD (Red -> Green -> Refactor):
 *
 *   - id ya registrado antes -> DUPLICATED
 *
 * Escriba PRIMERO la prueba que falla, luego la implementacion minima.
 */
public class Registry {

    /** R2: el id del documento debe ser mayor o igual a este valor. */
    static final int MIN_ID = 1;

    /** R4: edad mínima biológicamente posible. */
    static final int MIN_AGE = 0;

    /** R4: edad máxima biológicamente posible. */
    static final int MAX_AGE = 120;

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
        if (p.getAge() < 18) {
            return RegisterResult.UNDERAGE; // implementacion minima para R5
        }
        // Implementacion minima para pasar las pruebas de las iteraciones 2 a 5.
        // TODO iteracion 6: validar duplicados.
        return RegisterResult.VALID;
    }
}
