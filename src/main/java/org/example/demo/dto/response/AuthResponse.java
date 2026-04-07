package org.example.demo.dto.response;

import org.example.demo.dto.AccountDTO;

public record AuthResponse(
   String accessToken,
   AccountDTO user
) {}
