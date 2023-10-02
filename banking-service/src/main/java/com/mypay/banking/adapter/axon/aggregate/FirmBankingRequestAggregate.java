package com.mypay.banking.adapter.axon.aggregate;

import com.mypay.banking.adapter.axon.command.CreateFirmBankingRequestCommand;
import com.mypay.banking.adapter.axon.command.UpdateFirmBankingRequestCommand;
import com.mypay.banking.adapter.axon.event.FirmBankingRequestCreatedEvent;
import com.mypay.banking.adapter.axon.event.FirmBankingRequestUpdatedEvent;
import lombok.Data;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

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
