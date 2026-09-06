# Taller de Pruebas Unitarias (TDD) — Registraduría

**Curso:** Testing y Validación de Software
**Programa:** Maestría en Ingeniería de Software – Universidad de La Sabana
**Integrante:** ver [`integrantes.txt`](../blob/main/integrantes.txt)

## Resumen del dominio

El ejercicio simula el servicio de inscripción de votantes de una registraduría. La clase `Registry` ([código](../blob/main/registraduria/src/main/java/edu/unisabana/tyvs/domain/service/Registry.java)) expone un único método de negocio:

```java
RegisterResult registerVoter(Person p)
```

que decide si una persona (`Person`) queda registrada como votante, aplicando siete reglas de negocio evaluadas en orden estricto (la primera que falla determina el resultado):

| # | Regla | Resultado |
|---|-------|-----------|
| R1 | La persona no puede ser nula | `INVALID` |
| R2 | El id debe ser positivo (`id > 0`) | `INVALID` |
| R3 | La persona debe estar viva | `DEAD` |
| R4 | La edad debe ser biológicamente posible (`0 ≤ edad ≤ 120`) | `INVALID_AGE` |
| R5 | La persona debe ser mayor de edad (`edad ≥ 18`) | `UNDERAGE` |
| R6 | Solo una inscripción por documento | `DUPLICATED` |
| R7 | Si cumple todo lo anterior | `VALID` |

## Alcance del taller

- Dominio puro: sin bases de datos, sin HTTP, sin frameworks externos (Arquitectura Limpia).
- Desarrollo guiado por pruebas (**TDD**, Red → Green → Refactor) para construir las reglas R2, R4, R5 y R6 sobre el punto de partida que entrega el profesor (R1 y R3 ya implementadas).
- Pruebas por ejemplo (JUnit 5, patrón **AAA**) y pruebas basadas en propiedades (**jqwik**).
- Medición de calidad de las pruebas con **cobertura (JaCoCo)** y **mutación (PIT)**.
- Trazabilidad de cada regla de negocio a un escenario **BDD (Given–When–Then)**.

## Navegación de esta Wiki

- **[TDD: Historia Red → Green → Refactor](TDD-Red-Green-Refactor)** — las iteraciones 3 a 6, cada una con su ciclo completo.
- **[Patrón AAA](Patron-AAA)** — cómo se estructuraron las pruebas y por qué.
- **[Clases de Equivalencia, Valores Límite y BDD](Clases-de-Equivalencia-y-BDD)** — la matriz completa de casos y su traducción a Given–When–Then.
- **[Pruebas basadas en propiedades (jqwik)](Pruebas-de-Propiedades-jqwik)** — las propiedades implementadas y el análisis de shrinking.
- **[Resultados: cobertura y mutación](Resultados-Cobertura-y-Mutacion)** — reportes de JaCoCo y PIT, con interpretación.
- **[Reflexión final](Reflexion-Final)** — qué no se cubrió, qué defectos reales se encontraron y cómo mejorar `Registry`.

## Cómo ejecutar el proyecto

Desde la carpeta `registraduria/` (donde está el `pom.xml`):

```sh
mvn clean test                                              # pruebas unitarias y de propiedades
mvn clean verify                                             # + reporte de cobertura JaCoCo (target/site/jacoco/index.html)
mvn test-compile org.pitest:pitest-maven:mutationCoverage    # reporte de mutación PIT (target/pit-reports/index.html)
```

Ver [`defectos.md`](../blob/main/defectos.md) para el registro de defectos encontrados durante el desarrollo.
