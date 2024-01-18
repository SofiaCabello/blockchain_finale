package org.example;

import org.example.block.Block;
import org.example.block.Wallet;
import org.example.transaction.Transaction;
import org.example.transaction.TransactionOutput;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class Node implements Runnable{
    public String nodeId;  // 节点的唯一标识
    Wallet wallet;  // 每个节点都有一个钱包
    private ArrayList<Block> blockchain; // 区块链副本
    private HashMap<String, TransactionOutput> UTXOs;
    private ArrayList<Transaction> pendingTransactions; // 节点尚未处理的交易
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
            processPendingTransactions(); // 每个节点每次循环都会处理挂起的交易
            mineBlock();
            System.out.println("Node " + nodeId + " blockchain size: " + blockchain.size()); // 打印当前区块链长度
        }
        printWalletBalance();
    }

    private void mineBlock() {
        //在挖矿之前，先检查是否有挂起的交易
        if(!pendingTransactions.isEmpty()){
            // 如果有挂起的交易，则挖矿
            //System.out.println("Node " + nodeId + " is mining a block...");
            Block block = new Block(blockchain.get(blockchain.size() - 1).hash);
            // 激励机制
            Transaction coinbaseTx = new Transaction(null, wallet.publicKey, 12.5f, null);
            coinbaseTx.transactionId = "0";
            block.addTransaction(coinbaseTx);
            for(Transaction transaction : pendingTransactions){
                block.addTransaction(transaction); // 将挂起的交易添加到区块中，这样交易就被处理了
            }
            addBlock(block); // 将区块添加到区块链中
            // 创建激励交易
            pendingTransactions.clear();
        }
    }

    private void addBlock(Block newBlock) {
        newBlock.mineBlock(Main.difficulty);
        blockchain.add(newBlock);
    }

    private void processPendingTransactions() {
        // 处理挂起的交易
        // 发起随机交易
        PublicKey receiverKey = Main.nodeList.get((int)(Math.random() * Main.nodeList.size())).wallet.publicKey;
        Transaction newTransaction = wallet.sendFunds(receiverKey, 10f);
        if(newTransaction != null){
            pendingTransactions.add(newTransaction);
            System.out.println("From Node: " + newTransaction.sender + " to Node: " + newTransaction.receiver);
        }
    }

    private void printWalletBalance() {
        System.out.println("Node " + nodeId + " wallet balance: " + wallet.getBalance());
    }

    public String getNodeId() {
        return nodeId;
    }


}
