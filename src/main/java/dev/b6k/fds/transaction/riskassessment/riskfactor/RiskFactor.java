package dev.b6k.fds.transaction.riskassessment.riskfactor;

import dev.b6k.fds.transaction.riskassessment.Score;
import lombok.Builder;

import java.util.Objects;

@Builder
public record RiskFactor(Score score, Weight weight, Description description) {
    public RiskFactor {
        Objects.requireNonNull(score, "Score must not be null");
        Objects.requireNonNull(weight, "Weight must not be null");
        Objects.requireNonNull(description, "Description must not be null");
    }

    public double getWeightedScore() {
        return score.value() * weight.value();
    }

    public record Weight(double value) {
        public Weight {
            if (value < 0 || value > 1) {
                throw new IllegalArgumentException("Weight must be between 0 and 1");
            }
        }
    }

    @Builder
    public record Description(String code, String message) {
        public Description {
            Objects.requireNonNull(code, "Code must not be null");
            Objects.requireNonNull(message, "Message must not be null");
        }
    }
}