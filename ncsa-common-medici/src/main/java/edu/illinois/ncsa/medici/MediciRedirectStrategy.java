package edu.illinois.ncsa.medici;

import java.net.URI;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * Subclasses DefaultRedirectStrategy to provide redirect strategy for HttpPost
 * and HttpDelete overrides default redirect behavior.
 * 
 * @author Chris Navarro
 *
 */
public class MediciRedirectStrategy extends LaxRedirectStrategy {

    @Override
    protected boolean isRedirectable(final String method) {
        if (method.equals(HttpDelete.METHOD_NAME)) {
            return true;
        }

        return super.isRedirectable(method);
    }

    @Override
    public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
        final URI uri = getLocationURI(request, response, context);
        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            return copyEntity(new HttpPost(uri), request);
        } else if (method.equals(HttpDelete.METHOD_NAME)) {
            return new HttpDelete(uri);
        } else {
            return super.getRedirect(request, response, context);
        }
    }

    private HttpUriRequest copyEntity(final HttpEntityEnclosingRequestBase redirect, final HttpRequest original) {
        if (original instanceof HttpEntityEnclosingRequest) {
            redirect.setEntity(((HttpEntityEnclosingRequest) original).getEntity());
        }
        return redirect;
    }

}
