package com.example.ui_v01

data class MockResponse(
    val type: String,
    val message: String,
    val sourceUrl: String? = null
)

fun getMockResponse(question: String): MockResponse {
    return when (question) {

        "Mock general" -> MockResponse(
            type = "general",
            message = "This is a basic example of legal information."
        )

        "Mock custody" -> MockResponse(
            type = "follow_up",
            message = "Do you currently have a custody order?"
        )

        "Mock more info" -> MockResponse(
            type = "legal",
            message = "Here is a more detailed example of legal information.",
            sourceUrl = "https://courts.ca.gov/forms-rules/rules-court"
        )

        "Mock suggestion" -> MockResponse(
            type = "suggestion",
            message = "I would like to discuss our parenting schedule calmly."
        )

        else -> MockResponse(
            type = "general",
            message = "That's a good question. This is a mock response."
        )
    }
}
