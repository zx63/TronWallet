package org.tron.MyUtils;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.entity.EntityMeta;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletcli.Client;
import org.tron.walletserver.WalletClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShareData {

    static final Logger logger = LoggerFactory.getLogger(ShareData.class);

    public static boolean isLogin = false;
    public static SimpleObjectProperty<Boolean> isCold = new SimpleObjectProperty<>(false);

    private static String TAG_PASSWORD = "tag_password";
    private static String TAG_ADDRESS = "tag_address";
    private static String TAG_ACCOUNT = "tag_account";
    private static String TAG_PRIVATE_KEY = "tag_private_key";
    private static String TAG_BALANCE = "tag_balance";
    private static String TAG_FROZEN_BALANCE = "tag_frozen_balance";
    private static String TAG_VOTE_COUNT = "tag_vote_count";
    private static String TAG_BAND_WIDTH = "tag_band_width";
    private static String TAG_TO_TRANSACTION = "tag_to_transaction";
    private static String TAG_FROM_TRANSACTION = "tag_from_transaction";
    private static String TAG_TOKEN = "tag_token";
    private static String TAG_WITNESS = "tag_witness";

    private static Map<String, Object> data = new HashMap<>();

    public static String getPassword() {
        return (String) data.get(TAG_PASSWORD);
    }

    public static void setPassword(String password) {
        data.put(TAG_PASSWORD, password);
    }

    public static String getAddress() {
        return (String) data.get(TAG_ADDRESS);
    }


    public static List<Protocol.Transaction> pendingTransaction = new ArrayList<>();

    public static void setAddress(String address) {
        if (address == null || address.length() == 0) {
            return;
        }
        if (StringUtils.equals(getAddress(), address)) {
            return;
        }
        data.put(TAG_ADDRESS, address);

        EntityMeta entityMeta = new EntityMeta();
        entityMeta.address = address;
        entityMeta.cold = ShareData.isCold.get() ? 1 : 0;

        SQLiteUtil.setMetaEntity(entityMeta);
    }

    public static String getBalance() {
        return (String) data.get(TAG_BALANCE);
    }

    public static void setBalance(String balance) {
        data.put(TAG_BALANCE, balance);
    }

    public static Protocol.Account getAccount() {
        return (Protocol.Account) data.get(TAG_ACCOUNT);
    }

    public static void setAccount(Protocol.Account account) {
        data.put(TAG_ACCOUNT, account);
    }

    public static String getPrivateKey() {
        return (String) data.get(TAG_PRIVATE_KEY);
    }

    public static void setPrivateKey(String privateKey) {
        data.put(TAG_PRIVATE_KEY, privateKey);
    }

    public static String getFrozenBalance() {
        return (String) data.get(TAG_FROZEN_BALANCE);
    }

    public static void setFrozenBalance(String frozenBalance) {
        data.put(TAG_FROZEN_BALANCE, frozenBalance);
    }

    public static String getVoteCount() {
        return (String) data.get(TAG_VOTE_COUNT);
    }

    public static void setVoteCount(String voteCount) {
        data.put(TAG_VOTE_COUNT, voteCount);
    }

    public static void setTokenList(List<Contract.AssetIssueContract> tokenList) {
        data.put(TAG_TOKEN, tokenList);
    }

    public static List<Contract.AssetIssueContract> getTokenList() {
        return (List<Contract.AssetIssueContract>) data.get(TAG_TOKEN);
    }

    public static void setWitnessList(List<Protocol.Witness> witnessList) {
        data.put(TAG_WITNESS, witnessList);
    }

    public static List<Protocol.Witness> getWitnessList() {
        return (List<Protocol.Witness>) data.get(TAG_WITNESS);
    }


    public static void setToTransactionList(List<Protocol.Transaction> toThisList) {
        data.put(TAG_TO_TRANSACTION, toThisList);
    }

    public static List<Protocol.Transaction> getToTransactionList() {
        return (List<Protocol.Transaction>) data.get(TAG_TO_TRANSACTION);
    }

    public static void setFromTransactionList(List<Protocol.Transaction> fromThisList) {
        data.put(TAG_FROM_TRANSACTION, fromThisList);
    }

    public static List<Protocol.Transaction> getFromTransactionList() {
        return (List<Protocol.Transaction>) data.get(TAG_FROM_TRANSACTION);
    }

    public static void setBandwidth(String bandwidth) {
        data.put(TAG_BAND_WIDTH, bandwidth);
    }

    public static String getBandwidth() {
        return (String) data.get(TAG_BAND_WIDTH);
    }

    public static SimpleObjectProperty<String> addressSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> balanceSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> balanceTmpSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> freezeSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> tronPowerSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> tronPowerTmpSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<String> bandWidthSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<Protocol.Account> accountSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<List<Protocol.Witness>> witnessSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<List<Protocol.Transaction>> transactionSimpleObjectProperty = new SimpleObjectProperty<>();
    public static SimpleObjectProperty<List<Contract.AssetIssueContract>> tokenSimpleObjectProperty = new SimpleObjectProperty<>();

    public static ScheduledExecutorService accountExecutor = Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService toTransactionExecutor = Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService fromTransactionExecutor = Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService tokenExecutor = Executors.newSingleThreadScheduledExecutor();
    public static ScheduledExecutorService witnessExecutor = Executors.newSingleThreadScheduledExecutor();

    static {

        accountExecutor.scheduleWithFixedDelay(() -> {
            if (isCold.get()) {
                return;
            }
            try {
                Client client = Client.getInstance();
                if (ShareData.getPassword() == null || ShareData.getPassword().isEmpty()) {
                    return;
                }
                if (client.login(ShareData.getPassword())) {
                    Protocol.Account account = client.queryAccount();
                    if (client.getAddress() == null || client.getAddress().isEmpty()) {
                        return;
                    }

                    long frozenBalance = 0;
                    if (account.getFrozenCount() > 0) {
                        for (Protocol.Account.Frozen frozen : account.getFrozenList()) {
                            frozenBalance += frozen.getFrozenBalance();
                        }
                    }

                    long voteCount = 0;
                    if (account.getVotesCount() > 0) {
                        for (Protocol.Vote vote : account.getVotesList()) {
                            voteCount += vote.getVoteCount();
                        }
                    }

                    ShareData.setAccount(account);
                    ShareData.setFrozenBalance(String.valueOf(frozenBalance / Config.DROP_UNIT));
                    ShareData.setVoteCount(String.valueOf(voteCount));
                    ShareData.setBandwidth(String.valueOf(account.getFreeNetUsage() + account.getNetUsage()));
                    ShareData.setBalance(String.valueOf(account.getBalance() / Config.DROP_UNIT));
                    ShareData.setAddress(client.getAddress());
                }
                Platform.runLater(() -> {
                    addressSimpleObjectProperty.set(ShareData.getAddress());
                    if (ShareData.getAccount() != null) {
                        accountSimpleObjectProperty.set(null);
                    }
                    accountSimpleObjectProperty.set(ShareData.getAccount());
                    balanceSimpleObjectProperty.set(ShareData.getBalance());
                    freezeSimpleObjectProperty.set(ShareData.getFrozenBalance());
                    if (ShareData.getFrozenBalance() != null && ShareData.getVoteCount() != null) {
                        tronPowerSimpleObjectProperty.set(String.valueOf(Long.valueOf(ShareData.getFrozenBalance()) - Long.valueOf(ShareData.getVoteCount())));
                    }
                    bandWidthSimpleObjectProperty.set(ShareData.getBandwidth());
                });
            } catch (Throwable t) {
                logger.error("Unhandled exception", t);
            }
        }, 1, 2, TimeUnit.SECONDS);

        fromTransactionExecutor.scheduleWithFixedDelay(() -> {
            if (isCold.get()) {
                return;
            }
            try {
                Client client = Client.getInstance();
                if (ShareData.getPassword() == null || ShareData.getPassword().isEmpty()) {
                    return;
                }
                if (client.login(ShareData.getPassword())) {
                    if (client.getAddress() == null || client.getAddress().isEmpty()) {
                        return;
                    }

                    byte[] addressBytes = WalletClient.decodeFromBase58Check(client.getAddress());
                    if (addressBytes == null) {
                        return;
                    }

                    int transactionFromListCount = (int) WalletClient.getTransactionsFromThisCount(addressBytes).getNum();
                    GrpcAPI.TransactionList transactionFromList = WalletClient.getTransactionsFromThis(addressBytes, (transactionFromListCount - 100 <= 0) ? 0 : (transactionFromListCount - 100), 100).get();

                    if (transactionFromList != null) {
                        ShareData.setFromTransactionList(transactionFromList.getTransactionList());
                    }
                }
                Platform.runLater(() -> {
                    List<Protocol.Transaction> tList = new ArrayList<>();
                    if (ShareData.getFromTransactionList() != null)
                        tList.addAll(ShareData.getFromTransactionList());
                    if (ShareData.getToTransactionList() != null)
                        tList.addAll(ShareData.getToTransactionList());

                    if (!pendingTransaction.isEmpty()) {
                        tList.forEach(tx -> {
                            Iterator<Protocol.Transaction> it = pendingTransaction.iterator();
                            while (it.hasNext()) {
                                Protocol.Transaction ptx = it.next();
                                if (ptx.getRawData().getContractList().equals(tx.getRawData().getContractList())) {
                                    it.remove();
                                }
                            }
                        });
                        for (Protocol.Transaction transaction : pendingTransaction) {
                            tList.add(transaction);
                        }
                    }
                    tList.sort((o1, o2) -> Long.compare(o2.getRawData().getExpiration(), o1.getRawData().getExpiration()));
                    transactionSimpleObjectProperty.set(tList);
                });
            } catch (Throwable t) {
                logger.error("Unhandled exception", t);
            }
        }, 1, 5, TimeUnit.SECONDS);

        toTransactionExecutor.scheduleWithFixedDelay(() -> {
            if (isCold.get()) {
                return;
            }
            try {
                Client client = Client.getInstance();
                if (ShareData.getPassword() == null || ShareData.getPassword().isEmpty()) {
                    return;
                }
                if (client.login(ShareData.getPassword())) {
                    if (client.getAddress() == null || client.getAddress().isEmpty()) {
                        return;
                    }

                    byte[] addressBytes = WalletClient.decodeFromBase58Check(client.getAddress());
                    if (addressBytes == null) {
                        return;
                    }

                    int getTransactionsToThisCount = (int) WalletClient.getTransactionsToThisCount(addressBytes).getNum();
                    GrpcAPI.TransactionList transactionToList = WalletClient.getTransactionsToThis(addressBytes, (getTransactionsToThisCount - 100 <= 0) ? 0 : (getTransactionsToThisCount - 100), 100).get();

                    if (transactionToList != null) {
                        ShareData.setToTransactionList(transactionToList.getTransactionList());
                    }
                }
                Platform.runLater(() -> {
                    List<Protocol.Transaction> tList = new ArrayList<>();
                    if (ShareData.getFromTransactionList() != null)
                        tList.addAll(ShareData.getFromTransactionList());
                    if (ShareData.getToTransactionList() != null)
                        tList.addAll(ShareData.getToTransactionList());

                    if (!pendingTransaction.isEmpty()) {
                        tList.forEach(tx -> {
                            Iterator<Protocol.Transaction> it = pendingTransaction.iterator();
                            while (it.hasNext()) {
                                Protocol.Transaction ptx = it.next();
                                if (ptx.getRawData().getContractList().equals(tx.getRawData().getContractList())) {
                                    it.remove();
                                }
                            }
                        });
                        for (Protocol.Transaction transaction : pendingTransaction) {
                            tList.add(transaction);
                        }
                    }
                    tList.sort((o1, o2) -> Long.compare(o2.getRawData().getExpiration(), o1.getRawData().getExpiration()));
                    transactionSimpleObjectProperty.set(tList);
                });
            } catch (Throwable t) {
                logger.error("Unhandled exception", t);
            }
        }, 1, 5, TimeUnit.SECONDS);

        tokenExecutor.scheduleWithFixedDelay(() -> {
            if (isCold.get()) {
                return;
            }
            try {
                Client client = Client.getInstance();
                if (ShareData.getPassword() == null || ShareData.getPassword().isEmpty()) {
                    return;
                }
                if (client.login(ShareData.getPassword())) {
                    if (client.getAddress() == null || client.getAddress().isEmpty()) {
                        return;
                    }

                    GrpcAPI.AssetIssueList tokenList = client.getAssetIssueList().get();

                    if (tokenList != null) {
                        ShareData.setTokenList(tokenList.getAssetIssueList());
                    }
                }
                Platform.runLater(() -> {
                    tokenSimpleObjectProperty.set(ShareData.getTokenList());
                });
            } catch (Throwable t) {
                logger.error("Unhandled exception", t);
            }
        }, 1, 2, TimeUnit.SECONDS);

        witnessExecutor.scheduleWithFixedDelay(() -> {
            if (isCold.get()) {
                return;
            }
            try {
                Client client = Client.getInstance();
                if (ShareData.getPassword() == null || ShareData.getPassword().isEmpty()) {
                    return;
                }
                if (client.login(ShareData.getPassword())) {
                    if (client.getAddress() == null || client.getAddress().isEmpty()) {
                        return;
                    }

                    GrpcAPI.WitnessList witnessList = client.listWitnesses().get();
                    if (witnessList == null) {
                        return;
                    }

                    if (witnessList != null) {
                        ShareData.setWitnessList(witnessList.getWitnessesList());
                    }
                }
                Platform.runLater(() -> {
                    witnessSimpleObjectProperty.set(ShareData.getWitnessList());
                });
            } catch (Throwable t) {
                logger.error("Unhandled exception", t);
            }
        }, 1, 2, TimeUnit.SECONDS);

    }

    public static void reset() {
        isLogin = false;
        isCold.set(false);
        data.clear();
        addressSimpleObjectProperty.set("");
        balanceSimpleObjectProperty.set("0");
        balanceTmpSimpleObjectProperty.set("0");
        freezeSimpleObjectProperty.set("0");
        tronPowerSimpleObjectProperty.set("0");
        tronPowerTmpSimpleObjectProperty.set("0");
        bandWidthSimpleObjectProperty.set("0");
        accountSimpleObjectProperty.set(null);
        witnessSimpleObjectProperty.set(null);
        transactionSimpleObjectProperty.set(null);
        tokenSimpleObjectProperty.set(null);
        try {
            SQLiteUtil.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            SQLiteUtil.init(Config.WALLET_DB_FILE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
