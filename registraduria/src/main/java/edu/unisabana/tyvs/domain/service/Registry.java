package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

/**
 * PUNTO DE PARTIDA DEL TALLER - no es la solucion final.
 *
 * Esta clase es el estado del codigo al terminar la ITERACION 4 (regla
 * "edad biologicamente imposible"). Las reglas que faltan son las que usted
 * debe construir con TDD (Red -> Green -> Refactor):
 *
 *   - 0 <= edad < 18         -> UNDERAGE
 *   - id ya registrado antes -> DUPLICATED
 *
 * Escriba PRIMERO la prueba que falla, luego la implementacion minima.
 */
public class Registry {

    /** R2: el id del documento debe ser mayor o igual a este valor. */
    static final int MIN_ID = 1;

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
        if (p.getAge() < 0 || p.getAge() > 120) {
            return RegisterResult.INVALID_AGE; // implementacion minima para R4
        }
        // Implementacion minima para pasar las pruebas de las iteraciones 2 a 4.
        // TODO iteracion 5 en adelante: validar mayoria de edad y duplicados.
        return RegisterResult.VALID;
    }
}
