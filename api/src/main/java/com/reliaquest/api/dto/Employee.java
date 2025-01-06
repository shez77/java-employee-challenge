package com.reliaquest.api.dto;

import java.util.UUID;

/**
 * The Employee Entity retrieve from the internal service.
 * @param id
 * @param name
 * @param salary
 * @param age
 * @param title
 * @param email
 */
public record Employee(UUID id, String name, Integer salary, Integer age, String title, String email) {}
