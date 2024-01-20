package org.example;

import org.example.block.Block;
import org.example.block.Wallet;
import org.example.transaction.Transaction;
import org.example.transaction.TransactionOutput;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

public class Node implements Runnable{
    public String nodeId;  // 节点的唯一标识
    Wallet wallet;  // 每个节点都有一个钱包
    private ArrayList<Block> blockchain; // 区块链副本
    private ConcurrentHashMap<String, TransactionOutput> UTXOs;
    private ArrayList<Transaction> pendingTransactions; // 节点尚未处理的交易
    private final long runDuration = 10000;
    private final int runTimes = 10;

    public Node(String nodeId, ArrayList<Block> blockchain, ConcurrentHashMap<String, TransactionOutput> UTXOs){
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
            giveReward();
            System.out.println("Node " + nodeId + " blockchain size: " + blockchain.size()); // 打印当前区块链长度
            printWalletBalance();
            try{
                sleep(1000);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        printWalletBalance();
    }

    private void mineBlock() {
        //在挖矿之前，先检查是否有挂起的交易
        if(!pendingTransactions.isEmpty()){
            // 如果有挂起的交易，则挖矿
            //System.out.println("Node " + nodeId + " is mining a block...");
            Block block = new Block(blockchain.get(blockchain.size() - 1).hash);
            for(Transaction transaction : pendingTransactions){
                block.addTransaction(transaction); // 将挂起的交易添加到区块中，这样交易就被处理了
            }
            addBlock(block); // 将区块添加到区块链中
            // 创建激励交易
            pendingTransactions.clear();
        }
    }

    private void giveReward() {
        // 创建激励交易
        Transaction rewardTransaction = new Transaction(null, wallet.publicKey, 15f, null);
        rewardTransaction.transactionId = String.valueOf("0");
        rewardTransaction.outputs.add(new TransactionOutput(rewardTransaction.receiver, rewardTransaction.value, rewardTransaction.transactionId));
        UTXOs.put(rewardTransaction.outputs.get(0).id, rewardTransaction.outputs.get(0));
        wallet.UTXOs.put(rewardTransaction.outputs.get(0).id, rewardTransaction.outputs.get(0));
        pendingTransactions.add(rewardTransaction);
        System.out.println("Give reward to " + nodeId);
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
            System.out.println("Node " + nodeId + "---10.0--->" + Main.nodeIdMap.get(receiverKey));
        }
    }

    private void printWalletBalance() {
        System.out.println("Node " + nodeId + " wallet balance: " + wallet.getBalance());
    }

    public String getNodeId() {
        return nodeId;
    }


}
