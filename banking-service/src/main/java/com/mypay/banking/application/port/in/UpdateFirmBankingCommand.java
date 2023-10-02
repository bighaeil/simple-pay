package com.mypay.banking.application.port.in;

import com.mypay.common.SelfValidating;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
public class UpdateFirmBankingCommand extends SelfValidating<RequestFirmBankingCommand> {
    @NotNull
    private final String firmBankingAggregateIdentifier;

    @NotNull
    private final int firmBankingStatus;
}
