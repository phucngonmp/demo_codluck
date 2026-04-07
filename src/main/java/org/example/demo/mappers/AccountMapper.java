package org.example.demo.mappers;

import org.example.demo.dto.AccountDTO;
import org.example.demo.entities.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDTO toDTO(Account account);
    Account toAccount(AccountDTO accountDTO);
}
