package ovh.snet.grzybek.aocslack

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class LeaderBoardService(
    private val leaderboardClient: LeaderboardClient, private val slackNotifier: SlackNotifier,
    @Value("\${aoc.slack.only-active-users:false}")
    private val onlyActiveUsers: Boolean,
    @Value("\${aoc.slack.use-stars:false}")
    private val useStars: Boolean,
) {

    @Scheduled(cron = "\${aoc.slack.leaderboard.cron:0 0 5 * * ?}")
    fun notifyCurrentLeaderBoard() {
        val newLeaderBoard = leaderboardClient.getLeaderBoard()
        var ranking = newLeaderBoard.getSortedMembers(useStars)

        if (onlyActiveUsers) {
            ranking = ranking.filter { it.localScore > 0 }
        }

        val text = "*Current leaderboard:*\n\n" + ranking.mapIndexed { index, member ->
            member.getMessage(index + 1, useStars)
        }.joinToString("\n")
        slackNotifier.sendSlackMessage(text)
    }
}
