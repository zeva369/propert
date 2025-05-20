package com.sidus.propert.controller;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class TaskControllerTest {

    @LocalServerPort
    private int port;

    private String jwtToken;

    @BeforeAll
    void setupAll() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        //Call the authController /login and store the cookie jwtToken for using into the tests
        jwtToken = given()
                .contentType("application/json")
                .body("{\"id\": \"user1\", \"password\": \"password1\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .extract()
                .cookie("jwtToken");
    }

    @Test
    void findAllShouldReturnAllTasks() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
            .when()
                .get("/authenticated/tasks")
            .then()
                .statusCode(200)
                .body("size()", equalTo(3));
    }

    @Test
    void findAllWithDescriptionShouldReturnFilteredTasks() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
                .queryParam("description", "ina")
            .when()
                .get("/authenticated/tasks")
            .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    void findAllWithProjectIdShouldReturnFilteredTasks() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
                .queryParam("projectId", 1)
            .when()
                .get("/authenticated/tasks")
            .then()
                .statusCode(200)
                .body("size()", equalTo(2));
    }

    @Test
    void findByIdShouldReturnTask() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
        .when()
                .get("/authenticated/tasks/task1")
        .then()
                .statusCode(200)
                .body("id", equalTo("task1"))
                .body("description", equalTo("Tarea inicial"))
                .body("length", equalTo(5.0f));
    }

    //Test findById with a non-existing task
    @Test
    void findByIdShouldReturnNotFound() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
        .when()
                .get("/authenticated/tasks/task999")
        .then()
                .log().all()
                .statusCode(404);
    }
}
