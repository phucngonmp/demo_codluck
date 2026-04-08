package org.example.demo.dto;

import java.util.List;

public record AccountDTO(String username, String email, List<String> roles) {
}
