package com.arkamodh.authsdk.sdk.bridge

/**
 * Handle to a platform-specific listener subscription that can be canceled.
 */
public interface CancelableSubscription {
    public fun cancel()
}
