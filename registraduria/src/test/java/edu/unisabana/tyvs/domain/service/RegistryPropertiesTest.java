package edu.unisabana.tyvs.domain.service;

import edu.unisabana.tyvs.domain.model.Gender;
import edu.unisabana.tyvs.domain.model.Person;
import edu.unisabana.tyvs.domain.model.RegisterResult;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PRUEBAS BASADAS EN PROPIEDADES (jqwik).
 *
 * Una prueba por ejemplo dice "edad 40 y no viva -> DEAD".
 * Una propiedad dice "para TODA edad y TODO genero, si no esta viva -> DEAD",
 * y jqwik genera cientos de combinaciones para intentar refutarla.
 *
 * Es la continuacion natural de las clases de equivalencia: usted eligio un
 * representante por clase a mano; aqui la maquina explora la clase entera.
 *
 * Cuando una propiedad falla, jqwik no reporta la entrada aleatoria que la
 * rompio, sino la MAS SIMPLE que la rompe (shrinking). Si "edad 73 con nombre
 * 'xkqz'" falla, le reportara "edad 0 con nombre ''", que es mucho mas facil
 * de diagnosticar. Ver el analisis de shrinking en la Wiki (pagina
 * "Pruebas basadas en propiedades").
 */
class RegistryPropertiesTest {

    /** Genera cualquier valor del enum Gender, incluido UNIDENTIFIED. */
    @Provide
    Arbitrary<Gender> generos() {
        return Arbitraries.of(Gender.values());
    }

    /** Nombres arbitrarios, incluida la cadena vacia. */
    @Provide
    Arbitrary<String> nombres() {
        return Arbitraries.strings().alpha().ofMaxLength(20);
    }

    // -----------------------------------------------------------------
    // Propiedades de referencia (ya incluidas en el repositorio base)
    // -----------------------------------------------------------------

    /**
     * Regla R3: una persona no viva se rechaza SIEMPRE, sin importar su edad,
     * su documento ni su genero.
     *
     * Esta unica propiedad cubre mas terreno que cualquier tabla de ejemplos:
     * afirma algo sobre el espacio completo de entradas, no sobre cinco filas.
     */
    @Property
    void unaPersonaNoVivaSiempreEsRechazada(
            @ForAll("nombres") String nombre,
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 120) int edad,
            @ForAll("generos") Gender genero) {

        Person muerta = new Person(nombre, id, edad, genero, false);

        assertEquals(RegisterResult.DEAD, new Registry().registerVoter(muerta));
    }

    /**
     * Propiedad de DETERMINISMO (estructural): registrar la misma persona en
     * dos Registry recien creados produce el mismo resultado.
     *
     * Parece obvia, pero es justo la que se rompe cuando alguien introduce
     * estado compartido (por ejemplo, un Set estatico de ids en vez de uno de
     * instancia). Es un buen ejemplo de propiedad que atrapa errores de diseno
     * que ninguna prueba por ejemplo buscaria.
     */
    @Property
    void elResultadoNoDependeDeLaInstancia(
            @ForAll("nombres") String nombre,
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 0, max = 120) int edad,
            @ForAll("generos") Gender genero,
            @ForAll boolean viva) {

        Person p = new Person(nombre, id, edad, genero, viva);

        RegisterResult primera = new Registry().registerVoter(p);
        RegisterResult segunda = new Registry().registerVoter(p);

        assertEquals(primera, segunda);
    }

    /**
     * Propiedad de TOTALIDAD (estructural): registerVoter nunca devuelve null
     * ni lanza una excepcion, sea cual sea la entrada.
     *
     * Un contrato debil, pero sorprendentemente util: detecta desbordamientos,
     * divisiones por cero y NullPointerException que aparecen solo en los
     * bordes del dominio. Nota: aqui usamos rangos completos de int (sin
     * @IntRange) a proposito, para incluir Integer.MIN_VALUE/MAX_VALUE.
     */
    @Property
    void nuncaDevuelveNullNiLanzaExcepcion(
            @ForAll("nombres") String nombre,
            @ForAll int id,
            @ForAll int edad,
            @ForAll("generos") Gender genero,
            @ForAll boolean viva) {

        Person p = new Person(nombre, id, edad, genero, viva);

        RegisterResult resultado = new Registry().registerVoter(p);

        org.junit.jupiter.api.Assertions.assertNotNull(resultado);
    }

    // -----------------------------------------------------------------
    // Propiedades agregadas para cubrir las reglas R2, R5, R6 y una
    // propiedad estructural adicional (invariante de particion).
    // -----------------------------------------------------------------

    /**
     * Regla R2: cualquier id no positivo se rechaza SIEMPRE con INVALID,
     * sin importar la edad (siempre que sea una edad de adulto valido, para
     * aislar exclusivamente el efecto del id).
     */
    @Property
    void idNoPositivoSiempreEsInvalido(
            @ForAll @IntRange(min = -100_000, max = 0) int id,
            @ForAll @IntRange(min = 18, max = 120) int edad,
            @ForAll("generos") Gender genero) {

        Person p = new Person("X", id, edad, genero, true);

        assertEquals(RegisterResult.INVALID, new Registry().registerVoter(p));
    }

    /**
     * Regla R5: toda persona viva, con id valido, de entre 0 y 17 anios se
     * rechaza SIEMPRE con UNDERAGE. Corresponde al ejercicio propuesto en el
     * README ("todoMenorDeEdadEsRechazado").
     */
    @Property
    void todoMenorDeEdadEsRechazado(@ForAll @IntRange(min = 0, max = 17) int edad) {
        Person p = new Person("Menor", 1, edad, Gender.UNIDENTIFIED, true);

        assertEquals(RegisterResult.UNDERAGE, new Registry().registerVoter(p));
    }

    /**
     * Regla R5/R7: toda persona viva, con id valido y unico, de entre 18 y
     * 120 anios queda registrada SIEMPRE como VALID. Corresponde al ejercicio
     * propuesto en el README ("todoAdultoValidoSeRegistra").
     *
     * Se crea un Registry nuevo en cada evaluacion de la propiedad para que
     * el mismo id (1) nunca choque con la regla de duplicados (R6): cada
     * corrida es independiente, igual que en las pruebas por ejemplo.
     */
    @Property
    void todoAdultoValidoSeRegistra(@ForAll @IntRange(min = 18, max = 120) int edad) {
        Person p = new Person("Adulto", 1, edad, Gender.UNIDENTIFIED, true);

        assertEquals(RegisterResult.VALID, new Registry().registerVoter(p));
    }

    /**
     * Regla R6: si el mismo id se registra dos veces en el MISMO Registry,
     * la primera vez es VALID y la segunda SIEMPRE es DUPLICATED, sin
     * importar que cambien nombre, genero o edad (dentro del rango adulto).
     */
    @Property
    void elMismoIdRegistradoDosVecesSiempreEsDuplicadoLaSegundaVez(
            @ForAll @IntRange(min = 1, max = 100_000) int id,
            @ForAll @IntRange(min = 18, max = 120) int edad1,
            @ForAll @IntRange(min = 18, max = 120) int edad2,
            @ForAll("generos") Gender genero1,
            @ForAll("generos") Gender genero2) {

        Registry registry = new Registry();
        Person primera = new Person("A", id, edad1, genero1, true);
        Person segunda = new Person("B", id, edad2, genero2, true);

        RegisterResult resultado1 = registry.registerVoter(primera);
        RegisterResult resultado2 = registry.registerVoter(segunda);

        assertEquals(RegisterResult.VALID, resultado1);
        assertEquals(RegisterResult.DUPLICATED, resultado2);
    }

    /**
     * Propiedad ESTRUCTURAL de INVARIANTE DE PARTICION: sea cual sea la
     * entrada, el resultado siempre pertenece al conjunto conocido de
     * valores de {@link RegisterResult}. Garantiza que ninguna combinacion
     * de entradas cae en un "hueco" no contemplado por el enum.
     *
     * Nota: en Java esta propiedad esta parcialmente garantizada por el
     * sistema de tipos (el metodo solo puede devolver un valor del enum o
     * null/excepcion, ambos cubiertos por nuncaDevuelveNullNiLanzaExcepcion).
     * Aun asi, documentarla explicitamente dentro de un conjunto conocido de
     * resultados esperados es valioso: obliga a enumerar el "universo" de
     * resultados validos y sirve como red de seguridad si el enum crece en
     * el futuro (un nuevo valor no declarado aqui rompe la prueba).
     */
    @Property
    void elResultadoSiempreCaeEnUnaParticionConocida(
            @ForAll("nombres") String nombre,
            @ForAll int id,
            @ForAll int edad,
            @ForAll("generos") Gender genero,
            @ForAll boolean viva) {

        Set<RegisterResult> particionesConocidas = new HashSet<>(Arrays.asList(
                RegisterResult.VALID,
                RegisterResult.DUPLICATED,
                RegisterResult.INVALID,
                RegisterResult.DEAD,
                RegisterResult.UNDERAGE,
                RegisterResult.INVALID_AGE));

        Person p = new Person(nombre, id, edad, genero, viva);

        RegisterResult resultado = new Registry().registerVoter(p);

        assertTrue(particionesConocidas.contains(resultado));
    }
}
