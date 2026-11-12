package app.sandy.improv.domain

sealed class SendAction {
    data object Ignore : SendAction()
    data object Retry : SendAction()
    data class Send(val text: String) : SendAction()
}

fun sendAction(composer: String, lastRole: Role?, lastFailed: Boolean): SendAction {
    val t = composer.trim()
    if (t.isEmpty()) {
        return if (lastRole == Role.YOU && lastFailed) SendAction.Retry else SendAction.Ignore
    }
    return SendAction.Send(t)
}
