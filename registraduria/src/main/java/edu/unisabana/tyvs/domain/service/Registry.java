package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

/**
 * PUNTO DE PARTIDA DEL TALLER - no es la solucion final.
 *
 * Esta clase es el estado del codigo al terminar la ITERACION 3 (regla
 * "id no positivo"). Las reglas que faltan son las que usted debe construir
 * con TDD (Red -> Green -> Refactor):
 *
 *   - edad < 0 o edad > 120  -> INVALID_AGE
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
        // Implementacion minima para pasar las pruebas de las iteraciones 2 y 3.
        // TODO iteracion 4 en adelante: validar edad y duplicados.
        return RegisterResult.VALID;
    }
}
