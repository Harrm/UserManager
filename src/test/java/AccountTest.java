import org.testng.annotations.*;
import org.Account;

import java.time.LocalDate;


public class AccountTest {

    /**
     * Tests account creation and initialization of all fields
     */
    @Test
    public void creation() {
        Account account = new Account("login", "password");
        account.setLogin("changedLogin");
        account.setBirthday(LocalDate.of(2000, 1, 1));
        account.setSex(Account.Sex.Female);
        account.setName("Abc");
        assert account.getLogin().equals("changedLogin");
        assert account.getSex() == Account.Sex.Female;
        assert account.getBirthday().equals(LocalDate.of(2000, 1, 1));
        assert account.getName().equals("Abc");
        assert account.getPassword().equals("password");
    }
}
