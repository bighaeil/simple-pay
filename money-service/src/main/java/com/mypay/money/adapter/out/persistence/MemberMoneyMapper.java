package com.mypay.money.adapter.out.persistence;

import com.mypay.money.domain.MemberMoney;
import org.springframework.stereotype.Component;

@Component
public class MemberMoneyMapper {
    public MemberMoney mapToDomainEntity(MemberMoneyJpaEntity memberMoneyJpaEntity) {
        return MemberMoney.generateMemberMoney(
                new MemberMoney.MemberMoneyId(memberMoneyJpaEntity.getMemberMoneyId() + ""),
                new MemberMoney.MembershipId(memberMoneyJpaEntity.getMembershipId() + ""),
                new MemberMoney.MoneyBalance(memberMoneyJpaEntity.getBalance())
        );
    }
}