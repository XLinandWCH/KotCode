package org.xlin.kotcode.com.kotcode

import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.prompt.dsl.prompt

val agentStrategy = strategy<String, String>("是否调用工具策略图") {
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTool()
    val nodeSendToolResult by nodeLLMSendToolResult()
    val compressHistory by nodeLLMCompressHistory<ReceivedToolResult>()

    edge(nodeStart forwardTo nodeSendInput)

    edge(
        (nodeSendInput forwardTo nodeFinish)
            .transformed { it }
            .onAssistantMessage { true }
    )

    edge(
        (nodeSendInput forwardTo nodeExecuteTool)
            .onToolCall { true }
    )
    edge(
        (nodeExecuteTool forwardTo compressHistory)
            .onCondition { llm.readSession { prompt.messages.size >10 } }
    )

    edge(
        (nodeExecuteTool forwardTo nodeSendToolResult)
            .onCondition { !llm.readSession { prompt.messages.size >10 } }
    )

    edge(
        (compressHistory forwardTo nodeSendToolResult)

    )


    edge(
        (nodeSendToolResult forwardTo nodeFinish)
            .transformed { it }
            .onAssistantMessage { true }
    )

    edge(
        (nodeSendToolResult forwardTo nodeExecuteTool)
            .onToolCall { true }
    )
}