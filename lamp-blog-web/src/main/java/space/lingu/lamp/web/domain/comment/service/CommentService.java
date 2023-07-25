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

package space.lingu.lamp.web.domain.comment.service;

import org.springframework.stereotype.Service;
import space.lingu.NonNull;
import space.lingu.lamp.web.common.ParameterFailedException;
import space.lingu.lamp.web.common.ParameterMissingException;
import space.lingu.lamp.web.domain.comment.Comment;
import space.lingu.lamp.web.domain.comment.CommentDetailsMetadata;
import space.lingu.lamp.web.domain.comment.repository.CommentRepository;
import space.lingu.lamp.web.domain.content.*;
import space.lingu.lamp.web.domain.content.collection.ContentCollectionAccessor;
import space.lingu.lamp.web.domain.content.collection.ContentCollectionType;
import space.lingu.lamp.web.domain.content.common.ContentException;
import tech.rollw.common.web.page.Offset;
import tech.rollw.common.web.page.Page;
import tech.rollw.common.web.page.Pageable;

import java.util.List;

/**
 * @author RollW
 */
@Service
public class CommentService implements ContentAccessor,
        ContentPublisher, ContentCollectionAccessor {
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public Comment getContent(String contentId,
                              ContentType contentType) {
        long id = fromString(contentId);
        return commentRepository.getById(id);
    }

    @Override
    public ContentDetails publish(@NonNull UncreatedContent uncreatedContent,
                                  long timestamp)
            throws ContentException {
        ContentDetailsMetadata detailsMetadata =
                uncreatedContent.getMetadata();
        if (!(detailsMetadata instanceof CommentDetailsMetadata commentDetailsMetadata)) {
            throw new IllegalArgumentException("Metadata was not been serialized as comment metadata.");
        }
        Comment.Builder builder = Comment
                .builder()
                .setType(commentDetailsMetadata.contentType())
                .setContent(uncreatedContent.getContent())
                .setUserId(uncreatedContent.getUserId())
                .setParentId(commentDetailsMetadata.parentId())
                .setCreateTime(timestamp)
                .setCommentOn(commentDetailsMetadata.contentId());
        Comment comment = builder.build();
        long id = commentRepository.insert(comment);
        builder.setId(id);

        return builder.build();
    }

    @Override
    public Page<? extends ContentDetails> getContentCollection(
            ContentCollectionType contentCollectionType,
            String collectionId, Pageable pageable) {
        // TODO: collection
        return Page.of();
        //return getComment(contentCollectionType, collectionId, page, size);
    }

    @Override
    public List<? extends ContentDetails> getContentCollection(
            ContentCollectionType contentCollectionType,
            String collectionId) {
        return getComment(
                contentCollectionType,
                collectionId);
    }

    private List<Comment> getComment(ContentCollectionType contentCollectionType,
                                     String collectionId, int page, int size) {
        Offset offset = Offset.of(page, size);

        return switch (contentCollectionType) {
            case COMMENTS -> commentRepository.get(offset);
            case ARTICLE_COMMENTS -> commentRepository.getArticleComments(collectionId, offset);
            default -> throw new UnsupportedOperationException("Unsupported collection type: " + contentCollectionType);
        };
    }

    private List<Comment> getComment(ContentCollectionType contentCollectionType,
                                     String collectionId) {
        return switch (contentCollectionType) {
            case COMMENTS -> commentRepository.get();
            case ARTICLE_COMMENTS -> commentRepository.getArticleComments(collectionId);
            default -> throw new UnsupportedOperationException("Unsupported collection type: " + contentCollectionType);
        };
    }

    @Override
    public boolean supports(ContentType contentType) {
        return contentType == ContentType.COMMENT;
    }

    @NonNull
    private static Long fromString(String s) {
        if (s == null) {
            throw new ParameterMissingException("commentId");
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new ParameterFailedException("Invalid comment id: {}", s);
        }
    }
}
