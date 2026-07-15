package com.example.individual_project.ui.testtags

/**
 * Central registry of Compose semantic test tags. Instrumented tests must look these up via
 * `Modifier.testTag`/`onNodeWithTag` rather than falling back to visible text, so tests stay
 * stable across copy changes -- a missing tag is a production-UI gap to fix, not something to
 * paper over with a text/tag fallback helper.
 */
object TestTags {
    const val LOGIN_EMAIL    = "login_email"
    const val LOGIN_PASSWORD = "login_password"
    const val LOGIN_SUBMIT   = "login_submit"
    const val LOGIN_REGISTER = "login_register"
    const val LOGIN_FORGOT_PASSWORD = "login_forgot_password"
    const val LOGIN_ERROR    = "login_error"

    const val REGISTER_NAME             = "register_name"
    const val REGISTER_EMAIL            = "register_email"
    const val REGISTER_PHONE            = "register_phone"
    const val REGISTER_PASSWORD         = "register_password"
    const val REGISTER_CONFIRM_PASSWORD = "register_confirm_password"
    const val REGISTER_TERMS            = "register_terms"
    const val REGISTER_SUBMIT           = "register_submit"
    const val REGISTER_BACK             = "register_back"
    const val REGISTER_ERROR            = "register_error"

    const val HOME_SCREEN  = "home_screen"
    const val NAV_HOME_TAB    = "nav_home_tab"
    const val NAV_SEARCH_TAB  = "nav_search_tab"
    const val NAV_TICKETS_TAB = "nav_tickets_tab"
    const val NAV_PROFILE_TAB = "nav_profile_tab"

    const val EVENT_LIST       = "event_list"
    const val EVENT_CARD_PREFIX = "event_card_"
    const val BOOK_NOW_BUTTON = "book_now_button"

    const val BOOKING_QUANTITY_DECREASE = "booking_quantity_decrease"
    const val BOOKING_QUANTITY_INCREASE = "booking_quantity_increase"
    const val BOOKING_QUANTITY_VALUE    = "booking_quantity_value"
    const val BOOKING_CONTINUE          = "booking_continue"

    const val PROFILE_SCREEN     = "profile_screen"
    const val PROFILE_EDIT       = "profile_edit"
    const val PROFILE_LOGOUT     = "profile_logout"

    const val ADMIN_CREATE_EVENT = "admin_create_event"
}
