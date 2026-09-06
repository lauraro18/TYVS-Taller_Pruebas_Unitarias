# Pruebas basadas en propiedades (jqwik)

`RegistryPropertiesTest` contiene **8 propiedades** (3 de referencia del profesor + 5 propias), muy por encima del mínimo de 3 exigido por el taller.

## Propiedades implementadas

| Propiedad | Tipo | Regla / garantía que verifica |
|---|---|---|
| `unaPersonaNoVivaSiempreEsRechazada` | De negocio (referencia) | R3: no viva ⇒ siempre `DEAD` |
| `elResultadoNoDependeDeLaInstancia` | Estructural — determinismo (referencia) | Misma entrada ⇒ mismo resultado, en instancias distintas |
| `nuncaDevuelveNullNiLanzaExcepcion` | Estructural — totalidad (referencia) | Nunca `null`, nunca excepción, ni en los extremos de `int` |
| `idNoPositivoSiempreEsInvalido` | De negocio (R2) | `id ≤ 0` ⇒ siempre `INVALID`, para cualquier edad adulta y género |
| `todoMenorDeEdadEsRechazado` | De negocio (R5) | Toda edad en `[0, 17]` ⇒ siempre `UNDERAGE` |
| `todoAdultoValidoSeRegistra` | De negocio (R5/R7) | Toda edad en `[18, 120]`, con id único ⇒ siempre `VALID` |
| `elMismoIdRegistradoDosVecesSiempreEsDuplicadoLaSegundaVez` | De negocio (R6) | El mismo id, dos veces en el mismo `Registry` ⇒ 1ª `VALID`, 2ª siempre `DUPLICATED`, sin importar nombre/edad/género |
| `elResultadoSiempreCaeEnUnaParticionConocida` | Estructural — invariante de partición | El resultado siempre pertenece al conjunto conocido de valores de `RegisterResult` |

Se cumple el requisito del taller: al menos una propiedad de regla de negocio (todas las de arriba menos las tres estructurales) y al menos una estructural (tenemos tres: determinismo, totalidad y partición).

## De ejemplo a propiedad: `todoAdultoValidoSeRegistra`

| Prueba por ejemplo | Propiedad |
|---|---|
| `shouldAcceptAdultAt18`: "Diego, 18 años, vivo, id único → `VALID`" | `todoAdultoValidoSeRegistra`: "para **toda** edad entre 18 y 120, con id único → `VALID`" |
| Verifica un punto del espacio de entradas | Verifica el rango completo por muestreo (jqwik genera cientos de valores de edad) |

La propiedad no solo repite el ejemplo con otro número: si alguien cambiara `MIN_VOTING_AGE` a `19` por error, la prueba por ejemplo con edad 18 fallaría, pero **también** fallaría de inmediato la propiedad, mostrando exactamente qué edad rompe la regla — sin que nadie tuviera que haber pensado en probar esa edad específica.

## Ejercicio de shrinking (para completar en tu entrega)

Esta parte **debes ejecutarla tú en tu propia máquina** (con `mvn`), porque requiere ver la salida real de jqwik al fallar una propiedad; no es algo que se pueda simular sin ejecutar el build. Pasos:

1. Rompe deliberadamente una regla. Por ejemplo, en `Registry.java` comenta temporalmente la guarda de `UNDERAGE`:
   ```java
   // if (p.getAge() < MIN_VOTING_AGE) {
   //     return RegisterResult.UNDERAGE;
   // }
   ```
2. Ejecuta:
   ```sh
   mvn test -Dtest=RegistryPropertiesTest#todoMenorDeEdadEsRechazado
   ```
3. jqwik va a fallar y va a reportar, entre los detalles del fallo, una línea `Shrunk Sample` (o `Original Sample` seguida de `Shrunk Sample`) con el **contraejemplo mínimo** que rompe la propiedad — normalmente algo tan simple como `edad = 0` o el valor más bajo del rango que ya no está protegido.
4. Copia aquí esa salida (el bloque `Original Sample` / `Shrunk Sample` completo) y compárala con la entrada aleatoria original que jqwik probó primero.
5. Deshaz el cambio del paso 1 y vuelve a correr `mvn test` para confirmar que todo queda en verde otra vez.

**Plantilla para completar con tu resultado real:**

```text
Propiedad rota intencionalmente: todoMenorDeEdadEsRechazado
Original Sample (edad aleatoria que jqwik probó primero): edad = <pega aquí el valor real>
Shrunk Sample (contraejemplo mínimo reportado por jqwik):  edad = <pega aquí el valor real>
```

### Por qué el contraejemplo reducido es más útil

Si jqwik reportara la entrada aleatoria original (por ejemplo, "edad 73, id 88041, nombre 'xkqz'"), tendrías que revisar manualmente si el fallo depende del nombre, del id o de la edad. El *shrinking* elimina, uno por uno, cada dato que no es necesario para que la propiedad siga fallando, hasta quedarse con el caso más simple posible. En una propiedad de una sola variable como `todoMenorDeEdadEsRechazado`, lo más probable es que el mínimo sea el extremo del rango probado (`edad = 0`), porque jqwik intenta "reducir hacia cero" primero. Eso es exactamente lo que hace que la propiedad sea más fácil de depurar que un log con datos aleatorios: el reporte ya viene apuntando al borde del dominio, que es donde suelen vivir los defectos.

## Cuándo NO se usó property-based (y por qué eso está bien)

R1 (persona nula) no tiene una propiedad jqwik dedicada: `@ForAll Person` generaría instancias de `Person` construidas con datos aleatorios, pero jqwik no puede generar de forma natural "un `Person` que sea `null`" como parte de ese mismo `@ForAll` sin un proveedor artificial que le reste valor a la prueba. Aquí el ejemplo concreto (`shouldReturnInvalidWhenPersonIsNull`) documenta mejor el caso que cualquier propiedad forzada — es exactamente la situación que describe el README: "un caso de negocio importante merece ambas, pero no todo caso necesita una propiedad".
