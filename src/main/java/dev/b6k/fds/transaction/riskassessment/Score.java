package dev.b6k.fds.transaction.riskassessment;

public record Score(int value) {
    public Score {
        if (value < 0) {
            throw new IllegalArgumentException("Score must be greater than or equal to 0");
        }
    }

    public static Score of(int value) {
        return new Score(value);
    }
}
