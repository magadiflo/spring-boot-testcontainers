# [Getting started with Testcontainers in a Java Spring Boot Project](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/)

Tutorial tomado de la página web oficial de **Testcontainers** con pequeños cambios realizados por mí.

### Qué vamos a conseguir en esta guía

Vamos a crear un proyecto `Spring Boot` utilizando `Spring Data JPA` junto con `Postgres` e implementaremos endpoints
API
REST con el que interactuaremos con los registros almacenados en la base de datos. Luego probaremos esos endpoints
usando el módulo `Testcontainers`, `Postgres` y `TestRestTemplate`.

# Creando Aplicación Base

---

## Dependencias iniciales

````xml
<!--Spring Boot 3.2.3-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

