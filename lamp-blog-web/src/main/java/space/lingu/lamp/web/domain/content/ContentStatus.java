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

/**
 * @author RollW
 */
public enum ContentStatus {
    /**
     * Draft, invisible to the public. If the user deletes
     * draft content, it will be recycled for next use.
     */
    DRAFT,
    /**
     * Under reviewing.
     */
    REVIEWING,
    /**
     * Review rejected.
     */
    REVIEW_REJECTED,
    /**
     * Published, visible to public (if public visitable).
     */
    PUBLISHED,
    /**
     * Deleted, and invisible to author.
     */
    DELETED,
    /**
     * Hide, and invisible to author.
     */
    FORBIDDEN,
    /**
     * Hide, but visible to the author.
     */
    HIDE,
    ;

    public boolean isPublicVisitable() {
        return this == PUBLISHED;
    }

    public boolean needsReview() {
        return this == REVIEWING;
    }

    public boolean canRestore() {
        return this == DELETED || this == FORBIDDEN;
    }

}
