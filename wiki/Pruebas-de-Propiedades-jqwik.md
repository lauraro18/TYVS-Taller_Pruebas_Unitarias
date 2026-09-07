`RegistryPropertiesTest` trae 8 propiedades: las 3 que ya venían de ejemplo en el repo del profesor, más 5 que agregué yo (el mínimo pedido era 3).

| Propiedad | Qué verifica |
|---|---|
| `unaPersonaNoVivaSiempreEsRechazada` | R3: si no está viva, siempre da DEAD (venía de ejemplo) |
| `elResultadoNoDependeDeLaInstancia` | Determinismo: la misma entrada siempre da el mismo resultado, en instancias distintas (venía de ejemplo) |
| `nuncaDevuelveNullNiLanzaExcepcion` | Totalidad: nunca devuelve null ni lanza excepción, ni en los extremos de int (venía de ejemplo) |
| `idNoPositivoSiempreEsInvalido` | R2: id ≤ 0 siempre da INVALID |
| `todoMenorDeEdadEsRechazado` | R5: toda edad entre 0 y 17 da UNDERAGE |
| `todoAdultoValidoSeRegistra` | R5/R7: toda edad entre 18 y 120, con id único, da VALID |
| `elMismoIdRegistradoDosVecesSiempreEsDuplicadoLaSegundaVez` | R6: el mismo id registrado dos veces siempre da VALID y luego DUPLICATED |
| `elResultadoSiempreCaeEnUnaParticionConocida` | El resultado siempre es uno de los valores conocidos del enum |

Con esto cumplo lo que pedía el taller: al menos una propiedad de regla de negocio (la mayoría de la lista) y al menos una estructural (aquí hay tres: determinismo, totalidad y partición).

La diferencia frente a una prueba por ejemplo se ve bien con `todoAdultoValidoSeRegistra`. La prueba `shouldAcceptAdultAt18` dice "Diego, 18 años, vivo, id único → VALID", que es un solo punto del espacio de entradas. La propiedad dice lo mismo pero para toda edad entre 18 y 120, y jqwik genera cientos de combinaciones tratando de romperla. Si alguien cambiara por error `MIN_VOTING_AGE` a 19, la prueba por ejemplo fallaría, pero la propiedad también, y encima te dice exactamente qué edad la rompió sin que nadie tuviera que pensar en probar ese caso a mano.

## Lo del shrinking (ya lo corrí)

Para ver el shrinking en acción rompí una regla a propósito: comenté temporalmente la validación de `MIN_ID` en `Registry.java` (la de R2) y corrí

```sh
mvn clean test -Dtest=RegistryPropertiesTest#idNoPositivoSiempreEsInvalido
```

Esta propiedad dice que cualquier id ≤ 0 siempre debería dar `INVALID`. Al quitar esa validación, cualquier id (por más negativo que sea) pasa de largo, así que la propiedad tenía que fallar sí o sí. Esto fue lo que reportó jqwik:

```text
Original Sample
---------------
  arg0: -1647   (id)
  arg1: 25      (edad)
  arg2: FEMALE  (género)

  Original Error
  --------------
  org.opentest4j.AssertionFailedError:
    expected: <INVALID> but was: <VALID>

Shrunk Sample (3 steps)
-----------------------
  arg0: 0
  arg1: 18
  arg2: MALE
```

jqwik primero encontró la falla con una entrada bastante random (id -1647), y en solo 3 pasos la redujo hasta el caso más simple que sigue rompiendo la propiedad: id = 0. Si tuviera que revisar a mano por qué falló "id -1647, edad 25, género FEMALE", no sabría de una si el problema es el id, la edad o el género. Con "id 0, edad 18, género MALE" es inmediato: el id es el único valor en el borde de su rango, así que ahí está el problema. Eso es justo lo que hace útil el shrinking frente a un log de datos aleatorios: apunta directo al borde del dominio, que es donde suelen estar los errores.

Después de capturar esto deshice el cambio (volví a descomentar la validación de R2) y corrí `mvn clean verify` de nuevo para confirmar que las 22 pruebas vuelven a pasar todas.
