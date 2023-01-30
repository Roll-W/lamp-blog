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

package space.lingu.lamp.web.domain.content;

import space.lingu.lamp.web.domain.content.common.ContentException;

/**
 * @author RollW
 */
public interface ContentAccessService {
    /**
     * Get the details of the content with access check.
     *
     * @param contentId   the id of content
     * @param contentType the type of content
     * @param passes      the passes to access the content
     * @return the details of the content, may need to convert to the specific type.
     * @throws ContentException if the content is not found or the passes are not valid.
     */
    ContentDetails openContent(String contentId, ContentType contentType,
                               ContentAccessCredential... passes) throws ContentException;

    /**
     * Get the details of the content with no access check.
     *
     * @throws ContentException if the content is not found.
     * @see #openContent(String, ContentType, ContentAccessCredential...)
     */
    ContentDetails getContentDetails(String contentId, ContentType contentType)
            throws ContentException;

}
