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

package space.lingu.lamp.web.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import space.lingu.lamp.setting.ConfigProvider
import space.lingu.lamp.web.common.keys.DatabaseConfigKeys

/**
 * @author RollW
 */
@Configuration
@EnableJpaRepositories(value = ["space.lingu.lamp"])
@EntityScan(value = ["space.lingu.lamp"])
@EnableTransactionManagement
class DataSourceConfiguration(
    @Qualifier("localConfigProvider")
    private val configProvider: ConfigProvider
) {
    @Bean
    @Primary
    fun dataSourceProperties(): DataSourceProperties = DataSourceProperties().also {
        it.url = configProvider[DatabaseConfigKeys.DATABASE_URL]
        it.username = configProvider[DatabaseConfigKeys.DATABASE_USERNAME]
        it.password = configProvider[DatabaseConfigKeys.DATABASE_PASSWORD]
        it.driverClassName = "com.mysql.cj.jdbc.Driver"
    }
}