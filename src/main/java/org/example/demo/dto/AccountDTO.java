package org.example.demo.dto;

import org.example.demo.common.Role;

public record AccountDTO(String username, String email, Role role) {
}
