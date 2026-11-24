package com.example.model

object StudioFilterAndFxCatalog {

    val fxEffects: List<FxEffectSpec> = listOf(
        // Basic Effect -> Original
        FxEffectSpec("fx_none", "None", FxTab.BASIC, FxCategory.ORIGINAL, 0xFF8E8E93),

        // Basic Effect -> Basic
        FxEffectSpec("fx_spin_01", "Spin 01", FxTab.BASIC, FxCategory.BASIC, 0xFF8B6EFF),
        FxEffectSpec("fx_spin_02", "Spin 02", FxTab.BASIC, FxCategory.BASIC, 0xFF6C47FF),
        FxEffectSpec("fx_spin_03", "Spin 03", FxTab.BASIC, FxCategory.BASIC, 0xFF4ADBC8),
        FxEffectSpec("fx_spin_04", "Spin 04", FxTab.BASIC, FxCategory.BASIC, 0xFFFF5CA8),
        FxEffectSpec("fx_drop_01", "Drop 01", FxTab.BASIC, FxCategory.BASIC, 0xFFFFB25C),
        FxEffectSpec("fx_drop_02", "Drop 02", FxTab.BASIC, FxCategory.BASIC, 0xFFFF6B4A),
        FxEffectSpec("fx_drop_03", "Drop 03", FxTab.BASIC, FxCategory.BASIC, 0xFF38BDF8),
        FxEffectSpec("fx_drop_04", "Drop 04", FxTab.BASIC, FxCategory.BASIC, 0xFFFACC15),

        // Basic Effect -> Beats
        FxEffectSpec("fx_beat_pulse", "Beat Pulse", FxTab.BASIC, FxCategory.BEATS, 0xFFFF5CA8),
        FxEffectSpec("fx_bass_pump", "Bass Pump", FxTab.BASIC, FxCategory.BEATS, 0xFF8B6EFF),
        FxEffectSpec("fx_shock_drop", "Shock Drop", FxTab.BASIC, FxCategory.BEATS, 0xFF4ADBC8),
        FxEffectSpec("fx_flash_beat", "Flash Beat", FxTab.BASIC, FxCategory.BEATS, 0xFFFFD60A),

        // Basic Effect -> Glitch
        FxEffectSpec("fx_glitch_scan", "Scan Tear", FxTab.BASIC, FxCategory.GLITCH, 0xFF22D3EE),
        FxEffectSpec("fx_glitch_cyber", "Cyber Cut", FxTab.BASIC, FxCategory.GLITCH, 0xFFD946EF),
        FxEffectSpec("fx_glitch_vhs", "VHS Static", FxTab.BASIC, FxCategory.GLITCH, 0xFFFF5252),
        FxEffectSpec("fx_glitch_warp", "Warp Tear", FxTab.BASIC, FxCategory.GLITCH, 0xFF6366F1),

        // Basic Effect -> RGB
        FxEffectSpec("fx_rgb_split", "RGB Split", FxTab.BASIC, FxCategory.RGB, 0xFFFF3B30),
        FxEffectSpec("fx_rgb_prism", "Prism Shift", FxTab.BASIC, FxCategory.RGB, 0xFF06B6D4),
        FxEffectSpec("fx_rgb_chroma", "Chroma Echo", FxTab.BASIC, FxCategory.RGB, 0xFFA855F7),
        FxEffectSpec("fx_rgb_neon", "Neon Edge", FxTab.BASIC, FxCategory.RGB, 0xFF4ADE80),

        // Basic Effect -> Blur
        FxEffectSpec("fx_blur_halo", "Halo Blur", FxTab.BASIC, FxCategory.BLUR, 0xFF93C5FD),
        FxEffectSpec("fx_blur_motion", "Motion Blur", FxTab.BASIC, FxCategory.BLUR, 0xFF8B6EFF),
        FxEffectSpec("fx_blur_dream", "Dream Fog", FxTab.BASIC, FxCategory.BLUR, 0xFFF472B6),
        FxEffectSpec("fx_blur_soft", "Soft Lens", FxTab.BASIC, FxCategory.BLUR, 0xFFE2C275),

        // Basic Effect -> Rotate
        FxEffectSpec("fx_rot_orbit", "Orbit 360", FxTab.BASIC, FxCategory.ROTATE, 0xFF4ADBC8),
        FxEffectSpec("fx_rot_swing", "Pendulum", FxTab.BASIC, FxCategory.ROTATE, 0xFFF59E0B),
        FxEffectSpec("fx_rot_tilt", "Dutch Tilt", FxTab.BASIC, FxCategory.ROTATE, 0xFFEC4899),
        FxEffectSpec("fx_rot_vortex", "Vortex Roll", FxTab.BASIC, FxCategory.ROTATE, 0xFF6C47FF),

        // Basic Effect -> Retro
        FxEffectSpec("fx_retro_grain", "35mm Grain", FxTab.BASIC, FxCategory.RETRO, 0xFFE2C275),
        FxEffectSpec("fx_retro_dv", "MiniDV 99", FxTab.BASIC, FxCategory.RETRO, 0xFFFB923C),
        FxEffectSpec("fx_retro_cine", "Cinema Haze", FxTab.BASIC, FxCategory.RETRO, 0xFFB892B8),

        // Basic Effect -> Glow
        FxEffectSpec("fx_glow_bloom", "Glow Bloom", FxTab.BASIC, FxCategory.GLOW, 0xFFFFD60A),
        FxEffectSpec("fx_glow_star", "Starlight", FxTab.BASIC, FxCategory.GLOW, 0xFF38BDF8),
        FxEffectSpec("fx_glow_solar", "Solar Flare", FxTab.BASIC, FxCategory.GLOW, 0xFFFF6B4A),

        // Body Effect -> Original
        FxEffectSpec("fx_body_none", "None", FxTab.BODY, FxCategory.BODY_ORIGINAL, 0xFF8E8E93),

        // Body Effect -> Aura
        FxEffectSpec("fx_body_aura_violet", "Violet Aura", FxTab.BODY, FxCategory.BODY_AURA, 0xFF8B6EFF),
        FxEffectSpec("fx_body_aura_cyan", "Cyber Aura", FxTab.BODY, FxCategory.BODY_AURA, 0xFF4ADBC8),
        FxEffectSpec("fx_body_aura_gold", "Golden Aura", FxTab.BODY, FxCategory.BODY_AURA, 0xFFFFC857),
        FxEffectSpec("fx_body_aura_flame", "Flame Aura", FxTab.BODY, FxCategory.BODY_AURA, 0xFFFF6B4A),

        // Body Effect -> Clone
        FxEffectSpec("fx_body_clone_duo", "Ghost Duo", FxTab.BODY, FxCategory.BODY_CLONE, 0xFFA855F7),
        FxEffectSpec("fx_body_clone_trail", "Motion Clone", FxTab.BODY, FxCategory.BODY_CLONE, 0xFF38BDF8),
        FxEffectSpec("fx_body_clone_mirror", "Twin Mirror", FxTab.BODY, FxCategory.BODY_CLONE, 0xFFFF5CA8),

        // Body Effect -> Neon Outline
        FxEffectSpec("fx_body_outline_neon", "Electric Line", FxTab.BODY, FxCategory.BODY_OUTLINE, 0xFF22D3EE),
        FxEffectSpec("fx_body_outline_pink", "Neon Pink", FxTab.BODY, FxCategory.BODY_OUTLINE, 0xFFFF5CA8),
        FxEffectSpec("fx_body_outline_lime", "Acid Outline", FxTab.BODY, FxCategory.BODY_OUTLINE, 0xFF4ADE80),

        // Body Effect -> Beat Halo
        FxEffectSpec("fx_body_halo_ring", "Halo Pulse", FxTab.BODY, FxCategory.BODY_HALO, 0xFFFFD60A),
        FxEffectSpec("fx_body_halo_prism", "Prism Halo", FxTab.BODY, FxCategory.BODY_HALO, 0xFFD946EF),

        // Body Effect -> Laser
        FxEffectSpec("fx_body_laser_scan", "Laser Scan", FxTab.BODY, FxCategory.BODY_LASER, 0xFF06B6D4),
        FxEffectSpec("fx_body_laser_grid", "Grid Sweep", FxTab.BODY, FxCategory.BODY_LASER, 0xFF8B6EFF)
    )

    val filterPresets: List<StudioFilterSpec> = buildList {
        fun addCategory(
            category: FilterCategory,
            items: List<StudioFilterSpec>
        ) = addAll(items)

        addCategory(
            FilterCategory.AESTHETIC,
            listOf(
                StudioFilterSpec("natural", "Original", FilterCategory.AESTHETIC, 0xFF8C888C, 0xFF6E6B6F, FilterPreset.NATURAL),
                StudioFilterSpec("aes_velvet", "Velvet Mist", FilterCategory.AESTHETIC, 0xFFB892B8, 0xFF7D5A82, FilterPreset.VELVET_MATTE, warmthBoost = 0.04f, contrastBoost = -0.05f),
                StudioFilterSpec("aes_pearl", "Pearl Silk", FilterCategory.AESTHETIC, 0xFFE5D8CE, 0xFFC5B1A3, FilterPreset.FUJI_WARM, brightBoost = 0.08f, satBoost = -0.08f),
                StudioFilterSpec("aes_sage", "Sage Calm", FilterCategory.AESTHETIC, 0xFF8FA89B, 0xFF536B5E, FilterPreset.TOKYO_NIGHT, tintBoost = -0.08f, satBoost = -0.06f)
            )
        )
        addCategory(
            FilterCategory.CREATE,
            listOf(
                StudioFilterSpec("cr_studio", "Studio Pop", FilterCategory.CREATE, 0xFF8B6EFF, 0xFF4ADBC8, FilterPreset.CYBER_NEON, contrastBoost = 0.14f, satBoost = 0.16f),
                StudioFilterSpec("cr_clean", "Creator Clean", FilterCategory.CREATE, 0xFFD6E4FF, 0xFF6B8DD6, FilterPreset.NATURAL, contrastBoost = 0.10f, brightBoost = 0.05f),
                StudioFilterSpec("cr_matte", "Editorial Matte", FilterCategory.CREATE, 0xFF9D8189, 0xFF4A3B40, FilterPreset.VELVET_MATTE, contrastBoost = 0.06f, warmthBoost = 0.06f),
                StudioFilterSpec("cr_crisp", "HD Punch", FilterCategory.CREATE, 0xFF4ADBC8, 0xFF2563EB, FilterPreset.TEAL_ORANGE, contrastBoost = 0.18f, satBoost = 0.12f)
            )
        )
        addCategory(
            FilterCategory.VIVID,
            listOf(
                StudioFilterSpec("viv_neon", "Neon Burst", FilterCategory.VIVID, 0xFFFF5CA8, 0xFF7C3AED, FilterPreset.CYBER_NEON, satBoost = 0.28f, contrastBoost = 0.16f),
                StudioFilterSpec("viv_tropic", "Tropical", FilterCategory.VIVID, 0xFF06B6D4, 0xFFF59E0B, FilterPreset.TEAL_ORANGE, satBoost = 0.24f, warmthBoost = 0.08f),
                StudioFilterSpec("viv_citrus", "Citrus Pop", FilterCategory.VIVID, 0xFFFACC15, 0xFFFF6B4A, FilterPreset.GOLDEN_HOUR, satBoost = 0.25f, brightBoost = 0.06f),
                StudioFilterSpec("viv_ruby", "Ruby Rich", FilterCategory.VIVID, 0xFFFF3B30, 0xFF9333EA, FilterPreset.CYBER_NEON, satBoost = 0.22f, tintBoost = 0.10f)
            )
        )
        addCategory(
            FilterCategory.ESSENCE,
            listOf(
                StudioFilterSpec("ess_pure", "Pure Tone", FilterCategory.ESSENCE, 0xFFD1D5DB, 0xFF9CA3AF, FilterPreset.NATURAL, contrastBoost = 0.06f),
                StudioFilterSpec("ess_linen", "Warm Linen", FilterCategory.ESSENCE, 0xFFE2C275, 0xFFB89B5E, FilterPreset.FUJI_WARM, warmthBoost = 0.10f, brightBoost = 0.04f),
                StudioFilterSpec("ess_slate", "Minimal Slate", FilterCategory.ESSENCE, 0xFF64748B, 0xFF334155, FilterPreset.TOKYO_NIGHT, satBoost = -0.14f, contrastBoost = 0.10f),
                StudioFilterSpec("ess_ivory", "Soft Ivory", FilterCategory.ESSENCE, 0xFFF3ECE1, 0xFFD2C4B0, FilterPreset.VELVET_MATTE, brightBoost = 0.08f, warmthBoost = 0.06f)
            )
        )
        addCategory(
            FilterCategory.FOG,
            listOf(
                StudioFilterSpec("fog_morning", "Morning Fog", FilterCategory.FOG, 0xFFCBD5E1, 0xFF94A3B8, FilterPreset.VELVET_MATTE, brightBoost = 0.08f, contrastBoost = -0.14f),
                StudioFilterSpec("fog_nordic", "Nordic Mist", FilterCategory.FOG, 0xFF93C5FD, 0xFF475569, FilterPreset.TOKYO_NIGHT, contrastBoost = -0.10f, warmthBoost = -0.10f),
                StudioFilterSpec("fog_haze", "Silky Haze", FilterCategory.FOG, 0xFFE2E8F0, 0xFFA1A1AA, FilterPreset.VELVET_MATTE, satBoost = -0.16f, brightBoost = 0.06f),
                StudioFilterSpec("fog_dusk", "Blue Dusk", FilterCategory.FOG, 0xFF60A5FA, 0xFF1E3A8A, FilterPreset.TOKYO_NIGHT, warmthBoost = -0.12f, contrastBoost = -0.06f)
            )
        )
        addCategory(
            FilterCategory.LUNE,
            listOf(
                StudioFilterSpec("lune_silver", "Silver Moon", FilterCategory.LUNE, 0xFFE5E7EB, 0xFF6B7280, FilterPreset.CRISP_BW, contrastBoost = 0.14f),
                StudioFilterSpec("lune_eclipse", "Eclipse", FilterCategory.LUNE, 0xFF4B5563, 0xFF111827, FilterPreset.TOKYO_NIGHT, brightBoost = -0.08f, contrastBoost = 0.18f),
                StudioFilterSpec("lune_indigo", "Moonlight", FilterCategory.LUNE, 0xFF818CF8, 0xFF312E81, FilterPreset.TOKYO_NIGHT, warmthBoost = -0.15f, tintBoost = 0.08f),
                StudioFilterSpec("lune_frost", "Lunar Frost", FilterCategory.LUNE, 0xFFBAE6FD, 0xFF38BDF8, FilterPreset.VELVET_MATTE, warmthBoost = -0.12f, brightBoost = 0.05f)
            )
        )
        addCategory(
            FilterCategory.MOOD,
            listOf(
                StudioFilterSpec("mood_noir", "Shadow Noir", FilterCategory.MOOD, 0xFF3F3F46, 0xFF18181B, FilterPreset.CRISP_BW, contrastBoost = 0.22f, brightBoost = -0.06f),
                StudioFilterSpec("mood_indigo", "Deep Indigo", FilterCategory.MOOD, 0xFF4338CA, 0xFF1E1B4B, FilterPreset.TOKYO_NIGHT, contrastBoost = 0.16f, warmthBoost = -0.10f),
                StudioFilterSpec("mood_amber", "Dark Amber", FilterCategory.MOOD, 0xFFB45309, 0xFF451A03, FilterPreset.GOLDEN_HOUR, contrastBoost = 0.14f, warmthBoost = 0.16f),
                StudioFilterSpec("mood_plum", "Velvet Plum", FilterCategory.MOOD, 0xFF9D174D, 0xFF500724, FilterPreset.VELVET_MATTE, tintBoost = 0.12f, contrastBoost = 0.12f)
            )
        )
        addCategory(
            FilterCategory.POLAROID,
            listOf(
                StudioFilterSpec("pol_instant", "Instant 600", FilterCategory.POLAROID, 0xFFFDE68A, 0xFFD97706, FilterPreset.FUJI_WARM, warmthBoost = 0.12f, contrastBoost = -0.05f),
                StudioFilterSpec("pol_fade", "Faded Print", FilterCategory.POLAROID, 0xFFD6D3D1, 0xFFA8A29E, FilterPreset.VELVET_MATTE, brightBoost = 0.06f, satBoost = -0.12f),
                StudioFilterSpec("pol_aqua", "Cyan Print", FilterCategory.POLAROID, 0xFF67E8F9, 0xFF0E7490, FilterPreset.TOKYO_NIGHT, tintBoost = -0.08f, contrastBoost = 0.08f),
                StudioFilterSpec("pol_retro", "70s Frame", FilterCategory.POLAROID, 0xFFFDBA74, 0xFF9A3412, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.16f, satBoost = -0.06f)
            )
        )
        addCategory(
            FilterCategory.SEASON,
            listOf(
                StudioFilterSpec("sea_spring", "Spring Blossom", FilterCategory.SEASON, 0xFFF9A8D4, 0xFFEC4899, FilterPreset.FUJI_WARM, tintBoost = 0.10f, brightBoost = 0.06f),
                StudioFilterSpec("sea_summer", "Summer Sun", FilterCategory.SEASON, 0xFFFACC15, 0xFFF97316, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.16f, satBoost = 0.14f),
                StudioFilterSpec("sea_autumn", "Autumn Leaf", FilterCategory.SEASON, 0xFFEA580C, 0xFF7C2D12, FilterPreset.TEAL_ORANGE, warmthBoost = 0.18f, contrastBoost = 0.10f),
                StudioFilterSpec("sea_winter", "Winter Crisp", FilterCategory.SEASON, 0xFF7DD3FC, 0xFF0369A1, FilterPreset.TOKYO_NIGHT, warmthBoost = -0.16f, contrastBoost = 0.12f)
            )
        )
        addCategory(
            FilterCategory.FILM,
            listOf(
                StudioFilterSpec("film_kodak", "Gold 200", FilterCategory.FILM, 0xFFE2C275, 0xFFB45309, FilterPreset.FUJI_WARM, warmthBoost = 0.14f, contrastBoost = 0.10f),
                StudioFilterSpec("film_portra", "Portra 400", FilterCategory.FILM, 0xFFFDBA74, 0xFFD97706, FilterPreset.FUJI_WARM, warmthBoost = 0.08f, brightBoost = 0.05f),
                StudioFilterSpec("film_cine", "CineStill 800T", FilterCategory.FILM, 0xFF38BDF8, 0xFF1E3A8A, FilterPreset.TOKYO_NIGHT, warmthBoost = -0.14f, contrastBoost = 0.14f),
                StudioFilterSpec("film_tri", "Tri-X B&W", FilterCategory.FILM, 0xFFDADADA, 0xFF27272A, FilterPreset.CRISP_BW, contrastBoost = 0.24f)
            )
        )
        addCategory(
            FilterCategory.FOOD,
            listOf(
                StudioFilterSpec("food_fresh", "Fresh Dish", FilterCategory.FOOD, 0xFF4ADE80, 0xFF16A34A, FilterPreset.NATURAL, satBoost = 0.18f, brightBoost = 0.06f),
                StudioFilterSpec("food_sweet", "Cafe Latte", FilterCategory.FOOD, 0xFFFBBF24, 0xFF92400E, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.14f, contrastBoost = 0.08f),
                StudioFilterSpec("food_spice", "Spice Rich", FilterCategory.FOOD, 0xFFFF6B4A, 0xFFDC2626, FilterPreset.TEAL_ORANGE, satBoost = 0.20f, warmthBoost = 0.10f),
                StudioFilterSpec("food_crisp", "Bistro Clean", FilterCategory.FOOD, 0xFFFDE047, 0xFFCA8A04, FilterPreset.FUJI_WARM, contrastBoost = 0.12f, brightBoost = 0.05f)
            )
        )
        addCategory(
            FilterCategory.LIFE,
            listOf(
                StudioFilterSpec("life_daily", "Daily Vlog", FilterCategory.LIFE, 0xFF60A5FA, 0xFF3B82F6, FilterPreset.NATURAL, brightBoost = 0.05f, contrastBoost = 0.06f),
                StudioFilterSpec("life_sunny", "Weekend Sun", FilterCategory.LIFE, 0xFFFCD34D, 0xFFF59E0B, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.10f, satBoost = 0.10f),
                StudioFilterSpec("life_cozy", "Cozy Home", FilterCategory.LIFE, 0xFFFDBA74, 0xFFC2410C, FilterPreset.VELVET_MATTE, warmthBoost = 0.12f, contrastBoost = -0.04f),
                StudioFilterSpec("life_city", "Street Snap", FilterCategory.LIFE, 0xFF94A3B8, 0xFF334155, FilterPreset.TOKYO_NIGHT, contrastBoost = 0.14f)
            )
        )
        addCategory(
            FilterCategory.LIGHT,
            listOf(
                StudioFilterSpec("lgt_glow", "Sun Glow", FilterCategory.LIGHT, 0xFFFFD60A, 0xFFFF9F0A, FilterPreset.GOLDEN_HOUR, brightBoost = 0.12f, warmthBoost = 0.14f),
                StudioFilterSpec("lgt_halo", "Airy Bright", FilterCategory.LIGHT, 0xFFF8FAFC, 0xFFCBD5E1, FilterPreset.NATURAL, brightBoost = 0.16f, contrastBoost = -0.06f),
                StudioFilterSpec("lgt_prism", "Prism Light", FilterCategory.LIGHT, 0xFF4ADBC8, 0xFFFF5CA8, FilterPreset.CYBER_NEON, brightBoost = 0.08f, satBoost = 0.12f),
                StudioFilterSpec("lgt_dawn", "Soft Dawn", FilterCategory.LIGHT, 0xFFFED7AA, 0xFFFB923C, FilterPreset.FUJI_WARM, brightBoost = 0.10f, warmthBoost = 0.10f)
            )
        )
        addCategory(
            FilterCategory.PERSON,
            listOf(
                StudioFilterSpec("per_peach", "Peach Skin", FilterCategory.PERSON, 0xFFFDA4AF, 0xFFFB7185, FilterPreset.FUJI_WARM, warmthBoost = 0.08f, brightBoost = 0.05f),
                StudioFilterSpec("per_cream", "Cream Glow", FilterCategory.PERSON, 0xFFFDE68A, 0xFFF59E0B, FilterPreset.VELVET_MATTE, brightBoost = 0.08f, satBoost = -0.05f),
                StudioFilterSpec("per_studio", "Pro Portrait", FilterCategory.PERSON, 0xFFE2E8F0, 0xFF94A3B8, FilterPreset.NATURAL, contrastBoost = 0.08f, warmthBoost = 0.04f),
                StudioFilterSpec("per_rose", "Rose Velvet", FilterCategory.PERSON, 0xFFF472B6, 0xFFBE185D, FilterPreset.VELVET_MATTE, tintBoost = 0.08f, brightBoost = 0.04f)
            )
        )
        addCategory(
            FilterCategory.RETRO,
            listOf(
                StudioFilterSpec("ret_1984", "VHS 1984", FilterCategory.RETRO, 0xFFD946EF, 0xFF6366F1, FilterPreset.CYBER_NEON, tintBoost = 0.12f, contrastBoost = 0.12f),
                StudioFilterSpec("ret_sepia", "Warm Sepia", FilterCategory.RETRO, 0xFFD97706, 0xFF78350F, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.24f, satBoost = -0.22f),
                StudioFilterSpec("ret_cassette", "Cassette", FilterCategory.RETRO, 0xFFA3E635, 0xFF4D7C0F, FilterPreset.FUJI_WARM, tintBoost = -0.10f, contrastBoost = 0.10f),
                StudioFilterSpec("ret_mono", "Classic 1950", FilterCategory.RETRO, 0xFFD4D4D8, 0xFF3F3F46, FilterPreset.CRISP_BW, contrastBoost = 0.18f)
            )
        )
        addCategory(
            FilterCategory.SCENERY,
            listOf(
                StudioFilterSpec("scn_alpine", "Alpine Sky", FilterCategory.SCENERY, 0xFF38BDF8, 0xFF0284C7, FilterPreset.TOKYO_NIGHT, contrastBoost = 0.12f, satBoost = 0.14f),
                StudioFilterSpec("scn_emerald", "Forest Peak", FilterCategory.SCENERY, 0xFF10B981, 0xFF047857, FilterPreset.NATURAL, satBoost = 0.16f, tintBoost = -0.06f),
                StudioFilterSpec("scn_sunset", "Horizon Gold", FilterCategory.SCENERY, 0xFFFF9F0A, 0xFFEA580C, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.18f, satBoost = 0.12f),
                StudioFilterSpec("scn_ocean", "Deep Ocean", FilterCategory.SCENERY, 0xFF06B6D4, 0xFF1E40AF, FilterPreset.TEAL_ORANGE, warmthBoost = -0.12f, satBoost = 0.16f)
            )
        )
        addCategory(
            FilterCategory.TEAL_ORANGE,
            listOf(
                StudioFilterSpec("to_cinema", "Teal & Orange", FilterCategory.TEAL_ORANGE, 0xFFFF6B4A, 0xFF14B8A6, FilterPreset.TEAL_ORANGE, contrastBoost = 0.14f),
                StudioFilterSpec("to_block", "Blockbuster", FilterCategory.TEAL_ORANGE, 0xFFFB923C, 0xFF0EA5E9, FilterPreset.TEAL_ORANGE, contrastBoost = 0.20f, satBoost = 0.12f),
                StudioFilterSpec("to_sunset", "Miami Teal", FilterCategory.TEAL_ORANGE, 0xFF22D3EE, 0xFFF97316, FilterPreset.TEAL_ORANGE, warmthBoost = 0.08f, satBoost = 0.18f),
                StudioFilterSpec("to_night", "Neon Teal", FilterCategory.TEAL_ORANGE, 0xFF4ADBC8, 0xFFEA580C, FilterPreset.TOKYO_NIGHT, contrastBoost = 0.16f, warmthBoost = -0.06f)
            )
        )
        addCategory(
            FilterCategory.TRAVEL,
            listOf(
                StudioFilterSpec("trv_wander", "Wanderlust", FilterCategory.TRAVEL, 0xFF38BDF8, 0xFFF59E0B, FilterPreset.FUJI_WARM, satBoost = 0.14f, contrastBoost = 0.10f),
                StudioFilterSpec("trv_desert", "Desert Dunes", FilterCategory.TRAVEL, 0xFFE2C275, 0xFFB45309, FilterPreset.GOLDEN_HOUR, warmthBoost = 0.16f, contrastBoost = 0.12f),
                StudioFilterSpec("trv_island", "Island Blue", FilterCategory.TRAVEL, 0xFF22D3EE, 0xFF0284C7, FilterPreset.TEAL_ORANGE, satBoost = 0.18f, brightBoost = 0.05f),
                StudioFilterSpec("trv_tokyo", "Night Metro", FilterCategory.TRAVEL, 0xFF4ADBC8, 0xFF6366F1, FilterPreset.TOKYO_NIGHT, contrastBoost = 0.16f, warmthBoost = -0.10f)
            )
        )
    }
}
