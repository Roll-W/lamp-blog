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

package space.lingu.lamp.web.domain.content.event;

import org.springframework.context.ApplicationEvent;
import space.lingu.NonNull;
import space.lingu.Nullable;
import space.lingu.lamp.web.domain.content.Content;
import space.lingu.lamp.web.domain.content.ContentStatus;

/**
 * @author RollW
 */
public class ContentStatusEvent<C extends Content> extends ApplicationEvent {
    private final C content;
    @Nullable
    private final ContentStatus previousStatus;
    @NonNull
    private final ContentStatus currentStatus;

    public ContentStatusEvent(C content,
                              @Nullable ContentStatus previousStatus,
                              @NonNull ContentStatus currentStatus) {
        super(content);
        this.content = content;
        this.previousStatus = previousStatus;
        this.currentStatus = currentStatus;
    }

    public C getContent() {
        return content;
    }

    @Nullable
    public ContentStatus getPreviousStatus() {
        return previousStatus;
    }

    @NonNull
    public ContentStatus getCurrentStatus() {
        return currentStatus;
    }
}
