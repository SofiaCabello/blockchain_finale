package org.example;

import org.example.block.Block;
import org.example.block.Wallet;
import org.example.transaction.Transaction;
import org.example.transaction.TransactionOutput;

import java.awt.image.AreaAveragingScaleFilter;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static ConcurrentHashMap<String, TransactionOutput> UTXOs = new ConcurrentHashMap<>(); // 未使用的交易输出
    public static int difficulty = 5; // 难度
    public static float minimumTransaction = 0.1f; // 最小交易金额
    public static Wallet walletA; // 钱包A
    public static Wallet walletB; // 钱包B
    public static Wallet walletC;
    public static Transaction genesisTransaction; // 创世交易
    public static ArrayList<Node> nodeList = new ArrayList<>();
    public static HashMap<PublicKey, String> nodeIdMap = new HashMap<>();

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); // 设置Bouncey castle作为安全提供者
        // 以下是多线程测试
        Block genesisBlock = new Block("0");
        genesisBlock.mineBlock(difficulty);
        blockchain.add(genesisBlock);
        int numberOfNodes = 3;
        for(int i = 0; i < numberOfNodes; i++) {
            Node node = new Node("Node " + i, blockchain, UTXOs);
            nodeList.add(node);
            Transaction genesisTransaction = new Transaction(node.wallet.publicKey, node.wallet.publicKey, 100f, null);
            genesisTransaction.generateSignature(node.wallet.privateKey);
            genesisTransaction.transactionId = String.valueOf("0"); // 都属于创世交易，所以id都是0
            genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId));
            UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
            node.wallet.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        }
        nodeIdMap = getNodeIdMap();

        ArrayList<Thread> threadList = new ArrayList<>();
        for(Node node : nodeList) {
            Thread thread = new Thread(node);
            threadList.add(thread);
            thread.start();
        }

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Thread printBlockTransactionsHistoryThread = new Thread(() -> {
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printBlockTransactionsHistory();
        });
        printBlockTransactionsHistoryThread.start();


//        walletA = new Wallet();
//        walletB = new Wallet();
//        walletC = new Wallet();
//        Wallet coinbase = new Wallet();
//
//        // 创建创世交易，将100个币发送给walletA
//        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
//        genesisTransaction.generateSignature(coinbase.privateKey); // 对创世交易进行签名
//        genesisTransaction.transactionId = "0"; // 设置创世交易的id
//        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId)); // 将创世交易的输出添加到未使用的交易输出列表中
//        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // 将创世交易的输出添加到未使用的交易输出列表中
//
//        System.out.println("Creating and Mining Genesis block... ");
//        Block genesis = new Block("0");
//        genesis.addTransaction(genesisTransaction); // 将创世交易添加到区块中
//        addBlock(genesis); // 挖掘创世区块
//
//        // 测试
//        Block block1 = new Block(genesis.hash);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
//        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
//        addBlock(block1);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("WalletB's balance is: " + walletB.getBalance());
//
//        System.out.println("\nBlockChain is Valid: " + isChainValid());
//
//        Block block2 = new Block(block1.hash);
//        System.out.println("\nWalletA Attempting to send funds (20) to WalletC...");
//        block2.addTransaction(walletA.sendFunds(walletC.publicKey, 20f));
//        addBlock(block2);
//        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
//        System.out.println("WalletC's balance is: " + walletC.getBalance());

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0'); // 创建一个字符串，其中包含difficulty个0

        for(int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // 比较当前区块的hash和计算出来的hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }

            // 比较前一个区块的hash和当前区块的previousHash
            if(!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }

            // 检查hash是否被挖掘
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }

    public static HashMap<PublicKey, String> getNodeIdMap() {
        HashMap<PublicKey, String> nodeIdMap = new HashMap<>();
        for(Node node : nodeList) {
            nodeIdMap.put(node.wallet.publicKey, node.nodeId);
        }
        return nodeIdMap;
    }

    public static void printBlockTransactionsHistory() {
        System.out.println("-------------------PRINTING BLOCKCHAIN-------------------");
        for(Block block : blockchain){
            System.out.println("Block " + block.hash + " transactions: ");
            for(Transaction transaction : block.transactions){
                if(transaction.sender == null){
                    System.out.println("Reward transaction to Node: " + nodeIdMap.get(transaction.receiver));
                    System.out.println("Transaction amount: " + transaction.value);
                }else{
                    System.out.println(nodeIdMap.get(transaction.sender) + "---" + transaction.value + "-->" + nodeIdMap.get(transaction.receiver));
                    System.out.println("Transaction amount: " + transaction.value);

                }
            }
        }
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}