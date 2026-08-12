// package com.hnigrowth.ai.llm;

// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.stereotype.Component;

// /**
//  * Deterministic, no-network stand-in for a real LLM. It simply echoes a
//  * templated completion so the rest of the pipeline works end-to-end today.
//  * Swap in a real provider by implementing {@link LLMProvider}.
//  */
// @Component
// // @ConditionalOnProperty(name = "app.ai.provider", havingValue = "rule-based", matchIfMissing = true)
// public class RuleBasedLLMProvider implements LLMProvider {

//     @Override
//     public String complete(String prompt) {
//         // A real provider would call an external model here. The rule-based
//         // engines build their own output, so this is used only as a fallback.
//         return "[rule-based completion]\n" + prompt;
//     }

//     @Override
//     public String name() {
//         return "rule-based";
//     }
// }


package com.hnigrowth.ai.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "rule-based",
        matchIfMissing = true
)
public class RuleBasedLLMProvider implements LLMProvider {

    @Override
    public String complete(String prompt) {
        return "[rule-based completion]\n" + prompt;
    }

    @Override
    public String name() {
        return "rule-based";
    }
}