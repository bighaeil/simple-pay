package com.mypay.banking.adapter.axon.aggregate;

import com.mypay.banking.adapter.axon.command.CreateFirmBankingRequestCommand;
import com.mypay.banking.adapter.axon.command.UpdateFirmBankingRequestCommand;
import com.mypay.banking.adapter.axon.event.FirmBankingRequestCreatedEvent;
import com.mypay.banking.adapter.axon.event.FirmBankingRequestUpdatedEvent;
import com.mypay.banking.adapter.out.external.bank.ExternalFirmBankingRequest;
import com.mypay.banking.adapter.out.external.bank.FirmBankingResult;
import com.mypay.banking.application.port.out.RequestExternalFirmBankingPort;
import com.mypay.banking.application.port.out.RequestFirmBankingPort;
import com.mypay.banking.domain.FirmBankingRequest;
import com.mypay.common.event.RequestFirmBankingCommand;
import com.mypay.common.event.RequestFirmBankingFinishedEvent;
import com.mypay.common.event.RollbackFirmBankingFinishedEvent;
import com.mypay.common.event.RollbackFirmBankingRequestCommand;
import lombok.Data;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate()
@Data
public class FirmBankingRequestAggregate {
    @AggregateIdentifier
    private String id;
    private String fromBankName;
    private String fromBankAccountNumber;
    private String toBankName;
    private String toBankAccountNumber;
    private int moneyAmount;
    private int firmBankingStatus;

    @CommandHandler
    public FirmBankingRequestAggregate(CreateFirmBankingRequestCommand command) {
        System.out.println("CreateFirmBankingRequestCommand Handler");

        apply(new FirmBankingRequestCreatedEvent(
                command.getFromBankName(),
                command.getFromBankAccountNumber(),
                command.getToBankName(),
                command.getToBankAccountNumber(),
                command.getMoneyAmount())
        );
    }

    @CommandHandler
    public FirmBankingRequestAggregate(RequestFirmBankingCommand command,
                                       RequestFirmBankingPort firmBankingPort,
                                       RequestExternalFirmBankingPort externalFirmBankingPort) {
        System.out.println("FirmBankingRequestAggregate Handler");

        id = command.getAggregateIdentifier();

        // from -> to
        // 펌뱅킹 수행!
        firmBankingPort.createFirmBankingRequest(
                new FirmBankingRequest.FromBankName(command.getToBankName()),
                new FirmBankingRequest.FromBankAccountNumber(command.getToBankAccountNumber()),
                new FirmBankingRequest.ToBankName("mybank"),
                new FirmBankingRequest.ToBankAccountNumber("123-456-7890"),
                new FirmBankingRequest.MoneyAmount(command.getMoneyAmount()),
                new FirmBankingRequest.FirmBankingStatus(0),
                new FirmBankingRequest.FirmBankingAggregateIdentifier(id)
        );

        // firmBanking!
        FirmBankingResult firmBankingResult = externalFirmBankingPort.requestExternalFirmBanking(
                new ExternalFirmBankingRequest(
                        command.getFromBankName(),
                        command.getFromBankAccountNumber(),
                        command.getToBankName(),
                        command.getToBankAccountNumber(),
                        command.getMoneyAmount()
                )
        );

        int resultCode = firmBankingResult.getResultCode();

        // 0. 성공, 1. 실패
        apply(
                new RequestFirmBankingFinishedEvent(
                        command.getRequestFirmBankingId(),
                        command.getRechargeRequestId(),
                        command.getMembershipId(),
                        command.getToBankName(),
                        command.getToBankAccountNumber(),
                        command.getMoneyAmount(),
                        resultCode,
                        id
                )
        );
    }

    @CommandHandler
    public FirmBankingRequestAggregate(@NotNull RollbackFirmBankingRequestCommand command,
                                       RequestFirmBankingPort firmBankingPort,
                                       RequestExternalFirmBankingPort externalFirmBankingPort) {
        System.out.println("RollbackFirmBankingRequestCommand Handler");

        id = UUID.randomUUID().toString();

        // rollback 수행 (-> 법인 계좌 -> 고객 계좌 펌뱅킹)
        firmBankingPort.createFirmBankingRequest(
                new FirmBankingRequest.FromBankName("mypay"),
                new FirmBankingRequest.FromBankAccountNumber("123-456-7890"),
                new FirmBankingRequest.ToBankName(command.getBankName()),
                new FirmBankingRequest.ToBankAccountNumber(command.getBankAccountNumber()),
                new FirmBankingRequest.MoneyAmount(command.getMoneyAmount()),
                new FirmBankingRequest.FirmBankingStatus(0),
                new FirmBankingRequest.FirmBankingAggregateIdentifier(id)
        );

        // firmBanking!
        FirmBankingResult result = externalFirmBankingPort.requestExternalFirmBanking(
                new ExternalFirmBankingRequest(
                        "mypay",
                        "123-456-7890",
                        command.getBankName(),
                        command.getBankAccountNumber(),
                        command.getMoneyAmount()
                )
        );

        int res = result.getResultCode();

        apply(
                new RollbackFirmBankingFinishedEvent(
                        command.getRollbackFirmBankingId(),
                        command.getMembershipId(),
                        id
                )
        );
    }

    public FirmBankingRequestAggregate() {
    }

    @CommandHandler
    public String handle(UpdateFirmBankingRequestCommand command) {
        System.out.println("UpdateFirmBankingRequestCommand Handler");

        id = command.getAggregateIdentifier();
        apply(new FirmBankingRequestUpdatedEvent(command.getFirmBankingStatus()));

        return id;
    }

    @EventSourcingHandler
    public void on(FirmBankingRequestCreatedEvent event) {
        System.out.println("FirmBankingRequestCreatedEvent Sourcing Handler");

        id = UUID.randomUUID().toString();
        fromBankName = event.getFromBankName();
        fromBankAccountNumber = event.getFromBankAccountNumber();
        toBankName = event.getToBankName();
        toBankAccountNumber = event.getToBankAccountNumber();
    }

    @EventSourcingHandler
    public void on(FirmBankingRequestUpdatedEvent event) {
        System.out.println("FirmBankingRequestUpdatedEvent Sourcing Handler");

        firmBankingStatus = event.getFirmBankingStatus();
    }
}
