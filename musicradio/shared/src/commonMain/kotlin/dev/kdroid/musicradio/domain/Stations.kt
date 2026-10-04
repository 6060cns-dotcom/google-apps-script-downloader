package dev.kdroid.musicradio.domain

import androidx.compose.runtime.Immutable
import musicradio.shared.generated.resources.Res
import musicradio.shared.generated.resources.channel_kcm_02
import musicradio.shared.generated.resources.channel_kcm_03
import musicradio.shared.generated.resources.channel_kcm_04
import musicradio.shared.generated.resources.channel_kcm_05
import musicradio.shared.generated.resources.channel_kcm_06
import musicradio.shared.generated.resources.channel_kcm_07
import musicradio.shared.generated.resources.channel_kcm_08
import musicradio.shared.generated.resources.channel_kcm_09
import musicradio.shared.generated.resources.channel_kcm_10
import musicradio.shared.generated.resources.channel_kcm_107
import musicradio.shared.generated.resources.channel_kcm_11
import musicradio.shared.generated.resources.channel_kcm_112
import musicradio.shared.generated.resources.channel_kcm_113
import musicradio.shared.generated.resources.channel_kcm_115
import musicradio.shared.generated.resources.channel_kcm_116
import musicradio.shared.generated.resources.channel_kcm_117
import musicradio.shared.generated.resources.channel_kcm_118
import musicradio.shared.generated.resources.channel_kcm_12
import musicradio.shared.generated.resources.channel_kcm_13
import musicradio.shared.generated.resources.channel_kcm_14
import musicradio.shared.generated.resources.channel_kcm_15
import musicradio.shared.generated.resources.channel_kcm_16
import musicradio.shared.generated.resources.channel_kcm_17
import musicradio.shared.generated.resources.channel_kcm_18
import musicradio.shared.generated.resources.channel_kcm_19
import musicradio.shared.generated.resources.channel_kcm_20
import musicradio.shared.generated.resources.channel_kcm_21
import musicradio.shared.generated.resources.channel_kcm_22
import musicradio.shared.generated.resources.channel_kcm_23
import musicradio.shared.generated.resources.channel_kcm_25
import musicradio.shared.generated.resources.channel_kcm_26
import musicradio.shared.generated.resources.channel_kcm_27
import musicradio.shared.generated.resources.channel_kcm_28
import musicradio.shared.generated.resources.channel_kcm_29
import musicradio.shared.generated.resources.channel_kcm_30
import musicradio.shared.generated.resources.channel_kcm_31
import musicradio.shared.generated.resources.channel_kcm_32
import musicradio.shared.generated.resources.channel_kcm_33
import musicradio.shared.generated.resources.channel_kcm_34
import musicradio.shared.generated.resources.channel_kcm_35
import musicradio.shared.generated.resources.channel_kcm_39
import musicradio.shared.generated.resources.channel_kcm_40
import musicradio.shared.generated.resources.channel_kcm_41
import musicradio.shared.generated.resources.channel_kcm_42
import musicradio.shared.generated.resources.channel_kcm_46
import musicradio.shared.generated.resources.channel_kcm_47
import musicradio.shared.generated.resources.channel_kcm_48
import musicradio.shared.generated.resources.channel_kcm_49
import musicradio.shared.generated.resources.channel_kcm_50
import musicradio.shared.generated.resources.channel_kcm_51
import musicradio.shared.generated.resources.channel_kcm_52
import musicradio.shared.generated.resources.channel_kcm_53
import musicradio.shared.generated.resources.channel_kcm_54
import musicradio.shared.generated.resources.channel_kcm_55
import musicradio.shared.generated.resources.channel_kcm_56
import musicradio.shared.generated.resources.channel_kcm_57
import musicradio.shared.generated.resources.channel_kcm_58
import musicradio.shared.generated.resources.channel_kcm_59
import musicradio.shared.generated.resources.channel_kcm_60
import musicradio.shared.generated.resources.channel_kcm_61
import musicradio.shared.generated.resources.channel_kcm_62
import musicradio.shared.generated.resources.channel_kcm_63
import musicradio.shared.generated.resources.channel_kcm_64
import musicradio.shared.generated.resources.channel_kcm_65
import musicradio.shared.generated.resources.channel_kcm_66
import musicradio.shared.generated.resources.channel_kcm_665
import musicradio.shared.generated.resources.channel_kcm_67
import musicradio.shared.generated.resources.channel_kcm_68
import musicradio.shared.generated.resources.channel_kcm_69
import musicradio.shared.generated.resources.channel_kcm_70
import musicradio.shared.generated.resources.channel_kcm_72
import musicradio.shared.generated.resources.channel_kcm_73
import musicradio.shared.generated.resources.channel_kcm_74
import musicradio.shared.generated.resources.channel_kcm_75
import musicradio.shared.generated.resources.channel_kcm_76
import musicradio.shared.generated.resources.channel_kcm_77
import musicradio.shared.generated.resources.channel_kcm_78
import musicradio.shared.generated.resources.channel_kcm_79
import musicradio.shared.generated.resources.channel_kcm_80
import musicradio.shared.generated.resources.channel_kcm_82
import musicradio.shared.generated.resources.channel_kcm_85
import musicradio.shared.generated.resources.channel_kcm_999
import musicradio.shared.generated.resources.channel_kcm_livemusic
import musicradio.shared.generated.resources.channel_y24_10
import musicradio.shared.generated.resources.channel_y24_1054
import musicradio.shared.generated.resources.channel_y24_1055
import musicradio.shared.generated.resources.channel_y24_1056
import musicradio.shared.generated.resources.channel_y24_1057
import musicradio.shared.generated.resources.channel_y24_1058
import musicradio.shared.generated.resources.channel_y24_11
import musicradio.shared.generated.resources.channel_y24_12
import musicradio.shared.generated.resources.channel_y24_13
import musicradio.shared.generated.resources.channel_y24_14
import musicradio.shared.generated.resources.channel_y24_16
import musicradio.shared.generated.resources.channel_y24_17
import musicradio.shared.generated.resources.channel_y24_19
import musicradio.shared.generated.resources.channel_y24_2
import musicradio.shared.generated.resources.channel_y24_20
import musicradio.shared.generated.resources.channel_y24_21
import musicradio.shared.generated.resources.channel_y24_22
import musicradio.shared.generated.resources.channel_y24_23
import musicradio.shared.generated.resources.channel_y24_24
import musicradio.shared.generated.resources.channel_y24_25
import musicradio.shared.generated.resources.channel_y24_26
import musicradio.shared.generated.resources.channel_y24_27
import musicradio.shared.generated.resources.channel_y24_28
import musicradio.shared.generated.resources.channel_y24_29
import musicradio.shared.generated.resources.channel_y24_3
import musicradio.shared.generated.resources.channel_y24_30
import musicradio.shared.generated.resources.channel_y24_31
import musicradio.shared.generated.resources.channel_y24_32
import musicradio.shared.generated.resources.channel_y24_33
import musicradio.shared.generated.resources.channel_y24_34
import musicradio.shared.generated.resources.channel_y24_35
import musicradio.shared.generated.resources.channel_y24_37
import musicradio.shared.generated.resources.channel_y24_38
import musicradio.shared.generated.resources.channel_y24_39
import musicradio.shared.generated.resources.channel_y24_4
import musicradio.shared.generated.resources.channel_y24_40
import musicradio.shared.generated.resources.channel_y24_41
import musicradio.shared.generated.resources.channel_y24_42
import musicradio.shared.generated.resources.channel_y24_43
import musicradio.shared.generated.resources.channel_y24_44
import musicradio.shared.generated.resources.channel_y24_45
import musicradio.shared.generated.resources.channel_y24_46
import musicradio.shared.generated.resources.channel_y24_47
import musicradio.shared.generated.resources.channel_y24_48
import musicradio.shared.generated.resources.channel_y24_49
import musicradio.shared.generated.resources.channel_y24_5
import musicradio.shared.generated.resources.channel_y24_50
import musicradio.shared.generated.resources.channel_y24_51
import musicradio.shared.generated.resources.channel_y24_52
import musicradio.shared.generated.resources.channel_y24_53
import musicradio.shared.generated.resources.channel_y24_54
import musicradio.shared.generated.resources.channel_y24_55
import musicradio.shared.generated.resources.channel_y24_56
import musicradio.shared.generated.resources.channel_y24_57
import musicradio.shared.generated.resources.channel_y24_58
import musicradio.shared.generated.resources.channel_y24_6
import musicradio.shared.generated.resources.channel_y24_7
import musicradio.shared.generated.resources.channel_y24_8
import musicradio.shared.generated.resources.channel_y24_9
import musicradio.shared.generated.resources.channel_y24_main
import musicradio.shared.generated.resources.station_beshiour_hay
import musicradio.shared.generated.resources.station_chabad_org
import musicradio.shared.generated.resources.station_jewish_music_stream
import musicradio.shared.generated.resources.station_jewish_radio_network
import musicradio.shared.generated.resources.station_kol_barama
import musicradio.shared.generated.resources.station_kol_hahalakha
import musicradio.shared.generated.resources.station_kol_hay
import musicradio.shared.generated.resources.station_kol_hay_music
import musicradio.shared.generated.resources.station_kol_play
import musicradio.shared.generated.resources.station_kol_simha
import musicradio.shared.generated.resources.station_lakewood_scoop
import musicradio.shared.generated.resources.station_makelet_shira
import musicradio.shared.generated.resources.station_toker_fm
import musicradio.shared.generated.resources.station_yiddish24
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class StationCategory { Music, Torah, News }

/**
 * One stream inside a station. [title] is `null` for a station's own flagship channel, which
 * carries the station's localised name; the extra channels of a multi-stream station are proper
 * names of their own and stay as written.
 *
 * [artwork] is `null` unless the broadcaster publishes a cover for this particular stream, in
 * which case the station's own logo stands in.
 */
@Immutable
data class Channel(val id: String, val streamUrl: String, val title: String? = null, val artwork: DrawableResource? = null)

@Immutable
data class Station(
    val id: String,
    val name: StringResource,
    val artwork: DrawableResource,
    val category: StationCategory,
    val channels: List<Channel>,
) {
    val multiChannel: Boolean get() = channels.size > 1
}

/**
 * The catalog carried over from KdroidRadioDesktop's `RadioRepository`. Stream URLs are the
 * broadcasters' own public endpoints, so they are data, not configuration - a station that moves
 * its stream is edited here.
 */
object Stations {

    val all: List<Station> = listOf(
        Station(
            id = "beshiour_hay",
            name = Res.string.station_beshiour_hay,
            artwork = Res.drawable.station_beshiour_hay,
            category = StationCategory.Torah,
            channels = listOf(
                // One channel per magid shiur, from the broadcaster's own feed
                // (kcm.fm/Home/LiveJ, cat 5). No per-channel cover is published for them, so
                // they fall back to the station logo.
                //
                // The single stream this station used to carry, live.kcm.fm/livetorani, is gone:
                // it 404s in every form the feed still advertises, and the feed's own
                // playing_date for it stopped at 2024-06-16 while every channel below updates by
                // the minute. The lecture channels replaced it.
                //
                // Keyed by mount, never by the feed's item id - the two diverge from 103 up, and
                // one hidden entry (visible=2, "מרן רבנו עובדיה יוסף") points at mount 105, which
                // the visible "הרב שניר גואטה" already owns.
                Channel("beshiour_hay/89", "https://live.kcm.fm/89", "הרב אברהם יוסף"),
                Channel("beshiour_hay/92", "https://live.kcm.fm/92", "הרב בן ציון מוצפי"),
                Channel("beshiour_hay/93", "https://live.kcm.fm/93", "הרב ברוך רוזנבלום"),
                Channel("beshiour_hay/94", "https://live.kcm.fm/94", "הרב זמיר כהן"),
                Channel("beshiour_hay/95", "https://live.kcm.fm/95", "הרב חיים זאיד"),
                Channel("beshiour_hay/96", "https://live.kcm.fm/96", "הרב יגאל כהן"),
                Channel("beshiour_hay/97", "https://live.kcm.fm/97", "הרב יוסף בן פורת"),
                Channel("beshiour_hay/98", "https://live.kcm.fm/98", "הרב יצחק פנגר"),
                Channel("beshiour_hay/99", "https://live.kcm.fm/99", "הרב מרדכי נויגרשל"),
                Channel("beshiour_hay/101", "https://live.kcm.fm/101", "הרב ניסים יגן זצ\"ל"),
                Channel("beshiour_hay/102", "https://live.kcm.fm/102", "הרב ראובן אלבז"),
                Channel("beshiour_hay/104", "https://live.kcm.fm/104", "הרב שלמה לוינשטיין"),
                Channel("beshiour_hay/105", "https://live.kcm.fm/105", "מרן הרב עובדיה זצ\"ל"),
            ),
        ),
        single(
            "kol_hahalakha",
            Res.string.station_kol_hahalakha,
            Res.drawable.station_kol_hahalakha,
            StationCategory.Torah,
            "https://broadcast.adpronet.com/radio/8050/radio.mp3",
        ),
        single(
            "kol_barama",
            Res.string.station_kol_barama,
            Res.drawable.station_kol_barama,
            StationCategory.News,
            "https://cdn.cybercdn.live/Kol_Barama/Live_Audio/icecast.audio",
        ),
        single("kol_hay", Res.string.station_kol_hay, Res.drawable.station_kol_hay, StationCategory.News, "https://live.kcm.fm/live-new"),
        single(
            "kol_play",
            Res.string.station_kol_play,
            Res.drawable.station_kol_play,
            StationCategory.Music,
            "https://cdn.cybercdn.live/Kol_Barama/Music/icecast.audio",
        ),
        Station(
            id = "kol_hay_music",
            name = Res.string.station_kol_hay_music,
            artwork = Res.drawable.station_kol_hay_music,
            category = StationCategory.Music,
            channels = listOf(
                Channel("kol_hay_music/main", "https://live.kcm.fm/livemusic", artwork = Res.drawable.channel_kcm_livemusic),
                Channel(
                    "kol_hay_music/112",
                    "https://live.kcm.fm/112",
                    "פלייליסט שירי השבת של אסם נסטלה",
                    artwork = Res.drawable.channel_kcm_112,
                ),
                Channel("kol_hay_music/17", "https://live.kcm.fm/17", "תענוג לשבת", artwork = Res.drawable.channel_kcm_17),
                Channel("kol_hay_music/665", "https://live.kcm.fm/665", "שרים רפואה", artwork = Res.drawable.channel_kcm_665),
                Channel("kol_hay_music/117", "https://live.kcm.fm/117", "חסידיש רוטבלט", artwork = Res.drawable.channel_kcm_117),
                Channel("kol_hay_music/118", "https://live.kcm.fm/118", "שירי ירושלים", artwork = Res.drawable.channel_kcm_118),
                Channel("kol_hay_music/03", "https://live.kcm.fm/03", "ישראלי אלטרנטיבי", artwork = Res.drawable.channel_kcm_03),
                Channel("kol_hay_music/04", "https://live.kcm.fm/04", "ים תיכוני", artwork = Res.drawable.channel_kcm_04),
                Channel("kol_hay_music/05", "https://live.kcm.fm/05", "בטעם של פעם", artwork = Res.drawable.channel_kcm_05),
                Channel("kol_hay_music/06", "https://live.kcm.fm/06", "רק ילדים", artwork = Res.drawable.channel_kcm_06),
                Channel("kol_hay_music/08", "https://live.kcm.fm/08", "על הפארנצע'ס", artwork = Res.drawable.channel_kcm_08),
                Channel("kol_hay_music/09", "https://live.kcm.fm/09", "770", artwork = Res.drawable.channel_kcm_09),
                Channel("kol_hay_music/10", "https://live.kcm.fm/10", "נעימות", artwork = Res.drawable.channel_kcm_10),
                Channel("kol_hay_music/12", "https://live.kcm.fm/12", "חזנות", artwork = Res.drawable.channel_kcm_12),
                Channel("kol_hay_music/13", "https://live.kcm.fm/13", "פיוטים", artwork = Res.drawable.channel_kcm_13),
                Channel("kol_hay_music/19", "https://live.kcm.fm/19", "קומזיץ", artwork = Res.drawable.channel_kcm_19),
                Channel("kol_hay_music/20", "https://live.kcm.fm/20", "צעד תימני", artwork = Res.drawable.channel_kcm_20),
                Channel("kol_hay_music/22", "https://live.kcm.fm/22", "english classics", artwork = Res.drawable.channel_kcm_22),
                Channel("kol_hay_music/28", "https://live.kcm.fm/28", "מוזיקה אלקטרונית", artwork = Res.drawable.channel_kcm_28),
                Channel("kol_hay_music/35", "https://live.kcm.fm/35", "הפלייליסט", artwork = Res.drawable.channel_kcm_35),
                Channel("kol_hay_music/50", "https://live.kcm.fm/50", "שירת התורה", artwork = Res.drawable.channel_kcm_50),
                Channel("kol_hay_music/52", "https://live.kcm.fm/52", "הדואטים הגדולים", artwork = Res.drawable.channel_kcm_52),
                Channel("kol_hay_music/53", "https://live.kcm.fm/53", "אידיש", artwork = Res.drawable.channel_kcm_53),
                Channel("kol_hay_music/59", "https://live.kcm.fm/59", "שנות התש\"מ", artwork = Res.drawable.channel_kcm_59),
                Channel("kol_hay_music/60", "https://live.kcm.fm/60", "שנות התש\"נ", artwork = Res.drawable.channel_kcm_60),
                Channel("kol_hay_music/76", "https://live.kcm.fm/76", "מזרחית של פעם", artwork = Res.drawable.channel_kcm_76),
                Channel("kol_hay_music/14", "https://live.kcm.fm/14", "ווקאלי", artwork = Res.drawable.channel_kcm_14),
                Channel("kol_hay_music/78", "https://live.kcm.fm/78", "להתכונן לשבת באווירה מזרחית", artwork = Res.drawable.channel_kcm_78),
                Channel("kol_hay_music/48", "https://live.kcm.fm/48", "בדרך לכותל", artwork = Res.drawable.channel_kcm_48),
                Channel("kol_hay_music/02", "https://live.kcm.fm/02", "נכנסים לקצב", artwork = Res.drawable.channel_kcm_02),
                Channel("kol_hay_music/11", "https://live.kcm.fm/11", "בין הערביים", artwork = Res.drawable.channel_kcm_11),
                Channel("kol_hay_music/25", "https://live.kcm.fm/25", "לרקוד בחתונה", artwork = Res.drawable.channel_kcm_25),
                Channel("kol_hay_music/27", "https://live.kcm.fm/27", "מנגינה של תקווה", artwork = Res.drawable.channel_kcm_27),
                Channel("kol_hay_music/34", "https://live.kcm.fm/34", "הופעות", artwork = Res.drawable.channel_kcm_34),
                Channel("kol_hay_music/40", "https://live.kcm.fm/40", "עם הקפה של הבוקר", artwork = Res.drawable.channel_kcm_40),
                Channel("kol_hay_music/41", "https://live.kcm.fm/41", "תודה לאבא", artwork = Res.drawable.channel_kcm_41),
                Channel("kol_hay_music/42", "https://live.kcm.fm/42", "להתרגש", artwork = Res.drawable.channel_kcm_42),
                Channel("kol_hay_music/46", "https://live.kcm.fm/46", "צועדים לשמחה", artwork = Res.drawable.channel_kcm_46),
                Channel("kol_hay_music/49", "https://live.kcm.fm/49", "מתחת לחופה", artwork = Res.drawable.channel_kcm_49),
                Channel("kol_hay_music/69", "https://live.kcm.fm/69", "שירים שילדים אוהבים", artwork = Res.drawable.channel_kcm_69),
                Channel("kol_hay_music/77", "https://live.kcm.fm/77", "עם הצ'ולנט בליל שישי", artwork = Res.drawable.channel_kcm_77),
                Channel("kol_hay_music/79", "https://live.kcm.fm/79", "סעודה שלישית", artwork = Res.drawable.channel_kcm_79),
                Channel("kol_hay_music/80", "https://live.kcm.fm/80", "הלב בוכה", artwork = Res.drawable.channel_kcm_80),
                Channel("kol_hay_music/107", "https://live.kcm.fm/107", "פלייליסט השמחות של פריגת", artwork = Res.drawable.channel_kcm_107),
                Channel("kol_hay_music/18", "https://live.kcm.fm/18", "בדרך לרשב\"י", artwork = Res.drawable.channel_kcm_18),
                Channel("kol_hay_music/47", "https://live.kcm.fm/47", "שעת כושר", artwork = Res.drawable.channel_kcm_47),
                Channel("kol_hay_music/113", "https://live.kcm.fm/113", "ממתק לשבת", artwork = Res.drawable.channel_kcm_113),
                Channel("kol_hay_music/116", "https://live.kcm.fm/116", "יידל ורדיגר", artwork = Res.drawable.channel_kcm_116),
                Channel("kol_hay_music/115", "https://live.kcm.fm/115", "מנדי ג'רופי", artwork = Res.drawable.channel_kcm_115),
                Channel("kol_hay_music/999", "https://live.kcm.fm/999", "אברימי רוט", artwork = Res.drawable.channel_kcm_999),
                Channel("kol_hay_music/07", "https://live.kcm.fm/07", "קרליבך", artwork = Res.drawable.channel_kcm_07),
                Channel("kol_hay_music/15", "https://live.kcm.fm/15", "אברהם פריד", artwork = Res.drawable.channel_kcm_15),
                Channel("kol_hay_music/16", "https://live.kcm.fm/16", "מרדכי בן דוד", artwork = Res.drawable.channel_kcm_16),
                Channel("kol_hay_music/21", "https://live.kcm.fm/21", "גרין", artwork = Res.drawable.channel_kcm_21),
                Channel("kol_hay_music/23", "https://live.kcm.fm/23", "חיים בנט", artwork = Res.drawable.channel_kcm_23),
                Channel("kol_hay_music/26", "https://live.kcm.fm/26", "חיים ישראל", artwork = Res.drawable.channel_kcm_26),
                Channel("kol_hay_music/29", "https://live.kcm.fm/29", "משה לאופר", artwork = Res.drawable.channel_kcm_29),
                Channel("kol_hay_music/30", "https://live.kcm.fm/30", "שוואקי", artwork = Res.drawable.channel_kcm_30),
                Channel("kol_hay_music/31", "https://live.kcm.fm/31", "מיאמי", artwork = Res.drawable.channel_kcm_31),
                Channel("kol_hay_music/32", "https://live.kcm.fm/32", "בעלז", artwork = Res.drawable.channel_kcm_32),
                Channel("kol_hay_music/33", "https://live.kcm.fm/33", "ליפא שמלצער", artwork = Res.drawable.channel_kcm_33),
                Channel("kol_hay_music/39", "https://live.kcm.fm/39", "ישי ריבו", artwork = Res.drawable.channel_kcm_39),
                Channel("kol_hay_music/51", "https://live.kcm.fm/51", "האסק", artwork = Res.drawable.channel_kcm_51),
                Channel("kol_hay_music/54", "https://live.kcm.fm/54", "ברוך לוין", artwork = Res.drawable.channel_kcm_54),
                Channel("kol_hay_music/55", "https://live.kcm.fm/55", "דדי גראוכר", artwork = Res.drawable.channel_kcm_55),
                Channel("kol_hay_music/56", "https://live.kcm.fm/56", "מיכאל שטרייכר", artwork = Res.drawable.channel_kcm_56),
                Channel("kol_hay_music/57", "https://live.kcm.fm/57", "מנדי וואלד", artwork = Res.drawable.channel_kcm_57),
                Channel("kol_hay_music/58", "https://live.kcm.fm/58", "נפתלי קמפה", artwork = Res.drawable.channel_kcm_58),
                Channel("kol_hay_music/61", "https://live.kcm.fm/61", "גד אלבז", artwork = Res.drawable.channel_kcm_61),
                Channel("kol_hay_music/62", "https://live.kcm.fm/62", "בערי וועבר", artwork = Res.drawable.channel_kcm_62),
                Channel("kol_hay_music/63", "https://live.kcm.fm/63", "בני פרידמן", artwork = Res.drawable.channel_kcm_63),
                Channel("kol_hay_music/64", "https://live.kcm.fm/64", "אוהד מושקוביץ", artwork = Res.drawable.channel_kcm_64),
                Channel("kol_hay_music/65", "https://live.kcm.fm/65", "מרדכי שפירא", artwork = Res.drawable.channel_kcm_65),
                Channel("kol_hay_music/66", "https://live.kcm.fm/66", "שלומי גרטנר", artwork = Res.drawable.channel_kcm_66),
                Channel("kol_hay_music/67", "https://live.kcm.fm/67", "שמילי אונגר", artwork = Res.drawable.channel_kcm_67),
                Channel("kol_hay_music/68", "https://live.kcm.fm/68", "מיכאל שניצלער", artwork = Res.drawable.channel_kcm_68),
                Channel("kol_hay_music/70", "https://live.kcm.fm/70", "איציק אשל", artwork = Res.drawable.channel_kcm_70),
                Channel("kol_hay_music/72", "https://live.kcm.fm/72", "שלמה כהן", artwork = Res.drawable.channel_kcm_72),
                Channel("kol_hay_music/73", "https://live.kcm.fm/73", "משה גולדמן", artwork = Res.drawable.channel_kcm_73),
                Channel("kol_hay_music/74", "https://live.kcm.fm/74", "אייבי רוטנברג", artwork = Res.drawable.channel_kcm_74),
                Channel("kol_hay_music/75", "https://live.kcm.fm/75", "אהרן רזאל", artwork = Res.drawable.channel_kcm_75),
                Channel("kol_hay_music/82", "https://live.kcm.fm/82", "רבי אלתר", artwork = Res.drawable.channel_kcm_82),
                Channel("kol_hay_music/85", "https://live.kcm.fm/85", "שאבעס שוק", artwork = Res.drawable.channel_kcm_85),
            ),
        ),
        // Stream list, titles and covers come from the player grid on yiddish24.com/streams.
        Station(
            id = "yiddish24",
            name = Res.string.station_yiddish24,
            artwork = Res.drawable.station_yiddish24,
            category = StationCategory.Music,
            channels = listOf(
                Channel("yiddish24/main", "https://music.y24.app/1", "אלגעמיינע קאלעקשאן", artwork = Res.drawable.channel_y24_main),
                Channel("yiddish24/2", "https://music.y24.app/2", "שבת קאלעקשאן", artwork = Res.drawable.channel_y24_2),
                Channel("yiddish24/3", "https://music.y24.app/3", "נגינה אן מוזיק", artwork = Res.drawable.channel_y24_3),
                Channel("yiddish24/4", "https://music.y24.app/4", "חנוכה קאלעקשאן", artwork = Res.drawable.channel_y24_4),
                Channel("yiddish24/5", "https://music.y24.app/5", "חזנות קאלעקשאן", artwork = Res.drawable.channel_y24_5),
                Channel("yiddish24/6", "https://music.y24.app/6", "מוצאי שבת קאלעקשאן", artwork = Res.drawable.channel_y24_6),
                Channel("yiddish24/7", "https://music.y24.app/7", "אידישע ניגונים קאלעקשאן", artwork = Res.drawable.channel_y24_7),
                Channel("yiddish24/8", "https://music.y24.app/8", "חתונה פרייליך קאלעקשאן", artwork = Res.drawable.channel_y24_8),
                Channel("yiddish24/9", "https://music.y24.app/9", "קומזיץ קאלעקשאן", artwork = Res.drawable.channel_y24_9),
                Channel("yiddish24/10", "https://music.y24.app/10", "חתונה צווייטע טאנץ קאלעקשאן", artwork = Res.drawable.channel_y24_10),
                Channel("yiddish24/11", "https://music.y24.app/11", "רואיגע/שטייטע מוזיק", artwork = Res.drawable.channel_y24_11),
                Channel("yiddish24/12", "https://music.y24.app/12", "פורים קאלעקשאן", artwork = Res.drawable.channel_y24_12),
                Channel("yiddish24/13", "https://music.y24.app/13", "פסח קאלעקשאן", artwork = Res.drawable.channel_y24_13),
                Channel("yiddish24/14", "https://music.y24.app/14", "ל״ג בעומר", artwork = Res.drawable.channel_y24_14),
                Channel("yiddish24/16", "https://music.y24.app/16", "ימים נוראים", artwork = Res.drawable.channel_y24_16),
                Channel("yiddish24/17", "https://music.y24.app/17", "סוכות", artwork = Res.drawable.channel_y24_17),
                Channel("yiddish24/19", "https://music.y24.app/19", "שבועות", artwork = Res.drawable.channel_y24_19),
                Channel("yiddish24/20", "https://music.y24.app/20", "מרדכי בן דוד", artwork = Res.drawable.channel_y24_20),
                Channel("yiddish24/21", "https://music.y24.app/21", "אברהם פריעד", artwork = Res.drawable.channel_y24_21),
                Channel("yiddish24/22", "https://music.y24.app/22", "ליפא שמעלצער", artwork = Res.drawable.channel_y24_22),
                Channel("yiddish24/23", "https://music.y24.app/23", "מיכאל שניצלער", artwork = Res.drawable.channel_y24_23),
                Channel("yiddish24/24", "https://music.y24.app/24", "משה גאלדמאן", artwork = Res.drawable.channel_y24_24),
                Channel("yiddish24/25", "https://music.y24.app/25", "אייזיק האניג", artwork = Res.drawable.channel_y24_25),
                Channel("yiddish24/26", "https://music.y24.app/26", "קינדער קווייער", artwork = Res.drawable.channel_y24_26),
                Channel("yiddish24/27", "https://music2.y24.app/27", "לחיים", artwork = Res.drawable.channel_y24_27),
                Channel("yiddish24/28", "https://music2.y24.app/28", "בעלזא", artwork = Res.drawable.channel_y24_28),
                Channel("yiddish24/29", "https://music2.y24.app/29", "וויזניץ", artwork = Res.drawable.channel_y24_29),
                Channel("yiddish24/30", "https://music2.y24.app/30", "סקולען", artwork = Res.drawable.channel_y24_30),
                Channel("yiddish24/31", "https://music2.y24.app/31", "באבוב", artwork = Res.drawable.channel_y24_31),
                Channel("yiddish24/32", "https://music2.y24.app/32", "בנציון שענקער", artwork = Res.drawable.channel_y24_32),
                Channel("yiddish24/33", "https://music2.y24.app/33", "מוד׳זיץ", artwork = Res.drawable.channel_y24_33),
                Channel("yiddish24/34", "https://music2.y24.app/34", "מאטי אילאוויטש", artwork = Res.drawable.channel_y24_34),
                Channel("yiddish24/35", "https://music2.y24.app/35", "סאטמאר", artwork = Res.drawable.channel_y24_35),
                Channel("yiddish24/37", "https://music2.y24.app/37", "לעבעדיג קאלעקשאן", artwork = Res.drawable.channel_y24_37),
                Channel("yiddish24/38", "https://music2.y24.app/38", "פיאנע", artwork = Res.drawable.channel_y24_38),
                Channel("yiddish24/39", "https://music2.y24.app/39", "מעדיטעשאן", artwork = Res.drawable.channel_y24_39),
                Channel("yiddish24/40", "https://music2.y24.app/40", "חב''ד", artwork = Res.drawable.channel_y24_40),
                Channel("yiddish24/41", "https://music2.y24.app/41", "אביש בראדט", artwork = Res.drawable.channel_y24_41),
                Channel("yiddish24/42", "https://music2.y24.app/42", "בערי וועבער", artwork = Res.drawable.channel_y24_42),
                Channel("yiddish24/43", "https://music2.y24.app/43", "ברוך לוין", artwork = Res.drawable.channel_y24_43),
                Channel("yiddish24/44", "https://music2.y24.app/44", "ישראל ווערדיגער", artwork = Res.drawable.channel_y24_44),
                Channel("yiddish24/45", "https://music2.y24.app/45", "שלומי גרטנר", artwork = Res.drawable.channel_y24_45),
                Channel("yiddish24/46", "https://music2.y24.app/46", "שמחה ליינר", artwork = Res.drawable.channel_y24_46),
                Channel("yiddish24/47", "https://music2.y24.app/47", "אהר'לה סאמעט", artwork = Res.drawable.channel_y24_47),
                Channel("yiddish24/48", "https://music2.y24.app/48", "דודי קאליש", artwork = Res.drawable.channel_y24_48),
                Channel("yiddish24/49", "https://music2.y24.app/49", "יהודה גרין", artwork = Res.drawable.channel_y24_49),
                Channel("yiddish24/50", "https://music2.y24.app/50", "יעקב דאסקאל", artwork = Res.drawable.channel_y24_50),
                Channel("yiddish24/51", "https://music2.y24.app/51", "מוטי שטיינמץ", artwork = Res.drawable.channel_y24_51),
                Channel("yiddish24/52", "https://music2.y24.app/52", "שלומי דאסקאל", artwork = Res.drawable.channel_y24_52),
                Channel("yiddish24/53", "https://music2.y24.app/53", "זאנוויל וויינברגר", artwork = Res.drawable.channel_y24_53),
                Channel("yiddish24/54", "https://music2.y24.app/54", "מארש קאלעקשאן", artwork = Res.drawable.channel_y24_54),
                Channel("yiddish24/55", "https://music2.y24.app/55", "טאנץ קאלעקשאן", artwork = Res.drawable.channel_y24_55),
                Channel("yiddish24/56", "https://music2.y24.app/56", "יואלי קליין", artwork = Res.drawable.channel_y24_56),
                Channel("yiddish24/57", "https://music2.y24.app/57", "לוי פאלקאוויטש", artwork = Res.drawable.channel_y24_57),
                Channel("yiddish24/58", "https://music2.y24.app/58", "שמילי אונגר", artwork = Res.drawable.channel_y24_58),
                Channel("yiddish24/1054", "https://music.y24.app/1054", "יעקב שוואקי", artwork = Res.drawable.channel_y24_1054),
                Channel("yiddish24/1055", "https://music.y24.app/1055", "לעבעדיג", artwork = Res.drawable.channel_y24_1055),
                Channel("yiddish24/1056", "https://music.y24.app/1056", "ווארעם", artwork = Res.drawable.channel_y24_1056),
                Channel("yiddish24/1057", "https://music.y24.app/1057", "מארש וואלס", artwork = Res.drawable.channel_y24_1057),
                Channel("yiddish24/1058", "https://music.y24.app/1058", "וואקאליש", artwork = Res.drawable.channel_y24_1058),
            ),
        ),
        single(
            "jewish_radio_network",
            Res.string.station_jewish_radio_network,
            Res.drawable.station_jewish_radio_network,
            StationCategory.Music,
            "https://stream.jewishradionetwork.com:8000/stream",
        ),
        single(
            "jewish_music_stream",
            Res.string.station_jewish_music_stream,
            Res.drawable.station_jewish_radio_network,
            StationCategory.Music,
            "https://stream.jewishmusicstream.com:8000/stream",
        ),
        Station(
            id = "kol_simha",
            name = Res.string.station_kol_simha,
            artwork = Res.drawable.station_kol_simha,
            category = StationCategory.Music,
            channels = listOf(
                Channel("kol_simha/main", "https://broadcast.adpronet.com/radio/8000/radio.mp3"),
                Channel("kol_simha/hits", "https://broadcast.adpronet.com/radio/8030/radio.mp3", "להיטים"),
                Channel("kol_simha/quiet", "https://broadcast.adpronet.com/radio/8020/radio.mp3", "שקטים"),
            ),
        ),
        Station(
            id = "makelet_shira",
            name = Res.string.station_makelet_shira,
            artwork = Res.drawable.station_makelet_shira,
            category = StationCategory.Music,
            channels = listOf(
                Channel("makelet_shira/main", "https://music.shira24.com:5001/10"),
                Channel("makelet_shira/freilach", "https://music.shira24.com:5001/2", "פריילך Freilach"),
                Channel("makelet_shira/relax", "https://music.shira24.com:5001/7", "רוגע Relax"),
                Channel("makelet_shira/shabbos", "https://music.shira24.com:5001/3", "שבת Shabbos"),
                Channel("makelet_shira/vocal", "https://music.shira24.com:5001/8", "ווקאלי Vocal"),
            ),
        ),
        single(
            "toker_fm",
            Res.string.station_toker_fm,
            Res.drawable.station_toker_fm,
            StationCategory.Music,
            "https://broadcast.adpronet.com/radio/6060/radio.mp3",
        ),
        single(
            "chabad_org",
            Res.string.station_chabad_org,
            Res.drawable.station_chabad_org,
            StationCategory.Music,
            "https://stream.radio.co/sdfd68a101/listen",
        ),
        single(
            "lakewood_scoop",
            Res.string.station_lakewood_scoop,
            Res.drawable.station_lakewood_scoop,
            // Named "News" by the original catalog, but the mount reports itself as
            // "Scoop Radio Music" and plays music.
            StationCategory.Music,
            // /index.html/live was a broken carry-over; the DNAS mount is plain /live.
            "http://janus.shoutca.st:8869/live",
        ),
    )

    private val byId: Map<String, Station> = all.associateBy { it.id }
    private val channelsById: Map<String, Channel> = all.flatMap { it.channels }.associateBy { it.id }

    fun of(id: String): Station? = byId[id]

    fun channel(id: String): Channel? = channelsById[id]

    fun stationOfChannel(id: String): Station? = all.firstOrNull { station -> station.channels.any { it.id == id } }

    private fun single(id: String, name: StringResource, artwork: DrawableResource, category: StationCategory, streamUrl: String) =
        Station(id, name, artwork, category, listOf(Channel("$id/main", streamUrl)))
}

/** The list the user actually sees: news channels are optional, everything else always shows. */
fun visibleStations(showNews: Boolean): List<Station> =
    if (showNews) Stations.all else Stations.all.filter { it.category != StationCategory.News }
