package com.davoe.karootopomaps

import androidx.annotation.StringRes

enum class ElementCategory(
    val titleRes: Int,
    val iconRes: Int
) {
    BASE_TERRAIN(R.string.cat_base_terrain, R.drawable.ic_gis_terrain),
    WATER(R.string.cat_water, R.drawable.ic_gis_water),
    FOREST_NATURE(R.string.cat_forest_nature, R.drawable.ic_gis_forest),
    URBAN_BUILDINGS(R.string.cat_urban_buildings, R.drawable.ic_gis_urban),
    CONTOURS(R.string.cat_contours, R.drawable.ic_gis_contours),
    ROADS_PATHS(R.string.cat_roads_paths, R.drawable.ic_gis_roads),
    POIS_LABELS(R.string.cat_pois_labels, R.drawable.ic_gis_poi)
}

data class CustomizableElement(
    val key: String,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val category: ElementCategory,
    val defaultColorHex: String,
    val defaultEnabled: Boolean = true,
    val supportsVisibilityToggle: Boolean = true
)

object CustomThemeSchema {

    val ELEMENTS = listOf(
        // 🏞️ BASE TERRAIN
        CustomizableElement(
            key = "color_bg",
            nameRes = R.string.elem_bg_name,
            descRes = R.string.elem_bg_desc,
            category = ElementCategory.BASE_TERRAIN,
            defaultColorHex = "#F5EFD7"
        ),
        CustomizableElement(
            key = "color_meadow",
            nameRes = R.string.elem_meadow_name,
            descRes = R.string.elem_meadow_desc,
            category = ElementCategory.BASE_TERRAIN,
            defaultColorHex = "#D7E4B4"
        ),
        CustomizableElement(
            key = "color_rock",
            nameRes = R.string.elem_rock_name,
            descRes = R.string.elem_rock_desc,
            category = ElementCategory.BASE_TERRAIN,
            defaultColorHex = "#D8D4CC"
        ),
        CustomizableElement(
            key = "color_sand",
            nameRes = R.string.elem_sand_name,
            descRes = R.string.elem_sand_desc,
            category = ElementCategory.BASE_TERRAIN,
            defaultColorHex = "#E9DFA1"
        ),

        // 💧 WATER
        CustomizableElement(
            key = "color_water_body",
            nameRes = R.string.elem_water_body_name,
            descRes = R.string.elem_water_body_desc,
            category = ElementCategory.WATER,
            defaultColorHex = "#B3E3F9"
        ),
        CustomizableElement(
            key = "color_river",
            nameRes = R.string.elem_river_name,
            descRes = R.string.elem_river_desc,
            category = ElementCategory.WATER,
            defaultColorHex = "#429AD2"
        ),

        // 🌲 FOREST & NATURE
        CustomizableElement(
            key = "color_forest",
            nameRes = R.string.elem_forest_name,
            descRes = R.string.elem_forest_desc,
            category = ElementCategory.FOREST_NATURE,
            defaultColorHex = "#A3C8A7"
        ),
        CustomizableElement(
            key = "color_park",
            nameRes = R.string.elem_park_name,
            descRes = R.string.elem_park_desc,
            category = ElementCategory.FOREST_NATURE,
            defaultColorHex = "#88C773"
        ),

        // 🏙️ URBAN & BUILDINGS
        CustomizableElement(
            key = "color_residential",
            nameRes = R.string.elem_residential_name,
            descRes = R.string.elem_residential_desc,
            category = ElementCategory.URBAN_BUILDINGS,
            defaultColorHex = "#F48D84"
        ),
        CustomizableElement(
            key = "color_building",
            nameRes = R.string.elem_building_name,
            descRes = R.string.elem_building_desc,
            category = ElementCategory.URBAN_BUILDINGS,
            defaultColorHex = "#F48D84"
        ),
        CustomizableElement(
            key = "color_industrial",
            nameRes = R.string.elem_industrial_name,
            descRes = R.string.elem_industrial_desc,
            category = ElementCategory.URBAN_BUILDINGS,
            defaultColorHex = "#E7D0E2"
        ),

        // 📈 CONTOURS & RELIEF
        CustomizableElement(
            key = "color_contour_major",
            nameRes = R.string.elem_contour_major_name,
            descRes = R.string.elem_contour_major_desc,
            category = ElementCategory.CONTOURS,
            defaultColorHex = "#855F3C"
        ),
        CustomizableElement(
            key = "color_contour_minor",
            nameRes = R.string.elem_contour_minor_name,
            descRes = R.string.elem_contour_minor_desc,
            category = ElementCategory.CONTOURS,
            defaultColorHex = "#9E7E5A"
        ),
        CustomizableElement(
            key = "color_contour_label",
            nameRes = R.string.elem_contour_label_name,
            descRes = R.string.elem_contour_label_desc,
            category = ElementCategory.CONTOURS,
            defaultColorHex = "#5C442A"
        ),

        // 🛣️ ROADS, GRAVEL & TRAILS
        CustomizableElement(
            key = "color_motorway",
            nameRes = R.string.elem_motorway_name,
            descRes = R.string.elem_motorway_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#E05A47"
        ),
        CustomizableElement(
            key = "color_primary_road",
            nameRes = R.string.elem_primary_road_name,
            descRes = R.string.elem_primary_road_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#E5933A"
        ),
        CustomizableElement(
            key = "color_secondary_road",
            nameRes = R.string.elem_secondary_road_name,
            descRes = R.string.elem_secondary_road_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#F5D061"
        ),
        CustomizableElement(
            key = "color_residential_street",
            nameRes = R.string.elem_residential_street_name,
            descRes = R.string.elem_residential_street_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#FFFFFF"
        ),
        CustomizableElement(
            key = "color_gravel_track",
            nameRes = R.string.elem_gravel_track_name,
            descRes = R.string.elem_gravel_track_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#8B5230"
        ),
        CustomizableElement(
            key = "color_mtb_singletrack",
            nameRes = R.string.elem_mtb_singletrack_name,
            descRes = R.string.elem_mtb_singletrack_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#A020F0"
        ),
        CustomizableElement(
            key = "color_cycleway",
            nameRes = R.string.elem_cycleway_name,
            descRes = R.string.elem_cycleway_desc,
            category = ElementCategory.ROADS_PATHS,
            defaultColorHex = "#327A04"
        ),

        // 📍 POIS & LABELS
        CustomizableElement(
            key = "color_water_fountain",
            nameRes = R.string.elem_water_fountain_name,
            descRes = R.string.elem_water_fountain_desc,
            category = ElementCategory.POIS_LABELS,
            defaultColorHex = "#0088FF"
        ),
        CustomizableElement(
            key = "color_peak_mountain",
            nameRes = R.string.elem_peak_mountain_name,
            descRes = R.string.elem_peak_mountain_desc,
            category = ElementCategory.POIS_LABELS,
            defaultColorHex = "#8B4513"
        ),
        CustomizableElement(
            key = "color_cafe_food",
            nameRes = R.string.elem_cafe_food_name,
            descRes = R.string.elem_cafe_food_desc,
            category = ElementCategory.POIS_LABELS,
            defaultColorHex = "#D35400"
        ),
        CustomizableElement(
            key = "color_bike_shop",
            nameRes = R.string.elem_bike_shop_name,
            descRes = R.string.elem_bike_shop_desc,
            category = ElementCategory.POIS_LABELS,
            defaultColorHex = "#27AE60"
        ),
        CustomizableElement(
            key = "color_place_label",
            nameRes = R.string.elem_place_label_name,
            descRes = R.string.elem_place_label_desc,
            category = ElementCategory.POIS_LABELS,
            defaultColorHex = "#222222"
        )
    )
}
