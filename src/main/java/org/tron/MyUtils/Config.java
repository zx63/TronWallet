package org.tron.MyUtils;

public class Config {
    public static String WALLET_PATH = System.getProperty("user.home") + "/tronwallet/";
    public static String WALLET_DB_FILE_BAK = WALLET_PATH + "TronWalletBackup.dat";
    public static String WALLET_DB_FILE = WALLET_PATH + "TronWallet.dat";

    public static Boolean MYDEV = true;
    public static String FULL_NODE = MYDEV ? "116.85.36.174:50051" : "34.233.96.87:50051";
//    public static String FULL_NODE = MYDEV ? "127.0.0.1:50051" : "34.233.96.87:50051";
    public static String SOLIDITY_NODE = MYDEV ? null : "39.105.66.80:50051";

    public static int PASS_MIN_LENGTH = 8;

    public static final long EXPIRE = 60 * 1000;

    public static final long DROP_UNIT = 1000000;
}
