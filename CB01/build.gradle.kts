// use an integer for version numbers
version = 24


cloudstream {
    // All of these properties are optional, you can safely remove them

    description = "Film e SerieTV da CB01"
    authors = listOf("doGior","DieGon","SirFill")

    /**
    * Status int as the following:
    * 0: Down
    * 1: Ok
    * 2: Slow
    * 3: Beta only
    * */
    status = 0

    tvTypes = listOf("Movie", "TvSeries", "Cartoon")

    requiresResources = false
    language = "it"

    iconUrl = "https://raw.githubusercontent.com/SirFill/SIRFILL/master/CB01/cb01_icon.png"
}
dependencies{
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
}
