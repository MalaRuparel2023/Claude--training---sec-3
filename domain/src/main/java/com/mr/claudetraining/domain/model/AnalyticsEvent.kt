package com.mr.claudetraining.domain.model

/**
 * Type-safe catalog of the analytics events TalentSure emits. Each event carries its
 * Firebase event [name] and a [params] map; the data layer translates these into a single
 * Firebase Analytics `logEvent` call. Keeping the taxonomy here (framework-free) means
 * screens and ViewModels emit domain events instead of raw strings, so event/param names
 * are defined exactly once and stay testable without the Firebase SDK.
 *
 * Param values are restricted to the types Firebase accepts: [String], [Long], [Double],
 * [Boolean]. `Int` is widened to `Long` at construction.
 */
sealed class AnalyticsEvent(val name: String, val params: Map<String, Any?> = emptyMap()) {

    // --- Onboarding / auth ---------------------------------------------------------------
    /** [method] = "email" | "google". Firebase standard event. */
    data class SignUp(val method: String) : AnalyticsEvent(SIGN_UP, mapOf(PARAM_METHOD to method))

    /** [method] = "email" | "google". Firebase standard event. */
    data class Login(val method: String) : AnalyticsEvent(LOGIN, mapOf(PARAM_METHOD to method))

    data object SignOut : AnalyticsEvent("sign_out")

    // --- Navigation ----------------------------------------------------------------------
    /** Manual screen tracking (Compose has no Activities to auto-track). */
    data class ScreenView(val screen: String) :
        AnalyticsEvent(SCREEN_VIEW, mapOf(PARAM_SCREEN_NAME to screen))

    // --- Jobs ----------------------------------------------------------------------------
    data class ViewJobList(val count: Int) :
        AnalyticsEvent("view_job_list", mapOf(PARAM_ITEM_COUNT to count.toLong()))

    data class ViewJob(val jobId: String, val jobTitle: String) :
        AnalyticsEvent("view_job", mapOf(PARAM_JOB_ID to jobId, PARAM_JOB_TITLE to jobTitle))

    data class ApplyToJob(val jobId: String, val jobTitle: String) :
        AnalyticsEvent("apply_to_job", mapOf(PARAM_JOB_ID to jobId, PARAM_JOB_TITLE to jobTitle))

    data class SaveJob(val jobId: String, val saved: Boolean) :
        AnalyticsEvent("save_job", mapOf(PARAM_JOB_ID to jobId, PARAM_SAVED to saved))

    /** Firebase standard "search" event; [resultCount] doubles as an engagement signal. */
    data class SearchJobs(val query: String, val resultCount: Int) :
        AnalyticsEvent(SEARCH, mapOf(PARAM_SEARCH_TERM to query, PARAM_ITEM_COUNT to resultCount.toLong()))

    // --- Messaging (recruiter chat) ------------------------------------------------------
    data class OpenChat(val channelId: String) :
        AnalyticsEvent("open_chat", mapOf(PARAM_CHANNEL_ID to channelId))

    /** [recruiterId] is the other conversation member when known. */
    data class SendMessage(val channelId: String, val recruiterId: String? = null) :
        AnalyticsEvent(
            "send_message",
            buildMap {
                put(PARAM_CHANNEL_ID, channelId)
                recruiterId?.let { put(PARAM_RECRUITER_ID, it) }
            }
        )

    data class AddReaction(val emoji: String) :
        AnalyticsEvent("add_reaction", mapOf(PARAM_REACTION to emoji))

    companion object {
        // Firebase reserved/standard event names (FirebaseAnalytics.Event.*).
        const val SIGN_UP = "sign_up"
        const val LOGIN = "login"
        const val SCREEN_VIEW = "screen_view"
        const val SEARCH = "search"

        const val PARAM_METHOD = "method"
        const val PARAM_SCREEN_NAME = "screen_name"
        const val PARAM_ITEM_COUNT = "item_count"
        const val PARAM_JOB_ID = "job_id"
        const val PARAM_JOB_TITLE = "job_title"
        const val PARAM_SAVED = "saved"
        const val PARAM_SEARCH_TERM = "search_term"
        const val PARAM_CHANNEL_ID = "channel_id"
        const val PARAM_RECRUITER_ID = "recruiter_id"
        const val PARAM_REACTION = "reaction"
    }
}
