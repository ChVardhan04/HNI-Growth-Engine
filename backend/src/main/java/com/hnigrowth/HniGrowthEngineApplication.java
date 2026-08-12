package com.hnigrowth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the HNI AI Growth Engine.
 *
 * An AI-powered Sales Intelligence Platform (not a CRUD app) that discovers
 * prospects, scores them, generates personalized outreach, tracks engagement,
 * computes intent, recommends next actions, and automates CRM workflows.
 */
@SpringBootApplication
@EnableScheduling
public class HniGrowthEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(HniGrowthEngineApplication.class, args);
    }
}
