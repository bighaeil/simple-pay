package com.mypay.banking.adapter.out.external.bank;

import lombok.Data;

@Data
public class FirmBankingResult {
    private int resultCode; // 0: 성공, 1: 실패

    public FirmBankingResult(int resultCode) {
        this.resultCode = resultCode;
    }
}
