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

package space.lingu.lamp.web.domain.review.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import space.lingu.NonNull;
import space.lingu.lamp.web.domain.content.ContentType;
import space.lingu.lamp.web.domain.review.ReviewJob;
import space.lingu.lamp.web.domain.review.ReviewStatus;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author RollW
 */
@Component
public class OnReviewStateChangeListener implements ApplicationListener<OnReviewStateChangeEvent> {
    private final Multimap<ContentType, ReviewStatusMarker> typeStatusMarkers = Multimaps.newSetMultimap(new EnumMap<>(ContentType.class), HashSet::new);

    public OnReviewStateChangeListener(List<ReviewStatusMarker> statusMarkers) {
        statusMarkers.forEach(marker -> {
            List<ContentType> types = marker.getSupportedReviewTypes();
            types.forEach(reviewType -> typeStatusMarkers.put(reviewType, marker));
        });
    }

    @Override
    @Async
    public void onApplicationEvent(@NonNull OnReviewStateChangeEvent event) {
        ReviewJob job = event.getReviewJob();
        Collection<ReviewStatusMarker> markers = typeStatusMarkers.get(job.getType());
        markers.forEach(m -> reviewStateChanged(m, job, event.getCurrentStatus()));
    }

    private void reviewStateChanged(ReviewStatusMarker marker, ReviewJob job, ReviewStatus status) {
        Preconditions.checkNotNull(status, "Review status cannot be null");

        switch (status) {
            case NOT_REVIEWED -> throw new IllegalStateException("Review job is not reviewed.");
            case REJECTED -> marker.markAsRejected(job.getType(), job.getReviewContentId(), job.getResult());
            case REVIEWED -> marker.markAsReviewed(job.getType(), job.getReviewContentId());
        }
    }
}