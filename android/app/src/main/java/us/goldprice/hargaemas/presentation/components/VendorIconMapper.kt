package us.goldprice.hargaemas.presentation.components

import us.goldprice.hargaemas.R

/**
 * Maps vendor unit names from the JSON to their corresponding drawable resource IDs.
 * Returns null if no icon is found for the vendor.
 */
fun getVendorIconRes(vendorUnit: String): Int? {
    val normalized = vendorUnit
        .replace(Regex("(?i)gram\\s*-\\s*"), "")
        .trim()
        .uppercase()

    return when {
        normalized == "ANTAM" -> R.drawable.ic_vendor_antam
        normalized == "ANTAM MULIA RETRO" -> R.drawable.ic_vendor_antam_mulia_retro
        normalized == "ANTAM NON PEGADAIAN" -> R.drawable.ic_vendor_antam_non_pegadaian
        normalized == "BABY GALERI 24" -> R.drawable.ic_vendor_baby_galeri_24
        normalized == "BABY SERIES INVESTASI" -> R.drawable.ic_vendor_baby_series_investasi
        normalized == "BABY SERIES TUMBUHAN" -> R.drawable.ic_vendor_baby_series_tumbuhan
        normalized == "BATIK SERIES" -> R.drawable.ic_vendor_batik_series
        normalized == "DINAR G24" -> R.drawable.ic_vendor_dinar_g24
        normalized == "GALERI 24" -> R.drawable.ic_vendor_galeri_24
        normalized == "LOTUS ARCHI" -> R.drawable.ic_vendor_lotus_archi
        normalized == "LOTUS ARCHI GIFT" -> R.drawable.ic_vendor_lotus_archi_gift
        normalized == "SENTRA BUYBACK" -> R.drawable.ic_vendor_sentra_buyback
        normalized == "UBS" -> R.drawable.ic_vendor_ubs
        normalized == "UBS ANNA" -> R.drawable.ic_vendor_ubs_anna
        normalized == "UBS DISNEY" -> R.drawable.ic_vendor_ubs_disney
        normalized == "UBS ELSA" -> R.drawable.ic_vendor_ubs_elsa
        normalized == "UBS HELLO KITTY" -> R.drawable.ic_vendor_ubs_hello_kitty
        normalized == "UBS MICKEY FULLBODY" -> R.drawable.ic_vendor_ubs_mickey_fullbody
        else -> null
    }
}

/** Returns a clean display name from a vendor unit string. */
fun vendorDisplayName(vendorUnit: String): String {
    return vendorUnit.replace(Regex("(?i)gram\\s*-\\s*"), "").trim()
}
