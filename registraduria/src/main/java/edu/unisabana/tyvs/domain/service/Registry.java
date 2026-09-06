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

    public RegisterResult registerVoter(Person p) {
        if (p == null) {
            return RegisterResult.INVALID; // regla defensiva
        }
        if (p.getId() < 1) {
            return RegisterResult.INVALID; // implementacion minima para R2
        }
        if (!p.isAlive()) {
            return RegisterResult.DEAD;
        }
        // Implementacion minima para pasar las pruebas de las iteraciones 2 y 3.
        // TODO iteracion 4 en adelante: validar edad y duplicados.
        return RegisterResult.VALID;
    }
}
