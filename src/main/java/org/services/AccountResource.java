package org.services;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.spi.resource.Singleton;
import org.Account;
import org.AccountJsonSerializer;
import org.AccountStorage;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


/**
 * A resource that provides a user with a REST interface for the account storage
 */
@Path("/accounts")
@Singleton
public class AccountResource {

    /**
     * Initializes the storage at the data directory of the server base
     */
    public AccountResource() throws IOException {
        accounts = new AccountStorage("./data/");
    }

    /**
     * POST method, that creates a record for an account which JSON description lies in the stream
     * @return * Response 201 'CREATED' in case of success
     *          * Response 415 'UNSUPPORTED MEDIA TYPE' in case of invalid JSON description in the stream
     *          * Response 500 'INTERNAL SERVER ERROR' in case of I/O error(for example if 'data' dir does not exist)
     */
    @POST
    @Consumes("application/json")
    public Response createAccount(InputStream is) {
        try {
            Account account = AccountJsonSerializer.createFromJSON(is);
            accounts.store(account);
            return Response.created(URI.create("/accounts/" + account.getLogin())).build();

        } catch (IOException e) {
            System.err.println(e);
            return Response.serverError().build();

        } catch (AccountJsonSerializer.InvalidDescription e) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
    }

    /**
     * GET method, that returns a JSON representation of an account with the login specified in the path
     * @return a steam containing a JSON object
     * @throws NotFoundException, status code 404, 'NOT FOUND' if an account with given login does not exist in current 'data' dir
     * @throws WebApplicationException with status 500, 'INTERNAL SERVER ERROR' if I/O error occures
     */
    @GET
    @Path("{login}")
    @Produces("application/json")
    public StreamingOutput getAccount(@PathParam("login") String login) {
        try {
            Account account = accounts.get(login);
            return new StreamingOutput() {
                public void write(OutputStream outputStream) throws IOException,
                        WebApplicationException {
                    AccountJsonSerializer.toJSON(account, outputStream);
                }
            };

        } catch (AccountStorage.UserNotFoundException e) {
            throw new NotFoundException("User "+login+" does not exist");

        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);

        }
    }

    /**
     * PUT method that updates an account data with values provided in the JSON object which lies in the stream
     * @param login of the account to be updated
     * @param is containing partial JSON description of an account
     * @throws NotFoundException, status code 404, 'NOT FOUND' if an account with given login does not exist in current 'data' dir
     * @throws WebApplicationException with status 500, 'INTERNAL SERVER ERROR' if I/O error occures
     *                                  with status 415, 'UNSUPPORTED MEDIA TYPE', if the account description is incorrect
     */
    @PUT
    @Path("{login}")
    @Consumes("application/json")
    public void updateAccount(@PathParam("login") String login,
                               InputStream is) {
        try {
            Account current = accounts.get(login);
            String oldLogin = current.getLogin();
            AccountJsonSerializer.updateFromJSON(current, is);
            if(!oldLogin.equals(current.getLogin())) {
                accounts.remove(oldLogin);
            }
            accounts.store(current);

        } catch (AccountStorage.UserNotFoundException e) {
            throw new NotFoundException("User "+login+" does not exist");
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (AccountJsonSerializer.InvalidDescription e) {
            throw new WebApplicationException(e, Response.Status.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * DELETE method that removes a record of the account with the given login from the storage
     * @throws NotFoundException if the account is not in the storage
     * @throws WebApplicationException with status 500, 'INTERNAL SERVER ERROR' if I/O error occurs
     */
    @DELETE
    @Path("{login}")
    public void deleteAccount(@PathParam("login") String login) {
        try {
            accounts.remove(login);

        } catch (AccountStorage.UserNotFoundException e) {
            throw new NotFoundException("User "+login+" does not exist");
        } catch (IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private AccountStorage accounts;
}
