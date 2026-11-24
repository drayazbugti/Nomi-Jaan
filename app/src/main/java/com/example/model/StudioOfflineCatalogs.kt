package com.example.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.R

enum class StickerCategory(val label: String) {
    ALL("All"),
    ISLAMIC("Mosque & Islamic"),
    HEARTS("Hearts"),
    STARS_FIRE("Stars & Fire"),
    MUSIC_SMILEYS("Crowns, Music & Smileys"),
    BADGES("Studio Badges")
}

@Immutable
data class StudioStickerSpec(
    val id: String,
    val content: String,
    val category: StickerCategory,
    val isBadge: Boolean = false
)

enum class TransitionCategory(val label: String) {
    ALL("All"),
    FADE("Fade"),
    ZOOM("Zoom"),
    SLIDE("Slide"),
    SPIN("Spin"),
    GLITCH("Glitch"),
    LIGHT("Light"),
    BLUR("Blur"),
    SPLIT("Split"),
    CINEMA("Cinema"),
    MOTION("Motion")
}

enum class TransitionMotionStyle {
    FADE_PULSE,
    ZOOM_BURST,
    SLIDE_LEFT,
    SLIDE_RIGHT,
    SLIDE_UP,
    SLIDE_DOWN,
    SPIN_TWIST,
    GLITCH_JITTER,
    FLASH_GLOW,
    PAN_SWAY
}

@Immutable
data class TransitionEffectSpec(
    val id: String,
    val name: String,
    val category: TransitionCategory,
    val durationMs: Int,
    val motionStyle: TransitionMotionStyle
)

object StudioOfflineCatalogs {

    // 100 built-in 100% offline stylish fonts across English, Urdu, Hindi, Chinese, and Multi-Language
    val studioFonts: List<StudioFontSpec> = listOf(
        // 50 Stylish English Display, Calligraphy, Serif & Modern Fonts
        StudioFontSpec("sacramento", "Sacramento", "Sacramento", R.font.sacramento, FontLanguageFilter.ENGLISH),
        StudioFontSpec("buffalo_nickel", "Buffalo Nickel", "Buffalo Nickel", R.font.rye, FontLanguageFilter.ENGLISH),
        StudioFontSpec("cinzel_bold", "Cinzel-Bold", "Cinzel Bold", R.font.cinzel, FontLanguageFilter.ENGLISH),
        StudioFontSpec("playfair_display", "Playfair Display", "Playfair", R.font.playfair_display, FontLanguageFilter.ENGLISH),
        StudioFontSpec("chopin_script", "ChopinScript", "ChopinScript", R.font.great_vibes, FontLanguageFilter.ENGLISH),
        StudioFontSpec("zoika", "Zoika", "ZOIKA", R.font.oswald, FontLanguageFilter.ENGLISH),
        StudioFontSpec("oleo_script", "OleoScript", "OleoScript", R.font.oleo_script, FontLanguageFilter.ENGLISH),
        StudioFontSpec("alloy_ink", "Alloy Ink", "Alloy Ink", R.font.rubik_bubbles, FontLanguageFilter.ENGLISH),
        StudioFontSpec("lobster_retro", "Lobster Brush", "Lobster", R.font.lobster, FontLanguageFilter.ENGLISH),
        StudioFontSpec("jetbrains_mono", "JetBrains Mono", "STUDIO_4K", R.font.jetbrains_mono, FontLanguageFilter.ENGLISH),
        StudioFontSpec("abril_fatface", "Abril Fatface", "Abril Luxe", R.font.abril_fatface, FontLanguageFilter.ENGLISH),
        StudioFontSpec("alex_brush", "Alex Brush", "Alex Brush", R.font.alex_brush, FontLanguageFilter.ENGLISH),
        StudioFontSpec("allura", "Allura Script", "Allura", R.font.allura, FontLanguageFilter.ENGLISH),
        StudioFontSpec("amatic_sc", "Amatic Tall", "AMATIC", R.font.amatic_sc, FontLanguageFilter.ENGLISH),
        StudioFontSpec("anton", "Anton Poster", "ANTON HD", R.font.anton, FontLanguageFilter.ENGLISH),
        StudioFontSpec("audiowide", "Audiowide Neon", "AUDIOWIDE", R.font.audiowide, FontLanguageFilter.ENGLISH),
        StudioFontSpec("bangers", "Bangers Comic", "BOOM!", R.font.bangers, FontLanguageFilter.ENGLISH),
        StudioFontSpec("bebas_neue", "Bebas Neue", "BEBAS REEL", R.font.bebas_neue, FontLanguageFilter.ENGLISH),
        StudioFontSpec("black_ops_one", "Black Ops Stencil", "ALPHA 01", R.font.black_ops_one, FontLanguageFilter.ENGLISH),
        StudioFontSpec("bodoni_moda", "Bodoni Vogue", "Bodoni", R.font.bodoni_moda, FontLanguageFilter.ENGLISH),
        StudioFontSpec("bungee", "Bungee Block", "BUNGEE", R.font.bungee, FontLanguageFilter.ENGLISH),
        StudioFontSpec("caveat", "Caveat Hand", "Handwritten", R.font.caveat, FontLanguageFilter.ENGLISH),
        StudioFontSpec("chewy", "Chewy Pop", "Chewy!", R.font.chewy, FontLanguageFilter.ENGLISH),
        StudioFontSpec("cookie", "Cookie Ribbon", "Cookie", R.font.cookie, FontLanguageFilter.ENGLISH),
        StudioFontSpec("cormorant_garamond", "Cormorant Serif", "Cormorant", R.font.cormorant_garamond, FontLanguageFilter.ENGLISH),
        StudioFontSpec("courgette", "Courgette Brush", "Courgette", R.font.courgette, FontLanguageFilter.ENGLISH),
        StudioFontSpec("dancing_script", "Dancing Script", "Dancing", R.font.dancing_script, FontLanguageFilter.ENGLISH),
        StudioFontSpec("dm_serif_display", "DM Serif Edit", "Editorial", R.font.dm_serif_display, FontLanguageFilter.ENGLISH),
        StudioFontSpec("fraunces", "Fraunces Retro", "Fraunces", R.font.fraunces, FontLanguageFilter.ENGLISH),
        StudioFontSpec("indie_flower", "Indie Journal", "Indie Note", R.font.indie_flower, FontLanguageFilter.ENGLISH),
        StudioFontSpec("kaushan_script", "Kaushan Brush", "Kaushan", R.font.kaushan_script, FontLanguageFilter.ENGLISH),
        StudioFontSpec("libre_baskerville", "Baskerville", "Classic", R.font.libre_baskerville, FontLanguageFilter.ENGLISH),
        StudioFontSpec("lora", "Lora Literature", "Storybook", R.font.lora, FontLanguageFilter.ENGLISH),
        StudioFontSpec("marck_script", "Marck Ink", "Marck Ink", R.font.marck_script, FontLanguageFilter.ENGLISH),
        StudioFontSpec("merriweather", "Merriweather", "Headline", R.font.merriweather, FontLanguageFilter.ENGLISH),
        StudioFontSpec("monoton", "Monoton Disco", "MONOTON", R.font.monoton, FontLanguageFilter.ENGLISH),
        StudioFontSpec("orbitron", "Orbitron Sci-Fi", "ORBITRON", R.font.orbitron, FontLanguageFilter.ENGLISH),
        StudioFontSpec("pacifico", "Pacifico Surf", "Pacifico", R.font.pacifico, FontLanguageFilter.ENGLISH),
        StudioFontSpec("permanent_marker", "Permanent Marker", "MARKER", R.font.permanent_marker, FontLanguageFilter.ENGLISH),
        StudioFontSpec("pinyon_script", "Pinyon Royal", "Pinyon", R.font.pinyon_script, FontLanguageFilter.ENGLISH),
        StudioFontSpec("press_start_2p", "Pixel Arcade", "LEVEL UP", R.font.press_start_2p, FontLanguageFilter.ENGLISH),
        StudioFontSpec("righteous", "Righteous Retro", "Righteous", R.font.righteous, FontLanguageFilter.ENGLISH),
        StudioFontSpec("satisfy", "Satisfy Sign", "Satisfy", R.font.satisfy, FontLanguageFilter.ENGLISH),
        StudioFontSpec("shadows_into_light", "Shadows Sketch", "Sketchbook", R.font.shadows_into_light, FontLanguageFilter.ENGLISH),
        StudioFontSpec("shrikhand", "Shrikhand Bold", "Shrikhand", R.font.shrikhand, FontLanguageFilter.ENGLISH),
        StudioFontSpec("silkscreen", "Silkscreen 8-Bit", "PIXEL 4K", R.font.silkscreen, FontLanguageFilter.ENGLISH),
        StudioFontSpec("special_elite", "Typewriter Elite", "CLASSIFIED", R.font.special_elite, FontLanguageFilter.ENGLISH),
        StudioFontSpec("tangerine", "Tangerine Fine", "Tangerine", R.font.tangerine, FontLanguageFilter.ENGLISH),
        StudioFontSpec("titan_one", "Titan Poster", "TITAN", R.font.titan_one, FontLanguageFilter.ENGLISH),
        StudioFontSpec("ultra", "Ultra Heavy", "ULTRA", R.font.ultra, FontLanguageFilter.ENGLISH),

        // 14 Stylish Urdu & Arabic Calligraphy Fonts
        StudioFontSpec("noto_nastaliq_urdu", "نستعلیق اردو", "بگٹی ایڈٹز اسٹوڈیو", R.font.noto_nastaliq_urdu, FontLanguageFilter.URDU),
        StudioFontSpec("gulzar_urdu", "گلزار نستعلیق", "خوبصورت اردو تحریر", R.font.gulzar, FontLanguageFilter.URDU),
        StudioFontSpec("amiri_naskh", "امیری نسخ", "فنونِ خطاطی", R.font.amiri, FontLanguageFilter.URDU),
        StudioFontSpec("aref_ruqaa", "عارف رقعہ", "رقعة اردو اسٹائل", R.font.aref_ruqaa, FontLanguageFilter.URDU),
        StudioFontSpec("reem_kufi", "ریم کوفی", "تصمیم کوفی حدیث", R.font.reem_kufi, FontLanguageFilter.URDU),
        StudioFontSpec("rakkas_display", "رقاص خطاطی", "شاہکار اردو عنوان", R.font.rakkas, FontLanguageFilter.URDU),
        StudioFontSpec("lateef_urdu", "لطیف اردو", "خوش خطی اسٹوڈیو", R.font.lateef, FontLanguageFilter.URDU),
        StudioFontSpec("scheherazade_new", "شہرزاد کلاسک", "شاعری اور ادب", R.font.scheherazade_new, FontLanguageFilter.URDU),
        StudioFontSpec("cairo_modern", "قاہرہ ماڈرن", "صاف اور جدید اردو", R.font.cairo, FontLanguageFilter.URDU),
        StudioFontSpec("tajawal_clean", "تجول کلین", "تیز اور واضح انداز", R.font.tajawal, FontLanguageFilter.URDU),
        StudioFontSpec("changa_bold", "چنگا بولڈ", "سوشل میڈیا پوسٹ", R.font.changa, FontLanguageFilter.URDU),
        StudioFontSpec("markazi_text", "مرکزی تحریر", "مضمون و کیپشن", R.font.markazi_text, FontLanguageFilter.URDU),
        StudioFontSpec("almarai_sans", "المراعی سانس", "ویڈیو سب ٹائٹل", R.font.almarai, FontLanguageFilter.URDU),
        StudioFontSpec("readex_pro_urdu", "ریڈیکس پرو", "اسمارٹ اردو ٹیکسٹ", R.font.readex_pro, FontLanguageFilter.URDU),

        // 14 Stylish Hindi & Devanagari Fonts
        StudioFontSpec("noto_sans_devanagari", "नोटो देवनागरी", "बगती एडिट्ज़ स्टूडियो", R.font.noto_sans_devanagari, FontLanguageFilter.HINDI),
        StudioFontSpec("noto_serif_devanagari", "नोटो सेरिफ़ हिंदी", "क्लासिक हिंदी शीर्षक", R.font.noto_serif_devanagari, FontLanguageFilter.HINDI),
        StudioFontSpec("kalam_hindi", "कलम हस्तलेख", "सुंदर हिंदी सुलेख", R.font.kalam, FontLanguageFilter.HINDI),
        StudioFontSpec("tiro_hindi", "तिरो साहित्य", "काव्य और कहानी", R.font.tiro_devanagari_hindi, FontLanguageFilter.HINDI),
        StudioFontSpec("rozha_one", "रोज़ा बोल्ड", "सिनेमा हिंदी पोस्टर", R.font.rozha_one, FontLanguageFilter.HINDI),
        StudioFontSpec("teko_hindi", "टेको डिस्प्ले", "स्टाइलिश रील टाइटल", R.font.teko, FontLanguageFilter.HINDI),
        StudioFontSpec("yatra_one", "यात्रा कला", "रंगमंच शीर्षक", R.font.yatra_one, FontLanguageFilter.HINDI),
        StudioFontSpec("amita_brush", "अमिता ब्रश", "शुभकामना संदेश", R.font.amita, FontLanguageFilter.HINDI),
        StudioFontSpec("arya_bold", "आर्य बोल्ड", "दमदार हिंदी फ़ॉन्ट", R.font.arya, FontLanguageFilter.HINDI),
        StudioFontSpec("gotu_soft", "गोटू सॉफ्ट", "मॉडर्न हिंदी लुक", R.font.gotu, FontLanguageFilter.HINDI),
        StudioFontSpec("eczar_display", "एक्ज़र एडिट", "आकर्षक डिज़ाइन", R.font.eczar, FontLanguageFilter.HINDI),
        StudioFontSpec("hind_clean", "हिंद सैंस", "साफ़ और स्पष्ट", R.font.hind, FontLanguageFilter.HINDI),
        StudioFontSpec("martel_serif", "मार्टेल सेरिफ़", "शाही हिंदी लेखन", R.font.martel, FontLanguageFilter.HINDI),
        StudioFontSpec("modak_pop", "मोदक पॉप", "मज़ेदार बोल्ड टेक्स्ट", R.font.modak, FontLanguageFilter.HINDI),

        // 10 Stylish Chinese Fonts (Simplified Chinese)
        StudioFontSpec("noto_sans_sc", "简体黑体 SC", "极简影像工作室", R.font.noto_sans_sc, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("noto_display_sc", "简体宋体 SC", "高清修图大师", R.font.noto_serif_sc, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("ma_shan_zheng", "马善政毛笔 SC", "龙飞凤舞书法", R.font.ma_shan_zheng, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("zcool_xiaowei", "站酷小薇体 SC", "温柔优雅标题", R.font.zcool_xiaowei, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("zcool_qingke_huangyou", "黄油体 SC", "圆润时尚海报", R.font.zcool_qingke_huangyou, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("zcool_kuaile", "站酷快乐体 SC", "快乐创意字幕", R.font.zcool_kuaile, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("long_cang", "龙藏手写 SC", "行云流水手写", R.font.long_cang, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("liu_jian_mao_cao", "流江毛草 SC", "洒脱江湖狂草", R.font.liu_jian_mao_cao, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("zhi_mang_xing", "志莽行书 SC", "大气毛笔行书", R.font.zhi_mang_xing, FontLanguageFilter.SIMPLIFIED_CHINESE),
        StudioFontSpec("noto_sc_clean", "思源极简 SC", "电影质感字幕", R.font.noto_sans_sc, FontLanguageFilter.SIMPLIFIED_CHINESE),

        // 12 Multi-Language Clean & Display Fonts (English + Global Scripts + Numerals)
        StudioFontSpec("outfit_modern", "Outfit Sans", "Studio Clean · 4K", R.font.outfit, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("poppins_global", "Poppins Pro", "Bugti Editz · ग्लोबल", R.font.poppins, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("noto_sans_global", "Noto Global", "Multi اردو हिंदी 中文", R.font.noto_sans, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("plus_jakarta", "Plus Jakarta", "Creator Studio 2026", R.font.plus_jakarta_sans, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("montserrat_pro", "Montserrat", "CINEMA STUDIO", R.font.montserrat, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("raleway_thin", "Raleway Elegant", "Fashion & Reels", R.font.raleway, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("space_grotesk", "Space Grotesk", "TECH // STUDIO", R.font.space_grotesk, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("comfortaa_round", "Comfortaa Soft", "Soft Rounded Title", R.font.comfortaa, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("fredoka_bold", "Fredoka Fun", "Playful Bold Text", R.font.fredoka, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("syne_art", "Syne Editorial", "AVANT GARDE", R.font.syne, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("unbounded_wide", "Unbounded Wide", "WIDE DISPLAY", R.font.unbounded, FontLanguageFilter.MULTI_LANGUAGE),
        StudioFontSpec("roboto_mono", "Roboto Mono", "00:19 · 4K UHD", R.font.roboto_mono, FontLanguageFilter.MULTI_LANGUAGE)
    ).also {
        require(it.size == 100) { "Expected exactly 100 multilingual fonts, found ${it.size}" }
    }

    // 100 built-in 100% offline stickers (Hearts, Mosque/Islamic, Stars, Fire, Crowns, Music, Smileys, Studio Badges)
    val studioStickers: List<StudioStickerSpec> = buildList {
        fun addEmojis(category: StickerCategory, prefix: String, items: List<String>) {
            items.forEachIndexed { i, emoji ->
                add(
                    StudioStickerSpec(
                        id = "${prefix}_$i",
                        content = emoji,
                        category = category,
                        isBadge = false
                    )
                )
            }
        }

        fun addBadges(category: StickerCategory, prefix: String, items: List<String>) {
            items.forEachIndexed { i, text ->
                add(
                    StudioStickerSpec(
                        id = "${prefix}_$i",
                        content = text,
                        category = category,
                        isBadge = true
                    )
                )
            }
        }

        // 16 Mosque & Islamic icons + Islamic phrase badges
        addEmojis(
            StickerCategory.ISLAMIC,
            "islamic_icon",
            listOf("🕌", "🕋", "☪️", "🌙", "🤲", "📿", "🏮", "🐪", "🌴", "🕊️", "📖", "🤍")
        )
        addBadges(
            StickerCategory.ISLAMIC,
            "islamic_badge",
            listOf("EID MUBARAK", "JUMMAH", "RAMADAN", "MASHALLAH")
        )

        // 18 Hearts & Love
        addEmojis(
            StickerCategory.HEARTS,
            "heart",
            listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎",
                "💖", "💗", "💓", "💞", "💘", "💝", "❣️", "💟", "🫶"
            )
        )

        // 18 Stars, Fire & Sparkles
        addEmojis(
            StickerCategory.STARS_FIRE,
            "star_fire",
            listOf(
                "⭐", "🌟", "✨", "💫", "⚡", "🔥", "💥", "☄️", "☀️",
                "🌈", "❄️", "🌸", "🌹", "🦋", "🍀", "🌊", "🔮", "🌋"
            )
        )

        // 30 Crowns, Music Notes & Smileys
        addEmojis(
            StickerCategory.MUSIC_SMILEYS,
            "crown_music_smiley",
            listOf(
                "👑", "💎", "🏆", "🥇", "🎖️", "🎵", "🎶", "🎧", "🎤", "🎸",
                "🎹", "🥁", "🎬", "📷", "🎥", "😀", "😍", "🤩", "😎", "🥳",
                "😇", "🥰", "😂", "😘", "😉", "🙌", "👏", "👍", "✌️", "🎉"
            )
        )

        // 18 Studio & Creator Badges
        addBadges(
            StickerCategory.BADGES,
            "badge",
            listOf(
                "✦ 4K UHD", "HD+", "STUDIO", "★ PRO", "⚡ BOOST", "♥ GLOW",
                "VIP", "CREATOR", "DIRECTOR", "REELS", "VIRAL", "EDITZ",
                "BUGTI", "LIVE", "REC ●", "CINEMA", "ULTRA", "TOP PICK"
            )
        )
    }.also {
        require(it.size == 100) { "Expected exactly 100 offline stickers, found ${it.size}" }
    }

    // 50 distinct vibrant offline font colors
    val textColorChoices: List<Color> = listOf(
        // Whites & Neutrals (6)
        Color(0xFFFFFFFF),
        Color(0xFFF3F4F6),
        Color(0xFFD1D5DB),
        Color(0xFF9CA3AF),
        Color(0xFF4B5563),
        Color(0xFF121112),
        // Reds & Corals (6)
        Color(0xFFFF3B30),
        Color(0xFFFF5252),
        Color(0xFFFF6B4A),
        Color(0xFFDC2626),
        Color(0xFFB91C1C),
        Color(0xFFFB7185),
        // Pinks & Magentas (6)
        Color(0xFFFF5CA8),
        Color(0xFFEC4899),
        Color(0xFFF472B6),
        Color(0xFFD946EF),
        Color(0xFFC026D3),
        Color(0xFFFFA6C9),
        // Purples & Violets (6)
        Color(0xFF8B6EFF),
        Color(0xFF6C47FF),
        Color(0xFFA855F7),
        Color(0xFF7C3AED),
        Color(0xFF6366F1),
        Color(0xFFC4B5FD),
        // Blues & Cyans (8)
        Color(0xFF3B82F6),
        Color(0xFF2563EB),
        Color(0xFF1D4ED8),
        Color(0xFF38BDF8),
        Color(0xFF06B6D4),
        Color(0xFF22D3EE),
        Color(0xFF4ADBC8),
        Color(0xFF93C5FD),
        // Greens & Emeralds (6)
        Color(0xFF10B981),
        Color(0xFF22C55E),
        Color(0xFF4ADE80),
        Color(0xFF16A34A),
        Color(0xFF84CC16),
        Color(0xFFA3E635),
        // Yellows, Golds & Oranges (8)
        Color(0xFFFFD60A),
        Color(0xFFFACC15),
        Color(0xFFE2C275),
        Color(0xFFF59E0B),
        Color(0xFFFF9F0A),
        Color(0xFFFB923C),
        Color(0xFFEA580C),
        Color(0xFFFDE68A),
        // Luxe Earth & Metallic Tints (4)
        Color(0xFFD97706),
        Color(0xFFA16207),
        Color(0xFF14B8A6),
        Color(0xFF0EA5E9)
    ).also {
        require(it.size == 50) { "Expected exactly 50 font colors, found ${it.size}" }
    }

    // 100 built-in 100% offline Transition Effects (10 categories x 10 transitions each)
    val transitionEffects: List<TransitionEffectSpec> = buildList {
        val defs: List<Triple<TransitionCategory, TransitionMotionStyle, List<String>>> = listOf(
            Triple(
                TransitionCategory.FADE,
                TransitionMotionStyle.FADE_PULSE,
                listOf(
                    "Smooth Dissolve", "Mix Fade", "Black Fade", "White Flash", "Cross Dissolve",
                    "Soft Veil", "Silk Fade", "Midnight Dip", "Dawn Mist", "Velvet Cross"
                )
            ),
            Triple(
                TransitionCategory.ZOOM,
                TransitionMotionStyle.ZOOM_BURST,
                listOf(
                    "Snap Zoom In", "Hyper Zoom Out", "Lens Punch", "Warp Tunnel", "Dolly Rush",
                    "Elastic Pop", "Macro Zoom", "Micro Bounce", "Telescope Pull", "Beat Zoom"
                )
            ),
            Triple(
                TransitionCategory.SLIDE,
                TransitionMotionStyle.SLIDE_LEFT,
                listOf(
                    "Whip Left", "Whip Right", "Push Up", "Push Down", "Diagonal Slide",
                    "Smooth Glide", "Shutter Slide", "Curtain Swipe", "Fast Pan", "Reel Toss"
                )
            ),
            Triple(
                TransitionCategory.SPIN,
                TransitionMotionStyle.SPIN_TWIST,
                listOf(
                    "Vortex 360", "Tilt Roll", "Pendulum Swing", "Spin Blur", "Whirlwind CW",
                    "Whirlwind CCW", "Orbit Twist", "Barrel Flip", "Spiral Drop", "Gyro Turn"
                )
            ),
            Triple(
                TransitionCategory.GLITCH,
                TransitionMotionStyle.GLITCH_JITTER,
                listOf(
                    "RGB Glitch", "CRT Static", "Scanline Tear", "Cyber Jitter", "Signal Drop",
                    "Pixel Shake", "Chroma Split", "Arcade Warp", "VHS Tracker", "Holo Flash"
                )
            ),
            Triple(
                TransitionCategory.LIGHT,
                TransitionMotionStyle.FLASH_GLOW,
                listOf(
                    "Solar Flare", "Prism Beam", "Gold Strobe", "Lens Burn", "Halo Radiance",
                    "Neon Glow", "Warm Leak", "Crystal Shine", "Flashbulb", "Aurora Bloom"
                )
            ),
            Triple(
                TransitionCategory.BLUR,
                TransitionMotionStyle.FADE_PULSE,
                listOf(
                    "Gaussian Wipe", "Radial Blur", "Dream Haze", "Focus Shift", "Whip Blur",
                    "Silk Bokeh", "Speed Smear", "Mist Cross", "Cloud Dissolve", "Soft Pull"
                )
            ),
            Triple(
                TransitionCategory.SPLIT,
                TransitionMotionStyle.SLIDE_UP,
                listOf(
                    "Center Open", "Vertical Split", "Horizontal Slice", "Diamond Cut", "Iris Gate",
                    "Mirror Fold", "Twin Doors", "Tile Flip", "Grid Cascade", "Panel Reveal"
                )
            ),
            Triple(
                TransitionCategory.CINEMA,
                TransitionMotionStyle.SLIDE_RIGHT,
                listOf(
                    "2.39 Letterbox", "Film Burn 35mm", "Reel Splice", "Gate Weave", "Projector Cue",
                    "Noir Fade", "Anamorphic Sweep", "Dramatic Cut", "Trailer Slam", "Director Roll"
                )
            ),
            Triple(
                TransitionCategory.MOTION,
                TransitionMotionStyle.PAN_SWAY,
                listOf(
                    "Handheld Shake", "Heartbeat Pulse", "Drift Sway", "Ripple Wave", "Elastic Snap",
                    "Float Rise", "Drop Impact", "Breeze Wave", "Shockwave", "Bass Shake"
                )
            )
        )

        defs.forEach { (category, defaultStyle, names) ->
            names.forEachIndexed { idx, name ->
                val style = when {
                    name.contains("Right", ignoreCase = true) -> TransitionMotionStyle.SLIDE_RIGHT
                    name.contains("Up", ignoreCase = true) || name.contains("Rise", ignoreCase = true) -> TransitionMotionStyle.SLIDE_UP
                    name.contains("Down", ignoreCase = true) || name.contains("Drop", ignoreCase = true) -> TransitionMotionStyle.SLIDE_DOWN
                    else -> defaultStyle
                }
                add(
                    TransitionEffectSpec(
                        id = "tr_${category.name.lowercase()}_${idx + 1}",
                        name = name,
                        category = category,
                        durationMs = 450 + (idx % 5) * 150,
                        motionStyle = style
                    )
                )
            }
        }
    }.also {
        require(it.size == 100) { "Expected exactly 100 transition effects, found ${it.size}" }
    }

    // 100 ready-to-use 100% offline Short Templates
    val templates: List<StudioTemplate> = buildList {
        // Keep exact 4 original core template IDs first
        add(
            StudioTemplate(
                id = "golden_skyline",
                title = "Golden Skyline 4K",
                subtitle = "Crisp architectural Ultra HD + warm sunlight lift",
                tag = "Hot",
                category = "HDR",
                durationSec = 12,
                previewRes = R.drawable.img_demo_skyline,
                editState = StudioEditState(
                    enhanceMode = EnhanceMode.ULTRA_HD,
                    enhanceStrength = 0.85f,
                    filterPreset = FilterPreset.GOLDEN_HOUR,
                    filterIntensity = 0.65f,
                    contrast = 0.12f,
                    sharpen = 0.45f,
                    transitionEffectId = "tr_light_1"
                )
            )
        )
        add(
            StudioTemplate(
                id = "portrait_studio",
                title = "Velvet Portrait Glow",
                subtitle = "Face-locked clarity with soft skin & golden warmth",
                tag = "PRO",
                category = "Portrait",
                durationSec = 10,
                previewRes = R.drawable.img_demo_portrait,
                editState = StudioEditState(
                    enhanceMode = EnhanceMode.PORTRAIT,
                    enhanceStrength = 0.78f,
                    skinSmooth = 0.48f,
                    faceGlow = 0.35f,
                    warmth = 0.12f,
                    vignette = 0.18f,
                    transitionEffectId = "tr_fade_1"
                )
            )
        )
        add(
            StudioTemplate(
                id = "neon_rain",
                title = "Tokyo Night UHD",
                subtitle = "Night denoise + neon cyan shadows & cinema bars",
                tag = "New",
                category = "Neon",
                durationSec = 15,
                previewRes = R.drawable.img_showcase_cinema,
                editState = StudioEditState(
                    enhanceMode = EnhanceMode.NIGHT_DENOISE,
                    enhanceStrength = 0.88f,
                    filterPreset = FilterPreset.TOKYO_NIGHT,
                    filterIntensity = 0.80f,
                    contrast = 0.18f,
                    frameStyle = FrameStyle.CINEMA_BARS,
                    transitionEffectId = "tr_glitch_1"
                )
            )
        )
        add(
            StudioTemplate(
                id = "desert_film",
                title = "Desert Teal & Amber",
                subtitle = "Analog 35mm cinema grade with subtle film grain",
                tag = "Trending",
                category = "Film",
                durationSec = 18,
                previewRes = R.drawable.img_template_film,
                editState = StudioEditState(
                    enhanceMode = EnhanceMode.AI_UHD,
                    enhanceStrength = 0.75f,
                    filterPreset = FilterPreset.TEAL_ORANGE,
                    filterIntensity = 0.78f,
                    effectMode = EffectMode.FILM_GRAIN,
                    effectIntensity = 0.35f,
                    aspectRatio = AspectRatioOption.CINEMA_16_9,
                    transitionEffectId = "tr_cinema_2"
                )
            )
        )

        val categories = listOf(
            "Short Reels", "Cinematic", "Portrait", "Festive & Islamic", "Neon", "Film", "HDR"
        )
        val previews = listOf(
            R.drawable.img_demo_skyline,
            R.drawable.img_demo_portrait,
            R.drawable.img_showcase_cinema,
            R.drawable.img_template_film
        )
        val titlePrefixes = listOf(
            "Velocity", "Velvet", "Eid Noor", "Jummah Light", "Cyber Pulse", "35mm Kodak",
            "Aura", "Golden Sunset", "Silk Dream", "Midnight Rush", "Royal Crown", "Crystal 4K",
            "Pastel Bloom", "Teal Storm", "Warm Honey", "Vintage Reel"
        )
        val titleSuffixes = listOf(
            "Cut", "Mix", "Vibe", "Flash", "Story", "Drop"
        )
        val enhancePool = EnhanceMode.entries.filter { it != EnhanceMode.NONE }
        val filterPool = FilterPreset.entries
        val effectPool = EffectMode.entries
        val framePool = FrameStyle.entries
        val ratioPool = listOf(
            AspectRatioOption.REEL_9_16,
            AspectRatioOption.CINEMA_16_9,
            AspectRatioOption.SQUARE_1_1,
            AspectRatioOption.PORTRAIT_4_5,
            AspectRatioOption.ORIGINAL
        )

        // Generate 96 more distinct offline templates => total 100
        for (idx in 0 until 96) {
            val cat = categories[idx % categories.size]
            val prefix = titlePrefixes[(idx * 3) % titlePrefixes.size]
            val suffix = titleSuffixes[(idx / titlePrefixes.size) % titleSuffixes.size]
            val num = idx + 5
            val preview = when (cat) {
                "Portrait" -> R.drawable.img_demo_portrait
                "Neon" -> R.drawable.img_showcase_cinema
                "Film" -> R.drawable.img_template_film
                else -> previews[idx % previews.size]
            }
            val filter = when (cat) {
                "Neon" -> if (idx % 2 == 0) FilterPreset.CYBER_NEON else FilterPreset.TOKYO_NIGHT
                "Film" -> if (idx % 2 == 0) FilterPreset.FUJI_WARM else FilterPreset.TEAL_ORANGE
                "Festive & Islamic" -> FilterPreset.GOLDEN_HOUR
                "Portrait" -> if (idx % 2 == 0) FilterPreset.VELVET_MATTE else FilterPreset.FUJI_WARM
                else -> filterPool[idx % filterPool.size]
            }
            val ratio = when (cat) {
                "Short Reels" -> AspectRatioOption.REEL_9_16
                "Cinematic" -> AspectRatioOption.CINEMA_16_9
                "Portrait" -> AspectRatioOption.PORTRAIT_4_5
                else -> ratioPool[idx % ratioPool.size]
            }
            val enhance = enhancePool[idx % enhancePool.size]
            val effect = effectPool[idx % effectPool.size]
            val frame = if (cat == "Cinematic") FrameStyle.CINEMA_BARS else framePool[idx % framePool.size]
            val transition = transitionEffects[(idx * 7) % transitionEffects.size].id
            val durationSec = 7 + (idx % 14)

            val displayTitle = if (cat == "Festive & Islamic" && idx % 2 == 0) {
                "Noor ${prefix.replace("Eid Noor", "Ramadan")} #$num"
            } else {
                "$prefix $suffix #$num"
            }

            val tag = when (idx % 4) {
                0 -> "9:16"
                1 -> "4K"
                2 -> "Offline"
                else -> "${durationSec}s"
            }

            add(
                StudioTemplate(
                    id = "tpl_offline_${idx + 5}",
                    title = displayTitle,
                    subtitle = "$cat · ${filter.label} · ${enhance.title}",
                    tag = tag,
                    category = cat,
                    durationSec = durationSec,
                    previewRes = preview,
                    editState = StudioEditState(
                        enhanceMode = enhance,
                        enhanceStrength = 0.65f + (idx % 4) * 0.08f,
                        filterPreset = filter,
                        filterIntensity = 0.55f + (idx % 4) * 0.1f,
                        brightness = ((idx % 5) - 2) * 0.03f,
                        contrast = ((idx % 4) * 0.05f),
                        saturation = ((idx % 5) - 1) * 0.04f,
                        sharpen = 0.25f + (idx % 3) * 0.15f,
                        warmth = if (cat == "Festive & Islamic" || cat == "Portrait") 0.10f else 0f,
                        vignette = if (cat == "Cinematic" || cat == "Film") 0.22f else 0.08f,
                        skinSmooth = if (cat == "Portrait") 0.42f else 0f,
                        faceGlow = if (cat == "Portrait") 0.28f else 0f,
                        effectMode = effect,
                        effectIntensity = if (effect == EffectMode.NONE) 0f else 0.42f,
                        aspectRatio = ratio,
                        frameStyle = frame,
                        transitionEffectId = transition,
                        clipAnimation = ClipTransitionAnimation.entries[(idx % 4) + 1]
                    )
                )
            )
        }
    }.also {
        require(it.size == 100) { "Expected exactly 100 offline templates, found ${it.size}" }
    }
}
