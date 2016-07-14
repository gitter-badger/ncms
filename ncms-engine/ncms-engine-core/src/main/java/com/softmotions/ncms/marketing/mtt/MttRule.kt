package com.softmotions.ncms.marketing.mtt

import java.io.Serializable
import java.util.*

/**
 * Правило фильтрации входящих запросов
 * и контекстное выполнение действий
 *
 * @author Tyutyunkov Vyacheslav (tve@softmotions.com)
 */
data class MttRule(var id: Long = 0,
                   var name: String? = null,
                   var description: String? = null,
                   var ordinal: Long? = null, // в правилах может быть важен порядок
                   var cdate: Date? = null,
                   var mdate: Date? = null,
                   var enabled: Boolean? = true,
                   var flags: Long = 0 // флаги режима работы правила
) : Serializable {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    constructor(id: java.lang.Long,
                name: java.lang.String,
                description: java.lang.String,
                ordinal: java.lang.Long,
                cdate: java.sql.Timestamp,
                mdate: java.sql.Timestamp,
                enabled: java.lang.Boolean,
                flags: java.lang.Long)
    : this(id.toLong(), name.toString(), description.toString(), ordinal.toLong(),
            Date(cdate.time), Date(mdate.time),
            enabled.booleanValue(), flags.toLong()) {
    }
}