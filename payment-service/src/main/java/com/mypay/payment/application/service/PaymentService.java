package com.mypay.payment.application.service;

import com.mypay.common.UseCase;
import com.mypay.payment.application.port.in.RequestPaymentCommand;
import com.mypay.payment.application.port.in.RequestPaymentUseCase;
import com.mypay.payment.application.port.out.CreatePaymentPort;
import com.mypay.payment.application.port.out.GetMembershipPort;
import com.mypay.payment.application.port.out.GetRegisteredBankAccountPort;
import com.mypay.payment.domain.Payment;
import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional
public class PaymentService implements RequestPaymentUseCase {

    private final CreatePaymentPort createPaymentPort;
    private final GetMembershipPort getMembershipPort;
    private final GetRegisteredBankAccountPort getRegisteredBankAccountPort;

    // Todo Money Service -> Member Money 정보를 가져오기 위한 Port

    @Override
    public Payment requestPayment(RequestPaymentCommand command) {
        // 충전도, 멤버십, 머니 유효성 확인.....
        // getMembershipPort.getMembership(command.getRequestMembershipId());

        //getRegisteredBankAccountPort.getRegisteredBankAccount(command.getRequestMembershipId());

        //....

        // createPaymentPort
        return createPaymentPort.createPayment(
                command.getRequestMembershipId(),
                command.getRequestPrice(),
                command.getFranchiseId(),
                command.getFranchiseFeeRate());
    }
}
