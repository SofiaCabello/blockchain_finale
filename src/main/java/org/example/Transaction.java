package org.example;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    public String transactionId;   // 交易的唯一标识
    public PublicKey sender;
    public PublicKey receiver;
    public float value;
    public byte[] signature;    // 交易的签名
    public List<TransactionInput> inputs;  // 交易的输入列表
    public List<TransactionOutput> outputs = new ArrayList<>(); // 交易的输出列表

    private static int sequence = 0;  // 用于记录交易的数量

    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(receiver) +
                Float.toString(value) +
                sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(value);
        signature = CryptoInput.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(value);
        return CryptoInput.verifyECDSASig(sender, data, signature);
    }

    public boolean processTransaction() {
        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        for(TransactionInput i : inputs) {
            i.UTXO = Main.UTXOs.get(i.transactionOutputId);
        }

        if(getInputsValue() < Main.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.receiver, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        for(TransactionOutput o : outputs) {
            Main.UTXOs.put(o.id, o);
        }

        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;
            Main.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue;
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }
}
