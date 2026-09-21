package com.twohearts.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.VideoCameraFront
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * TwoHearts Icon set — the ONE canonical icon vocabulary.
 *
 * ## Style rule
 *
 * TwoHearts uses a **single outlined stroke family** for everything that is
 * an action, a navigation destination, or an informational glyph. Filled
 * variants are reserved for a small number of deliberate cases:
 *
 *  - the *active/selected* state of a toggle or navigation item;
 *  - destructive confirmation affordances ([Destructive]);
 *  - the brand heart ([HeartFilled]) when it marks a saved/favourite item.
 *
 * Material's default icon set mixes filled and outlined weights freely.
 * Screens previously reached for `Icons.Default.*` (Material 2 naming, which
 * resolves to *filled*), `Icons.Filled.*` and `Icons.Outlined.*` almost
 * interchangeably — so the same "edit" action could render as a solid pencil
 * on one screen and an outlined one on another. That inconsistency is what
 * the directive's iconography rule prohibits.
 *
 * ## Usage
 *
 * ```kotlin
 * Icon(imageVector = ThIcons.Edit, contentDescription = "Edit note")
 * ```
 *
 * Screens should reference [ThIcons] rather than importing
 * `androidx.compose.material.icons.*` directly, so the vocabulary stays
 * auditable in one place. Prefer a plain text label over an icon when the
 * label is clearer than any glyph.
 */
object ThIcons {
    // ── Navigation & chrome ─────────────────────────────────────────
    val Back = Icons.AutoMirrored.Outlined.ArrowBack
    val Close = Icons.Outlined.Close
    val ChevronRight = Icons.Outlined.ChevronRight
    val Menu = Icons.Outlined.Menu
    val More = Icons.Outlined.MoreHoriz
    val Home = Icons.Outlined.Home

    // ── Actions ─────────────────────────────────────────────────────
    val Add = Icons.Outlined.Add
    val Edit = Icons.Outlined.Edit
    val Search = Icons.Outlined.Search
    val SearchOff = Icons.Outlined.SearchOff
    val Clear = Icons.Outlined.Clear
    val Check = Icons.Outlined.Check
    val CheckCircle = Icons.Outlined.CheckCircle

    /** Unselected counterpart to [CheckCircle] for radio-style option rows. */
    val CircleOutline = Icons.Outlined.RadioButtonUnchecked
    val Attach = Icons.Outlined.AttachFile

    /** Destructive actions — the one place a filled glyph is intentional. */
    val Trash = Icons.Filled.Delete
    val DeleteForever = Icons.Filled.DeleteForever

    // ── Status ──────────────────────────────────────────────────────
    val Info = Icons.Outlined.Info
    val Warning = Icons.Outlined.Warning
    val Error = Icons.Outlined.Error
    val Lock = Icons.Outlined.Lock
    val Key = Icons.Outlined.Key
    val CloudUpload = Icons.Outlined.CloudUpload
    val CloudOff = Icons.Outlined.CloudOff
    val WifiOff = Icons.Outlined.WifiOff

    // ── Relationship & brand ────────────────────────────────────────
    /** Outlined heart — decoration, hints, empty states. */
    val Heart = Icons.Outlined.FavoriteBorder

    /** Filled heart — an item the couple has marked as meaningful. */
    val HeartFilled = Icons.Filled.Favorite

    val Account = Icons.Outlined.AccountCircle
    val Person = Icons.Outlined.Person
    val People = Icons.Outlined.People
    val Gift = Icons.Outlined.CardGiftcard
    val Star = Icons.Outlined.Star
    val ThumbUp = Icons.Outlined.ThumbUp

    // ── Content types ───────────────────────────────────────────────
    val Note = Icons.Outlined.Note
    val Article = Icons.Outlined.Article
    val FileText = Icons.Outlined.Description
    val File = Icons.Outlined.InsertDriveFile
    val Folder = Icons.Outlined.FolderOpen
    val Image = Icons.Outlined.Image
    val PhotoLibrary = Icons.Outlined.PhotoLibrary
    val Camera = Icons.Outlined.CameraAlt
    val Video = Icons.Outlined.VideoCameraFront
    val VideoFile = Icons.Outlined.VideoFile
    val Play = Icons.Outlined.PlayCircle

    // ── Time, dates, reminders ──────────────────────────────────────
    val Calendar = Icons.Outlined.CalendarToday
    val CalendarMonth = Icons.Outlined.CalendarMonth
    val DateRange = Icons.Outlined.DateRange
    val Clock = Icons.Outlined.Schedule
    val Alarm = Icons.Outlined.Alarm
    val Repeat = Icons.Outlined.Repeat
    val Timeline = Icons.Outlined.Timeline

    // ── Places & mood ───────────────────────────────────────────────
    val MapPin = Icons.Outlined.LocationOn
    val Place = Icons.Outlined.Place
    val Smile = Icons.Outlined.Mood

    // ── Notifications ───────────────────────────────────────────────
    val Bell = Icons.Outlined.Notifications
    val BellOff = Icons.Outlined.NotificationsOff
    val BellNone = Icons.Outlined.NotificationsNone

    // ── Games & Yuki ────────────────────────────────────────────────
    val Gamepad = Icons.Outlined.SportsEsports
    val Cat = Icons.Outlined.Pets
    val Lightbulb = Icons.Outlined.Lightbulb

    // ── Settings & storage ──────────────────────────────────────────
    val Settings = Icons.Outlined.Settings
    val Palette = Icons.Outlined.Palette
    val Storage = Icons.Outlined.Storage
    val Cleaning = Icons.Outlined.CleaningServices
    val Analytics = Icons.Outlined.Analytics
    val Security = Icons.Outlined.Security

    // ── Appearance toggles ──────────────────────────────────────────
    val LightMode = Icons.Outlined.LightMode
    val DarkMode = Icons.Outlined.DarkMode
    val SystemTheme = Icons.Outlined.SettingsBrightness

    /** Every icon in the set, keyed by name — for previews and audits. */
    val allIcons: Map<String, ImageVector> = mapOf(
        "Back" to Back,
        "Close" to Close,
        "ChevronRight" to ChevronRight,
        "Menu" to Menu,
        "More" to More,
        "Home" to Home,
        "Add" to Add,
        "Edit" to Edit,
        "Search" to Search,
        "SearchOff" to SearchOff,
        "Clear" to Clear,
        "Check" to Check,
        "CheckCircle" to CheckCircle,
        "CircleOutline" to CircleOutline,
        "Attach" to Attach,
        "Trash" to Trash,
        "DeleteForever" to DeleteForever,
        "Info" to Info,
        "Warning" to Warning,
        "Error" to Error,
        "Lock" to Lock,
        "Key" to Key,
        "CloudUpload" to CloudUpload,
        "CloudOff" to CloudOff,
        "WifiOff" to WifiOff,
        "Heart" to Heart,
        "HeartFilled" to HeartFilled,
        "Account" to Account,
        "Person" to Person,
        "People" to People,
        "Gift" to Gift,
        "Star" to Star,
        "ThumbUp" to ThumbUp,
        "Note" to Note,
        "Article" to Article,
        "FileText" to FileText,
        "File" to File,
        "Folder" to Folder,
        "Image" to Image,
        "PhotoLibrary" to PhotoLibrary,
        "Camera" to Camera,
        "Video" to Video,
        "VideoFile" to VideoFile,
        "Play" to Play,
        "Calendar" to Calendar,
        "CalendarMonth" to CalendarMonth,
        "DateRange" to DateRange,
        "Clock" to Clock,
        "Alarm" to Alarm,
        "Repeat" to Repeat,
        "Timeline" to Timeline,
        "MapPin" to MapPin,
        "Place" to Place,
        "Smile" to Smile,
        "Bell" to Bell,
        "BellOff" to BellOff,
        "BellNone" to BellNone,
        "Gamepad" to Gamepad,
        "Cat" to Cat,
        "Lightbulb" to Lightbulb,
        "Settings" to Settings,
        "Palette" to Palette,
        "Storage" to Storage,
        "Cleaning" to Cleaning,
        "Analytics" to Analytics,
        "Security" to Security,
        "LightMode" to LightMode,
        "DarkMode" to DarkMode,
        "SystemTheme" to SystemTheme,
    )
}
