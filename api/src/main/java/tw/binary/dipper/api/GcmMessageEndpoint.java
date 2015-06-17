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
        name = "gcmMessageApi",
        version = "v1",
        resource = "gcmMessage",
        namespace = @ApiNamespace(
                ownerDomain = "api.dipper.binary.tw",
                ownerName = "api.dipper.binary.tw",
                packagePath = ""
        )
)
public class GcmMessageEndpoint {

    private static final Logger logger = Logger.getLogger(GcmMessageEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(GcmMessage.class);
    }

    /**
     * Returns the {@link GcmMessage} with the corresponding ID.
     *
     * @param Id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code GcmMessage} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "gcmMessage/{Id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public GcmMessage get(@Named("Id") String Id) throws NotFoundException {
        logger.info("Getting GcmMessage with ID: " + Id);
        GcmMessage gcmMessage = ofy().load().type(GcmMessage.class).id(Id).now();
        if (gcmMessage == null) {
            throw new NotFoundException("Could not find GcmMessage with ID: " + Id);
        }
        return gcmMessage;
    }

    /**
     * Inserts a new {@code GcmMessage}.
     */
    @ApiMethod(
            name = "insert",
            path = "gcmMessage",
            httpMethod = ApiMethod.HttpMethod.POST)
    public GcmMessage insert(GcmMessage gcmMessage) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that gcmMessage.Id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.
        ofy().save().entity(gcmMessage).now();
        logger.info("Created GcmMessage.");

        return ofy().load().entity(gcmMessage).now();
    }

    /**
     * Updates an existing {@code GcmMessage}.
     *
     * @param Id         the ID of the entity to be updated
     * @param gcmMessage the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code GcmMessage}
     */
    @ApiMethod(
            name = "update",
            path = "gcmMessage/{Id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public GcmMessage update(@Named("Id") String Id, GcmMessage gcmMessage) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        checkExists(Id);
        ofy().save().entity(gcmMessage).now();
        logger.info("Updated GcmMessage: " + gcmMessage);
        return ofy().load().entity(gcmMessage).now();
    }

    /**
     * Deletes the specified {@code GcmMessage}.
     *
     * @param Id the ID of the entity to delete
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code GcmMessage}
     */
    @ApiMethod(
            name = "remove",
            path = "gcmMessage/{Id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("Id") String Id) throws NotFoundException {
        checkExists(Id);
        ofy().delete().type(GcmMessage.class).id(Id).now();
        logger.info("Deleted GcmMessage with ID: " + Id);
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
            path = "gcmMessage",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<GcmMessage> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<GcmMessage> query = ofy().load().type(GcmMessage.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<GcmMessage> queryIterator = query.iterator();
        List<GcmMessage> gcmMessageList = new ArrayList<GcmMessage>(limit);
        while (queryIterator.hasNext()) {
            gcmMessageList.add(queryIterator.next());
        }
        return CollectionResponse.<GcmMessage>builder().setItems(gcmMessageList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String Id) throws NotFoundException {
        try {
            ofy().load().type(GcmMessage.class).id(Id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find GcmMessage with ID: " + Id);
        }
    }
}