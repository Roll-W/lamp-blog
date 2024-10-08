/*
 * Copyright (C) 2023 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.lingu.lamp.setting.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import space.lingu.lamp.DataEntity
import space.lingu.lamp.setting.SystemSettingResourceKind
import tech.rollw.common.web.system.SystemResourceKind

/**
 * @author RollW
 */
@Entity
@Table(name = "system_setting", uniqueConstraints = [
    UniqueConstraint(columnNames = ["key"])
])
class SystemSettingDo(
    @Id
    @Column(name = "id")
    @GeneratedValue(GenerationType.IDENTITY)
    private var id: Long? = null,

    @Column(name = "key")
    var key: String = "",

    @Column(name = "value")
    var value: String? = null
) : DataEntity<Long> {
    override fun getId(): Long? = id

    fun setId(id: Long?) {
        this.id = id
    }

    override fun getCreateTime(): Long = 0

    override fun getUpdateTime(): Long = 0

    override fun getSystemResourceKind(): SystemResourceKind =
        SystemSettingResourceKind
}