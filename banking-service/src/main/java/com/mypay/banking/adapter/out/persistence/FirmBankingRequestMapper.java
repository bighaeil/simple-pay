package com.mypay.banking.adapter.out.persistence;

import com.mypay.banking.domain.FirmBankingRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FirmBankingRequestMapper {
    public FirmBankingRequest mapToDomainEntity(FirmBankingRequestJpaEntity entity, UUID uuid) {
        return FirmBankingRequest.generateFirmBankingRequest(
                new FirmBankingRequest.FirmBankingRequestId(entity.getRequestFirmBankingId() + ""),
                new FirmBankingRequest.FromBankName(entity.getFromBankName()),
                new FirmBankingRequest.FromBankAccountNumber(entity.getFromBankAccountNumber()),
                new FirmBankingRequest.ToBankName(entity.getToBankName()),
                new FirmBankingRequest.ToBankAccountNumber(entity.getToBankAccountNumber()),
                new FirmBankingRequest.MoneyAmount(entity.getMoneyAmount()),
                new FirmBankingRequest.FirmBankingStatus(entity.getFirmBankingStatus()),
                uuid
        );
    }
}
