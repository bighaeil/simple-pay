package com.mypay.banking.adapter.in.web;

import com.mypay.banking.application.port.in.RequestFirmBankingCommand;
import com.mypay.banking.application.port.in.RequestFirmBankingUseCase;
import com.mypay.banking.application.port.in.UpdateFirmBankingCommand;
import com.mypay.banking.application.port.in.UpdateFirmBankingUseCase;
import com.mypay.banking.domain.FirmBankingRequest;
import com.mypay.common.WebAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebAdapter
@RestController
@RequiredArgsConstructor
public class RequestFirmBankingController {

    private final RequestFirmBankingUseCase requestFirmBankingUseCase;
    private final UpdateFirmBankingUseCase updateFirmBankingUseCase;

    @PostMapping(path = "/banking/firm-banking/request")
    FirmBankingRequest requestFirmBanking(@RequestBody RequestFirmbankingRequest request) {
        RequestFirmBankingCommand command = RequestFirmBankingCommand.builder()
                .toBankName(request.getToBankName())
                .toBankAccountNumber(request.getToBankAccountNumber())
                .fromBankName(request.getFromBankName())
                .fromBankAccountNumber(request.getFromBankAccountNumber())
                .moneyAmount(request.getMoneyAmount())
                .build();

        return requestFirmBankingUseCase.requestFirmBanking(command);
    }

    @PostMapping(path = "/banking/firm-banking/request-eda")
    void requestFirmBankingByEvent(@RequestBody RequestFirmbankingRequest request) {
        RequestFirmBankingCommand command = RequestFirmBankingCommand.builder()
                .toBankName(request.getToBankName())
                .toBankAccountNumber(request.getToBankAccountNumber())
                .fromBankName(request.getFromBankName())
                .fromBankAccountNumber(request.getFromBankAccountNumber())
                .moneyAmount(request.getMoneyAmount())
                .build();

        requestFirmBankingUseCase.requestFirmBankingByEvent(command);
    }

    @PutMapping(path = "/banking/firm-banking/update-eda")
    void updateFirmBankingByEvent(@RequestBody UpdateFirmBankingRequest request) {
        UpdateFirmBankingCommand command = UpdateFirmBankingCommand.builder()
                .firmBankingAggregateIdentifier(request.getFirmBankingRequestAggregateIdentifier())
                .firmBankingStatus(request.getStatus())
                .build();

        updateFirmBankingUseCase.updateFirmBankingByEvent(command);
    }
}
