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

package space.lingu.lamp.web.database.dao;

import space.lingu.lamp.web.system.MessageResource;
import space.lingu.light.Dao;
import space.lingu.light.Delete;
import space.lingu.light.Query;
import tech.rollw.common.web.page.Offset;

import java.util.List;
import java.util.Locale;

/**
 * @author RollW
 */
@Dao
public interface MessageResourceDao extends AutoPrimaryBaseDao<MessageResource> {
    @Override
    @Query("SELECT * FROM message_resource WHERE id = {id}")
    MessageResource getById(long id);

    @Override
    @Query("SELECT * FROM message_resource WHERE id IN ({ids})")
    List<MessageResource> getByIds(List<Long> ids);

    @Override
    @Query("SELECT * FROM message_resource ORDER BY id DESC")
    List<MessageResource> get();

    @Override
    @Query("SELECT COUNT(*) FROM message_resource")
    int count();

    @Override
    @Query("SELECT * FROM message_resource ORDER BY id DESC LIMIT {offset.limit()} OFFSET {offset.offset()}")
    List<MessageResource> get(Offset offset);

    @Override
    default String getTableName() {
        return "message_resource";
    }

    @Delete("DELETE FROM message_resource WHERE `key` = {key}")
    void deleteByKey(String key);

    @Delete("DELETE FROM message_resource WHERE `key` = {key} AND locale = {locale}")
    void deleteByKey(String key, Locale locale);

    @Query("SELECT * FROM message_resource WHERE `key` = {key}")
    List<MessageResource> getByKey(String key);

    @Query("SELECT * FROM message_resource WHERE `key` = {key} AND locale = {locale}")
    MessageResource getByKeyAndLocale(String key, Locale locale);
}
