package com.mypay.banking.application.service;

import com.mypay.banking.adapter.axon.command.CreateFirmBankingRequestCommand;
import com.mypay.banking.adapter.axon.command.UpdateFirmBankingRequestCommand;
import com.mypay.banking.adapter.out.external.bank.ExternalFirmBankingRequest;
import com.mypay.banking.adapter.out.external.bank.FirmBankingResult;
import com.mypay.banking.adapter.out.persistence.FirmBankingRequestJpaEntity;
import com.mypay.banking.adapter.out.persistence.FirmBankingRequestMapper;
import com.mypay.banking.application.port.in.RequestFirmBankingCommand;
import com.mypay.banking.application.port.in.RequestFirmBankingUseCase;
import com.mypay.banking.application.port.in.UpdateFirmBankingCommand;
import com.mypay.banking.application.port.in.UpdateFirmBankingUseCase;
import com.mypay.banking.application.port.out.RequestExternalFirmBankingPort;
import com.mypay.banking.application.port.out.RequestFirmBankingPort;
import com.mypay.banking.domain.FirmBankingRequest;
import com.mypay.common.UseCase;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;

import javax.transaction.Transactional;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Transactional
public class RequestFirmBankingService implements RequestFirmBankingUseCase, UpdateFirmBankingUseCase {
    private final FirmBankingRequestMapper mapper;
    private final RequestFirmBankingPort requestFirmBankingPort;
    private final RequestExternalFirmBankingPort requestExternalFirmBankingPort;
    private final CommandGateway commandGateway;

    @Override
    public FirmBankingRequest requestFirmBanking(RequestFirmBankingCommand command) {
        // Business Logic
        // a -> b 계좌

        // 1. 요청에 대해 정보를 먼저 write . "요청" 상태로
        FirmBankingRequestJpaEntity entity = requestFirmBankingPort.createFirmBankingRequest(
                new FirmBankingRequest.FromBankName(command.getFromBankName()),
                new FirmBankingRequest.FromBankAccountNumber(command.getFromBankAccountNumber()),
                new FirmBankingRequest.ToBankName(command.getToBankName()),
                new FirmBankingRequest.ToBankAccountNumber(command.getToBankAccountNumber()),
                new FirmBankingRequest.MoneyAmount(command.getMoneyAmount()),
                new FirmBankingRequest.FirmBankingStatus(0),
                new FirmBankingRequest.FirmBankingAggregateIdentifier("")
        );

        // 2. 외부 은행에 펌뱅킹 요청
        FirmBankingResult result = requestExternalFirmBankingPort.requestExternalFirmBanking(
                new ExternalFirmBankingRequest(
                        command.getFromBankName(),
                        command.getFromBankAccountNumber(),
                        command.getToBankName(),
                        command.getToBankAccountNumber(),
                        command.getMoneyAmount()
                )
        );

        // Transactional UUID
        UUID randomUUID = UUID.randomUUID();
        entity.setUuid(randomUUID.toString());

        // 3. 결과에 따라서 1번에서 작성했던 FirmBankingRequest 정보를 Update
        if (result.getResultCode() == 0) {
            // 성공
            entity.setFirmBankingStatus(1);
        } else {
            // 실패
            entity.setFirmBankingStatus(2);
        }

        // 4. 결과를 리턴하기 전에 바뀐 상태 값을 기준으로 다시 save
        return mapper.mapToDomainEntity(requestFirmBankingPort.modifyFirmBankingRequest(entity), randomUUID);
    }

    @Override
    public void requestFirmBankingByEvent(RequestFirmBankingCommand command) {
        CreateFirmBankingRequestCommand createFirmBankingRequestCommand = CreateFirmBankingRequestCommand.builder()
                .toBankName(command.getToBankName())
                .toBankAccountNumber(command.getToBankAccountNumber())
                .fromBankName(command.getFromBankName())
                .fromBankAccountNumber(command.getFromBankAccountNumber())
                .moneyAmount(command.getMoneyAmount())
                .build();

        commandGateway.send(createFirmBankingRequestCommand).whenComplete(
                (result, throwable) -> {
                    if (throwable != null) {
                        // 실패
                        throwable.printStackTrace();
                    } else {
                        System.out.println("createFirmBankingRequestCommand completed, Aggregate ID: " + result.toString());

                        // Request FirmBanking 의 DB save
                        FirmBankingRequestJpaEntity requestJpaEntity = requestFirmBankingPort.createFirmBankingRequest(
                                new FirmBankingRequest.FromBankName(command.getFromBankName()),
                                new FirmBankingRequest.FromBankAccountNumber(command.getFromBankAccountNumber()),
                                new FirmBankingRequest.ToBankName(command.getToBankName()),
                                new FirmBankingRequest.ToBankAccountNumber(command.getToBankAccountNumber()),
                                new FirmBankingRequest.MoneyAmount(command.getMoneyAmount()),
                                new FirmBankingRequest.FirmBankingStatus(0),
                                new FirmBankingRequest.FirmBankingAggregateIdentifier(result.toString())
                        );

                        // 은행에 펌뱅킹 요청
                        FirmBankingResult firmBankingResult = requestExternalFirmBankingPort.requestExternalFirmBanking(
                                new ExternalFirmBankingRequest(
                                        command.getFromBankName(),
                                        command.getFromBankAccountNumber(),
                                        command.getToBankName(),
                                        command.getToBankAccountNumber(),
                                        command.getMoneyAmount()
                                )
                        );

                        // 결과에 따라서 DB save
                        // 3. 결과에 따라서 1번에서 작성했던 FirmBankingRequest 정보를 Update
                        if (firmBankingResult.getResultCode() == 0) {
                            // 성공
                            requestJpaEntity.setFirmBankingStatus(1);
                        } else {
                            // 실패
                            requestJpaEntity.setFirmBankingStatus(2);
                        }

                        requestFirmBankingPort.modifyFirmBankingRequest(requestJpaEntity);
                    }
                }
        );
        // Command -> Event Sourcing
    }

    @Override
    public void updateFirmBankingByEvent(UpdateFirmBankingCommand command) {
        // command.
        UpdateFirmBankingRequestCommand updateFirmBankingRequestCommand = new UpdateFirmBankingRequestCommand(
                command.getFirmBankingAggregateIdentifier(), command.getFirmBankingStatus()
        );

        commandGateway.send(updateFirmBankingRequestCommand).whenComplete(
                (result, throwable) -> {
                    if (throwable != null) {
                        // 실패
                        throwable.printStackTrace();
                    } else {
                        System.out.println("updateFirmBankingRequestCommand completed, Aggregate ID: " + result.toString());

                        FirmBankingRequestJpaEntity entity = requestFirmBankingPort.getFirmBankingRequest(
                                new FirmBankingRequest.FirmBankingAggregateIdentifier(command.getFirmBankingAggregateIdentifier())
                        );

                        // status 의 변경으로 인한 외부 은행과의 커뮤니케이션
                        // if rollback -> 0, status 변경도 해주겠지만
                        // + 기존 펌뱅킹 정보에서 from <-> to 가 변경된 펌뱅킹을 요청하는 새로운 요청
                        entity.setFirmBankingStatus(command.getFirmBankingStatus());
                        requestFirmBankingPort.modifyFirmBankingRequest(entity);
                    }
                }
        );
    }
}
