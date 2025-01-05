package io.tonglink.app.config.cache

enum class RedisKey (val key: String) {
    TONGLINK_HOME("TongLinkHomeModel"),
    TONGLINK_RANK("TongLinkRankModel");
    /*WEB_MAIN_COLLECTION("WebMainCollectionModel"),
    WEB_MAIN_SERVICE("WebMainServiceSettingModel"),
    WEB_MAIN_PROFILE("WebMainProfileModel"),

    WEB_COLLECTION_DETAIL("WebCollectionDetailModel"),
    WEB_COLLECTION_NOTICE("WebCollectionNoticeModel"),
    WEB_COLLECTION_BOX_DETAIL("WebCollectionBoxDetailModel"),
    WEB_COLLECTION_BOX_NOTICE("WebCollectionBoxNoticeModel"),
    WEB_COLLECTION_REPLY("WebCollectionReplyModel"),

    WEB_MAIN_COLLECTION_DEFAULT_VALUE("all:10"),

    WEB_RANK_LIST("WebRankList"),
    WEB_RANK_USER_LIST("WebRankUserList"),

    CONFIG("Config"),
    CONFIG_MAINTENANCE_USER("ConfigMaintenanceUser");*/

    companion object {
        fun fromKey(key: String): RedisKey? {
            return values().find { it.key == key }
        }
    }
}