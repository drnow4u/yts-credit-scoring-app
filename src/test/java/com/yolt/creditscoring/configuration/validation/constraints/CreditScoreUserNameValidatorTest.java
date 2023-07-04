package com.yolt.creditscoring.configuration.validation.constraints;

import com.yolt.creditscoring.TestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.yolt.creditscoring.TestUtils.USER_NAME_EXACTLY_256;
import static org.assertj.core.api.BDDAssertions.then;

class CreditScoreUserNameValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"User Name", "John van Doe", "Józef Brzęczyszczykiewicz", "D'Hondt", "Jong-a-Pin", "Stoové", "Van 't Schip", USER_NAME_EXACTLY_256})
    void thatUserNameIsValid(String name) {
        // Given
        CreditScoreUserNameValidator creditScoreUserNameValidator = new CreditScoreUserNameValidator();
        // When
        boolean valid = creditScoreUserNameValidator.isValid(name, null);
        // Then
        then(valid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"X", "<h1>John Doe</h1>", " ", "John Doe ", " John Doe", TestUtils.USER_NAME_LONGER_256})
    void thatUserNameIsInvalid(String name) {
        // Given
        CreditScoreUserNameValidator creditScoreUserNameValidator = new CreditScoreUserNameValidator();
        // When
        boolean valid = creditScoreUserNameValidator.isValid(name, null);
        // Then
        then(valid).isFalse();
    }

}