package com.twohearts.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VideoCameraFront
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * TwoHearts Icon set — stroke-based vector icons matching legacy Icon.tsx.
 *
 * Maps legacy SVG icon names to Material Icons. In later stages, these can be
 * replaced with custom SVG drawables for exact visual matching.
 *
 * Legacy icon names → Material Icon equivalents:
 * - IconBack → Icons.Filled.ArrowBack
 * - IconClose → Icons.Filled.Close
 * - IconCheck → Icons.Filled.Check
 * - IconInfo → Icons.Outlined.Info
 * - IconChevronRight → Icons.Outlined.ChevronRight
 * - IconHome → Icons.Filled.Home
 * - IconSearch → Icons.Filled.Search
 * - IconMenu → Icons.Outlined.Menu
 * - IconPlus → Icons.Filled.Add
 * - IconHeart → Icons.Outlined.FavoriteBorder
 * - IconSmile → Icons.Outlined.Mood
 * - IconImage → Icons.Outlined.Image
 * - IconGamepad → Icons.Outlined.Gamepad
 * - IconFileText → Icons.Outlined.Description
 * - IconFile → Icons.Outlined.Description
 * - IconTrash → Icons.Filled.Delete
 * - IconEdit → Icons.Filled.Edit
 * - IconCalendar → Icons.Filled.DateRange
 * - IconMapPin → Icons.Outlined.LocationOn
 * - IconBellOff → Icons.Outlined.NotificationsOff
 * - IconCamera → Icons.Outlined.CameraAlt
 * - IconVideo → Icons.Outlined.VideoCameraFront
 * - IconLock → Icons.Filled.Lock
 * - IconBell → Icons.Filled.Notifications
 * - IconClock → Icons.Outlined.Schedule
 * - IconRepeat → Icons.Outlined.Repeat
 * - IconSettings → Icons.Filled.Settings
 * - IconCat → Icons.Outlined.Pets
 */
object ThIcons {
    val Back = Icons.Filled.ArrowBack
    val Close = Icons.Filled.Close
    val Check = Icons.Filled.Check
    val CheckCircle = Icons.Outlined.CheckCircle
    val Info = Icons.Outlined.Info
    val ChevronRight = Icons.Outlined.ChevronRight
    val Home = Icons.Filled.Home
    val Search = Icons.Filled.Search
    val Menu = Icons.Outlined.Menu
    val Plus = Icons.Filled.Add
    val Heart = Icons.Outlined.FavoriteBorder
    val Smile = Icons.Outlined.Mood
    val Image = Icons.Outlined.Image
    val Gamepad = Icons.Outlined.Gamepad
    val FileText = Icons.Outlined.Description
    val File = Icons.Outlined.Description
    val Trash = Icons.Filled.Delete
    val Edit = Icons.Filled.Edit
    val Calendar = Icons.Filled.DateRange
    val MapPin = Icons.Outlined.LocationOn
    val BellOff = Icons.Outlined.NotificationsOff
    val Camera = Icons.Outlined.CameraAlt
    val Video = Icons.Outlined.VideoCameraFront
    val Lock = Icons.Filled.Lock
    val Bell = Icons.Filled.Notifications
    val Clock = Icons.Outlined.Schedule
    val Repeat = Icons.Outlined.Repeat
    val Settings = Icons.Filled.Settings
    val Cat = Icons.Outlined.Pets
    val Account = Icons.Outlined.AccountCircle
    val Gift = Icons.Outlined.CardGiftcard
    val DarkMode = Icons.Outlined.DarkMode
    val CloudUpload = Icons.Outlined.CloudUpload

    /** All icons in the set for enumeration/preview. */
    val allIcons: Map<String, ImageVector> = mapOf(
        "Back" to Back,
        "Close" to Close,
        "Check" to Check,
        "CheckCircle" to CheckCircle,
        "Info" to Info,
        "ChevronRight" to ChevronRight,
        "Home" to Home,
        "Search" to Search,
        "Menu" to Menu,
        "Plus" to Plus,
        "Heart" to Heart,
        "Smile" to Smile,
        "Image" to Image,
        "Gamepad" to Gamepad,
        "FileText" to FileText,
        "File" to File,
        "Trash" to Trash,
        "Edit" to Edit,
        "Calendar" to Calendar,
        "MapPin" to MapPin,
        "BellOff" to BellOff,
        "Camera" to Camera,
        "Video" to Video,
        "Lock" to Lock,
        "Bell" to Bell,
        "Clock" to Clock,
        "Repeat" to Repeat,
        "Settings" to Settings,
        "Cat" to Cat,
        "Account" to Account,
        "Gift" to Gift,
        "DarkMode" to DarkMode,
        "CloudUpload" to CloudUpload,
    )
}
