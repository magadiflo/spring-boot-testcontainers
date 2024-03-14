package dev.magadiflo.springboottestcontainers.web.api;

import dev.magadiflo.springboottestcontainers.persistence.entity.Customer;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Collections;

/**
 * Configuración Manual
 * ********************
 * <p>
 * Somos nosotros los que le decimos cuándo debemos iniciar el contenedor, cuándo detenerse
 * y además agregar ciertas configuraciones dinámicas usando la anotación @DynamicPropertySource
 */

@Sql(scripts = {"/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerRestControllerManualConfigTest {
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15.2-alpine");
    private static final String CUSTOMERS_ENDPOINT_PATH = "/api/v1/customers";
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        POSTGRES_CONTAINER.start();
    }

    @AfterAll
    static void afterAll() {
        POSTGRES_CONTAINER.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    }

    @Test
    void connectionEstablished() {
        Assertions.assertThat(POSTGRES_CONTAINER.isCreated()).isTrue();
        Assertions.assertThat(POSTGRES_CONTAINER.isRunning()).isTrue();
    }

    @Test
    void shouldGetAllCustomers() {
        Customer[] customersResponse = this.restTemplate.getForObject(CUSTOMERS_ENDPOINT_PATH, Customer[].class);
        Assertions.assertThat(customersResponse.length).isEqualTo(5);
    }

    @Test
    void shouldFindCustomerWhenValidCustomerId() {
        ResponseEntity<Customer> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH + "/{id}", HttpMethod.GET, null, Customer.class, Collections.singletonMap("id", 1));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldFindCustomerWhenEmailIsValid() {
        ResponseEntity<Customer> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH + "/email/{email}", HttpMethod.GET, null, Customer.class, Collections.singletonMap("email", "karito.casanova@gmail.com"));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundWhenEmailIsInvalid() {
        ResponseEntity<Customer> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH + "/email/{email}", HttpMethod.GET, null, Customer.class, Collections.singletonMap("email", "karito.casanova@outlook.com"));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldCreateNewCustomer() {
        Customer customerRequest = Customer.builder()
                .name("Rosita Pardo")
                .email("rosita.pardo@gmail.com")
                .build();
        ResponseEntity<Customer> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH, HttpMethod.POST, new HttpEntity<>(customerRequest), Customer.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getId()).isEqualTo(6);
        Assertions.assertThat(response.getBody().getName()).isEqualTo(customerRequest.getName());
        Assertions.assertThat(response.getBody().getEmail()).isEqualTo(customerRequest.getEmail());
    }

    @Test
    void shouldUpdateCustomerWhenCustomerIsValid() {
        Customer customerToUpdate = Customer.builder()
                .name("Karol Casanova Mas Naa")
                .email("karito.casanova@outlook.com")
                .build();
        ResponseEntity<Customer> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH + "/{id}", HttpMethod.PUT, new HttpEntity<>(customerToUpdate), Customer.class, Collections.singletonMap("id", 1));

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getId()).isEqualTo(1);
        Assertions.assertThat(response.getBody().getName()).isEqualTo(customerToUpdate.getName());
        Assertions.assertThat(response.getBody().getEmail()).isEqualTo(customerToUpdate.getEmail());
    }

    @Test
    void shouldDeleteCustomerWithValidId() {
        ResponseEntity<Void> response = this.restTemplate.exchange(CUSTOMERS_ENDPOINT_PATH + "/{id}", HttpMethod.DELETE, null, Void.class, Collections.singletonMap("id", 1));
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}