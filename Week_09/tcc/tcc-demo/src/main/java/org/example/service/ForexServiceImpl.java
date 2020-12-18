package org.example.service;


import org.dromara.hmily.annotation.HmilyTCC;
import org.example.entity.Account;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class ForexServiceImpl implements ForexService{
    @DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12345")
    private AccountServiceA accountServiceA;

    @DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12346")
    private AccountServiceB accountServiceB;

    @HmilyTCC(confirmMethod = "confirmOrderStatus", cancelMethod = "cancelOrderStatus")
    public void forexHandle() {
        accountServiceA.updateDollar(Account.builder().userId(1).money(0).build());
        accountServiceA.updateRMB(Account.builder().userId(1).money(7).build());

        accountServiceB.updateDollar(Account.builder().userId(2).money(1).build());
        accountServiceB.updateRMB(Account.builder().userId(2).money(0).build());
    }

    public void confirmOrderStatus() {
        System.out.println("=========交易confirm操作完成================");
    }

    public void cancelOrderStatus() {
        System.out.println("=========交易confirm操作完成================");
    }
}
