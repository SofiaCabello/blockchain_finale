package org.example.transaction;

import org.example.CryptoInput;
import org.example.Main;
import org.example.StringUtil;

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


    public Transaction(PublicKey from, PublicKey to, float value, List<TransactionInput> inputs) {
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash() {
        // 这样算更具有线程安全性
        long currentTimeMillis = System.currentTimeMillis();
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(receiver) +
                        Float.toString(value) +
                        currentTimeMillis
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
        if(sender == null) {
            return true;
        }

        // 验证交易签名
        if (!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // 获取每个输入的UTXO
        for (TransactionInput i : inputs) {
            i.UTXO = Main.UTXOs.get(i.transactionOutputId);
            // 验证UTXO是否存在
            if (i.UTXO == null) {
                System.out.println("#Referenced input on Transaction(" + transactionId + ") is Missing or Invalid");
                return false;
            }
        }

        // 检查交易是否满足最小交易量要求
        if (getInputsValue() < Main.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        // 计算交易金额和找零
        float totalInputValue = getInputsValue();
        float leftOver = totalInputValue - value;
        transactionId = calculateHash();

        // 生成交易输出
        outputs.add(new TransactionOutput(this.receiver, value, transactionId)); // 交易金额给接收方
        if (leftOver > 0) {
            outputs.add(new TransactionOutput(this.sender, leftOver, transactionId)); // 找零回给发送方
        }

        // 将交易输出添加到UTXOs列表
        for (TransactionOutput o : outputs) {
            Main.UTXOs.put(o.id, o);
        }

        // 移除交易输入引用的UTXO
        for (TransactionInput i : inputs) {
            if (i.UTXO != null) {
                Main.UTXOs.remove(i.UTXO.id);
            }
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
