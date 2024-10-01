package com.gildedrose

import kotlin.math.max
import kotlin.math.min

/** The default maximum quality */
const val MAX_QUALITY = 50

/** The default minimum quality */
const val MIN_QUALITY = 0

fun interface SellInCalculator {
    /**
     * Calculate the new number of days to sell the given item in.
     *
     * @param item The item to calculate it for. Do not modify the item.
     * @return The new number of days to sell the item.
     */
    fun newSellInDays(item: Item): Int
}

/** Default calculator for the new number of days to sell in. */
val defaultSellInCalculator = SellInCalculator { it.sellIn - 1 }

/** Calculator for legendary items */
val legendarySellInCalculator = SellInCalculator { it.sellIn }

fun interface QualityCalculator {
    /**
     * Calculate the new quality of an item.
     *
     * @param quality The current quality of the item.
     * @param sellIn The new, already updated sell date.
     * @return The new quality for the item.
     */
    fun newQuality(quality: Int, sellIn: Int): Int
}

/** Default calculator for the new quality. */
val defaultQualityCalculator = QualityCalculator { quality, sellIn ->
    // Subtract 1 if before sell date, otherwise 2.
    val newQuality = quality - (if (sellIn >= 0) 1 else 2)
    // Do not go under zero.
    max(MIN_QUALITY, newQuality)
}

/** Calculator for legendary items that do not deteriorate. */
val legendaryQualityCalculator = QualityCalculator { quality, _ -> quality }

/** Calculator for brie */
val agedQualityCalculator = QualityCalculator { quality, sellIn ->
    // Add 1 if before sell date, otherwise 2.
    val newQuality = quality + (if (sellIn >= 0) 1 else 2)
    // Do not go over 50.
    min(MAX_QUALITY, newQuality)
}

val backstageQualityCalculator = QualityCalculator { quality, sellIn ->
    val newQuality = when {
        // More than 10 days in advance
        sellIn >= 10 -> quality + 1
        // 10-5 days in advance
        sellIn >= 5 -> quality + 2
        // Less than 5 days in advance
        sellIn >= 0 -> quality + 3
        // Convert has passed
        else -> 0
    }

    // Do not go over 50.
    min(MAX_QUALITY, newQuality)
}

val conjuredQualityCalculator = QualityCalculator { quality, sellIn ->
    // Apply the normal calculate twice, since these degrade twice as fast.
    defaultQualityCalculator.newQuality(quality, sellIn).let {
        defaultQualityCalculator.newQuality(it, sellIn)
    }
}

// Normally these should be part of the Item class, but we cannot modify these
// at this time, so use a map instead.
// Hard-code the names for now.
val sellInMapping = mapOf(
    "Sulfuras, Hand of Ragnaros" to legendarySellInCalculator
).withDefault { defaultSellInCalculator }

val qualityMapping = mapOf(
    "Sulfuras, Hand of Ragnaros" to legendaryQualityCalculator,
    "Aged Brie" to agedQualityCalculator,
    "Backstage passes to a TAFKAL80ETC concert" to backstageQualityCalculator,
    "Conjured Mana Cake" to conjuredQualityCalculator
).withDefault { defaultQualityCalculator }


class GildedRose(private var items: List<Item>) {
    fun updateQuality() {

        for (item in items) {
            val sellCalculator = sellInMapping.getValue(item.name)
            val qualityCalculator = qualityMapping.getValue(item.name)

            // First, adjust the sell date.
            item.sellIn = sellCalculator.newSellInDays(item)
            // Next, adjust the quality.
            item.quality = qualityCalculator.newQuality(item.quality, item.sellIn)
        }
    }
}
