package _Blog_Backend.dto;

public record SubscriptionResponse(
        boolean isSubscribed,
        long totalSubscribers
) {

}
