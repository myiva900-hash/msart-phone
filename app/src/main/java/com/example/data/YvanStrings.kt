package com.example.data

enum class YvanLang(val code: String, val label: String) {
    RW("rw", "Kinyarwanda"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String): YvanLang = entries.firstOrNull { it.code == code } ?: RW
    }
}

class YvanStrings(private val lang: YvanLang) {
    val isRw: Boolean get() = lang == YvanLang.RW

    fun t(rw: String, en: String): String = if (isRw) rw else en

    val bootSubtitle get() = t(
        "Telefoni ya 2 • Sisitemu ya Root • Ububiko 16 GB",
        "Phone 2 OS • Rooted Kernel • 16 GB Partition"
    )
    val skipIntro get() = t("Simbuka", "Skip")
    val enterPhone2 get() = t("Injira muri Phone 2", "Enter Phone 2")
    val swipeOrTapUnlock get() = t("Kanda hano ufungure Telefoni", "Tap to Unlock Phone 2")
    val enterPinPrompt get() = t("Andika PIN ya Phone 2", "Enter Phone 2 PIN")
    val wrongPin get() = t("PIN si yo, ongera ugerageze", "Incorrect PIN, try again")
    val rootActive get() = t("ROOT IFUNGUYE (# su)", "ROOT ACTIVE (# su)")
    val rootDisabled get() = t("ROOT IFUNZE", "ROOT LOCKED")
    val storage16Gb get() = t("Ububiko 16 GB", "16 GB Storage")
    val usedLabel get() = t("Byakoreshejwe", "Used")
    val freeLabel get() = t("Bisigaye", "Free")
    val searchApps get() = t("Shakisha app muri Phone 2...", "Search apps in Phone 2...")
    val allApps get() = t("App Zose", "All Apps")
    val quickSettings get() = t("Igenamiterere Ryihuse", "Quick Settings")
    val recentApps get() = t("App Zifunguye", "Recent Apps")
    val noRecentApps get() = t("Nta app irafungurwa vuba", "No recent apps open")
    val clearAll get() = t("Funga Zose", "Clear All")
    val rebootIntro get() = t("Subiramo Boot Video", "Reboot Intro")
    val powerMenu get() = t("Zimya / Subiramo", "Power Menu")
    val storageFullWarning get() = t(
        "Ububiko bwa 16 GB bwuzuye! Siba cache cyangwa ukuremo app zimwe.",
        "16 GB Storage is full! Wipe cache or uninstall some apps."
    )
    val installSuccess get() = t("Yashyizwe muri Phone 2!", "Installed in Phone 2!")
    val uninstallSuccess get() = t("Yakuwemo muri Phone 2", "Uninstalled from Phone 2")
}
