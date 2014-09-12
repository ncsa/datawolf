package edu.illinois.ncsa.jpa.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.persist.PersistService;

import edu.illinois.ncsa.domain.Account;
import edu.illinois.ncsa.domain.dao.AccountDao;
import edu.illinois.ncsa.jpa.JPADevModule;

public class AccountDAOTest {
    private static Injector injector;

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(new JPADevModule());

        // Initialize persistence service
        PersistService service = injector.getInstance(PersistService.class);
        service.start();

        // Persistence.setInjector(injector);
    }

    @Test
    public void testCreateAndStore() throws Exception {
        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account account = new Account();
        account.setUserid("test-id");
        accountDao.save(account);

        Account account1 = accountDao.findOne(account.getId());
        assert (account1.getId().equals(account.getId()));
    }

    // @Test
    public void testFindUserById() throws Exception {
        AccountDao accountDao = injector.getInstance(AccountDao.class);
        Account account = new Account();
        account.setUserid("test-id");

        accountDao.save(account);

        Account account1 = accountDao.findByUserid("test-id");
        assert (account1.getId().equals(account.getId()));

    }
}
