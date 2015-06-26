/**
 *
 */
package ch.wellernet.restlet.android;

import static java.lang.String.format;
import static roboguice.RoboGuice.getInjector;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.ServerResource;

import android.content.Context;
import android.util.Log;

/**
 * Restlet that can be used in a RoboGuice environment. It takes instances of target server resource from RoboGuice injector. Ensure that the desired
 * implementation class is bound to {@link #targetClass}.
 *
 * @author Lucien Weller <lucien@wellernet.ch>
 */
public class RoboGuiceRestlet extends Restlet {

    private static final String TAG = RoboGuiceRestlet.class.getName();

    private final Context context;
    private final Class<? extends ServerResource> targetClass;

    public RoboGuiceRestlet(Context context, Class<? extends ServerResource> targetClass) {
        this.context = context;
        this.targetClass = targetClass;
    }

    /**
     * @see org.restlet.Restlet#handle(org.restlet.Request, org.restlet.Response)
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (isStarted()) {
            ServerResource targetResource = createServerResource();

            if (targetResource == null) {
                Log.e(TAG, format("Cannot find a suitable target resource instance of type %s", targetClass.getName()));
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                targetResource.init(getContext(), request, response);
                if (response == null || response.getStatus().isSuccess()) {
                    targetResource.handle();
                }
                targetResource.release();
            }
        }
    }

    private ServerResource createServerResource() {
        return getInjector(context).getInstance(targetClass);
    }
}
