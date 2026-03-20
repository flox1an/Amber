package com.greenart7c3.nostrsigner.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.greenart7c3.nostrsigner.R

@Immutable
sealed class Route(
    @param:StringRes val titleRes: Int,
    val route: String,
    @param:DrawableRes val icon: Int,
) {
    data object TorSettings : Route(
        titleRes = R.string.connect_via_tor_short,
        route = "TorSettings",
        icon = R.drawable.incoming_request,
    )

    data object Login : Route(
        titleRes = R.string.app_name,
        route = "login",
        icon = R.drawable.incoming_request,
    )

    data object IncomingRequest : Route(
        titleRes = R.string.incoming_request,
        route = "IncomingRequest",
        icon = R.drawable.incoming_request,
    )

    data object Applications : Route(
        route = "Applications",
        titleRes = R.string.applications,
        icon = R.drawable.applications,
    )

    data object Settings : Route(
        titleRes = R.string.settings,
        route = "Settings",
        icon = R.drawable.settings,
    )

    data object Permission : Route(
        titleRes = R.string.permissions,
        route = "Permission/{packageName}",
        icon = R.drawable.settings,
    )

    data object AccountBackup : Route(
        titleRes = R.string.account_backup,
        route = "AccountBackup",
        icon = R.drawable.settings,
    )

    data object ExportAllAccounts : Route(
        titleRes = R.string.export_all_accounts_title,
        route = "ExportAllAccounts",
        icon = R.drawable.settings,
    )

    data object Logs : Route(
        titleRes = R.string.logs,
        route = "Logs",
        icon = R.drawable.settings,
    )

    data object QrCode : Route(
        titleRes = R.string.qrCode,
        route = "qrcode/{content}",
        icon = R.drawable.settings,
    )

    data object ActiveRelays : Route(
        titleRes = R.string.relays,
        route = "ActiveRelays",
        icon = R.drawable.relays,
    )

    data object Language : Route(
        titleRes = R.string.language,
        route = "Language",
        icon = R.drawable.settings,
    )

    data object DefaultRelays : Route(
        titleRes = R.string.default_relays,
        route = "DefaultRelays",
        icon = R.drawable.settings,
    )

    data object SignPolicy : Route(
        titleRes = R.string.sign_policy,
        route = "SignPolicy",
        icon = R.drawable.settings,
    )

    data object Security : Route(
        titleRes = R.string.security,
        route = "Security",
        icon = R.drawable.settings,
    )

    data object Accounts : Route(
        titleRes = R.string.accounts,
        route = "Accounts",
        icon = R.drawable.settings,
    )

    data object NewApplication : Route(
        titleRes = R.string.add_a_new_application,
        route = "NewApplication",
        icon = R.drawable.settings,
    )

    data object NewNsecBunker : Route(
        titleRes = R.string.add_a_nsecbunker,
        route = "NewNsecBunker",
        icon = R.drawable.settings,
    )

    data object NSecBunkerCreated : Route(
        titleRes = R.string.add_a_nsecbunker,
        route = "NewNsecBunkerCreated/{key}",
        icon = R.drawable.settings,
    )

    data object Activity : Route(
        titleRes = R.string.activity_title,
        route = "Activity/{key}",
        icon = R.drawable.settings,
    )

    data object RelayLogScreen : Route(
        titleRes = R.string.logs,
        route = "RelayLogScreen/{url}",
        icon = R.drawable.settings,
    )

    data object EditConfiguration : Route(
        titleRes = R.string.edit_configuration,
        route = "EditConfiguration/{key}",
        icon = R.drawable.settings,
    )

    data object SetupPin : Route(
        titleRes = R.string.setup_pin,
        route = "SetupPin",
        icon = R.drawable.settings,
    )

    data object ConfirmPin : Route(
        titleRes = R.string.confirm_pin,
        route = "ConfirmPin/{pin}",
        icon = R.drawable.settings,
    )

    data object SeeDetails : Route(
        titleRes = R.string.incoming_request,
        route = "SeeDetails",
        icon = R.drawable.settings,
    )

    data object RelaysScreen : Route(
        titleRes = R.string.relays,
        route = "RelaysScreen",
        icon = R.drawable.settings,
    )

    data object DefaultProfileRelaysScreen : Route(
        titleRes = R.string.default_profile_relays,
        route = "DefaultProfileRelaysScreen",
        icon = R.drawable.settings,
    )

    data object EditProfile : Route(
        titleRes = R.string.edit_profile,
        route = "EditProfile/{key}",
        icon = R.drawable.settings,
    )

    data object Feedback : Route(
        titleRes = R.string.give_us_feedback,
        route = "Feedback",
        icon = R.drawable.settings,
    )

    data object Activities : Route(
        titleRes = R.string.activities,
        route = "Activities",
        icon = R.drawable.incoming_request,
    )

    data object CrashReport : Route(
        titleRes = R.string.crashreport_found,
        route = "CrashReport",
        icon = R.drawable.settings,
    )
}

val routes = listOf(
    Route.IncomingRequest,
    Route.Applications,
    Route.Settings,
    Route.Permission,
    Route.AccountBackup,
    Route.ExportAllAccounts,
    Route.Logs,
    Route.ActiveRelays,
    Route.Language,
    Route.DefaultRelays,
    Route.SignPolicy,
    Route.Security,
    Route.Accounts,
    Route.NewApplication,
    Route.NewNsecBunker,
    Route.NSecBunkerCreated,
    Route.Activity,
    Route.RelayLogScreen,
    Route.EditConfiguration,
    Route.SetupPin,
    Route.ConfirmPin,
    Route.SeeDetails,
    Route.RelaysScreen,
    Route.DefaultProfileRelaysScreen,
    Route.TorSettings,
    Route.EditProfile,
    Route.Feedback,
    Route.Activities,
    Route.CrashReport,
)
