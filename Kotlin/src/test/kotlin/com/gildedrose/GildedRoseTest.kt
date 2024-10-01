package com.gildedrose

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class GildedRoseTest {

    @ParameterizedTest
    @CsvSource(
        "Standard item,-1", "'Sulfuras, Hand of Ragnaros',0"
    )
    fun sellInDecreasesAsExpected(name: String, expectedDiff: Int) {
        val sellIn = 10
        val items = listOf(Item(name, sellIn, 10))
        val app = GildedRose(items)
        app.updateQuality()
        assertEquals(items.first().sellIn, sellIn + expectedDiff)
    }

    @ParameterizedTest
    @CsvSource(
        // Default case
        "Standard item,10,-1",
        // Default case after sell date
        "Standard item,0,-2",
        // Legendary items do no change
        "'Sulfuras, Hand of Ragnaros',10,0",
        // This increases in quality
        "Aged Brie,10,1",
        // Apparently twice as fast after sell date
        "Aged Brie,0,2",
        // Passes increase by one if more than 10 days
        "Backstage passes to a TAFKAL80ETC concert,15,1",
        // Passes increase by two if <= 10 days
        "Backstage passes to a TAFKAL80ETC concert,10,2",
        "Backstage passes to a TAFKAL80ETC concert,9,2",
        // Passes increase by three if <= 5 days
        "Backstage passes to a TAFKAL80ETC concert,5,3",
        "Backstage passes to a TAFKAL80ETC concert,4,3",
        // They are worthless after the concert
        "Backstage passes to a TAFKAL80ETC concert,-1,-20",
        "Backstage passes to a TAFKAL80ETC concert,-78,-20",
    )
    fun qualityDecreasesAsExpected(name: String, sellIn: Int, expectedDiff: Int) {
        val quality = 20
        val items = listOf(Item(name, sellIn, quality))
        val app = GildedRose(items)
        app.updateQuality()
        assertEquals(quality + expectedDiff, items.first().quality)
    }

    @Test
    fun qualityCannotGoBelowZero() {
        val items = listOf(Item("Item", 0, 1))
        val app = GildedRose(items)
        app.updateQuality()
        assertEquals(0, items.first().quality)
        app.updateQuality()
        assertEquals(0, items.first().quality)
    }

    @Test
    fun qualityCannotGoAboveFifty() {
        val items = listOf(Item("Backstage passes to a TAFKAL80ETC concert", 1, 50))
        val app = GildedRose(items)
        app.updateQuality()
        assertEquals(50, items.first().quality)
    }
}
