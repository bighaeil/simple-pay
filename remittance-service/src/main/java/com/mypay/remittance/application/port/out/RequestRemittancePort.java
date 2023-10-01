package com.mypay.remittance.application.port.out;

import com.mypay.remittance.adapter.out.persistence.RemittanceRequestJpaEntity;
import com.mypay.remittance.application.port.in.RequestRemittanceCommand;

public interface RequestRemittancePort {
    RemittanceRequestJpaEntity createRemittanceRequestHistory(RequestRemittanceCommand command);

    boolean saveRemittanceRequestHistory(RemittanceRequestJpaEntity entity);
}
