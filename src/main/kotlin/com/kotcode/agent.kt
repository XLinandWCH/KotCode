package org.xlin.kotcode.com.kotcode

import ai.koog.agents.chatMemory.feature.ChatMemory
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.agents.features.tracing.feature.Tracing
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.message.Message
import kotlin.time.ExperimentalTime

@ExperimentalTime
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
            你可以使用文件系统工具来读取、列出、编辑文件和PowerShell。
            关于PowerShell 工具，可以调用用户内置的python进行一些基础的运算等等，请熟练运用PowerShell的功能。
            如果需要使用工具，Koog会处理格式问题，直接调用函数即可。
        """.trimIndent(),
        strategy = agentStrategy,

    ){
        install(ChatMemory)
        install(Tracing)

        handleEvents {
            onToolCallStarting { eventContext ->
                println("正在执行 ${eventContext.toolName} 工具")
            }

//            // 新增：监听流式帧，实时输出增量内容
//            onLLMStreamingFrameReceived { eventContext ->
//                when (val frame = eventContext.streamFrame) {
//                    is StreamFrame.TextDelta -> {
//                        print(frame.text)
//                        System.out.flush() // 强制刷新缓冲区，保证实时显示
//                    }
//                    is StreamFrame.TextComplete -> {
//                        println() // 完整文本输出后换行
//                    }
//                    // 可选：如果需要显示推理过程，可添加如下处理
//                    is StreamFrame.ReasoningDelta -> {
//                        frame.text?.let { println("\n[推理过程] $it") }
//                    }
//                    else -> {}
//                }
//            }

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