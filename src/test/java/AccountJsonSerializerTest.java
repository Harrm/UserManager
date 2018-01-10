import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.Account;
import org.AccountJsonSerializer;
import org.eclipse.jetty.io.WriterOutputStream;
import org.testng.annotations.Test;
import java.io.*;
import java.time.LocalDate;



public class AccountJsonSerializerTest {

    static Account getDefaultAccount() {
        Account account = new Account("user1234", "qwerty");
        account.setBirthday(LocalDate.of(2000, 1, 1));
        account.setSex(Account.Sex.Male);
        account.setName("Anonymous");
        return account;
    }

    static ObjectNode getDefaultAccountNode() {
        ObjectNode root = new ObjectNode(JsonNodeFactory.instance);
        root.put("name", "Anonymous");
        root.put("login", "user1234");
        root.put("password", "qwerty");
        root.put("sex", "Male");
        root.put("birthday", "2000-01-01");
        return root;
    }

    /**
     * Tests account creation from JSON object
     * @throws IOException
     */
    @Test
    public void simpleRead() throws IOException {
        InputStream is = new ByteArrayInputStream((getDefaultAccountNode().toString()).getBytes());
        Account a = AccountJsonSerializer.createFromJSON(is);
        assert a.getName().equals("Anonymous");
        assert a.getPassword().equals("qwerty");
        assert a.getBirthday().equals(LocalDate.of(2000, 1, 1));
        assert a.getSex().equals(Account.Sex.Male);
        assert a.getLogin().equals("user1234");
    }

    /**
     * Tests account updating from JSON object
     * @throws IOException
     */
    @Test
    public void simpleUpdate() throws IOException {
        ObjectNode node = getDefaultAccountNode();
        node.removeAll();
        node.put("name", "Deanonymous");
        InputStream is = new ByteArrayInputStream((node.toString()).getBytes());
        Account a = getDefaultAccount();
        AccountJsonSerializer.updateFromJSON(a, is);
        assert a.getName().equals("Deanonymous");
        assert a.getPassword().equals("qwerty");
        assert a.getBirthday().equals(LocalDate.of(2000, 1, 1));
        assert a.getSex().equals(Account.Sex.Male);
        assert a.getLogin().equals("user1234");
    }

    /**
     * Shows that Invalid Description is thrown when JSON object is incomplete or empty
     * @throws IOException
     */
    @Test(expectedExceptions = org.AccountJsonSerializer.InvalidDescription.class)
    public void simpleWrongRead() throws IOException {
        InputStream is = new ByteArrayInputStream("{}".getBytes());
        AccountJsonSerializer.createFromJSON(is);
    }

    /**
     * Shows that InvalidDescription is thrown when login has an illegal format(should contain only letters and '_')
     * @throws IOException
     */
    @Test(expectedExceptions = org.AccountJsonSerializer.InvalidDescription.class)
    public void wrongLoginRead() throws IOException {
        ObjectNode account = getDefaultAccountNode();
        account.put("login", "lo g in");
        InputStream is = new ByteArrayInputStream((account.toString()).getBytes());
        AccountJsonSerializer.createFromJSON(is);
    }

    /**
     * Shows that InvalidDescription is thrown when birthday field has an illegal format(should be YYYY-MM-DD)
     * @throws IOException
     */
    @Test(expectedExceptions = org.AccountJsonSerializer.InvalidDescription.class)
    public void wrongBirthdayRead() throws IOException {
        ObjectNode account = getDefaultAccountNode();
        account.put("birthday", "1.1.2000");
        InputStream is = new ByteArrayInputStream((account.toString()).getBytes());
        AccountJsonSerializer.createFromJSON(is);
    }

    /**
     * Shows that InvalidDescription is thrown when sex field has an illegal value(should be Male or Female)
     * @throws IOException
     */
    @Test(expectedExceptions = org.AccountJsonSerializer.InvalidDescription.class)
    public void wrongSexRead() throws IOException {
        ObjectNode account = getDefaultAccountNode();
        account.put("sex", "Undefined");
        InputStream is = new ByteArrayInputStream((account.toString()).getBytes());
        AccountJsonSerializer.createFromJSON(is);
    }

    /**
     * Shows that IllegalArgument is thrown when the input stream is empty
     * @throws IOException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void emptyRead() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[] {});
        AccountJsonSerializer.createFromJSON(is);
    }

    /**
     * Test serialization of Account to JSON
     * @throws IOException
     */
    @Test
    public void write() throws IOException {
        StringWriter writer = new StringWriter();
        OutputStream os = new WriterOutputStream(writer);
        AccountJsonSerializer.toJSON(getDefaultAccount(), os);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(writer.toString());
        AccountJsonSerializer.validateJsonAsFullAccount(root);
    }
}
