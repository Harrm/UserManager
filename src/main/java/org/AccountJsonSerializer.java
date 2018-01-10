package org;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;


/**
 * This class is intended to wrap Jackson tools of processing JSON and
 * simplify the account serialization/deserialization to/from JSON
 * Also it checks the validity of account descriptions, for some fields are restricted to appropriate formats
 * login - should contain only letters, digits and '_'
 * birthday - should be a date in the format of YYYY-MM-DD
 * sex - should be either Male or Female
 */
public class AccountJsonSerializer {

    /**
     * Serializes given account to JSON, writing generated JSON object to the stream
     * @throws IOException if something is wrong with the stream
     */
    public static void toJSON(Account account, OutputStream os) throws IOException {
        JsonFactory f = new JsonFactory();

        JsonGenerator g = f.createGenerator(os);

        g.writeStartObject();
        g.writeStringField("login", account.getLogin());
        g.writeStringField("name", account.getName());
        g.writeStringField("sex", account.getSex().toString());
        g.writeStringField("birthday", account.getBirthday().toString());
        g.writeStringField("password", account.getPassword());
        g.writeEndObject();
        g.close();

    }

    /**
     * Creates an account from JSON description, contained in the stream, and initializes
     * the account fields from it.
     * @param is contains a JSON object that represents an account.
     * @throws IOException if something is wrong with the stream
     * @throws IllegalArgumentException if the stream is empty
     * @throws InvalidDescription if a description in the stream has an invalid format
     */
    public static Account createFromJSON(InputStream is) throws IOException {
        if(is.available() == 0) {
            throw new IllegalArgumentException("Input stream is empty!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(is);
        Account account;

        validateJsonAsFullAccount(root);

        String login, password;
        login = root.get("login").asText();
        password = root.get("password").asText();
        account = new Account(login, password);
        updateFromJSON(account, root);
        return account;
    }

    /**
     * Sets some fields of the given account to values from a JSON object in the stream
     * @param is contains a JSON object that represents an account. Some account fields may be missing
     * @throws IOException if something is wrong with the stream
     * @throws IllegalArgumentException if the stream is empty
     * @throws InvalidDescription if a description in the stream has an invalid format
     */
    public static void updateFromJSON(Account account, InputStream is) throws IOException {
        if(is.available() == 0) {
            throw new IllegalArgumentException("Input stream is empty!");
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(is);

        updateFromJSON(account, root);
    }

    /**
     * Checks the correctness of a JSON object, that should represent a complete account description
     * @throws InvalidDescription if the object is not a valid account description
     */
    public static void validateJsonAsFullAccount(JsonNode root) {
        if (root.has("login") &&
                root.has("password") &&
                root.has("birthday") &&
                root.has("name") &&
                root.has("sex")) {
            validateJsonAsPartialAccount(root);
        } else {
            throw new InvalidDescription("Some fields are missing!");
        }
    }

    /**
     * Checks the correctness of a JSON object, that should represent a partial account description
     * @throws InvalidDescription if the object is not a valid account description
     */
    public static void validateJsonAsPartialAccount(JsonNode root) {
        if(root.has("login")) {
            if(!"a".matches("[a-zA-Z0-9_]+")) {
                throw new InvalidDescription("Invalid login: should consist only of letters and '_'");
            }
        }
        if(root.has("birthday")) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
                LocalDate date = LocalDate.parse(root.get("birthday").asText(), formatter);

            } catch (DateTimeParseException e) {
                throw new InvalidDescription("Invalid birthday: required format is YYYY-MM-DD");
            }
        }

        if(root.has("sex")) {
            try {
                Account.Sex.valueOf(root.get("sex").asText());

            } catch (IllegalArgumentException e) {
                throw new InvalidDescription("Invalid sex: 'Male' and 'Female' are valid values");
            }
        }
    }

    /**
     * Sets some fields of the given account to values from a JSON object in the given JSON object
     * @param root is a JSON object that represents an account. Some account fields may be missing
     * @throws IOException if something is wrong with the stream
     * @throws IllegalArgumentException if the stream is empty
     * @throws InvalidDescription if a description in the stream has an invalid format
     */
    private static void updateFromJSON(Account account, JsonNode root) throws IOException {
        validateJsonAsPartialAccount(root);
        if(root.has("login")) {
            account.setLogin(root.get("login").asText());
        }
        if(root.has("name")) {
            account.setName(root.get("name").asText());
        }
        if(root.has("birthday")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            LocalDate date = LocalDate.parse(root.get("birthday").asText(), formatter);
            account.setBirthday(date);
        }
        if(root.has("sex")) {
            account.setSex(Account.Sex.valueOf(root.get("sex").asText()));
        }
        if(root.has("password")) {
            account.setPassword(root.get("password").asText());
        }
    }

    public static class InvalidDescription extends RuntimeException {
        InvalidDescription(String what) {
            super("Invalid account description: "+what);
        }
    }
}
