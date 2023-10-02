package com.mypay.banking.adapter.axon.aggregate;

import com.mypay.banking.adapter.axon.command.CreateRegisteredBankAccountCommand;
import com.mypay.banking.adapter.axon.event.CreateRegisteredBankAccountEvent;
import com.mypay.banking.adapter.out.external.bank.BankAccount;
import com.mypay.banking.adapter.out.external.bank.GetBankAccountRequest;
import com.mypay.banking.application.port.out.RequestBankAccountInfoPort;
import com.mypay.common.event.CheckRegisteredBankAccountCommand;
import com.mypay.common.event.CheckedRegisteredBankAccountEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import javax.validation.constraints.NotNull;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate()
public class RegisteredBankAccountAggregate {
    @AggregateIdentifier
    private String id;
    private String membershipId;
    private String bankName;
    private String bankAccountNumber;

    @CommandHandler
    public RegisteredBankAccountAggregate(CreateRegisteredBankAccountCommand command) {
        System.out.println("CreateRegisteredBankAccountCommand Sourcing Handler");

        apply(new CreateRegisteredBankAccountEvent(command.getMembershipId(), command.getBankName(), command.getBankAccountNumber()));
    }

    public RegisteredBankAccountAggregate() {
    }

    @CommandHandler
    public void handle(@NotNull CheckRegisteredBankAccountCommand command, RequestBankAccountInfoPort bankAccountInfoPort) {
        System.out.println("CheckRegisteredBankAccountCommand Handler");

        // command 를 통해, 이 어그리거트(RegisteredBankAccount) 가 정상인지를 확인해야해요.
        id = command.getAggregateIdentifier();

        // Check! Registerd Bank Account
        BankAccount account = bankAccountInfoPort.getBankAccountInfo(new GetBankAccountRequest(command.getBankName(), command.getBankAccountNumber()));
        boolean isValidAccount = account.isValid();

        String firmBankingUUID = UUID.randomUUID().toString();

        // CheckedRegisteredBankAccountEvent
        apply(
                new CheckedRegisteredBankAccountEvent(
                        command.getRechargeRequestId(),
                        command.getCheckRegisteredBankAccountId(),
                        command.getMembershipId(),
                        isValidAccount,
                        command.getAmount(),
                        firmBankingUUID,
                        account.getBankName(),
                        account.getBankAccountNumber()
                )
        );
    }

    @EventSourcingHandler
    public void on(CreateRegisteredBankAccountEvent event) {
        System.out.println("CreateRegisteredBankAccountEvent Sourcing Handler");

        id = UUID.randomUUID().toString();
        membershipId = event.getMembershipId();
        bankName = event.getBankName();
        bankAccountNumber = event.getBankAccountNumber();
    }

}
