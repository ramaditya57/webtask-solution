package com.ramaditya.webtask.webtask.service;

import com.ramaditya.webtask.webtask.model.FinalQueryRequest;
import com.ramaditya.webtask.webtask.model.GenerateWebhookRequest;
import com.ramaditya.webtask.webtask.model.GenerateWebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

@Service
public class StartupService implements CommandLineRunner {

    private final QuerySolverService solverService;
    private final RestTemplate rest;

    public StartupService(QuerySolverService solverService, RestTemplate rest) {
        this.solverService = solverService;
        this.rest = rest;
    }

    @Override
    public void run(String... args) throws Exception {

        // 🚫 Prevent command line input during Maven build
        if (System.getProperty("skip.startup") != null) {
            return;
        }

        Scanner scanner = new Scanner(System.in);

        // 🔹 Ask user for input
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter your regNo: ");
        String regNo = scanner.nextLine();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        // 1️⃣ Build request dynamically from user input
        GenerateWebhookRequest request = new GenerateWebhookRequest(
                name,
                regNo,
                email
        );

        // 2️⃣ Generate webhook URL
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        System.out.println("\nGenerating webhook...");
        GenerateWebhookResponse response =
                rest.postForObject(url, request, GenerateWebhookResponse.class);

        if (response == null || response.getWebhook() == null) {
            System.out.println("Failed to generate webhook. Exiting...");
            return;
        }

        String webhook = response.getWebhook();
        String token = response.getAccessToken();

        System.out.println("Webhook received: " + webhook);
        System.out.println("Access Token received.\n");

        // 3️⃣ Prepare SQL query based on regNo
        String sqlQuery = solverService.getFinalQuery(regNo);
        FinalQueryRequest finalQuery = new FinalQueryRequest(sqlQuery);

        // 4️⃣ Set headers with JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(finalQuery, headers);

        // 5️⃣ Send final query to webhook
        System.out.println("Submitting SQL Query...\n");
        rest.postForObject(webhook, entity, String.class);

        System.out.println("SQL query submitted successfully!");
    }
}
