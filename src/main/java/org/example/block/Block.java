package org.example.block;

import org.example.StringUtil;
import org.example.transaction.Transaction;

import java.util.ArrayList;
import java.util.Objects;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot; // 默克尔树根
    public ArrayList<Transaction> transactions = new ArrayList<>();
    private long timeStamp;
    private int nonce; // 随机数

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySha256(
                previousHash +
                Long.toString(timeStamp) +
                Integer.toString(nonce) +
                merkleRoot
        );
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        //System.out.println("Block Mined!!! : " + hash);
    }

    public void addTransaction(Transaction transaction) {
        if(transaction == null) {
            return;
        }
        if((!Objects.equals(previousHash, "0"))) {
            boolean result = transaction.processTransaction();
            if(!result) {
                System.out.println("Transaction failed to process. Discarded.");
                return;
            }
        }
        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
    }
}
