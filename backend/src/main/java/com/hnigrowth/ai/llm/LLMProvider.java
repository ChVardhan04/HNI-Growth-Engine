package com.hnigrowth.ai.llm;

/**
 * Abstraction over a text-generation backend.
 *
 * Today the only implementation is {@link RuleBasedLLMProvider}. To go live
 * with a real model, add an OpenAiLLMProvider / GeminiLLMProvider / ClaudeLLMProvider
 * that implements this interface and select it via the {@code app.ai.provider}
 * property — no business/service code changes required.
 */
public interface LLMProvider {

    /** @return the generated completion for the given prompt. */
    String complete(String prompt);

    /** @return a stable identifier, e.g. "rule-based", "openai", "claude". */
    String name();
}
