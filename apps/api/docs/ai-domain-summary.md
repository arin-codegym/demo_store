# AI Domain Summary

This module owns the AI reply pipeline for `USER_AI` conversations and the
RAG document pipeline used by that reply flow.

## Runtime Reply Flow

1. A user sends a chat message.
2. `MessageCreatedAiListener` listens after the message transaction commits.
3. The listener ignores AI-generated messages and non-AI conversations.
4. `AiChatService` loads the user message, conversation, and recent history.
5. `AiResponseRouter` chooses one answer source in this order:
   - FAQ rule match
   - reusable answer cache
   - business facts
   - RAG document context
   - general LLM fallback
6. `AiMessageCommandService` stores the assistant message and publishes the
   normal chat message event.
7. `AiChatService` writes AI usage metadata for success or failure.

## RAG Import Flow

1. `RagAdminController` receives an admin upload.
2. `RagImportService` creates an import job.
3. `DocumentStorageService` stores the uploaded file.
4. One `DocumentTextExtractor` is selected by mime type or file name.
5. The extracted text is chunked by `TextChunker`.
6. `EmbeddingService` generates a vector for each chunk.
7. RAG document and chunk rows are written through MyBatis mappers.

## Package Roles

- `ai.chat`: orchestration from chat event to AI message.
- `ai.cache`: reusable answer cache for safe, generic questions.
- `ai.faq`: deterministic FAQ matching before LLM/RAG.
- `ai.business`: business fact classification and prompt input.
- `ai.prompt`: builds provider prompts from conversation history and context.
- `ai.general`: general LLM fallback.
- `ai.rag`: RAG import, retrieval, answer generation, and document handling.
- `ai.embedding`: shared embedding provider for import and retrieval.
- `ai.shared`: DTOs shared across multiple AI subdomains.

## Design Notes

- Keep MyBatis mappers as interfaces.
- Keep `DocumentTextExtractor` as an interface because multiple extractors are
  loaded as a list and selected at runtime.
- Prefer concrete service classes when there is only one implementation and no
  clear provider boundary.
- `AiResponseRouter` should remain the only place that decides answer source
  priority. Other services should gather data or execute a chosen path.
