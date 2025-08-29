package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> body = new HashMap<>();
        body.put("name", "John Doe");
        body.put("regNo", "REG12347");
        body.put("email", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(generateWebhookUrl, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String webhookUrl = jsonResponse.get("webhook").asText();
            String accessToken = jsonResponse.get("accessToken").asText();

            String finalQuery = "SELECT p.AMOUNT AS SALARY, " +
                    "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                    "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                    "d.DEPARTMENT_NAME " +
                    "FROM PAYMENTS p " +
                    "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                    "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                    "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                    "ORDER BY p.AMOUNT DESC " +
                    "LIMIT 1;";

            Map<String, String> queryBody = new HashMap<>();
            queryBody.put("finalQuery", finalQuery);

            HttpHeaders queryHeaders = new HttpHeaders();
            queryHeaders.setContentType(MediaType.APPLICATION_JSON);
            queryHeaders.setBearerAuth(accessToken);

            HttpEntity<Map<String, String>> queryRequest = new HttpEntity<>(queryBody, queryHeaders);

            ResponseEntity<String> queryResponse = restTemplate.postForEntity(webhookUrl, queryRequest, String.class);

            System.out.println("Response from Webhook: " + queryResponse.getBody());
        } else {
            System.err.println("Failed to generate webhook!");
        }
    }
}
