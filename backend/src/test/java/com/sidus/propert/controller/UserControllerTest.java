package com.sidus.propert.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sidus.propert.model.entity.User;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class UserControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    //Test createUsers method with REST Assured
    @Test
    void createUsersShouldReturnCreated() throws JsonProcessingException {
        User u = new User();
        u.setId("seva@gmail.com");
        u.setUsername("testuser");
        u.setPassword("testpassword");

        //Convert the user to JSON String
        String jsonUser = new ObjectMapper().writeValueAsString(u);

        given()
                .contentType("application/json")
                .body(jsonUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .body("id", equalTo("seva@gmail.com"))
                .body("username", equalTo("testuser"));


    }

    @Test
    void createUsersShouldReturn400DuplicatedUser() throws JsonProcessingException {
        User u = new User();
        u.setId("seva@gmail.com");
        u.setUsername("testuser");
        u.setPassword("testpassword");
        //Convert the user to JSON String
        String jsonUser = new ObjectMapper().writeValueAsString(u);

        given()
                .contentType("application/json")
                .body(jsonUser)
        .when()
                .post("/users");

        //Try to create the same user again
        given()
                .contentType("application/json")
                .body(jsonUser)
        .when()
                .post("/users")
        .then()
                .statusCode(400);
    }

    //create should return Validation Exception when username is null
    @Test
    void createUsersShouldReturn400WhenUsernameIsNull() throws JsonProcessingException {
        User u = new User();
        u.setId("seva@gmail.com");
        u.setUsername(null);
        u.setPassword("testpassword");

        //Convert the user to JSON String
        String jsonUser = new ObjectMapper().writeValueAsString(u);

        Response response = given()
                .contentType("application/json")
                .body(jsonUser)
        .when()
                .post("/users");

        List<Map<String, Object>> messages = response
        .then()
                .statusCode(400)
                .extract()
                .jsonPath()
                .getList("message");

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).get("field")).isEqualTo("username");

    }
}
