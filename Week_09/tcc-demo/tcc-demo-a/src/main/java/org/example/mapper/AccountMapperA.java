package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.Account;

@Mapper
public interface AccountMapperA {
    int updateDollar(Account account);
    int updateRMB(Account account);
}
