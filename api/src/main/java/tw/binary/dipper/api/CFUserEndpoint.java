package tw.binary.dipper.api;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p/>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "cFUserApi",
        version = "v1",
        resource = "cFUser",
        namespace = @ApiNamespace(
                ownerDomain = "api.dipper.binary.tw",
                ownerName = "api.dipper.binary.tw",
                packagePath = ""
        )
)
public class CFUserEndpoint {

    private static final Logger logger = Logger.getLogger(CFUserEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(CFUser.class);
    }

    /**
     * Returns the {@link CFUser} with the corresponding ID.
     *
     * @param Id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code CFUser} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "cFUser/{Id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CFUser get(@Named("Id") String Id) throws NotFoundException {
        logger.info("Getting CFUser with ID: " + Id);
        CFUser cFUser = ofy().load().type(CFUser.class).id(Id).now();
        if (cFUser == null) {
            throw new NotFoundException("Could not find CFUser with ID: " + Id);
        }
        return cFUser;
    }


    /**
     * Inserts a new {@code CFUser}.
     */
    @ApiMethod(
            name = "insert",
            path = "cFUser",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public CFUser insert(CFUser cFUser) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that cFUser.Id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        // 為了更新登入時間，因此不檢查資料是否重複
        ofy().save().entity(cFUser).now();
        logger.info("Created CFUser with ID: " + cFUser.getId());
        return ofy().load().entity(cFUser).now();
    }

    /**
     * Updates an existing {@code CFUser}.
     *
     * @param Id     the ID of the entity to be updated
     * @param cFUser the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code CFUser}
     */
    @ApiMethod(
            name = "update",
            path = "cFUser/{Id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public CFUser update(@Named("Id") String Id, CFUser cFUser) {
        // TODO: You should validate your ID parameter against your resource's ID here.
        if (!Id.equals(cFUser.getId()))
            return null;
        // 為了更新登入時間，因此不檢查資料是否重複
        //checkExists(Id);
        ofy().save().entity(cFUser).now();
        logger.info("Updated CFUser: " + cFUser.getId());
        return ofy().load().entity(cFUser).now();
    }


    /**
     * Deletes the specified {@code CFUser}.
     *
     * @param Id the ID of the entity to delete
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code CFUser}
     */
    @ApiMethod(
            name = "remove",
            path = "cFUser/{Id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("Id") String Id) throws NotFoundException {
        checkExists(Id);
        ofy().delete().type(CFUser.class).id(Id).now();
        logger.info("Deleted CFUser with ID: " + Id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "list",
            path = "cFUser",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<CFUser> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<CFUser> query = ofy().load().type(CFUser.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<CFUser> queryIterator = query.iterator();
        List<CFUser> cFUserList = new ArrayList<CFUser>(limit);
        while (queryIterator.hasNext()) {
            cFUserList.add(queryIterator.next());
        }
        return CollectionResponse.<CFUser>builder().setItems(cFUserList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    @ApiMethod(name = "listByUser")
    public CollectionResponse<CFUser> listByUser(@Nullable @Named("cursor") String cursor, @Named("UserId") String UserId, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<CFUser> query = ofy().load().type(CFUser.class).filter("UserId =", UserId).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<CFUser> queryIterator = query.iterator();
        List<CFUser> myUserList = new ArrayList<CFUser>(limit);
        while (queryIterator.hasNext()) {
            myUserList.add(queryIterator.next());
        }
        return CollectionResponse.<CFUser>builder().setItems(myUserList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String Id) throws NotFoundException {
        try {
            ofy().load().type(CFUser.class).id(Id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find CFUser with ID: " + Id);
        }
    }
}