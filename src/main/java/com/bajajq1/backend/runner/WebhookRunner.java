package com.bajajq1.backend.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;

    public WebhookRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Step 1: Prepare request body
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Harshita Panwar");
        requestBody.put("regNo", "0827CI221059");
        requestBody.put("email", "harshitapanwar220819@acropolis.in");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // Step 2: Send request
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String webhookUrl = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            System.out.println("Webhook: " + webhookUrl);
            System.out.println("Token: " + accessToken);

            // Step 3: Submit your final query
            submitFinalQuery(webhookUrl, accessToken);
        } else {
            System.out.println("Failed to get webhook.");
        }
    }

    private void submitFinalQuery(String webhookUrl, String accessToken) {
        String finalQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) != 1 " +
                "AND p.AMOUNT = ( " +
                "SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(PAYMENT_TIME) != 1 " +
                ");";
        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

        System.out.println("Response: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());

        System.out.println("AccessToken: " + accessToken);
        System.out.println("Webhook URL: " + webhookUrl);

    }
}
