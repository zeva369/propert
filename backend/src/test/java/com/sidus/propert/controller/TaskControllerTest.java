package com.sidus.propert.controller;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
            .statusCode(404);
    }

    //Test findPredecessors with a non-existing task
    @Test
    void findPredecessorsShouldReturnNotFound() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .get("/authenticated/tasks/task999/predecessors")
        .then()
            .statusCode(404);
    }

    //Test findPredecessors with a task that has predecessors
    @Test
    void findPredecessorsShouldReturnPredecessors() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .get("/authenticated/tasks/task2/predecessors")
        .then()
            .statusCode(200)
            .body("size()", equalTo(1))
            .body("[0].id", equalTo("task1"));
    }

    //Test findPredecessors with a task that has no predecessors
    @Test
    void findPredecessorsShouldReturnEmptyList() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .get("/authenticated/tasks/task1/predecessors")
        .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    //Test addPredecessor with a non-existing task
    @Test
    void addPredecessorShouldReturnNotFound() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
            .queryParam("pred-id", "task1")
        .when()
            .post("/authenticated/tasks/task999/predecessors")
        .then()
            .statusCode(404);
    }

    //Test addPredecessor with a valid task
    @Test
    void addPredecessorShouldReturnOk() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
            .queryParam("pred-id", "task1")
        .when()
            .post("/authenticated/tasks/task2/predecessors")
        .then()
            .statusCode(200)
            .body("id", equalTo("task1"));
    }

    //Test addPredecessor with a task that already has the predecessor
    @Test
    void addPredecessorShouldBeIdempotentAndReturnOk() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
            .queryParam("pred-id", "task2")
        .when()
            .post("/authenticated/tasks/task3/predecessors")
        .then()
            .statusCode(200);
    }

    //Test removePredecessor with a non-existing task
    @Test
    void removePredecessorShouldReturnNotFoundWithNonExistingTask() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .delete("/authenticated/tasks/task999/predecessors/task1")
        .then()
            .statusCode(404);
    }

    //Test removePredecessor with a valid task and a valid Task as predecessor but not a predecessor
    @Test
    void removePredecessorShouldReturnNotFoundWithValidPredecessorButNotPredecessor() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .delete("/authenticated/tasks/task1/predecessors/task3")
        .then()
            .statusCode(404);
    }

    //Test removePredecessor with a valid task and a valid predecessor
    @Test
    void removePredecessorShouldReturnNoContent() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .delete("/authenticated/tasks/task3/predecessors/task2")
        .then()
            .statusCode(204);
    }

    //Test removePredecessor with a valid task and an invalid predecessor
    @Test
    void removePredecessorShouldReturnNotFoundWithValidTaskAndInvalidPredecessor() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
        .when()
            .delete("/authenticated/tasks/task2/predecessors/task999")
        .then()
            .statusCode(404);
    }

    //This tests are gonna be moved to the ProjectControllerTest
    /*
    //Test create with an existing task should return CONFLICT
    @Test
    void createShouldReturnConflict() {
        given()
            .contentType("application/json")
            .cookie("jwtToken", jwtToken)
            .body("{\"id\": \"task1\", \"description\": \"Tarea inicial\", \"length\": 5.0, \"predecessors\": []}")
        .when()
            .post("/authenticated/tasks")
        .then()
            .statusCode(409);
    }

    //Test create with a new task should return CREATED and the task
    @Test
    void createShouldReturnCreated() {
        given()
                .contentType("application/json")
                .cookie("jwtToken", jwtToken)
                .body("{\"id\": \"task4\", \"description\": \"Tarea 4\", \"length\": 5.0, \"predecessors\": []}")
                .when()
                .post("/authenticated/tasks")
                .then()
                .log().all()
                .statusCode(201)
                .body("id", equalTo("task4"))
                .body("description", equalTo("Tarea 4"))
                .body("length", equalTo(5.0f))
                .body("predecessors.size()", equalTo(0));
    }*/
}
