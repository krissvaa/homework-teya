package lv.kriss.demo.teya.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lv.kriss.demo.teya.exception.DuplicateBalanceException;
import lv.kriss.demo.teya.exception.InvalidTransactionException;
import lv.kriss.demo.teya.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404WithExceptionMessageAsDetail() {
        var ex = new ResourceNotFoundException("No account: 123");

        var problem = handler.handleNotFound(ex);

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getDetail()).isEqualTo("No account: 123");
    }

    @Test
    void handleInvalidTransaction_returns400WithExceptionMessageAsDetail() {
        var ex = new InvalidTransactionException("Insufficient funds on balance: 123");

        var problem = handler.handleInvalidTransaction(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Insufficient funds on balance: 123");
    }

    @Test
    void handleDuplicateBalance_returns409WithExceptionMessageAsDetail() {
        var ex = new DuplicateBalanceException("Account 123 already has a EUR balance");

        var problem = handler.handleDuplicateBalance(ex);

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getDetail()).isEqualTo("Account 123 already has a EUR balance");
    }

    @Test
    void handleValidation_returns400WithFieldErrorsMap() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "createTransactionRequest");
        bindingResult.addError(new FieldError("createTransactionRequest", "amount", "must be positive"));
        bindingResult.addError(new FieldError("createTransactionRequest", "currency", "must be a 3-letter ISO 4217 currency code"));
        var methodParameter = new MethodParameter(
                GlobalExceptionHandler.class.getDeclaredMethod("handleValidation", MethodArgumentNotValidException.class), 0);
        var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var problem = handler.handleValidation(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Validation failed");
        assertThat(problem.getProperties()).containsEntry("errors", Map.of(
                "amount", "must be positive",
                "currency", "must be a 3-letter ISO 4217 currency code"
        ));
    }

    @Test
    void handleValidation_fieldErrorsWithNullMessage_areExcludedFromErrorsMap() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "createAccountRequest");
        bindingResult.addError(new FieldError("createAccountRequest", "name", null));
        var methodParameter = new MethodParameter(
                GlobalExceptionHandler.class.getDeclaredMethod("handleValidation", MethodArgumentNotValidException.class), 0);
        var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var problem = handler.handleValidation(ex);

        assertThat(problem.getProperties()).containsEntry("errors", Map.of());
    }

    @Test
    void handleValidation_duplicateFieldErrors_keepsFirstMessage() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "createAccountRequest");
        bindingResult.addError(new FieldError("createAccountRequest", "name", "must not be blank"));
        bindingResult.addError(new FieldError("createAccountRequest", "name", "must not exceed 255 characters"));
        var methodParameter = new MethodParameter(
                GlobalExceptionHandler.class.getDeclaredMethod("handleValidation", MethodArgumentNotValidException.class), 0);
        var ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        var problem = handler.handleValidation(ex);

        assertThat(problem.getProperties()).containsEntry("errors", Map.of("name", "must not be blank"));
    }

    @Test
    void handleConstraintViolation_returns400WithErrorsMapKeyedByLastPathSegment() {
        var violation = constraintViolation("getTransactions.balanceId", "must not be blank");
        var ex = new ConstraintViolationException(setOf(violation));

        var problem = handler.handleConstraintViolation(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Validation failed");
        assertThat(problem.getProperties()).containsEntry("errors", Map.of("balanceId", "must not be blank"));
    }

    @Test
    void handleConstraintViolation_propertyPathWithoutDot_usesWholePathAsKey() {
        var violation = constraintViolation("balanceId", "must not be blank");
        var ex = new ConstraintViolationException(setOf(violation));

        var problem = handler.handleConstraintViolation(ex);

        assertThat(problem.getProperties()).containsEntry("errors", Map.of("balanceId", "must not be blank"));
    }

    @Test
    void handleConstraintViolation_multipleViolations_allPresentInErrorsMap() {
        var first = constraintViolation("getTransactions.balanceId", "must not be blank");
        var second = constraintViolation("getBalances.accountId", "must not be blank");
        var ex = new ConstraintViolationException(setOf(first, second));

        var problem = handler.handleConstraintViolation(ex);

        assertThat(problem.getProperties()).containsEntry("errors", Map.of(
                "balanceId", "must not be blank",
                "accountId", "must not be blank"
        ));
    }

    @Test
    void handleMalformedRequest_returns400WithGenericDetail_regardlessOfExceptionMessage() {
        var ex = new HttpMessageNotReadableException("JSON parse error at line 3", mock());

        var problem = handler.handleMalformedRequest(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Malformed request body");
    }

    @Test
    void handleIllegalArgument_returns400WithExceptionMessageAsDetail() {
        var ex = new IllegalArgumentException("Invalid currency code: XYZ123");

        var problem = handler.handleIllegalArgument(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).isEqualTo("Invalid currency code: XYZ123");
    }

    @Test
    void handleUnexpected_returns500WithoutLeakingExceptionMessage() {
        var ex = new RuntimeException("stack trace detail that should not reach the client");

        var problem = handler.handleUnexpected(ex);

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("Internal server error");
    }

    private static ConstraintViolation<?> constraintViolation(String propertyPath, String message) {
        var path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    private static Set<ConstraintViolation<?>> setOf(ConstraintViolation<?>... violations) {
        return new LinkedHashSet<>(Set.of(violations));
    }
}
