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

## Configurando application.yml

El archivo de propiedades principal `src/main/resources/application.yml` contendrá las configuraciones que requiere
nuestra aplicación para su funcionamiento.

En este archivo definimos la conexión a la base de datos real `db_production` con el que trabajará nuestra aplicación,
además del puerto en el que se estará ejecutando y otras configuraciones adicionales.

Es importante notar que estamos usando la propiedad `spring.sql.init.mode=always` con el que habilitamos la
inicialización del esquema, es decir, con esa configuración estamos habilitando la ejecución de los
archivos `schema.sql` y `data.sql`.

Ahora, podríamos haber definido las configuraciones propias de `jpa/hibernate` (`spring.jpa.hibernate.ddl-auto=update`)
para que nos cree a partir de las clases java de entidad las tablas en la base de datos, pero, en esta ocasión usaremos
los archivos `schema.sql` y `data.sql`, así que omitimos esa configuración y únicamente dejamos la configuración
`spring.sql.init.mode=always`.

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: spring-boot-testcontainers

  datasource:
    url: jdbc:postgresql://localhost:5432/db_production
    username: postgres
    password: magadiflo

  sql:
    init:
      mode: always

  jpa:
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG            # Permite ver la consulta SQL en consola
    org.hibernate.orm.jdbc.bind: TRACE  # Permite ver los parámetros de la consulta SQL
````

## Agregando script de creación de squema y poblamiento de tabla

Como no utilizamos ninguna base de datos en memoria, necesitamos crear las tablas de la base de datos de Postgres de
alguna manera. El enfoque recomendado es utilizar alguna herramienta de migración de bases de datos como `Flyway` o
`Liquibase`, pero para esta guía utilizaremos el soporte de inicialización de esquema simple proporcionado por Spring
Boot.

Cree un archivo `schema.sql` con el siguiente contenido en el directorio `src/main/resources`.

````sql
CREATE TABLE IF NOT EXISTS customers(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE
);
````

Cree el archivo `data.sql` para insertar registros en el esquema anterior:

````sql
TRUNCATE TABLE customers RESTART IDENTITY;

INSERT INTO customers(name, email)
VALUES('John Doe', 'john.doe@example.com'),
('Alice Smith', 'alice.smith@example.com'),
('Bob Johnson', 'bob.johnson@example.com'),
('Emily Davis', 'emily.davis@example.com'),
('Michael Brown', 'michael.brown@example.com'),
('Emma Wilson', 'emma.wilson@example.com'),
('Daniel Taylor', 'daniel.taylor@example.com'),
('Olivia Martinez', 'olivia.martinez@example.com'),
('James Anderson', 'james.anderson@example.com'),
('Sophia Rodriguez', 'sophia.rodriguez@example.com'),
('William Garcia', 'william.garcia@example.com'),
('Ava Lopez', 'ava.lopez@example.com'),
('Alexander Perez', 'alexander.perez@example.com'),
('Charlotte Gonzalez', 'charlotte.gonzalez@example.com'),
('Mason Sanchez', 'mason.sanchez@example.com'),
('Amelia Ramirez', 'amelia.ramirez@example.com'),
('Ethan Torres', 'ethan.torres@example.com'),
('Isabella Flores', 'isabella.flores@example.com'),
('Liam Rivera', 'liam.rivera@example.com'),
('Mia Cruz', 'mia.cruz@example.com'),
('Noah Diaz', 'noah.diaz@example.com'),
('Harper Stewart', 'harper.stewart@example.com'),
('Benjamin Murphy', 'benjamin.murphy@example.com'),
('Evelyn Coleman', 'evelyn.coleman@example.com'),
('Logan Reed', 'logan.reed@example.com'),
('Avery Bailey', 'avery.bailey@example.com'),
('Jacob Butler', 'jacob.butler@example.com'),
('Sofia Garcia', 'sofia.garcia@example.com'),
('Elijah Roberts', 'elijah.roberts@example.com'),
('Aria Martinez', 'aria.martinez@example.com'),
('Oliver Perez', 'oliver.perez@example.com'),
('Chloe Adams', 'chloe.adams@example.com'),
('William Scott', 'william.scott@example.com'),
('Scarlett Foster', 'scarlett.foster@example.com'),
('Lucas Price', 'lucas.price@example.com'),
('Layla Evans', 'layla.evans@example.com'),
('Michael Long', 'michael.long@example.com'),
('Grace Lee', 'grace.lee@example.com'),
('Jackson King', 'jackson.king@example.com'),
('Riley Hughes', 'riley.hughes@example.com'),
('Lily Nguyen', 'lily.nguyen@example.com'),
('Aiden Thompson', 'aiden.thompson@example.com'),
('Zoe Mitchell', 'zoe.mitchell@example.com'),
('Henry Cook', 'henry.cook@example.com'),
('Audrey White', 'audrey.white@example.com');
````

## Creando Entidad JPA

Vamos a trabajar con la entidad JPA llamada `Customer`

````java

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
}
````

## Creando Spring Data JPA Repository

Spring Data JPA es una abstracción además de JPA y proporciona operaciones CRUD básicas, capacidades de clasificación y
paginación y generación dinámica de consultas a partir de nombres de métodos.

Creemos una interfaz de repositorio Spring Data JPA para la entidad `Cusotmer`. Además, crearemos un método
personalizado con el que obtendremos un `Customer` a partir de su email:

````java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findCustomerByEmail(String email);
}
````

## Creando Servicios

Como buena práctica, creamos la capa de servicio con el que implementaremos los métodos para interactuar con el
repositorio de Customer:

````java
public interface CustomerService {
    List<Customer> findAllCustomers();

    Optional<Customer> findCustomerById(Long id);

    Optional<Customer> findCustomerByEmail(String email);

    Customer saveCustomer(Customer customer);

    Optional<Customer> updateCustomer(Long id, Customer customer);

    Optional<Boolean> deleteCustomerById(Long id);
}
````

````java

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return this.customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerById(Long id) {
        return this.customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerByEmail(String email) {
        return this.customerRepository.findCustomerByEmail(email);
    }

    @Override
    @Transactional
    public Customer saveCustomer(Customer customer) {
        return this.customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Optional<Customer> updateCustomer(Long id, Customer customer) {
        return this.customerRepository.findById(id)
                .map(customerDB -> {
                    customerDB.setName(customer.getName());
                    customerDB.setEmail(customer.getEmail());
                    return customerDB;
                })
                .map(this.customerRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteCustomerById(Long id) {
        return this.customerRepository.findById(id)
                .map(customerDB -> {
                    this.customerRepository.deleteById(id);
                    return true;
                });
    }
}
````

## Creando endpoints API REST

Finalmente, cree un controlador para implementar los endpoints API REST utilizando todos los métodos implementados en la
capa de servicio de Customer:

````java

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/api/v1/customers")
public class CustomerRestController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<Customer>> listAllCustomers() {
        return ResponseEntity.ok(this.customerService.findAllCustomers());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return this.customerService.findCustomerById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return this.customerService.findCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@RequestBody Customer customer) {
        Customer customerDB = this.customerService.saveCustomer(customer);
        URI location = URI.create("/api/v1/customers/" + customerDB.getId());
        return ResponseEntity.created(location).body(customerDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return this.customerService.updateCustomer(id, customer)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        return this.customerService.deleteCustomerById(id)
                .map(wasDeleted -> new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
````

## Probando Endpoints

Hasta este punto se hicieron las pruebas y todos los endpoints están funcionando correctamente. Solo por documentación
mostraré los resultados obtenidos al consultar algunos endpoints:

````bash
$ curl -v http://localhost:8080/api/v1/customers | jq

>
< HTTP/1.1 200
<
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  {...}
]
````

````bash
$ curl -v http://localhost:8080/api/v1/customers/email/michael.brown@example.com | jq

>
< HTTP/1.1 200
<
{
  "id": 5,
  "name": "Michael Brown",
  "email": "michael.brown@example.com"
}
````

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"name\": \"martin\", \"email\": \"martin@gmail.com\"}" http://localhost:8080/api/v1/customers | jq

< HTTP/1.1 201
<
{
  "id": 46,
  "name": "martin",
  "email": "martin@gmail.com"
}
````
