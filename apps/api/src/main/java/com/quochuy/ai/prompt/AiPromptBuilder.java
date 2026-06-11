package com.quochuy.ai.prompt;

import com.quochuy.ai.business.dto.AiBusinessContext;
import com.quochuy.ai.business.dto.AiBusinessPrompt;
import com.quochuy.ai.general.dto.GeneralPrompt;
import com.quochuy.ai.product.dto.ProductComparisonContext;
import com.quochuy.ai.product.dto.ProductComparisonPrompt;
import com.quochuy.ai.rag.dto.RagPrompt;
import com.quochuy.ai.rag.dto.RagRetrieveResult;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;

import java.util.List;

/**
 * Builds provider-ready prompts from chat history plus selected context.
 *
 * Router code decides the answer source; this builder only formats the prompt
 * for the chosen source.
 */
public interface AiPromptBuilder {
	AiBusinessPrompt buildBusinessPrompt(Conversation conversation, List<Message> recentMessages,
										 AiBusinessContext context);
	
	RagPrompt buildRagPrompt(Conversation conversation, List<Message> recentMessages,
							 RagRetrieveResult rag);
	
	GeneralPrompt buildGeneralPrompt(Conversation conversation, List<Message> recentMessages);
	
	ProductComparisonPrompt buildProductComparisonPrompt(Conversation conversation,
														 List<Message> recentMessages,
														 ProductComparisonContext context);
}
