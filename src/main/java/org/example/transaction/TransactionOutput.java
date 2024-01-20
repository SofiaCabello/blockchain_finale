package org.example.transaction;

import org.example.util.StringUtil;

import java.security.PublicKey;

public class TransactionOutput {
    // 交易输出类
    public String id; // 交易输出的id
    public PublicKey receiver; //接收者的公钥
    public float value; //交易金额
    public String parentTransactionId; //交易的id

    public TransactionOutput(PublicKey receiver, float value, String parentTransactionId) {
        this.receiver = receiver;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(
                StringUtil.getStringFromKey(receiver) +
                Float.toString(value) +
                parentTransactionId
        );
    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey == receiver);
    }
}
