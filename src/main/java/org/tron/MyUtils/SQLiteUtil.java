package org.tron.MyUtils;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.MyController.GuiUtils;
import org.tron.entity.EntityAccount;
import org.tron.entity.EntityBalance;
import org.tron.entity.EntityMeta;
import org.tron.entity.EntityPassword;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class SQLiteUtil {
    static ConnectionSource connectionSource;
    static final Logger logger = LoggerFactory.getLogger(SQLiteUtil.class);


    static Dao<EntityMeta, String> metaDao;
    static Dao<EntityBalance, String> balanceDao;
    static Dao<EntityAccount, String> accountDao;
    static Dao<EntityPassword, Integer> passwordDao;


    private SQLiteUtil() {
    }

    public static void init(String dbFile) throws SQLException {
        try {
            connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbFile);
        } catch (Exception e) {
            GuiUtils.crashAlert(new Throwable("Failed to open wallet file " + dbFile));
        }

        TableUtils.createTableIfNotExists(connectionSource, EntityMeta.class);
        TableUtils.createTableIfNotExists(connectionSource, EntityBalance.class);
        TableUtils.createTableIfNotExists(connectionSource, EntityAccount.class);
        TableUtils.createTableIfNotExists(connectionSource, EntityPassword.class);

        metaDao = DaoManager.createDao(connectionSource, EntityMeta.class);
        balanceDao = DaoManager.createDao(connectionSource, EntityBalance.class);
        accountDao = DaoManager.createDao(connectionSource, EntityAccount.class);
        passwordDao = DaoManager.createDao(connectionSource, EntityPassword.class);
    }

    public static void close() throws IOException {
        connectionSource.close();
    }

    public static EntityMeta getMetaEntity() {
        List<EntityMeta> list = null;
        try {
            list = metaDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void setMetaEntity(EntityMeta entityMeta) {
        try {
            metaDao.createOrUpdate(entityMeta);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static EntityPassword getEntityPassword() {
        List<EntityPassword> list = null;
        try {
            list = passwordDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void setPasswordEntity(EntityPassword entityPassword) {
        try {
            passwordDao.createOrUpdate(entityPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static EntityAccount getEntityAccount() {
        List<EntityAccount> list = null;
        try {
            list = accountDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static void setAccountEntity(EntityAccount entityAccount) {
        try {
            accountDao.createOrUpdate(entityAccount);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
