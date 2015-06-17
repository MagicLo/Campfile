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
        name = "myResourceApi",
        version = "v1",
        resource = "myResource",
        namespace = @ApiNamespace(
                ownerDomain = "api.dipper.binary.tw",
                ownerName = "api.dipper.binary.tw",
                packagePath = ""
        )
)
public class MyResourceEndpoint {

    private static final Logger logger = Logger.getLogger(MyResourceEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(MyResource.class);
    }

    /**
     * Returns the {@link MyResource} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code MyResource} with the provided ID.
     */
    @ApiMethod(
            name = "get",
            path = "myResource/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public MyResource get(@Named("id") String id) throws NotFoundException {
        logger.info("Getting MyResource with ID: " + id);
        MyResource myResource = ofy().load().type(MyResource.class).id(id).now();
        if (myResource == null) {
            throw new NotFoundException("Could not find MyResource with ID: " + id);
        }
        return myResource;
    }

    /**
     * Inserts a new {@code MyResource}.
     */
    @ApiMethod(
            name = "insert",
            path = "myResource",
            httpMethod = ApiMethod.HttpMethod.POST)
    public MyResource insert(MyResource myResource) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that myResource.Id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        //
        // If your client provides the ID then you should probably use PUT instead.

        ofy().save().entity(myResource).now();
        logger.info("Created MyResource with ID: " + myResource.getId());

        return ofy().load().entity(myResource).now();
    }

    /**
     * Updates an existing {@code MyResource}.
     *
     * @param id         the ID of the entity to be updated
     * @param myResource the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code MyResource}
     */
    @ApiMethod(
            name = "update",
            path = "myResource/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public MyResource update(@Named("id") String id, MyResource myResource) throws NotFoundException {
        // TODO: You should validate your ID parameter against your resource's ID here.
        if (!id.equals(myResource.getId()))
            return null;
        //checkExists(Id);
        ofy().save().entity(myResource).now();  //不存在就新增
        logger.info("Updated MyResource: " + myResource);
        return ofy().load().entity(myResource).now();
    }

    /**
     * Deletes the specified {@code MyResource}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code Id} does not correspond to an existing
     *                           {@code MyResource}
     */
    @ApiMethod(
            name = "remove",
            path = "myResource/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void remove(@Named("id") String id) throws NotFoundException {
        checkExists(id);
        ofy().delete().type(MyResource.class).id(id).now();
        logger.info("Deleted MyResource with ID: " + id);
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
            path = "myResource",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<MyResource> list(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<MyResource> query = ofy().load().type(MyResource.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MyResource> queryIterator = query.iterator();
        List<MyResource> myResourceList = new ArrayList<MyResource>(limit);
        while (queryIterator.hasNext()) {
            myResourceList.add(queryIterator.next());
        }
        return CollectionResponse.<MyResource>builder().setItems(myResourceList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }


    @ApiMethod(name = "listByUser")
    public CollectionResponse<MyResource> listByUser(@Nullable @Named("cursor") String cursor, @Named("userId") String UserId, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<MyResource> query = ofy().load().type(MyResource.class).filter("userId =", UserId).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<MyResource> queryIterator = query.iterator();
        List<MyResource> myResourceList = new ArrayList<MyResource>(limit);
        while (queryIterator.hasNext()) {
            myResourceList.add(queryIterator.next());
        }
        return CollectionResponse.<MyResource>builder().setItems(myResourceList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExists(String id) throws NotFoundException {
        try {
            ofy().load().type(MyResource.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find MyResource with ID: " + id);
        }
    }

    //自建函數，要保留
    private MyResource findRecord(String id) {
        return ofy().load().type(MyResource.class).id(id).now();
        //or return ofy().load().type(Quote.class).filter("id",id).first.now();
    }
}