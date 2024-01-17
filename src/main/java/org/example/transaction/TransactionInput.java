package org.example.transaction;

public class TransactionInput {
    // 交易输入
    public String transactionOutputId; //对应于TransactionOutputs->id
    public TransactionOutput UTXO; //包含未使用的交易输出

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
