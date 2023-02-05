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

package space.lingu.lamp.web.domain.review.service;

import space.lingu.lamp.web.domain.content.Content;
import space.lingu.lamp.web.domain.content.ContentType;
import space.lingu.lamp.web.domain.review.ReviewJob;
import space.lingu.lamp.web.domain.review.dto.ReviewInfo;

import java.util.List;

/**
 * @author RollW
 */
public interface ReviewService {
    ReviewInfo makeReview(long jobId, boolean passed, String reason);

    default ReviewJob assignReviewer(Content content) {
        return assignReviewer(content, false);
    }

    /**
     * Assign a reviewer to a content.
     *
     * @param allowAutoReview if false, disable {@link ReviewerAllocateService#AUTO_REVIEWER
     *                        auto reviewer} and force to assign a staff reviewer.
     */
    ReviewJob assignReviewer(Content content, boolean allowAutoReview);

    ReviewJob assignReviewer(String contentId, ContentType contentType,
                             boolean allowAutoReview);

    List<ReviewJob> getReviewJobs(long reviewerId);

    List<ReviewJob> getUnfinishedReviewJobs(long reviewerId);

    List<ReviewJob> getFinishedReviewJobs(long reviewerId);

    ReviewInfo getReviewInfo(String reviewContentId, ContentType contentType);

    ReviewInfo getReviewInfo(long jobId);
}