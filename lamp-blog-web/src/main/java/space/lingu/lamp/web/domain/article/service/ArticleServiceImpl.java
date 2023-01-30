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

package space.lingu.lamp.web.domain.article.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import space.lingu.NonNull;
import space.lingu.lamp.Result;
import space.lingu.lamp.web.common.DataErrorCode;
import space.lingu.lamp.web.domain.article.Article;
import space.lingu.lamp.web.domain.article.dto.ArticleInfo;
import space.lingu.lamp.web.domain.article.repository.ArticleRepository;
import space.lingu.lamp.web.domain.content.BasicContentInfo;
import space.lingu.lamp.web.domain.content.Content;
import space.lingu.lamp.web.domain.content.ContentAccessor;
import space.lingu.lamp.web.domain.content.ContentStatus;
import space.lingu.lamp.web.domain.content.ContentType;
import space.lingu.lamp.web.domain.content.common.ContentException;
import space.lingu.lamp.web.domain.content.event.ContentPublishEvent;
import space.lingu.lamp.web.domain.content.event.ContentStatusEvent;
import space.lingu.lamp.web.domain.content.event.PublishEventStage;
import space.lingu.lamp.web.domain.review.ReviewType;
import space.lingu.lamp.web.domain.review.event.ReviewStatusMarker;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author RollW
 */
@Service
public class ArticleServiceImpl implements ArticleService, ReviewStatusMarker, ContentAccessor<Article> {
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final ArticleRepository articleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ArticleServiceImpl(ArticleRepository articleRepository,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.articleRepository = articleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Result<ArticleInfo> publishArticle(long userId,
                                              @NotEmpty String title,
                                              String content) {
        if (articleRepository.isTitleExist(userId, title)) {
            return Result.of(DataErrorCode.ERROR_DATA_EXISTED);
        }
        long createTime = System.currentTimeMillis();
        Article.Builder articleBuilder = Article.builder()
                .setUserId(userId)
                .setTitle(title)
                .setContent(content)
                .setCreateTime(createTime)
                .setUpdateTime(createTime);
        Article article = articleBuilder.build();
        article = articleRepository.createArticle(article);
        ContentPublishEvent<Article> articlePublishEvent =
                new ContentPublishEvent<>(article, PublishEventStage.REVIEWING);
        applicationEventPublisher.publishEvent(articlePublishEvent);

        return Result.success(
                ArticleInfo.from(article)
        );
    }

    @Override
    public Result<Void> deleteArticle(long articleId) {
        if (!articleRepository.isArticleExist(articleId)) {
            return Result.of(DataErrorCode.ERROR_DATA_NOT_EXIST);
            // TODO: replace with ContentErrorCode
        }

        BasicContentInfo contentInfo = new BasicContentInfo(
                1,
                String.valueOf(articleId),
                ContentType.ARTICLE
        );
        ContentStatusEvent<Content> statusEvent =
                new ContentStatusEvent<>(contentInfo, null, ContentStatus.DELETED);
        applicationEventPublisher.publishEvent(statusEvent);
        return Result.success();
    }

    @Override
    public Result<Void> updateArticle(long articleId) {
        return Result.success();
    }

    @Override
    public Article getArticle(long articleId) {
        return articleRepository.get(articleId);
    }

    @Override
    public void markAsReviewed(ReviewType reviewType, String contentId) {
        long id = Long.parseLong(contentId);
        Article article = articleRepository.get(id);
        //articleRepository.updateArticleStatus(id, ContentStatus.PUBLISHED);
        ContentPublishEvent<Article> articlePublishEvent =
                new ContentPublishEvent<>(article, PublishEventStage.PUBLISHED);
        applicationEventPublisher.publishEvent(articlePublishEvent);
    }

    @Override
    public void markAsRejected(ReviewType reviewType, String contentId, String reason) {
        long id = Long.parseLong(contentId);

    }

    @NonNull
    @Override
    public List<ReviewType> getSupportedReviewTypes() {
        return List.of(ReviewType.ARTICLE);
    }

    @Override
    public Article getContent(String contentId, ContentType contentType) {
        if (contentType != ContentType.ARTICLE) {
            return null;
        }
        try {
            return articleRepository.get(Long.parseLong(contentId));
        } catch (NumberFormatException e) {
            throw new ContentException(e);
        }
    }

    @Override
    public boolean supports(ContentType contentType) {
        return contentType == ContentType.ARTICLE;
    }
}
