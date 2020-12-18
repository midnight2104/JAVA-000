package org.example.service;

import org.example.entity.Account;
import org.example.mapper.AccountMapperA;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService(version = "1.0.0")
public class AccountServiceAImpl implements AccountServiceA{
    @Resource
    private AccountMapperA accountMapper;

    public int updateDollar(Account account){
        return accountMapper.updateDollar(account);
    }

    public int updateRMB(Account account){
        return accountMapper.updateRMB(account);
    }
}
