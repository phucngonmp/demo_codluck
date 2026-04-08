package org.example.demo.mappers;

import org.example.demo.dto.AccountDTO;
import org.example.demo.entities.Account;
import org.example.demo.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "roles", expression = "java(mapRoles(account.getRoles()))")
    AccountDTO toDTO(Account account);

    default List<String> mapRoles(List<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(Role::getName).toList();
    }
}
