package org.xlin.kotcode.com.kotcode

import ai.koog.agents.chatMemory.feature.ChatMemory
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.message.Message

suspend fun  KotCode(){

    // 定义会话ID，同一个会话ID会共享历史上下文，不同ID会隔离历史
    val sessionId = "kotcode-default-session"

    val agent = AIAgent(
        promptExecutor = MultiLLMPromptExecutor(DeepSeekLLMClient(System.getenv("DEEPSEEK_API_KEY"))),
        llmModel = DeepSeekModels.DeepSeekChat,
        toolRegistry = toolRegistry,
        temperature = 0.5,
        systemPrompt = """
            你是一名编程助手，目标是帮助我们解决编码任务。  
            你可以使用文件系统工具来读取、列出和编辑文件。  
            如果需要使用工具，Koog会处理格式问题，直接调用函数即可。
        """.trimIndent(),
        strategy = agentStrategy,

    ){
        install(ChatMemory)

        handleEvents {
            onToolCallStarting { eventContext ->
                println("正在执行 ${eventContext.toolName} 工具")
            }
            onLLMCallCompleted { context ->
                // 只提取响应中的文本类型内容（过滤掉工具调用Call类型的响应）
                val textResponses = context.responses.filterIsInstance<Message.Assistant>()
                    .mapNotNull { it.content.takeIf { text -> text.isNotBlank() } }

                if (textResponses.isNotEmpty()) {
                    println(" ${textResponses.joinToString("\n")}")
                } else {
                    // 如果没有纯文本响应，打印工具调用信息（可选）
                    val toolCalls = context.responses.filterIsInstance<Message.Tool.Call>()
                    if (toolCalls.isNotEmpty()) {
                        println(" ${toolCalls.joinToString { it.tool }}")
                    }
                }
            }
        }
    }
    while (true){
        print(">>> ")
        val context = readlnOrNull()
        if (context == "exit") break
        if (context.isNullOrEmpty()) continue
        agent.run(agentInput = context,sessionId = sessionId)


    }
    println("Goodbye!")

}