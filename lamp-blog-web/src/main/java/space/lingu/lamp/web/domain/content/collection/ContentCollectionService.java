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

package space.lingu.lamp.web.domain.content.collection;

import space.lingu.lamp.web.domain.content.ContentAccessCredentials;
import space.lingu.lamp.web.domain.content.ContentDetails;
import space.lingu.lamp.web.domain.content.ContentMetadataDetails;
import tech.rollw.common.web.page.Page;
import tech.rollw.common.web.page.Pageable;

import java.util.List;

/**
 *
 * @deprecated moved to {@link ContentCollectionProviderFactory}
 * @author RollW
 */
@Deprecated
public interface ContentCollectionService {
    Page<ContentMetadataDetails<? extends ContentDetails>> accessContentsRelated(
            ContentCollectionType collectionType,
            long contentCollectionId,
            ContentAccessCredentials contentAccessCredentials,
            Pageable pageable);

    Page<ContentMetadataDetails<? extends ContentDetails>> getContentsRelated(
            ContentCollectionType contentCollectionType,
            long contentCollectionId,
            Pageable pageable);

    List<ContentMetadataDetails<? extends ContentDetails>> getContentsRelated(
            ContentCollectionType contentCollectionType,
            long contentCollectionId);
}
