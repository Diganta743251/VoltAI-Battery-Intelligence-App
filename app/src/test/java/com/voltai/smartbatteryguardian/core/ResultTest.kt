package com.voltai.smartbatteryguardian.core

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

class ResultTest {

    @Test
    fun `Success result should contain data`() {
        val data = "test data"
        val result = Result.Success(data)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.isError).isFalse()
        assertThat(result.isLoading).isFalse()
        assertThat(result.isEmpty).isFalse()
        assertThat(result.getOrNull()).isEqualTo(data)
        assertThat(result.getOrDefault("default")).isEqualTo(data)
    }

    @Test
    fun `Error result should contain error information`() {
        val exception = RuntimeException("Test error")
        val result = Result.Error(
            exception = exception,
            errorCode = "TEST_ERROR",
            userFriendlyMessage = "Something went wrong"
        )

        assertThat(result.isSuccess).isFalse()
        assertThat(result.isError).isTrue()
        assertThat(result.isLoading).isFalse()
        assertThat(result.isEmpty).isFalse()
        assertThat(result.getOrNull()).isNull()
        assertThat(result.getOrDefault("default")).isEqualTo("default")
        assertThat(result.errorCode).isEqualTo("TEST_ERROR")
        assertThat(result.userFriendlyMessage).isEqualTo("Something went wrong")
    }

    @Test
    fun `Loading result should have correct state`() {
        val result = Result.Loading

        assertThat(result.isSuccess).isFalse()
        assertThat(result.isError).isFalse()
        assertThat(result.isLoading).isTrue()
        assertThat(result.isEmpty).isFalse()
        assertThat(result.getOrNull()).isNull()
        assertThat(result.getOrDefault("default")).isEqualTo("default")
    }

    @Test
    fun `Empty result should have correct state`() {
        val result = Result.Empty

        assertThat(result.isSuccess).isFalse()
        assertThat(result.isError).isFalse()
        assertThat(result.isLoading).isFalse()
        assertThat(result.isEmpty).isTrue()
        assertThat(result.getOrNull()).isNull()
        assertThat(result.getOrDefault("default")).isEqualTo("default")
    }

    @Test
    fun `getOrThrow should return data for Success`() {
        val data = "test data"
        val result = Result.Success(data)

        assertThat(result.getOrThrow()).isEqualTo(data)
    }

    @Test(expected = RuntimeException::class)
    fun `getOrThrow should throw exception for Error`() {
        val exception = RuntimeException("Test error")
        val result = Result.Error(exception)

        result.getOrThrow()
    }

    @Test(expected = IllegalStateException::class)
    fun `getOrThrow should throw IllegalStateException for Loading`() {
        val result = Result.Loading

        result.getOrThrow()
    }

    @Test(expected = NoSuchElementException::class)
    fun `getOrThrow should throw NoSuchElementException for Empty`() {
        val result = Result.Empty

        result.getOrThrow()
    }

    @Test
    fun `map should transform Success data`() {
        val result = Result.Success(5)
        val mapped = result.map { it * 2 }

        assertThat(mapped).isInstanceOf(Result.Success::class.java)
        assertThat(mapped.getOrNull()).isEqualTo(10)
    }

    @Test
    fun `map should preserve Error state`() {
        val exception = RuntimeException("Test error")
        val result = Result.Error(exception)
        val mapped = result.map { it.toString() }

        assertThat(mapped).isInstanceOf(Result.Error::class.java)
        assertThat((mapped as Result.Error).exception).isEqualTo(exception)
    }

    @Test
    fun `map should preserve Loading state`() {
        val result = Result.Loading
        val mapped = result.map { it.toString() }

        assertThat(mapped).isEqualTo(Result.Loading)
    }

    @Test
    fun `map should preserve Empty state`() {
        val result = Result.Empty
        val mapped = result.map { it.toString() }

        assertThat(mapped).isEqualTo(Result.Empty)
    }

    @Test
    fun `map should handle exceptions in transform`() {
        val result = Result.Success(5)
        val mapped = result.map<Int> { throw RuntimeException("Transform error") }

        assertThat(mapped).isInstanceOf(Result.Error::class.java)
        assertThat((mapped as Result.Error).exception.message).isEqualTo("Transform error")
    }

    @Test
    fun `flatMap should chain Success results`() {
        val result = Result.Success(5)
        val flatMapped = result.flatMap { Result.Success(it * 2) }

        assertThat(flatMapped).isInstanceOf(Result.Success::class.java)
        assertThat(flatMapped.getOrNull()).isEqualTo(10)
    }

    @Test
    fun `flatMap should chain to Error`() {
        val result = Result.Success(5)
        val exception = RuntimeException("Flat map error")
        val flatMapped = result.flatMap<Int> { Result.Error(exception) }

        assertThat(flatMapped).isInstanceOf(Result.Error::class.java)
        assertThat((flatMapped as Result.Error).exception).isEqualTo(exception)
    }

    @Test
    fun `flatMap should preserve Error state`() {
        val exception = RuntimeException("Original error")
        val result = Result.Error(exception)
        val flatMapped = result.flatMap { Result.Success(it * 2) }

        assertThat(flatMapped).isInstanceOf(Result.Error::class.java)
        assertThat((flatMapped as Result.Error).exception).isEqualTo(exception)
    }

    @Test
    fun `onSuccess should execute action for Success`() {
        var executed = false
        val result = Result.Success(5)

        result.onSuccess { executed = true }

        assertThat(executed).isTrue()
    }

    @Test
    fun `onSuccess should not execute action for Error`() {
        var executed = false
        val result = Result.Error(RuntimeException("Error"))

        result.onSuccess { executed = true }

        assertThat(executed).isFalse()
    }

    @Test
    fun `onError should execute action for Error`() {
        var executed = false
        val result = Result.Error(RuntimeException("Error"))

        result.onError { executed = true }

        assertThat(executed).isTrue()
    }

    @Test
    fun `onError should not execute action for Success`() {
        var executed = false
        val result = Result.Success(5)

        result.onError { executed = true }

        assertThat(executed).isFalse()
    }

    @Test
    fun `onLoading should execute action for Loading`() {
        var executed = false
        val result = Result.Loading

        result.onLoading { executed = true }

        assertThat(executed).isTrue()
    }

    @Test
    fun `onLoading should not execute action for Success`() {
        var executed = false
        val result = Result.Success(5)

        result.onLoading { executed = true }

        assertThat(executed).isFalse()
    }

    @Test
    fun `safeCall should return Success for successful operation`() {
        val result = safeCall { "success" }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(result.getOrNull()).isEqualTo("success")
    }

    @Test
    fun `safeCall should return Error for failed operation`() {
        val exception = RuntimeException("Test error")
        val result = safeCall<String> { throw exception }

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isEqualTo(exception)
    }

    @Test
    fun `safeCall should use custom error code and message`() {
        val result = safeCall<String>(
            errorCode = "CUSTOM_ERROR",
            userFriendlyMessage = "Custom message"
        ) {
            throw RuntimeException("Internal error")
        }

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        assertThat(error.errorCode).isEqualTo("CUSTOM_ERROR")
        assertThat(error.userFriendlyMessage).isEqualTo("Custom message")
    }

    @Test
    fun `safeSuspendCall should return Success for successful operation`() = runTest {
        val result = safeSuspendCall { "success" }

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(result.getOrNull()).isEqualTo("success")
    }

    @Test
    fun `safeSuspendCall should return Error for failed operation`() = runTest {
        val exception = RuntimeException("Test error")
        val result = safeSuspendCall<String> { throw exception }

        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isEqualTo(exception)
    }

    @Test
    fun `Flow asResult should emit Loading then Success`() = runTest {
        val flow = flowOf("data").asResult()

        flow.test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)

            val successItem = awaitItem()
            assertThat(successItem).isInstanceOf(Result.Success::class.java)
            assertThat(successItem.getOrNull()).isEqualTo("data")

            awaitComplete()
        }
    }

    @Test
    fun `Flow asResult should emit Loading then Error on exception`() = runTest {
        val exception = RuntimeException("Flow error")
        val flow = flow<String> { throw exception }.asResult()

        flow.test {
            assertThat(awaitItem()).isEqualTo(Result.Loading)

            val errorItem = awaitItem()
            assertThat(errorItem).isInstanceOf(Result.Error::class.java)
            assertThat((errorItem as Result.Error).exception).isEqualTo(exception)

            awaitComplete()
        }
    }

    @Test
    fun `getDefaultUserMessage should return appropriate messages`() {
        assertThat(getDefaultUserMessage(UnknownHostException()))
            .isEqualTo("No internet connection available")

        assertThat(getDefaultUserMessage(java.net.SocketTimeoutException()))
            .isEqualTo("Connection timeout. Please try again")

        assertThat(getDefaultUserMessage(IOException()))
            .isEqualTo("Network error occurred. Please check your connection")

        assertThat(getDefaultUserMessage(SecurityException()))
            .isEqualTo("Permission denied. Please check app permissions")

        assertThat(getDefaultUserMessage(IllegalArgumentException()))
            .isEqualTo("Invalid input provided")

        assertThat(getDefaultUserMessage(IllegalStateException()))
            .isEqualTo("App is in an invalid state. Please restart")

        assertThat(getDefaultUserMessage(RuntimeException()))
            .isEqualTo("An unexpected error occurred. Please try again")
    }

    @Test
    fun `combine should merge two Success results`() {
        val result1 = Result.Success(5)
        val result2 = Result.Success("test")
        val combined = result1.combine(result2) { num, str -> "$str: $num" }

        assertThat(combined).isInstanceOf(Result.Success::class.java)
        assertThat(combined.getOrNull()).isEqualTo("test: 5")
    }

    @Test
    fun `combine should return first Error`() {
        val error1 = RuntimeException("Error 1")
        val result1 = Result.Error(error1)
        val result2 = Result.Success("test")
        val combined = result1.combine(result2) { _, str -> str }

        assertThat(combined).isInstanceOf(Result.Error::class.java)
        assertThat((combined as Result.Error).exception).isEqualTo(error1)
    }

    @Test
    fun `combine should return second Error if first is Success`() {
        val error2 = RuntimeException("Error 2")
        val result1 = Result.Success(5)
        val result2 = Result.Error(error2)
        val combined = result1.combine(result2) { num, _ -> num }

        assertThat(combined).isInstanceOf(Result.Error::class.java)
        assertThat((combined as Result.Error).exception).isEqualTo(error2)
    }

    @Test
    fun `combine should return Loading if either is Loading`() {
        val result1 = Result.Loading
        val result2 = Result.Success("test")
        val combined = result1.combine(result2) { _, str -> str }

        assertThat(combined).isEqualTo(Result.Loading)
    }

    @Test
    fun `toResult should return Success for non-null value`() {
        val value = "test"
        val result = value.toResult()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat(result.getOrNull()).isEqualTo(value)
    }

    @Test
    fun `toResult should return Error for null value`() {
        val value: String? = null
        val result = value.toResult("Custom error message")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val error = result as Result.Error
        assertThat(error.exception.message).isEqualTo("Custom error message")
        assertThat(error.userFriendlyMessage).isEqualTo("Required data is missing")
    }

    @Test
    fun `AppError types should have correct properties`() {
        val networkError = AppError.NetworkError("Network failed", 404)
        assertThat(networkError.errorCode).isEqualTo("NETWORK_ERROR")
        assertThat(networkError.userFriendlyMessage).isEqualTo("Network connection problem")
        assertThat(networkError.recoverable).isTrue()
        assertThat(networkError.statusCode).isEqualTo(404)

        val dbError = AppError.DatabaseError("Query failed", "SELECT * FROM table")
        assertThat(dbError.errorCode).isEqualTo("DATABASE_ERROR")
        assertThat(dbError.userFriendlyMessage).isEqualTo("Data storage problem")
        assertThat(dbError.recoverable).isTrue()

        val validationError = AppError.ValidationError("Invalid email", "email")
        assertThat(validationError.errorCode).isEqualTo("VALIDATION_ERROR")
        assertThat(validationError.userFriendlyMessage).isEqualTo("Invalid input provided")
        assertThat(validationError.recoverable).isTrue()

        val permissionError = AppError.PermissionError("Location denied", "ACCESS_FINE_LOCATION")
        assertThat(permissionError.errorCode).isEqualTo("PERMISSION_ERROR")
        assertThat(permissionError.userFriendlyMessage).isEqualTo("Permission required to continue")
        assertThat(permissionError.recoverable).isTrue()

        val securityError = AppError.SecurityError("Auth failed", "Invalid token")
        assertThat(securityError.errorCode).isEqualTo("SECURITY_ERROR")
        assertThat(securityError.userFriendlyMessage).isEqualTo("Security verification failed")
        assertThat(securityError.recoverable).isFalse()

        val businessError = AppError.BusinessLogicError("Invalid operation", "transfer")
        assertThat(businessError.errorCode).isEqualTo("BUSINESS_LOGIC_ERROR")
        assertThat(businessError.userFriendlyMessage).isEqualTo("Operation cannot be completed")
        assertThat(businessError.recoverable).isTrue()

        val unknownError = AppError.UnknownError()
        assertThat(unknownError.errorCode).isEqualTo("UNKNOWN_ERROR")
        assertThat(unknownError.userFriendlyMessage).isEqualTo("Something went wrong")
        assertThat(unknownError.recoverable).isTrue()
    }
}
