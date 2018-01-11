package org;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.*;


/**
 * This class manages the account I/O on a disk.
 * Accounts are serialized to JSON during writing on disk and automatically deserialized during reading from it.
 * Each account is stored in a separate file named after the account login, with '.js' extention
 */
public class AccountStorage {

    /**
     * @param dataPath path to a directory where accounts will be stored
     * @throws IOException if dataPath doesn't exist and error occured during its creation
     */
    public AccountStorage(String dataPath) throws IOException {
        this.dataPath = dataPath;
        Path p = FileSystems.getDefault().getPath(dataPath);
        if(!Files.exists(p)) {
            FileSystems.getDefault().provider().createDirectory(p);
        }
    }

    /**
     * Reads an account from JSON file at the specified in dataPath location
     *
     * @param login of the needed account
     * @return the read account
     * @throws UserNotFoundException if the file doesn't exist
     * @throws IOException if something wrong happens during reading the file
     * @throws CorruptedFileException if the file contains incorrect JSON description of an account
     */
    public Account get(String login) throws IOException {
        File accountFile = new File(dataPath + login + ".json");
        if(!accountFile.exists()) {
            throw new UserNotFoundException(login);
        }
        InputStream is = new FileInputStream(accountFile);
        try {
            return AccountJsonSerializer.createFromJSON(is);
        } catch (IllegalArgumentException| AccountJsonSerializer.InvalidDescription e) {
            throw new CorruptedFileException(accountFile.getName());
        }
    }

    /**
     * Writes the given accout on disk to the specified in dataPath location
     * @param account to be stored
     * @throws IOException if something wrong happens during writing the file
     */
    public void store(Account account) throws IOException {
        File accountFile = new File(dataPath + String.valueOf(account.getLogin()) + ".json");
        OutputStream os = new FileOutputStream(accountFile);
        AccountJsonSerializer.toJSON(account, os);
    }

    /**
     * Removes the record of an account with the given login from the storage
     * @param login of the account to be removed
     * @throws IOException if something wrong happens during deleting the file
     * @throws UserNotFoundException if the account is not in the storage
     */
    public void remove(String login) throws IOException {
        FileSystem files = FileSystems.getDefault();
        try {
            files.provider().delete(files.getPath(dataPath + login + ".json"));
        } catch (NoSuchFileException e) {
            throw new UserNotFoundException(login);
        }
    }

    public void setDataPath(String path) {
        dataPath = path;
    }

    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String login) {
            super("User "+login+" does not exist");
        }
    }

    public class CorruptedFileException extends RuntimeException {
        public CorruptedFileException(String file) { super("JSON object in "+file+" is illegal."); }
    }

    private String dataPath;
    private ObjectMapper mapper = new ObjectMapper();

}
