package com.mypay.money.adapter.axon.saga;

import com.mypay.common.event.*;
import com.mypay.money.adapter.axon.event.RechargingRequestCreatedEvent;
import com.mypay.money.adapter.out.persistence.MemberMoneyJpaEntity;
import com.mypay.money.application.port.out.IncreaseMoneyPort;
import com.mypay.money.domain.MemberMoney;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
@NoArgsConstructor
public class MoneyRechargeSaga {

    @NonNull
    private transient CommandGateway commandGateway;

    @Autowired
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "rechargingRequestId")
    public void handle(RechargingRequestCreatedEvent event) {
        System.out.println("RechargingRequestCreatedEvent Start saga");

        String checkRegisteredBankAccountId = UUID.randomUUID().toString();
        SagaLifecycle.associateWith("checkRegisteredBankAccountId", checkRegisteredBankAccountId);

        // "충전 요청" 이 시작 되었다.

        // 뱅킹의 계좌 등록 여부 확인하기. (RegisteredBankAccount)
        // CheckRegisteredBankAccountCommand -> Check Bank Account
        // -> axon server -> Banking Service -> Common

        // 기본적으로 axon framework 에서, 모든 aggregate 의 변경은, aggregate 단위로 되어야만 한다.
        commandGateway.send(
                        new CheckRegisteredBankAccountCommand(
                                event.getRegisteredBankAccountAggregateIdentifier(),
                                event.getRechargingRequestId(),
                                event.getMembershipId(),
                                checkRegisteredBankAccountId,
                                event.getBankName(),
                                event.getBankAccountNumber(),
                                event.getAmount()
                        )
                )
                .whenComplete(
                        (result, throwable) -> {
                            if (throwable != null) {
                                throwable.printStackTrace();
                                System.out.println("CheckRegisteredBankAccountCommand Command failed");
                            } else {
                                System.out.println("CheckRegisteredBankAccountCommand Command success");
                            }
                        }
                );
    }

    @SagaEventHandler(associationProperty = "checkRegisteredBankAccountId")
    public void handle(CheckedRegisteredBankAccountEvent event) {
        System.out.println("CheckedRegisteredBankAccountEvent saga: " + event.toString());

        boolean status = event.isChecked();
        if (status) {
            System.out.println("CheckedRegisteredBankAccountEvent event success");
        } else {
            System.out.println("CheckedRegisteredBankAccountEvent event Failed");
        }

        String requestFirmBankingId = UUID.randomUUID().toString();
        SagaLifecycle.associateWith("requestFirmBankingId", requestFirmBankingId);

        // 송금 요청
        // 고객 계좌 -> 법인 계좌
        commandGateway.send(
                        new RequestFirmBankingCommand(
                                requestFirmBankingId,
                                event.getFirmBankingRequestAggregateIdentifier(),
                                event.getRechargingRequestId(),
                                event.getMembershipId(),
                                event.getFromBankName(),
                                event.getFromBankAccountNumber(),
                                "mypay",
                                "123456789",
                                event.getAmount()
                        )
                )
                .whenComplete(
                        (result, throwable) -> {
                            if (throwable != null) {
                                throwable.printStackTrace();
                                System.out.println("RequestFirmBankingCommand Command failed");
                            } else {
                                System.out.println("RequestFirmBankingCommand Command success");
                            }
                        }
                );
    }

    @SagaEventHandler(associationProperty = "requestFirmBankingId")
    public void handle(RequestFirmBankingFinishedEvent event, IncreaseMoneyPort increaseMoneyPort) {
        System.out.println("RequestFirmBankingFinishedEvent saga: " + event.toString());

        boolean status = event.getStatus() == 0;
        if (status) {
            System.out.println("RequestFirmBankingFinishedEvent event success");
        } else {
            System.out.println("RequestFirmBankingFinishedEvent event Failed");
        }

        // DB Update 명령.
        MemberMoneyJpaEntity resultEntity =
                increaseMoneyPort.increaseMoney(new MemberMoney.MembershipId(event.getMembershipId()), event.getMoneyAmount());

        if (resultEntity == null) {
            // 실패 시, 롤백 이벤트
            String rollbackFirmBankingId = UUID.randomUUID().toString();
            SagaLifecycle.associateWith("rollbackFirmBankingId", rollbackFirmBankingId);

            commandGateway.send(
                            new RollbackFirmBankingRequestCommand(
                                    rollbackFirmBankingId,
                                    event.getRequestFirmBankingAggregateIdentifier(),
                                    event.getRechargingRequestId(),
                                    event.getMembershipId(),
                                    event.getToBankName(),
                                    event.getToBankAccountNumber(),
                                    event.getMoneyAmount()
                            )
                    )
                    .whenComplete(
                            (result, throwable) -> {
                                if (throwable != null) {
                                    throwable.printStackTrace();
                                    System.out.println("RollbackFirmbankingRequestCommand Command failed");
                                } else {
                                    System.out.println("Saga success : " + result.toString());
                                    SagaLifecycle.end();
                                }
                            }
                    );
        } else {
            // 성공 시, saga 종료.
            SagaLifecycle.end();
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "rollbackFirmBankingId")
    public void handle(RollbackFirmBankingFinishedEvent event) {
        System.out.println("RollbackFirmBankingFinishedEvent saga: " + event.toString());
    }

}
