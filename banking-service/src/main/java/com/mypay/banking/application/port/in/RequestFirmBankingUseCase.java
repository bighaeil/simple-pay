package com.mypay.banking.application.port.in;

import com.mypay.banking.domain.FirmBankingRequest;

public interface RequestFirmBankingUseCase {
    FirmBankingRequest requestFirmBanking(RequestFirmBankingCommand command);
}
