package com.mypay.money.application.port.in;

import com.mypay.money.domain.MemberMoney;

public interface CreateMemberMoneyPort {
    void createMemberMoney(MemberMoney.MembershipId membershipId, MemberMoney.MoneyAggregateIdentifier aggregateIdentifier);
}
