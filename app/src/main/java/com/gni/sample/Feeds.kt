package com.gni.sample

import com.gni.mediology.api.ArticleKeys

/** A home-screen section backed by one category feed URL. */
data class Section(
    val name: String,
    val categoryId: String,
    val apiUrl: String
)

object Feeds {

    const val WIDGET_URL = "https://orange-bessy-31.tiiny.site/cat1.json"

    val SECTIONS = listOf(
        Section(
            name = "Top News",
            categoryId = "cat1",
            apiUrl = "https://orange-bessy-31.tiiny.site/cat1.json"
        ),
        Section(
            name = "Sports",
            categoryId = "cat2",
            apiUrl = "https://orange-bessy-31.tiiny.site/cat2.json"
        )
    )

    /** Maps the JSON field names of these feeds onto the roles the SDK understands. */
    val KEYS = ArticleKeys(
        idKey = "id",
        titleKey = "t",
        imageKey = "image",
        summaryKey = "intro"
    )

    /**
     * The personalised feed hands back a categoryId from read history and expects the host to
     * resolve it to a feed URL.
     */
    fun urlFor(categoryId: String): String =
        SECTIONS.firstOrNull { it.categoryId == categoryId }?.apiUrl ?: SECTIONS.first().apiUrl
}
