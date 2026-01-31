// use an integer for version numbers
version = 12


cloudstream {
    language = "it"
    // All of these properties are optional, you can safely remove them

     description = "Live streams da CalcioStreaming"
    authors = listOf("doGior","DieGon","SirFill ")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "Live",
    )

    iconUrl = "https://raw.githubusercontent.com/SirFill/SIRFILL/master/CalcioStreaming/calcio_icon.png"
}
