package org.example;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Node implements Runnable{
    private String nodeId;
    Wallet wallet;
    private ArrayList<Block> blockchain;
    private HashMap<String, TransactionOutput> UTXOs;
    private ArrayList<Transaction> pendingTransactions;
    private final long runDuration = 10000;

    public Node(String nodeId, ArrayList<Block> blockchain, HashMap<String, TransactionOutput> UTXOs){
        this.nodeId = nodeId;
        this.wallet = new Wallet();
        this.blockchain = blockchain;
        this.UTXOs = UTXOs;
        this.pendingTransactions = new ArrayList<>();
    }

    @Override
    public void run(){
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < runDuration){
            processPendingTransactions();
            mineBlock();
        }
        printWalletBalance();
    }

    private void mineBlock() {
        if(!pendingTransactions.isEmpty()){
            System.out.println("Node " + nodeId + " is mining a block...");
            Block block = new Block(blockchain.get(blockchain.size() - 1).hash);
            for(Transaction transaction : pendingTransactions){
                block.addTransaction(transaction);
            }
            addBlock(block);
            System.out.println("Node " + nodeId + " mined a block: " + block.hash);
            pendingTransactions.clear();
        }
    }

    private void addBlock(Block newBlock) {
        newBlock.mineBlock(Main.difficulty);
        blockchain.add(newBlock);
    }

    private void processPendingTransactions() {
        Transaction newTransaction = wallet.sendFunds(wallet.publicKey, 10f);
        if(newTransaction != null){
            pendingTransactions.add(newTransaction);
        }
    }

    private void printWalletBalance() {
        System.out.println("Node " + nodeId + " wallet balance: " + wallet.getBalance());
    }
}
