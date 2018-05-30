package org.tron.MyUtils;

public class Config {
    public static String WALLET_PATH = System.getProperty("user.home") + "/tronwallet/";
    public static String WALLET_DB_FILE_BAK = System.getProperty("user.home") + "/tronwallet/" + "WalletBackup.db";
    public static String WALLET_DB_FILE = System.getProperty("user.home") + "/tronwallet/" + "Wallet2.db";

    public static Boolean MYDEV = true;
    public static String FULL_NODE = MYDEV ? "116.85.36.174:50051" : "34.233.96.87:50051";
    public static String SOLIDITY_NODE = MYDEV ? null : "39.105.66.80:50051";
    public static int PASS_MIN_LENGTH = 6;

    public static final long EXPIRE = 60 * 1000;

    public static final long DROP_UNIT = 1000000;
}
