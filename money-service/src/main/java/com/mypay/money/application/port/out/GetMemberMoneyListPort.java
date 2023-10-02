package com.mypay.money.application.port.out;

import com.mypay.money.adapter.out.persistence.MemberMoneyJpaEntity;

import java.util.List;

public interface GetMemberMoneyListPort {
    List<MemberMoneyJpaEntity> getMemberMoneyPort(List<String> membershipIds);
}
