package com.example.glmaclient.persistentcloudanchor;

import com.google.ar.core.Anchor;
import com.google.ar.core.HostCloudAnchorFuture;
import com.google.ar.core.ResolveCloudAnchorFuture;
import com.google.ar.core.Session;
import com.google.common.base.Preconditions;

public class CloudAnchorManager {
    interface CloudAnchorHostListener {

        /** This method is invoked when the results of a Cloud Anchor operation are available. */
        void onComplete(String cloudAnchorId, Anchor.CloudAnchorState cloudAnchorState);
    }

    /** Listener for the results of a resolve operation. */
    interface CloudAnchorResolveListener {

        /** This method is invoked when the results of a Cloud Anchor operation are available. */
        void onComplete(String cloudAnchorId, Anchor anchor, Anchor.CloudAnchorState cloudAnchorState);
    }

    private Session session;

    CloudAnchorManager(Session session) {
        this.session = Preconditions.checkNotNull(session);
    }

    /** Hosts an anchor. The {@code listener} will be invoked when the results are available. */
    synchronized void hostCloudAnchor(Anchor anchor, CloudAnchorHostListener listener) {
        Preconditions.checkNotNull(listener, "The listener cannot be null.");
        // Creating a Cloud Anchor with lifetime  = 1 day. This is configurable up to 365 days.
        HostCloudAnchorFuture unused =
                session.hostCloudAnchorAsync(anchor, /* ttlDays= */ 1, listener::onComplete);
    }

    /** Resolves an anchor. The {@code listener} will be invoked when the results are available. */
    synchronized void resolveCloudAnchor(final String anchorId, CloudAnchorResolveListener listener) {
        Preconditions.checkNotNull(listener, "The listener cannot be null.");
        ResolveCloudAnchorFuture unused =
                session.resolveCloudAnchorAsync(
                        anchorId,
                        (anchor, cloudAnchorState) -> {
                            listener.onComplete(anchorId, anchor, cloudAnchorState);
                        });
    }
}
