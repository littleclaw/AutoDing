package com.furry.rimet.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.pengxh.autodingding.bean.HistoryRecordBean;

import com.furry.rimet.greendao.HistoryRecordBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig historyRecordBeanDaoConfig;

    private final HistoryRecordBeanDao historyRecordBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        historyRecordBeanDaoConfig = daoConfigMap.get(HistoryRecordBeanDao.class).clone();
        historyRecordBeanDaoConfig.initIdentityScope(type);

        historyRecordBeanDao = new HistoryRecordBeanDao(historyRecordBeanDaoConfig, this);

        registerDao(HistoryRecordBean.class, historyRecordBeanDao);
    }
    
    public void clear() {
        historyRecordBeanDaoConfig.clearIdentityScope();
    }

    public HistoryRecordBeanDao getHistoryRecordBeanDao() {
        return historyRecordBeanDao;
    }

}