package org.tron.walletcli;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.tron.MyUtils.ShareData;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.walletserver.WalletClient;

import java.util.HashMap;
import java.util.Optional;

public class Client {

  private static Client client = new Client();
  private static final Logger logger = LoggerFactory.getLogger("Client");
  private WalletClient wallet;

  public static Client getInstance(){
    return client;
  }
  private Client(){}

  public boolean registerWallet(String password) {
    if (!WalletClient.passwordValid(password)) {
      return false;
    }
    wallet = new WalletClient(true);
    // create account at network
//    Boolean ret = wallet.createAccount(Protocol.AccountType.Normal, userName.getBytes());getBytes
    wallet.store(password);
    return true;
  }

  public boolean importWallet(String password, String priKey) {
    if (!WalletClient.passwordValid(password)) {
      return false;
    }
    if (!WalletClient.priKeyValid(priKey)) {
      return false;
    }
    wallet = new WalletClient(priKey);
    if (wallet.getEcKey() == null) {
      return false;
    }
    wallet.store(password);
    return true;
  }

  public boolean changePassword(String oldPassword, String newPassword) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: ChangePassword failed, Please login first !!");
      return false;
    }
    if (!WalletClient.passwordValid(oldPassword)) {
      logger.warn("Warning: ChangePassword failed, OldPassword is invalid !!");
      return false;
    }
    if (!WalletClient.passwordValid(newPassword)) {
      logger.warn("Warning: ChangePassword failed, NewPassword is invalid !!");
      return false;
    }
    if (!WalletClient.checkPassWord(oldPassword)) {
      logger.warn("Warning: ChangePassword failed, Wrong password !!");
      return false;
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(oldPassword);
      if (wallet == null) {
        logger.warn("Warning: ChangePassword failed, No wallet !!");
        return false;
      }
    }
    byte[] priKeyAsc = wallet.getEcKey().getPrivKeyBytes();
    String priKey = Hex.toHexString(priKeyAsc, 0, priKeyAsc.length);
    return importWallet(newPassword, priKey);
  }

  public boolean checkPassword(String password) {
    return wallet != null ? wallet.checkPassWord(password) : false;
  }

  public boolean login(String password) {
    if(ShareData.isLogin) {
      return true;
    } else {
      wallet = null;
    }
    if(StringUtils.isEmpty(password)) {
      return false;
    }
    if (!WalletClient.passwordValid(password)) {
      return false;
    }
    if (wallet == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: Login failed, Please registerWallet or importWallet first !!");
        return false;
      }
    }
    ShareData.isLogin =  wallet.login(password);
    return ShareData.isLogin;
  }

  public void logout() {
    if (wallet != null) {
      wallet.logout();
    }
    //Neddn't logout
  }

  //password is current, will be enc by password2.
  public String backupWallet(String password) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: BackupWallet failed, Please login first !!");
      return null;
    }
    if (!WalletClient.passwordValid(password)) {
      logger.warn("Warning: BackupWallet failed, password is Invalid !!");
      return null;
    }

    if (!WalletClient.checkPassWord(password)) {
      logger.warn("Warning: BackupWallet failed, Wrong password !!");
      return null;
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: BackupWallet failed, no wallet can be backup !!");
        return null;
      }
    }
    ECKey ecKey = wallet.getEcKey();
    byte[] privKeyPlain = ecKey.getPrivKeyBytes();
    //Enced by encPassword
    String priKey = ByteArray.toHexString(privKeyPlain);

    return priKey;
  }

  public String getAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: GetAddress failed,  Please login first !!");
      return null;
    }

    if (wallet.getEcKey() == null) {
      return WalletClient.getAddressByStorage();
    }

    String addressEncode58Check = WalletClient.encode58Check(wallet.getAddress());
    return addressEncode58Check;
  }

  public Account queryAccount() {
//    if (wallet == null || !wallet.isLoginState()) {
//      logger.warn("Warning: QueryAccount failed,  Please login first !!");
//      return null;
//    }

    try {
      return wallet.queryAccount(WalletClient.decodeFromBase58Check(ShareData.getAddress()));
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public GrpcAPI.Return sendCoin(String password, String toAddress, long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: SendCoin failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    byte[] to = WalletClient.decodeFromBase58Check(toAddress);
    if (to == null) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: SendCoin failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    return wallet.sendCoin(to, amount);
  }

  public GrpcAPI.Return transferAsset(String password, String toAddress, String assertName, long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    byte[] to = WalletClient.decodeFromBase58Check(toAddress);
    if (to == null) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: TransferAsset failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    return wallet.transferAsset(to, assertName.getBytes(), amount);
  }

  public GrpcAPI.Return participateAssetIssue(String password, String toAddress, String assertName,
      long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    byte[] to = WalletClient.decodeFromBase58Check(toAddress);
    if (to == null) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: TransferAsset failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    return wallet.participateAssetIssue(to, assertName.getBytes(), amount);
  }

  public GrpcAPI.Return assetIssue(String password, String name, long totalSupply, int trxNum, int icoNum,
      long startTime, long endTime, int voteScore, String description, String url,
      long freeNetLimit,long publicFreeNetLimit, HashMap<String, String> frozenSupply) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: assetIssue failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: assetIssue failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
      builder.setOwnerAddress(ByteString.copyFrom(wallet.getAddress()));
      builder.setName(ByteString.copyFrom(name.getBytes()));
      if (totalSupply <= 0) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      builder.setTotalSupply(totalSupply);
      if (trxNum <= 0) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      builder.setTrxNum(trxNum);
      if (icoNum <= 0) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      builder.setNum(icoNum);
      long now = System.currentTimeMillis();
      if (startTime <= now) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      if (endTime <= startTime) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      if (freeNetLimit < 0) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
      if (publicFreeNetLimit < 0) {
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }

      builder.setStartTime(startTime);
      builder.setEndTime(endTime);
      builder.setVoteScore(voteScore);
      builder.setDescription(ByteString.copyFrom(description.getBytes()));
      builder.setUrl(ByteString.copyFrom(url.getBytes()));
      builder.setFreeAssetNetLimit(freeNetLimit);
      builder.setPublicFreeAssetNetLimit(publicFreeNetLimit);

      for (String daysStr : frozenSupply.keySet()) {
        String amountStr = frozenSupply.get(daysStr);
        long amount = Long.parseLong(amountStr);
        long days = Long.parseLong(daysStr);
        Contract.AssetIssueContract.FrozenSupply.Builder frozenSupplyBuilder
            = Contract.AssetIssueContract.FrozenSupply.newBuilder();
        frozenSupplyBuilder.setFrozenAmount(amount);
        frozenSupplyBuilder.setFrozenDays(days);
        builder.addFrozenSupply(frozenSupplyBuilder.build());
      }

      return wallet.createAssetIssue(builder.build());
    } catch (Exception ex) {
      ex.printStackTrace();
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return createWitness(String password, String url) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createWitness failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: createWitness failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.createWitness(url.getBytes());
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return updateWitness(String password, String url) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateWitness failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: updateWitness failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.updateWitness(url.getBytes());
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public Block GetBlock(long blockNum) {
    return WalletClient.GetBlock(blockNum);
  }

  public GrpcAPI.Return voteWitness(String password, HashMap<String, String> witness) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: VoteWitness failed,  Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
    if (!WalletClient.passwordValid(password)) {
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: VoteWitness failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.voteWitness(witness);
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public Protocol.Transaction createUnsignedVoteWitnessTransaction(String owner, HashMap<String, String> witness) {
    try {
      System.out.println("createUnsignedVoteWitnessTransaction for " + owner);
      return WalletClient.createUnsignedVoteWitnessTransaction(WalletClient.decodeFromBase58Check(owner), witness);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

//  public Optional<AccountList> listAccounts() {
//    try {
//      return WalletClient.listAccounts();
//    } catch (Exception ex) {
//      ex.printStackTrace();
//      return Optional.empty();
//    }
//  }

  public Optional<WitnessList> listWitnesses() {
    try {
      return WalletClient.listWitnesses();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AssetIssueList> getAssetIssueList() {
    try {
      return WalletClient.getAssetIssueList();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<NodeList> listNodes() {
    try {
      return WalletClient.listNodes();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public GrpcAPI.NumberMessage getTotalTransaction() {
    return WalletClient.getTotalTransaction();
  }

  public GrpcAPI.NumberMessage getNextMaintenanceTime() {
    return WalletClient.getNextMaintenanceTime();
  }

  public GrpcAPI.Return updateAccount(String password, byte[] accountNameBytes) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warnging: updateAccount failed, Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: updateAccount failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.updateAccount(accountNameBytes);
    } catch (Exception ex) {
      ex.printStackTrace();
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return updateAsset(String password,
                                    byte[] description, byte[] url, long newLimit, long newPublicLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateAsset failed, Please login first !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: updateAsset failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.updateAsset(description, url, newLimit, newPublicLimit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return freezeBalance(String password, long frozen_balance, long frozen_duration) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: freezeBalance failed, Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: freezeBalance failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.freezeBalance(frozen_balance, frozen_duration);
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return unfreezeBalance(String password) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeBalance failed, Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: unfreezeBalance failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.unfreezeBalance();
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return unfreezeAsset(String password) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeAsset failed, Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: unfreezeAsset failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.unfreezeAsset();
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

  public GrpcAPI.Return withdrawBalance(String password) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warnging: withdrawBalance failed, Please login first !!");
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }

    if (wallet.getEcKey() == null || wallet.getEcKey().getPrivKey() == null) {
      wallet = WalletClient.GetWalletByStorage(password);
      if (wallet == null) {
        logger.warn("Warning: withdrawBalance failed, Load wallet failed !!");
        return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
      }
    }

    try {
      return wallet.withdrawBalance();
    } catch (Exception ex) {
      ex.printStackTrace();
      return GrpcAPI.Return.newBuilder().setResult(false).setCode(GrpcAPI.Return.response_code.OTHER_ERROR).build();
    }
  }

}
